package edu.cit.camoro.peertayo.auth.repository;

import edu.cit.camoro.peertayo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findTop20ByOrderByFirstNameAsc();

    List<User> findTop20ByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByFirstNameAsc(
            String firstName,
            String lastName,
            String email
    );
}
