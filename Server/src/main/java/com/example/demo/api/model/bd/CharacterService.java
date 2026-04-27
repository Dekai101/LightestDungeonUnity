package com.example.demo.api.model.bd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CharacterService {

    @Autowired
    private CharacterRepository characterRepository;

    public List<Character> getAllCharacters() {
        return (List<Character>) characterRepository.findAll();
    }

    public Optional<Character> getCharacterById(Integer id) {
        return characterRepository.findById(id);
    }
}
