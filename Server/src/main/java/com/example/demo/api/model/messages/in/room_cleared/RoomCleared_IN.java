package com.example.demo.api.model.messages.in.room_cleared;

import com.example.demo.api.model.messages.MessageBody;

public class RoomCleared_IN extends MessageBody{

    public final static String TYPE = "ROOM_CLEARED";

    @Override
    public String getMessageType(){
        return TYPE;
    }

    public long playerId;
}
