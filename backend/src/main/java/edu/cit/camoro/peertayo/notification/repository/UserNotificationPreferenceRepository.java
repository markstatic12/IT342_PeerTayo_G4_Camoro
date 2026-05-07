package edu.cit.camoro.peertayo.notification.repository;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.notification.entity.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {

    Optional<UserNotificationPreference> findByUser(User user);
}
