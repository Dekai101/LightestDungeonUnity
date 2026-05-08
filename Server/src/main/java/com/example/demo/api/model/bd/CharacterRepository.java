package com.example.demo.api.model.bd;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface CharacterRepository extends CrudRepository<Character, Integer> {

    @Query("SELECT c FROM Character c LEFT JOIN FETCH c.skills")
    List<Character> findAllWithSkills();

    List<Character> findAll();

    List<Character> findByLevel(Integer level);
}