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

package org.encos.flydown.test.services;

import org.encos.flydown.annotations.RequestRate;
import org.encos.flydown.limiters.params.FlydownDevil;

/**
 * @author Enrico Costanzi
 */
public class ContextServiceTest {

    public static final String DUMMY_IP_CONTEXT = "IP";
    public static final String DUMMY_OTHER_CONTEXT = "other_context";

    @RequestRate(value = FlydownDevil.CONTEXT_VAR, contextKey = DUMMY_IP_CONTEXT)
    public void ipContext() {

    }

}
