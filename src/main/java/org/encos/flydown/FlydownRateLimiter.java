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

import org.encos.flydown.exceptions.RateException;
import org.encos.flydown.limiters.cache.AbstractRateCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


class FlydownRateLimiter {

    private final static Logger log = LoggerFactory.getLogger(FlydownRateLimiter.class);

    AbstractRateCache rateCache;

    FlydownRateLimiter(AbstractRateCache rateCache) {
        this.rateCache = rateCache;
    }

    public void checkSuspension(RateLimitData rateLimitData, String methodId, String id) throws RateException {
        String suspensionKey = suspensionKey(rateLimitData, methodId, id);
        log.debug("checking suspension for key {}", suspensionKey);
        try {
            rateCache.isSuspended(suspensionKey);
        } catch (RateException e) {
            log.info("suspending request mapped on key {}", suspensionKey);
            throw e;
        } catch (Exception e) {
            log.warn("Suspension evaluation failure while checking suspension key {}", suspensionKey, e);
        }
    }


    public long cacheRequest(RateLimitData rateLimitData, String methodId, String id) throws RateException {
        String evaluationKey = evaluationKey(rateLimitData, methodId, id);
        String suspensionKey = suspensionKey(rateLimitData, methodId, id);

        log.debug("caching request with key {}", evaluationKey);

        return cacheRequest(suspensionKey, evaluationKey, rateLimitData.getMax(),
                rateLimitData.getRange(), rateLimitData.getSuspension());
    }

    private long cacheRequest(String suspensionKey, String evaluationKey, int maxCount,
                              long timeRange, long suspensionTime) throws RateException {

        long cacheTime = TimeUnit.MILLISECONDS.toMillis(timeRange);

        try {
            rateCache.isSuspended(suspensionKey);
            rateCache.cacheRequest(evaluationKey, timeRange, TimeUnit.MILLISECONDS);

            if (rateCache.checkTooManyEntries(evaluationKey, maxCount, timeRange, TimeUnit.MILLISECONDS)) {
                rateCache.suspend(suspensionKey, suspensionTime, TimeUnit.MILLISECONDS);
                rateCache.delete(evaluationKey);
                throw new RateException("Request rate too high, retry later...");
            } else {
                log.debug("key {} not yet expired", suspensionKey);
            }

        } catch (RateException e) {
            throw e;
        } catch(Exception e){
            log.warn("Cache failed", e);
        }

        return cacheTime;
    }

    private String suspensionKey(RateLimitData rateLimitData, String methodId, String id) {
        return MessageFormat.format("{0}:{1}={2}",
                methodId, rateLimitData.getFlydownIdentifier().name(), id);
    }

    private String evaluationKey(RateLimitData rateLimitData, String methodId, String id) {
        return MessageFormat.format("{0}_{1}:{2}={3}",
                rateLimitData.getFlydownEvent(), methodId,
                rateLimitData.getFlydownIdentifier().name(), id);
    }

}