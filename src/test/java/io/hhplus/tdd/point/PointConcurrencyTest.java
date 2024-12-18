package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.service.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class PointConcurrencyTest  {
    
    @Autowired
    private PointServiceImpl pointServiceImpl;

    private long userId;

    @BeforeEach
    void setUp() {
        userId = 1L; //테스트를 위해 사용자 지정
        //pointServiceImpl.chargePoint(userId, 0); // 초기화
        UserPoint userPoint = pointServiceImpl.getUserPoint(userId); //신규 생성
    }

    @Test
    void concurrentChargePointTest() throws InterruptedException {
        // Given
        int threads = 10; //동시에 충전 할 스레드 수
        long chargeAmount = 100L; //각 스레드가 충전 할 포인트
        long expectedTotal = threads * chargeAmount;

        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        // When
        for (int i = 0; i < threads; i++) {
            executorService.execute(() -> pointServiceImpl.chargePoint(userId, chargeAmount));
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Then
        UserPoint userPoint = pointServiceImpl.getUserPoint(userId);
        assertThat(userPoint.point()).isEqualTo(expectedTotal);
    }

    @Test
    void concurrentUsePointTest() throws InterruptedException {
        long initPoint = 1000L; //초기로 지정 할 포인트 값
        int threads = 5; //동시에 사용 할 스레드 수
        long useAmount = 100L; //각 스레드가 사용 할 포인트
        pointServiceImpl.chargePoint(userId, initPoint); //초기 포인트로 충전

        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        // When
        for (int i = 0; i < threads; i++) {
            executorService.execute(() -> {
                try {
                    pointServiceImpl.usePoint(userId, useAmount);
                } catch (IllegalArgumentException e) {
                    System.err.println("포인트 부족 예외 발생: " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Then: 최종 포인트가 예상값과 동일한지 확인
        UserPoint userPoint = pointServiceImpl.getUserPoint(userId);
        Long expectedTotal = initPoint - (threads * useAmount); // 초기 포인트 - 총 사용량
        assertThat(userPoint.point()).isEqualTo(expectedTotal);
    }
}
