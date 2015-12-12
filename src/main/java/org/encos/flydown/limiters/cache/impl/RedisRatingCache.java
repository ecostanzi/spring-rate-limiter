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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Enrico Costanzi
 */
public class RedisRatingCache extends AbstractRateCache {

    private final static Logger log = LoggerFactory.getLogger(RedisRatingCache.class);
    private final static String SUSPENSION_KEY_LIST = "suspension_key_list";
    RedisTemplate<String, Object> cacheTemplate;

    @SuppressWarnings("unchecked")
    public RedisRatingCache(String ip, int port) {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(ip);
        jedisConnectionFactory.setPort(port);

        //todo to be defined properly

        cacheTemplate = new RedisTemplate();
        cacheTemplate.setConnectionFactory(jedisConnectionFactory);

    }

    @SuppressWarnings("unchecked")
    public RedisRatingCache(JedisConnectionFactory jedisConnectionFactory) {
        cacheTemplate = new RedisTemplate();
        cacheTemplate.setConnectionFactory(jedisConnectionFactory);
    }

    public boolean isSuspended(String suspensionKey) throws RateException {
        Object suspensionValue = cacheTemplate.opsForValue().get(suspensionKey);
        if (suspensionValue != null) {
            Long expire = cacheTemplate.getExpire(suspensionKey);
            log.error("'{}' suspension key found! Expires in {}", suspensionKey, expire);
            return true;
        }

        return false;
    }

    public long cacheRequest(String evaluationKey, long timeRange, TimeUnit timeRangeUnit) {
        Date date = new Date();
        cacheTemplate.opsForList().rightPush(evaluationKey, date.getTime());
        cacheTemplate.expire(evaluationKey, timeRange, timeRangeUnit);
        log.debug("push({0}, {1}, {2})", evaluationKey, timeRange, timeRangeUnit.name());
        return timeRangeUnit.toMillis(timeRange);
    }

    public long suspend(String suspensionKey, long suspensionTime, TimeUnit suspensionTimeUnit) {
        cacheTemplate.opsForValue().set(suspensionKey, System.currentTimeMillis());

        //suspending action for evaluationKey
        cacheTemplate.expire(suspensionKey, suspensionTime, suspensionTimeUnit);
        cacheTemplate.opsForList().leftPush(SUSPENSION_KEY_LIST, suspensionKey);
        log.info(MessageFormat.format("suspend({0}, {1}, {2})", suspensionKey, String.valueOf(suspensionTime), suspensionTimeUnit.name()));

        return suspensionTimeUnit.toMillis(suspensionTime);
    }

    public boolean checkTooManyEntries(String evaluationKey, int maxCount, long timeRange, TimeUnit timeRangeUnit) {
        Long keyListLength = cacheTemplate.opsForList().size(evaluationKey);
        log.debug(MessageFormat.format("size([{0}])={1}, max={2}", evaluationKey, keyListLength, maxCount));
        if (keyListLength > maxCount) {
            List<Object> values = cacheTemplate.opsForList().range(evaluationKey, 0, keyListLength - 1);
            Long firstEntry = (Long) values.get(0);
            Long lastEntry = (Long) values.get(values.size() - 1);

            return (lastEntry - firstEntry < timeRangeUnit.toMillis(timeRange));
        }

        //not yet expired
        return false;
    }

    public void put(String key, String value) {
        cacheTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return (String) cacheTemplate.opsForValue().get(key);
    }

    //todo get suspension key list

    public void delete(String key) {
        log.debug("Deleting key {}", key);
        cacheTemplate.delete(key);
    }
}
