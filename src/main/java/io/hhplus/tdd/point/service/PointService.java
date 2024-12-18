package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.stereotype.Service;

@Service
public class PointService {
    private static final Long MAX_POINT = 10_000_000L; // 최대 포인트 제한

    private final UserPointRepository userPointRepository;

    public PointService(UserPointRepository userPointRepository, PointHistoryRepository pointHistoryRepository) {
        this.userPointRepository = userPointRepository;
    }

    /**
     * 사용자 포인트 조회
     * @param userId
     * @return
     */
    public UserPoint getUserPoint(long userId) {
        UserPoint userPoint = userPointRepository.selectById(userId);
        //사용자 포인트 존재여부 체크
        if (userPoint == null) {
            throw PointException.EX_NOT_FOUND; 
        }
        return userPoint;
    }

    /**
     * 포인트 충전
     * @param userId
     * @param amount
     * @return
     */
    public UserPoint chargePoint(long userId, long amount){
        //충전 할 포인트 체크
        if(amount <= 0){
            throw PointException.EX_INV;
        }

        //현재 사용자 포인트 조회
        UserPoint curUserPoint = userPointRepository.selectById(userId);
        long newPoint = curUserPoint.point() + amount;

        //최대 포인트 제한 체크
        if(newPoint > MAX_POINT) {
            throw PointException.EX_MAX;
        }

        //포인트 충전(update)
        UserPoint userPoint = userPointRepository.insertOrUpdate(userId, newPoint);

        return userPoint;

    }

    /**
     * 포인트 사용
     * @param userId
     * @param amount
     * @return
     */
    public UserPoint usePoint(long userId, long amount){
        //사용 할 포인트 체크
        if(amount <= 0){
            throw PointException.EX_INV;
        }

        //현재 포인트 조회
        UserPoint curUserPoint = userPointRepository.selectById(userId);
        //포인트 사용 가능 여부 체크
        if(curUserPoint.point() < amount){
            throw PointException.EX_LOW;
        }

        //포인트 차감
        long newPoint = curUserPoint.point() - amount; //현재 포인트에서 사용 할 포인트 차감
        UserPoint userPoint = userPointRepository.insertOrUpdate(userId, newPoint);

        return userPoint;
    }
}
