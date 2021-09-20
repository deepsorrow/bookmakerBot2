package com.example.bookmakerBot;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class SameGamesResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    public String teamNames;
    @Transient
    public List<UpcomingGame> teamNamesArray;

    public SameGamesResult() {
    }

    public SameGamesResult(UpcomingGame upcomingGame1, UpcomingGame upcomingGame2) {
        teamNamesArray = new ArrayList<>();
        teamNamesArray.add(upcomingGame1);
        teamNamesArray.add(upcomingGame2);

//        teamNames = teamName1 + ";" + teamName2;
    }

    public void convertToString(){
        teamNames = "";
        for(UpcomingGame s : teamNamesArray)
            teamNames += s + ";";

        teamNames = teamNames.substring(0, teamNames.length()-1);
    }
}
