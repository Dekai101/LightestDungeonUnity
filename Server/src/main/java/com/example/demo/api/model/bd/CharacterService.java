package com.example.demo.api.model.bd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CharacterService {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private BdPlayerRepository bdPlayerRepository;

    @Autowired
    private EnemyRepository enemyRepository;

    // ---- CHARACTER (Entity) ----
    public List<Character> getAllCharacters() {
        return characterRepository.findAll();
    }

    public List<Character> getAllCharactersWithSkills(){
        return characterRepository.findAllWithSkills();
    }

    

    public Optional<Character> getCharacterById(Integer id) {
        return characterRepository.findById(id);
    }

    // ---- PLAYERS ----
    public List<BdPlayer> getAllPlayers() {
        return bdPlayerRepository.findAllWithSkills();
    }

    public Optional<BdPlayer> getPlayerById(Integer id) {
        return bdPlayerRepository.findById(id);
    }

    // ---- ENEMIES ----
    public List<Enemy> getAllEnemies() {
        return (List<Enemy>) enemyRepository.findAll();
    }

    public Optional<Enemy> getEnemyWithSkills(Integer id) {
        return enemyRepository.findByIdWithSkills(id);
    }

    public Optional<Enemy> getEnemyById(Integer id) {
        return enemyRepository.findById(id);
    }

    public List<Enemy> getEnemiesByLevel(Integer level) {
        return enemyRepository.findByLevelWithLoot(level);
    }
}
