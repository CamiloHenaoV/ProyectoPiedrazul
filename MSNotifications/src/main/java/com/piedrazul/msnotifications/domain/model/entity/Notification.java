package com.piedrazul.msnotifications.domain.model.entity;

import com.piedrazul.msnotifications.domain.model.entity.enums.NotificationChannel;
import com.piedrazul.msnotifications.domain.model.entity.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "notifications")
@Getter @Setter
public class Notification {

    @Id
    @GeneratedValue()
    private Long id;

    @Column(nullable = false)
    private String recipient;          // email o teléfono

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private String originEvent;        // ej: "user.registered"

    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    protected Notification() {}

    public static Notification create(String recipient, String subject,
                                      String body, NotificationChannel channel,
                                      String originEvent) {
        var n = new Notification();
        n.recipient   = recipient;
        n.subject     = subject;
        n.body        = body;
        n.channel     = channel;
        n.originEvent = originEvent;
        n.status      = NotificationStatus.PENDING;
        n.createdAt   = LocalDateTime.now();
        return n;
    }

    public void markSent() {
        this.status  = NotificationStatus.SENT;
        this.sentAt  = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status       = NotificationStatus.FAILED;
        this.errorMessage = reason;
    }

}
