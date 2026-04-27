package com.example.demo.api.model.bd;

import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends CrudRepository<Character, Integer> {

    List<Character> findAll();

    List<Character> findByLevel(Integer level);
}