package com.example.bookmakerBot;

import org.openqa.selenium.By;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AddRemoveMatchesByBookmakers {
    public static void onThunderpick(Bookmaker bookmaker, RemoteWebDriver driver, BrowserProfile bp) throws Exception {
        ArrayList<Match> allCurrentMatches = new ArrayList<>();
        for (LiveGame liveGame : bookmaker.liveGamesLinks) {
            driver.switchTo().window(liveGame.windowHandle);
            ArrayList<Match> currentMatches = InitMatchesByBookmakers.getMatchesOnThunderpick(driver, liveGame,
                    bookmaker, false);
            // if there were some matches, and now they're gone maybe it's time to refresh page? _state_disabled--1t7v7
            if(currentMatches.isEmpty()){
                if(!Match.getMatchesByGame(bookmaker.matches, liveGame.game).isEmpty()) {
                    driver.switchTo().window(liveGame.windowHandle);
                    driver.navigate().refresh();

                    if(driver.findElements(By.className("user-summary__content")).isEmpty()){
                        System.out.println("Noticed that was logged out from thunderpick. Restarting...");
                        driver.quit();
                        bp.run(new String[]{});
                        return;
                    }

                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (Exception ignored) {
                    }
                    currentMatches = InitMatchesByBookmakers.getMatchesOnThunderpick(driver, liveGame,
                            bookmaker, false);
                }
            }
            allCurrentMatches.addAll(currentMatches);
        }
        addDeleteMatchesGeneral(allCurrentMatches, bookmaker, driver);

        try {
            if (bookmaker.matches.isEmpty()) {
                System.out.println("No current matches on thunderpick. Waiting...");
                TimeUnit.SECONDS.sleep(Utils.getRandomNumber(30, 60));
            }
        } catch (Exception ignored) {}
    }

    public static void onFonbet(Bookmaker bookmaker, RemoteWebDriver driver) throws Exception {
        ArrayList<Match> currentMatches = InitMatchesByBookmakers.getMatchesOnFonbet(driver,
                bookmaker.liveGamesLinks.get(0), bookmaker);
        addDeleteMatchesGeneral(currentMatches, bookmaker, driver);
    }

    private static void addDeleteMatchesGeneral(ArrayList<Match> currentMatches, Bookmaker bookmaker, RemoteWebDriver driver){
        ArrayList<Match> matchesToAdd    = new ArrayList<>(currentMatches);
        ArrayList<Match> matchesToDelete = new ArrayList<>();

        for(Match match : bookmaker.matches){
            Match matchFound = null;
            for(Match newMatch : matchesToAdd) {
                if (match.datakey.equalsIgnoreCase(newMatch.datakey)){
                    matchFound = newMatch;
                    break;
                }
            }

            if(matchFound != null)
                matchesToAdd.remove(matchFound);
            else
                matchesToDelete.add(match);
        }

        String currentWindowHandle = driver.getWindowHandle();
        for(Match match : matchesToDelete) {
            if(bookmaker.name.equals(Bookmaker.THUNDERPICK)) {
                driver.switchTo().window(match.windowHandle);
                driver.close();
            }
            bookmaker.matches.remove(match);
        }
        for(Match newMatch : matchesToAdd) {
            if(bookmaker.name.equals(Bookmaker.THUNDERPICK)) {
                driver.switchTo().newWindow(WindowType.TAB);
                driver.get(newMatch.href);
                newMatch.windowHandle = driver.getWindowHandle();
            }
            bookmaker.matches.add(newMatch);
        }
        driver.switchTo().window(currentWindowHandle);
    }
}
