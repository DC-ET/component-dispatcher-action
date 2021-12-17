package com.zoi7.component.dispatcher.action;

import com.zoi7.component.dispatcher.action.exception.handler.ActionExceptionHandler;
import com.zoi7.component.dispatcher.action.exception.handler.SimpleActionExceptionHandler;
import com.zoi7.component.dispatcher.action.interceptor.ActionInterceptor;
import com.zoi7.component.dispatcher.action.reject.ActionRejectPolicy;
import com.zoi7.component.dispatcher.action.reject.SimpleActionRejectPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author yjy
 */
@Configuration
public class ComponentDispatcherActionAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(DispatcherActionProperties.class)
    @ConfigurationProperties(prefix = "component.dispatcher.action")
    public DispatcherActionProperties dispatcherActionProperties() {
        return new DispatcherActionProperties();
    }

    @Bean
    @ConditionalOnMissingBean(ActionRejectPolicy.class)
    public SimpleActionRejectPolicy defaultActionRejectPolicy() {
        return new SimpleActionRejectPolicy();
    }

    @Bean
    @ConditionalOnMissingBean(ActionExceptionHandler.class)
    public ActionExceptionHandler simpleActionExceptionHandler() {
        return new SimpleActionExceptionHandler();
    }

    @Bean
    public ActionDispatcher actionDispatcher(DispatcherActionProperties dispatcherActionProperties,
                                             List<ActionInterceptor> actionInterceptorList,
                                             ActionRejectPolicy actionRejectPolicy,
                                             ActionExceptionHandler actionExceptionHandler) {
        return new ActionDispatcher(dispatcherActionProperties, actionInterceptorList, actionRejectPolicy, actionExceptionHandler);
    }

    @Bean
    public DispatcherActionInitializer actionInitializer(ActionDispatcher actionDispatcher) {
        return new DispatcherActionInitializer(actionDispatcher);
    }


}
