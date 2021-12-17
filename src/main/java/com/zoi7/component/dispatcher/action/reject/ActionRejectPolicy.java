package com.zoi7.component.dispatcher.action.reject;

import java.util.concurrent.ExecutorService;

@FunctionalInterface
public interface ActionRejectPolicy {

    /**
     * 处理请求溢出的场景
     * @param runnable 待执行的请求
     * @param executorService 执行者线程池
     */
    void handle(Runnable runnable, ExecutorService executorService);

}
