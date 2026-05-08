package com.example.demo.api.model;

import org.springframework.web.socket.WebSocketSession;

import com.example.demo.api.model.bd.BdPlayer;
import com.example.demo.api.model.bd.Character;

public class Player {
    long id;
    BdPlayer cBdPlayer;
    WebSocketSession session;
    String name;


    public Player(long id, WebSocketSession session) {
        this.id = id;
        this.session = session;
    }

    public void setBdPlayer(Character character, int xp_points, int skill_points){
        this.cBdPlayer = (BdPlayer) character;
        this.cBdPlayer.setSkillPoints(skill_points);
        this.cBdPlayer.setXpPoints(xp_points);
    }

    public Character getCharacter(){
        return cBdPlayer;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getId() {
        return id;
    }


    public WebSocketSession getSession() {
        return session;
    }


    public String getName() {
        return name;
    }
    

}
