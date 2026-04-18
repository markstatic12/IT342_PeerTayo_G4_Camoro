package edu.cit.camoro.peertayo.repository;

import edu.cit.camoro.peertayo.entity.EvaluationAssignment;
import edu.cit.camoro.peertayo.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findAllByAssignmentInAndActiveTrue(List<EvaluationAssignment> assignments);

    void deleteAllByAssignmentIn(List<EvaluationAssignment> assignments);
}
