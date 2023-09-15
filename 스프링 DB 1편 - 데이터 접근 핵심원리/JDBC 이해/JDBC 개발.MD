# JDBC 개발

## JDBC 개발 - 등록

```
/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            // 커넥션 획득
            con = getConnection();

            // sql 전달
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 쿼리 실행
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        }finally {
            // 리소스 정리
            close(con, pstmt, null);
        }

    }
    
    private void close(Connection con, Statement stmt, ResultSet rs){
        
        if (rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if(stmt!=null){
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
        if(con!=null){
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private static Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}

```
**커넥션 획득**
getConnection() : 이전에 만들어둔 DBConnectionUtil 를 통해서 데이터베이스 커넥션을 획득한다.


**save() - SQL 전달**
- sql : 데이터베이스에 전달할 SQL을 정의한다. 여기서는 데이터를 등록해야 하므로 insert sql 을 준비했다.
- con.prepareStatement(sql) : 데이터베이스에 전달할 SQL과 파라미터로 전달할 데이터들을 준비한다.
    - sql : insert into member(member_id, money) values(?, ?)"
    - pstmt.setString(1, member.getMemberId()) : SQL의 첫번째 ? 에 값을 지정한다. 문자이므로 setString 을 사용한다.
    - pstmt.setInt(2, member.getMoney()) : SQL의 두번째 ? 에 값을 지정한다. Int 형 숫자이므로 setInt 를 지정한다.
- pstmt.executeUpdate() : Statement 를 통해 준비된 SQL을 커넥션을 통해 실제 데이터베이스에 전달한다. 참고로 executeUpdate() 은 int 를 반환하는데 영향받은 DB row 수를 반환한다. 여기서는 하나의 row를 등록했으므로 1을 반환한다


**리소스 정리**
쿼리를 실행하고 나면 리소스를 정리해야 한다. 여기서는 Connection , PreparedStatement 를 사용했다. 
리소스를 정리할 때는 항상 역순으로 해야한다. Connection 을 먼저 획득하고 Connection 을 통해 PreparedStatement 를 만들었기 때문에 리소스를 반환할 때는 PreparedStatement 를 먼저 종료하고, 
그 다음에 Connection 을 종료하면 된다. 참고로 여기서 사용하지 않은 ResultSet 은 결과를 조회할 때 사용한다. 


> 주의
> 리소스 정리는 꼭! 해주어야 한다. 따라서 예외가 발생하든, 하지 않든 항상 수행되어야 하므로 finally 구문에 주의해서 작성해야한다. 만약 이 부분을 놓치게 되면 커넥션이 끊어지지 않고 계속 유지되는 문제가 발생할 수 있다. 이런 것을 리소스 누수라고 하는데, 결과적으로 커넥션 부족으로 장애가 발생할 수 있다.



## JDBC 개발 - 조희

```
public Member findById(String memberId) throws SQLException {
    String sql = "select * from member where member_id = ?";

    Connection con= null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try{
        con = getConnection();
        pstmt= con.prepareStatement(sql);
        pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if(rs.next()){
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found memberId= "+ memberId);
            }
    }catch (SQLException e){
        log.error("db error", e);
        throw e;
    }finally {
        close(con,pstmt,rs);
    }
}

```

**findById() - 쿼리 실행**

- sql : 데이터 조회를 위한 select SQL을 준비한다.
- rs = pstmt.executeQuery() 데이터를 변경할 때는 executeUpdate() 를 사용하지만, 데이터를 조회할 때는 executeQuery() 를 사용한다. executeQuery() 는 결과를 ResultSet 에 담아서 반환한다



### ResultSet

- ResultSet 은 다음과 같이 생긴 데이터 구조이다. 보통 select 쿼리의 결과가 순서대로 들어간다. 
    - 예를 들어서 select member_id, money 라고 지정하면 member_id , money 라는 이름으로 데이터가 저장된다.
    - 참고로 select * 을 사용하면 테이블의 모든 컬럼을 다 지정한다

- ResultSet 내부에 있는 커서( cursor )를 이동해서 다음 데이터를 조회할 수 있다.

- rs.next() : 이것을 호출하면 커서가 다음으로 이동한다. 참고로 최초의 커서는 데이터를 가리키고 있지 않기 때문에 rs.next() 를 최초 한번은 호출해야 데이터를 조회할 수 있다.
    - rs.next() 의 결과가 true 면 커서의 이동 결과 데이터가 있다는 뜻이다.
    - rs.next() 의 결과가 false 면 더이상 커서가 가리키는 데이터가 없다는 뜻이다.

- rs.getString("member_id") : 현재 커서가 가리키고 있는 위치의 member_id 데이터를 String 타입으로 반환한다.

- rs.getInt("money") : 현재 커서가 가리키고 있는 위치의 money 데이터를 int 타입으로 반환한다.


#### ResultSet 결과 예시

<img width="379" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/380b425e-727e-49e6-8a6c-cc16123d9f17">

- 참고로 이 ResultSet 의 결과 예시는 회원이 2명 조회되는 경우이다.

- 1-1 에서 rs.next() 를 호출한다.
- 1-2 의 결과로 cursor 가 다음으로 이동한다. 이 경우 cursor 가 가리키는 데이터가 있으므로 true 를 반환한다.
- 2-1 에서 rs.next() 를 호출한다.
- 2-2 의 결과로 cursor 가 다음으로 이동한다. 이 경우 cursor 가 가리키는 데이터가 있으므로 true 를 반환한다.
- 3-1 에서 rs.next() 를 호출한다.
- 3-2 의 결과로 cursor 가 다음으로 이동한다. 이 경우 cursor가 가리키는 데이터가 없으므로 false 를 반환한다.


findById() 에서는 회원 하나를 조회하는 것이 목적이다. 따라서 조회 결과가 항상 1건이므로 while 대신에 if 를 사용한다. 

다음 SQL을 보면 PK인 member_id 를 항상 지정하는 것을 확인할 수 있다.

`SQL: select * from member where member_id = ?`



## JDBC 개발 - 수정, 삭제

수정과 삭제는 등록과 비슷하다. 등록, 수정, 삭제처럼 데이터를 변경하는 쿼리는 executeUpdate() 를 사용하면 된다.

```
public void update(String memberId, int money) throws SQLException {
    String sql = "update member set money = ? where member_id = ?";

    Connection con = null;
    PreparedStatement pstmt = null;

    try{
        con = getConnection();
        pstmt = con.prepareStatement(sql);
        pstmt.setInt(1, money);
        pstmt.setString(2, memberId);
        int resultSize = pstmt.executeUpdate();
        log.info("resultSize = {}",resultSize);
    } catch (SQLException e) {
        log.error("db error", e);
        throw e;
    }finally {
        close(con, pstmt, null);
    }
}

public void delete(String memberId) throws SQLException {
    String sql = "delete from member  where member_id = ?";

    Connection con = null;
    PreparedStatement pstmt = null;

    try{
        con = getConnection();
        pstmt = con.prepareStatement(sql);
        pstmt.setString(1, memberId);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        log.error("db error", e);
        throw e;
    }finally {
        close(con, pstmt, null);
    }
}
```
- 회원을 삭제한 다음 findById() 를 통해서 조회한다. 회원이 없기 때문에 NoSuchElementException 이 발생한다.  
- assertThatThrownBy 는 해당 예외가 발생해야 검증에 성공한다.
