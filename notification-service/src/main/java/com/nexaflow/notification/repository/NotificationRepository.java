package com.nexaflow.notification.repository;

import com.nexaflow.notification.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Notification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    List<Notification> findByRecipientLoginOrderByCreatedAtDesc(String recipientLogin, Pageable pageable);

    List<Notification> findByOrganizationIdAndRecipientLoginOrderByCreatedAtDesc(
        Long organizationId,
        String recipientLogin,
        Pageable pageable
    );

    Optional<Notification> findOneByIdAndRecipientLogin(Long id, String recipientLogin);

    long countByRecipientLoginAndIsReadFalse(String recipientLogin);

    long countByOrganizationIdAndRecipientLoginAndIsReadFalse(Long organizationId, String recipientLogin);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification notification set notification.isRead = true where notification.recipientLogin = :recipientLogin and notification.isRead = false")
    int markAllAsReadByRecipientLogin(@Param("recipientLogin") String recipientLogin);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update Notification notification
        set notification.isRead = true
        where notification.organizationId = :organizationId
          and notification.recipientLogin = :recipientLogin
          and notification.isRead = false
        """
    )
    int markAllAsReadByOrganizationIdAndRecipientLogin(
        @Param("organizationId") Long organizationId,
        @Param("recipientLogin") String recipientLogin
    );
}
