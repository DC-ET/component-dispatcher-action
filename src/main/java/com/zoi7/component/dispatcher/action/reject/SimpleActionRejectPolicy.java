package com.zoi7.component.dispatcher.action.reject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 默认的请求拒绝策略
 */
public class SimpleActionRejectPolicy implements ActionRejectPolicy {

    private static final Logger log = LoggerFactory.getLogger(SimpleActionRejectPolicy.class);
    
    @Override
    public void handle(Runnable runnable, ExecutorService executorService) {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            log.info("Action request reject, cause pool is shutdown or terminating");
            return;
        }
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            if (tpe.isTerminating()) {
                log.info("Action request reject, cause pool is terminating");
                return;
            }
            int activeCount = tpe.getActiveCount();
            long completedTaskCount = tpe.getCompletedTaskCount();
            long taskCount = tpe.getTaskCount();
            log.error("Action request overflow, activeCount: {}, " +
                    "completedTaskCount: {}, taskCount: {}",
                    activeCount, completedTaskCount, taskCount);
            return;
        }
        log.error("Action request overflow");
    }

}
