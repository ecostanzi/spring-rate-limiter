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

package org.encos.flydown.limiters.cache;

import org.encos.flydown.exceptions.RateException;

import java.util.concurrent.TimeUnit;

/**
 * @author Enrico Costanzi
 */
interface IRateCache {

    boolean isSuspended(String suspensionKey) throws RateException;

    long cacheRequest(String evaluationKey, long timeRange, TimeUnit timeRangeUnit) throws RateException;

    long suspend(String suspensionKey, long suspensionTime, TimeUnit suspensionTimeUnit);

    boolean checkTooManyEntries(String evaluationKey, int maxCount, long timeRange, TimeUnit timeRangeUnit);

    void put(String key, String value);

    String get(String key);

    void delete(String key);
}
