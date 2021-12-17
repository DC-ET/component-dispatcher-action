package com.zoi7.component.dispatcher.action;

import com.zoi7.component.dispatcher.action.annotation.ReqAction;
import com.zoi7.component.dispatcher.action.annotation.ReqHandlers;
import com.zoi7.component.dispatcher.action.annotation.ReqMethod;
import com.zoi7.component.dispatcher.action.annotation.ReqNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 初始化并装载所有请求方法至 分发器的 actionMap 中
 *
 * 依赖于Spring自动扫描Component, @ReqAction 的类必须在扫描范围内
 *
 * @author yjy
 * 2018-05-28 9:42
 */
public class DispatcherActionInitializer implements ApplicationContextAware {
    
    private static final Logger logger = LoggerFactory.getLogger(DispatcherActionInitializer.class);

    private final ActionDispatcher actionDispatcher;

    public DispatcherActionInitializer(ActionDispatcher actionDispatcher) {
        this.actionDispatcher = actionDispatcher;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.run(applicationContext);
    }

    private void run(ApplicationContext applicationContext) {
        logger.info("DispatcherActionInitializer start");
        final Map<ReqNames, ReqHandlers> actionMap = new HashMap<>();
        // 获取所有 带@ReqAction 注解的类
        Map<String, Object> actions = applicationContext.getBeansWithAnnotation(ReqAction.class);
        // 遍历所有处理器类
        Class<?> clazz;
        ReqNames reqNames;
        for (Object o : actions.values()) {
            clazz = o.getClass();
            ReqAction reqAction = AnnotationUtils.findAnnotation(clazz, ReqAction.class);
            if (reqAction == null) {
                continue;
            }
            String module = reqAction.value();
            // 遍历所有方法
            for (Method method : clazz.getMethods()) {
                // if 是处理器方法
                if (method.isAnnotationPresent(ReqMethod.class)) {
                    ReqMethod reqMethod = AnnotationUtils.findAnnotation(method, ReqMethod.class);
                    if (reqMethod == null) {
                        continue;
                    }
                    reqNames = new ReqNames(module, reqMethod.value());
                    if (actionMap.containsKey(reqNames)) {
                        // 已有相同的请求名, 抛出异常
                        throw new IllegalStateException("this ReqNames has already registered: "
                                + reqNames.getModule() + ":" + reqNames.getCmd());
                    }
                    // 存入处理器集合中
                    actionMap.put(reqNames, new ReqHandlers(o, method));
                    logger.info("ReqMethod > [ {} / {} ] registered.", reqNames.getModule(), reqNames.getCmd());
                }
            }
        }
        // 绑定至请求分发器上
        actionDispatcher.setHandlerMap(actionMap);
        logger.info("DispatcherActionInitializer finish");
    }

}
