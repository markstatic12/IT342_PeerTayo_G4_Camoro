package com.example.backend.config;

import com.example.backend.entity.Criteria;
import com.example.backend.entity.ERole;
import com.example.backend.entity.Role;
import com.example.backend.repository.CriteriaRepository;
import com.example.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs on every startup. Idempotent — only inserts if the records do not exist.
 * Seeds: roles (RESPONDENT, FACILITATOR) and the 10 fixed evaluation criteria.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CriteriaRepository criteriaRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedCriteria();
    }

    private void seedRoles() {
        for (ERole roleName : ERole.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(null, roleName));
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedCriteria() {
        if (criteriaRepository.count() > 0) {
            return; // Already seeded
        }

        List<Criteria> criteriaList = List.of(
                buildCriteria("Communication Skills",
                        "Ability to clearly express ideas and actively listen to others."),
                buildCriteria("Teamwork and Collaboration",
                        "Willingness to work cooperatively and contribute to the group."),
                buildCriteria("Leadership",
                        "Ability to guide, motivate, and take initiative within the group."),
                buildCriteria("Reliability and Responsibility",
                        "Consistently meets deadlines and fulfills assigned tasks."),
                buildCriteria("Problem Solving",
                        "Ability to identify issues and propose effective solutions."),
                buildCriteria("Adaptability",
                        "Openness to change and ability to adjust to new situations or challenges."),
                buildCriteria("Work Quality",
                        "Produces accurate, thorough, and high-quality output."),
                buildCriteria("Critical Thinking",
                        "Analyzes information objectively to make informed decisions."),
                buildCriteria("Respect and Professionalism",
                        "Treats peers with courtesy and maintains professional conduct."),
                buildCriteria("Contribution to Group Goals",
                        "Actively helps the team achieve its shared objectives.")
        );

        criteriaRepository.saveAll(criteriaList);
        log.info("Seeded {} evaluation criteria.", criteriaList.size());
    }

    private Criteria buildCriteria(String title, String description) {
        return Criteria.builder()
                .title(title)
                .description(description)
                .isActive(true)
                .build();
    }
}
