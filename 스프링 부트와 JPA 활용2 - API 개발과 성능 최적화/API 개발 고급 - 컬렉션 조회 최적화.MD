## 주문 조회(OneToMany 관계 조회)

### 주문 조회 V1 : 엔티티 직접 노출

```
    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(orderItem -> orderItem.getItem().getName());
        }
        return  all;
    }
```
- orderItem , item 관계를 직접 초기화하면 Hibernate5Module 설정에 의해 엔티티를 JSON으로
생성

- 양방향 연관관계면 무한 루프에 걸리지 않게 한곳에 @JsonIgnore 를 추가해야 함

- 엔티티를 직접 노출하므로 좋은 방법이 아님

<br><Br>

### 주문 조회 V2 : 엔티티를 DTO로 변환

```
    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    @Data
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // orderItem도 DTO로 변환 후 리턴!

        public OrderDto(Order o) {
            this.orderId = o.getId();
            this.name = o.getMember().getName();
            this.orderDate = o.getOrderDate();
            this.orderStatus = o.getStatus();
            this.address = o.getDelivery().getAddress();
            this.orderItems = o.getOrderItems().stream()    // orderItem도 엔티티로 리턴 x(dto로 변환)
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto{
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
```
- 지연로딩으로 너무 많은 SQL 실행됨
- SQL 실행 수
    - order 1번
    - member, address N번(order 조회 수 만큼)
    - orderItem N번(order 조회 수 만큼)
    - item(orderItem 조회 수 만큼)

> 참고: 지연로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고, 없으면 SQL 실행. 따라서 영속성 컨텍스트에서 이미 로딩된 회원엔티티를 추가로 조회하면 SQL 실행 X

<BR><bR>

### 주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화

```
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem(); // 데이터 뻥튀기 됨
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    ✅ OrderRepository - 추가 코드

    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class
        ).
                getResultList();
    }

```
- fetch join으로 SQL이 1번만 실행됨
- distinct를 사용한 이유는 1대다 조인이 있으므로 데이터베이스 ROW가 증가. 그 결과 같은 order 엔티티의 조회수도 증가함. JPA의 distinct는 SQL에 distinct를 추가하고, 더해서 같은 엔티티가 조회되면, 애플리케이션에서 중복 걸러 줌
    - 이 예에서 order가 컬렉션 페치조인때문에 중복 조회되는 것을 막아줌
- 단점: **페이징 불가능** (1대다 페치조인 사용시!)

<br>

> 참고: 컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 하이버네이트는 경고 로그를 남기면서 모든
데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다). 자세한 내용은 자바 ORM 표준
JPA 프로그래밍의 페치 조인 부분을 참고하자.


> 참고: 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가
부정합하게 조회될 수 있다. 자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자.

<br><br>

### 주문 조회 V3.1 : 엔티티를 DTO로 변환 - 페이징과 한계 돌파

#### ✅ 페이징과 한계 돌파
- 컬렉션을 페치 조인하면 페이징이 불가능!
    - 컬렉션을 페치 조인하면 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가
    - 일대다에서 일(1)을 기준으로 페이징을 하는것이 목적이지만, 데이터는 다(N)를 기준으로 row가 생성됨
    - Order를 기준으로 페이징 하고 싶은데, 다(N)인 OrderItem을 조인하면 OrderItem이 기준이 돼버림
- 이 경우 하이버네이트는 경고 로그를 남기고 모든 DB 데이터를 읽어서 메모리에서 페이징 시도 

    ➡️ 최악의 경우 장애로 이어짐

<BR>

#### 💡 한계 돌파

그러면 페이징 + 컬렉션 엔티티를 함께 조회하려면?

- 먼저 ToOne(OneToOne, ManyToOne) 관계를 모두 페치조인 함! ToOne 관계는 row수를
증가시키지 않으므로 페이징 쿼리에 영향을 주지 않음
- 컬렉션은 지연로딩으로 조회
- **지연로딩 성능 최적화를 위해 ``` hibernate.default_batch_fetch_size``` , ```@BatchSize```를 적용!**
    - hibernate.default_batch_fetch_size: 글로벌 설정
    - @BatchSize: 개별 최적화
    - 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회함

```
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
            @RequestParam(value="offset", defaultValue = "0") int offset,
            @RequestParam(value="limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // ToOne만 페치조인(페이징 가능)
        
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    ✅ OrderRepository - 추가 코드
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery("select o from Order o " +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    ✅ 최적화 옵션
    spring:
        jpa:
            properties:
                hibernate:
                    default_batch_fetch_size: 1000

    개별로 설정하려면 @BatchSize 를 적용하면 된다. (컬렉션은 컬렉션 필드에, 엔티티는 엔티티 클래스에
적용)

```

- **장점**
    - 쿼리 호출 수가 ```1 + N``` ➡️ ```1 + 1``` 로 최적화 됨
    - 조인보다 DB 데이터 전송량이 최적화 됨

    (Order와 OrderItem을 조인하면 Order가 OrderItem 만큼 중복해서 조회됨. 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없음.)
    - 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소
    - 컬렉션 페치 조인은 페이징이 불가능하지만 이 방법은 페이징 가능

<br>

#### ⭐ **결론**

ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않음.

따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄여 해결하고, 나머지는 batch size로 최적화!

<br>

> 참고: default_batch_fetch_size 의 크기는 적당한 사이즈를 골라야 하는데, 100~1000 사이를
선택하는 것을 권장한다. 이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를
1000으로 제한하기도 한다. 1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로 DB
에 순간 부하가 증가할 수 있다. 하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야
하므로 메모리 사용량이 같다. 1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든
순간 부하를 어디까지 견딜 수 있는지로 결정하면 된다.

<br><Br>

### 주문 조회 V4 : JPA에서 DTO 직접 조회

```
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    ✅ OrderRepository - 추가 코드

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // query 1번 -> N개

        result.forEach(o -> {
             List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // query N개 나감
             o.setOrderItems(orderItems);
        });
        return result;
    }

    // ToOne 관계 find

    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        "from Order o " +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    // ToMany 관계 find

    private List<OrderItemQueryDto> findOrderItems(long orderId) {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id,i.name,oi.orderPrice, oi.count )" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

```

- 쿼리: 루트 1번, 컬렉션 N번 실행(N+1문제 발생)
- ToOne(N:1, 1:1) 관계들을 먼저 조회하고, ToMany(1:N) 관계는 각각 별도로 처리
    - 이 방식을 채택한 이유
    - 1️⃣ ToOne 관계는 조인해도 데이터 row 증가 x
    - 2️⃣ ToMany(1:N) 관계는 조인하면 row 증가
- row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화하기 쉬우므로 한번에 조회, ToMany 관계는 최적화 하기 어려우므로 ```findOrderItems()``` 같은 별도의 메서드로 조회

<br><Br>

### 주문 조회 V5 : JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화

```
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5(){
        return orderQueryRepository.findOrderQueryDto_optimization();

    }

    ✅ OrderRepository - 추가 코드

    public List<OrderQueryDto> findOrderQueryDto_optimization() {

        List<OrderQueryDto> result = findOrders(); // query 1번 : order find

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        // orderId로 orderItemList를 가져와 order의 orderItem에 set
        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id,i.name,oi.orderPrice, oi.count )" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList(); // query 1번 : orderItem find

        // 쿼리 한번 날리고 메모리에서 맵으로 orderItem 매칭(order와 orderItem) = 쿼리 총 2번 나감
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        // orderId를 key로 하고 orderItemList를 value로 하는 map 생성
        return orderItemMap;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList()); // orderId 리스트 생성
        return orderIds;
    }

```
- query: 루트 1번, 컬렉션 1번
- ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem을 한꺼번에 조회
- MAP을 사용해서 매칭성능 향상(O(1))

<BR><bR>

### 주문 조회 V6: JPA에서 DTO로 직접 조회, 플랫 데이터 최적화

```
@GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findOrderQueryDto_flat();

        // OrderFlatDto -> OrderQueryDto
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }
```
- queyr : 1번
- 단점
    - 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5보다 더 느릴 수 있음
    - 애플리케이션에서 추가 작업이 큼
    - 페이지 불가능(ORDER를 기준으로 할때)


<BR><bR>


