# HTTP 헤더: 일반 헤더

## HTTP 헤더

➕ 전송, 응답 둘다 사용
- header-field = field-name ":" OWS field-value OWS(OWS: 띄어쓰기 허용)

    - field-name은 대소문자 구분 없음

    <img width="595" alt="image" src="https://user-images.githubusercontent.com/81572478/191768004-c8e9379b-4d87-4165-9dec-ea71f02e6e09.png">



- 용도
    - HTTP 전송에 필요한 모든 부가정보 O
    ex) 메시지 바디 내용/크기, 압축, 인증, 요청 클라이언트 ..

    - 표준 헤더 너무 많음
    - 필요시 임의의 헤더 추가 가능

<br>

- HTTP BODY

    - Representation(표현) = representation Metadata + Representation Data
    - 메시지 본문(message body)을 통해 표현 데이터 전달
    - 메시지 본문 = 페이로드(payload)
    - 표현 : 요청이나 응답에서 전달할 실제 데이터
    - 표현 헤더 : 표현 데이터를 해석할 수 있는 정보 제공
        - 데이터 유형/데이터 길이/압축 정보 등

<br><br>

## 표현

<img width="331" alt="image" src="https://user-images.githubusercontent.com/81572478/191770271-bfb51eee-9e1f-4354-aa38-45b21dccfdd5.png">

<br>

- Content-Type : 표현 데이터의 형식
    - 미디어 타입, 문자 인코딩
        
        ex) text/html; charset=utf-8 (html타입의 utf-8로 전달됨)
        
        applicaion/json (json타입으로 전달됨)

        image/png (image파일 전달됨)

    <img width="281" alt="image" src="https://user-images.githubusercontent.com/81572478/191771160-01795506-fe82-4862-99a8-ddffb60894ab.png">

<br>

- Content-Encoding: 표현 데이터의 압축 방식
    - 표현 데이터를 압축하기 위해 사용
    - 데이터를 전달하는 곳에서 압축 후 인코딩 헤더 추가
    - 데이터를 읽는 쪽에서 인코딩 헤더의 정보로 압축 해제

        ex) gzip, deflate, identity

    <img width="280" alt="image" src="https://user-images.githubusercontent.com/81572478/191771276-99a10e8f-7130-481d-a569-f4c518732e20.png">

<br>

- Content-Language: 표현 데이터의 길이
    - 표현 데이터의 자연언어 표현
    
        ex) ko(한국어), en(영어), en-US

    <img width="239" alt="image" src="https://user-images.githubusercontent.com/81572478/191771498-ca119c0b-f454-42c9-a7c2-bcaaeddc3972.png">

<br>

- Content-Length: 표현 데이터의 길이
    - 바이트 단위
    - Transfer-Encoding(전송 코딩)을 사용하면 Content-Length를 사용하면 안됨

    <img width="240" alt="image" src="https://user-images.githubusercontent.com/81572478/191771695-37083d44-82ee-4965-93b6-6ec3846652fd.png">



<br><br>

## 협상(콘텐츠 네고시에이션)

: 클라이언트가 선호하는 표현 요청
➕ 협상 헤더는 '요청'시에만 사용


- Accept: 클라이언트가 선호하는 미디어 타입 전달
- Accept-Charset: 클라이언트가 선호하는 문자 인코딩
- Accept-Encoding: 클라이언트가 선호하는 압축 인코딩
- Accept-Language: 클라이언트가 선호하는 자연 언어


<br>

<img width="545" alt="image" src="https://user-images.githubusercontent.com/81572478/191772443-36bf5cd3-d727-40cc-acfe-1a0edabb0a41.png">
: 영어로 응답 감

<img width="542" alt="image" src="https://user-images.githubusercontent.com/81572478/191772582-0c0ae74d-b0c8-47d0-8f39-b87ed02f716f.png">
: 한국어로 응답 감

<br>

<img width="539" alt="image" src="https://user-images.githubusercontent.com/81572478/191772737-842d8bcd-fc7e-4226-b533-d955ef712ae4.png">
: 독일어로 응답 감, 우선순위가 필요하게 됨

<br>

### 협상과 우선순위1(Quality Values(q))

<img width="359" alt="image" src="https://user-images.githubusercontent.com/81572478/191773029-4ea4821c-c8d9-4d70-a0a0-796510ac65b0.png">

- Quality Values(q) 값 사용
- 0~1, 클수록 높은 우선순위(생략하면 1)
- ```Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7```
    1. ko-KR;q=1(q 생략)
    2. ko;q=0.9
    3. en-US;q=0.8
    4. en:q=0.7

<img width="539" alt="image" src="https://user-images.githubusercontent.com/81572478/191773613-acf27400-1750-43cd-b16a-3d7ec6318bcc.png">
: 영어로 응답 감

<br>

### 협상과 우선순위2

- 구체적인 것이 우선!
- ```Accept: text/*, text/plain, text/plain;format=flowed,*/*```
    1. text/plain;format=flowed
    2. text/plain
    3. ```text/*```
    4. ```*/*```

- 구체적인 것을 기준으로 미디어 타입을 맞춤
<img width="479" alt="image" src="https://user-images.githubusercontent.com/81572478/191774396-fc36ba54-31c3-4129-89b1-cd292abf0477.png">


<br>
<Br>


## 전송방식

- 단순 전송(Content-Length)
<img width="591" alt="image" src="https://user-images.githubusercontent.com/81572478/191774886-e066fbf3-cb66-4d1b-a275-ebdb517575e2.png">
: 컨텐츠 길이만큼 한번에 쭉 받음

- 압축 전송(Content-Encoding)
<img width="599" alt="image" src="https://user-images.githubusercontent.com/81572478/191775082-5f3a8907-ff48-4b81-aeb8-f705a8666250.png">
: 단순 전송에서 압축하고 전송, content 길이 및 압축 정보 전송
- 분할 전송(Transfer-Encoding)
<img width="594" alt="image" src="https://user-images.githubusercontent.com/81572478/191775471-960f0b57-8f44-482d-a4ad-649bb82d5f3b.png">
: chunk로 나누어 전송(Content-Length 넣으면 안됨, 길이 예상할 수 없다!)
- 범위 전송(Range, Content-Range)
<img width="587" alt="image" src="https://user-images.githubusercontent.com/81572478/191775824-056d0d4d-aa78-4455-9b3f-98f87649a9e6.png">
: 범위 지정해 받을 수 있음

<br><Br>

## 일반 정보

- From: 유저 에이전트 이메일 정보
    - 일반적으로 잘 사용x
    - 검색 엔진같은 곳에서 주로 사용
    - 요청에서 사용

- Referer: 이전 웹 페이지 주소
    - 현재 요청된 페이지의 이전 웹 페이지 주소
    - A → B로 이동하는 경우 B를 요청할때 Referer: A를 포함해서 요청
    - Referer를 사용해서 ```유입 경로``` 분석 가능
    - 요청에서 사용

- User-Agent: 유저 에이전트 애플리케이션 정보
    - 클라이언트의 애플리케이션 정보(웹 브라우저 정보 등)
    - 통계 정보
    - 어떤 종류의 브라우저에서 장애가 발생하는지 파악 가능
    - 요청에서 사용

- Server: 요청을 처리하는 ORIGIN 서버의 소프트웨어 정보
    - Server: Apache/2.2.22(Debian)
    - server: nginx
    - 응답에서 사용
    - 진짜 표현 데이터를 만들어주는 서버

- Date: 메시지가 발생한 날짜와 시간
    - 응답에서 사용

<br>
<Br>

## 특별한 정보

- ⭐Host: 요청한 호스트 정보(도메인)
    - 요청에서 사용
    - 필수
    - 하나의 서버가 여러 도메인을 처리해야 할 때 
    <img width="606" alt="image" src="https://user-images.githubusercontent.com/81572478/191777918-dc66f1b4-8396-4ff0-9f1c-1893664b108f.png">
    : 가상호스트를 통해 여러 도메인을 한번에 처리할 수 있는 서버, 실제 애플리케이션이 여러개 구동

        Host 헤더를 통해 어디로 갈지 정할 수 있음!

    - 하나의 IP 주소에 여러 도메인이 적용되어 있을 때

- Location: 페이지 리다이렉션
    - 웹 브라우저는 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동이동(리다이렉트)
    - 201(Created): Location 값은 요청에 의해 생성된 리소스 URI
    - 3xx(Redirection): Location 값은 요청을 자동으로 리디렉션하기 위한 대상 리소스를 가리킴

- Allow: 허용 가능한 HTTP 메소드
    - 405(Method Not Allowed) 에서 응답에 포함해야함 
        
        ex) Allow: GET, HEAD, PUT (경로는 있는데 POST 제공안하는 상황)

- Retry-After: 유제 에이전트가 다음 요청을 하기까지 기다려야 하는 시간

    - 503(Service Unavailable): 서비스가 언제까지 불능인지 알려줄 수 있음

<br><br>

- 인증

    - Authorization: 클라이언트 인증 정보를 서버에 전달
        - Authorization: Basic xxxxxxxxxxxxxxx
        - 인증 매커니즘 많음, value가 다 다름!(위의 x값)

    - WWW-Authenticate: 리소스 접근시 필요한 인증방법 정의
        - 리소스 접근시 필요한 인증 방법 정의
        - 401 Unauthorized 응답과 함께 사용

<br><Br>

## 쿠키

- [쿠키&세션 정리](https://github.com/zeunxx/2022-spring-study/blob/master/%EB%B0%95%EC%A7%80%EC%9D%80/chap13_1_%EC%84%B8%EC%85%98_%EC%BF%A0%ED%82%A4.md) 

- Set-Cookie : 서버에서 클라이언트로 쿠키 전달(응답)
- Cookie : 클라이언트가 서버에서 받은 쿠키를 저장하고, HTTP 요청시 서버로 전달

<img width="568" alt="image" src="https://user-images.githubusercontent.com/81572478/191780454-a02a3f2f-c958-4e70-bfed-5839870f7caf.png">
: 로그인 하고 다시 처음 페이지 들어가도 로그인 안한 것처럼 유저를 구분하지 못함

(Stateless 프로토콜: 클라이언트가 다시 요청하버 서버는 이전 요청을 기억하지 못함)

<img width="563" alt="image" src="https://user-images.githubusercontent.com/81572478/191780969-85469906-0e2e-4766-bb10-f6d12e577f4d.png">
<img width="568" alt="image" src="https://user-images.githubusercontent.com/81572478/191781020-8f83644b-0973-45a1-b289-1c139cb927cf.png">
<img width="454" alt="image" src="https://user-images.githubusercontent.com/81572478/191781149-8bc3cafe-c4cd-4c52-9c3f-7748afbc63dd.png">

<br>

``` set-cookie: sessionId=abcde1234; expires=Sat, 26-Dec-2020 00:00:00 GMT; path=/; domain=.google.com; Secure```

- 사용처
    - 사용자 로그인 세션 관리
    - 광고 정보 트래킹

- 쿠키 정보는 항상 서버에 전송됨
    - 네트워크 트래픽 추가 유발
    - 최소한의 정보만 사용(세션 id, 인증 토큰)
    - 서버에 전송하지 않고, 웹 브라우저 내부에 데이터를 저장하고 싶으면 웹 스토리지 사용

- 주의!: 보안에 민감한 데이터 저장x

### 쿠키 : 생명주기(Expires, max-age)

- ```Set-Cookie: expires=Sat, 26-Dec-2020 04:39:21 GMT```
    - 만료일이 되면 쿠키 삭제

- ```Set-Cookie: max-age=3600 (3600초)```
    - 0이나 음수 지정하면 쿠키 삭제

- 세션 쿠키: 만료 날짜를 생략하면 브라우저 종료시까지만 유지
- 영속 쿠키: 만료 날짜 입력하면 해당 날짜까지 유지

<BR>

### 쿠키 : 도메인(Domain)

```domain=example.org```

- 도메인 명시: 명시한 문서 기준 `도메인+서브 도메인` 포함

    - ```domain=example.org```을 지정해서 쿠키 생성
        - example.org는 물론이고
        - dev.example.org도 쿠키 접근

- 도메인 생략 : 현재 문서 기준 도메인에만 적용
    - ```domain=example.org```을 지정해서 쿠키 생성
        - example.org는 쿠키 접근
        - dev.example.org도 쿠키 미접근

<br>

### 쿠키 : 경로(Path)

```path=/home```

- 이 경로를 포함한 하위 경로 페이지만 쿠키 접근
- 일반적으로 path=/ 루트로 지정
    
    ex) ```path=/home``` 지정

    /home 가능, /home/level1 가능, /hello 불가능

<br>

### 쿠키 : 보안(Secure, HttpOnly, SameSite)

- Secure
    - 쿠키는 http, https를 구분하지 않고 전송
    - Secure 적용하면 https인 경우만 전송

- HttpOnly
    - XSS 공격방지
    - 자바스크립트에서 접근 불가
    - HTTP 전송에만 사용

- SameSite
    - XSRF 공격 방지
    - 요청 도메인과 쿠키에 설정된 도메인이 같은 경우만 쿠키 전송

<BR>