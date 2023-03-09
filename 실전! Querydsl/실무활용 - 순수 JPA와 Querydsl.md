## 순수 JPA와 Qeurydsl


### 순수 JPA 리포지토리와 Querydsl
```

    // 순수 JPA
    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // querydsl
    public List<Member> findAll_Querydsl(){
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

     // 순수 JPA
    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member  m where m.username =: username", Member.class)
                .setParameter("username",username)
                .getResultList();
    }

    // querydsl
    public List<Member> findByUsername_Querydsl(String username){
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }
```

> 참고: 동시성 문제는 걱정하지 않아도 된다. 왜냐하면 여기서 스프링이 주입해주는 엔티티 매니저는 실제
동작 시점에 진짜 엔티티 매니저를 찾아주는 프록시용 가짜 엔티티 매니저이다. 이 가짜 엔티티 매니저는
실제 사용 시점에 트랜잭션 단위로 실제 엔티티 매니저(영속성 컨텍스트)를 할당해준다.
> 더 자세한 내용은 자바 ORM 표준 JPA 책 13.1 트랜잭션 범위의 영속성 컨텍스트를 참고하자.


<br><Br>

### 동적 쿼리와 성능 최적화 조회 - Builder 사용

```
    BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto( // builder로 동적쿼리 만들고 new QMemberTeamDto로 성능 최적화
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();

```

- dto 클래스에 `@QueryProjection` 추가

- builder로 동적쿼리 만들고 `select(new QMemberTeamDto)`로 성능 최적화

> 참고: @QueryProjection 을 사용하면 해당 DTO가 Querydsl을 의존하게 된다. 이런 의존이 싫으면, 해당 에노테이션을 제거하고, Projection.bean(), fields(), constructor() 을 사용하면 된다.

<br>

### 동적 쿼리와 성능 최적화 조회 - Where절 파라미터 사용

```
    public List<MemberTeamDto> search(MemberSearchCondition condition){

        return queryFactory
                .select(new QMemberTeamDto( // builder로 동적쿼리 만들고 new QMemberTeamDto로 성능 최적화
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                        )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username)?member.username.eq(username):null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName)?team.name.eq(teamName):null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe!=null?member.age.goe(ageGoe):null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe!=null?member.age.loe(ageLoe):null;
    }

```

- where 파라미터로 동적쿼리 만들고 `select(new QMemberTeamDto)`로 성능 최적화

- 한눈에 파악 쉽고, 재사용 좋음
```
private BooleanExpression ageBetween(int ageLoe, int ageGoe){
        return ageGoe(ageGoe).and(ageLoe(ageLoe));
    }
```
- 이렇게 condition 조립 가능!

<br><Br>

### 조회 API 컨트롤러

```
    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }
```

- `http://localhost:8080/v1/members?teamName=teamB&ageGoe=31&ageLoe=35`
