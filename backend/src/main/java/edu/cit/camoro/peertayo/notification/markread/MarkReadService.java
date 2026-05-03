package edu.cit.camoro.peertayo.notification.markread;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.notification.entity.Notification;
import edu.cit.camoro.peertayo.notification.repository.NotificationRepository;
import edu.cit.camoro.peertayo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkReadService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void markAsRead(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
