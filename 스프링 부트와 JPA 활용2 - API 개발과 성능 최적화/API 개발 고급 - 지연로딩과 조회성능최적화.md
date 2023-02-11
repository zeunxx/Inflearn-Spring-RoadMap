## 주문 조회 (XtoOne 관계 조회)
### 간단한 주문 조회 V1 : 엔티티 직접 노출
```
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        // -> 양방향 연관관계 : 무한 루프 돌게 됨 = @JsonIgnore로 양방향 조회 끊어줘야 함

        for (Order order : all) {
            order.getMember().getName(); // 프록시 초기화 위해 -> Lazy 강제 초기화
            order.getDelivery().getStatus();
        }

        return all;
    }
```
- 엔티티를 직접 노출하는 것은 좋지 않음
- order -> member 와 order -> address 는 지연 로딩, 따라서 실제 엔티티 대신에 프록시 존재
    - jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모름 = 예외 발생 

<Br>

> 엔티티를 직접 노출할때는 양방향 연관관계가 걸린 곳은 꼭! 한 곳을 ```@JsonIgnore```처리 해야한다! 안그러면 양쪽을 서로 호출하면서 무한 루프 발생!

> but 엔티티를 직접 노출하지 말기! DTO로 변환해서 반환하는 것이 좋음

> 지연로딩(LAZY)를 피하기 위해 즉시로딩(EAGER)으로 설정하면 안됨! 즉시로딩대문에 연관관계가 필요없는 경우에도 데이터를 항상 조회해서 성능 문제 발생 가능! 즉시로딩으로 설정하면 성능 튜닝이 매우 어려워짐. 항상 지연로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 페치조인(fetch join)을 사용해라!!!

<br><Br>

### 간단한 주문 조회 V2 : 엔티티를 DTO로 변환

```
@GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());

        // 2번 loop 돔 -> 각각 member, delivery 조회 = 너무 많은 쿼리 나감
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order o) {
            this.orderId = o.getId();
            this.name = o.getMember().getName(); // Lazy 초기화 (프록시 초기화)
            this.orderStatus = o.getStatus();
            this.orderDate = o.getOrderDate();
            this.address = o.getDelivery().getAddress(); // Lazy 초기화 (프록시 초기화)
        }
    }

```
- 엔티티를 DTO로 변형하는 일반적인 방법
- 쿼리가 총 1+N+N번 실행됨(V1과 쿼리수 결과는 같음)
- 엔티티를 DTO로 변환하는 일반적인 방법이다.
    - 쿼리가 총 1 + N + N번 실행된다. (v1과 쿼리수 결과는 같음)
    - order 조회 1번(order 조회 결과 수가 N이 됨)
    - order -> member 지연 로딩 조회 N 번
    - order -> delivery 지연 로딩 조회 N 번
- 예) order의 결과가 4개면 최악의 경우 1 + 4 + 4번 실행됨(최악의 경우)
    - 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략

<BR><bR>

### 간단한 주문 조회 V3 : 엔티티를 DTO로 변환 - 페치 조인 최적화

```
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }


    ✅ OrderRepository - 추가 코드
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
        "select o from Order o" +
        " join fetch o.member m" +
        " join fetch o.delivery d", Order.class)
        .getResultList();
    }
```

- fetch join으로 쿼리 1번만 호출
- fetch join으로 order->member, order->delivery는 이미 조회된 상태이므로 지연로딩 x


<br><Br>

### 간단한 주문 조회 V4 : JPA에서 DTO로 바로 조회


```
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }


    ✅ OrderRepository - 추가 코드 (오로지 화면을 위한 코드)
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order o " +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }

```
- 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
- new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
- SELECT 절에서 원하는 데이터를 직접 선택하므로 DB -> 애플레키이션 네트워크 용량 최적화(BUT 생각보다 미비)
- 리포지토리 재상요성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점

<BR>

### ✍️ 정리

엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두 가지 방법은 각각 장단점이 존재!

둘중 상황에 따라서 더 나은 방법을 선택하면 됨

엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해짐

**쿼리 방식 선택 권장 순서**
1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접
사용한다.