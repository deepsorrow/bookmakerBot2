package com.example.bookmakerBot;

import org.hibernate.engine.transaction.jta.platform.internal.WeblogicJtaPlatform;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {
    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static String printMatchInfo(MatchInfo matchInfo, ArrayList<Bookmaker> bookmakers) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        String output = "";
        String title = "---" + matchInfo.getHomeTeam() + " vs " + matchInfo.getAwayTeam() + "---" + "\n";
        for (MapInfo mapInfo : matchInfo.mapInfos) {
            for (Bookmaker bookmaker : bookmakers) {
                double homeOdds = mapInfo.getHomeOddsBy(bookmaker);
                double awayOdds = mapInfo.getAwayOddsBy(bookmaker);
                if (homeOdds == -2.0 || awayOdds == -2.0)
                    continue;

                output += "" + dateFormat.format(new Date()) + ": " + mapInfo.getMapName() + " " + bookmaker.name + " "
                        + ((homeOdds == -1.0) ? "L" : homeOdds) + " "
                        + ((awayOdds == -1.0) ? "L" : awayOdds) + "\n";
            }
        }
        if (!output.isEmpty())
            output = title + output;

        return output;
    }

    public static boolean containsIgnoreCase(String text1, String text2) {
        return text1.toLowerCase().contains(text2.toLowerCase());
    }

    public static boolean forkExists(double k1, double k2) {
        return (1 / k1) + (1 / k2) <= 0.95;
    }

    public static boolean placeOnHomeThunderpickIsBetter(double h1, double a1, double h2, double a2) {
        return ((1 / h1) + (1 / a2)) < ((1 / h2) + (1 / a1));
    }

    public static void click(RemoteWebDriver driver, WebElement element){
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }

    public static Bookmaker findThunderpick(ArrayList<Bookmaker> bookmakers) {
        for (Bookmaker bookmaker : bookmakers) {
            if (bookmaker.name.equals(Bookmaker.THUNDERPICK))
                return bookmaker;
        }

        return null;
    }

    public static int getSecondBet(double K1, double K2, double firstBet) {
        int result = (int) ((K1 * firstBet) / K2);
        return roundToNearestBy50(result);
    }

    public static int roundToNearestBy50(int x) {
        if (x % 50 < 25) {
            return x - (x % 50);
        } else if (x % 50 > 25) {
            return x + (50 - (x % 50));
        } else {
            return x + 25; //when it is halfawy between the nearest 50 it will automatically round up, change this
            // line to 'return x - 25' if you want it to automatically round down
        }
    }

    public static Integer getSecondCoeffNumForFonbet(RemoteWebDriver driver) {
        List<WebElement> sportCompetitions = driver.findElements(By.className("sport-competition--rj3-5"));
        if (sportCompetitions.isEmpty())
            return null;

        List<WebElement> tableComponentTexts = sportCompetitions.get(0).findElements(By.xpath("./child::*"));
        if (tableComponentTexts.isEmpty())
            return null;

        if (tableComponentTexts.get(4).equals("X"))
            return 2;
        else
            return 1;
    }
}
