package com.example.demo.api.model.messages.in.players_turn;

import com.example.demo.api.model.bd.BdPlayer;
import com.example.demo.api.model.bd.Character;
import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.bd.Skill;

public class PlayerTurn {
    public BdPlayer player;
    public String choiceType;
    public Skill skillCasted;
    public Item itemUsed;
    public boolean choiceMade;
    public Character target;

    public PlayerTurn(){}

    public PlayerTurn(BdPlayer player, String choiceType, boolean choiceMade, Skill skillCasted, Item itemUsed, Character target) {
        this.player = player;
        this.choiceType = choiceType;
        this.choiceMade = choiceMade;
        this.skillCasted = skillCasted;
        this.itemUsed = itemUsed;
    }
}
