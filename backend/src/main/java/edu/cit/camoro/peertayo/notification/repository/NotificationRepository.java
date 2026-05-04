package edu.cit.camoro.peertayo.notification.repository;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);
}
