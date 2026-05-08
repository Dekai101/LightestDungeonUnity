package com.example.demo.api.model.messages.in.players_turn;

import java.util.List;

import com.example.demo.api.model.messages.MessageBody;

public class PlayersTurn_IN extends MessageBody{

    public final static String TYPE = "PLAYERS_TURN";

    @Override
    public String getMessageType(){
        return TYPE;
    }

    public List<PlayerTurn> players;
}
