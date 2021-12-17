package com.zoi7.component.dispatcher.action.exception.handler;

public interface ActionExceptionHandler {

    /**
     * 异常处理
     * @param e 异常
     * @param module 映射ReqAction
     * @param cmd 映射ReqMethod
     * @param params 参数
     */
    void handle(Exception e, String module, String cmd, Object... params);

}
