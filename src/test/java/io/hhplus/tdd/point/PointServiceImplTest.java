package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc //MockMvc를 사용하여 컨트롤러를 테스트할 수 있도록 설정
public class PointServiceImplTest {
    private PointServiceImpl pointServiceImpl;
    private UserPoint userPoint;

    @Mock
    UserPointTable userPointTable;
    @Mock
    PointHistoryTable pointHistoryTable;


    //테스트 메소드 실행 전 beforeEach 추가
    @BeforeEach
    public void setUp() {
        //Mockito 초기화
        MockitoAnnotations.openMocks(this);
        
        //pointServiceImpl 초기화
        pointServiceImpl = new PointServiceImpl(userPointTable, pointHistoryTable);

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
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // When
        UserPoint result = pointServiceImpl.getUserPoint(userId);

        // Then
        assertEquals(100L, result.point());
        verify(userPointTable).selectById(userId); // Mock 호출 검증
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
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(historyList);

        // When
        List<PointHistory> result = pointServiceImpl.getPointHistory(userId);

        // Then
        assertEquals(2, result.size());
        assertEquals(500L, result.get(0).amount());
        assertEquals(-300L, result.get(1).amount());
        verify(pointHistoryTable).selectAllByUserId(userId); // Mock 호출 검증
    }

    /**
     * 포인트 충전 테스트
     */
    @Test
    public void chargePointTest() {
        // Given
        long userId = 1L;
        long chargeAmount = 200L;

        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(userId, userPoint.point() + chargeAmount))
                .thenReturn(new UserPoint(userId, userPoint.point() + chargeAmount, System.currentTimeMillis()));

        // When
        UserPoint result = pointServiceImpl.chargePoint(userId, chargeAmount);

        // Then
        assertEquals(userPoint.point() + chargeAmount, result.point());
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, userPoint.point() + chargeAmount);
        verify(pointHistoryTable).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    /**
     * 포인트 사용 테스트
     */
    @Test
    public void usePointTest() {
        // Given
        long userId = 1L;
        long useAmount = 50L;

        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(userId, userPoint.point() - useAmount))
                .thenReturn(new UserPoint(userId, userPoint.point() - useAmount, System.currentTimeMillis()));

        // When
        UserPoint result = pointServiceImpl.usePoint(userId, useAmount);

        // Then
        assertEquals(userPoint.point() - useAmount, result.point());
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, userPoint.point() - useAmount);
        verify(pointHistoryTable).insert(eq(userId), eq(-useAmount), eq(TransactionType.USE), anyLong());
    }
}
