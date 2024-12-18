package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.TransactionType;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class PointHistoryRepository {

    private final Map<Long, List<PointHistory>> pointHistoryTable = new ConcurrentHashMap<>();

    // 특정 사용자 ID의 모든 포인트 내역 조회
    public List<PointHistory> selectAllByUserId(long userId) {
        return pointHistoryTable.getOrDefault(userId, new ArrayList<>());
    }

    // 포인트 충전/사용 이력 추가
    public void insert(long userId, long amount, TransactionType type) {
        // ID 대신 시간 기반 생성
        PointHistory history = new PointHistory(System.currentTimeMillis(), userId, amount, type, System.currentTimeMillis());
        pointHistoryTable.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(history);
    }

}
