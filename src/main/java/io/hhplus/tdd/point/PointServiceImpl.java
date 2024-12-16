package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    //같은 Taable을 사용할 수 있도록 생성자 선언
    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * 사용자 포인트 조회
     * @param userId
     * @return
     */
    @Override
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    /**
     * 포인트 충전/사용 내역 조회
     * @param userId
     * @return
     */
    @Override
    public List<PointHistory> getPointHistory(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 포인트 충전
     * @param userId
     * @param amount
     * @return
     */
    @Override
    public UserPoint chargePoint(long userId, long amount){
        //현재 사용자 포인트 조회
        UserPoint curUserPoint = userPointTable.selectById(userId);
        long newPoint = curUserPoint.point() + amount;

        //포인트 충전(update)
        UserPoint userPoint = userPointTable.insertOrUpdate(userId, newPoint);

        //포인트 충전 이력 저장
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPoint;
    }

    /**
     * 포인트 사용
     * @param userId
     * @param amount
     * @return
     */
    @Override
    public UserPoint usePoint(long userId, long amount){
        //현재 포인트 조회
        UserPoint curUserPoint = userPointTable.selectById(userId);
        //포인트 사용 가능 여부 체크
        if(curUserPoint.point() < amount){
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        //포인트 갱신
        long newPoint = curUserPoint.point() - amount; //현재 포인트에서 사용 할 포인트 차감
        UserPoint userPoint = userPointTable.insertOrUpdate(userId, newPoint);

        //포인트 충전 이력 저장
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return userPoint;
    }
}
