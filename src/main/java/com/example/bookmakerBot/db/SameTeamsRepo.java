package com.example.bookmakerBot.db;

import com.example.bookmakerBot.SameGamesResult;
import org.springframework.data.repository.CrudRepository;

public interface SameTeamsRepo extends CrudRepository<SameGamesResult, Long> {
    SameGamesResult getByTeamNamesLike(String teamNames);
}
