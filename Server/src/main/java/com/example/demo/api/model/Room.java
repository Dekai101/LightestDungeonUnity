package com.example.demo.api.model;

public class Room {
    private long id;
    private String type;
    private int level;

    public Room(long id, String type, int level){
        this.id = id;
        this.type = type;
        this.level = level;
    }

    public String getType(){
        return type;
    }


    public long getId() {
        return id;
    }

    public int getLevel(){
        return level;
    }
}
