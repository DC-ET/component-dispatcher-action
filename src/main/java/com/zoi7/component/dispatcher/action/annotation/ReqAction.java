package com.zoi7.component.dispatcher.action.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 请求处理类标记 类似于@Controller
 * @author yjy
 * 2018-05-25 16:41
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ReqAction {

    String value() default ""; // 模块


}
