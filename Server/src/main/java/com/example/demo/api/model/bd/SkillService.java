package com.example.demo.api.model.bd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public List<Skill> getAllSkills() {
        return (List<Skill>) skillRepository.findAll();
    }

    public Optional<Skill> getSkillById(Integer id) {
        return skillRepository.findById(id);
    }

    public Optional<Skill> getSkillByIdWithEffects(Integer id){
        return skillRepository.findByIdWithEffects(id);
    }

    public List<Skill> getActiveSkills() {
        return skillRepository.findByIsPassive(false);
    }

    public List<Skill> getPassiveSkills() {
        return skillRepository.findByIsPassive(true);
    }

    public List<Skill> getSkillsByTargetType(String targetType) {
        return skillRepository.findByTargetType(targetType);
    }
}