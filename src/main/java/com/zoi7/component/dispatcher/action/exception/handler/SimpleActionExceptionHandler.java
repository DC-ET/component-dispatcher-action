package com.zoi7.component.dispatcher.action.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleActionExceptionHandler implements ActionExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleActionExceptionHandler.class);

    @Override
    public void handle(Exception e, String module, String cmd, Object... params) {
        log.error("ActionExceptionHandler exception, request params: {}", params, e);
    }

}
