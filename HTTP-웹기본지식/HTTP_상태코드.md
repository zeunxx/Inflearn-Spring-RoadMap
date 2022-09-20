# HTTP 상태코드

## HTTP 상태코드
: 클라이언트가 보낸 요청의 처리 상태를 응답에서 알려주는 기능

- 1xx(Informational): 요청이 수신되어 처리 중(거의 사용 x)
- 2xx(Successful): 요청 정상 처리
- 3xx(Redirection): 요청을 완료하려면 추가 행동이 필요
- 4xx(Client Error): 클라이언트 오류, 잘못된 문법 등으로 서버가 요청 수행 불가
- 5xx(Server Error): 서버 오류, 서버가 정상 요청 처리 못함

<br><Br>

## 2xx - 성공

: 클라이언트의 요청을 성공적으로 처리

- 200 OK

    <img width="570" alt="image" src="https://user-images.githubusercontent.com/81572478/191285514-7fff5065-44ca-45dc-ba56-ac20f6f1790b.png">

- 201 Created
<img width="653" alt="image" src="https://user-images.githubusercontent.com/81572478/191285627-25279ea5-53d0-4244-b25f-30997fa6859d.png">

- 202 Accepted
: 요청이 접소되었으나 처리가 완료되지 않음

    - 배치 처리 등에서 사용

<br>


- 204 No Content
: 서버가 요청을 성공적으로 수행했지만, 응답 페이로드 본문에 보낼 데이터 x

    - 웹 문서 편집기 save 버튼 
        - save 버튼의 결과로 아무 내용 없어도 될 때
        - save 버튼 눌러도 같은 화면 유지
        
        = 결과 내용이 없어도 204메시지(2xx)만으로 성공 인식

<Br>
<br>

## 3xx - 리다이렉션

: 요청을 완료하기 위해 유저 에이전트(주로 웹 브라우저)의 추가 조치 필요

<Br>

### 🔎 리다이렉션 
: 웹 브라우저는 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동 이동(리다이렉트)

<img width="556" alt="image" src="https://user-images.githubusercontent.com/81572478/191286853-e974475c-2c0e-426e-ae61-1018d1be754d.png">

<br>

📌 리다이렉션 종류

    1. 영구 리다이렉션 : 특정 리소스의 URI가 영구적으로 이동

        ex) /members → /users

    2. 일시 리다이렉션 : 일시적인 변경(PRG: Post/Redirect/Get)

        ex) 주문 완료 후 주문 내역 화면으로 이동

    3. 특수 리다이렉션: 결과 대신 캐시 사용

<br>

- 영구 리다이렉션: 301, 308

    - 리소스의 URI가 영구적으로 이동
    - 원래의 URI를 사용X, 검색 엔진 등에서도 변경 인지

    - **301 Moved Permanently : 리다이렉트시 요청 메소드가 GET으로 변하고, 본문이 제거될 수 있음(MAY)**

    <img width="568" alt="image" src="https://user-images.githubusercontent.com/81572478/191288756-118e1941-564c-4d6b-9e00-4a85e25ef58f.png">

    <BR>

    - **308 Permanent Redirect**
    : 301과 기능 같음, **리다이렉트시 요청 메소드와 본문 유지(처음 POST 보내면 리다이렉트도 유지)**

    <img width="557" alt="image" src="https://user-images.githubusercontent.com/81572478/191288975-467ead60-657f-4e05-b537-3a72da353c2a.png">

    <bR>

- 일시적인 리다이렉션: 302, 307, 303

    - 리소스의 URI가 일시적으로 변경
    - 따라서 검색 엔진 등에서 URL을 변경하면 안됨

    <BR>

    - **302 Found : 리다이렉트 요청 메소드가 GET으로 변하고, 본문 제거(MAY)**

    - **307 Temporary Redirect** : 302와 기능 같음, **리다이렉트 요청 메서드와 본문유지(요청 메소드를 변경하면 안됨)**

    - **303 See Other** : 302와 기능 같음, **리다이렉트시 요청 메소드가 GET으로 변경**

<BR>

### PRG: Post/Redirect/Get 
(일시적인 리다이렉션 예시)

- POST로 주문 후에 웹 브라우저를 새로고침하면? 
    - 새로고침은 다시 요청 = 중복 주문이 될 수 있음

<BR>

<img width="663" alt="image" src="https://user-images.githubusercontent.com/81572478/191291281-368adf20-5aa5-4a20-9d4b-fa45b4880073.png">

: 새로고침(=마지막 요청을 재요청)으로 인해 중복주문 들어감

<BR>

➡️ PRG 사용

- POST로 주문 후에 새로고침으로 인한 중복 주문 방지
- POST로 주문 후에 주문 결과 화면을 GET 메서드로 리다이렉트
- 새로고침해도 결과 화면을 GET으로 조회
- 중복 주문 대신 결과 화면만 GET으로 다시 요청

<img width="672" alt="image" src="https://user-images.githubusercontent.com/81572478/191292200-a6fe40fa-6cdb-46e2-8ef2-11fdd11361b6.png">
: POST의 응답으로 302/303 응답을 줌 → 리다이렉트하는데 GET 메소드 변경

<BR>


- PRG 이후 리다이렉트
    - URL이 이미 POST → GET으로 리다이렉트 됨
    - 새로고침해도 GET으로 결과 화면만 조회


<bR>

➡️ 307,303을 권장하지만 대부분의 애플리케이션 라이브러리들이 302를 기본값으로 사용! 

∴ 자동 리다이렉션시 GET으로 변해도 되면 **302** 사용!

<BR>

➕ 304 Not Modified
- 캐시를 목적으로 사용
- 클라이언트에게 리소스가 수정되지 않음을 알려줌, 클라이언트는 로컬 PC에 저장된 캐시 재사용(캐시로 리다이렉트)
- 304 응답은 응답에 메시지 바디 포함X(로컬 캐시를 사용해야 해서)
- 조건부 GET, HEAD 요청시 사용

<BR><bR>

## 4xx - 클라이언트 오류

- 클라이언트의 요청에 잘못된 문법등으로 서버가 요청을 수행할 수 없음

- 오류의 원인 : 클라이언트

    ⭐ 클라이언트가 이미 잘못된 요청/데이터를 보내므로, **똑같이 재시도하면 실패** 


<br><BR>

- 400 Bad Request : 클라이언트가 잘못된 요청을 해서 서버가 요청을 처리할 수 없음

    - 요청 구문/메시지 등등 오류
    - 클라이언트는 요청 내용을 다시 검토하고 보내야함
        
        ex) 요청 파라미터가 잘못, API 스펙이 맞지않을때

<br>

- 401 Unauthorized : 클라이언트가 해당 리소스에 대한 인증이 필요함

    - 인증(Authentication)되지 않음 (not 인가)
    - 401 오류 발생시 응답에 WWW-Authenticate 헤더와 함께 인증방법 설명

    ➕ 참고
        
        - 인증(Authentication) : 본인이 누구인지 확인(로그인)

        - 인가(Authorization) : 권한부여(ADMIN 권한 처럼 특정 리소스에 접근할 수 있는 권한이 있어야 인가 있음)

        - 오류메시지가 Unauthorized이지만 인증되지 않음

<Br>

403 Forbidden : 서버가 요청을 이해했지만 승인을 거부함

- 주로 인증 자격 증명은 있지만, 접근 권한이 불충분한 경우

    ex) admin 등급이 아닌 사용자가 로그인은 했지만, admin 등급의 리소스에 접근하는 경우 

<br>

- 404 Not Found : 요청 리소스를 찾을 수 없음

    - 요청 리소스가 서버에 없음
    - 또는 클라이언트가 권한이 부족한 리소스에 접근할 때 해당 리소스를 숨기고 싶을 때

<br>
<Br>

## 5xx - 서버 오류

- 서버 문제로 오류 발생
- 서버에 문제가 있기때문에 재시도 하면 성공할 수도 있음(복구 되거나 등)

<br><Br>

- 500 Internal Server Error : 서버 내부 문제로 오류 발생(애매하면 500 오류)

<br>

- 503 Service Unavailable : 서비스 이용불가

    - 서버가 일시적인 과부하/예정된 작업으로 잠시 요청을 처리할 수 없음
    - Retry-After 헤더 필드로 얼마뒤에 복구되는지 보낼 수 있음

<Br>

