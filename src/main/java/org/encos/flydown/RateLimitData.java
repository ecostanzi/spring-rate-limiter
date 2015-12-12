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

import org.encos.flydown.annotations.ExceptionRate;
import org.encos.flydown.annotations.RequestRate;
import org.encos.flydown.conf.FlydownProperties;
import org.encos.flydown.limiters.params.FlydownEvent;
import org.encos.flydown.limiters.params.FlydownIdentifier;

import java.util.Properties;

public class RateLimitData {

    private FlydownIdentifier flydownIdentifier;
    private FlydownEvent flydownEvent;

    private int max;
    private long range;
    private long suspension;

    private Integer paramIndex;
    private String contextKey;

    private String suspensionKeyPrefix;
    private String rateEvaluationKeyPrefix;

    private RateLimitData(FlydownIdentifier flydownIdentifier, FlydownEvent flydownEvent) {
        this.flydownIdentifier = flydownIdentifier;
        this.flydownEvent = flydownEvent;

        rateEvaluationKeyPrefix = flydownEvent.name() + "_" + flydownIdentifier.name();
        suspensionKeyPrefix = flydownIdentifier.name();
    }

    public static RateLimitData build(ExceptionRate exceptionRate) {
        return build(
                exceptionRate.value(),
                FlydownEvent.EXCEPTION,

                exceptionRate.max(),
                exceptionRate.range(),
                exceptionRate.suspendFor(),

                exceptionRate.paramIndex(),
                exceptionRate.contextKey()
        );
    }

    public static RateLimitData build(RequestRate requestRate) {
        return build(
                requestRate.value(),
                FlydownEvent.REQUEST,

                requestRate.max(),
                requestRate.range(),
                requestRate.suspendFor(),

                requestRate.paramIndex(),
                requestRate.contextKey()
        );
    }


    private static RateLimitData build(FlydownIdentifier blocking, FlydownEvent flydownEvent,
                                       int max, long range,
                                       long suspendFor, int paramIndex, String contextKey) {

        //todo validation

        RateLimitData rateLimitData = new RateLimitData(blocking, flydownEvent);

        rateLimitData.setMax(max > 0 ? max : FlydownProperties.MAX_REQUESTS_DEFAULT);
        rateLimitData.setRange(range > 0 ? range : FlydownProperties.TIME_RANGE_DEFAULT);
        rateLimitData.setSuspension(suspendFor > 0 ? suspendFor : FlydownProperties.SUSPENSION_TIME_DEFAULT);

        if (blocking == FlydownIdentifier.PARAM) {
            rateLimitData.setParamIndex(paramIndex);
        }

        if (blocking == FlydownIdentifier.CONTEXT_VAR) {
            rateLimitData.setContextKey(contextKey);
        }

        return rateLimitData;
    }

    static void setProperties(Properties properties) {
        properties.putAll(properties);
    }

    public FlydownIdentifier getFlydownIdentifier() {
        return flydownIdentifier;
    }

    public void setFlydownIdentifier(FlydownIdentifier flydownIdentifier) {
        this.flydownIdentifier = flydownIdentifier;
    }

    public FlydownEvent getFlydownEvent() {
        return flydownEvent;
    }

    public void setFlydownEvent(FlydownEvent flydownEvent) {
        this.flydownEvent = flydownEvent;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public long getRange() {
        return range;
    }

    public void setRange(long range) {
        this.range = range;
    }

    public long getSuspension() {
        return suspension;
    }

    public void setSuspension(long suspension) {
        this.suspension = suspension;
    }

    public Integer getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(Integer paramIndex) {
        this.paramIndex = paramIndex;
    }

    public String getContextKey() {
        return contextKey;
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public String getSuspensionKeyPrefix() {
        return suspensionKeyPrefix;
    }

    public void setSuspensionKeyPrefix(String suspensionKeyPrefix) {
        this.suspensionKeyPrefix = suspensionKeyPrefix;
    }

    public String getRateEvaluationKeyPrefix() {
        return rateEvaluationKeyPrefix;
    }

    public void setRateEvaluationKeyPrefix(String rateEvaluationKeyPrefix) {
        this.rateEvaluationKeyPrefix = rateEvaluationKeyPrefix;
    }
}
