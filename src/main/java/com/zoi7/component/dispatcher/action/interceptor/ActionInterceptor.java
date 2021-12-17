package com.zoi7.component.dispatcher.action.interceptor;

/**
 *
 * Action拦截器
 *
 * @author yjy
 * Created at 2021/3/20 3:10 下午
 */
public interface ActionInterceptor {

    /** 优先级最高 */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
    /** 优先级最低 */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * 排序 越小越靠前
     * @return 排序值
     */
    int order();

    /**
     * 是否需要拦截
     * @param module module
     * @param cmd cmd
     * @return 是否拦截
     */
    boolean needIntercept(String module, String cmd);

    /**
     * 前置拦截器
     * @param module module
     * @param cmd cmd
     * @param params 参数
     * @return 是否通过
     * @throws Exception e
     */
    boolean preAction(String module, String cmd, Object... params) throws Exception;

    /**
     * 后置拦截器
     * @param module module
     * @param cmd cmd
     * @param result 返回值
     * @param params 参数
     * @return result
     * @throws Exception e
     */
    Object postAction(String module, String cmd, Object result, Object... params) throws Exception;


}
