/*
 * Copyright [2015] [Enrico Costanzi]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.encos.flydown;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.encos.flydown.annotations.ExceptionRate;
import org.encos.flydown.annotations.ExceptionRates;
import org.encos.flydown.annotations.RequestRate;
import org.encos.flydown.annotations.RequestRates;
import org.encos.flydown.conf.FlydownProperties;
import org.encos.flydown.exceptions.FlydownRuntimeException;
import org.encos.flydown.exceptions.RateExceededException;
import org.encos.flydown.limiters.cache.AbstractRateCache;
import org.encos.flydown.limiters.params.FlydownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Properties;


/**
 *
 * @author Enrico Costanzi
 */
@Aspect
public class Flydown implements InitializingBean {

    private final static Logger log = LoggerFactory.getLogger(Flydown.class);

    private AbstractRateCache rateCache;
    private FlydownRateLimiter limiter;

    private Properties flydownProperties = new FlydownProperties();

    public void afterPropertiesSet() throws Exception {
        if (rateCache == null) {
            throw new IllegalArgumentException("rate cache cannot be null");
        }

        this.limiter = new FlydownRateLimiter(this.rateCache, this.flydownProperties);
    }

    public AbstractRateCache getRateCache() {
        return rateCache;
    }

    public void setRateCache(AbstractRateCache rateCache) {
        this.rateCache = rateCache;
    }

    public FlydownRateLimiter getLimiter() {
        return limiter;
    }

    public void setLimiter(FlydownRateLimiter limiter) {
        this.limiter = limiter;
    }

    public Properties getFlydownProperties() {
        return flydownProperties;
    }

    public void setFlydownProperties(Properties flydownProperties) {
        this.flydownProperties.putAll(flydownProperties);
    }

    private void beforeRequestRate(JoinPoint joinPoint, RateLimitData rateLimitData) throws RateExceededException {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodKey = getMethodKey(methodSignature);

        String identifier = null;

        switch (rateLimitData.getFlydownDevil()){
            case CONTEXT_VAR:
                String context = rateCache.getFromContext(rateLimitData.getContextKey());

                if (context == null || "".equals(context.trim())) {
                    //todo invalid or unset paramindex
                }

                identifier = MessageFormat.format("{0}[{1}]", rateLimitData.getContextKey(), context);
                break;
            case PRINCIPAL:
                Object objectPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (!(objectPrincipal instanceof Principal)) {
                    throw new FlydownRuntimeException(String.format(
                            "Your application principal doesn't implement class %s", Principal.class.getName()));
                }

                Principal principal = (Principal) objectPrincipal;
                identifier = principal.getName();
                break;

            case PARAM:
                Object[] arguments = joinPoint.getArgs();
                int rateParamIndex = rateLimitData.getParamIndex();
                if (rateParamIndex < 0) {
                    //todo invalid or unset paramindex
                }
                if (arguments.length < rateParamIndex - 1) {
                    log.warn("not enough arguments for picking up param {} from method {}",
                            rateParamIndex, methodSignature.getMethod().getName());
                    return;
                }

                //fixme what if more method have the same params?
                methodKey = MessageFormat.format("{0}[{1}]", getMethodKey(methodSignature), rateParamIndex);
                identifier = arguments[rateParamIndex].toString();
                break;
        }

        if (rateLimitData.getFlydownEvent() == FlydownEvent.REQUEST) {
            limiter.cacheRequest(rateLimitData, methodKey, identifier);
        } else {
            //exception, not storing anything, just checking suspension
            limiter.checkSuspension(rateLimitData, methodKey, identifier);
        }

    }

    @Before("requestRatePointcut()")
    public void beforeRequestRate(JoinPoint joinPoint) throws RateExceededException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        RequestRate requestRate = methodSignature.getMethod().getAnnotation(RequestRate.class);
        if(requestRate != null){
            beforeRequestRate(joinPoint, requestRate);
        }

        RequestRates requestRates = methodSignature.getMethod().getAnnotation(RequestRates.class);
        if(requestRates != null){
            for(RequestRate singleRate : requestRates.value()){
                beforeRequestRate(joinPoint, singleRate);
            }
        }

    }

    public void beforeRequestRate(JoinPoint joinPoint, RequestRate requestRate) throws RateExceededException {
        RateLimitData rateLimitData = RateLimitData.build(requestRate);
        beforeRequestRate(joinPoint, rateLimitData);
    }

    @Pointcut("@annotation(org.encos.flydown.annotations.RequestRate) || " +
            "@annotation(org.encos.flydown.annotations.RequestRates)")
    public void requestRatePointcut(){}




    @Before("exceptionRatePointcut()")
    public void beforeExceptionRate(JoinPoint joinPoint) throws RateExceededException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        ExceptionRate exceptionRate = methodSignature.getMethod().getAnnotation(ExceptionRate.class);
        if(exceptionRate != null){
            beforeExceptionRate(joinPoint, exceptionRate);
        }

        ExceptionRates exceptionRates = methodSignature.getMethod().getAnnotation(ExceptionRates.class);
        if(exceptionRates != null){
            for(ExceptionRate singleRate : exceptionRates.value()){
                beforeExceptionRate(joinPoint, singleRate);
            }
        }

    }

    private void beforeExceptionRate(JoinPoint joinPoint, ExceptionRate exceptionRate) throws RateExceededException {
        RateLimitData rateLimitData = RateLimitData.build(exceptionRate);
        beforeRequestRate(joinPoint, rateLimitData);
    }

    @AfterThrowing(value = "exceptionRatePointcut()", throwing = "e")
    public void afterThrowingExceptionRate(JoinPoint joinPoint, Exception e) throws RateExceededException {
        log.debug("handling exception rate {}", e.getClass().getSimpleName());
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        ExceptionRate exceptionRate = methodSignature.getMethod().getAnnotation(ExceptionRate.class);
        if(exceptionRate != null){
            afterThrowingExceptionRate(methodSignature, exceptionRate, e);
        }

        ExceptionRates exceptionRates = methodSignature.getMethod().getAnnotation(ExceptionRates.class);
        if(exceptionRates != null){
            for(ExceptionRate singleRate : exceptionRates.value()){
                afterThrowingExceptionRate(methodSignature, singleRate, e);
            }
        }

    }

    private void afterThrowingExceptionRate(MethodSignature methodSignature, ExceptionRate exceptionRate,
                                            Exception e) throws RateExceededException {
        String methodKey = getMethodKey(methodSignature);
        RateLimitData rateLimitData = RateLimitData.build(exceptionRate);
        String id = rateCache.getFromContext(exceptionRate.value().name());

        //not my own exceptions
        if (id == null || e.getClass().equals(RateExceededException.class)) {
            return;
        }

        for(Class rateException : exceptionRate.exceptions()){
            if (rateException.equals(e.getClass())) { //fixme instance?
                limiter.cacheRequest(rateLimitData, methodKey, id);
            }
        }
    }

    @Pointcut("@annotation(org.encos.flydown.annotations.ExceptionRate) || " +
            "@annotation(org.encos.flydown.annotations.ExceptionRates)")
    public void exceptionRatePointcut(){}


    private String getMethodKey(MethodSignature methodSignature){

        Method method = methodSignature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        return MessageFormat.format("{0}.{1}", className, methodName);
    }

}