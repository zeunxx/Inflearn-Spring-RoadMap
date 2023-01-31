## JPQL 경로 표현식

- 경로 표현식
    - .(점)을 찍어 객체 그래프를 탐색하는 것
    <img width="259" alt="image" src="https://user-images.githubusercontent.com/81572478/215494996-6c8d0ef3-da8e-4283-b421-fee6cf1a1dd5.png">

- 상태필드 : 단순히 값을 저장하기 위한 필드
- 연관필드 : 연관관계를 위한 필드
    - 단일 값 연관 필드:
        @ManyToOne, @OneToOne, 대상이 엔티티(ex: m.team)
    
    - 컬렉션 값 연관 필드:
        @OneToMany, @ManyToMany, 대상이 컬렉션(ex: m.orders) ➡️ List<Order>
<br>

#### 💡 경로 표현식 특징
- 상태필드: 경로탐색의 끝, 더이상 탐색 x
- 단일 값 연관 경로: 묵시적 내부조인(inner join) 발생, 탐색 o
    ```
    String query = "select m.team.name from Member m ";
    List<String> resultList = em.createQuery(query, String.class)
                    .getResultList();
    ```
    - m.team.name / m.team.id 등 더 m.team에서 더 탐색 가능


- 컬렉션 값 연관 경로 : 묵시적 내부조인 발생, 탐색 x
    ```
    String query = "select t.members from Team t ";
    Collection resultList = em.createQuery(query, Collection.class)
                    .getResultList();
    ```
    - 탐색하고 싶으면 FROM 절에서 명시적 조인을 통해 별칭을 얻어 별칭 통해서 탐색!
    ```
    String query = "select m.name from Team t join t.members m";
    List<String> resultList = em.createQuery(query, String.class)
                    .getResultList();
    ```

➡️ 묵시적 내부조인이 발생하면 안좋음. 그냥 **명시적 조인** 사용!!

<br>

#### 💡 명시적 조인, 묵시적 조인
- 명시적 조인 : join 키워드 직접 사용
    - ```select m from Member m join m.team t```

- 묵시적 조인 : 경로 표현식에 의해 묵시적으로 SQL 조인 발생(내부조인만 가능)
    - ```select m.team from Member m```

<br>

#### 💡 경로 표현식 예제
- ```select o.member.team from Order o``` -> 성공
- ```select t.members from Team``` -> 성공
- ```select t.members.username from Team t``` -> 실패
- ```select m.username from Team t join t.members m``` -> 성공

<br>

#### 💡 경로 탐색을 사용한 묵시적 조인시 주의사항
- 항상 내부 조인
- 컬렉션은 경로탐색의 끝! 명시적 조인을 통해 별칭을 얻어야 함
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시적 조인으로 인해 SQL의 FROM (JOIN) 절에 영향을 줌

## 📌 실무조언

- 가급적 묵시적 조인 대신 **명시적 조인 사용**
- 조인은 SQL 튜닝에 중요 포인트
- 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어려움

<BR><bR>

## ⭐ JPQL - fetch join ⭐ 

### Fetch Join
- SQL 조인 종류 아님
- JPQL에서 성능 최적화를 위해 제공하는 기능
- 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회 가능
- join fetch 명령어 사용
- 페치 조인 ::= [ LEFT [OUTER] | INNER ] JOIN FETCH 조인경로

<br>

#### 💡 엔티티 페치 조인
- 회원을 조회하면서 연관된 팀도 함께 조회(SQL 한번에)
- SQL을 보면 회원 뿐만 아니라 팀(T.*)도 함께 조회

<img width="333" alt="image" src="https://user-images.githubusercontent.com/81572478/215501348-d31772c4-f5c9-4e7e-bc9c-14bbfc4936ae.png">


<img width="617" alt="image" src="https://user-images.githubusercontent.com/81572478/215501700-e08084bc-77e1-4347-8fb7-d4c7dd68a105.png">

#### <fetch join 전>
```
String query = "select m from Member m join fetch m.team";
List<Member> resultList = em.createQuery(query, Member.class)
                    .getResultList();

for (Member member1 : resultList) {
    System.out.println("member = " + member1.getName()+", "+ member1.getTeam().getName());

    // 회원1, 팀A(SQL)
    // 회원2, 팀A(1차 캐시)
    // 회원3, 팀B(SQL)

}
```

#### <fetch join 사용 코드>

<img width="371" alt="image" src="https://user-images.githubusercontent.com/81572478/215505608-d5bcf2be-b7de-449a-9c65-086a500e5bf2.png">
- 지연로딩을 사용했지만 fetch join이 우선이라 지연로딩 x

<br>

#### 💡 컬렉션 페치 조인
- 일대다 관계, 컬렉션 페치 조인

<img width="403" alt="image" src="https://user-images.githubusercontent.com/81572478/215505990-320d5c76-59fe-421f-9bdd-3cb6c96d663d.png">

<img width="410" alt="image" src="https://user-images.githubusercontent.com/81572478/215507722-766e8eae-7d10-498f-9ed3-d9324c733cc8.png">


<img width="122" alt="image" src="https://user-images.githubusercontent.com/81572478/215507316-9bad8973-5a14-4799-86d6-01d8af77018d.png">

- 팀A가 두줄 출력됨
- 일대다 관계에서 이런 일이 발생함!!

<BR>

#### 💡 페치 조인과 DISTINCT

- SQL의 DISTINCH는 중복된 결과 제거
- JPQL의 DISTINCT는 2가지 기능 제공
    1. SQL에 DISTINCT를 추가
    2. 애플리케이션에서 엔티티 중복 제거
        

```
String query = "select distinct t from Team t join fetch t.members";
List<Team> resultList = em.createQuery(query, Team.class)
                    .getResultList();

```

<img width="374" alt="image" src="https://user-images.githubusercontent.com/81572478/215509043-802f61d5-6a5d-46b9-9546-f5e09c33d021.png">

➡️ DISTINCH가 추가로 애플리케이션에서 중복 제거시도!!
➡️ 같은 식별자를 가진 Team 엔티티 제거

<img width="300" alt="image" src="https://user-images.githubusercontent.com/81572478/215509348-af47d5ac-2aeb-404e-8eb1-20de3756f015.png">

<img width="115" alt="image" src="https://user-images.githubusercontent.com/81572478/215509403-53a56ca8-0c09-4d14-b377-17fb906359e2.png">

<br>

#### 💡 페치 조인과 일반 조인의 차이
- 일반 조인 실행시 연관된 엔티티를 함께 조회하지 않음
    - select할때 team만 조회함
    <img width="451" alt="image" src="https://user-images.githubusercontent.com/81572478/215510384-b9c3d0b2-ff62-49a0-b626-35b015c87b0e.png">

- JPQL은 결과를 반환할때 연관관계 고려X
- 단지 SELECT 절에 지정한 엔티티만 조회함
- 여기서는 팀엔티티만 조회, 회원엔티티는 조회 X

➡️ 페치 조인을 사용할때만 연관된 엔티티도 **함께 조회(즉시 로딩)**
➡️ **페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념**

<br>

#### 💡 페치 조인의 특징과 한계
- **페치 조인 대상에는 별칭을 줄 수 없음**
    ```
    String query = "select t from Team t join fetch t.members m"; ➡️ 불가능

    String query = "select t from Team t join fetch t.members "; ➡️ 가능 별칭 없애야 함
    ```
    - 하이버네이트는 가능, 가급적 사용 x

- **둘 이상의 컬렉션은 페치 조인 할 수 없음**
    - 페치 조인의 컬렉션은 하나만 지정 가능

- **컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults) 사용 불가**
    - 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
    - 컬렉션(일대다/다대다)의 경우: 하이버네이트는 경고로그를 남기고 메모리에서 페이징(매우 위험)
    ```
    String query = "select t from Team t join fetch t.members m";
    List<Team> resultList = em.createQuery(query, Team.class)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .getResultList();
    ➡️ 일대다 :  위험함

    String query = "select m from Member m join fetch m.team";
    List<Member> resultList = em.createQuery(query, Member.class)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .getResultList();
    ➡️ 다대일 : 일대다를 다대일로 변경  - 안전

    또는 @BatchSize 어노테이션 사용!

    <property name="default_batch_fetch_size" value="100"/>
    
    /
    
    @BatchSize(size =100)
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    ```
<BR>

#### ✅ 페치 조인: 연관된 엔티티들을 SQL 한번으로 조회 - 성능 최적화
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선함
    - @OneToMany(fetch = FetchType.LAZY) //글로벌 로딩 전략
- 실무에서 글로벌 로딩 전략은 모두 지연 로딩
- 최적화가 필요한 곳은 페치 조인 적용
- 모든 것을 페치 조인으로 해결 불가
- 페치 조인은 객체 그래프를 유지할 때 사용하면 효과적
    - m.team / m.name 등을 찾아갈때 좋음
- 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 하면, 페치 조인 보단 일반 조인을 사용해서 필요한 데이터들만 조회 -> DTO로 반환하는 것이 효과적

<BR><bR>

## JPQL - 다형성 쿼리

<img width="351" alt="image" src="https://user-images.githubusercontent.com/81572478/215675223-fba20c53-19db-437d-a230-d2947e4ad5b8.png">

- TYPE
    - 조회 대상을 특정 자식으로 한정 가능
        EX) ITEM 중에서 Book, Movie를 조회해라

    - [JPQL]
        ```select i from Item i where tyep(i) IN (Book, Movie)```

    - [SQL]
        ```select i from i where i.DTYPE in ('B','M')```

- TREAT
    - 자바의 타입 캐스팅과 유사
    - 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰때 사용
    - FROM, WHERE, SELECT(하이버네이트 지원) 사용

    EX) 부모인 Item과 자식 Book이 있을때

    - [JPQL]
        ```select i from Item i where treat(i as Book).author = 'kim'```

    - [SQL]
        ```select i.* from Item i where i.DTYPE='B' and i.author = 'kim'```

<br><Br>

## JPQL - 엔티티 직접 사용

- 기본키 값
    : JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본키 값 사용

    - [JPQL]
    ```select count(m.id) from Member m //엔티티의 아이디를 사용```
    ```select count(m) from Member m //엔티티를 직접 사용```

    - [SQL](JPQL 둘다 같은 다음 SQL 실행)
    ```select count(m.id) as cnt from Member m```

    <img width="425" alt="image" src="https://user-images.githubusercontent.com/81572478/215676152-a7c564a0-7655-42bf-a2b3-ab0b08ab3a5b.png">

- 외래키 값

    <img width="427" alt="image" src="https://user-images.githubusercontent.com/81572478/215676713-b8a91b00-df93-4147-bdc6-03de0ea6236d.png">

<br><Br>

## JPQL - Named 쿼리

- 미리 정의해서 이름을 부여해두고 사용하는 JPQL = 쿼리에 이름 부여해 재활용 가능

- 정적 쿼리

- 어노테이션, XML에 정의
    - xml이 항상 우선권을 가지고, 애플리케이션 운영 환경에 따라 다른 xml 배포 가능
- 애플리케이션 로딩 시점에 초기화 후 재사용
- **애플리케이션 로딩 시점에 쿼리를 검증**

<img width="425" alt="image" src="https://user-images.githubusercontent.com/81572478/215679969-d70b528f-3a93-4c7f-a72a-0f952d171b14.png">

<br><Br>

## JPQL - 벌크 연산

- 재고가 10개 미만인 모든 상품의 가격을 10% 상승하려면?
- JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행됨
    1. 재고가 10개 미만인 상품을 리스트로 조회
    2. 상품 엔티티의 가격을 10% 증가
    3. 트랜잭션 커밋 시점에 변경 감지 동작
    - 변경된 데이터가 100건이라면 100번의 UPDATE SQL 실행

EX) 쿼리 한번으로 여러 테이블 ROW 변경(ENTITY)

```
int resultCount = em.createQuery("update Member m set m.age = 20")
                    .executeUpdate();

System.out.println("resultCount = " + resultCount);
```
- executeUpdate()의 결과는 영향받은 엔티티 수 반환
- UPDATE, DELETE 지원
- INSERT(insert into  .. select, 하이버네이트 지원)

    <img width="393" alt="image" src="https://user-images.githubusercontent.com/81572478/215681298-3439e126-21cb-4999-91cb-1a351d02d5bd.png">

<br>
📌 벌크 연산 주의

- 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
    - 벌크 연산을 먼저 실행
    - 벌크 연산 수행 후 영속성 컨텍스트 초기화

    ```
    Member member3 = new Member();
    member3.setName("회원 3");
    member3.setTeam(team2);
    em.persist(member3);

    // FLUSH 자동 호출 (member들 다 persist됨)
    int resultCount = em.createQuery("update Member m set m.age = 20")
            .executeUpdate();

    // 아직 영속성 컨텍스트 초기화 X
    Member member1 = em.find(Member.class, member.getId());
    System.out.println("member1 = " + member1.getAge()); // 0

    em.clear();

    // 영속성 컨텍스트 초기화
    Member member1 = em.find(Member.class, member.getId());
    System.out.println("member1 = " + member1.getAge()); // 20

    ```



    