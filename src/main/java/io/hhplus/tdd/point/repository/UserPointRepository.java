package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.dto.UserPoint;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserPointRepository {

    private final Map<Long, UserPoint> userPointTable = new ConcurrentHashMap<>();

    // 사용자 포인트 조회
    public UserPoint selectById(long userId) {
        return userPointTable.getOrDefault(userId, UserPoint.empty(userId));
    }

    // 사용자 포인트 저장 또는 업데이트
    public UserPoint insertOrUpdate(long userId, long point) {
        UserPoint updatedPoint = new UserPoint(userId, point, System.currentTimeMillis());
        userPointTable.put(userId, updatedPoint);
        return updatedPoint;
    }
}
