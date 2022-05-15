package com.marian.owncloudbackend.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.marian.owncloudbackend.CommandRunner.ADMIN_EMAIL;

import com.marian.owncloudbackend.DTO.NotificationDTO;
import com.marian.owncloudbackend.DTO.UserDTO;
import com.marian.owncloudbackend.DTO.UserStorageRequest;
import com.marian.owncloudbackend.entity.NotificationEntity;
import com.marian.owncloudbackend.entity.UserEntity;
import com.marian.owncloudbackend.enums.NotificationState;
import com.marian.owncloudbackend.mapper.NotificationEntityMapper;
import com.marian.owncloudbackend.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    private final NotificationEntityMapper notificationEntityMapper;

    private final SimpMessagingTemplate template;

    private final UserService userService;


    public void notifyAdminForUserRequest(UserStorageRequest userStorageRequest) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity sender = userService.getUserByEmail(userEmail);
        UserEntity admin = userService.getUserByEmail(ADMIN_EMAIL);

        NotificationEntity notificationEntity = new NotificationEntity(sender.getEmail()+":"+userStorageRequest.description() + ":" +
                userStorageRequest.preferredAmount(),
                admin);
        NotificationEntity save = notificationRepository.save(notificationEntity);

        NotificationDTO notificationDTO = notificationEntityMapper.entityToDTO(save);

        template.convertAndSendToUser(ADMIN_EMAIL, "/queue/notify", notificationDTO);
    }

    public List<NotificationDTO> getAllNotificationsOrdered() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        List<NotificationEntity> byOrderByNotificationStateDesc = notificationRepository.findByUserEntityOrderByNotificationStateDescDateTimeDesc(userByEmail);

        return notificationEntityMapper.entitiesToDTO(byOrderByNotificationStateDesc);
    }

    public void updateNotificationsState() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity userByEmail = userService.getUserByEmail(userEmail);
        List<NotificationEntity> byOrderByNotificationStateDesc = notificationRepository.findByUserEntityOrderByNotificationStateDescDateTimeDesc(userByEmail);
        for (NotificationEntity notificationEntity : byOrderByNotificationStateDesc) {
            notificationEntity.setNotificationState(NotificationState.READ);
        }
        notificationRepository.saveAll(byOrderByNotificationStateDesc);

    }

    public UserDTO notifyUserForAssignment(UserDTO userDTO, String amount, String description) {

        UserEntity user = userService.getUserByEmail(userDTO.email());

        NotificationEntity notificationEntity = new NotificationEntity(ADMIN_EMAIL+":"+description + ":" +
                amount, user);
        NotificationEntity save = notificationRepository.save(notificationEntity);

        NotificationDTO notificationDTO = notificationEntityMapper.entityToDTO(save);

        template.convertAndSendToUser(userDTO.email(), "/queue/notify", notificationDTO);

        return userDTO;
    }
}
