package com.example.bookmakerBot;

import java.util.ArrayList;
import java.util.List;

public class Bookmaker {
    public String name;
    public ArrayList<Match> matches;
    public ArrayList<LiveGame> liveGamesLinks;
    public ArrayList<UpcomingGamesLink> upcomingGamesLinks = new ArrayList<>();
    public List<UpcomingGame> upcomingGames;
    public final static String THUNDERPICK = "thunderpick";
    public final static String FONBET = "fonbet";
    public boolean loginRequired;

    public Bookmaker(String name) {
        this.name = name;
        matches = new ArrayList<>();
        liveGamesLinks = new ArrayList<>();
        loginRequired = true;
    }
}
