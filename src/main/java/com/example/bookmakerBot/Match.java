package com.example.bookmakerBot;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.awt.print.Book;
import java.util.ArrayList;

import static com.example.bookmakerBot.MatchInfo.removeThings;

public class Match {
    public String datakey;
    public Bookmaker bookmaker;
    public String windowHandle;
    public String homeTeam;
    public String awayTeam;
    public String game;
    public ArrayList<MapOdds> mapOdds;
    public boolean nailedIt;
    public String href;
    //public boolean reverse;

    public Match(String homeTeam, String awayTeam, String game, String datakey, String windowHandle) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.game = game;
        this.windowHandle = windowHandle;
        this.mapOdds = new ArrayList<>();
        this.datakey = datakey;
    }

    public void setOwner(Bookmaker bookmaker){
        this.bookmaker = bookmaker;
    }

    public static Match getMatchByBookmaker(ArrayList<Match> matches, Bookmaker bookmaker){
        for(Match match : matches){
            if(match.bookmaker == bookmaker)
                return match;
        }

        return null;
    }

    public static MapOdds findMapOddsByMapName(ArrayList<MapOdds> mapOddsList, String mapName){
        for(MapOdds mapOdds : mapOddsList)
            if(mapOdds.mapName.equalsIgnoreCase(mapName))
                return mapOdds;
        return null;
    }

    public static ArrayList<Match> getMatchesByGame(ArrayList<Match> matches, String game){
        ArrayList<Match> result = new ArrayList<>();
        for(Match match : matches){
            if(match.game.equals(game))
                result.add(match);
        }

        return result;
    }

    public boolean homeEquals(String anotherTeamName){
        int distHome = LevenshteinDistance.getDefaultInstance().apply(removeThings(anotherTeamName.toLowerCase()), removeThings(homeTeam.toLowerCase()));
        return distHome <= 1;
    }

    public boolean awayEquals(String anotherTeamName){
        int distAway = LevenshteinDistance.getDefaultInstance().apply(removeThings(anotherTeamName.toLowerCase()), removeThings(awayTeam.toLowerCase()));
        return distAway <= 1;
    }
}
