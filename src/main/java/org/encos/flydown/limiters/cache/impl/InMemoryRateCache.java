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

import org.encos.flydown.exceptions.RateExceededException;
import org.encos.flydown.limiters.cache.AbstractRateCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class InMemoryRateCache extends AbstractRateCache {

    //fixme make this methods threadsafe (dont' use syncronized, but some async queuing model)
    private final static Logger log = LoggerFactory.getLogger(InMemoryRateCache.class);

    private Map<String, Object> cacheTemplate = new HashMap<String, Object>();

    public boolean isSuspended(String suspensionKey) throws RateExceededException {
        Object o = cacheTemplate.get(suspensionKey);
        return o != null && new Date((Long) o).after(new Date());
    }

    public long cacheRequest(String evaluationKey, long timeRange, TimeUnit timeRangeUnit) throws RateExceededException {
        ArrayList<Long> currentList = (ArrayList<Long>) cacheTemplate.get(evaluationKey);
        if (currentList == null) {
            currentList = new ArrayList<Long>();
            cacheTemplate.put(evaluationKey, currentList);
        }

        long now = System.currentTimeMillis();
        currentList.add(now);
        return now;
    }

    public long suspend(String suspensionKey, long suspensionTime, TimeUnit suspensionTimeUnit) {
        long now = suspensionTimeUnit.toMillis(suspensionTime);
        cacheTemplate.put(suspensionKey, now);

        return now;
    }

    public boolean checkTooManyEntries(String evaluationKey, int maxCount, long timeRange, TimeUnit timeRangeUnit) {
        List<Long> list = (List<Long>) cacheTemplate.get(evaluationKey);
        return (list.size() > maxCount); //todo dummy solution, must check the timerange
    }

    public void put(String key, String value) {
        cacheTemplate.put(key, value);
    }

    public String get(String key) {
        return (String) cacheTemplate.get(key);
    }

    public void delete(String key) {
        cacheTemplate.remove(key);
    }
}