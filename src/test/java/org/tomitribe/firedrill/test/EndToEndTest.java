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

import com.paulhammant.ngwebdriver.NgWebDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.linkText;
import static org.openqa.selenium.By.name;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.jsReturnsValue;

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
    public void testEndtoEnd() throws Exception {
        login("eric", "trey");
        tryMe("GET", "/movies");
        oAuth("imdb", "m0vies", "eric", "trey");

        removeQueryParameter("first");
        removeQueryParameter("max");
        removeQueryParameter("field");
        removeQueryParameter("searchTerm");

        tryMeAction("Invoke");
        statusCode(200);
    }

    private void login(final String username, final String password) {
        webDriver.get("http://registry.superbiz.io:8080/registry/login");

        final WebElement formUsername = webDriver.findElement(name("username"));
        formUsername.sendKeys(username);
        webDriver.findElement(name("password")).sendKeys(password);
        formUsername.submit();

        waitForPageToLoad();
        assertEquals("http://registry.superbiz.io:8080/registry/", webDriver.getCurrentUrl());
    }

    private void tryMe(final String method, final String endpoint) {
        final WebElement getMusics = webDriver.findElement(linkText(method + " " + endpoint));
        getMusics.click();
        waitForPageToLoad();
        assertThat(webDriver.getCurrentUrl(), containsString(method + endpoint));

        final WebElement tryMe = webDriver.findElement(linkText("Try Me"));
        tryMe.click();
        waitForPageToLoad();
    }

    private void tryMeAdd(final String option) {
        final WebElement optionsDropdown = webDriver.findElement(className("dropdown-primary"));
        optionsDropdown.click();
        final Optional<WebElement> oAuth = webDriver.findElements(tagName("span"))
                                                    .stream()
                                                    .filter(e -> e.getText().equals(option))
                                                    .findFirst();
        assertTrue(oAuth.isPresent());
        oAuth.ifPresent(WebElement::click);
    }

    private void tryMeAction(final String action) {
        webDriver.findElement(xpath("//div[@class='bolt-button']/div/div")).click();
        webDriver.findElement(xpath(format("//div[@class='bolt-button']/div/div/a[div[text() = '%s']]", action)))
                 .click();
        new NgWebDriver((JavascriptExecutor) webDriver).waitForAngularRequestsToFinish();
    }

    private void oAuth(final String clientId, final String clientSecret, final String username, final String password) {
        tryMeAdd("Add OAuth 2.0");

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
    }

    private WebElement getFormSection(final String name) {
        final Optional<WebElement> formSection = webDriver.findElements(className("form-line"))
                                                          .stream()
                                                          .filter(e -> contains(e.getAttribute("ng-if"), name))
                                                          .findFirst();
        assertTrue(formSection.isPresent());
        return formSection.get();
    }

    private void removeQueryParameter(final String name) {
        final WebElement parameter = webDriver.findElement(xpath(format(
                "//div[@class='parameters']/div/div/table/tbody/tr[td/div/div/div/span/span[contains(text(), '%s')]]",
                name)));
        parameter.findElement(xpath("./td[last()]/div/i")).click();
    }

    private void statusCode(final int statusCode) {
        assertEquals(String.valueOf(statusCode), webDriver.findElement(
                xpath("//div[@class='response']//h3[text() = 'Status code']/following-sibling::div/span")).getText());
    }

    private void waitForPageToLoad() {
        new WebDriverWait(webDriver, 30).until(jsReturnsValue("return document.readyState==\"complete\";"));
        new NgWebDriver((JavascriptExecutor) webDriver).waitForAngularRequestsToFinish();
    }
}
