package edu.cit.camoro.peertayo.evaluation.repository;

import edu.cit.camoro.peertayo.evaluation.entity.EvaluationAssignment;
import edu.cit.camoro.peertayo.evaluation.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findAllByAssignmentInAndActiveTrue(List<EvaluationAssignment> assignments);

    void deleteAllByAssignmentIn(List<EvaluationAssignment> assignments);
}
