# 동시성 제어 방식에 대한 분석 및 보고서 작성

## `synchronized`와 `ReentrantLock`
`synchronized`와 `ReentrantLock`은 Java에서 동시성 제어(Concurrency Control)를 위한 주요 메커니즘이다.

### 1. 기본 개념
- **synchronized**
  - Java 키워드로, 메서드나 블록에 적용해 동기화 처리.
  - 자바 가상 머신(JVM) 수준에서 관리됨.
  - 내부적으로 모니터 락(Monitor Lock)을 사용.

- **ReentrantLock**
  - java.util.concurrent.locks 패키지의 클래스.
  - 생성자의 매개변수(`fair`)에 `true`를 주면 lock이 풀렸을 때 가장 오래 기다린 쓰레드가 lock을 획득.
 
### 2. 사용법
- **synchronized**
```java
public synchronized void method() {
    // 동기화된 코드
}

public void method() {
    synchronized (this) {
        // 동기화된 블록
    }
}
```

- **ReentrantLock**
```java
import java.util.concurrent.locks.ReentrantLock;

ReentrantLock lock = new ReentrantLock();

public void method() {
    lock.lock();
    try {
        // 동기화된 코드
    } finally {
        lock.unlock();  // 반드시 해제해야 함
    }
}
  ```

### 3. 차이점
|비교 항목|synchronized|ReentrantLock|
|-----|----------|----------|
|락 관리|JVM이 자동 관리|	수동으로 락 해제 필요|
|성능|단순 동기화, 성능 개선 적음|	성능이 더 우수 (경쟁 심할 때)|
|재진입 가능성	|재진입 가능	|재진입 가능|
|공정성(Fairness)	|지원하지 않음	|생성 시 공정성 설정 가능|
|대기 시간 제한	|지원하지 않음	|`tryLock(timeout)` 사용 가능|
|조건 대기 지원|	조건 변수 지원하지 않음|`Condition` 객체 지원|
|예외 처리	|락 해제 자동 보장 (`finally` 불필요)	|반드시 `finally`로 락 해제 필요|

### 4. 정리
- **synchronized**: 간단하고 유지보수가 쉬운 기본 동기화 방식으로, 단순한 동기화 작업에 적합하다.
- **ReentrantLock**: 더 복잡한 동기화 제어(예: 조건 변수, 타임아웃, 공정성 등)가 필요한 경우 유리하다.

---

## `ConcurrentHashMap` + `ReentrantLock`
### 1. ConcurrentHashMap 사용 이유
- **동시성 지원**: `ConcurrentHashMap`은 여러 스레드에서 동시 읽기/쓰기를 지원한다.
- **Key-Value 저장**: 사용자 ID별로 `ReentrantLock` 객체를 관리하기 위해 사용한다.
- **Lock 생성 및 관리**: `computeIfAbsent` 메서드를 사용하여 특정 사용자 ID에 대한 `ReentrantLock`을 동적으로 생성 및 저장한다.

### 2. ReentrantLock 사용 이유
- **명시적 잠금**: 특정 사용자 ID에 대해 명시적으로 잠금을 설정하고 해제할 수 있다.
- **공정성 및 재진입**: `ReentrantLock`은 재진입을 지원하고 공정한 잠금 정책을 설정할 수 있다.
- **동기화 범위 제어**: Java `synchronized` 키워드보다 더 유연하게 잠금 범위를 관리할 수 있다.

### 3. 함께 사용한 이유
**유저별 독립적 동기화**
- 각 사용자 ID별로 별도의 `ReentrantLock` 인스턴스를 관리하기 때문에 사용자 간 동시 처리가 가능한다.
- 여러 사용자가 동시에 포인트를 사용하더라도 서로 다른 사용자에 대해 잠금을 따로 설정한다.

---

## 결론
- `ReentrantLock`을 통해 **동시성을 제어**한다.
- `ReentrantLock(boolean fair)`에 매개변수를 `true`로 줘서 **순차적으로 스레드를 처리**한다.
- `ConcurrentHashMap`을 이용하여 동시성을 **사용자 ID 별로 제어** 할 수 있게 한다.
