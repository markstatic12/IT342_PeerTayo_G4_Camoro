package com.example.backend.repository;

import com.example.backend.entity.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    List<Criteria> findByIsActiveTrue();
}
