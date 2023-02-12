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