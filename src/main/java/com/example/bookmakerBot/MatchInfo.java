package com.example.bookmakerBot;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.awt.print.Book;
import java.util.ArrayList;

public class MatchInfo {
    ArrayList<Match> matches;
    ArrayList<MapInfo> mapInfos;

    public MatchInfo(Match match) {
        this.matches = new ArrayList<>();
        this.mapInfos = new ArrayList<>();
        this.matches.add(match);
    }

    public boolean hasThisMatch(Match match) {
        for (Match _match : matches) {
            if (!_match.game.equals(match.game))
                continue;

            int distAway = LevenshteinDistance.getDefaultInstance().apply(removeThings(_match.awayTeam.toLowerCase()), removeThings(match.awayTeam.toLowerCase()));
            int distHome = LevenshteinDistance.getDefaultInstance().apply(removeThings(_match.homeTeam.toLowerCase()), removeThings(match.homeTeam.toLowerCase()));
            if (distAway <= 1 || distHome <= 1)
                return true;
            else {
                int distAwayReverse = LevenshteinDistance.getDefaultInstance().apply(removeThings(_match.awayTeam.toLowerCase()), removeThings(match.homeTeam.toLowerCase()));
                int distHomeReverse = LevenshteinDistance.getDefaultInstance().apply(removeThings(_match.homeTeam.toLowerCase()), removeThings(match.awayTeam.toLowerCase()));

                if (distAwayReverse <= 1 || distHomeReverse <= 1) {
                    ArrayList<MapOdds> prevMapOdds = new ArrayList<>(match.mapOdds);
                    match.mapOdds.clear();
                    for (MapOdds mapOdds : prevMapOdds) {
                        MapOdds newMapOdds = new MapOdds(mapOdds.bookmaker, mapOdds.awayOdds, mapOdds.homeOdds, mapOdds.mapName);
                        match.mapOdds.add(newMapOdds);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public static String removeThings(String original) {
        return original.replace("esports", "").replace("gaming", "").replace("e-sports", "");
    }

    public String getGame() {
        return matches.get(0).game;
    }

    public String getHomeTeam() {
        return matches.get(0).homeTeam;
    }

    public String getAwayTeam() {
        return matches.get(0).awayTeam;
    }

    public Bookmaker getBookmaker() {
        return matches.get(0).bookmaker;
    }

    public Match getMatchBy(Bookmaker bookmaker) {
        for (Match match : matches) {
            if (match.bookmaker == bookmaker)
                return match;
        }
        return null;
    }

    public void initMapInfos() {
        ArrayList<MapInfo> result = new ArrayList<>();

        for (Match match : matches) {
            for (MapOdds mapOdds : match.mapOdds) {
                if (!hasThisMapOdds(mapOdds)) {
                    mapInfos.add(new MapInfo(mapOdds));
                }
            }
        }
    }

    public boolean hasThisMapOdds(MapOdds mapOdds) {
        for (MapInfo mapInfo : mapInfos) {
            if (mapInfo.getMapName().equals(mapOdds.mapName)) {
                mapInfo.mapsOdds.add(mapOdds);
                return true;
            }
        }

        return false;
    }

    public static void insertMatchToRightMatchInfo(Match match, ArrayList<MatchInfo> matchInfos) {
        boolean matchInfoFound = false;
        for (MatchInfo matchInfo : matchInfos) {
            if (matchInfo.hasThisMatch(match)) {
                matchInfo.matches.add(match);
                matchInfoFound = true;
            }
        }

        if (!matchInfoFound) {
            matchInfos.add(new MatchInfo(match));
        }
    }
}
