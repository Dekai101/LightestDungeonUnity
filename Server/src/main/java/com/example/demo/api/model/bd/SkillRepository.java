package com.example.demo.api.model.bd;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends CrudRepository<Skill, Integer> {

    List<Skill> findAll();

    List<Skill> findByIsPassive(Boolean isPassive);

    List<Skill> findByTargetType(String targetType);

    @Query("""
        SELECT DISTINCT s
        FROM Skill s
        LEFT JOIN FETCH s.effects e
        LEFT JOIN FETCH e.statistic
        LEFT JOIN FETCH e.status
        WHERE s.id = :id
    """)
    Optional<Skill> findByIdWithEffects(@Param("id") Integer id);
}