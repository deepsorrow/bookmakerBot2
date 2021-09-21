package com.example.bookmakerBot;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaceBetByBookmakers {
    public static boolean onThunderpick(RemoteWebDriver driver, Match match, String mapName, int amount,
                                        double requiredKoeff, boolean onHome) {
        try {
            driver.switchTo().window(match.windowHandle);
            WebElement parentRow = null;
            List<WebElement> rows = driver.findElements(By.className("market-row__name"));
            for (WebElement row : rows) {
                if (row.getText().equalsIgnoreCase(mapName)) {
                    parentRow = row.findElement(By.xpath("./.."));
                    break;
                }
            }

            if (parentRow == null)
                return false;

            List<WebElement> crosses = driver.findElements(By.className("ft-remove"));
            for (WebElement cross : crosses)
                cross.click();

            WebElement oddsCell;
            try {
                oddsCell = getOddsCell(driver, parentRow, mapName, onHome);

                WebElement btn = oddsCell.findElement(By.tagName("button"));
                List<WebElement> span = oddsCell.findElements(By.tagName("span"));
                if (btn.getAttribute("class").contains("disabled")) {
                    System.out.println("Thunderpick odds blocked!");
                    return false;
                }
                else if(span.isEmpty()){
                    System.out.println("Thunderpick odds blocked!");
                    return false;
                } else if (Double.parseDouble(span.get(0).getText()) < requiredKoeff) {
                    String newKoeff = oddsCell.findElement(By.tagName("span")).getText();
                    System.out.println("Didn't managed to place bet on thunderpick as coefficient dropped " +
                            "from " + requiredKoeff + " to " + newKoeff + ". Match: " + match.homeTeam + " - " + match.awayTeam);
                    return false;
                }
                else {
                    ((JavascriptExecutor) driver).executeScript("window.scroll(0, -1000)");
                    if(!mapName.equals("Overall"))
                        ((JavascriptExecutor) driver).executeScript("window.scroll(0, " + (oddsCell.getLocation().getY()-150) + ")");
                    TimeUnit.MILLISECONDS.sleep(300);
                    oddsCell.click();
                    for(int i = 0; i < 3; ++i){
                         if(driver.findElements(By.className("thp-tile")).isEmpty())
                            TimeUnit.MILLISECONDS.sleep(500);
                         else
                             break;
                    }

                    if(driver.findElements(By.className("thp-tile")).isEmpty()) {
                        System.out.println("A tile on thunderpick is not appeared for some reason!");
                        return false;
                    }
                }
            } catch (Exception e) {
                System.out.println("Thunderpick odds not found! " + e.toString());
                return false;
            }

            List<WebElement> inputs;
            inputs = driver.findElements(By.className("thp-input"));
            WebElement input = inputs.get(inputs.size() - 1);
            input.clear();
            TimeUnit.MILLISECONDS.sleep(300);
            input.click();
            TimeUnit.MILLISECONDS.sleep(200);
            input.sendKeys("" + amount);
            TimeUnit.MILLISECONDS.sleep(300);

            try {
                driver.findElement(By.className("bet-slip__floating-button")).click();
            } catch (Exception e) {
                System.out.println("Couldn't click on slip button: " + e.toString());
                return false;
//                List<WebElement> yesButtons = driver.findElements(By.className("bet-slip-change-dialog__yes-button"));
//                if(!yesButtons.isEmpty()) {
//                    yesButtons.get(0).click();
//                    TimeUnit.MILLISECONDS.sleep(300);
//                }
//                driver.findElement(By.className("bet-slip__floating-button")).click();
            }

            List<WebElement> appearedInfo = driver.findElements(By.className("bet-slip-info__title"));
            for(int i = 0; i < 20; ++i){
//                List<WebElement> finalButtons = driver.findElements(By.xpath("//div[@class='btn--round btn--large']"));
//                if (finalButtons.size() == 2) {
//                    System.out.println("Final button appeared on thunderpick. Skipping...");
//                    return false;
//                }
                List<WebElement> changedKoeffsButtons = driver.findElements(By.className("btn--spacing-medium"));
                if(!changedKoeffsButtons.isEmpty()){
                    oddsCell = getOddsCell(driver, parentRow, mapName, onHome);
                    List<WebElement> span = oddsCell.findElements(By.tagName("span"));
                    if(span.isEmpty()){
                        System.out.println("Thunderpick odds blocked!");
                        return false;
                    } else if(Double.parseDouble(span.get(0).getText()) < requiredKoeff) {
                        changedKoeffsButtons.get(changedKoeffsButtons.size()-1).click();
                        System.out.println("Didn't managed to place bet on thunderpick as coefficient dropped " +
                                "from " + requiredKoeff + " to " + span.get(0).getText() + ". Match: " + match.homeTeam
                                + " - " + match.awayTeam);
                        return false;
                    } else {
                        changedKoeffsButtons.get(0).click();
                        System.out.println("Coefficients changed from " + requiredKoeff + " to " + span.get(0).getText()
                                + ". Placed anyway.");
                        return true;
                    }
                }

                if(appearedInfo.isEmpty()) {
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                else{
                    String appearedText = appearedInfo.get(0).getText();
                    if(appearedText.equalsIgnoreCase("Ставки приняты!"))
                        return true;
                    else {
                        System.out.println("Couldn't place bet on thunderpick, appeared info: " + appearedText);
                        return false;
                    }
                }
            }

//            try {
//                List<WebElement> finalButtons = driver.findElements(By.xpath("//div[@class='btn--round btn--large']"));
//                if (finalButtons.size() == 2) {
//                    System.out.println("Final button appeared on thunderpick. Skipping...");
//                    return false;
//                }
//                    //finalButtons.get(0).click();
//
//                return false;
////                String greenText = driver.findElement(By.className("bet-slip-info__title")).getText();
////                if (greenText.equalsIgnoreCase("Ставки приняты!")) {
////                    System.out.println("Successfully placed bet on thunderpick. Amount: " + amount
////                            + ", game: " + match.homeTeam + " - " + match.awayTeam);
////                    return true;
////                }
////                else {
////                    System.out.println("Green text not appeared after placing bet on thunderpick..?");
////                    return false;
////                }
//            } catch (Exception e) {
//                System.out.println("ERROR WHILE CLICKING ON BET ON THUNDERPICK: " + e.toString());
//                e.printStackTrace();
//                return false;
//            }
        } catch (Exception e) {
            System.out.println("ERROR WHILE PLACING BET ON THUNDERPICK: " + e.toString());
            e.printStackTrace();
            return false;
        }

        System.out.println("Somehow reached the end while placing bet on thunerpick. False.");
        return false;
    }

    public static WebElement getOddsCell(RemoteWebDriver driver, WebElement parentRow, String mapName, boolean onHome){
        WebElement oddsCell;
        if (onHome){
            if(mapName.equals("Overall"))
                oddsCell = driver.findElements(By.className("odds-button--variant-light")).get(0);
            else
                oddsCell = parentRow.findElement(By.className("market-row__odds--left"));
        }
        else {
            if(mapName.equals("Overall"))
                oddsCell = driver.findElements(By.className("odds-button--variant-light")).get(1);
            else
                oddsCell = parentRow.findElement(By.className("market-row__odds--right"));
        }

        return oddsCell;
    }

    public static boolean onFonbet(RemoteWebDriver driver, Match match, String mapNameRequired, int amount, boolean onHome) throws InterruptedException {
        driver.switchTo().window(match.windowHandle);

        List<WebElement> existingCoupons = driver.findElements(By.className("stake-clear--1NatC"));
        for(WebElement existingCoupon : existingCoupons)
            existingCoupon.click();

        WebElement match_title = null;
        List<WebElement> match_titles = driver.findElements(By.className("table__match-title-text"));
        for (WebElement match_title_candidate : match_titles) {
            if (match_title_candidate.getTagName().equalsIgnoreCase("a") &&
                    match_title_candidate.getAttribute("href").equals(match.datakey)) {
                match_title = match_title_candidate;
                break;
            }
        }

        if (match_title == null)
            return false;

        WebElement table__row = match_title.findElement(By.xpath("./../../.."));
        WebElement table__body = table__row.findElement(By.xpath("./.."));
        List<WebElement> table__rows = table__body.findElements(By.xpath("./child::*"));
        // list until map events
        boolean match_title_reached = false;
        for (WebElement tableRow : table__rows) {
            if (!match_title_reached) {
                if (tableRow.equals(table__row))
                    match_title_reached = true;
                if(!mapNameRequired.equals("Overall"))
                    continue;
            }

            String mapNameCurrent = tableRow.findElements(By.className("table__col")).get(1)
                    .findElement(By.className("table__match-title-text")).getText();
            if(!(mapNameCurrent.equalsIgnoreCase("1-я карта") || mapNameCurrent.equalsIgnoreCase("2-я карта")
                    || mapNameCurrent.equalsIgnoreCase("3-я карта") || mapNameCurrent.equalsIgnoreCase("4-я карта")
                    || mapNameCurrent.equalsIgnoreCase("5-я карта"))) { // means we've stumbled upon some next game
                System.out.println("Reached next game for some reason on fonbet: " + mapNameCurrent + ". Quitting..");
                return false;
            }

            if(!mapFoundOnFonbet(mapNameRequired, mapNameCurrent))
                continue;

            List<WebElement> columns = tableRow.findElements(By.className("table__col"));

            while(columns.get(2).getAttribute("class").contains("_state_blocked")) {
                TimeUnit.MILLISECONDS.sleep(500);
                columns = tableRow.findElements(By.className("table__col"));
            }

            if(onHome)
                columns.get(2).click();
            else
                columns.get(4).click();

            List<WebElement> coupons;
            while(true) {
                coupons = driver.findElements(By.className("stake-wide--1XHB_"));
                if(coupons.size() == 1)
                    break;
                else if(coupons.isEmpty())
                    TimeUnit.MILLISECONDS.sleep(500);
                else {
                    System.out.println("For some reason there's more than 1 coupon on fonbet. Quitting..");
                    return false;
                }
            }

            //List<WebElement> placedBetsBefore = driver.findElements(By.className("coupon__table-row--3vSjv"));

            while(true) {
                List<WebElement> errorContainers = driver.findElements(By.className("error-box--3tiP1"));
                if(!errorContainers.isEmpty()) {
                    errorContainers.get(0).findElement(By.className("button--54u30")).click();
                    TimeUnit.MILLISECONDS.sleep(300);
                    System.out.println("Error box appeared and was successfully clicked...");
                    continue;
                }

                List<WebElement> acceptButtons = driver.findElements(By.className("button-accept--2SBJ-"));
                if(!acceptButtons.isEmpty() && acceptButtons.get(0).getAttribute("class").contains("_enabled--1njsj")) {
                    acceptButtons.get(0).click();
                    TimeUnit.MILLISECONDS.sleep(300);
                    System.out.println("Accept button appeared and was successfully clicked...");
                    continue;
                }

                WebElement input = driver.findElement(By.className("sum-panel__input--2FGMZ"));
                input.click();
                input.clear();
                input.sendKeys("" + amount);

                acceptButtons = driver.findElements(By.className("button-accept--2SBJ-"));
                if(!acceptButtons.isEmpty() && acceptButtons.get(0).getAttribute("class").contains("_enabled--1njsj")) {
                    acceptButtons.get(0).click();
                    TimeUnit.MILLISECONDS.sleep(300);
                    System.out.println("Accept button appeared and was successfully clicked...");
                    continue;
                }

                errorContainers = driver.findElements(By.className("error-box--3tiP1"));
                if(!errorContainers.isEmpty()) {
                    errorContainers.get(0).findElement(By.className("button--54u30")).click();
                    System.out.println("Error box appeared and was successfully clicked...");
                    continue;
                }

                driver.findElement(By.className("button--54u30")).click();

                //List<WebElement> placedBetsNow = driver.findElements(By.className("coupon__table-row--3vSjv"));
                //if(placedBetsNow.size() > placedBetsBefore.size()) {
                for(int i=0; i<10; ++i){
                    if(driver.findElements(By.className("seconds-overlay--1b4JN")).isEmpty())
                        break;
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                return true;
                //}
            }

        }

        System.out.println("Somehow we reached the end while placing bet on Fonbet.");
        return false;
    }

    private static boolean mapFoundOnFonbet(String mapNameRequired, String mapNameCurrent){
        return (mapNameRequired.equalsIgnoreCase("Map 1 Winner")
                        && mapNameCurrent.equalsIgnoreCase("1-я карта")) ||
                (mapNameRequired.equalsIgnoreCase("Map 2 Winner")
                        && mapNameCurrent.equalsIgnoreCase("2-я карта")) ||
                (mapNameRequired.equalsIgnoreCase("Map 3 Winner")
                        && mapNameCurrent.equalsIgnoreCase("3-я карта")) ||
                (mapNameRequired.equalsIgnoreCase("Map 4 Winner")
                        && mapNameCurrent.equalsIgnoreCase("4-я карта")) ||
                (mapNameRequired.equalsIgnoreCase("Map 5 Winner")
                        && mapNameCurrent.equalsIgnoreCase("5-я карта"));

    }

}
