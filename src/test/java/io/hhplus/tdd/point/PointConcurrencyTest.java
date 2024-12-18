package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.facade.PointFacade;
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
    private PointFacade pointFacade;

    private long userId;

    @BeforeEach
    void setUp() {
        userId = 1L; //테스트를 위한 사용자 ID 지정
        UserPoint userPoint = pointFacade.getUserPoint(userId); //신규 생성(사용자 포인트 초기화)
    }

    @Test
    void concurrentChargePointTest() throws InterruptedException {
        // Given
        int threads = 10; //동시에 충전 할 스레드 수
        long chargeAmount = 100L; //각 스레드가 충전 할 포인트
        long totalPoint = threads * chargeAmount; //최종 예상 포인트

        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        // When
        for (int i = 0; i < threads; i++) {
            executorService.execute(() -> pointFacade.chargePoint(userId, chargeAmount));
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Then
        UserPoint userPoint = pointFacade.getUserPoint(userId);
        assertThat(userPoint.point()).isEqualTo(totalPoint);
    }

    @Test
    void concurrentUsePointTest() throws InterruptedException {
        long initPoint = 1000L; //초기로 지정 할 포인트 값
        int threads = 5; //동시에 사용 할 스레드 수
        long useAmount = 100L; //각 스레드가 사용 할 포인트
        long totalPoint = threads * useAmount; //최종 사용 포인트
        pointFacade.chargePoint(userId, initPoint); //초기 포인트로 충전

        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        // When
        for (int i = 0; i < threads; i++) {
            executorService.execute(() -> {
                try {
                    pointFacade.usePoint(userId, useAmount);
                } catch (IllegalArgumentException e) {
                    System.err.println("포인트 부족 예외 발생: " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Then: 최종 포인트가 예상값과 동일한지 확인
        UserPoint userPoint = pointFacade.getUserPoint(userId);
        Long expectedTotal = initPoint - totalPoint; // 초기 포인트 - 총 사용량
        assertThat(userPoint.point()).isEqualTo(expectedTotal);
    }
}
