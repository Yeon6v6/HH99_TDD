package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.TransactionType;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.facade.PointFacade;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//@SpringBootTest
public class PointFacadeTest {
    private PointFacade pointFacade;
    private UserPoint userPoint;

    @Mock
    UserPointRepository userPointRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    //테스트 메소드 실행 전 beforeEach 추가
    @BeforeEach
    public void setUp() {
        //Mockito 초기화
        MockitoAnnotations.openMocks(this);

        // PointFacade 초기화
        pointFacade = new PointFacade( new PointService(userPointRepository, pointHistoryRepository), new PointHistoryService(pointHistoryRepository));

        //테스트 객체 생성(포인트 초기 값 : 100L)
        userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());
    }

    //Test는 일반적으로 Given / When / Then 패턴으로 진행하는 것이 좋다.
    /*
    @Test
    public void test(){
        //Given(데이터가 주어질 때)
        //When(기능을 실행하면)
        //Then(어떠한 결과를 기대한다)
    }
     */

    /**
     * 포인트 조회 테스트
     */
    @Test
    public void getUserPointTest() {
        // Given
        long userId = 1L;
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        // When
        UserPoint result = pointFacade.getUserPoint(userId);

        // Then
        assertEquals(100L, result.point());
        verify(userPointRepository).selectById(userId); // Mock 호출 검증
    }
    /**
     * 포인트 조회 테스트 : 사용자 포인트 없음
     */
    @Test
    public void getUserPointTest_UserNotFound() {
        // Given
        long userId = 999L; //존재하지 않는 사용자
        when(userPointRepository.selectById(userId)).thenReturn(null);

        // When + Then
        PointException e = assertThrows(PointException.class, () -> pointFacade.chargePoint(userId, 500L));

        assertThat(e).isSameAs(PointException.EX_NOT_FOUND);// Mock 호출 검증
    }

    /**
     * 포인트 내역 조회 테스트
      */ 
    @Test
    public void getPointHistoryTest() {
        // Given
        long userId = 1L;
        List<PointHistory> historyList = List.of(
                new PointHistory(1L, userId, 500L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, -300L, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(historyList);

        // When
        List<PointHistory> result = pointHistoryRepository.selectAllByUserId(userId);

        // Then
        assertEquals(2, result.size());
        assertEquals(500L, result.get(0).amount());
        assertEquals(-300L, result.get(1).amount());
        verify(pointHistoryRepository).selectAllByUserId(userId); // Mock 호출 검증
    }

    /**
     * 포인트 충전 테스트
     */
    @Test
    public void chargePointTest() {
        // Given
        long userId = 1L;
        long chargeAmount = 200L;

        when(userPointRepository.selectById(userId)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(userId, userPoint.point() + chargeAmount))
                .thenReturn(new UserPoint(userId, userPoint.point() + chargeAmount, System.currentTimeMillis()));

        // When
        UserPoint result = pointFacade.chargePoint(userId, chargeAmount);

        // Then
        assertEquals(userPoint.point() + chargeAmount, result.point());
        verify(userPointRepository).selectById(userId);
        verify(userPointRepository).insertOrUpdate(userId, userPoint.point() + chargeAmount);
        verify(pointHistoryRepository).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE));
    }

    /**
     * 포인트 충전 테스트 : 최대 포인트 초과 예외 발생
     */
    @Test
    public void chargePoint_OverMaxPoint() {
        // Given
        long userId = 1L;
        long initPoint = 950_000L;
        long chargeAmount = 100_000L;

        UserPoint userPointWithHighBalance = new UserPoint(userId, initPoint, System.currentTimeMillis());
        when(userPointRepository.selectById(userId)).thenReturn(userPointWithHighBalance);

        // When + Then
        PointException e = assertThrows(PointException.class, () -> pointFacade.chargePoint(userId, chargeAmount));

        assertThat(e).isSameAs(PointException.EX_MAX);
    }

    /**
     * 포인트 사용 테스트
     */
    @Test
    public void usePointTest() {
        // Given
        long userId = 1L;
        long initPoint = 100L;
        long useAmount = 200L;

        pointFacade.chargePoint(userId, initPoint); // 초기 포인트 충전

        // When & Then
        PointException ex = assertThrows(PointException.class, () -> pointFacade.usePoint(userId, useAmount));

        assertThat(ex).isSameAs(PointException.EX_LOW);
    }

    /**
     * 포인트 사용 테스트 : 잔여 포인트 부족 예외 발생
     */
    @Test
    public void usePointTest_InsuffPoint() {
        // Given
        long userId = 1L;
        long curPoint = 50_000L; // 현재 포인트
        long useAmount = 100_000L;  // 사용 할 포인트

        UserPoint userPoint = new UserPoint(userId, curPoint, System.currentTimeMillis());
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        // When + Then
        PointException e = assertThrows(PointException.class, () -> pointFacade.usePoint(userId, useAmount));

        // 예외 메시지 검증
        assertThat(e).isSameAs(PointException.EX_LOW);
    }
}
