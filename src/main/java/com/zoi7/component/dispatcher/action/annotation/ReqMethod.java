package com.zoi7.component.dispatcher.action.annotation;

import java.lang.annotation.*;

/**
 *
 * 具体的处理方法
 * @author yjy
 * 2018-05-25 16:46
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqMethod {

    String value() default ""; // 请求名


}
