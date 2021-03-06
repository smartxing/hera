package io.anyway.hera.spring;

import io.anyway.hera.service.NonMetricService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzz on 16/8/17.
 */
@NonMetricService
@Component
public class MetricBeanPostProcessor implements BeanPostProcessor, PriorityOrdered,DisposableBean{

    private Map<String,BeanPostProcessorWrapper> wrapperIdx= new HashMap<String, BeanPostProcessorWrapper>();

    private int order = LOWEST_PRECEDENCE;

    @Value("${hera.appId:}")
    private String appId;

    @Autowired
    private List<BeanPreProcessorWrapper> beanPreProcessorWrappers = Collections.emptyList();

    @Autowired
    private List<BeanPostProcessorWrapper> beanPostProcessorWrappers = Collections.emptyList();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for(BeanPreProcessorWrapper each: beanPreProcessorWrappers){
            if(each.interest(bean)){
                return each.preWrapBean(bean,appId,beanName);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        for(BeanPostProcessorWrapper each: beanPostProcessorWrappers){
            if(each.interest(bean)){
                wrapperIdx.put(beanName,each);
                return each.wrapBean(bean,appId,beanName);
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String,BeanPostProcessorWrapper> each: wrapperIdx.entrySet()){
            each.getValue().destroyWrapper(appId,each.getKey());
        }
    }
}
