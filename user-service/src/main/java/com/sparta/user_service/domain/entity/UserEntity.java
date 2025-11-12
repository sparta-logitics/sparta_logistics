package com.sparta.user_service.domain.entity;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.example.sparta.common.model.BaseEntity;
import com.sparta.user_service.domain.enums.UserStatusEnum;
import com.sparta.user_service.presentation.request.UserCreateRequest;
import com.example.sparta.common.enums.UserRoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name="p_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column()
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String slackId;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private UserStatusEnum status = UserStatusEnum.PENDING;

    // 권한별 식별값 추가
//    @Column(name = "hub_id")
//    private UUID hubId;
//
//    @Column(name = "company_id")
//    private UUID companyId;
//
//    @Column(name = "delivery_id")
//    private UUID deliveryId;
//
//    @Column(name = "master_id")
//    private UUID masterId;

    public static UserEntity create(UserCreateRequest userCreateRequest, String encodedPassword){
        return UserEntity.builder()
                .role(userCreateRequest.getRole())
                .username(userCreateRequest.getUsername())
                .name(userCreateRequest.getName())
                .password(encodedPassword)
                .slackId(userCreateRequest.getSlackId())
                .build();
    }

    public void changeStatus(UserStatusEnum status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.status = status;
    }

    public void changeRole(UserRoleEnum role) {
        this.role = role;
    }

    public void updateUserInfo(String name, String slackId) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (slackId != null && !slackId.isBlank()) {
            this.slackId = slackId;
        }
    }
}

