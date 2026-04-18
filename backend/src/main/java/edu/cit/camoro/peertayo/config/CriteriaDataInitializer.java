package edu.cit.camoro.peertayo.config;

import edu.cit.camoro.peertayo.entity.Criterion;
import edu.cit.camoro.peertayo.repository.CriterionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CriteriaDataInitializer {

    private final CriterionRepository criterionRepository;

    @Bean
    CommandLineRunner seedCriteria() {
        return args -> {
            if (criterionRepository.count() > 0) {
                return;
            }

            List<Criterion> criteria = List.of(
                    Criterion.builder().title("Quality of Work").description("Produces accurate, thorough, and well-organized outputs that meet or exceed expectations.").active(true).build(),
                    Criterion.builder().title("Reliability & Dependability").description("Consistently delivers on commitments and can be counted on to follow through on tasks.").active(true).build(),
                    Criterion.builder().title("Collaboration & Teamwork").description("Works effectively with others and contributes constructively to group efforts.").active(true).build(),
                    Criterion.builder().title("Communication Skills").description("Expresses ideas clearly, listens actively, and communicates updates in a timely manner.").active(true).build(),
                    Criterion.builder().title("Initiative & Proactiveness").description("Identifies and acts on opportunities without being prompted; goes beyond minimum requirements.").active(true).build(),
                    Criterion.builder().title("Problem Solving").description("Approaches challenges analytically and proposes practical, effective solutions.").active(true).build(),
                    Criterion.builder().title("Professionalism & Conduct").description("Maintains a respectful, ethical, and positive demeanor in all interactions.").active(true).build(),
                    Criterion.builder().title("Time Management").description("Prioritizes tasks effectively, meets deadlines, and manages workload without compromising quality.").active(true).build(),
                    Criterion.builder().title("Adaptability & Learning").description("Responds positively to change, accepts feedback constructively, and continuously improves.").active(true).build(),
                    Criterion.builder().title("Overall Contribution").description("Holistic assessment of the individual net positive impact on the team or group outcome.").active(true).build()
            );

            criterionRepository.saveAll(criteria);
        };
    }
}
