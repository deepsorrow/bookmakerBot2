package com.example.bookmakerBot.db;

import com.example.bookmakerBot.UpcomingGame;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface UpcomingGameRepo extends CrudRepository<UpcomingGame, Long> {
    UpcomingGame getByHomeTeamAndAwayTeamAndGameAndTournamentAndBeginDate(String homeTeam, String awayTeam, String game,
                                                                          String tournament, Date beginDate);
    @Query("SELECT homeTeam, awayTeam, beginDate, game FROM UpcomingGame")
    List<UpcomingGame> getSameTeams();
}
