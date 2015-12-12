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

package org.encos.flydown.limiters.cache.impl;

import org.encos.flydown.exceptions.RateException;
import org.encos.flydown.limiters.cache.AbstractRateCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class GuavaRateCache extends AbstractRateCache {

    private final static Logger log = LoggerFactory.getLogger(GuavaRateCache.class);

    public GuavaRateCache() {
        //todo
    }

    public boolean isSuspended(String suspensionKey) throws RateException {
        return false;
    }

    public long cacheRequest(String evaluationKey, long timeRange, TimeUnit timeRangeUnit) throws RateException {
        return 0;
    }

    public long suspend(String suspensionKey, long suspensionTime, TimeUnit suspensionTimeUnit) {
        return 0;
    }

    public boolean checkTooManyEntries(String evaluationKey, int maxCount, long timeRange, TimeUnit timeRangeUnit) {
        return false;
    }

    public void put(String key, String value) {

    }

    public String get(String key) {
        return null;
    }

    public void delete(String key) {

    }
}