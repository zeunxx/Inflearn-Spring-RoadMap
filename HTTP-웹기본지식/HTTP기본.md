# HTTP 기본

## HTTP

: 현재는 HTTP 메시지에 모든 것을 전송!

- html, text
- image, 음성, 영상, 파일
- json,  xml(api)
- 거의 모든 형태의 데이터 전송 가능! 서버간 데이터를 주고 받을 때도 대부분 HTTP 사용!

➕ 기반 프로토콜
- TCP : HTTP/1.1, HTTP/2
- UDP : HTTP/3
- 현재 HTTP/1.1 주로 사용! 2,3도 점점 증가


<BR>

### 🔎 HTTP 특징
- 클라이언트 서버 구조
- 무상태 프로토콜(stateless), 비연결성
- HTTP 메시지
- 단순, 확장가능

<BR><bR>

## 클라이언트 서버 구조

- Request Response 구조
- 클라이언트는 서버에 요청 전송, 응답 대기
- 서버가 요청에 대한 결과 만들어 응답

<img width="476" alt="image" src="https://user-images.githubusercontent.com/81572478/190576902-da115c29-da52-4384-8550-1d436bc3f31d.png">

<BR>

💡 비지니스 로직 등 중요한 데이터는 서버에 집중, 클라이언트는 UI, 사용성 등에 집중! 
    
    = 클라이언트, 서버가 각각 독립적으로 진화 가능!

<BR><bR>

## 무상태 프로토콜(Stateless)

- 서버가 클라이언트의 상태 보존 x
- 장점 : 서버 확장성 높음
- 단점 : 클라이언트가 추가 데이터 전송
<br>

<img width="462" alt="image" src="https://user-images.githubusercontent.com/81572478/190577733-ef5d19b3-bbc0-47a8-9316-861d39023cac.png">

➡️ 중간에 점원이 바뀌면 처음부터 다시 설명!

<Br>

<img width="534" alt="image" src="https://user-images.githubusercontent.com/81572478/190577914-be14bedb-1b29-47b5-84d1-70507004aa14.png">

➡️ 중간에 점원이 바뀌어도 결제 바로 가능!

<br>


🔎 stateful, stateless 차이

- 상태 유지 : 중간에 다른 점원으로 바뀌면 안됨! = **항상 같은 서버가 유지되어야 함**

    (중간에 다른 점원으로 바뀔 때 상태 정보를 다른 점원에게 미리 알려줘야 함)

- 무상태 : 중간에 다른 점원으로 바뀌어도 상관 없음
    - 갑자기 고객이 증가해도 점원을 대거 투입할 수 있음
   = **갑자기 클라이언트 요청이 증가해도 서버를 대거 투입 가능!**

   - 무상태는 응답 서버를 쉽게 바꿀 수 있다 = 무한한 서버 증설 가능

<img width="589" alt="image" src="https://user-images.githubusercontent.com/81572478/190579899-91ab4db8-35fa-4d14-9177-2e5da11cd050.png">
중간에 서버가 장애나면 ? 다른 서버가 응답하면 됨!!

<br>

∴ 무상태 서버는 스케일 아웃 가능 = 수평 확장 유리

<img width="584" alt="image" src="https://user-images.githubusercontent.com/81572478/190580157-d5ace1ed-c6e0-40fc-82ec-7629b165277b.png">

<br>

🔎 stateless의 실무한계

- 모든 것을 무상태로 설계 할 수 있는 경우도 있고 없는 경우도 o
- 무상태 ex) 로그인이 필요 없는 단순한 서비스 소개 화면
- 상태 유지 ex) 로그인
    - 로그인한 사용자의 경우 로그인 했다는 상태를 서버에 유지
    - 일반적으로 브라우저 **쿠키**와 서버 **세션**등을 사용해서 상태 유지
    - 상태 유지는 최소한만 사용!


<br><br>

## 비연결성(connectionless)

<br>

- 연결을 유지하는 모델 : 서버는 연결을 계속 유지, 서버 자원 소모

<img width="575" alt="image" src="https://user-images.githubusercontent.com/81572478/190581020-fd44814d-cb6f-4f35-be16-b180ca3e6a6b.png">


- 연결을 유지하지 않는 모델 : 서버는 연결 유지 x, 최소한의 자원 유지

<img width="515" alt="image" src="https://user-images.githubusercontent.com/81572478/190581149-16420433-1a26-4e2d-aff1-957e9d652471.png">
(필요한 것만 주고받고 연결 끊음)

<br>

### 🔎 비연결성

- HTTP는 기본이 연결을 유지하지 **않는** 모델
- 일반적으로 초 단위 이하의 빠른 속도로 응답
    - 1시간동안 수천명이 서비스를 사용해도 실제 서버에서 동시에 처리하는 요청은 수십개 이하로 작음!

- 서버 자원을 매우 효율적으로 사용 가능

#### 단점

- TCP/IP 연결을 새로 맺어야 함 = 3 way handshake 시간 추가
- 웹 브라우저로 사이트 요청시 html/js/css/etc img등 수많은 자원이 함께 다운로드
- 지금은 **HTTP 지속 연결**(Persistent Conections)로 문제 해결!

<Br>

<img width="491" alt="image" src="https://user-images.githubusercontent.com/81572478/190582157-e36c6686-e53a-4039-ae1c-2b60dd47714a.png">
<br>
<img width="547" alt="image" src="https://user-images.githubusercontent.com/81572478/190582228-2f99d141-c649-42a6-8505-289af3cc1a85.png">
(지속 연결은 어느정도 연결 유지!)

<Br>

💡 stateless를 기억하자!
- 같은 시간에 딱 맞추어 발생하는 대용량 트래픽!

    ex) 선착순 이벤트, 명절 ktx 예약 ..

<br><Br>

## HTTP 메시지

<img width="635" alt="image" src="https://user-images.githubusercontent.com/81572478/190582902-0f8a3b49-7454-449d-9415-18e2ee78fb09.png">

<BR>

- start line
    - request line : 요청 메시지
        - HTTP 메서드 (GET:조회)
        - 요청 대상(/search?q=hello&hl=ko) : 절대경로("/")로 시작하는 경로
        - HTTP version 

    - status line : 응답 메시지
        - HTTP 버전
        - HTTP 상태코드 : 요청 성공, 실패 나타냄
        - 이유 문구 : 짧은 상태코드 설명

- header : HTTP 헤더, HTTP 전송에 필요한 모든 부가정보 포함됨

    ex) 메시지 바디 내용/크기/압축/인증/요청 클라이언트 정보 ..
    - field name: ows(띄쓰 허용) field-value ows
    - ex) Host: www.google.com

- empty line : 임의의 빈칸
- messeage body : 실제 전송할 데이터

    ex) html, json, 이미지, 문서 등 byte로 표현 가능한 모든 데이터

<BR>

🔎 HTTP 메서드
- 종류: GET(리소스 조회), POST(요청 내역 처리), PUT, DELETE ..
- 서버가 수행할 동작 지정

🔎 HTTP 상태코드
- 200 성공
- 400 클라이언트 요청 오류
- 500 서버 내부 오류