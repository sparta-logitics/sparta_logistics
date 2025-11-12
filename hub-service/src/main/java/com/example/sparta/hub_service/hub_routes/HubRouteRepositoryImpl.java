package com.example.sparta.hub_service.hub_routes;

import static com.example.sparta.hub_service.core.domain.QHubRoute.hubRoute;
import static com.example.sparta.hub_service.core.domain.QHubRouteSegment.hubRouteSegment;

import com.example.sparta.hub_service.core.vo.HubId;
import com.example.sparta.hub_service.hub_routes.dto.HubRouteResult;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<HubRouteResult> findDetailedRouteBetween(
        HubId departureHubId,
        HubId arrivalHubId
    ) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(hubRoute).distinct()
                .leftJoin(hubRoute.hubRouteSegments, hubRouteSegment).fetchJoin()
                .leftJoin(hubRouteSegment.hubConnection).fetchJoin()
                .where(
                    hubRoute.departureHubId.eq(departureHubId),
                    hubRoute.arrivalHubId.eq(arrivalHubId),
                    hubRoute.deletedAt.isNull()
                )
                .fetchOne())
            .map(HubRouteResult::from);
    }
}
