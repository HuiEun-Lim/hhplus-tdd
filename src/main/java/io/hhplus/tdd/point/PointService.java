package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint charge(Long userId, Long amount) {

        if (amount == 0) {
            throw new IllegalArgumentException("충전할 포인트가 없습니다. (충전 포인트 0)");
        }

        if (amount < 0) {
            throw new IllegalArgumentException("충전할 포인트가 음수일 수 없습니다.");
        }

        if (amount < 1000) {
            throw new IllegalArgumentException("포인트 1,000 미만은 충전할 수 없습니다.");
        }

        if (amount % 1000 != 0) {
            throw new IllegalArgumentException("포인트는 1,000 단위로 충전할 수 있습니다.");
        }

        UserPoint origin = userPointTable.selectById(userId);

        if (origin.point() + amount > 1000000) {
            throw new IllegalArgumentException("포인트는 1,000,000을 초과하여 충전할 수 없습니다.");
        }

        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(userId, origin.point() + amount);
    }

    public UserPoint use(long userId, Long amount) {

        if(amount <= 0) {
            throw new IllegalArgumentException("사용 포인트가 0 이하일 수 없습니다.");
        }


        UserPoint origin = userPointTable.selectById(userId);

        if (origin.point() < amount) {
            throw new IllegalArgumentException("충전 포인트보다 사용 포인트가 더 많습니다.");
        }

        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(userId, origin.point() - amount);
    }

    public UserPoint point(long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> history(long userId) {

        List<PointHistory> pointHistList = pointHistoryTable.selectAllByUserId(userId);

        if (pointHistList.isEmpty()) {
            throw new IllegalArgumentException("사용자의 포인트 포인트 내역이 없습니다.");
        }

        return pointHistList;
    }
}
