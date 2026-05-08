package com.example.demo.api.model.bd;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface BdPlayerRepository extends CrudRepository<BdPlayer, Integer> {

    @Query("SELECT p FROM BdPlayer p LEFT JOIN FETCH p.skills")
    List<BdPlayer> findAllWithSkills();

    List<BdPlayer> findAll();

    Optional<BdPlayer> findById(Integer id);
}