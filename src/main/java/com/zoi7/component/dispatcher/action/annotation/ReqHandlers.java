package com.zoi7.component.dispatcher.action.annotation;

import java.lang.reflect.Method;

/**
 *
 * 处理器
 * @author yjy
 * 2018-05-28 11:43
 */
public class ReqHandlers {

    private Object target; // 处理器对象(如 TestController 对象)
    private Method method; // 处理方法(如 login 方法对象)

    public ReqHandlers(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "ReqHandlers{" +
                "target=" + target +
                ", method=" + method +
                '}';
    }
}
