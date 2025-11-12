package com.example.sparta.order_service.infrastructure.repository;

import com.example.sparta.order_service.domain.repository.OrderQueryRepository;
import com.example.sparta.order_service.presentation.dto.request.SearchCondition;
import com.example.sparta.order_service.presentation.dto.response.OrderResponse;
import com.example.sparta.order_service.presentation.dto.response.QOrderResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.example.sparta.order_service.domain.entity.QOrder.order;

// TODO 권한에 따른 verify 절차 이후 조회 로직 변경 필요
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    private Expression<String> productNameDisplay() {
        return new CaseBuilder()
                .when(order.orderLineCount.eq(0)).then("상품 없음")
                .when(order.orderLineCount.eq(1)).then(order.representativeProductName)
                .otherwise(Expressions.stringTemplate(
                        "CONCAT({0}, ' 외 ', CAST({1} AS string), '개')",
                        order.representativeProductName,
                        order.orderLineCount.subtract(1) // (n-1)개
                ));
    }


    @Override
    public Page<OrderResponse> search(SearchCondition condition, String userEmail, Pageable pageable) {
        List<OrderResponse> content = queryFactory
                .select(new QOrderResponse(
                        order.orderId,
                        order.createdAt,
                        order.dueDate,
                        order.recipientInfo.companyName,
                        order.recipientInfo.name,
                        productNameDisplay(),
                        order.totalAmount,
                        order.status.stringValue()
                ))
                .from(order)
                .where(
//                        userEmailEq(userEmail),
                        statusEq(condition.state()),
                        orderDateBetween(condition.startDate(), condition.endDate()),
                        searchByKeyword(condition.searchType(), condition.keyword()),
                        order.deletedAt.isNull()
                )
                .orderBy(getSortOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(
//                        userEmailEq(userEmail),
                        statusEq(condition.state()),
                        orderDateBetween(condition.startDate(), condition.endDate()),
                        searchByKeyword(condition.searchType(), condition.keyword()),
                        order.deletedAt.isNull()
                );
        Long total = countQuery.fetchOne();
        if (total == null)
            total = 0L;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression userEmailEq(String userEmail) {
        return StringUtils.hasText(userEmail) ? order.userEmail.eq(userEmail) : null;
    }

    private BooleanExpression statusEq(String status) {
        if (!StringUtils.hasText(status))
            return null;

        return order.status.stringValue().eq(status);
    }

    private BooleanBuilder orderDateBetween(String startDate, String endDate) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(startDate)) {
            try {
                LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
                builder.and(order.createdAt.goe(startDateTime));
            } catch (DateTimeParseException e) {
            }
        }

        if (StringUtils.hasText(endDate)) {
            try {
                LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
                builder.and(order.createdAt.loe(endDateTime));
            } catch (DateTimeParseException e) {
            }
        }

        return builder.hasValue() ? builder : null;
    }

    private BooleanExpression searchByKeyword(String searchType, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        // SearchCondition의 searchType 값에 따라 분기
        return switch (searchType) {
            case "recipientCompany" -> order.recipientInfo.companyName.contains(keyword);
            case "originCompany" -> order.originInfo.companyName.contains(keyword);
            case "responsibility" -> order.recipientInfo.name.contains(keyword);
            case "product" -> order.representativeProductName.contains(keyword);
            default -> // searchType이 null이거나 "all" 등일 때 전체 검색
                    order.recipientInfo.companyName.contains(keyword)
                            .or(order.recipientInfo.name.contains(keyword))
                            .or(order.representativeProductName.contains(keyword));
        };
    }

    private OrderSpecifier<?>[] getSortOrderSpecifiers(Sort sort) {
        if (sort.isEmpty()) {
            // 기본 정렬: 생성일(createdAt) 내림차순
            return new OrderSpecifier[]{new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, order.createdAt)};
        }

        return sort.stream()
                .map(s -> {
                    com.querydsl.core.types.Order direction = s.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
                    String property = s.getProperty();

                    return switch (property) {
                        case "companyName" -> new OrderSpecifier<>(direction, order.recipientInfo.companyName);
                        case "responsibility" -> new OrderSpecifier<>(direction, order.recipientInfo.name);
                        case "dueDate" -> new OrderSpecifier<>(direction, order.dueDate);
                        case "totalAmount" -> new OrderSpecifier<>(direction, order.totalAmount);
                        case "productName" -> new OrderSpecifier<>(direction, order.representativeProductName);
                        case "orderDate" -> new OrderSpecifier<>(direction, order.createdAt);
                        default -> new OrderSpecifier<>(direction, order.createdAt);
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }
}
