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

package org.encos.flydown.conf;

import java.util.Properties;

/**
 * @author Enrico Costanzi
 */
public class FlydownProperties extends Properties {

    public static final String FLYDOWN_REQUESTS_LIMIT = "flydown.requests.limit";
    public final static Integer REQUEST_LIMIT_DEFAULT = 10;

    public static final String FLYDOWN_INTERVAL_TIME = "flydown.interval.time";
    public final static Integer INTERVAL_DEFAULT = 10 * 1000;

    public static final String FLYDOWN_SUSPENSION_TIME = "flydown.suspension.time";
    public final static Integer SUSPENSION_TIME_DEFAULT = 0;

    public FlydownProperties() {
        setProperty(FLYDOWN_REQUESTS_LIMIT, String.valueOf(REQUEST_LIMIT_DEFAULT));
        setProperty(FLYDOWN_INTERVAL_TIME,String.valueOf(INTERVAL_DEFAULT));
        setProperty(FLYDOWN_SUSPENSION_TIME, String.valueOf(SUSPENSION_TIME_DEFAULT));
    }

}
