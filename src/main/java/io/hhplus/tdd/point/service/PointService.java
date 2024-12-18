package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PointService {
    UserPoint getUserPoint(long userId); //사용자 포인트 조회
    List<PointHistory> getPointHistory(long userId); //포인트 충전/사용 내역 조회
    UserPoint chargePoint(long userId, long amount); //포인트 충전
    UserPoint usePoint(long userId, long amount); //포인트 사용
}
