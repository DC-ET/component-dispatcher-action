package com.zoi7.component.dispatcher.action.exception;

/**
 *
 * 请求找不到
 * @author yjy
 * 2018-06-05 17:16
 */
public class MethodNotFoundException extends RuntimeException {

    public MethodNotFoundException(String message) {
        super(message);
    }

}
