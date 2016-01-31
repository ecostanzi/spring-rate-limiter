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

import org.encos.flydown.conf.FlydownProperties;
import org.encos.flydown.exceptions.RateExceededException;
import org.encos.flydown.exceptions.SuspensionException;
import org.encos.flydown.limiters.cache.AbstractRateCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


class FlydownRateLimiter {

    private final static Logger log = LoggerFactory.getLogger(FlydownRateLimiter.class);

    AbstractRateCache rateCache;
    Properties flydownProperties;

    FlydownRateLimiter(AbstractRateCache rateCache, Properties properties) {
        this.rateCache = rateCache;
        this.flydownProperties = properties;
    }

    public void checkSuspension(RateLimitData rateLimitData, String methodId, String id) throws RateExceededException {
        String suspensionKey = suspensionKey(rateLimitData, methodId, id);
        log.debug("checking suspension for key {}", suspensionKey);
        try {
            rateCache.isSuspended(suspensionKey);
        } catch (RateExceededException e) {
            log.info("suspending request mapped on key {}", suspensionKey);
            throw e;
        } catch (Exception e) {
            log.warn("Suspension evaluation failure while checking suspension key {}", suspensionKey, e);
        }
    }


    public long cacheRequest(RateLimitData rateLimitData, String methodId, String id) throws RateExceededException {
        String evaluationKey = evaluationKey(rateLimitData, methodId, id);
        String suspensionKey = suspensionKey(rateLimitData, methodId, id);

        log.debug("caching request with key {}", evaluationKey);

        return cacheRequest(suspensionKey, evaluationKey, rateLimitData.getMax(),
                rateLimitData.getRange(), rateLimitData.getSuspension());
    }

    private long cacheRequest(String suspensionKey, String evaluationKey, int maxCount,
                              long interval, long suspensionTime) throws RateExceededException {

        int localMaxCount = maxCount >=0 ?
                maxCount :
                Integer.parseInt(flydownProperties.getProperty(FlydownProperties.FLYDOWN_REQUESTS_LIMIT));

        long localInterval = interval > 0 ? interval :
                Integer.parseInt(flydownProperties.getProperty(FlydownProperties.FLYDOWN_INTERVAL_TIME));

        long localSuspensionTime = suspensionTime > 0 ? suspensionTime :
                Integer.parseInt(flydownProperties.getProperty(FlydownProperties.FLYDOWN_SUSPENSION_TIME));

        long cacheTime = TimeUnit.MILLISECONDS.toMillis(localInterval);

        try {
            rateCache.isSuspended(suspensionKey);
            rateCache.cacheRequest(evaluationKey, localInterval, TimeUnit.MILLISECONDS);

            if (rateCache.checkTooManyEntries(evaluationKey, localMaxCount, localInterval, TimeUnit.MILLISECONDS)) {
                if(localSuspensionTime > 0){
                    rateCache.suspend(suspensionKey, localSuspensionTime, TimeUnit.MILLISECONDS);
                    rateCache.delete(evaluationKey);
//                    throw new SuspensionException(evaluationKey);
                }
                throw new RateExceededException(evaluationKey);
            } else {
                log.debug("key {} not yet expired", suspensionKey);
            }

        } catch (RateExceededException e) {
            throw e;
        } catch(Exception e){
            log.warn("Cache failed", e);
        }

        return cacheTime;
    }

    private String suspensionKey(RateLimitData rateLimitData, String methodId, String id) {
        return MessageFormat.format("{0}:{1}={2}",
                methodId, rateLimitData.getFlydownDevil().name(), id);
    }

    private String evaluationKey(RateLimitData rateLimitData, String methodId, String id) {
        return MessageFormat.format("{0}_{1}:{2}={3}",
                rateLimitData.getFlydownEvent(), methodId,
                rateLimitData.getFlydownDevil().name(), id);
    }

}