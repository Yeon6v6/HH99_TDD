package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;

    public PointHistoryService(PointHistoryRepository pointHistoryRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
    }

    /**
     * 포인트 충전/사용 내역 조회
     * @param userId
     * @return
     */
    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }
}
