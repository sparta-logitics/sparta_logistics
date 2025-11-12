package com.sparta.deliveryservice.repository.specification;

import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.DeliveryRouteHistory;
import com.sparta.deliveryservice.domain.DeliveryRouteHistory_;
import com.sparta.deliveryservice.domain.Delivery_;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import com.sparta.deliveryservice.dto.request.DeliverySearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Driver;
import java.util.UUID;

@Component
public class DeliverySpecification {

    /**
     * TDD [GREEN]: DeliverySearchCriteria DTO를 기반으로
     * 동적인 Specification(쿼리 조건)을 생성합니다.
     */
    public static Specification<Delivery> build(DeliverySearchCriteria criteria) {

        // 1. 'WHERE 1=1'과 같은 기본 Specification을 생성합니다.
        Specification<Delivery> spec = (root, query, cb) -> null;

        // 2. [TDD 검증] 'status' 조건이 DTO에 포함되어 있다면,
        if (criteria.getStatus() != null) {
            // 'status'로 필터링하는 Specification을 'AND' 조건으로 추가합니다.
            spec = spec.and(withStatus(criteria.getStatus()));
        }

        // 3. (다음 TDD) 'driverId' 조건이 있다면 'AND'로 추가...
         if (criteria.getDriverId() != null) {
             spec = spec.and(withDriverId(criteria.getDriverId()));
         }

         // recipientName (LIKE) 조건 로직 추가
        // StringUtils.hasText는 null, "", " " (공백)을 모두 걸러줍니다
        if (StringUtils.hasText(criteria.getRecipientName())) {
            spec = spec.and(withRecipientNameLike(criteria.getRecipientName()));
        }

        return spec;
    }

    /**
     * 'status' 필드로 'equal' 조건을 생성하는 Specification
     */
    private static Specification<Delivery> withStatus(DeliveryStatus status) {
        // root.get("status")는 Delivery 엔티티의 'status' 필드명을 의미한다
        return (root, query, criteriaBuilder) ->
                // [REFACTOR] "매직 스트링"을 Metamodel로 교체
//                criteriaBuilder.equal(root.get("status"), status);
                criteriaBuilder.equal(root.get(Delivery_.status), status);
    }

    /**
     * [GREEN] 'driverId' 필드로 'OR' 조건을 생성하는 Specification
     * 이 쿼리는 LEFT JOIN과 OR 조건을 생성
     */
    private static Specification<Delivery> withDriverId(UUID driverId) {
        return (root, query, criteriaBuilder) -> {

            // 1. [핵심] 중복된 Delivery가 조회되는 것을 방지
            // 예: driverId=1이 companyDriverId이면서 route.driverId일 경우
            query.distinct(true);

            // 2. Delivery(root)와 DeliveryRouteHistory(자식)를 LEFT JOIN
            Join<Delivery, DeliveryRouteHistory> routeJoin = root.join(Delivery_.routeHistories, JoinType.LEFT);

            // 3. 조건 1: companyDriverId가 일치하는 경우
            var companyDriverMatch = criteriaBuilder.equal(
                    root.get(Delivery_.companyDriverId), driverId
            );

            // 4. 조건 2: (JOIN된) routeHistories.driverId가 일치하는 경우
            var routeDriverMatch = criteriaBuilder.equal(
                    routeJoin.get(DeliveryRouteHistory_.driverId), driverId
            );

            // 5. [핵심] 두 조건을 OR로 결합하여 반환
            return criteriaBuilder.or(companyDriverMatch, routeDriverMatch);
        };
    }

    /**
     * [GREEN] 'recipientName' 필드로 'LIKE' 조건을 생성하는 Specification
     * 예: "김" -> "김%"
     */
    private static Specification<Delivery> withRecipientNameLike(String recipientName) {
        return (root, query, criteriaBuilder) ->
                // JPA Metamodel(Delivery_.recipientName)을 사용하여 타입-세이프하게 LIKE 쿼리 생성
                criteriaBuilder.like(
                        root.get(Delivery_.recipientName), // 컬럼: recipient_name
                        recipientName + "%" // 조건: "김%" starts with
                );
    }
}
