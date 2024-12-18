package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.TransactionType;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointHistoryServiceImpl implements PointHistoryService {
    private static final Long MAX_POINT = 10_000_000L; // 최대 포인트 제한

    private final PointHistoryRepository pointHistoryRepository;

    //사용자 별로 요청을 순차적으로 처리하기 위한 Lock
    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    //같은 Taable을 사용할 수 있도록 생성자 선언
    public PointHistoryServiceImpl(PointHistoryRepository pointHistoryRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
    }

    private Lock getUserLock(long userId) {
        //사용자별 락을 생성하거나 기존 락 반환
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    /**
     * 포인트 충전/사용 내역 조회
     * @param userId
     * @return
     */
    @Override
    public List<PointHistory> getPointHistory(long userId){
        Lock lock = getUserLock(userId);
        lock.lock();
        try{
            return pointHistoryRepository.selectAllByUserId(userId);
        }finally{
            lock.unlock();
        }
    }

}