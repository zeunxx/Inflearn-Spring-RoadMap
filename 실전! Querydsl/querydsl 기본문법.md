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