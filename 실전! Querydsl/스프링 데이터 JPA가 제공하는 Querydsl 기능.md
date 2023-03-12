## 스프링 데이터 JPA가 제공하는 Querydsl 기능

여기서 소개하는 기능은 제약이 커서 복잡한 실무 환경에서 사용하기에는 많이 부족함!

<br>

### 인터페이스 지원 - QuerydslPredicateExecutor
```
public interface MemberRepository extends JpaRepository<Member, Long>,QuerydslPredicateExecutor<Member> {
    ...
}

✅ respository 외부 클래스
QMember member = QMember.member;
Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));
```
- 단점!
    - 조인 x(묵시적 조인은 가능하지만 left join이 불가능)
    - 클라이언트가 Querydsl에 의존해야 함! 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야 함
    - 복잡한 실무환경에서 사용하기에는 한계가 명확

> 참고: QuerydslPredicateExecutor 는 Pagable, Sort를 모두 지원하고 정상 동작한다.

<br><Br>

### Querydsl Web 지원

✅ 한계점
- 단순한 조건만 가능
- 조건을 커스텀하는 기능이 복잡하고 명시적이지 않음
- 컨트롤러가 Querydsl에 의존
- 복잡한 실무환경에서 사용하기에는 한계가 명확


<br><Br>

### 리포지토리 지원 - QuerydslRepositorySupport

```
public class MemberRepositoryImpl extends QuerydslRepositorySupport implements  MemberRepositoryCustom{

    @Override
    public Page<MemberTeamDto> searchPageSimple2(MemberSearchCondition condition, Pageable pageable) {

        List<MemberTeamDto> result = from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .select(new QMemberTeamDto( // builder로 동적쿼리 만들고 new QMemberTeamDto로 성능 최적화
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName"))
                )
                .fetch();   


    // 페이징 버전

        JPQLQuery<MemberTeamDto> jpaQuery = from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ));


        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpaQuery);
        List<MemberTeamDto> result = query.fetch();

    }

}

```


✅ 장점
- getQuerydsl().applyPagination() 스프링 데이터가 제공하는 페이징을 Querydsl로 편리하게 변환
가능(단! Sort는 오류발생)
- from() 으로 시작 가능(최근에는 QueryFactory를 사용해서 select() 로 시작하는 것이 더 명시적)
- EntityManager 제공

<br>

✅ 한계
- Querydsl 3.x 버전을 대상으로 만듬
- Querydsl 4.x에 나온 JPAQueryFactory로 시작할 수 없음
    - select로 시작할 수 없음 (from으로 시작해야함)
- QueryFactory 를 제공하지 않음
- 스프링 데이터 Sort 기능이 정상 동작하지 않음

<br><Br>

### Querydsl 지원 클래스 직접 생성

스프링 데이터가 제공하는 QuerydslRepositorySupport 가 지닌 한계를 극복하기 위해 직접 Querydsl 
지원 클래스를 만들어보자

<br>

✅ 장점
- 스프링 데이터가 제공하는 페이징을 편리하게 변환
- 페이징과 카운트 쿼리 분리 가능
- 스프링 데이터 Sort 지원
- select(), selectFrom() 으로 시작 가능
- EntityManager, QueryFactory 제공

```

    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<Member> query = selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();

        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable){

        return applyPagination(pageable,
                query -> query
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
        );
    }


    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable){

        return applyPagination(pageable,
                contentQuery -> contentQuery // content용 쿼리
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ),

                countQuery -> countQuery // count용 쿼리
                        .select(member.id)
                        .from(member)
                        .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
        );
    }

```