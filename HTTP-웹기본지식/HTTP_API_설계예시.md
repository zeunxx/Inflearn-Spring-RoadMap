# HTTP API 설계 예시

## HTTP API- 컬렉션

### 회원관리 시스템 API 설계 : POST 기반 등록

- 회원 목록 /members → GET
- 회원 등록 /members → POST
- 회원 조회 /members/{id} → GET
- 회원 수정 /members/{id} → PATCH, PUT, POST
- 회원 삭제 /members/{id} → DELETE


<BR>

🔎 POST : 신규 자원 등록 특징

1. 클라이언트는 등록될 리소스의 URI를 모름

    - 회원 등록 POST /members
2. ```서버```가 새로 등록된 리소스 URI를 생성해줌

    - HTTP/1.1 201 Created
    
        Location: **/members/100** 
3. 컬렉션(Collection)

    - 서버가 관리하는 리소스 디렉토리
    - 서버가 리소스의 URI를 생성하고 관리
    - 여기서 컬렉션은 /members

<BR><BR>


## HTTP API - 스토어

### 파일 관리 시스템 API 설계 : PUT 기반 등록

- 파일 목록 /files → GET
- 파일 조회 /files/{filename} → GET
- 파일 등록 /files/{filename} → PUT
    
    : 기존에 없으면 새로 생성, 있으면 덮어버림

- 파일 삭제 /files/{filename} → DELETE
- 파일 대량 등록 /files → POST

<BR>

🔎 PUT : 신규 자원 등록 특징
1. 클라이언트가 리소스 URI를 알고 있어야 함
    - 파일 등록 PUT /files/{filename} 
    - PUT /files/star.jpg

2. ```클라이언트```가 직접 리소스의 URI를 지정
3. 스토어(Store)
    - 클라이언트가 관리하는 리소스 저장소
    - 클라이언트가 리소스의 URI를 알고 관리
    - 여기서 스토어는 /files


<BR><BR>

## HTML FORM 사용

1. GET, POST만 지원하므로 제약이 있음
2. AJAX같은 기술을 사용해 해결 가능!
    - 여기서는 순수 HTML, HTML FORM 이야기


- 회원 목록 /members → GET
- 회원 등록 폼 /members/new → GET
- 회원 등록 /members/new, /members → POST
- 회원 조회 /members/{id} → GET
- 회원 수정 폼 /members/{id}/edit → GET
- 회원 수정 /members/{id}/edit, /members/{id} → POST
- 회원 삭제 /members/{id}/delete → POST

<BR>


🔎 컨트롤 URI

1. GET,POST만 지원하므로 제약이 있음
2. 이런 제약을 해결하기 위해 **동사로 된 리소스 경로** 사용
3. POST의 /new, /edit, /delete가 컨트롤 URI
4. HTTP메소드로 해결하기 애매한 경우 사용(HTTP API 포함)

<BR><bR>

<img width="475" alt="image" src="https://user-images.githubusercontent.com/81572478/190913823-c740bcb8-07f1-4051-95b6-6b0f2d7fbce1.png">
