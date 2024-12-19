package io.hhplus.tdd.point.facade;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointFacade {
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    //사용자 별로 요청을 순차적으로 처리하기 위한 Lock
    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    private Lock getUserLock(long userId) {
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    // 사용자 포인트 조회
    public UserPoint getUserPoint(long userId) {
        Lock lock = getUserLock(userId);
        try {
            lock.lock();
            return pointService.getUserPoint(userId);
        } finally {
            lock.unlock();
        }
    }

    // 포인트 충전
    public UserPoint chargePoint(long userId, long amount) {
        Lock lock = getUserLock(userId);
        try {
            lock.lock();
            return pointService.chargePoint(userId, amount);
        } finally {
            lock.unlock();
        }
    }

    // 포인트 사용
    public UserPoint usePoint(long userId, long amount) {
        Lock lock = getUserLock(userId);
        try {
            lock.lock();
            return pointService.usePoint(userId, amount);
        } finally {
            lock.unlock();
        }
    }

    // 포인트 충전/사용 내역 조회
    public List<PointHistory> getPointHistory(long userId) {
        Lock lock = getUserLock(userId);
        try {
            lock.lock();
            return pointHistoryService.getPointHistory(userId);
        } finally {
            lock.unlock();
        }
    }
}
