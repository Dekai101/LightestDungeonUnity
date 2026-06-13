package com.example.demo.api.model.messages.out.generic;

import com.example.demo.api.model.messages.MessageBody;


public class Message_OUT extends MessageBody{

    public String TYPE = "GENERIC_MESSAGE";
    
    public Message_OUT(String text) {
        this.text = text;
    }

    public Message_OUT(String text, String type) {
        this.text = text;
        this.TYPE = type.toUpperCase();
    }

    @Override
    public String getMessageType(){
        return TYPE;
    }

    public String text;





}
