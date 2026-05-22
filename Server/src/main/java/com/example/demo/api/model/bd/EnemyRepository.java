package com.example.demo.api.model.bd;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnemyRepository extends CrudRepository<Enemy, Integer> {

    List<Enemy> findByLevel(Integer level);

    @Query("SELECT e FROM Enemy e LEFT JOIN FETCH e.skills WHERE e.id = :id")
    Optional<Enemy> findByIdWithSkills(@Param("id") Integer id);
}