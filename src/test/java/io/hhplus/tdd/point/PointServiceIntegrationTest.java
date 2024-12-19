package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PointServiceIntegrationTest {
    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        userPointTable.insertOrUpdate(userId, 0L); // 초기 포인트 0 설정
    }

    @DisplayName("여러 사용자의 포인트 충전을 동시에 실행한다.")
    @Test
    void concurrentChargeTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        long initialPoint = 0;
        long chargeAmount = 1000;
        int numberOfUsers = 5;

        for (long id = 1; id <= numberOfUsers; id++) {
            long usrId = id;
            userPointTable.insertOrUpdate(usrId, initialPoint);
            executorService.submit(() -> pointService.charge(usrId, chargeAmount));
        }

        executorService.shutdown();

        for (long id = 1; id <= numberOfUsers; id++) {
            UserPoint userPoint = userPointTable.selectById(id);
            assertThat(userPoint.point()).isEqualTo(chargeAmount);
        }
    }

    @DisplayName("한 사용자가 동시에 포인트 충전 및 사용을 한다.")
    @Test
    void concurrentChargeAndUseTest() throws InterruptedException {
        userPointTable.insertOrUpdate(userId, 10000L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> pointService.charge(userId, 5000L));
        executorService.submit(() -> pointService.use(userId, 3000L));

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(finished).isTrue(); // 모든 작업이 종료되었는지 확인
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(12000L);
    }

    @DisplayName("한 사용자가 동시에 포인트 사용 및 조회를 한다.")
    @Test
    void concurrentUseAndQueryTest() throws InterruptedException {
        userPointTable.insertOrUpdate(userId, 10000L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> pointService.use(userId, 5000L));
        executorService.submit(() -> {
            UserPoint userPoint = pointService.point(userId);
            assertThat(userPoint.point()).isGreaterThanOrEqualTo(5000L);
        });

        executorService.shutdown();
    }
}