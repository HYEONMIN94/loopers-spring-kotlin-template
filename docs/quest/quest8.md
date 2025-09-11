# 📝 Round 8 Quests

---

## 💻 Implementation Quest

> 이번에는 카프카 기반의 **이벤트 파이프라인**을 구현합니다.
각 이벤트를 외부 시스템과 적절하게 주고 받을 수 있는 구조를 직접 체험해봅니다.
>

<aside>
🎯

**Must-Have (이번 주에 무조건 가져가야 좋을 것-**무조건 ****하세요**)**

- Kafka
- Event Pipeline
- **At Least Once** producer
- **At Most Once** consumer

**Nice-To-Have (부가적으로 가져가면 좋을 것-**시간이 ****허락하면 ****꼭 ****해보세요**)**

- 상품별 유저 이벤트 집계 테이블 만들기
- 이외의 이벤트 기반 처리를 고민해보기
- 메세지 단건 처리 → 배치 처리도 알아보기
- DLQ 도 고민해보기
</aside>

### Source codes

- **kotlin**

[카프카 모듈 추가 by hubtwork · Pull Request #8 · Loopers-dev-lab/loopers-spring-kotlin-template](https://github.com/Loopers-dev-lab/loopers-spring-kotlin-template/pull/8)

- **java**

### 📋 과제 정보

**Kafka 기반 이벤트 파이프라인을 구현합니다.** (최소 기준)

- `commerce-api` → Kafka → `commerce-collector` 구조로 동작합니다.
- **Producer** 는 **At Least Once** 보장을 위해 이벤트를 반드시 발행합니다.
- **Consumer** 는 아래 3가지 역할을 담당하며 **멱등 처리**를 통해 중복 이벤트가 와도 최종적으로 한 번만 반영되도록 구현합니다.
    1. **캐시 무효화** : `StockAdjusted`, `LikeChanged` 발생 시 *상황에 따라* Redis 캐시 삭제
    2. **감사 로그(Audit Log)** : 모든 이벤트를 `event_log` 테이블에 저장
    3. **집계(Metrics)** : 좋아요 수 / 판매량 / 상세 페이지 조회 수 등을 `product_metrics` 테이블에 upsert

**토픽 설계** (예시)

- `catalog-events` (상품/재고/좋아요 이벤트, key=productId)
- `order-events` (주문/결제 이벤트, key=orderId)
- *각 세부 이벤트 별로 분리하고 싶다면, 분리해도 좋습니다.*

**Producer, Consumer 필수 처리**

- **Producer**
    - acks=all, idempotence=true 설정
- **Consumer**
    - **manual Ack** 처리
    - `event_handled(event_id PK)` (DB or Redis) 기반의 멱등 처리
    - `version` 또는 `updated_at` 기준으로 최신 이벤트만 반영
        - **Writing** 혹은 **`PR Message`** 에 어떻게 처리해야하는지, 우리 케이스에서 어떤 케이스가 있을지에 대해서 고민해보고 적어두기..

> *왜 이벤트 핸들링 테이블과 로그 테이블을 분리하는 걸까? 에 대해 고민해보자*
>

---

## ✅ Checklist

### 🎾 Producer

- [ ]  도메인(애플리케이션) 이벤트 설계
- [ ]  Producer 앱에서 도메인 이벤트 발행 (catalog-events, order-events, 등)
- [ ]  **PartitionKey** 기반의 이벤트 순서 보장
- [ ]  메세지 발행이 실패했을 경우에 대해 고민해보기

### ⚾ Consumer

- [ ]  Consumer 앱에서 3종 처리 (Audit Log / Cache Evict / Metrics 집계)
- [ ]  `event_handled` 테이블을 통한 멱등 처리 구현
- [ ]  재고 소진 시 상품 캐시 삭제
- [ ]  중복 메세지 재전송 테스트 → 최종 결과가 한 번만 반영되는지 확인

---

## ✍️ Technical Writing Quest

> 이번 주에 학습한 내용, 과제 진행을 되돌아보며
**"내가 어떤 판단을 하고 왜 그렇게 구현했는지"** 를 글로 정리해봅니다.
>
>
> **좋은 블로그 글은 내가 겪은 문제를, 타인도 공감할 수 있게 정리한 글입니다.**
>
> 이 글은 단순 과제가 아니라, **향후 이직에 도움이 될 수 있는 포트폴리오** 가 될 수 있어요.
>

### 📚 Technical Writing Guide

### ✅ 작성 기준

| 항목 | 설명 |
| --- | --- |
| **형식** | 블로그 |
| **길이** | 제한 없음, 단 꼭 **1줄 요약 (TL;DR)** 을 포함해 주세요 |
| **포인트** | “무엇을 했다” 보다 **“왜 그렇게 판단했는가”** 중심 |
| **예시 포함** | 코드 비교, 흐름도, 리팩토링 전후 예시 등 자유롭게 |
| **톤** | 실력은 보이지만, 자만하지 않고, **고민이 읽히는 글**예: “처음엔 mock으로 충분하다고 생각했지만, 나중에 fake로 교체하게 된 이유는…” |

---

### ✨ 좋은 톤은 이런 느낌이에요

> 내가 겪은 실전적 고민을 다른 개발자도 공감할 수 있게 풀어내자
>

| 특징 | 예시 |
| --- | --- |
| 🤔 내 언어로 설명한 개념 | Stub과 Mock의 차이를 이번 주문 테스트에서 처음 실감했다 |
| 💭 판단 흐름이 드러나는 글 | 처음엔 도메인을 나누지 않았는데, 테스트가 어려워지며 분리했다 |
| 📐 정보 나열보다 인사이트 중심 | 테스트는 작성했지만, 구조는 만족스럽지 않다. 다음엔… |

### ❌ 피해야 할 스타일

| 예시 | 이유 |
| --- | --- |
| 많이 부족했고, 반성합니다… | 회고가 아니라 일기처럼 보입니다 |
| Stub은 응답을 지정하고… | 내 생각이 아닌 요약문처럼 보입니다 |
| 테스트가 진리다 | 너무 단정적이거나 오만해 보입니다 |

### 🎯 Feature Suggestions

- Kafka.. 왜 쓸까? 꼭 필요할까?
- Kafka는 기본적으로 At Least Once인데, Consumer 멱등 처리가 없으면 무슨 일이 벌어질까?
- 캐시 무효화와 집계 로직을 한 컨슈머에서 처리하는 것과, 그룹을 나누는 것 중 어떤 차이가 있을까?
- 이벤트 순서 보장을 위해 key를 aggregateId로 두었는데, 만약 랜덤 키를 썼다면 어떤 문제가 생겼을까?
- 멱등 처리를 DB 테이블로 할 때와 Redis로 할 때의 차이점은 무엇일까?