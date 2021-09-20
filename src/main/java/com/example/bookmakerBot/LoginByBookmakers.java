package com.example.bookmakerBot;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginByBookmakers {
    public static void onThunderpick(RemoteWebDriver driver){
        String currentWindowHandle = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://thunderpick.com/ru/?list=live&login=true");
        try{
            WebElement emailElement = driver.findElement(By.id("email"));
            emailElement.click();
            if(emailElement.getAttribute("value").isEmpty()) {
                TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
                emailElement.sendKeys("kropotovlesha@yandex.ru");
            }

            WebElement passwordElement = driver.findElement(By.id("password"));
            passwordElement.click();
            if(passwordElement.getAttribute("value").isEmpty()) {
                passwordElement.sendKeys("Nonermal_2");
            }
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(2, 3));
            driver.findElement(By.id("password")).sendKeys(Keys.ENTER);
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(4, 5));
            while(driver.findElements(By.cssSelector("iframe")).size() > 1) {
                //if(iframe.getAttribute("name").equals("c-ffavuqejqem2")) {
                    System.out.println("fucking recaptcha!");
                    TimeUnit.SECONDS.sleep(2);
                //}
            }
        } catch (Exception ignored){

        }
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    public static void onFonbet(RemoteWebDriver driver){
        String currentWindowHandle = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("https://www.fonbet.ru/");
        try{
            driver.findElement(By.className("_login-btn")).click();

            List<WebElement> inputs = driver.findElements(By.className("textFieldNewDesign--8ifNQ"));
            inputs.get(0).click();
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
            inputs.get(0).findElement(By.tagName("input")).sendKeys("79284397851");
            TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
            inputs.get(1).click();
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
        } catch (Exception ignored){

        }
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }
}
