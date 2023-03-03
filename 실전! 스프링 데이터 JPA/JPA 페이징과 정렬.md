## 페이징과 정렬

### 순수 JPA 페이징과 정렬


✅ 조건
- 검색 조건: 나이가 10살
- 정렬 조건: 이름으로 내림차순
- 페이징 조건: 첫 번째 페이지, 페이지당 보여줄 데이터는 3건

```
    public List<Member> findByPage(int age, int offset, int limit){
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc")
                .setParameter("age",age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long totalCount(int age){
        return em.createQuery("select count(m) from Member m where m.age = :age ", Long.class)
                .setParameter("age",age)
                .getSingleResult();
    }

```

<br>

### 스프링 데이터 JPA 페이징과 정렬

💡 **페이징과 정렬 파라미터**
- ```org.springframework.data.domain.Sort``` : 정렬 기능
- ```org.springframework.data.domain.Pageable``` : 페이징 기능 (내부에 Sort 포함)

<BR>

💡 **특별한 반환 타입**
- ```org.springframework.data.domain.Page``` : 추가 count 쿼리 결과를 포함하는 페이징
- ```org.springframework.data.domain.Slice``` : 추가 count 쿼리 없이 다음 페이지만 확인 가능
(내부적으로 limit + 1조회)
- List (자바 컬렉션): 추가 count 쿼리 없이 결과만 반환

<br>

```
public interface MemberRepository extends Repository<Member, Long> {
    Page<Member> findByAge(int age, Pageable pageable);
}


@Test
public void page() throws Exception {

    //given

    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    //when

    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,
    "username"));
    Page<Member> page = memberRepository.findByAge(10, pageRequest);

    //then

    List<Member> content = page.getContent(); //조회된 데이터
    assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
    assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
    assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
    assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
    assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
    assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
}
```
- 두 번째 파라미터로 받은 Pageable 은 인터페이스! 따라서 실제 사용할 때는 해당 인터페이스를 구현한
```org.springframework.data.domain.PageRequest``` 객체를 사용

- PageRequest 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를
입력. 여기에 추가로 정렬 정보도 파라미터로 사용 가능 (참고로 페이지는 0부터 시작)

> 주의: Page는 1부터 시작이 아니라 0부터 시작이다.

<br>

📌 **Page 인터페이스**

```
public interface Page<T> extends Slice<T> {
    int getTotalPages(); //전체 페이지 수
    long getTotalElements(); //전체 데이터 수
    <U> Page<U> map(Function<? super T, ? extends U> converter); //변환기
}
```
📌 **Slice 인터페이스**
```
public interface Slice<T> extends Streamable<T> {
    int getNumber(); //현재 페이지
    int getSize(); //페이지 크기
    int getNumberOfElements(); //현재 페이지에 나올 데이터 수
    List<T> getContent(); //조회된 데이터
    boolean hasContent(); //조회된 데이터 존재 여부
    Sort getSort(); //정렬 정보
    boolean isFirst(); //현재 페이지가 첫 페이지 인지 여부
    boolean isLast(); //현재 페이지가 마지막 페이지 인지 여부
    boolean hasNext(); //다음 페이지 여부
    boolean hasPrevious(); //이전 페이지 여부
    Pageable getPageable(); //페이지 요청 정보
    Pageable nextPageable(); //다음 페이지 객체
    Pageable previousPageable();//이전 페이지 객체
    <U> Slice<U> map(Function<? super T, ? extends U> converter); //변환기
}
```


<Br>

✅ **count 쿼리를 분리할 수 있음**
```
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m") // count는 조인 안하게 분리해서 작성
    Page<Member> findByAge(int age, Pageable pageable);
```
> 참고: 전체 count 쿼리는 매우 무겁다.

<br>

✅ **페이지 유지하며 엔티티를 DTO로 변환하기**
```
    Page<Member> page = memberRepository.findByAge(age, pageRequest); // totalCount도 한번에 가능!

    Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
    // 이제 api로 반환 가능!!
```
<br><br>