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

package org.encos.flydown.test;

import org.encos.flydown.exceptions.RateException;
import org.encos.flydown.limiters.cache.impl.InMemoryRateCache;
import org.encos.flydown.test.utils.security.DefaultAuthentication;
import org.encos.flydown.test.utils.security.DefaultPrincipal;
import org.encos.flydown.test.utils.security.DefaultSecurityContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotEquals;

/**
 * @author Enrico Costanzi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-application.xml")
public class ContextTest {

    private static Integer maxRequests;
    @Autowired
    InMemoryRateCache memoryRateCache;
    @Autowired
    ContextServiceTest ratedClass;

    @BeforeClass
    public static void initTests() {
        maxRequests = 11;//fixme all this stuff has to be fixed using properties and final variables
    }

    @Test(expected = RateException.class)
    public void testContext() throws Exception {
        memoryRateCache.addToContext(ContextServiceTest.DUMMY_IP_CONTEXT, "127.0.0.1");
        for (int i = 0; i < maxRequests; i++) {
            ratedClass.ipContext();
            assertNotEquals(i, maxRequests + 1);
        }
    }

    @Test(expected = RateException.class)
    public void testPrincipal() throws Exception {
        DefaultSecurityContext securityContext =
                new DefaultSecurityContext(new DefaultAuthentication(new DefaultPrincipal("enrico")));
        SecurityContextHolder.setContext(securityContext);

        for (int i = 0; i < maxRequests; i++) {
            ratedClass.getPrincipal();
            assertNotEquals(i, maxRequests + 1);

        }
    }

    @Test(expected = RateException.class)
    public void testParam() throws Exception {
        for (int i = 0; i < maxRequests; i++) {
            ratedClass.checkParam("myFakeTestParam");
            assertNotEquals(i, maxRequests + 1);
        }
    }
}
