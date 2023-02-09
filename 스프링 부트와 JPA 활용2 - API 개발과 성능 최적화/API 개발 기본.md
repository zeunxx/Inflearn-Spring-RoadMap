
## 회원 등록 API
### 등록 V1 : 엔티티를 RequestBody에 직접 매핑

```
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;
        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

```

✅ 문제점
- 엔티티에 프레젠테이션 계층을 위한 로직이 추가됨
- 엔티티에 API 검증을 위한 로직이 들어감(@NotEmpty 등)
- 실무에서는 회원엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기 어려움
- 엔티티가 변경되면 API 스펙이 변함

➡️ 결론
    : API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받음

<BR>

###  등록 V2: 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받음

```
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest createMemberRequest){

        Member member = new Member();
        member.setName(createMemberRequest.getName());
        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }
```
- CreateMemberRequest를 Member 엔티티 대신에 RequestBody와 매핑
- 엔티티와 프레젠테이션 계층을 위한 로직 분리!
- 엔티티와 API 스펙을 명확하게 분리
- 엔티티가 변해도 API 스펙이 변하지 않음

> 참고: 실무에서는 엔티티를 API 스펙에 노출하면 안됨!


<BR><BR>

## 회원 수정 API


```
    /**
     * 회원 수정 API
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;

    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }
```

- 회원 수정도 DTO를 요청 파라미터에 매핑
- 변경 감지를 사용해서 데이터를 수정



<br><Br>

## 회원 조회 API
### 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출

```
@GetMapping("/api/v1/members")
public List<Member> membersV1() {
    return memberService.findMembers();
}
```

✅ 문제점
- 엔티티에 프레젠테이션 계층을 위한 로직이 추가됨 
- 기본적으로 엔티티의 모든 값이 노출됨
- 응답 스펙을 맞추기 위해 로직이 추가 됨(@JsonIgnore 등)
- 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어려움
- 엔티티가 변경되면 API 스펙도 변함!
- 추가로 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기도 어려움 (별도의 Result 클래스 생성으로 해결)

<br>

> 참고: 엔티티를 외부에 노출하지 마세요!
실무에서는 member 엔티티의 데이터가 필요한 API가 계속 증가함. 어떤 API는 name필드가 필요하지만, 어떤 API는 필요없을 수 있음. 결론적으로 엔티티 대신 API 스펙에 맞는 별도의 DTO를 노출해야 함!

<br>

### 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환

```
/**
 * 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
 */
 @GetMapping("/api/v2/members")
 public Result membersV2() {
    List<Member> findMembers = memberService.findMembers();

    //엔티티 -> DTO 변환
    List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
    return new Result(collect);
 }


 @Data
 @AllArgsConstructor
 static class Result<T> {
    private T data;
 }
 @Data
 @AllArgsConstructor
 static class MemberDto {
    private String name;
 }
```
- 엔티티를 DTO로 변환해서 반환!
- 엔티티가 변해도 API 스펙이 변경되지 않음
- 추가로 Result 클래스로 컬렉션을 감싸서 향후 필요한 필드 추가 가능!