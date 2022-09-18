# HTTP 메소드 활용

## 클라이언트에서 서버로 데이터 전송

### 🔎 데이터 전달 방식

<bR>

1. 쿼리 파라미터를 통한 데이터 전송

    - GET
    - 주로 정렬 필터(검색어)

<BR>

2. 메시지 바디를 통한 데이터 전송

    - POST, PUT, PATCH
    - 회원 가입, 상품 주문, 리소스 등록, 리소스 변경


### 🔎 4가지 상황

- 정적 데이터 조회

    - 이미지, 정적 텍스트 문서
<img width="628" alt="image" src="https://user-images.githubusercontent.com/81572478/190911193-36ac75f0-dbf7-401f-9a61-a54121785a3c.png">

        : 단순히 GET 경로(URI)오면 이미지 리소스 만들어서 전송(쿼리 파라미터 사용 X)

    ➡️ 조회는 GET 사용, 정적 데이터는 일반적으로 쿼리 파라미터 없이 리소스 경로로 단순히 조회 가능

<BR>

- 동적 데이터 조회

    - 주로 검색, 게시판 목록에서 정렬 필터(검색어)
    <img width="622" alt="image" src="https://user-images.githubusercontent.com/81572478/190911254-09322d66-fb8d-4f05-9d29-e47fbb2eedfa.png">

        ➡️ 조회 조건을 줄여주는 필터, 조회 결과를 정렬하는 정렬 조건에 주로 사용 (q=hello&hl=ko)

        조회는 GET 사용, GET은 쿼리 파라미터 사용해 데이터 전달

<BR>

- HTML Form을 통한 데이터 전송

    - 회원 가입, 상품 주문, 데이터 변경
    <img width="638" alt="image" src="https://user-images.githubusercontent.com/81572478/190911397-67ae0321-8e59-4cf4-8abc-d330742c10b8.png">

        : form 의 submit을 누르면 form 태그 정보 바탕으로 HTTP 메시지 생성!(key=value, 쿼리 파라미터와 유사) + name이 키, 입력한 값이 값

        <img width="631" alt="image" src="https://user-images.githubusercontent.com/81572478/190911553-2c99b1ee-dd35-415c-9539-968d44c9b78b.png">

        : POST면 메시지 BODY에, GET이면 쿼리로 정보 입력됨(GET 전송은 조회만!!)
    <BR><BR>

    ➕ 파일전송할때! multipart/form-data

    <img width="638" alt="image" src="https://user-images.githubusercontent.com/81572478/190911659-7c63ada5-2b47-460e-9859-3f1f9b4ec60b.png">

    : byte로 되어있는 파일도 전송해야 한다면, boundary=----XXX가 정보의 경계를 생성함

    = 여러 종류의 데이터 전송가능!


<BR>

- HTTP API를 통한 데이터 전송

    - 회원 가입, 상품 주문, 데이터 변경
    - 서버 to 서버, 앱 클라이언트, 웹 클라이언트(Ajax)

        <img width="516" alt="image" src="https://user-images.githubusercontent.com/81572478/190912006-d5c06197-3904-40f3-9ac7-99caa089dc9a.png">


    1. 서버 to 서버 : 백엔드 시스템 통신
    2. 앱 클라이언트 : 아이폰, 안드로이드
    3. 웹 클라이언트 : HTML에서 Form 전송 대신 자바 스크립트를 통한 통신에 사용(Ajax)
        ex) React, VueJs같은 웹 클라이언트와 API 통신

    4. POST, PUT, PATCH : 메시지 바디를 통해 데이터 전송
    5. GET : 조회, 쿼리 파라미터로 데이터 전달
    6. Content-Type: application/json을 주로 사용(사실상 표준)