package com.example.demo.api.model.bd;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface EnemyRepository extends CrudRepository<Enemy, Integer> {

    List<Enemy> findByLevel(Integer level);
}