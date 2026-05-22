package com.example.demo.api.model;

import org.springframework.web.socket.WebSocketSession;

import com.example.demo.api.model.bd.BdPlayer;
import com.example.demo.api.model.bd.Character;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Player {
    long id;
    BdPlayer cBdPlayer;

    @JsonIgnore
    WebSocketSession session;

    String name;


    public Player(long id, WebSocketSession session) {
        this.id = id;
        this.session = session;
    }

    public Player(Player p) {
        this.id = p.id;
        this.cBdPlayer = new BdPlayer(p.cBdPlayer);
        this.session = p.session;
        this.name = p.name;
    }

    public void setBdPlayer(Character character, int xp_points, int skill_points){
        this.cBdPlayer = (BdPlayer) character;
        this.cBdPlayer.setSkillPoints(skill_points);
        this.cBdPlayer.setXpPoints(xp_points);
    }

    public BdPlayer getCharacter(){
        return cBdPlayer;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    @JsonIgnore
    public WebSocketSession getSession() {
        return session;
    }

    public String getName() {
        return name;
    }
    

}
