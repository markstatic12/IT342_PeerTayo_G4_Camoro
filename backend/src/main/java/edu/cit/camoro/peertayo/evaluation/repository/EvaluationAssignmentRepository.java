package edu.cit.camoro.peertayo.evaluation.repository;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationAssignment;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationAssignmentRepository extends JpaRepository<EvaluationAssignment, Long> {

    List<EvaluationAssignment> findAllByEvaluation(EvaluationForm evaluation);

    List<EvaluationAssignment> findAllByEvaluationAndEvaluatorAndSubmittedFalse(EvaluationForm evaluation, User evaluator);

    List<EvaluationAssignment> findAllByEvaluatorAndSubmittedFalseOrderByEvaluationDeadlineAsc(User evaluator);

    long countByEvaluation(EvaluationForm evaluation);

    long countByEvaluationAndSubmittedTrue(EvaluationForm evaluation);

    List<EvaluationAssignment> findAllByEvaluationAndSubmittedTrue(EvaluationForm evaluation);

    void deleteAllByEvaluation(EvaluationForm evaluation);
}
