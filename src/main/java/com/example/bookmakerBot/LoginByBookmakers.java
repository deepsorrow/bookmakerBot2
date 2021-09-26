package com.example.bookmakerBot;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginByBookmakers {
    public static void onThunderpick(RemoteWebDriver driver) {
        String currentWindowHandle = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://thunderpick.com/ru/?list=live&login=true");
        if (driver.findElements(By.className("user-summary__content")).isEmpty()) {
            try {
                boolean loaded = false;
                for(int i = 0; i < 5; ++i){
                    if(!driver.findElements(By.id("email")).isEmpty()){
                        loaded = true;
                        break;
                    } else {
                        TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
                    }
                }

                if(!loaded){
                    driver.close();
                    onThunderpick(driver);
                    return;
                }
                WebElement emailElement = driver.findElement(By.id("email"));
                Utils.click(driver, emailElement);
                if (emailElement.getAttribute("value").isEmpty()) {
                    TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
                    emailElement.sendKeys("kropotovlesha@yandex.ru");
                }
                WebElement passwordElement = driver.findElement(By.id("password"));
                Utils.click(driver, passwordElement);
                if (passwordElement.getAttribute("value").isEmpty()) {
                    passwordElement.sendKeys("Nonermal_2");
                }
                TimeUnit.SECONDS.sleep(Utils.getRandomNumber(2, 3));
                driver.findElement(By.id("password")).sendKeys(Keys.ENTER);
                TimeUnit.SECONDS.sleep(Utils.getRandomNumber(4, 5));
                while (driver.findElements(By.cssSelector("iframe")).size() > 1) {
                    //if(iframe.getAttribute("name").equals("c-ffavuqejqem2")) {
                    System.out.println("fucking recaptcha!");
                    TimeUnit.SECONDS.sleep(2);
                    //}
                }
            } catch (Exception ignored) {

            }
        }
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    public static void onFonbet(RemoteWebDriver driver) {
        String currentWindowHandle = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.fonbet.ru/");
        try {
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
            Utils.click(driver, driver.findElement(By.className("_login-btn")));

            List<WebElement> inputs = driver.findElements(By.className("textFieldNewDesign--8ifNQ"));
            Utils.click(driver, inputs.get(0));
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
            inputs.get(0).findElement(By.tagName("input")).sendKeys("79284397851");
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
            Utils.click(driver, inputs.get(1));
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
            inputs.get(1).findElement(By.tagName("input")).sendKeys("Crawler_0pp");
//            for(WebElement input : inputs){
//                input.click();
//                try{
//                    String typePassword = input.getAttribute("type");
//                    input.sendKeys("Crawler_0pp");
//                } catch(Exception e) {
//                    input.sendKeys("79284397851");
//                }
//            }
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));

            inputs.get(1).findElement(By.tagName("input")).sendKeys(Keys.ENTER);

            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(2, 3));
        } catch (Exception ignored) {

        }
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }
}
