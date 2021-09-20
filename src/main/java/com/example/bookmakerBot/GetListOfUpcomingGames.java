package com.example.bookmakerBot;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class GetListOfUpcomingGames {

    public static List<UpcomingGame> onFonbet(RemoteWebDriver driver, Bookmaker bookmaker) throws InterruptedException {
        ArrayList<UpcomingGame> upcomingGames = new ArrayList<>();
        for (UpcomingGamesLink upcomingGameLink : bookmaker.upcomingGamesLinks) {
            openAndLoadPage(driver, upcomingGameLink, "sport-base-event--dByYH");

            String tournamentName = "";
            //for scroll
            //driver.findElements(By.className("sport-base-event--dByYH")).get(0).click();
            driver.findElements(By.className("filter-item-drop-down-element__button--gAVB3")).get(2).click();
            List<WebElement> tournamentLinks = driver.findElement(By.className("filter-item-drop-down-element__popup__panel--zfZLv"))
                    .findElements(By.tagName("a"));
            ArrayList<TournamentLinkFonbet> links = TournamentLinkFonbet.parseLinks(tournamentLinks);
            for (TournamentLinkFonbet tournamentLink : links) {
                tournamentName = tournamentLink.tournamentName;
                if(tournamentLink.href.equals("https://www.fonbet.ru/sports/esports")
                        || !(Utils.containsIgnoreCase(tournamentName, "DOTA 2")
                        || Utils.containsIgnoreCase(tournamentName, "COUNTER-STRIKE")))
                    continue;
                driver.get(tournamentLink.href);

                List<WebElement> events = getEventsForFonbet(driver);
                for (WebElement event : events) {
                    if (event.getAttribute("class").contains("sport-competition--rj3-5")) {
                        tournamentName = event.findElements(By.className("table-component-text--2U5hR"))
                                .get(0).getText();
                    } else if (event.getAttribute("class").contains("sport-base-event--dByYH")) {
                        if (!tournamentName.contains("КАРТ"))
                            continue;

                        String game;
                        if (Utils.containsIgnoreCase(tournamentName, "COUNTER-STRIKE"))
                            game = "CS:GO";
                        else if (Utils.containsIgnoreCase(tournamentName, "DOTA 2"))
                            game = "DOTA 2";
                        else
                            continue;

                        try {
                            String eventName = event.findElement(By.className("sport-event__name--mWe5W")).getText();
                            String[] teams = eventName.split(" — ");
                            teams[0] = teams[0].replace("Game 1. ", "");
                            teams[0] = teams[0].replace("Game 2. ", "");
                            teams[0] = teams[0].replace("Game 3. ", "");
                            teams[0] = teams[0].replace("Game 4. ", "");
                            teams[0] = teams[0].replace("Game 5. ", "");
                            String time = event.findElement(By.className("event-block-planned-time__time--AxDhC")).getText();

                            Date beginDate = parseBeginDateForFonbet(time);

                            UpcomingGame upcomingGame = new UpcomingGame(teams[0], teams[1], beginDate, game,
                                    tournamentName, bookmaker);
                            if (!UpcomingGame.gameExist(upcomingGame, upcomingGames))
                                upcomingGames.add(upcomingGame);
                        } catch (org.openqa.selenium.NoSuchElementException e) {
                            continue;
                        }


                    }
                }
            }
        }

        driver.switchTo().newWindow(WindowType.TAB);

        return upcomingGames;
    }

    public static Date parseBeginDateForFonbet(String time) {
        String[] timeParts = time.split(" ");
        String[] timeDateParts = timeParts[timeParts.length - 1].split(":");
        ArrayList<String> timePartsFull = new ArrayList<>();
        for (int i = 0; i < timeParts.length - 1; ++i)
            timePartsFull.add(timeParts[i]);
        timePartsFull.add(timeDateParts[0]);
        timePartsFull.add(timeDateParts[1]);

        Calendar beginDate = Calendar.getInstance();
        beginDate.set(Calendar.SECOND, 0);
        beginDate.set(Calendar.MILLISECOND, 0);
        if (timePartsFull.get(0).equalsIgnoreCase("Сегодня")) {
            beginDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePartsFull.get(2)));
            beginDate.set(Calendar.MINUTE, Integer.parseInt(timePartsFull.get(3)));
        } else if (timePartsFull.get(0).equalsIgnoreCase("Завтра")) {
            beginDate.add(Calendar.DATE, 1);
            beginDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePartsFull.get(2)));
            beginDate.set(Calendar.MINUTE, Integer.parseInt(timePartsFull.get(3)));
        } else {
            beginDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePartsFull.get(3)));
            beginDate.set(Calendar.MINUTE, Integer.parseInt(timePartsFull.get(4)));
            beginDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timePartsFull.get(0)));
            beginDate.set(Calendar.MONTH, getMonthNumberFonbet(timePartsFull.get(1)));
        }

        return beginDate.getTime();
    }

    public static List<WebElement> getEventsForFonbet(RemoteWebDriver driver) throws InterruptedException {
        List<WebElement> events = null;
        while(true) {
            boolean loaded = false;
            for(int i=0; i<15; ++i) {
                try {
                    events = driver.findElement(By.className("sport-section-virtual-list--3gOAc"))
                            .findElements(By.xpath("./child::*"));
                    loaded = true;
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    TimeUnit.SECONDS.sleep(2);
                }
            }

            if(loaded)
                break;
            else
                driver.navigate().refresh();
        }

        return events;
    }

    public static List<UpcomingGame> onThunderpick(RemoteWebDriver driver, Bookmaker bookmaker) throws InterruptedException {
        ArrayList<UpcomingGame> upcomingGames = new ArrayList<>();
        for (UpcomingGamesLink upcomingGameLink : bookmaker.upcomingGamesLinks) {
            openAndLoadPage(driver, upcomingGameLink, "match-group");

            while (true) {
                int upcomingGamesSizeBefore = upcomingGames.size();
                List<WebElement> groups = driver.findElements(By.className("match-group"));
                for (WebElement group : groups) {
                    String title = group.findElement(By.className("match-group__title")).getText();
                    List<WebElement> matches = group.findElements(By.className("match-row__container"));
                    for (WebElement match : matches) {
                        String tournament = match.findElement(By.className("match-row__championship")).getText();
                        String homeTeam = match.findElement(By.className("match-row__home-name")).getText();

                        String awayTeam = match.findElement(By.className("match-row__away-name")).getText();
                        String[] hoursAndMinutes = match.findElement(By.className("match-row__match-info"))
                                .getText().split(":");

                        Calendar beginDate = Calendar.getInstance();
                        beginDate.set(Calendar.SECOND, 0);
                        beginDate.set(Calendar.MILLISECOND, 0);
                        beginDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hoursAndMinutes[0]));
                        beginDate.set(Calendar.MINUTE, Integer.parseInt(hoursAndMinutes[1]));
                        if (!title.equalsIgnoreCase("Предстоящие матчи")) {
                            String[] timeParts = title.split(", ");
                            String[] timeParts2 = new String[]{};
                            try {
                                timeParts2 = timeParts[1].split(" ");
                            } catch (Exception e) {

                            }
                            beginDate.set(Calendar.MONTH, getMonthNumberThunderpick(timeParts2[0]));
                            String day = timeParts2[1].replaceAll("[\\D.]", "");
                            beginDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                        }

                        UpcomingGame upcomingGame = new UpcomingGame(homeTeam, awayTeam, beginDate.getTime(),
                                upcomingGameLink.game, tournament, bookmaker);
                        if (!UpcomingGame.gameExist(upcomingGame, upcomingGames))
                            upcomingGames.add(upcomingGame);
                    }
                }
                if (upcomingGamesSizeBefore == upcomingGames.size())
                    break;

                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,300)");
            }
        }

        driver.switchTo().newWindow(WindowType.TAB);

        return upcomingGames;
    }

    public static void openAndLoadPage(RemoteWebDriver driver, UpcomingGamesLink upcomingGameLink,
                                       String exampleClass) throws InterruptedException {
        if (upcomingGameLink.windowHandle.isEmpty()) {
            if (driver.getWindowHandles().size() != 1)
                driver.switchTo().newWindow(WindowType.TAB);
            driver.get(upcomingGameLink.link);
        } else {
            driver.switchTo().window(upcomingGameLink.windowHandle);
        }

        TimeUnit.SECONDS.sleep(3);

        // wait until load
        boolean loaded = false;
        while(true) {
            for (int i = 0; i < 10; ++i) {
                List<WebElement> events = driver.findElements(By.className(exampleClass));
                if (events.size() == 0)
                    TimeUnit.SECONDS.sleep(2);
                else {
                    loaded = true;
                    break;
                }
            }
            if(loaded)
                break;
            else
                driver.navigate().refresh();
        }
    }

    public static int getMonthNumberFonbet(String month) {
        if (Utils.containsIgnoreCase(month, "январ"))
            return 1;
        else if (Utils.containsIgnoreCase(month, "феврал"))
            return 2;
        else if (Utils.containsIgnoreCase(month, "март"))
            return 3;
        else if (Utils.containsIgnoreCase(month, "апрел"))
            return 4;
        else if (Utils.containsIgnoreCase(month, "ма"))
            return 5;
        else if (Utils.containsIgnoreCase(month, "июн"))
            return 6;
        else if (Utils.containsIgnoreCase(month, "июл"))
            return 7;
        else if (Utils.containsIgnoreCase(month, "август"))
            return 8;
        else if (Utils.containsIgnoreCase(month, "сентябр"))
            return 9;
        else if (Utils.containsIgnoreCase(month, "октябр"))
            return 10;
        else if (Utils.containsIgnoreCase(month, "ноябр"))
            return 11;
        else if (Utils.containsIgnoreCase(month, "декабр"))
            return 12;
        else
            return -999;
    }

    public static int getMonthNumberThunderpick(String month) {
        if (Utils.containsIgnoreCase(month, "January"))
            return 1;
        else if (Utils.containsIgnoreCase(month, "February"))
            return 2;
        else if (Utils.containsIgnoreCase(month, "March"))
            return 3;
        else if (Utils.containsIgnoreCase(month, "April"))
            return 4;
        else if (Utils.containsIgnoreCase(month, "May"))
            return 5;
        else if (Utils.containsIgnoreCase(month, "June"))
            return 6;
        else if (Utils.containsIgnoreCase(month, "Jule"))
            return 7;
        else if (Utils.containsIgnoreCase(month, "August"))
            return 8;
        else if (Utils.containsIgnoreCase(month, "September"))
            return 9;
        else if (Utils.containsIgnoreCase(month, "October"))
            return 10;
        else if (Utils.containsIgnoreCase(month, "November"))
            return 11;
        else if (Utils.containsIgnoreCase(month, "December"))
            return 12;
        else
            return -999;
    }

    public static void scrollDownFonbet(RemoteWebDriver driver) throws InterruptedException {
        List<WebElement> events = driver.findElements(By.className("sport-base-event--dByYH"));
        for (WebElement event : events) {
            try {
                event.click();
                break;
            } catch (Exception e) {
                continue;
            }
        }
        WebElement container = driver.findElement(By.tagName("body"));
        container.sendKeys(Keys.ARROW_DOWN);
        TimeUnit.MILLISECONDS.sleep(300);
    }
}
