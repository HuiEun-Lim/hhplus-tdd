package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @DisplayName("충전 할 포인트가 0이면 오류가 발생한다.")
    @Test
    void chargePointZero() {

        // given
        final long amount = 0;
        final long userId = 1;

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트가 없습니다. (충전 포인트 0)");
    }

    @DisplayName("충전 할 포인트가 음수면 오류가 발생한다.")
    @Test
    void chargePointLessZero() {

        // given
        final long amount = -1;
        final long userId = 1;

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전할 포인트가 음수일 수 없습니다.");
    }

    @DisplayName("포인트는 1,000 미만은 충전할 수 없다.")
    @Test
    void chargePointUnderThousand() {

        // given
        final long amount = 999;
        final long userId = 1;

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 1,000 미만은 충전할 수 없습니다.");
    }

    @DisplayName("포인트는 1,000 단위가 아니면 충전할 수 없다.")
    @Test
    void chargePointNotByThousand() {

        // given
        final long amount = 2999;
        final long userId = 1;

        // when
        // then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트는 1,000 단위로 충전할 수 있습니다.");
    }

    @DisplayName("포인트의 총합이 1,000,000 초과이면 예외가 발생한다.")
    @Test
    void chargePointOverMax() {

        // given
        final long amount = 1000000;
        final long userId = 1;

        // when
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 1, 100000));

        // then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트는 1,000,000을 초과하여 충전할 수 없습니다.");
    }

    @DisplayName("포인트를 충전한다.")
    @Test
    void chargeNewPoint() {

        // given
        final long amount = 10000;
        final long userId = 1;

        // when
        when(userPointTable.insertOrUpdate(userId, amount))
                .thenReturn(new UserPoint(userId, amount, 100000));

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 0, 100000));

        UserPoint userPoint = pointService.charge(userId, amount);

        // then
        assertThat(userPoint.point()).isEqualTo(10000);
    }

    @DisplayName("기존 포인트에 새 포인트를 충전한다.")
    @Test
    void chargeOriginPointPlusNewPoint() {

        // given
        final long amount = 10000;
        final long originAmount = 20000;
        final long userId = 1;

        // when
        when(userPointTable.insertOrUpdate(userId, amount + originAmount))
                .thenReturn(new UserPoint(userId, amount + originAmount, 100000));

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, originAmount, 100000));

        UserPoint userPoint = pointService.charge(userId, amount);

        // then
        assertThat(userPoint.point()).isEqualTo(30000);
    }

    @DisplayName("사용 포인트가 0 이하일 수 없다.")
    @Test
    void usePointZeroOrLess() {

        // given
        final long amount = 0;
        final long userId = 1;

        // when
        // then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 포인트가 0 이하일 수 없습니다.");
    }

    @DisplayName("충전 포인트보다 많은 포인트를 사용할 수 없다.")
    @Test
    void usePointMoreAvailable() {

        // given
        final long amount = 1001;
        final long userId = 1;

        // when
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 1000, 100000));

        // then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 포인트보다 사용 포인트가 더 많습니다.");
    }

    @DisplayName("포인트를 사용한다.")
    @Test
    void usePoint() {

        // given
        final long amount = 10000;
        final long originAmount = 20000;
        final long userId = 1;

        // when
        when(userPointTable.insertOrUpdate(userId, amount + originAmount))
                .thenReturn(new UserPoint(userId, amount + originAmount, 100000));

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, originAmount, 100000));

        UserPoint userPoint = pointService.charge(userId, amount);

        // then
        assertThat(userPoint.point()).isEqualTo(30000);
    }

    @DisplayName("사용자 포인트를 조회한다.")
    @Test
    void selectUserPoint() {

        // given
        final long userId = 1;

        // when
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 10000, 100000));

        UserPoint userPoint = pointService.point(userId);

        // then
        assertThat(userPoint.id()).isEqualTo(userId);
        assertThat(userPoint.point()).isEqualTo(10000);
        assertThat(userPoint.updateMillis()).isEqualTo(100000);
    }

    @DisplayName("충전한 내역이 없는 사용자의 포인트 내역을 조회한다.")
    @Test
    void selectNoUserPointHistory() {

        // given
        final long userId = 1;

        // when
        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(Collections.emptyList());

        // then
        assertThatThrownBy(() -> pointService.history(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자의 포인트 포인트 내역이 없습니다.");

    }

    @DisplayName("포인트 내역을 조회한다.")
    @Test
    void selectPointHistory() {

        // given
        final long userId = 1;

        PointHistory pointHistory1 = new PointHistory(1, userId, 10000, TransactionType.CHARGE, 1000);
        PointHistory pointHistory2 = new PointHistory(2, userId, 5000, TransactionType.USE, 1000);
        PointHistory pointHistory3 = new PointHistory(3, userId, 2000, TransactionType.USE, 1000);

        List<PointHistory> mockList = List.of(pointHistory1, pointHistory2, pointHistory3);

        // when
        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(mockList);

        List<PointHistory> historyList = pointService.history(userId);

        // then
        assertThat(historyList).hasSize(3);
        assertThat(historyList.get(0).amount()).isEqualTo(10000);
        assertThat(historyList.get(1).amount()).isEqualTo(5000);
        assertThat(historyList.get(2).amount()).isEqualTo(2000);
        verify(pointHistoryTable).selectAllByUserId(userId);
    }
}