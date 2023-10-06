## 트랜잭션 AOP

트랜잭션 템플릿 덕분에 트랜잭션을 처리하는 반복코드는 해결할 수 있지만, 서비스 계층에 순수한 비즈니스 로직만 남긴다는 목표는 달성 못했다.


이때 스프링 AOP를 통해 프록시를 도입하면 문제를 깔끔하게 해결할 수 있다.


<BR>

### 프록시를 통한 문제 해결


프록시 도입 전
![image](https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/370c44b5-e585-4a24-9e75-f87a75685fd1)

프록시를 도입하기 전에는 기존처럼 서비스 로직에서 트랜잭션 직접 시작

```
✅ 서비스 코드 

//트랜잭션 시작
TransactionStatus status = transactionManager.getTransaction(new
DefaultTransactionDefinition());
try {
 //비즈니스 로직
    bizLogic(fromId, toId, money);
    transactionManager.commit(status); //성공시 커밋
} catch (Exception e) {
    transactionManager.rollback(status); //실패시 롤백
    throw new IllegalStateException(e);
}
```

<BR>

![image](https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/47be1b91-74c4-48ef-85bb-ab1f623416be)


프록시를 사용하면 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리할 수 있다.

```

 ✅ 프록시 코드
public class TransactionProxy {
    private MemberService target;
        public void logic() {
        //트랜잭션 시작
            TransactionStatus status = transactionManager.getTransaction(..);
        try {
        //실제 대상 호출
            target.logic();
            transactionManager.commit(status); //성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }
    }
}


 ✅ 서비스 코드
public class Service {
    public void logic() {
    //트랜잭션 관련 코드 제거, 순수 비즈니스 로직만 남음
        bizLogic(fromId, toId, money);
 }
}


```

- 프록시 도입 전: 서비스에 비즈니스 로직과 트랜잭션 처리 로직이 함께 섞여있다.

- 프록시 도입 후: 트랜잭션 프록시가 트랜잭션 처리 로직을 모두 가져간다. 그리고 트랜잭션을 시작한 후에 실제 서비스를 대신 호출한다. 트랜잭션 프록시 덕분에 서비스 계층에는 순수한 비즈니즈 로직만 남길 수 있다.

<br>

### 스프링이 제공하는 트랜잭션 AOP

- 스프링이 제공하는 AOP 기능을 사용하면 프록시를 매우 편리하게 적용할 수 있다.

- 물론 스프링 AOP를 직접 사용해서 트랜잭션을 처리해도 되지만, 트랜잭션은 매우 중요한 기능이고, 전세계 누구나 다 사용하는 기능이다. 스프링은 트랜잭션 AOP를 처리하기 위한 모든 기능을 제공한다. 스프링 부트를 사용하면 트랜잭션 AOP를 처리하기 위해 필요한 스프링 빈들도 자동으로 등록해준다.

- 개발자는 트랜잭션 처리가 필요한 곳에 `@Transactional` 애노테이션만 붙여주면 된다. 스프링의 트랜잭션 AOP는 이 애노테이션을 인식해서 트랜잭션 프록시를 적용해준다.

<BR>

#### ✅ @Transactional

`org.springframework.transaction.annotation.Transactional`

> 참고
> 스프링 AOP를 적용하려면 어드바이저, 포인트컷, 어드바이스가 필요하다. 스프링은 트랜잭션 AOP 처리를 위해 다음 클래스를 제공한다. 스프링 부트를 사용하면 해당 빈들은 스프링 컨테이너에 자동으로 등록된다.
>
> 어드바이저: BeanFactoryTransactionAttributeSourceAdvisor
> 포인트컷: TransactionAttributeSourcePointcut
> 어드바이스: TransactionInterceptor


<Br><Br>

### 트랜잭션 AOP 적용


```
@Slf4j
public class MemberServiceV3_3 {
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }

    private void bizLogic( String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById( fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney()- money);
        validation(toMember);
        memberRepository.update(toId, fromMember.getMoney()+ money);
    }


    private static void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}

```

- 순수한 비즈니스 로직만 남기고, 트랜잭션 관련 코드는 모두 제거했다.
- 스프링이 제공하는 트랜잭션 AOP를 적용하기 위해 @Transactional 애노테이션을 추가했다.
- @Transactional 애노테이션은 메서드에 붙여도 되고, 클래스에 붙여도 된다. 클래스에 붙이면 외부에서 호출 가능한 public 메서드가 AOP 적용 대상이 된다.

<Br>

#### @Transactional 테스트 코드

```
/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_3Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberE";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_3 memberService;

    @TestConfiguration
    static class TestConfig{
        @Bean
        DataSource dataSource(){
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager(){
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3(){
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3(){
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    public void accountTransfer() throws Exception{
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        memberService.accountTransfer(memberA.getMemberId(),memberB.getMemberId(),2000);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    public void accountTransferEx() throws Exception{
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEX = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEX);

        //when 예외가 터져야 함
        assertThatThrownBy(()->memberService.accountTransfer(memberA.getMemberId(),memberEX.getMemberId(),2000) )
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}
```

- @SpringBootTest : 스프링 AOP를 적용하려면 스프링 컨테이너가 필요하다. 이 애노테이션이 있으면 테스트시 스프링 부트를 통해 스프링 컨테이너를 생성한다. 그리고 테스트에서 @Autowired 등을 통해 스프링 컨테이너가 관리하는 빈들을 사용할 수 있다.

- @TestConfiguration : 테스트 안에서 내부 설정 클래스를 만들어서 사용하면서 이 에노테이션을 붙이면, 
스프링 부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있
다.
- TestConfig
    - DataSource 스프링에서 기본으로 사용할 데이터소스를 스프링 빈으로 등록한다. 추가로 트랜잭션 매니저에서도 사용한다.
    - DataSourceTransactionManager 트랜잭션 매니저를 스프링 빈으로 등록한다.
        - 스프링이 제공하는 트랜잭션 AOP는 스프링 빈에 등록된 트랜잭션 매니저를 찾아서 사용하기 때문에 트랜잭션 매니저를 스프링 빈으로 등록해두어야 한다.


<br><Br>

### 트랜잭션 AOP 정리 


![image](https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/a979aed9-e3f2-46da-9437-b09277f74692)



####  ✅ 선언적 트랜잭션 관리 vs 프로그래밍 방식 트랜잭션 관리

- 선언적 트랜잭션 관리(Declarative Transaction Management)

    - @Transactional 애노테이션 하나만 선언해서 매우 편리하게 트랜잭션을 적용하는 것을 선언적 트랜잭션 관리라 한다.

    - 선언적 트랜잭션 관리는 과거 XML에 설정하기도 했다. 이름 그대로 해당 로직에 트랜잭션을 적용하겠다 라고 어딘가에 선언하기만 하면 트랜잭션이 적용되는 방식이다.

<BR>

- 프로그래밍 방식의 트랜잭션 관리(programmatic transaction management)
    - 트랜잭션 매니저 또는 트랜잭션 템플릿 등을 사용해서 트랜잭션 관련 코드를 직접 작성하는 것을 프로그래밍 방식의 트랜잭션 관리라 한다.

<BR>

- 선언적 트랜잭션 관리가 프로그래밍 방식에 비해서 훨씬 간편하고 실용적이기 때문에 실무에서는 대부분 선언적 트랜잭션 관리를 사용한다.

- 프로그래밍 방식의 트랜잭션 관리는 스프링 컨테이너나 스프링 AOP 기술 없이 간단히 사용할 수 있지만 실무에서는 대부분 스프링 컨테이너와 스프링 AOP를 사용하기 때문에 거의 사용되지 않는다.

- 프로그래밍 방식 트랜잭션 관리는 테스트 시에 가끔 사용될 때는 있다.


<bR>

#### 정리

- 스프링이 제공하는 선언적 트랜잭션 관리 덕분에 드디어 트랜잭션 관련 코드를 순수한 비즈니스 로직에서 제거할 수 있었다.
- 개발자는 트랜잭션이 필요한 곳에 @Transactional 애노테이션 하나만 추가하면 된다. 나머지는 스프링 트랜잭션 AOP가 자동으로 처리해준다.
- @Transactional 애노테이션의 자세한 사용법은 뒤에서 설명한다. 지금은 전체 구조를 이해하는데 초점을 맞추자.


<BR><bR>

### 스프링 부트의 자동 리소스 등록

스프링 부트가 등장하기 이전에는 데이터소스와 트랜잭션 매니저를 개발자가 직접 스프링 빈으로 등록해서 사용했다. 

그런데 스프링 부트로 개발을 시작한 개발자라면 데이터소스나 트랜잭션 매니저를 직접 등록한적이 없을 것이다.


<BR>

#### 데이터소스와 트랜잭션 매니저를 스프링 빈으로 직접 등록
```
@Bean
DataSource dataSource() {
    return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
}
@Bean
PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
}
```

기존에는 이렇게 데이터소스와 트랜잭션 매니저를 직접 스프링 빈으로 등록해야 했다. 그런데 스프링 부트가 나오면서 많은 부분이 자동화되었다. 

<BR>

**데이터 소스 - 자동 등록**
- 스프링 부트는 데이터소스( DataSource )를 스프링 빈에 자동으로 등록한다.
- 자동으로 등록되는 스프링 빈 이름: dataSource
- 참고로 개발자가 직접 데이터소스를 빈으로 등록하면 스프링 부트는 데이터소스를 자동으로 등록하지 않는다.


이때 스프링 부트는 다음과 같이 application.properties 에 있는 속성을 사용해서 DataSource 를 생성한다. 그리고 스프링 빈에 등록한다


```
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
spring.datasource.username=sa
spring.datasource.password=
```
- 스프링 부트가 기본으로 생성하는 데이터소스는 커넥션풀을 제공하는 HikariDataSource 이다. 커넥션풀과 관련된 설정도 application.properties 를 통해서 지정할 수 있다.

- spring.datasource.url 속성이 없으면 내장 데이터베이스(메모리 DB)를 생성하려고 시도한다.

<BR>


**트랜잭션 매니저 - 자동 등록**

- 스프링 부트는 적절한 트랜잭션 매니저(PlatformTransactionManager )를 자동으로 스프링 빈에 등록한다.
- 자동으로 등록되는 스프링 빈 이름: transactionManager
- 참고로 개발자가 직접 트랜잭션 매니저를 빈으로 등록하면 스프링 부트는 트랜잭션 매니저를 자동으로 등록하지 않는다.


어떤 트랜잭션 매니저를 선택할지는 현재 등록된 라이브러리를 보고 판단하는데, JDBC를 기술을 사용하면 DataSourceTransactionManager 를 빈으로 등록하고, JPA를 사용하면 JpaTransactionManager 를 빈으로 등록한다. 둘다 사용하는 경우 JpaTransactionManager 를 등록한다. 참고로 JpaTransactionManager 는 DataSourceTransactionManager 가 제공하는 기능도 대부분 지원한다.

<BR>

