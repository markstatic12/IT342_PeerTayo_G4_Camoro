package edu.cit.camoro.peertayo.repository;

import edu.cit.camoro.peertayo.entity.ERole;
import edu.cit.camoro.peertayo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}
