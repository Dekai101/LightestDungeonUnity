package com.example.demo.api.model.messages.out.identify_player;

import com.example.demo.api.model.messages.MessageBody;

public class PlayerIdentifier_OUT extends MessageBody{

    public final static String TYPE = "PLAYER_INFO";

    @Override
    public String getMessageType(){
        return TYPE;
    }

    public long playerId;
    public long playerNumber;
}
