
## 객체지향 쿼리 언어란?

- JPA가 지원하는 쿼리 방법
    - JPQL
    - JPA Criteria
    - QueryDSL
    - 네이티브 SQL
    - JDBC API 직접사용, MyBatis, SpringJdbcTemplate 함께 사용

<BR>

### 🔎 JPQL

💁 가장 단순한 조회방법

    - EntityManager.find()
    - BUT, 나이가 18살 이상인 회원을 모두 검색하고 싶다면?

- JPA를 사용하면 엔티티 객체를 중심으로 개발, BUT 문제는 **검색쿼리**

- 검색을 할 때도 **테이블이 아닌 엔티티 객체를 대상으로 검색**

- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능!
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색조건이 포함된 SQL이 필요(WHERE, GROUP BY ... 등)

- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공

- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, 
HAVING, JOIN 지원

➡️ JPQL은 엔티티 객체를 대상으로 쿼리
➡️ SQL은 데이터베이스 테이블을 대상으로 쿼리

✅ JPQL을 짜면 SQL로 번역되어 실행 됨!

```
List<Member> result = em.createQuery(
                            "select m from Member m where m.name like '%kim%'", Member.class
                                ).getResultList();
```

- 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X
- JPQL을 한마디로 정의하면 객체 지향 SQL

<img width="427" alt="image" src="https://user-images.githubusercontent.com/81572478/215014060-cc12c9f1-7b69-4dc1-a0fe-6c178b539967.png">


<BR>

### 🔎 Cirteria 소개

```
//Criteria 사용 준비
CriteriaBuilder cb = em.getCriteriaBuilder(); 
CriteriaQuery<Member> query = cb.createQuery(Member.class); 
//루트 클래스 (조회를 시작할 클래스)
Root<Member> m = query.from(Member.class); 
//쿼리 생성 CriteriaQuery<Member> cq = 
query.select(m).where(cb.equal(m.get("username"), “kim”)); 
List<Member> resultList = em.createQuery(cq).getResultList();

```
- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- JPA 공식 기능
- **단점: 너무 복잡하고 실용성이 없음**

- Criteria 대신에 **QueryDSL 사용 권장**

<BR>

### 🔎 QueryDSL 

<img width="424" alt="image" src="https://user-images.githubusercontent.com/81572478/215015551-f8d50846-a075-47d6-8684-a1417f3ffdc2.png">

- 문자가 아닌 자바 코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- 컴파일 시점에서 문법 오류를 찾을 수 있음
- 동적 쿼리 작성 편리함
- 단순하고 쉬움
- 실무 사용 권장

<BR>

### 🔎 네이티브 SQL

```
String sql ="SELECT ID, AGE, TEAM_ID, NAME FROM MEMBER WHERE NAME = 'kim'"; 
List<Member> resultList =
                    em.createNativeQuery(sql, Member.class).getResultList();
```

#### 🔎 JDBC 직접 사용, SpringJdbcTemplate 등

- JPA를 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스등을 함께 사용 가능
- 단 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요
- ex) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 flush

> JDBC 등은 JPA와 관련이 없기 때문에 자동으로 영속성 컨텍스트에 의해 관리 받지 않음! 따라서 영속성 컨텍스트에만 있고 DB에 없을 경우도 있으므로 먼저 flush하고 JDBC로 DB 값 조회 등을 해야 함!

<br><Br>

## JPQL(Java Persistence Query Language)

### 🔎 JPQL 소개

- JPQL은 객체지향 쿼리 언어, 테이블을 대상으로 쿼리하는 것이 아니라 엔티티 객체를 대상으로 쿼리
- JPQL은 SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않음
- JPQL은 결국 SQL로 변환됨

<img width="377" alt="image" src="https://user-images.githubusercontent.com/81572478/215018322-67c8c267-ee63-4eb0-8078-b898576b8663.png">

<br>

### 🔎 JPQL 문법

<img width="398" alt="image" src="https://user-images.githubusercontent.com/81572478/215020049-9e664ed3-b79d-4ea5-b268-061231ed2b7b.png">

- ```select m from Member as m where m.age > 18```
- 엔티티와 속성은 대소문자 구분 O (Member, age)
- JPQL 키워드는 대소문자 구분 X (SELECT, FROM, where)
- 엔티티의 이름 사용, 테이블 이름이 아님!(ORDER(엔티티 이름), NOT ORDERS(테이블 이름))
- **별칭은 필수(m)** (as는 생략 가능)

<br>

<img width="382" alt="image" src="https://user-images.githubusercontent.com/81572478/215020405-06ab4bdb-5366-4be4-9ad6-2df8c8c15027.png">

<br>

#### 💡 TypeQuery, Query

- TypeQuery : 반환 타입이 명확할 때 사용
- Query :  반환 타입이 명확하지 않을 때 사용

<img width="366" alt="image" src="https://user-images.githubusercontent.com/81572478/215020592-3f9826ea-a110-4fba-8229-451690ec535a.png">

<br>

#### 💡 결과조회 API
- ```query.getResultList()```: **결과가 하나 이상일 때**, 리스트 반환
    - 결과가 없으면 빈 리스트 반환

- ```query.getSingleResult()```: **결과가 정확히 하나**, 단일 객체 반환
    - 결과가 없으면: javax.persistence.NoResultException
    - 둘 이상이면: javax.persistence.NonUniqueResultException

➡️ 리스트 쓰는게 나을거같음..

<BR>

#### 💡파라미터 바인딩 - 이름 기준, 위치 기준
```
// 이름 기준
SELECT m FROM Member m where m.username=:username 
query.setParameter("username", usernameParam);

// 위치 기준 - 웬만하면 사용 x
SELECT m FROM Member m where m.username=?1 
query.setParameter(1, usernameParam);


➡️ 이런 식으로 쓰는 게 좋음
List<Member> resultList = em.createQuery("select m from Member m where m.name = :name", Member.class)
                    .setParameter("name", "member1")
                    .getResultList();
```

<br><br>

### 1️⃣ 프로젝션

- SELECT 절에 조회할 대상을 지정하는 것
- 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 데이터 타입)
    - SELECT m FROM Member m -> 엔티티 프로젝션
    - SELECT m.team FROM Member m -> 엔티티 프로젝션
    - SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
        -  SELECT a.address FROM Address a = 불가능
    - SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션
- DISTINCT로 중복 제거

<BR>

#### 💡 프로젝션 - 여러 값 조회

- SELECT m.username, m.age FROM Member m

1. Query 타입으로 조회
    - Query : 반환 타입이 명확하지 않을 때 사용
2. Object[] 타입으로 조회
3. **new 명령어로 조회**
    - 단순 값을 DTO로 바로 조회
    ```
    List<MemberDto> resultList = em.createQuery("SELECT new org.example.MemberDto(m.name, m.age) FROM Member m", MemberDto.class)
                    .getResultList();
    ```
    - 패키지 명을 포함한 전체 클래스명 입력
    - 순서와 타입이 일치하는 생성자 필요

<BR>

### 2️⃣ 페이징
- JPA는 페이징을 다음 두 API로 추상화

1. ```setFirstResult(int startPosition)``` : 조회 시작 위치
2. ```setMaxResults(int maxResult)``` : 조회할 데이터 수

```
List<Member> resultList = em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(0) // 0번째부터
                    .setMaxResults(10) // 10개만 조회
                    .getResultList();
```

<br>

### 3️⃣ 조인

- 내부 조인:
    ```SELECT m FROM Member m [INNER] JOIN m.team t```

- 외부 조인:
    ```SELECT m FROM Member m LEFT [OUTER] JOIN m.team t```

- 세타 조인:
    ```select count(m) from Member m, Team t where m.username = t.name```
    - join 안적지만 join 쿼리 나감

#### 💡 조인 - ON 절

- ON절을 활용한 조인
1. 조인 대상 필터링
```
예) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인

JPQL:
SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A' 

SQL:
SELECT m.*, t.* FROM 
Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='A'

```
    ➡️ WHERE과 다름(애초에 A가 아니면 조인부터 안함)

2. 연관관계 없는 엔티티 외부 조인
 (세타조인처럼 전혀 상관없는 엔티티가 **외부** 조인 가능)

 ```
 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인

JPQL:
SELECT m, t FROM
Member m LEFT JOIN Team t on m.username = t.name

SQL:
SELECT m.*, t.* FROM 
Member m LEFT JOIN Team t ON m.username = t.name
 ```


