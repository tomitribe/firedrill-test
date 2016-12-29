/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.firedrill.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.tomitribe.firedrill.test.registry.Registry;
import org.tomitribe.firedrill.test.registry.TryMe.Result;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

/**
 * @author Roberto Cortez
 */
@RunWith(JUnit4.class)
public class EndToEndTest {
    private WebDriver webDriver;

    @Before
    public void setUp() throws Exception {
        webDriver = new ChromeDriver();
        webDriver.manage().timeouts().setScriptTimeout(30, SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        webDriver.quit();
    }

    @Test
    public void testOAuth() throws Exception {
        final Result result = Registry.registry("http://registry.superbiz.io:8080/registry", webDriver)
                                      .login("eric", "trey")
                                      .tryMe("GET", "/movies")
                                      .removeAllQueryParameters()
                                      .oAuth("imdb", "m0vies", "eric", "trey")
                                      .addQueryParam("first", "1")
                                      .addQueryParam("max", "10")
                                      .invoke();

        assertEquals(200, result.getStatusCode());
    }

    @Test
    public void testSignature() throws Exception {
        final Result result = Registry.registry("http://registry.superbiz.io:8080/registry", webDriver)
                                      .login("eric", "trey")
                                      .tryMe("GET", "/musics")
                                      .removeAllQueryParameters()
                                      .signature("eric:eric1", "parker")
                                      .withDigest("sha-256")
                                      .withDate()
                                      .invoke();

        assertEquals(200, result.getStatusCode());
    }

    @Test
    public void testOAuthAndSignature() throws Exception {
        final Result result = Registry.registry("http://registry.superbiz.io:8080/registry", webDriver)
                                      .login("eric", "trey")
                                      .tryMe("GET", "/books/{id}")
                                      .addPathParam("id", "1")
                                      .oAuth("imdb", "m0vies", "eric", "trey")
                                      .signature("eric:eric1", "parker")
                                      .withDigest("sha-256")
                                      .withDate()
                                      .invoke();

        assertEquals(200, result.getStatusCode());
    }
}
