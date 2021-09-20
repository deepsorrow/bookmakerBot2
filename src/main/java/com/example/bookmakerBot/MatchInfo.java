package com.example.bookmakerBot;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.awt.print.Book;
import java.util.ArrayList;

public class MatchInfo {
    ArrayList<Match> matches;
    ArrayList<MapInfo> mapInfos;

    public MatchInfo(Match match) {
        this.matches  = new ArrayList<>();
        this.mapInfos = new ArrayList<>();
        this.matches.add(match);
    }

    public boolean hasThisMatch(Match match) {
        for (Match _match : matches) {
            if(!_match.game.equals(match.game))
                continue;

            int distAway = LevenshteinDistance.getDefaultInstance().apply(_match.awayTeam.toLowerCase(), match.awayTeam.toLowerCase());
            int distHome = LevenshteinDistance.getDefaultInstance().apply(_match.homeTeam.toLowerCase(), match.homeTeam.toLowerCase());
            if (distAway <= 4 || distHome <= 4)
                return true;
        }

        return false;
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

    public Match getMatchBy(Bookmaker bookmaker){
        for(Match match : matches){
            if(match.bookmaker == bookmaker)
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

    public static void insertMatchToRightMatchInfo(Match match, ArrayList<MatchInfo> matchInfos){
        boolean matchInfoFound = false;
        for(MatchInfo matchInfo : matchInfos){
            if(matchInfo.hasThisMatch(match)) {
                matchInfo.matches.add(match);
                matchInfoFound = true;
            }
        }

        if(!matchInfoFound){
            matchInfos.add(new MatchInfo(match));
        }
    }
}
