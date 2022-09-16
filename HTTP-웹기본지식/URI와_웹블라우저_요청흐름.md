# URI와 웹 브라우저 요청 흐름

## URI (Uniform Resource Identifier)

: URI는 로케이터(Locator), 이름(Name) 또는 둘 다 추가로 분류될 수 있다!

<br>

- URI : 통일된 자원(URI로 식별할 수 있는 모든 것, NO 제한) 식별
<img width="396" alt="image" src="https://user-images.githubusercontent.com/81572478/190570051-784ae985-f181-439c-b0ca-bdd2c89218ff.png">
- URL : 자원 위치로 식별
- URN : 자원 이름으로 식별 (이름은 리소스 찾기 힘듦!)
    - 위치는 변할 수 있지만, 이름은 변하지 않음

<BR>

<img width="614" alt="image" src="https://user-images.githubusercontent.com/81572478/190570237-d34ad998-cb0d-406a-aed1-c654f779ff40.png">


<BR>

🔎 URL 전체 문법

- scheme://[userinfo@]host[:port][/path][?query][#fragment]
- ```https://www.google.com/search?q=hello&hl=ko```

<BR>

- scheme : 주로 프로토콜 사용
    - 프로토콜 : 어떤 방식으로 자원에 접근할 것인가 하는 약속 규칙 ex) http, https, ftp ..
    - http : 80 port, https : 443 port 주로 사용(포트 생략 가능)
    - https = http + 보안

- userinfo@ : 사용자 정보 포함 인증(거의 사용 x)

- host : 호스트명, 도메인명 또는 IP 주소 직접 사용가능

- port : 생략 가능

- path : 리소스 경로, 계층적 구조
    - ex) /home/fiel1.jpg

- query : key=value 형태
    - ?로 시작, &로 추가가능 ex) ?keyA=valueA&keyB=valueB
    - query parameter, query string 등으로 불림

- fragment : html 내부 북마크 등 사용, 서버에 전송하는 정보 아님(잘 사용x)

<BR><BR>

## 웹 브라우저 요청 흐름

1. IP, port 정보 찾고 http 요청 메시지 생성

<img width="597" alt="image" src="https://user-images.githubusercontent.com/81572478/190572109-f0f8158a-0bbe-4fda-acfa-89521ca18a6f.png">

- HTTP 요청 메시지
<img width="377" alt="image" src="https://user-images.githubusercontent.com/81572478/190572186-ddf035f8-3aff-493f-bf5e-3512eb5483b5.png">

<BR>

2. HTTP 메시지 전송

<img width="545" alt="image" src="https://user-images.githubusercontent.com/81572478/190572377-3d52764a-bab9-45a0-8671-c76a5c0426e7.png">

- tcp handshake 해서 구글서버와 연결
- 패킷 생성해 전송 데이터(HTTP 메시지) 전송

<img width="568" alt="image" src="https://user-images.githubusercontent.com/81572478/190572681-985de74b-f767-4a62-b776-109433366d5f.png">

<BR>

3. 요청 패킷 도착하면 역캡슐화 통해 패킷 해석

<BR>

4. 구글 서버에서 HTTP 응답 메시지 전송

<img width="378" alt="image" src="https://user-images.githubusercontent.com/81572478/190572896-a0f9c1cd-a5cd-49ae-aaa3-75ab418fa547.png">

+ 2번과 같이 패킷 전송
<br>

5. 응답 받은 HTML data 이용해 웹 브라우저 html 렌더링

<img width="581" alt="image" src="https://user-images.githubusercontent.com/81572478/190573984-3208d56d-0c38-47f8-8288-4d631611d187.png">


