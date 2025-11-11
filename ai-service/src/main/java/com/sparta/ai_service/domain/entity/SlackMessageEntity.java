package com.sparta.ai_service.domain.entity;

import com.example.sparta.common.model.BaseEntity;
import com.sparta.ai_service.domain.enums.ChannelTypeEnum;
import com.sparta.ai_service.domain.enums.MessageStatusEnum;
import com.sparta.ai_service.domain.enums.SenderTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_slack_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SlackMessageEntity extends BaseEntity {
    public static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slack_message_id", updatable = false, nullable = false)
    @Comment("Slack 메시지 고유 ID")
    private UUID slackMessageId;

    @Column(name = "order_id", nullable = false)
    @Comment("관련 주문의 ID (Order-Service에서 관리)")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Comment("메시지 발신 주체 타입 (USER, SYSTEM)")
    private SenderTypeEnum senderType;

    @Comment("발신자 User-Service 기준 UUID (SYSTEM일 경우 SYSTEM_UUID 사용)")
    private UUID senderId;

    @Comment("발신자 Slack ID (채널 또는 개인)")
    private String senderSlackId;

    @Enumerated(EnumType.STRING)
    @Comment("수신자 타입: 개인(User) 또는 채널(Channel)")
    private ChannelTypeEnum recipientType;

    @Comment("수신자 User-Service 기준 UUID (recipientType이 USER일 경우만)")
    private UUID recipientId;

    @Comment("수신자 Slack ID (채널 또는 개인)")
    private String recipientSlackId;

    @Column(columnDefinition = "text")
    @Comment("Slack에 발송된 실제 메시지 내용")
    private String message;

    @Column(columnDefinition = "text")
    @Comment("AI 예측 요청에 대한 응답 전체 로그 (디버깅용)")
    private String aiResponseLog;

    @Comment("AI가 계산한 예상 배송 시간")
    private LocalDateTime predictedShippingTime;

    @Comment("Slack 메시지가 실제 발송된 시각")
    private LocalDateTime sentAt;

    @Builder.Default
    @Comment("AI 예측이 정상적으로 계산되었는지 여부")
    private Boolean predictionSucceeded = false;

    @Builder.Default
    @Comment("Slack 발송 재시도 횟수")
    private Integer retryCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Comment("Slack 메시지 발송 상태 (PENDING, SENT, FAILED)")
    private MessageStatusEnum status = MessageStatusEnum.PENDING;

    public static SlackMessageEntity createForOrder(
            UUID orderId,
            SenderTypeEnum senderType,
            UUID senderId,
            String senderSlackId,
            ChannelTypeEnum recipientType,
            UUID recipientId,
            String recipientSlackId,
            String message
    ) {
        return SlackMessageEntity.builder()
                .orderId(orderId)
                .senderType(senderType)
                .senderId(senderId)
                .senderSlackId(senderSlackId)
                .recipientType(recipientType)
                .recipientId(recipientId)
                .recipientSlackId(recipientSlackId)
                .message(message)
                .predictionSucceeded(false)
                .retryCount(0)
                .status(MessageStatusEnum.PENDING)
                .build();
    }
}
