## 중급 문법

### 프로젝션과 결과 반환 - 기본

- 프로젝션: select 대상 지정

✅ **프로젝션 대상이 하나**
```

List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

```

- 프로젝션 대상이 하나면 타입을 명확히 지정할 수 있음 (List<String>)
- 프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회!

<BR>


✅ **튜플 조회**
프로젝션 대상이 둘 이상일때 사용
```
    List<Tuple> result = queryFactory.select(member.username, member.age)
                .from(member)
                .fetch();

    for (Tuple tuple : result) {
        String username = tuple.get(member.username);
        Integer age = tuple.get(member.age);
    }


```
- 튜플은 repository내에서만 사용하는 것 권장
- repository 외에서 쓸땐 dto로 변환하는 것이 좋음

<br>

✅ **DTO 조회**

1️⃣ 순수 JPA에서 DTO 조회 코드
```
    List<MemberDto> result =
                em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m ", MemberDto.class)
                .getResultList();
```
- 순수 JPA에서 DTO를 조회할때는 new 명령어를 사용해야 함
- DTO의 package 이름을 다 적어야 해서 지저분함
- 생성자 방식만 지원!

<br>

2️⃣ **querydsl 빈 생성(Bean population)**

결과를 DTO 반환할때 사용

- 프로퍼티 접근 방법
- 필드 직접 접근 방법
- 생성자 사용 방법

<br>

```
💡 프로퍼티 접근 - Setter

    List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

    // MemberDto 클래스 내에 getter, setter 필요

💡 필드 직접 접근

    List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

    // MemberDto 클래스 내에 getter, setter 필요 없음, 필드에 바로 값 넣음

💡 생성자 사용

    List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age)) // MemberDto의 생성자 순서와 일치해야 함
                .from(member)
                .fetch();

     ➡️이름이 아닌 타입(String, int 등)으로 들어가므로 별칭이 달라도 상관없음


➕ 별칭이 다를 때!

    //UserDto는 name인데 별칭이 username이라 달라서 null로 들어감

    List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), //UserDto는 name인데 별칭이 username이라 달라서 null로 들어감
                        member.age))
                .from(member)
                .fetch();


    List<UserDto> result2 = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), //UserDto의 name

                        ExpressionUtils.as(JPAExpressions //UserDto의 age(서브쿼리 별칭 생성)
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

```
- 프로퍼티나 필드 접근 생성 방식에서 이름이 다를때 해결방안
- `ExpressionUtils.as(source, alias)`: 필드나 서브쿼리에 별칭 적용
- `username.as("memberName")`: 필드에 별칭 적용

<br><Br>

### 프로젝션과 결과 반환 - @QueryProjection

✅ 생성자 + @QueryProjection

```
- MemberDto.class

public class MemberDto {

    private String username;
    private int age;

    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}


- repository

List<MemberDto> result = queryFactory
            .select(new QMemberDto(member.username, member.age))
            .from(member)
            .fetch();

➡️ 컴파일 시점에 오류 바로 확인 가능!

```

이 방법은 컴파일러로 타입을 체크할 수 있으므로 가장 안전한 방법

다만 DTO에 QueryDSL 어노테이션을 유지해야 하는 점과 DTO까지 Q 파일을 생성해야 하는 단점이 존재!
- dto 클래스가 querydsl에 대한 의존성이 생기게 됨
- dto가 순수하지 않음

<br>

**distinct**

```
List<String> result = queryFactory
    .select(member.username).distinct()
    .from(member)
    .fetch();

```
> 참고: distincy는 JPQL의 distinct와 같음


<br><Br>

## 동적쿼리

- BooleanBuilder
- Where 다중 파라미터 사용

### 동적쿼리 해결 -  BooleanBuilder 사용

```
    BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond!=null){
            builder.and(member.username.eq(usernameCond));
        } // username 값이 넘어오면 username 값 추가

        if (ageCond!=null){
            builder.and(member.age.eq(ageCond));
        } // age 값이 넘어오면 age 값 추가

        List<Member> result = queryFactory.selectFrom(member)
                .where(builder)
                .fetch();

        // builder의 모든 값이 null 이면 조건 x
        // new BooleanBuilder() 생성시 ()안에 값 넣으면 not null (반드시 값 넘어와야 함)
```

<br>

### 동적쿼리 해결 -  Where 다중 파라미터 사용

```
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        queryFactory = new JPAQueryFactory(em);
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private Predicate usernameEq(String usernameCond) {
        if(usernameCond == null){
            return null;
        }
        return member.username.eq(usernameCond);
    }
    private Predicate ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

```

- where 조건에 null 값은 무시됨
- 메서드를 다른 쿼리에서도 재활용 할 수 있음
- 쿼리 자체의 가독성이 높아짐

<br>

 ➕**조합 가능**

```
private BooleanExpression allEq(String usernameCond, Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
```
- null 체크는 주의해서 처리!

<br><Br>

### 수정, 삭제 배치 쿼리(벌크)

쿼리 한번으로 대량 데이터 수정
```
    // member1, member2 -> 비회원으로 변경됨
    long count = queryFactory
            .update(member)
            .set(member.username, "비회원")
            .where(member.age.lt(28))
            .execute();

    // bulk 연산은 영속성 컨텍스트 무시하고 바로 db에 쿼리 날림
    // 따라서 영속성 컨텍스트와 db의 상태가 다름!!(영속성: member1, member2 / db: 비회원, 비회원)

    // 따라서 벌크 연산 수행 후 영속성컨텍스트 한번 날리는 코드 필요

    em.flush();
    em.clear();
    
```
<br>

➕ 기존 숫자에 1 더하기
```
long count = queryFactory.update(member)
            .set(member.age, member.age.add(1))
            .execute();
```
- 곱하기 : `multiply(x)`

<br>

➕ 데이터 삭제
```
 long count = queryFactory.delete(member)
            .where(member.age.gt(18))
            .execute();

    // 18세 미만 member 삭제
```

> 주의: JPQL 배치와 마찬가지로, 영속성 컨텍스트에 있는 엔티티를 무시하고 실행되기 때문에 배치 쿼리를
실행하고 나면 영속성 컨텍스트를 초기화 하는 것이 안전함

<br><Br>

### SQL function 호출하기

SQL function은 JPA와 같이 Dialect에 등록된 내용만 호출할 수 있음

```
    List<String> result = queryFactory.select(
                        Expressions.stringTemplate("function('replace',{0},{1},{2})",
                                member.username, "member", "M"))
                // member 단어를 M으로 바꿔 조회
                .from(member)
                .fetch();   

```

- 소문자로 변경해서 비교

```
        List<String> result = queryFactory.select(member.username)
                .from(member)
                .where(member.username.eq(
                        Expressions.stringTemplate("function('lower',{0})", member.username)))
                .fetch();

    ➡️ lower같은 ansi 표준 함수들은 querydsl이 상당 부분 내장!

    .where(member.username.eq(member.username.lower())) // 이렇게 변경 가능!
```

