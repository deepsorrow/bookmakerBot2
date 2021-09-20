package com.example.bookmakerBot;

import org.apache.commons.text.similarity.LevenshteinDistance;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Entity
public class UpcomingGame {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public String homeTeam;
    public String awayTeam;
    public Date   beginDate;
    public String game;
    public String tournament;
    @Transient
    public Bookmaker bookmaker;
    public String bookmakerName;
    @Transient
    public boolean checked = false;

    public UpcomingGame(String homeTeam, String awayTeam, Date beginDate, String game,
                        String tournament, Bookmaker bookmaker) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.beginDate = beginDate;
        this.game = game;
        this.tournament = tournament;
        this.bookmaker = bookmaker;
        bookmakerName = bookmaker.name;
    }

    public UpcomingGame() {

    }

    public static boolean gameExist(UpcomingGame upcomingGame, ArrayList<UpcomingGame> upcomingGames){
        for(UpcomingGame _upcomingGame : upcomingGames){
            if(upcomingGame.homeTeam.equals(_upcomingGame.homeTeam)
                    && upcomingGame.awayTeam.equals(_upcomingGame.awayTeam)
                    && upcomingGame.beginDate.equals(_upcomingGame.beginDate)
                    && upcomingGame.game.equals(_upcomingGame.game)){
                return true;
            }
        }

        return false;
    }

    public static ArrayList<ArrayList<UpcomingGame>> findTeamsWithSameName(ArrayList<Bookmaker> bookmakers){
        ArrayList<ArrayList<UpcomingGame>> upcomingGamesWithSameGameAndTime = new ArrayList<>();

        for(Bookmaker bookmaker : bookmakers) {
            for(UpcomingGame upcomingGame : bookmaker.upcomingGames) {
                ArrayList<UpcomingGame> sameGames = new ArrayList<>();
                for(Bookmaker _bookmaker : bookmakers){
                    if(bookmaker == _bookmaker)
                        continue;
                    for(UpcomingGame _upcomingGame : _bookmaker.upcomingGames){
                        if(upcomingGame.beginDate.equals(_upcomingGame.beginDate)
                                && upcomingGame.game.equals(_upcomingGame.game)) {
                            sameGames.add(upcomingGame);
                            sameGames.add(_upcomingGame);
                        }
                    }
                }
                upcomingGamesWithSameGameAndTime.add(sameGames);
            }
        }

        HashMap<Bookmaker, UpcomingGame> map = new HashMap<>();
        for(int i=0; i < (upcomingGamesWithSameGameAndTime.size()+1)/2; ++i){
            ArrayList<UpcomingGame> sameGames = upcomingGamesWithSameGameAndTime.get(i);
            for(UpcomingGame upcomingGame : sameGames){
                UpcomingGame foundGame = findMaxDist(upcomingGamesWithSameGameAndTime, sameGames, upcomingGame);
                if(foundGame != null){

                }

            }
        }

        return upcomingGamesWithSameGameAndTime;
    }

    public static UpcomingGame findMaxDist(ArrayList<ArrayList<UpcomingGame>> upcomingGamesWithSameGameAndTime,
                                           ArrayList<UpcomingGame> sameGames, UpcomingGame upcomingGame){
        for(int j=0; j < (upcomingGamesWithSameGameAndTime.size()+1)/2; ++j){
            for(UpcomingGame _upcomingGame : sameGames) {
                if(upcomingGame.bookmaker == _upcomingGame.bookmaker)
                    continue;
                int homeDist = LevenshteinDistance.getDefaultInstance().apply(upcomingGame.homeTeam, _upcomingGame.homeTeam);
                int awayDist = LevenshteinDistance.getDefaultInstance().apply(upcomingGame.awayTeam, _upcomingGame.awayTeam);

                if(homeDist > 50 && awayDist > 50)
                    return _upcomingGame;

            }
        }
        return null;
    }
}
