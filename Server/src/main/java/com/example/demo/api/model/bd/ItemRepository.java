package com.example.demo.api.model.bd;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends CrudRepository<Item, Integer> {

    //Find by quality with effects
    @Query("""
        SELECT DISTINCT i
        FROM Item i
        LEFT JOIN FETCH i.effects
        WHERE i.quality IN ?1
    """)
    List<Item> findByQualityIn(List<String> qualities);

    @Query("""
        SELECT DISTINCT i
        FROM Item i
        LEFT JOIN FETCH i.effects
        WHERE i.id = ?1
    """)
    Optional<Item> findByIdWithEffects(Integer id);

}