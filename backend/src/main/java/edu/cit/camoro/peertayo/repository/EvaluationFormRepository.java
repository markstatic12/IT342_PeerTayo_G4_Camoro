package edu.cit.camoro.peertayo.repository;

import edu.cit.camoro.peertayo.entity.EvaluationForm;
import edu.cit.camoro.peertayo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationFormRepository extends JpaRepository<EvaluationForm, Long> {

    List<EvaluationForm> findAllByCreatedByOrderByCreatedAtDesc(User createdBy);

    Optional<EvaluationForm> findByIdAndCreatedBy(Long id, User createdBy);
}
