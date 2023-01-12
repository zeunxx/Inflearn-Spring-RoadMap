
### 데이터 베이스 방언
- JPA는 특정 데이터베이스에 종속 x
- 각각의 데이터베이스가 제공하는 SQL 문법과 함수는 조금씩 다름
    - 가변문자, 문자열을 자르는 함수, 페이징 등 
    
    ➡️ 이런 것을 JPA가 맞춰줌
- 방언: SQL 표준을 지키지 않는 특정 데이터베이스만의 고유한 기능

<img width="390" alt="image" src="https://user-images.githubusercontent.com/81572478/211987557-1d6c1beb-7038-43fb-9181-551e75ffc3c4.png">


<br><Br>

### 객체와 테이블 생성하고 매핑하기
- 개발자는 SQL을 쿼리를 작성하지 않고 테이블에 entity manager 통해 insert 할 수 있음

```
@Entity // JPA가 관리할 객체
public class Member { 
 @Id //  데이터베이스 PK와 매핑
 private Long id; 
 private String name; 
 //Getter, Setter … 
}
```
```
public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin(); // database transaction 시작

        try{
            // 저장
            Member member = new Member();
            member.setId(1L);
            member.setName("HelloA");
            em.persist(member); // db에 저장

            // 조회
            Member findMember = em.find(Member.class, 1L);
            System.out.println("findMember = " + findMember.getName());

            // 삭제
            em.remove(findMember);

            // 수정
            Member findMember = em.find(Member.class, 1L); // 조회
            findMember.setName("HelloJPA"); // 그냥 엔티티의 멤버변수만 바꾸면 JPA가 수정해줌
            // 트랜잭션 커밋 직전에 바뀐 부분 찾아서 UPDATE 쿼리 만들어서 날리고 커밋됨


            tx.commit(); // transaction commit

            // 문제가 발생하지 않으면 커밋
        }catch (Exception e){
            tx.rollback();
            
            // 문제가 발생하면 롤백
        }finally {
            em.close();
            // 반드시 사용후 엔티티 매니저 닫기 == db connection 종료
        }
        emf.close();
    }
```
<br>

### 📌 주의
- 엔티티 매니저 팩토리는 하나만 생성해서 애플리케이션 전체에
서 공유
- 엔티티 매니저는 쓰레드간에 공유X (사용하고 버려야 한다). 
- **JPA의 모든 데이터 변경은 트랜잭션 안에서 실행**

<BR>

### 조회할때 조건문을 넣고 싶다면? 
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검
색 조건이 포함된 SQL이 필요

➡️ **JPQL** 사용
- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, 
HAVING, JOIN 지원
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X
- JPQL은 **엔티티 객체**를 대상으로 쿼리
- SQL은 데이터베이스 **테이블**을 대상으로 쿼리