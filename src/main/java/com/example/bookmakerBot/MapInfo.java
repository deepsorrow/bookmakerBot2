package com.example.bookmakerBot;

import java.util.ArrayList;

public class MapInfo{
    ArrayList<MapOdds> mapsOdds;

    public MapInfo(MapOdds mapOdds) {
        mapsOdds = new ArrayList<>();
        mapsOdds.add(mapOdds);
    }

    public String getMapName(){
        return mapsOdds.get(0).mapName;
    }

    public double getHomeOddsBy(Bookmaker bookmaker){
        for(MapOdds mapOdds : mapsOdds){
            if(mapOdds.bookmaker == bookmaker)
                return mapOdds.homeOdds;
        }
        return -2.0;
    }

    public double getAwayOddsBy(Bookmaker bookmaker){
        for(MapOdds mapOdds : mapsOdds){
            if(mapOdds.bookmaker == bookmaker)
                return mapOdds.awayOdds;
        }
        return -2.0;
    }
}
