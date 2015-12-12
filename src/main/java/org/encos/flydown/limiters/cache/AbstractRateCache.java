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

import org.encos.flydown.context.IRateContextIdentifier;
import org.encos.flydown.context.ThreadContext;

/**
 * @author Enrico Costanzi
 */
public abstract class AbstractRateCache implements IRateCache {

    private IRateContextIdentifier rateContextIdentifier;

    public AbstractRateCache() {
        this.rateContextIdentifier = new ThreadContext();
    }

    public AbstractRateCache(IRateContextIdentifier rateContextIdentifier) {
        this.rateContextIdentifier = rateContextIdentifier;
    }

    public final void addToContext(String key, String value) {
        this.put(rateContextIdentifier.get() + "_" + key, value);
    }

    public final String getFromContext(String key) {
        return this.get(rateContextIdentifier.get() + "_" + key);
    }

    public final void removeFromContext(String key) {
        this.delete(rateContextIdentifier.get() + "_" + key);
    }
}
