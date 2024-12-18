package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PointHistoryService {
    List<PointHistory> getPointHistory(long userId); //포인트 충전/사용 내역 조회
}
