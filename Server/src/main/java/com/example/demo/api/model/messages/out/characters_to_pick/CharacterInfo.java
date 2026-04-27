package com.example.demo.api.model.messages.out.characters_to_pick;

import com.example.demo.api.model.bd.Character;

public class CharacterInfo {
    public com.example.demo.api.model.bd.Character character;
    public String name;
    public String imageURL;
    public int selectedPlayerId;
    public boolean isSelected;

    public CharacterInfo(){}

    public CharacterInfo(Character character, int selectedPlayerId, boolean isSelected) {
        this.character = character;
        this.selectedPlayerId = selectedPlayerId;
        this.isSelected = isSelected;
    }
    
    public Character getCharacter(){
        return this.character;
    }
}
