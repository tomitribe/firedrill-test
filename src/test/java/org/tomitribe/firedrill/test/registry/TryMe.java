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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;
import static org.tomitribe.firedrill.test.driver.WebDriverUtils.waitForPageToLoad;
import static org.tomitribe.firedrill.test.registry.TryMe.Action.INVOKE;
import static org.tomitribe.firedrill.test.registry.TryMe.Option.OAUTH;

/**
 * @author Roberto Cortez
 */
@AllArgsConstructor(staticName = "tryMe", access = AccessLevel.PACKAGE)
public class TryMe {
    private final WebDriver webDriver;

    public TryMe oAuth(final String clientId, final String clientSecret,
                        final String username, final String password) {
        addOption(OAUTH);

        final WebElement oAuthForm = getFormSection("oauth");

        final WebElement oAuthFormOptions = oAuthForm.findElement(xpath("./div/div/h2/div"));
        oAuthFormOptions.findElements(tagName("li")).forEach((webElement) -> {
            oAuthFormOptions.click();
            webElement.click();
        });

        final List<WebElement> inputs = oAuthForm.findElements(tagName("input"));
        // clientId
        inputs.get(2).sendKeys(clientId);
        // clientSecret
        inputs.get(3).sendKeys(clientSecret);
        // username
        inputs.get(4).sendKeys(username);
        // password
        inputs.get(5).sendKeys(password);

        return this;
    }

    public TryMe removeQueryParameter(final String name) {
        final WebElement parameter = webDriver.findElement(xpath(format(
                "//div[@class='parameters']/div/div/table/tbody/tr[td/div/div/div/span/span[contains(text(), '%s')]]",
                name)));
        parameter.findElement(xpath("./td[last()]/div/i")).click();

        return this;
    }

    public Result invoke() {
        action(INVOKE);
        return Result.result(getStatusCode());
    }

    private void addOption(final Option option) {
        webDriver.findElement(className("dropdown-primary")).click();
        webDriver.findElements(tagName("span"))
                 .stream()
                 .filter(e -> e.getText().equals(option.getLabel()))
                 .findFirst()
                 .ifPresent(WebElement::click);
    }

    private WebElement getFormSection(final String name) {
        final Optional<WebElement> formSection = webDriver.findElements(className("form-line"))
                                                          .stream()
                                                          .filter(e -> contains(e.getAttribute("ng-if"), name))
                                                          .findFirst();
        assertTrue(formSection.isPresent());
        return formSection.get();
    }

    private void action(final Action action) {
        webDriver.findElement(xpath("//div[@class='bolt-button']/div/div")).click();
        webDriver.findElement(
                xpath(format("//div[@class='bolt-button']/div/div/a[div[text() = '%s']]", action.getLabel()))).click();
        waitForPageToLoad(webDriver);
    }

    private int getStatusCode() {
        return Integer.valueOf(webDriver.findElement(
                xpath("//div[@class='response']//h3[text() = 'Status code']/following-sibling::div/span")).getText());
    }

    @AllArgsConstructor(staticName = "result", access = AccessLevel.PACKAGE)
    @Getter
    public static class Result {
        private final int statusCode;
    }

    @AllArgsConstructor
    @Getter
    public enum Option {
        OAUTH("Add OAuth 2.0");

        private String label;
    }

    @AllArgsConstructor
    @Getter
    public enum Action {
        INVOKE("Invoke");

        private String label;
    }
}
