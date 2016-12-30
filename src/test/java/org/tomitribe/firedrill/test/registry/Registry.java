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
package org.tomitribe.firedrill.test.registry;

import lombok.AllArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.StringJoiner;

import static org.openqa.selenium.By.linkText;
import static org.openqa.selenium.By.name;
import static org.tomitribe.firedrill.test.driver.WebDriverUtils.waitForPageToLoad;

/**
 * @author Roberto Cortez
 */
@AllArgsConstructor(staticName = "registry")
public class Registry {
    private final String url;
    private final WebDriver webDriver;

    public Registry login(final String username, final String password) {
        webDriver.get(url + "/login");

        final WebElement formUsername = webDriver.findElement(name("username"));
        formUsername.sendKeys(username);
        webDriver.findElement(name("password")).sendKeys(password);
        formUsername.submit();

        waitForPageToLoad(webDriver);
        return this;
    }

    public TryMe tryMe(final String application, final String method, final String path, final String version) {
        webDriver.get(new StringJoiner("/").add(url).add("endpoint").add(application).add(method).add(path).toString() +
                      "?version=" +
                      version);
        waitForPageToLoad(webDriver);

        webDriver.findElement(linkText("Try Me")).click();
        waitForPageToLoad(webDriver);

        return TryMe.tryMe(webDriver);
    }
}
