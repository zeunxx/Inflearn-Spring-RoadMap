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