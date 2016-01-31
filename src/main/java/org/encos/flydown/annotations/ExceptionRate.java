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

package org.encos.flydown.annotations;

import org.encos.flydown.limiters.params.FlydownDevil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionRate {

    /**
     * The devil to be locked
     * @return
     */
    FlydownDevil value();

    /**
     * The maximum number of requests allowed in the interval
     * @return
     */
    int max() default -1;

    /**
     * The interval (in milliseconds) in which to consider the requests
     * @return
     */
    long interval() default -1;

    /**
     * The suspension time (in milliseconds) to be set for the devil that perform
     * too many requests. Suspends only if the value is positive.
     * @return
     */
    long suspendFor() default 0;

    /**
     * The method parameter index to consider for rating
     * @return
     */
    int paramIndex() default -1;

    /**
     * The context key to consider for rating
     * @return
     */
    String contextKey() default "";

    /**
     * The list of exception class considered for rating
     * @return
     */
    Class<? extends Throwable>[] exceptions();
}
