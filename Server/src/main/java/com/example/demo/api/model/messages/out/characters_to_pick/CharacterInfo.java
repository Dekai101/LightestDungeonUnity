package com.example.demo.api.model.messages.out.characters_to_pick;

import java.util.List;

import com.example.demo.api.model.bd.Character;
import com.example.demo.api.model.bd.Skill;

public class CharacterInfo {
    public com.example.demo.api.model.bd.Character character;
    public List<Skill> skills;
    public int selectedPlayerId;
    public boolean isSelected;

    public CharacterInfo(){}

    public CharacterInfo(Character character, int selectedPlayerId, boolean isSelected, List<Skill> skills) {
        this.character = character;
        this.selectedPlayerId = selectedPlayerId;
        this.isSelected = isSelected;
        this.skills = skills;
    }
    
    public Character getCharacter(){
        return this.character;
    }
}
