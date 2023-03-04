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