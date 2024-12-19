package io.hhplus.tdd.point.facade;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PointFacade {
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    // 사용자별 작업 큐 및 Lock 관리
    private final ConcurrentHashMap<Long, Queue<Runnable>> userQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    // 사용자별 Lock 가져오기
    private ReentrantLock getUserLock(long userId) {
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    // 사용자별 Queue 가져오기
    private Queue<Runnable> getUserQueue(long userId) {
        return userQueues.computeIfAbsent(userId, id -> new LinkedList<>());
    }

    // 작업 큐에 작업 추가 및 처리 시작
    private void submitTask(long userId, Runnable task) {
        Queue<Runnable> queue = getUserQueue(userId);

        synchronized (queue) {
            queue.add(task); // 작업을 큐에 추가
            if (queue.size() == 1) {
                processQueue(userId); // 큐가 비어 있었다면 처리 시작
            }
        }
    }

    // 큐 처리
    private void processQueue(long userId) {
        Queue<Runnable> queue = getUserQueue(userId);
        ReentrantLock lock = getUserLock(userId);

        new Thread(() -> {
            while (true) {
                Runnable task;
                synchronized (queue) {
                    task = queue.poll(); // 큐에서 작업을 갖고오기
                    if (task == null) {
                        return; // 큐가 비어 있으면 종료
                    }
                }
                lock.lock();
                try {
                    task.run(); // 작업 실행
                } finally {
                    lock.unlock();
                }
            }
        }).start();
    }

    // 제너릭 메소드로 사용(공통 작업)
    private <T> T executeTask(long userId, Supplier<T> task) {
        AtomicReference<T> result = new AtomicReference<>();
        submitTask(userId, () -> result.set(task.get()));
        return result.get();
    }

    // 사용자 포인트 조회
    public UserPoint getUserPoint(long userId) {
        return executeTask(userId, () -> pointService.getUserPoint(userId));
    }

    // 포인트 충전
    public UserPoint chargePoint(long userId, long amount) {
        return executeTask(userId, () -> pointService.chargePoint(userId, amount));
    }

    // 포인트 사용
    public UserPoint usePoint(long userId, long amount) {
        return executeTask(userId, () -> pointService.usePoint(userId, amount));
    }

    // 포인트 충전/사용 내역 조회
    public List<PointHistory> getPointHistory(long userId) {
        return executeTask(userId, () -> pointHistoryService.getPointHistory(userId));
    }
}
