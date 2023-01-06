### 주문 검색 기능 개발
#### JPA에서 **동적 쿼리**를 어떻게 해결해야 하는가?
<bR>
<img width="429" alt="image" src="https://user-images.githubusercontent.com/81572478/210948381-e7a720b8-220a-456a-8f30-7e74213adf45.png">

<BR>

#### 1. 주문 검색시 status와 name 필터 모두 사용
```
    /**
     * 검색 기능
     */
    public List<Order> findAll(OrderSearch orderSearch){
        return em.createQuery("select o from Order o join o.member m"+
                        " where o.status = :status "+
                        " and m.name like :name",Order.class)
                .setParameter("status",orderSearch.getOrderStatus()) // 파라미터 바인딩
                .setParameter("name",orderSearch.getMemberName())
                .setMaxResults(1000) // 최대 1000개만 조회
                .getResultList();
        // order를 조회하고 order와 member(order와 연관됨)를 join
        // 위 코드에서 status나 name이 조건이 없을시(null)일때 필터링 하지 않고 다 가져와야함(== 동적쿼리)

        return em.createQuery("select o from Order o join o.member m",
                .setMaxResults(1000) // 최대 1000개만 조회
                .getResultList();

    }
        // 위 코드에서 status나 name이 조건이 없을시(null)일때 필터링 하지 않고 다 가져와야함(== 동적쿼리)
```

#### 2. 검색 조건이 없는 경우 동적 쿼리 사용

1️⃣ 동적으로 jpql문 조건에 따라 작성
```
    /**
     * 검색 기능
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) { // 오더 상태에 값이 있다면
            if (isFirstCondition) {
                jpql += "where";
                isFirstCondition = false;

            } else {
                jpql += " and";
            }
            jpql += "o.status =:status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);// 최대 1000개만 조회


        // 파라미터 바인딩 동적으로
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

```

➡️ **사용 XXXXX !!!** 매우 번거롭고, 실수 할 가능성 매우 높음

<br>

2️⃣ JPA Criteria 사용
```
/**
     * JPA Criteria 로 해결
     */
    public List<Order> findAllCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if(orderSearch.getOrderStatus()!=null){ // 조건에 맞춰 status 필터 넣음
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }
```
➡️ **사용 XXXXX !!!** 치명적인 단점: 유지보수 못함... 무슨 쿼리가 만들어지는지 떠올리기 어려움

<br>

### 3️⃣ Querydsl로 처리하는 것이 좋음!!