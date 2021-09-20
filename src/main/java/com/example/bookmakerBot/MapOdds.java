package com.example.bookmakerBot;

public class MapOdds {
    public double homeOdds;
    public double awayOdds;
    public String mapName;
    public Bookmaker bookmaker;

    public MapOdds(Bookmaker bookmaker, double homeOdds, double awayOdds, String mapName) {
        this.bookmaker = bookmaker;
        this.homeOdds = homeOdds;
        this.awayOdds = awayOdds;
        this.mapName = mapName;
    }
}
