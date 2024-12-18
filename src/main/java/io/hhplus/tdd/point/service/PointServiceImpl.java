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
public class PointServiceImpl implements PointService {
    private static final Long MAX_POINT = 10_000_000L; // 최대 포인트 제한

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    //사용자 별로 요청을 순차적으로 처리하기 위한 Lock
    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    //같은 Taable을 사용할 수 있도록 생성자 선언
    public PointServiceImpl(UserPointRepository userPointRepository, PointHistoryRepository pointHistoryRepository) {
        this.userPointRepository = userPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    private Lock getUserLock(long userId) {
        //사용자별 락을 생성하거나 기존 락 반환
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    /**
     * 사용자 포인트 조회
     * @param userId
     * @return
     */
    @Override
    public UserPoint getUserPoint(long userId) {
        Lock lock = getUserLock(userId);
        try{
            lock.lock();

            return userPointRepository.selectById(userId);
        }finally{
            lock.unlock();
        }
    }

    /**
     * 포인트 충전
     * @param userId
     * @param amount
     * @return
     */
    @Override
    public UserPoint chargePoint(long userId, long amount){
        Lock lock = getUserLock(userId);
        try{
            lock.lock();
            //충전 할 포인트 체크
            if(amount <= 0){
                throw new IllegalArgumentException("충전 포인트를 다시 설정해주세요!");
            }

            //현재 사용자 포인트 조회
            UserPoint curUserPoint = userPointRepository.selectById(userId);
            long newPoint = curUserPoint.point() + amount;

            //최대 포인트 제한 체크
            if(newPoint > MAX_POINT) {
                //throw new IllegalArgumentException("최대 포인트를 초과할 수 없습니다." + "\n" + "남은 포인트 : " + (MAX_POINT-curUserPoint.point()));
                throw new IllegalArgumentException("최대 포인트를 초과할 수 없습니다.");
            }

            //포인트 충전(update)
            UserPoint userPoint = userPointRepository.insertOrUpdate(userId, newPoint);

            //포인트 충전 이력 저장
            pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE);

            return userPoint;
        }finally{
            lock.unlock();
        }
    }

    /**
     * 포인트 사용
     * @param userId
     * @param amount
     * @return
     */
    @Override
    public UserPoint usePoint(long userId, long amount){
        Lock lock = getUserLock(userId);
        try{
            lock.lock();

            //사용 할 포인트 체크
            if(amount <= 0){
                throw new IllegalArgumentException("사용 할 포인트를 다시 설정해주세요!");
            }

            //현재 포인트 조회
            UserPoint curUserPoint = userPointRepository.selectById(userId);
            //포인트 사용 가능 여부 체크
            if(curUserPoint.point() < amount){
                throw new IllegalArgumentException("포인트가 부족합니다.");
            }

            //포인트 차감
            long newPoint = curUserPoint.point() - amount; //현재 포인트에서 사용 할 포인트 차감
            UserPoint userPoint = userPointRepository.insertOrUpdate(userId, newPoint);

            //포인트 충전 이력 저장
            pointHistoryRepository.insert(userId, amount, TransactionType.USE);

            return userPoint;

        }finally{
            lock.unlock();
        }
    }
}