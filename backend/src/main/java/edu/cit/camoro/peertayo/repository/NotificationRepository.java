package edu.cit.camoro.peertayo.repository;

import edu.cit.camoro.peertayo.entity.Notification;
import edu.cit.camoro.peertayo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);
}
