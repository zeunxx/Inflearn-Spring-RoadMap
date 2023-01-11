## JPA 소개

### 지금 시대는 객체를 DB에 관리

- 객체를 영구 보관하는 다양한 저장소 ex) RDB, NoSQL, File 등
- 현실적인 대안은 관계형 데이터베이스
- 객체 -> SQL 변환 -> SQL+RDB
<Br>

### 객체와 관계형 데이터베이스와의 차이
<br>

#### 1. 상속

<img width="379" alt="image" src="https://user-images.githubusercontent.com/81572478/211726304-e231eb79-ed95-4990-b60c-612374f612c7.png">

- 관계형 데이터베이스는 상속 관계가 없음(비슷해도 객체와 완전히 동일 x)
- 관계형 DB는 상속관계를 부모/자식 테이블을 만들음 = 슈퍼타입/서브타입 관계

<br>

#### 2. 연관관계

- 객체는 **참조**를 사용: member.getTeam()
- 테이블은 **외래 키**를 사용: JOIN ON m.team_id = t.team_id

    ➡️ 객체를 테이블에 맞추어 모델링

    ```
    class Member {
    String id; //MEMBER_ID 컬럼 사용
    Long teamId; //TEAM_ID FK 컬럼 사용 //**
    String username;//USERNAME 컬럼 사용
    }

    class Team {
    Long id; //TEAM_ID PK 사용
    String name; //NAME 컬럼 사용
    }

    ➡️ 테이블에 맞춘 객체 저장

    INSERT INTO MEMBER(MEMBER_ID, TEAM_ID, USERNAME) VALUES …
    ```
    <bR>

    ✅ 객체다운 모델링
    ```
    class Member {
    String id; //MEMBER_ID 컬럼 사용
    Team team; //참조로 연관관계를 맺는다. //**
    String username;//USERNAME 컬럼 사용
    
    Team getTeam() {
    return team;
    }

    }
    class Team {
    Long id; //TEAM_ID PK 사용
    String name; //NAME 컬럼 사용
    }

    ✅ 객체 모델링 저장 : member.getTeam().getId();

    INSERT INTO MEMBER(MEMBER_ID, TEAM_ID, USERNAME) VALUES …

    ✅ 객체 모델링 조회

    SELECT M.*, T.*
    FROM MEMBER M
    JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID 


    public Member find(String memberId) {
    //SQL 실행 ...
    Member member = new Member();
    //데이터베이스에서 조회한 회원 관련 정보를 모두 입력
    Team team = new Team();
    //데이터베이스에서 조회한 팀 관련 정보를 모두 입력
    //회원과 팀 관계 설정
    member.setTeam(team); //**
    return member;
    }

    
    ```
    - 객체와 DB가 연관관계를 바라보는 방법이 다르므로 매우 복잡

<BR>

- 객체 그래프 탐색
    - 객체는 객체 그래프를 자유롭게 탐색할 수 있어야 함
    - BUT 처음 실행하는 SQL에 따라 탐색범위가 결정됨
    - 엔티티의 신뢰문제 발생

    ```
    class MemberService {
    ...
    public void process() {
    Member member = memberDAO.find(memberId);
    member.getTeam(); //???
    member.getOrder().getDelivery(); // ???
        }
    }

    : getTeam과 getDelivery를 하기위해 find()의 코드를 살펴봐야함 = 신뢰불가
    ```


<br><Br>


## ⭐ JPA ⭐ 

### Java Persistence API

- 자바 진영의 ORM 기술 표준
- ORM : Object-relational mapping(객체 관계 매핑)
    - 객체는 객체대로 설계
    - 관계형 데이터베이스는 관계형 DB대로 설계
    - ORM 프레임워크가 중간에서 매핑


<img width="397" alt="image" src="https://user-images.githubusercontent.com/81572478/211730728-4f8439c9-bb5f-4174-ab02-591413ee1973.png">
<img width="397" alt="image" src="https://user-images.githubusercontent.com/81572478/211730789-67722689-ba7b-48d0-a7d2-5c372ca42586.png">

- Java connection에 저장/조회하듯 한줄의 코드만 작성!

<br>

➡️ 생산성 증가
- 저장: jpa.persist(member)
- 조회: Member member = jpa.find(memberId)
- 수정: member.setName(“변경할 이름”)
- 삭제: jpa.remove(member)

<br>

### 💡JPA와 패러다임의 불일치 해결

#### 1. JPA와 상속

<img width="397" alt="image" src="https://user-images.githubusercontent.com/81572478/211732093-c5bab050-6380-4ae6-88d8-ecdeabb68af1.png">

- 개발자가 하던 매핑과정을 jpa가 대신 해줌
<BR>

#### 2. 신뢰할 수 있는 엔티티 계층

<img width="395" alt="image" src="https://user-images.githubusercontent.com/81572478/211732263-62b1dbb0-e59e-4114-9529-9a6a2fe0cfcf.png">

<bR>

#### 3. JPA와 비교하기

<img width="397" alt="image" src="https://user-images.githubusercontent.com/81572478/211732413-abbb1ca2-dac9-4178-91bf-e56933f3963e.png">

<bR>

#### 4. JPA의 성능 최적화 기능
- 1차 캐시와 동일성 보장
    - 같은 트랜잭션 안에서는 같은 엔티티 반환(약간의 조회성능 향상)
    - DB Isolation Level이 Read Commit이어도 애플리케이션에서 Repeatable Read 보장
    - 같은 멤버를 조회할땐 처음엔 sql 쿼리 날리고 그 다음부터는 캐시에서 꺼냄
- 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)
    - 트랜잭션을 커밋할때까지 INSERT SQL 모음
    - JDBC BATCH SQL 기능을 사용해 한번에 SQL 전송 
    ```
    transaction.begin(); // [트랜잭션] 시작
    em.persist(memberA);
    em.persist(memberB);
    em.persist(memberC);
    //여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.
    //커밋하는 순간 데이터베이스에 INSERT SQL을 모아서 보낸다.
    transaction.commit(); // [트랜잭션] 커밋

    ```
    -> 네트워크 통신비용 감소
- 지연 로딩(lazy loading)
    - 지연 로딩: 객체가 실제 사용될때 로딩
    - 즉시 로딩: JOIN SQL로 **한번에 연관된 객체까지** 미리 조회
    <img width="376" alt="image" src="https://user-images.githubusercontent.com/81572478/211733278-be64c653-95bc-4911-b373-d9b047de8725.png">


