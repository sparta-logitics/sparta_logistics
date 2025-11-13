package com.example.sparta.hub_service.hub.infrastructure.repository;

import static com.example.sparta.hub_service.hub.domain.entity.QHub.hub;

import com.example.sparta.hub_service.hub.application.dto.HubResult;
import com.example.sparta.hub_service.hub.application.dto.HubSearchCondition;
import com.example.sparta.hub_service.hub.domain.vo.HubCode;
import com.example.sparta.hub_service.hub.domain.vo.HubStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HubResult> search(
        HubSearchCondition condition,
        Pageable pageable
    ) {
        List<HubResult> content = queryFactory
            .select(
                Projections.constructor(
                    HubResult.class,
                    hub.id,
                    hub.code,
                    hub.name,
                    hub.address.address,
                    hub.status,
                    hub.hubLocation.latitude,
                    hub.hubLocation.longitude
                )
            )
            .from(hub)
            .where(
                nameContains(condition.name()),
                addressContains(condition.address()),
                codeEq(condition.code()),
                statusEq(condition.status())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(hub.countDistinct())
            .from(hub)
            .where(
                nameContains(condition.name()),
                addressContains(condition.address()),
                codeEq(condition.code()),
                statusEq(condition.status())
            );

        return PageableExecutionUtils.getPage(
            content,
            pageable,
            countQuery::fetchOne
        );
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? hub.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression addressContains(String address) {
        return address != null ? hub.address.address.containsIgnoreCase(address) : null;
    }

    private BooleanExpression codeEq(HubCode code) {
        return code != null ? hub.code.eq(code) : null;
    }

    private BooleanExpression statusEq(HubStatus status) {
        return status != null ? hub.status.eq(status) : null;
    }
}
