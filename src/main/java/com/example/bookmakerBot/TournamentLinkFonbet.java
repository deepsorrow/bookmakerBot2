package com.example.bookmakerBot;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class TournamentLinkFonbet {
    public String href;
    public String tournamentName;

    public TournamentLinkFonbet(String href, String tournamentName) {
        this.href = href;
        this.tournamentName = tournamentName;
    }

    public static ArrayList<TournamentLinkFonbet> parseLinks(List<WebElement> links){
        ArrayList<TournamentLinkFonbet> result = new ArrayList<>();
        for(WebElement link : links){
            String href = link.getAttribute("href");
            String tournamentName = link.findElement(By.className("filter-component-text--3YtmB")).getText();

            result.add(new TournamentLinkFonbet(href, tournamentName));
        }
        return result;
    }
}
