## 실무 활용 - 스프링 데이터 JPA와 Querydsl

### 스프링 데이터 JPA와 Querydsl


```
public interface MemberRepository extends JpaRepository<Member, Long> {

    // select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}

```
- 스프링 데이터 JPA : MemberRepository 생성
- Querydsl 전용 기능인 회원 search를 작성할 수 없음 = 사용자 정의 리포지토리 필요

<br><Br>

### 사용자 정의 리포지토리

✅ **사용자 정의 리포지토리 사용법**
1. 사용자 정의 인터페이스 작성
2. 사용자 정의 인터페이스 구현
3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속

<br>

<img width="413" alt="image" src="https://user-images.githubusercontent.com/81572478/224250179-35b74a46-8f3f-442f-9053-99866224ba0d.png">

- 개발자가 진짜 사용하는 것은 MemberRepository

1. 사용자 정의 인터페이스 작성
```
public interface MemberRepositoryCustom{

    List<MemberTeamDto> search(MemberSearchCondition condition);
}

```

<br>

2. 사용자 정의 인터페이스 구현

```
@RequiredArgsConstructor
public class MemberRepositoryImpl implements  MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
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
}

```

<br>

3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속

```
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}

```

- 그리고 MemberRepository 사용하면 search 메소드 사용가능!

<br><Br>

### 스프링 데이터 페이징 활용

- 스프링 데이터의 Page, Pageable을 활용해보자.
- 전체 카운트를 한번에 조회하는 단순한 방법
- 데이터 내용과 전체 카운트를 별도로 조회하는 방법


<br>
```

    ✅ 사용자 정의 인터페이스

    // content와 count 한번에 조회
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    // content와 count 따로 조회
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);


    ✅ 사용자 정의 인터페이스 구체화

    ➡️ conten, count 한번에 조회

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> result = queryFactory
                .select(new QMemberTeamDto(
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
                .offset(pageable.getOffset()) //paging
                .limit(pageable.getPageSize()) //paging
                .fetchResults();// content용 쿼리와 count용 쿼리 두개 날라감(fetchResults)

        List<MemberTeamDto> content = result.getResults();
        long totalCount = result.getTotal();
        return new PageImpl<>(content, pageable,totalCount);


    }
    
    ➡️ conten, count 따로 조회

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
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
                .offset(pageable.getOffset()) //paging
                .limit(pageable.getPageSize()) //paging
                .fetch(); // content만 가져옴

        long totalCount = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetchCount(); //count용 쿼리
        
        return new PageImpl<>(content, pageable, totalCount);

    }


- content, count 한번에 조회: 
    - Querydsl이 제공하는 fetchResults() 를 사용하면 내용과 전체 카운트를 한번에 조회할 수 있다.(실제
쿼리는 2번 호출)
    - fetchResult() 는 카운트 쿼리 실행시 필요없는 order by 는 제거한다

- content, count 따로 조회
    - 전체 카운트를 조회 하는 방법을 최적화 할 수 있으면 이렇게 분리하면 된다. (예를 들어서 전체 카운트를
조회할 때 조인 쿼리를 줄일 수 있다면 상당한 효과가 있다.)
    - 코드를 리펙토링해서 내용 쿼리과 전체 카운트 쿼리를 읽기 좋게 분리하면 좋다.


<br>

### 스프링 데이터 페이징 활용2 - CountQuery 최적화
```
JPAQuery<Member> countQuery = queryFactory
            .select(member)
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
            );

return PageableExecutionUtils.getPage(content, pageable, ()-> countQuery.fetchCount());
// page 상황을 보고 fetchCount 날릴지 말지 알아서 결정해줌

```

- 스프링 데이터 라이브러리가 제공
- count 쿼리가 생략 가능한 경우 생략해서 처리
    - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
    - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)

<Br><Br>

### 스프링 데이터 페이징 활용3 - 컨트롤러 개발

```
    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }
```

<br>

➕ **스프링 데이터 정렬(Sort)**

스프링 데이터 JPA는 자신의 정렬(Sort)을 Querydsl의 정렬(OrderSpecifier)로 편리하게 변경하는
기능을 제공한다. 이 부분은 뒤에 스프링 데이터 JPA가 제공하는 Querydsl 기능에서 살펴보겠다.
스프링 데이터의 정렬을 Querydsl의 정렬로 직접 전환하는 방법은 다음 코드를 참고하자

```
JPAQuery<Member> query = queryFactory
 .selectFrom(member);
for (Sort.Order o : pageable.getSort()) {
 PathBuilder pathBuilder = new PathBuilder(member.getType(),
member.getMetadata());
 query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC,
 pathBuilder.get(o.getProperty())));
}
List<Member> result = query.fetch();

```

> 참고: 정렬( Sort )은 조건이 조금만 복잡해져도 Pageable 의 Sort 기능을 사용하기 어렵다. 루트 엔티티
범위를 넘어가는 동적 정렬 기능이 필요하면 스프링 데이터 페이징이 제공하는 Sort 를 사용하기 보다는
파라미터를 받아서 직접 처리하는 것을 권장한다