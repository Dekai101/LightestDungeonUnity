package com.example.demo.api.model.bd;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {

    List<Item> findByQualityIn(List<String> qualities);
}