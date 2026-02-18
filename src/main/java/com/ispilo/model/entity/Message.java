package com.ispilo.model.entity;

import com.ispilo.model.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_client_msg_id", columnList = "client_msg_id", unique = true),
    @Index(name = "idx_is_read", columnList = "is_read")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(name = "client_msg_id", nullable = false, unique = true)
    private String clientMsgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(length = 2000)
    private String content;

    @Column(name = "encrypted_content", length = 4000)
    private String encryptedContent;

    @Column(name = "is_encrypted")
    @Builder.Default
    private Boolean isEncrypted = true;

    @Column(name = "encryption_algorithm")
    @Builder.Default
    private String encryptionAlgorithm = "AES-256-GCM";

    @Column(name = "encryption_iv", length = 500)
    private String encryptionIv; // Initialization Vector for GCM mode

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
