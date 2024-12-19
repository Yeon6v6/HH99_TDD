package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointRepository {

    private final UserPointTable userPointTable;

    // 사용자 포인트 조회
    public UserPoint selectById(long userId) {
        return userPointTable.selectById(userId);
    }

    // 사용자 포인트 저장 또는 업데이트
    public UserPoint insertOrUpdate(long userId, long point) {
        return userPointTable.insertOrUpdate(userId, point);
    }
}
