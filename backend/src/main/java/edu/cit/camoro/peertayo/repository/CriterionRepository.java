package edu.cit.camoro.peertayo.repository;

import edu.cit.camoro.peertayo.entity.Criterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CriterionRepository extends JpaRepository<Criterion, Long> {

    List<Criterion> findAllByActiveTrueOrderByIdAsc();

    List<Criterion> findAllByIdInAndActiveTrue(Collection<Long> ids);
}
