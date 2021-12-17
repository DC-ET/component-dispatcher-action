package com.zoi7.component.dispatcher.action;

import java.util.concurrent.TimeUnit;

/**
 * 分发器配置, 此处已提供默认配置
 * 如果自定义配置, 只需在各自项目中配置相应参数即可覆盖默认配置
 * 如在application.yml 中配置
 * <pre>
 *      dispatcher:
 *          corePoolSize: 200
 *          maximumPoolSize: 5
 *          keepAliveTime: 12
 *          timeUnit: SECOND
 * </pre>
 * @author yjy
 * 2018-05-31 12:42
 */
public class DispatcherActionProperties {

    /** 请求处理核心线程数量 */
    private int threads = 100;
    /** 空闲线程保留时间 */
    private int keepAliveTime = 1;
    private TimeUnit timeUnit = TimeUnit.MINUTES;
    /** 允许堆积的请求数量 */
    private int acceptRequest = 2000;
    /** 请求初始化阻塞超时时间, 单位: 秒 (当请求进来发现处理器未初始化完成时的等待时间) */
    private int waitInitTimeout = 30;

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getAcceptRequest() {
        return acceptRequest;
    }

    public void setAcceptRequest(int acceptRequest) {
        this.acceptRequest = acceptRequest;
    }

    public int getWaitInitTimeout() {
        return waitInitTimeout;
    }

    public void setWaitInitTimeout(int waitInitTimeout) {
        this.waitInitTimeout = waitInitTimeout;
    }

    @Override
    public String toString() {
        return "DispatcherActionProperties{" +
                "threads=" + threads +
                ", keepAliveTime=" + keepAliveTime +
                ", timeUnit=" + timeUnit +
                ", acceptRequest=" + acceptRequest +
                ", waitInitTimeout=" + waitInitTimeout +
                '}';
    }
}
