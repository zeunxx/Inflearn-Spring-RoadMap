## 기본 문법

### JPQL vs Querydsl

1️⃣ JPQL
```
    //member1을 찾아라
        String qlString =
                "select m from Member m" +
                " where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
```
<BR>

2️⃣ Querydsl
```
    //member1을 찾아라
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

```

- EntityManager로 JPAQueryFactory 생성
- Querydsl은 JPQL 빌더
- JPQL: 문자(실행 시점 오류), Querydsl: 코드(컴파일 시점 오류)
- JPQL: 파라미터 바인딩 직접, Querydsl: 파라미터 바인딩 자동 처리

<br><Br>

### 기본 Q-Type 활용

- Q클래스를 인스턴스를 사용하는 2가지 방법
```
QMember qMember = new QMember("m"); //별칭 직접 지정
QMember qMember = QMember.member; //기본 인스턴스 사용 
```
- 기본 인스턴스를 static import 하면 좋음

> 참고: 같은 테이블을 조인해야 하는 경우가 아니면 기본 인스턴스 사용하기! 같은 테이블을 조인하는 경우는 별칭이 달라야 하므로 별칭 직접 지정

<br><Br>

### 검색 조건 쿼리

```
Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();


```
- 검색 조건은 `.and()`, `.or()`을 메서드 체인으로 연결 가능
- select, from을 selectFrom으로 합칠 수 있음

<BR>

✅ **JPQL이 제공하는 모든 검색 조건 제공**
```
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'
member.username.isNotNull() //이름이 is not null
member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30
member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30
member.username.like("member%") //like 검색
member.username.contains("member") // like ‘%member%’ 검색
member.username.startsWith("member") //like ‘member%’ 검색
...

```

<BR>

✅ **AND 조건을 파라미터로 처리**
```
Member findMember = queryFactory.selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        (member.age.between(10,30))
                )
                .fetchOne();
```

- `.and()` 대신 `,` 사용해도 and 됨
- null이 있으면 null을 무시하고 and로 엮음
        
  - 메서드 추출을 활용해서 동적 쿼리를 깔끔하게 만들 수 있음

<br><Br>

### 결과 조회

- `fetch()` : 리스트 조회, 데이터 없으면 빈 리스트 반환
- `fetchOne()` : 단 건 조회

  - 결과가 없으면 : null
  - 결과가 둘 이상이면 : `com.querydsl.core.NonUniqueResultException`
- `fetchFirst()` : limit(1).fetchOne()
- `fetchResults()` : 페이징 정보 포함, total count 쿼리 추가 실행
- `fetchCount()` : count 쿼리로 변경해서 count 수 조회

<Br>

```
// list
List<Member> fetch = queryFactory.selectFrom(member)
                .fetch();

// 단 건
Member fetchOne = queryFactory.selectFrom(member)
                .fetchOne();

// 처음 한 건 조회
Member fetchFirst = queryFactory.selectFrom(member)
                .fetchFirst();// limit(1).fetchOne()

// 페이징에서 사용
QueryResults<Member> results = queryFactory.selectFrom(member)
                .fetchResults();
results.getTotal(); // total count 쿼리 날라감
List<Member> content = results.getResults();

// count 쿼리로 변경
long count = queryFactory.selectFrom(member)
                                    .fetchCount();

```

<br><Br>

### 정렬

```
/**
* 회원 정렬 순서
* 1. 회원 나이 내림차순(desc)
* 2. 회원 이름 올림차순(asc)
* 단 2에서 회원이름이 없으면 마지막에 출력(nulls last)
*/

List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(),
                        member.username.asc().nullsLast())
                .fetch();
```
- desc() , asc() : 일반 정렬
- nullsLast() , nullsFirst() : null 데이터 순서 부여

<br><br>

### 페이징

```
✅ 조회 건수 제한
List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //원래 0부터 시작
                .limit(2) // 최대 2건 조회
                .fetch();


✅ 전체 조회 수(데이터 수)가 필요할때
QueryResults<Member> queryResults = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

````

> 주의: count 쿼리가 실행되니 성능상 주의!!

> 참고: 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만, 
count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두
조인을 해버리기 때문에 성능이 안나올 수 있다. count 쿼리에 조인이 필요없는 성능 최적화가 필요하다면, 
count 전용 쿼리를 별도로 작성해야 한다.

<br><Br>

### 집합


 ✅**집합 함수**
```
List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch(); // 어차피 결과 1개라 fetchOne도 가능!
```
- JPQL이 제공하는 모든 집합 함수를 제공
- tuple은 프로젝션과 결과 반환에서 설명

<br>

 ✅**GroupBy 사용**
 ```
/**
     * 팀의 이름과 각 팀의 평균 연령 구하기
     */

List<Tuple> result = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

 ```
 - `groupBy`, 그룹화된 결과를 제한하려면 `having`

<br>

➕ **gruopBy(), having() 예시**
```
.groupBy(item.price)
.having(item.price.gt(1000))

```
<br><Br>

### 조인 - 기본 조인

- join(조인 대상, 별칭으로 사용할 Q타입)

```
/**
 * 팀 A에 소속된 모든 회원
 */

List<Member> result = queryFactory.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
```

- `join()`, `innerJoin()`: 내부 조인
- `leftJoin()` : left 외부 조인(left outer join)
- `rightJoin()` : rigth 외부 조인(rigth outer join)
- JPQL의 on 과 성능 최적화를 위한 fetch 조인 제공 다음 on 절에서 설명

<br>

### 조인 - 세타조인

연관관계가 없는 필드로 조인!!

```
/**
* 세타 조인
* 회원의 이름이 팀 이름과 같은 회원 조회
*/
List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

```

- from 절에 여러 엔티티를 선택해서 세타 조인
- 외부조인 불가능 ➡️ 다음의 조인 on을 사용하면 외부 조인 가능!