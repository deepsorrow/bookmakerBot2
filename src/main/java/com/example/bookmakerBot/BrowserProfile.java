package com.example.bookmakerBot;

import com.example.bookmakerBot.db.SameTeamsRepo;
import com.example.bookmakerBot.db.UpcomingGameRepo;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.print.Book;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class BrowserProfile {

    @Autowired
    UpcomingGameRepo upcomingGameRepo;
    @Autowired
    SameTeamsRepo sameTeamsRepo;

    public void run(String[] args) throws Exception {
        BrowserProfile bp = new BrowserProfile();
        String profileId = "81dbcb80-b49d-47c3-bff9-9216b67cbe8f";
        DesiredCapabilities dc = new DesiredCapabilities();
        RemoteWebDriver driver;
        while (true) {
            try {
                driver = new RemoteWebDriver(new URL(bp.startProfile(profileId)), dc);
                break;
            } catch (IOException e) {
                TimeUnit.SECONDS.sleep(2);
            }
        }

        // Thunderpick init
        ArrayList<Bookmaker> bookmakers = new ArrayList<>();

        Bookmaker thunderpick = new Bookmaker(Bookmaker.THUNDERPICK);
        thunderpick.liveGamesLinks.add(new LiveGame("DOTA 2",
                "https://thunderpick.com/ru/esports/dota-2?list=live"));
        thunderpick.liveGamesLinks.add(new LiveGame("CS:GO",
                "https://thunderpick.com/ru/esports/csgo?list=live"));
//        thunderpick.upcomingGamesLinks.add(new UpcomingGamesLink("DOTA 2",
//                "https://thunderpick.com/ru/esports/dota-2?list=upcoming"));
//        thunderpick.upcomingGamesLinks.add(new UpcomingGamesLink("CS:GO",
//                "https://thunderpick.com/ru/esports/csgo?list=upcoming"));

        // Fonbet init
        Bookmaker fonbet = new Bookmaker(Bookmaker.FONBET);
        fonbet.liveGamesLinks.add(new LiveGame("Any",
                "https://www.fonbet.ru/live/esports"));
//        fonbet.upcomingGamesLinks.add(new UpcomingGamesLink("Any",
//                "https://www.fonbet.ru/sports/esports?mode=1"));

        bookmakers.add(thunderpick);
        bookmakers.add(fonbet);

//        for(Bookmaker bookmaker : bookmakers) {
//            if(bookmaker.name.equals(Bookmaker.FONBET))
//                bookmaker.upcomingGames = GetListOfUpcomingGames.onFonbet(driver, bookmaker);
//            else if(bookmaker.name.equals(Bookmaker.THUNDERPICK))
//                bookmaker.upcomingGames = GetListOfUpcomingGames.onThunderpick(driver, bookmaker);
//
//            for(UpcomingGame upcomingGame : bookmaker.upcomingGames){
//                UpcomingGame foundGame = upcomingGameRepo.getByHomeTeamAndAwayTeamAndGameAndTournamentAndBeginDate(
//                        upcomingGame.homeTeam, upcomingGame.awayTeam, upcomingGame.game, upcomingGame.tournament,
//                        upcomingGame.beginDate);
//                if(foundGame == null)
//                    upcomingGameRepo.save(upcomingGame);
//            }
//        }
//
//        ArrayList<SameGamesResult> sameGamesResults = UpcomingGame.findTeamsWithSameName(bookmakers);
//        for(SameGamesResult sameGamesResult : sameGamesResults) {
//            SameGamesResult foundEntity = sameTeamsRepo.getByTeamNamesLike(sameGamesResult.teamNames);
//            if(foundEntity != null)
//                sameTeamsRepo.delete(foundEntity);
//
//                sameTeamsRepo.save(sameGamesResult);
//        }

        String firstEmptyTabWindowHandle = driver.getWindowHandle();
        for (Bookmaker bookmaker : bookmakers) {
            for (LiveGame liveGame : bookmaker.liveGamesLinks) {
                if (bookmaker.loginRequired) {
                    if (bookmaker.name.equals(Bookmaker.THUNDERPICK))
                        LoginByBookmakers.onThunderpick(driver);
                    else if (bookmaker.name.equals(Bookmaker.FONBET))
                        LoginByBookmakers.onFonbet(driver);
                    bookmaker.loginRequired = false;
                }

                if (bookmaker.name.equals(Bookmaker.THUNDERPICK)) {
                    bookmaker.matches.addAll(InitMatchesByBookmakers.getMatchesOnThunderpick(driver, liveGame,
                            bookmaker, true));
                } else if (bookmaker.name.equals(Bookmaker.FONBET)) {
                    bookmaker.matches.addAll(InitMatchesByBookmakers.getMatchesOnFonbet(driver, liveGame, bookmaker));
                }
            }
        }
        String currentWindowHandle = driver.getWindowHandle();
        driver.switchTo().window(firstEmptyTabWindowHandle);
        driver.close();
        driver.switchTo().window(currentWindowHandle);

        while (true) {
            try {
                for (Bookmaker bookmaker : bookmakers) {
                    if (bookmaker.name.equals(Bookmaker.THUNDERPICK))
                        UpdateMapOddsByBookmakers.onThunderpick(driver, bookmaker.matches);
                    else if (bookmaker.name.equals(Bookmaker.FONBET))
                        UpdateMapOddsByBookmakers.onFonbet(driver, bookmaker.matches, bookmaker.liveGamesLinks.get(0));
                }

                ArrayList<MatchInfo> matchInfos = new ArrayList<>();
                for (Bookmaker bookmaker : bookmakers) {
                    for (Match match : bookmaker.matches) {
                        MatchInfo.insertMatchToRightMatchInfo(match, matchInfos);
                    }
                }

                for (MatchInfo matchInfo : matchInfos) {
                    matchInfo.initMapInfos();
                    System.out.println(Utils.printMatchInfo(matchInfo, bookmakers));

                    for (MapInfo mapInfo : matchInfo.mapInfos) {
                        double homeOdds1 = mapInfo.getHomeOddsBy(thunderpick);
                        double awayOdds1 = mapInfo.getAwayOddsBy(thunderpick);
                        if (homeOdds1 <= 1 || awayOdds1 <= 1)
                            continue;

                        for (Bookmaker bookmaker2 : bookmakers) {
                            if (bookmaker2 == thunderpick)
                                continue;

                            double homeOdds2 = mapInfo.getHomeOddsBy(bookmaker2);
                            double awayOdds2 = mapInfo.getAwayOddsBy(bookmaker2);
                            if (homeOdds2 <= 1 || awayOdds2 <= 1)
                                continue;

                            //test
                            //Match matchOnFonbet      = Match.getMatchByBookmaker(matchInfo.matches, fonbet);
                            //matchOnFonbet.nailedIt = PlaceBetByBookmakers.onFonbet(driver, matchOnFonbet,
                            //        mapInfo.getMapName(), 70, true);
                            //test
                            boolean firstForkExists = Utils.forkExists(homeOdds1, awayOdds2);
                            boolean secondForkExists = Utils.forkExists(homeOdds2, awayOdds1);
                            if (firstForkExists || secondForkExists) {
                                boolean onHomeFirstBet = Utils.placeOnHomeThunderpickIsBetter(homeOdds1, awayOdds1, homeOdds2, awayOdds2);
                                Match matchOnThunderpick = Match.getMatchByBookmaker(matchInfo.matches, thunderpick);
                                Match matchOnFonbet = Match.getMatchByBookmaker(matchInfo.matches, fonbet);
                                //test
                                //matchOnFonbet.nailedIt = PlaceBetByBookmakers.onFonbet(driver, matchOnFonbet,
                                //        mapInfo.getMapName(), 50, !onHomeFirstBet);
                                //test
                                if (matchOnThunderpick == null || matchOnFonbet == null
                                        || matchOnThunderpick.nailedIt || matchOnFonbet.nailedIt)
                                    continue;

                                int firstBet = 100;
                                matchOnThunderpick.nailedIt = PlaceBetByBookmakers.onThunderpick(driver, matchOnThunderpick,
                                        mapInfo.getMapName(), firstBet, onHomeFirstBet ? homeOdds1 : awayOdds1, onHomeFirstBet);
                                if (matchOnThunderpick.nailedIt) {
                                    System.out.println("Successfully placed " + firstBet + " on thunderpick at "
                                            + matchOnThunderpick.homeTeam + " - " + matchOnThunderpick.awayTeam + " on "
                                            + (onHomeFirstBet ? "home " + homeOdds1 : "away " + awayOdds1));

                                    int secondBet;
                                    if (onHomeFirstBet)
                                        secondBet = Utils.getSecondBet(homeOdds1, awayOdds2, firstBet);
                                    else
                                        secondBet = Utils.getSecondBet(homeOdds2, awayOdds1, firstBet);
                                    matchOnFonbet.nailedIt = PlaceBetByBookmakers.onFonbet(driver, matchOnFonbet,
                                            mapInfo.getMapName(), secondBet, !onHomeFirstBet);

                                    if (matchOnFonbet.nailedIt)
                                        System.out.println("Successfully placed " + secondBet + " on fonbet at "
                                                + matchOnFonbet.homeTeam + " - " + matchOnFonbet.awayTeam + " on "
                                                + (!onHomeFirstBet ? "home " + homeOdds2 : "away " + awayOdds2));
                                }
                            }
                        }
                    }
                }

                TimeUnit.SECONDS.sleep(Utils.getRandomNumber(1, 2));

                for (Bookmaker bookmaker : bookmakers) {
                    if (bookmaker.name.equals(Bookmaker.THUNDERPICK))
                        AddRemoveMatchesByBookmakers.onThunderpick(bookmaker, driver);
                    else if (bookmaker.name.equals(Bookmaker.FONBET))
                        AddRemoveMatchesByBookmakers.onFonbet(bookmaker, driver);
                }
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                System.out.println("Stale element exception occurred: " + e + "\nRestarting...");
                e.printStackTrace();
                driver.quit();
                run(new String[]{});
            } catch (java.net.SocketException e){
                System.out.println("Socket exception occurred: " + e + "\nRestarting...");
                e.printStackTrace();
                driver.quit();
                run(new String[]{});
            } catch (Exception e){
                System.out.println("Exception occurred: " + e + "\nRestarting...");
                e.printStackTrace();
                driver.quit();
                run(new String[]{});
            }
        }
    }

    private String startProfile(String profileId) throws Exception {
        /*Send GET request to start the browser profile by profileId. Returns response in the following format:
        '{"status":"OK","value":"http://127.0.0.1:XXXXX"}', where XXXXX is the localhost port on which browser profile is
        launched. Please make sure that you have Multilogin listening port set to 35000. Otherwise please change the port
        value in the url string*/
        String url = "http://127.0.0.1:35000/api/v1/profile/start?automation=true&profileId=" + profileId;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Get JSON text from the response and return the value by key "value"
        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("value");
    }
}