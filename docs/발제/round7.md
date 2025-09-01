## 🧭 루프팩 BE L2 - Round 7

> 느슨하게, 유연하게, 확장 가능하게!
>
>
> 애플리케이션 이벤트 기반으로 **무거운 동기 연산을 분리**하고, **복잡한 도메인 흐름을 제어**합니다.
>

<aside>
🎯

**Summary**

</aside>

지금까지 우리는 재고 차감, 포인트 차감, 쿠폰 사용, 결제 처리 등 **모든 흐름을 하나의 트랜잭션 안에서 처리**해왔습니다. 하지만 이 방식은 트랜잭션이 커지고, 실패 포인트가 많아지며, 시스템의 결합도도 높아지는 단점이 있습니다.

이번 라운드에서는 애플리케이션 이벤트를 활용해 **유스케이스의 후속 흐름을 분리**하고, **비동기 트랜잭션 흐름**을 설계하는 방법을 학습합니다.

<aside>
📌

**Keywords**

</aside>

- 애플리케이션 이벤트 (ApplicationEventPublisher)
- 트랜잭션 분리 & 도메인 decoupling
- 후속 처리 비동기화
- 사후 정합성 처리 전략

<aside>
🧠

**Learning**

</aside>

## 🪓 문제 분석 - 무거워진 트랜잭션과 집중된 관심사

지금까지 우리는 **아래 흐름을 한 호흡에 처리**하도록 했습니다.

```json
createOrder()
 ├── 재고 차감
 ├── 포인트 차감
 ├── 쿠폰 사용
 ├── 결제 요청
 └── 주문 저장
```

> ✅ 주문 등록
✅ 상품 재고 차감
✅ 할인 처리 (쿠폰 기반의 사용)
✅ 결제 처리 (포인트, 카드 등)
✅ 데이터 플랫폼에 주문 정보 전송
>

모두 하나의 트랜잭션 범위 내에서 처리하려다 보니, 아래 문제가 발생하기 시작했어요.

- 하나의 과정이라도 실패하면 전체 롤백되므로 재시도가 불가능
- 외부 API(PG, 데이터 플랫폼) 요청이 실패하면 주문도 실패
- 트랜잭션이 길어질수록 DB 락 유지 시간 증가로 인한 전반적인 시스템 성능 저하

| **문제점** | **설명** |
| --- | --- |
| 🧨 실패 전파 | PG API가 느려지거나 실패하면 주문 전체가 롤백됩니다 |
| 🧱 높은 결합도 | User, Product, Coupon, Payment 도메인이 모두 한 흐름에 엮입니다 |
| 🔁 재시도 불가 | 롤백은 가능하지만, 어디까지 성공했는지 불확실하여 복구가 어렵습니다 |
| 🐌 성능 저하 | 트랜잭션이 길어질수록 DB 락이 길게 유지되어 TPS가 하락합니다 |

### 🍰 흐름을 나누는 사고

> 이 문제를 해결하기 위한 핵심 전략은 **트랜잭션을 나누는 것**입니다.
>
>
> 모든 처리를 동시에 하지 말고, **지금 꼭 해야 하는 것**과 **조금 나중에 해도 되는 것**을 분리합니다.
>

| **구분** | **하는 일** | **트랜잭션 경계** |
| --- | --- | --- |
| ✅ 핵심 트랜잭션 | 주문 생성, 금액 계산, 유효성 검증 | 반드시 커밋 보장 |
| ✉ 후속 트랜잭션 | 쿠폰 차감, 포인트 적립 기록, PG 호출 | 커밋 이후 실행 |
- **핵심 로직**은 시스템의 **정합성을 보장**
- **후속 로직**은 **부가적인 비즈니스 확장**

예를 들어, PG 장애가 발생해도 주문은 저장되어야 합니다. 이것이 바로 **트랜잭션의 분리**가 필요한 이유입니다.

### ✉️ Command vs Event

> 트랜잭션을 나누기 위한 도구로 이벤트를 사용합니다.
이벤트는 특정한 기술이 아니라 **흐름의 소유권을 위임하는 메세지 전달 방식**입니다.
커맨드는 **하기 위한 정보**를 담고, 이벤트는 **발생한 그 순간의 정보**를 담습니다.
>

| **항목** | **Command** | **Event** |
| --- | --- | --- |
| 의미 | “~을 해라” (명령) | “~이 발생했다” (사실 통지) |
| 컨텍스트 | 요청 (request) | 결과 (result) |
| 주체 | 지목된 핸들러에 의해 실행 | 후속 핸들러가 알아서 반응 |
| 흐름 제어 | 호출자가 제어 | 호출자가 제어하지 않음 |

---

## ➗ Spring Application Event

<aside>
💡

**TL;DR**
Spring 은 `ApplicationEvent` 라는 개념을 통해 애플리케이션 내부에서 이벤트 기반의 흐름 제어를 제공합니다.

</aside>

### 🧠 ApplicationEvent

> Spring에서는 내부 컴포넌트끼리 메시지를 주고받을 수 있는 **이벤트 기반 구조**를 제공합니다.
>
>
> 이를 통해 **서비스 간 직접 호출 없이**, 메시지를 던지듯 통신할 수 있습니다.
>

```java
[주문 생성 서비스]
   └── (이벤트 발행) → [쿠폰 처리 서비스], [포인트 처리 서비스] …
```

우리가 사용할 도구들은 아래와 같습니다.

| **구성 요소** | **설명** |
| --- | --- |
| ApplicationEventPublisher | 이벤트를 발행하는 역할
이벤트 발행 시 스프링 내부적으로 @EventListener를 호출함 |
| @EventListener | 이벤트에 대한 처리를 수행하는 역할 |
| @TransactionalEventListener | 트랜잭션이 커밋된 뒤에만 이벤트가 처리되도록 보장 |
| phase = AFTER_COMMIT | 트랜잭션이 성공적으로 커밋된 경우에만 이벤트가 동작 |
| @Async | 이벤트 리스너를 **비동기(별도 스레드)**로 실행함 |

### ✍️ Why use ApplicationEvent?

| **비교 항목** | **설명** |
| --- | --- |
| 단순 호출 vs 이벤트 | 직접 호출은 강하게 결합됨 (OrderService → CouponService 직접 호출)
ApplicationEvent는 흐름을 **통지**만 하고, **처리는 외부에 위임** |
| 유연한 확장 | 새로운 후속 로직이 필요하면, 이벤트 리스너만 추가하면 됨 |
| 추가 기술 의존 없음 | Kafka 같은 메시지 브로커 없이도 **Spring 단에서 구현 가능** |
| 커밋 이후 보장 | @TransactionalEventListener(phase = AFTER_COMMIT)로 안전하게 커밋 후 처리 가능 |

### Java 코드 예시

`OrderApplicationService.java`

```java
// 1. 주문 저장 후 이벤트 발행
@Transactional
void createOrder(...) {
		Order order = orderRepository.save(...);
		eventPublisher.publishEvent(OrderCreatedEvent.from(order));
}

```

`OrderEventHandler.java`

```java
// 2. 커밋 이후 비동기로 후속 로직 처리
@Component
class OrderEventHandler {
		..
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    void handle(OrderCreatedEvent event) {
        if (event.couponId != null) {
		        couponService.useCoupon(event.couponId);
        }
        pointService.record(event.amount());
        pgClient.requestPayment(PaymentCommand.from(event));
    }
}
```

### Kotlin 코드 예시

`OrderApplicationService.kt`

```kotlin
// 1. 주문 저장 후 이벤트 발행
@Transactional
fun createOrder(...) {
    val order = orderRepository.save(...)
    eventPublisher.publishEvent(OrderCreatedEvent.from(order))
}
```

`OrderEventHandler.kt`

```kotlin
// 2. 커밋 이후 비동기로 후속 로직 처리
@Component
class OrderEventHandler {
		..
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun handle(event: OrderCreatedEvent) {
        event.couponId?.let { couponService.useCoupon(it) }
        pointService.record(event.amount)
        pgClient.requestPayment(PaymentCommand.from(event))
    }
}
```

---

## 💣 오해 - Silver Bullet ?

> @Async, 이벤트 기반 구조는 **확장성**과 **응답 속도 면**에서 매우 유리합니다.
>
>
> 하지만 그만큼 **제어 흐름이 눈에 보이지 않고**, **실패 감지가 어렵고**, **운영 상 이슈 대응이 어려울 수 있습니다.**
>

### 🔥 발생할 수 있는 문제

| **리스크** | **설명** | **대응 전략** |
| --- | --- | --- |
| ❌ 예외 은닉 | 이벤트 리스너 내 실패는 사용자에게 노출되지 않음 | 로그 적재, 모니터링, 실패 이벤트 보관 필요 |
| ❌ 순서 보장 어려움 | 이벤트 리스너는 병렬 실행될 수 있음 | 업무적으로 순서 의존이 없는 흐름만 분리 |
| ❌ 중복 실행 | 트랜잭션 재시도나 이벤트 중복 발행 시 여러 번 실행될 수 있음 | idempotency 처리: 이벤트 ID 기준 중복 차단 |
| ❌ 장애 누락 | 슬랙 알림, 메일 전송 등 외부 연동 실패 시 조용히 무시될 수 있음 | 예외 발생 시 DLQ (Dead Letter Queue) 등 보완 구조 필요 |

### **☂ 그래서 실무에서는 이런 고민들을 해요**

- 실패한 이벤트를 로그만 남겨도 괜찮을까?
- 재시도는 어떻게 할까? Spring Retry? Scheduled 재처리?
- 중요 이벤트는 **이벤트 저장소 (Outbox)**에 적재 후 처리하는 게 좋지 않을까?
- 정말 중요한 처리(PG 콜백 등)는 결국 메시지 브로커(Kafka 등)로 보내야 하지 않을까?

---

### **🌾** Summary

> 정리하면, 우리는 왜 ApplicationEvent 기반 트랜잭션 분리를 도입했을까요?
>

| **구분** | **ApplicationEvent (Spring)** |
| --- | --- |
| 전송 범위 | 애플리케이션 내부 (단일 JVM) |
| 보존 | 없음 (메모리 기반) |
| 신뢰성 | 장애 발생 시 손실 가능 |
| 속도 | 매우 빠름 |
| 적절한 사용 | 내부 후속 처리 흐름 |

| **목적** | **설명** |
| --- | --- |
| 🎯 트랜잭션 최소화 | 핵심 흐름만 빠르게 처리하고, 후속 로직은 별도로 |
| 🎯 결합도 감소 | OrderService는 쿠폰, 포인트, 결제 흐름을 몰라도 됨 |
| 🎯 장애 격리 | 외부 시스템(PG) 실패가 전체 서비스에 영향을 주지 않음 |
| 🎯 유연한 확장 | 이벤트 구독만으로 기능 확장 가능 → 신규 알림, 적립도 쉽게 추가 가능 |

<aside>
📚

**References**

</aside>

| 구분 | 링크 |
| --- | --- |
| 🔍 Event vs Command | [littlemobs - Event 와 Command 의 차이점 쉽게 이해하기](https://littlemobs.com/blog/difference-between-event-and-command/) |
| ⚙ Spring Application Events | [Spring Application Events](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/application-events.html) |
| 📖 Event in Spring | [Baeldung - Event in Spring](https://www.baeldung.com/spring-events) |

<aside>
🌟

**Next Week Preview**

</aside>

> **만약 다른 시스템과 이벤트를 주고 받으려면 어떻게 해야할까요?**
>
>
>
> 이번 주차에는 핵심 로직은 빠르게 끝내고, 후속 로직은 나중에 처리하자는 구조적 분리를 배웠습니다.
> 하지만 이 흐름은 여전히 **단일 JVM 안에서만 동작**합니다.
>
> 그럼 여러분은 아래 질문에 대해 어떻게 답변할 수 있을까요?
>
> - **이벤트를 애플리케이션 밖으로 발행해서, 조회 시스템이나 추천 시스템을 만든다면?**
> - **이벤트가 여러 팀/서브시스템으로 퍼져야 한다면?**
>
> 이런 질문에 답하기 위해 **Kafka를 도입하고, 외부 메시징 기반의 시스템 설계로 확장해 나갑니다.**
>