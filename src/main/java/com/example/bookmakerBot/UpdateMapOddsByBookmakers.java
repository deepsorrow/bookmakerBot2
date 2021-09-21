package com.example.bookmakerBot;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpdateMapOddsByBookmakers {


    public static void onThunderpick(RemoteWebDriver driver, ArrayList<Match> matches) {
        String rowName;

        ArrayList<Match> endedMatches = new ArrayList<>();
        for (Match match : matches) {
            driver.switchTo().window(match.windowHandle);
            try {
                List<WebElement> elementsFound = driver.findElements(By.className("thp-search"));
                if (!elementsFound.isEmpty()) {
                    endedMatches.add(match);
                    driver.close();
                    continue;
                }
            } catch (Exception e) {
            }

            match.mapOdds.clear();
            List<WebElement> overall = driver.findElements(By.className("odds-button--variant-light"));
            double overallOddsLeft;
            try {
                overallOddsLeft = Double.parseDouble(overall.get(0).findElement(By.tagName("span")).getText());
            } catch (Exception e){
                overallOddsLeft = -1.0;
            }
            double overallOddsRight;
            try {
                overallOddsRight = Double.parseDouble(overall.get(1).findElement(By.tagName("span")).getText());
            } catch (Exception e){
                overallOddsRight = -1.0;
            }

            match.mapOdds.add(new MapOdds(match.bookmaker, overallOddsLeft, overallOddsRight, "Overall"));

            List<WebElement> matchRows = driver.findElements(By.className("market-row__container"));
            for (WebElement matchRow : matchRows) {
                rowName = matchRow.findElement(By.className("market-row__name")).getText();
                if (!(rowName.startsWith("Map ") && rowName.endsWith(" Winner") && rowName.length() == 12))
                    continue;

                double oddsLeft = 0;
                double oddsRight = 0;
                WebElement oddsLeftSpan = null;
                try {
                    oddsLeftSpan = matchRow.findElement(By.className("market-row__odds--left"))
                            .findElement(By.tagName("span"));
                } catch (Exception e) {
                    oddsLeft = -1.0;
                }
                if (oddsLeft != -1.0)
                    oddsLeft = Double.parseDouble(oddsLeftSpan.getText());

                WebElement oddsRightSpan = null;
                try {
                    oddsRightSpan = matchRow.findElement(By.className("market-row__odds--right"))
                            .findElement(By.tagName("span"));
                } catch (Exception e) {
                    oddsRight = -1.0;
                }
                if (oddsRight != -1.0)
                    oddsRight = Double.parseDouble(oddsRightSpan.getText());

                match.mapOdds.add(new MapOdds(match.bookmaker, oddsLeft, oddsRight, rowName));
            }
        }

        for (Match endedMatch : endedMatches)
            matches.remove(endedMatch);

    }

    public static void onFonbet(RemoteWebDriver driver, ArrayList<Match> matches, LiveGame liveGame) {
        if (!liveGame.windowHandle.isEmpty())
            driver.switchTo().window(liveGame.windowHandle);
        else {
            driver.get(liveGame.link);
            liveGame.windowHandle = driver.getWindowHandle();
        }

        List<WebElement> title_texts = driver.findElements(By.className("table__match-title-text"));
        List<WebElement> datakey_elements = new ArrayList<>();
        for(int i=0; i<10; ++i) {
            try {
                for (WebElement title_text : title_texts) {
                    if (title_text.getTagName().equals("a"))
                        datakey_elements.add(title_text);
                }
                break;
            } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                title_texts = driver.findElements(By.className("table__match-title-text"));
                datakey_elements.clear();
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (Exception ignored1) {}
            }
        }

        List<WebElement> moreOrLessButtons = driver.findElements(By.className("moreOrLessButton"));
        for (WebElement moreOrLessButton : moreOrLessButtons)
            if (moreOrLessButton.getText().contains("Показать"))
                moreOrLessButton.click();

        for (Match match : matches) {
            WebElement matchBody = null;
            for (WebElement datakey_element : datakey_elements) {
                if (datakey_element.getAttribute("href").equals(match.datakey)) {
                    matchBody = datakey_element.findElement(By.xpath("./../../../.."));
                    break;
                }
            }

            if (matchBody == null)
                continue;

            String title = matchBody.findElement(By.className("table__title-text")).getText();
            List<WebElement> rows = matchBody.findElements(By.className("table__row"));
            boolean itIsSecondRow = true;
            boolean reachedMatch = false;
            match.mapOdds.clear();
            while (true) {
                try {
                    for (WebElement row : rows) {
                        if (row.getAttribute("class").contains("table__row _type_segment"))
                            continue;
                        if (!reachedMatch) {
                            WebElement match_title = row.findElements(By.className("table__match-title-text")).get(0);
                            if (match_title.getTagName().equals("a") && match_title.getAttribute("href").equals(match.datakey))
                                reachedMatch = true;
                            else
                                continue;
                        }

                        List<WebElement> koeffs = row.findElements(By.className("table__col"));

                        if (title.toUpperCase().contains("ИЗ 1-Й КАРТЫ")) {
                            List<Double> odds = getOddsForFonbet(koeffs);
                            match.mapOdds.add(new MapOdds(match.bookmaker, odds.get(0), odds.get(1), "Map 1 Winner"));
                            break;
                        } else {
                            String mapName = "";
                            if (itIsSecondRow) { // на общий исход матча по всем карта не ставим
                                itIsSecondRow = false;
                                mapName = "Overall";
                            } else {
                                mapName = row.findElement(By.className("table__match-title-text")).getText();
                                if (mapName.equals("1-я карта"))
                                    mapName = "Map 1 Winner";
                                else if (mapName.equals("2-я карта"))
                                    mapName = "Map 2 Winner";
                                else if (mapName.equals("3-я карта"))
                                    mapName = "Map 3 Winner";
                                else if (mapName.equals("4-я карта"))
                                    mapName = "Map 4 Winner";
                                else if (mapName.equals("5-я карта"))
                                    mapName = "Map 5 Winner";
                                else if (mapName.contains("Свернуть"))
                                    continue;
                                else {
                                    System.out.println("Fonbet. Unexpected map name: " + mapName);
                                    break;
                                }
                            }

                            List<Double> odds = getOddsForFonbet(koeffs);
                            match.mapOdds.add(new MapOdds(match.bookmaker, odds.get(0), odds.get(1), mapName));
                        }
                    }
                } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
                    match.mapOdds.clear();
                    continue;
                }
                break;
            }
        }
    }

    private static List<Double> getOddsForFonbet(List<WebElement> koeffs) {
        String koeffHome = koeffs.get(2).getText();
        double oddsLeft;
        if (koeffHome.isEmpty())
            oddsLeft = 0;
        else if (!koeffs.get(2).getAttribute("class").contains("_state_blocked")) {
            oddsLeft = Double.parseDouble(koeffHome);
        } else { // koeffs.get(2).getAttribute("class").contains("_type_normal")
            oddsLeft = -1.0;
        }

        String koeffAway = koeffs.get(4).getText();
        double oddsRight;
        if (koeffAway.isEmpty())
            oddsRight = 0;
        else if (!koeffs.get(4).getAttribute("class").contains("_state_blocked")) {
            oddsRight = Double.parseDouble(koeffAway);
        } else {
            oddsRight = -1.0;
        }

        return Arrays.asList(oddsLeft, oddsRight);
    }
}
