package com.example.bookmakerBot;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InitMatchesByBookmakers {
    public static ArrayList<Match> getMatchesOnThunderpick(RemoteWebDriver driver, LiveGame liveGame,
                                                           Bookmaker bookmaker, boolean openTabs) {
        if (!liveGame.windowHandle.isEmpty())
            driver.switchTo().window(liveGame.windowHandle);
        else {
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(liveGame.link);
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,300)");
            liveGame.windowHandle = driver.getWindowHandle();
        }

        ArrayList<Match> resultMatches = new ArrayList<>();

        while (driver.findElements(By.className("match-row__container")).isEmpty()) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (Exception ignored) {
            }
        }

        WebElement liveGroup = null;
        while (true) {
            try {
                List<WebElement> groups = driver.findElements(By.className("match-group"));
                for (WebElement group : groups) {
                    List<WebElement> title = group.findElements(By.className("match-group__title"));
                    if (title.size() != 0 && title.get(0).getText().startsWith("Live")) {
                        liveGroup = group;
                        break;
                    }
                }
                break;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                System.out.println("Stale element exception occurred while accessing groups on thunderpick.");
            }
        }

        if (liveGroup == null)
            return resultMatches;

        List<WebElement> webMatches = liveGroup.findElements(By.className("match-row__container"));

        for (WebElement match : webMatches) {
            if (!driver.getCurrentUrl().equals(liveGame.link))
                driver.switchTo().window(liveGame.windowHandle);

            String datakey = match.getAttribute("data-key");

            WebElement home = match.findElement(By.className("match-row__home-name"));
            WebElement away = match.findElement(By.className("match-row__away-name"));

            String homeTeam = home.getText();
            String awayTeam = away.getText();

            String href = match.findElement(By.className("match-row__total-markets")).getAttribute("href");
            if (href == null)
                continue;

            if (openTabs) {
                driver.switchTo().newWindow(WindowType.TAB);
                driver.navigate().to(href);
            }

            Match thisMatch = new Match(homeTeam, awayTeam, liveGame.game, datakey, driver.getWindowHandle());
            thisMatch.href = href;
            thisMatch.setOwner(bookmaker);
            resultMatches.add(thisMatch);
        }

        return resultMatches;
    }

    public static ArrayList<Match> getMatchesOnFonbet(RemoteWebDriver driver, LiveGame liveGame, Bookmaker bookmaker)
            throws InterruptedException {
        if (!liveGame.windowHandle.isEmpty())
            driver.switchTo().window(liveGame.windowHandle);
        else {
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(liveGame.link);
            liveGame.windowHandle = driver.getWindowHandle();
        }

        ArrayList<Match> resultMatches = new ArrayList<>();

        while (true) {
            try {
                driver.findElement(By.className("sport-section-virtual-list--3gOAc"));
                break;
            } catch (org.openqa.selenium.NoSuchElementException e) {
                TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));
            }
        }

        String tournamentName = "";
        List<WebElement> events = driver.findElement(By.className("sport-section-virtual-list--3gOAc"))
                .findElements(By.xpath("./child::*"));
        for (WebElement event : events) {
            if (event.getAttribute("class").contains("sport-competition--rj3-5")) {
                tournamentName = event.findElements(By.className("table-component-text--2U5hR"))
                        .get(0).getText();
            } else if (event.getAttribute("class").contains("sport-base-event--dByYH")) {
                if (!tournamentName.contains("КАРТ") ||
                        event.findElements(By.className("table-component-favorite--3dbUN")).isEmpty())
                    continue;

                String game;
                if (Utils.containsIgnoreCase(tournamentName, "COUNTER-STRIKE"))
                    game = "CS:GO";
                else if (Utils.containsIgnoreCase(tournamentName, "DOTA 2"))
                    game = "DOTA 2";
                else
                    continue;

                WebElement eventNameElement = event.findElements(By.className("table-component-text--2U5hR")).get(0);
                String eventName = eventNameElement.getText();
                String datakey = eventNameElement.getAttribute("href");
                String[] teams = eventName.split(" — ");
                teams[0] = teams[0].replace("Game 1. ", "");
                teams[0] = teams[0].replace("Game 2. ", "");
                teams[0] = teams[0].replace("Game 3. ", "");
                teams[0] = teams[0].replace("Game 4. ", "");
                teams[0] = teams[0].replace("Game 5. ", "");

                Match thisMatch = new Match(teams[0], teams[1], game, datakey, driver.getWindowHandle());

                thisMatch.setOwner(bookmaker);
                resultMatches.add(thisMatch);
            }
        }
        //for scroll
        //driver.findElements(By.className("sport-base-event--dByYH")).get(0).click();

//        List<WebElement> bodies = driver.findElements(By.className("table__body"));
//        for (WebElement body : bodies) {
//            List<WebElement> rows = body.findElements(By.className("table__row"));
//            boolean itIsFirst = true;
//            String title = "";
//            for (WebElement row : rows) {
//                if (itIsFirst) {
//                    title = row.findElement(By.className("table__title-text")).getText();
//                    itIsFirst = false;
//                    continue;
//                }
//
//                if (row.findElements(By.tagName("td")).get(0).getAttribute("class").contains("_indent_1")
//                && row.findElements(By.tagName("td")).get(1).getAttribute("colspan").equals("0")) {
//                    String teamNames = "";
//                    String datakey   = "";
//                    List<WebElement> title_texts = row.findElements(By.className("table__match-title")).get(0)
//                            .findElements(By.className("table__match-title-text"));
//                    for (WebElement elem : title_texts) {
//                        if (elem.getTagName().equals("div")) {
//                            teamNames = elem.getText();
//                        } else if (elem.getTagName().equals("a")) {
//                            datakey = elem.getAttribute("href");
//                        }
//                    }
//
//                    String[] names = teamNames.split(" — ");
//                    Match thisMatch;
//                    if (title.contains("COUNTER-STRIKE"))
//                        thisMatch = new Match(names[0], names[1], "CS:GO", datakey, driver.getWindowHandle());
//                    else if (title.contains("DOTA 2"))
//                        thisMatch = new Match(names[0], names[1], "DOTA 2", datakey, driver.getWindowHandle());
//                    else
//                        continue;
//
//                    thisMatch.setOwner(bookmaker);
//                    resultMatches.add(thisMatch);
//                }
//            }
//        }

        return resultMatches;
    }
}
