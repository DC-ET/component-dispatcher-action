package com.zoi7.component.dispatcher.action;

import com.zoi7.component.dispatcher.action.annotation.ReqHandlers;
import com.zoi7.component.dispatcher.action.annotation.ReqNames;
import com.zoi7.component.dispatcher.action.exception.MethodNotFoundException;
import com.zoi7.component.dispatcher.action.exception.handler.ActionExceptionHandler;
import com.zoi7.component.dispatcher.action.interceptor.ActionInterceptor;
import com.zoi7.component.dispatcher.action.reject.ActionRejectPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 * 请求分发器
 * @author yjy
 * 2018-05-31 11:32
 */
public class ActionDispatcher implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ActionDispatcher.class);

    /** 分发器配置项 */
    private final DispatcherActionProperties dispatcherActionProperties;
    /** 逻辑处理线程池 */
    private final ThreadPoolExecutor workers;
    /** 逻辑处理线程池-单线程 */
    private final ThreadPoolExecutor syncWorkers;
    /** 所有action拦截器 */
    private final List<ActionInterceptor> actionInterceptorList = new ArrayList<>();
    /** 请求任务溢出时的拒绝策略 */
    private final ActionRejectPolicy actionRejectPolicy;
    /** 异常处理器 */
    private final ActionExceptionHandler actionExceptionHandler;
    /** 逻辑处理器集合 */
    private Map<ReqNames, ReqHandlers> handlerMap;

    private final CountDownLatch readyLatch = new CountDownLatch(1);

    /** ReqNames 对应 拦截器集合的缓存 */
    private static final Map<ReqNames, List<ActionInterceptor>> REQ_NAMES_INTERCEPTOR_MAP = new HashMap<>();

    ActionDispatcher(DispatcherActionProperties dispatcherActionProperties,
                     List<ActionInterceptor> actionInterceptorList,
                     ActionRejectPolicy actionRejectPolicy,
                     ActionExceptionHandler actionExceptionHandler) {
        this.dispatcherActionProperties = dispatcherActionProperties;
        if (actionInterceptorList != null && !actionInterceptorList.isEmpty()) {
            actionInterceptorList.sort(Comparator.comparingInt(ActionInterceptor::order));
            this.actionInterceptorList.addAll(actionInterceptorList);
        }
        this.actionRejectPolicy = actionRejectPolicy;
        this.actionExceptionHandler = actionExceptionHandler;
        // 初始化分发器线程池
        workers = new ThreadPoolExecutor(dispatcherActionProperties.getThreads(), dispatcherActionProperties.getThreads(),
                dispatcherActionProperties.getKeepAliveTime(), dispatcherActionProperties.getTimeUnit(),
                new LinkedBlockingQueue<>(dispatcherActionProperties.getAcceptRequest()),
                runnable -> new Thread(runnable, "action-dispatcher-pool1"),
                actionRejectPolicy::handle);
        workers.allowCoreThreadTimeOut(true);

        // 初始化分发器线程池-单线程
        syncWorkers = new ThreadPoolExecutor(1, 1,
                dispatcherActionProperties.getKeepAliveTime(), dispatcherActionProperties.getTimeUnit(),
                new LinkedBlockingQueue<>(dispatcherActionProperties.getAcceptRequest()),
                runnable -> new Thread(runnable, "action-dispatcher-pool2"),
                actionRejectPolicy::handle);
        workers.allowCoreThreadTimeOut(true);
    }

    /**
     * 处理请求的分发
     * @param module 映射ReqAction
     * @param cmd 映射ReqMethod
     * @param params 参数
     * @return Future of T
     */
    public <T> Future<T> doDispatcher(String module, String cmd, Object... params) {
        return doDispatcher0(module, cmd, false, params);
    }

    /**
     * 按顺序处理请求的分发
     * @param module 映射ReqAction
     * @param cmd 映射ReqMethod
     * @param params 参数
     * @return Future of T
     */
    public <T> Future<T> doDispatcherSync(String module, String cmd, Object... params) {
        return doDispatcher0(module, cmd, true, params);
    }

    private <T> Future<T> doDispatcher0(String module, String cmd, boolean sync, Object... params) {
        ThreadPoolExecutor pool = sync ? syncWorkers : workers;
        return pool.submit(() -> {
            try {
                // 检查初始化状态
                checkState();
                // 前置拦截
                if (!triggerPreAction(module, cmd, params)) {
                    return null;
                }
                // 逻辑处理
                T res = (T) invoke(module, cmd, params);
                // 后置拦截
                return triggerPostAction(module, cmd, res, params);
            } catch (Exception e) {
                this.actionExceptionHandler.handle(e, module, cmd, params);
                throw e;
            }
        });
    }

    /**
     * 触发前置拦截器
     * @param module module
     * @param cmd cmd
     * @param params 参数
     * @throws Exception e
     */
    private boolean triggerPreAction(String module, String cmd, Object... params) throws Exception {
        List<ActionInterceptor> interceptors = figureInterceptors(module, cmd);
        for (ActionInterceptor actionInterceptor : interceptors) {
            boolean flag = actionInterceptor.preAction(module, cmd, params);
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    /**
     * 触发后置拦截器
     * @param module module
     * @param cmd cmd
     * @param result 返回值
     * @param params 参数
     * @return result
     * @throws Exception e
     */
    private <T> T triggerPostAction(String module, String cmd, T result, Object... params) throws Exception {
        List<ActionInterceptor> interceptors = figureInterceptors(module, cmd);
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            ActionInterceptor actionInterceptor = interceptors.get(i);
            result = (T)actionInterceptor.postAction(module, cmd, result, params);
        }
        return result;
    }

    /**
     * 计算此请求的拦截器集合
     * @param module ..
     * @param cmd ..
     * @return 拦截器集合
     */
    private List<ActionInterceptor> figureInterceptors(String module, String cmd) {
        ReqNames reqNames = new ReqNames(module, cmd);
        // 直接获取内存结果，如果已存在则直接返回
        List<ActionInterceptor> interceptors = REQ_NAMES_INTERCEPTOR_MAP.get(reqNames);
        if (interceptors != null) {
            return interceptors;
        }
        // 根据条件匹配，并将结果存入内存
        interceptors = new ArrayList<>();
        for (ActionInterceptor actionInterceptor : this.actionInterceptorList) {
            if (actionInterceptor.needIntercept(module, cmd)) {
                interceptors.add(actionInterceptor);
            }
        }
        REQ_NAMES_INTERCEPTOR_MAP.put(reqNames, interceptors);
        return interceptors;
    }

    /**
     * 真正处理请求
     * @param module 请求模块
     * @param cmd 请求命令
     * @param params 参数
     * @return 返回值
     * @throws IllegalAccessException e1
     * @throws InvocationTargetException e2
     * @throws MethodNotFoundException e3
     */
    @SuppressWarnings("unchecked")
    private Object invoke(String module, String cmd, Object... params) throws InterruptedException,
            InvocationTargetException, IllegalAccessException {

        // if 处理器还未注册完成 > 抛出异常
        if (handlerMap == null) {
            throw new IllegalStateException("handlerMap is not ready yet");
        }
        // 根据请求 找到指定处理器并执行处理
        ReqNames reqNames = new ReqNames(module, cmd);
        ReqHandlers reqHandlers = handlerMap.get(reqNames);
        // if 未找到精确方法
        if (reqHandlers == null) {
            // 尝试匹配指定module中的默认的方法
            reqNames = new ReqNames(module, "");
            reqHandlers = handlerMap.get(reqNames);
            // if 未找到module中默认的方法
            if (reqHandlers == null) {
                // 尝试找到全局默认的方法
                reqNames = new ReqNames("", "");
                reqHandlers = handlerMap.get(reqNames);
            }
        }
        if (reqHandlers != null) {
            return reqHandlers.getMethod().invoke(reqHandlers.getTarget(), params);
        } else {
            throw new MethodNotFoundException("接口不存在, module: " + module + ", cmd: " + cmd);
        }
    }

    /**
     * 确认是否初始化完成, 未完成则等待
     */
    private void checkState() throws InterruptedException {
        if (readyLatch.getCount() > 0) {
            logger.info("Waiting ActionDispatcher ready");
            boolean success = readyLatch.await(dispatcherActionProperties.getWaitInitTimeout(), TimeUnit.SECONDS);
            if (success) {
                logger.info("ActionDispatcher is ready");
            } else {
                logger.error("checkReady failed, cause readyLatch.await failed");
            }
        }
    }

    public Map<ReqNames, ReqHandlers> getHandlerMap() {
        return handlerMap;
    }

    void setHandlerMap(Map<ReqNames, ReqHandlers> handlerMap) {
        this.handlerMap = handlerMap;
        this.readyLatch.countDown();
    }

    public ActionRejectPolicy getActionRejectPolicy() {
        return actionRejectPolicy;
    }

    public DispatcherActionProperties getActionConfigBean() {
        return dispatcherActionProperties;
    }

    @Override
    public void destroy() throws Exception {
        this.workers.shutdown();
    }
}
