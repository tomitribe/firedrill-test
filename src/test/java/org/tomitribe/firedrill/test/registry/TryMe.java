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
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.Keys.RETURN;
import static org.tomitribe.firedrill.test.driver.WebDriverUtils.waitForPageToLoad;
import static org.tomitribe.firedrill.test.registry.TryMe.Action.INVOKE;
import static org.tomitribe.firedrill.test.registry.TryMe.Option.DIGEST;
import static org.tomitribe.firedrill.test.registry.TryMe.Option.OAUTH;
import static org.tomitribe.firedrill.test.registry.TryMe.Option.SIGNATURE;
import static org.tomitribe.firedrill.test.registry.TryMe.Parameter.HEADER;
import static org.tomitribe.firedrill.test.registry.TryMe.Parameter.QUERY;

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

    public TryMe signature(final String keyId, final String key) {
        addOption(SIGNATURE);

        final WebElement signatureForm = getFormSection("signature");
        final List<WebElement> inputs = signatureForm.findElements(tagName("input"));
        // keyId
        inputs.get(2).sendKeys(keyId);
        // key
        inputs.get(3).sendKeys(key);

        return this;
    }

    public TryMe addHeader(final String name, final String value) {
        addParameter(HEADER, name, value);
        return this;
    }

    public TryMe addQueryParam(final String name, final String value) {
        addParameter(QUERY, name, value);
        return this;
    }

    public TryMe removeParameter(final String name) {
        getParameterRow(name).ifPresent(e -> e.findElement(xpath("./td[last()]/div/i")).click());
        return this;
    }

    public TryMe removeAllQueryParameters() {
        final List<String> queryParameters =
                webDriver.findElements(
                        xpath("//div[@class='parameters']/div/div/table/tbody/tr[td/div[text() = 'query']]"))
                         .stream()
                         .map(p -> p.findElement(xpath("./td[1]/div/div/div/span/span")).getText())
                         .collect(toList());

        queryParameters.forEach(this::removeParameter);
        return this;
    }

    public TryMe withDigest(final String algorithm) {
        addOption(DIGEST);
        addParameterValue("Digest", algorithm);
        signParameter("Digest");
        return this;
    }

    public TryMe withDate() {
        addHeader("Date", " ");
        signParameter("Date");
        return this;
    }

    public Result invoke() {
        action(INVOKE);
        return Result.result(getStatusCode());
    }

    private WebElement getParametersDropdown() {
        return webDriver.findElement(xpath("//div[@class='parameters']//div[contains(@class, 'dropdown-primary')]"));
    }

    private void selectDropdownOption(final WebElement webElement, final String label) {
        webElement.findElement(xpath(format("./following-sibling::div/span/div[text() = '%s']", label))).click();
    }

    private void addOption(final Option option) {
        // TODO - Other dropdowns share the same classname. This works because it's the first. Fix.
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

    private Optional<WebElement> getParameterRow(final String name) {
        try {
            final WebElement element = webDriver.findElement(xpath(format(
                    "//div[@class='parameters']/div/div/table/tbody" +
                    "/tr[td[1]/div/div/div/span/following-sibling::span/span[text() = '%s']]",
                    name)));
            return Optional.of(element);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void addParameter(final Parameter parameter, final String name, final String value) {
        if (!getParameterRow(name).isPresent()) {
            final WebElement parametersDropdown = getParametersDropdown();
            parametersDropdown.click();
            selectDropdownOption(parametersDropdown, parameter.getLabel());
            addParameterName(name);
        }
        addParameterValue(name, value);
    }

    private void addParameterName(final String name) {
        final WebElement parameterRow = getParameterRow("").orElseThrow(IllegalStateException::new);
        final WebElement parameterNameInput =
                parameterRow.findElement(xpath("./td[1]/div/div[contains(@class, 'selectize-input')]"));
        parameterNameInput.click();

        try {
            parameterNameInput.findElement(
                    xpath(format("./following-sibling::div/div/div/div[div/div[text() = '%s']]", name))).click();
        } catch (Exception e) {
            parameterNameInput.findElement(
                    xpath(".//input")).sendKeys(name, RETURN);
        }
    }

    private void addParameterValue(final String name, final String value) {
        final WebElement parameterRow = getParameterRow(name).orElseThrow(IllegalStateException::new);
        final WebElement parameterValueInput =
                parameterRow.findElement(xpath("./td[6]//div/div[contains(@class, 'selectize-input')]"));
        parameterValueInput.click();


        try {
            parameterValueInput.findElement(
                    xpath(format("./following-sibling::div/div/div/div[div/div[contains(text(), '%s')]]", value)))
                       .click();
        } catch (Exception e) {
            parameterValueInput.findElement(xpath(".//input")).sendKeys(value, RETURN);
        }
    }

    private void signParameter(final String name) {
        getParameterRow(name).ifPresent(e -> e.findElement(xpath("./td[7]/i/div/div")).click());
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
    enum Option {
        OAUTH("Add OAuth 2.0"),
        SIGNATURE("Add HTTP Signature"),
        DIGEST("Add Digest")
        ;

        private String label;
    }

    @AllArgsConstructor
    @Getter
    enum Parameter {
        HEADER("Add Header"),
        PATH("Add Path Parameter"),
        QUERY("Add Query Parameter")
        ;

        private String label;
    }


    @AllArgsConstructor
    @Getter
    enum Action {
        INVOKE("Invoke")
        ;

        private String label;
    }
}
