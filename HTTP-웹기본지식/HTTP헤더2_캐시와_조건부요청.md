# HTTP 헤더 2 - 캐시와 조건부 요청

## 캐시 기본 등작

<BR>
<img width="600" alt="image" src="https://user-images.githubusercontent.com/81572478/192253726-3daa880f-40a2-4c13-8387-2b9f910ce644.png">
<img width="594" alt="image" src="https://user-images.githubusercontent.com/81572478/192253823-6e1a78e7-2c99-4575-a5f6-6e69d8e0b059.png">


- 캐시가 없을 때

    - 데이터가 변경되지 않아도 계속 네트워크를 통해 데이터를 다운로드 받아야 함
    - 인터넷 네트워크는 매우 느리고 비쌈
    - 브라우저 로딩 속도가 느림
    - 느린 사용자 경험 

<BR><BR>

<img width="590" alt="image" src="https://user-images.githubusercontent.com/81572478/192254095-a10a67b9-59d4-4451-868b-029c582da5d2.png">
<img width="593" alt="image" src="https://user-images.githubusercontent.com/81572478/192254191-1748548d-5d80-4174-b704-bfc02df4018a.png">
<img width="590" alt="image" src="https://user-images.githubusercontent.com/81572478/192254266-ae417529-28c1-4767-8a5a-05df1bdcfcb6.png">

- 캐시 적용
    - 캐시 덕분에 가능 시간동안 네트워크 사용하지 않아도 됨
    - 비싼 네트워크 사용량 줄임
    - 브라우저 로딩 속도가 매우 빠름
    - 빠른 사용자 경험

<bR>

<img width="602" alt="image" src="https://user-images.githubusercontent.com/81572478/192254500-4898002c-d5a1-43e7-99c3-3f9556469ef1.png">
: 시간 초과되면 다시 요청해야함

- 서버를 통해 데이터를 다시 조회하고, 캐시를 갱신, 이때 다시 네트워크 다운로드가 발생한다

<BR><bR>

🔎 캐시가 만료되었는데, 서버가 가진 데이터와 클라이언트가 가진 데이터가 동일하다면 굳이 다운 받아야 할까?

<BR>

## 검증 헤더와 조건부 요청

### 캐시 시간 초과
:  캐시 유효 시간이 초과해서 서버에 다시 요청하면 
<img width="432" alt="image" src="https://user-images.githubusercontent.com/81572478/192255082-f17d5ea5-3c4d-4cdc-89b0-ba003e2f220a.png">

<BR>

캐시 만료 후에도 서버에서 데이터를 변경하지 않았다면, 데이터를 전송하는 대신 저장해두었던 캐시를 재사용!
단, 클라이언트의 데이터와 서버의 데이터가 같다는 사실을 확인할 방법이 필요!!

<BR>

### 검증 헤더 추가

<img width="613" alt="image" src="https://user-images.githubusercontent.com/81572478/192256716-6ecd224a-9925-42d6-b534-d52e027406ed.png">
<img width="608" alt="image" src="https://user-images.githubusercontent.com/81572478/192256863-e98a9fbc-c8c3-4af6-88a4-575b4d1c580a.png">
: 최종 수정일도 캐시에 저장

<img width="557" alt="image" src="https://user-images.githubusercontent.com/81572478/192257004-ab72251e-f000-4ea1-aea0-9b942225bd75.png">
: 조건부 요청(if-modified-since: 날짜)

🔎 이때 이후로 데이터가 바뀌었는지? 

- 안바뀌면 304 응답

- 바뀌면 다시 다운로드

<img width="559" alt="image" src="https://user-images.githubusercontent.com/81572478/192257080-b34a9eff-1963-46e8-ba95-23ed686744f6.png">
: 서버에서 클라이언트의 캐시에 있던 데이터를 검증함

<img width="571" alt="image" src="https://user-images.githubusercontent.com/81572478/192257319-0dc80082-b325-410c-b17a-7d14de30c97b.png">
<img width="644" alt="image" src="https://user-images.githubusercontent.com/81572478/192257421-8631b8c8-1570-4e48-a8d1-fce4108e556f.png">
: HTTP 헤더만 전송(기존보다 네트워크 부화 줄게됨)

<img width="645" alt="image" src="https://user-images.githubusercontent.com/81572478/192257571-79f2883b-d02e-43bc-8cef-a877217312a4.png">
: 캐시 재사용

<br>

- 캐시 유효 시간이 초과해도, 서버의 데이터가 갱신되지 않으면
- 304 Not Modified + 헤더 메타 정보만 응답(바디 x)
- 클라이언트는 서버가 보낸 응답 헤더 정보로 캐시의 메타 정보 갱신
- 클라이언트는 캐시에 저장되어 있던 데이터 재활용
- 결과적으로 네트워크 다운로드 발생하지만 용량이 적은 헤더 정보만 다운로드


<Br><Br>

### 검증 헤더
: 캐시 데이터와 서버 데이터가 같은지 검증하는 데이터
- Last-Modified, ETag

### 조건부 요청 헤더

: 검증헤더로 조건에 따른 분기
- If-Modified-Since: Last-Modified 사용
- If-None-Match: ETag 사용
    - 조건이 만족하면 200 OK
    - 조건 만족하지 않으면 304 Not Modified


#### 1️⃣ If-Modified-Since: 이후에 데이터가 수정되었으면?

- 데이터 미변경 예시
    - 캐시: 2020년 11월 10일 10:00:00 vs 서버: 2020년 11월 10일 10:00:00
    - **304 Not Modified**(리다이렉션: 캐시로 리다이렉션함!), 헤더 데이터만 전송(body 미포함)
    - 전송용량 0.1M(헤더 0.1M, 바디 1.0M)

- 데이터 변경 예시
     - 캐시: 2020년 11월 10일 10:00:00 vs 서버: 2020년 11월 10일 **11**:00:00
    - **200 OK**, 모든 데이터 전송(body 포함)
    - 전송용량 1.1M(헤더 0.1M, 바디 1.0M)


#### 📌 단점

- 1초 미만 단위로 캐시 조정 불가능
- 날짜 기반의 로직 사용
- 데이터를 수정해서 날짜가 다르지만, 같은 데이터를 수정해서 데이터 결과가 똑같은 경우: 같은 데이터를 받는데 날짜가 갱신되어 다시 다운됨
- 서버에서 별도의 캐시 로직 관리하고 싶은 경우
    ex) 스페이스/주석처럼 크게 영향 없는 변경에서 캐시 유지하고 싶은 경우

<br>

#### 2️⃣ ETag(Entity Tag)
- 캐시용 데이터에 임의의 고유한 버전 이름 달아둠
- 데이터가 변경되면 이름을 바꾸어서 변경(Hash 다시 생성)
    ex) ETag: "aaaaa" → ETag: "bbbbb"

- 단순하게 ETag만 보내서 같으면 유지, 다르면 다시 받기

<br>

<img width="607" alt="image" src="https://user-images.githubusercontent.com/81572478/192260337-7cdaceb8-b086-4379-981b-79e593a8323d.png">
<img width="601" alt="image" src="https://user-images.githubusercontent.com/81572478/192260407-220e22b4-d225-4cbb-a36c-f1e57ddb377f.png">
<img width="572" alt="image" src="https://user-images.githubusercontent.com/81572478/192260476-79a5c1ba-a99c-44fb-9c64-eae9d720aaa8.png">
<img width="549" alt="image" src="https://user-images.githubusercontent.com/81572478/192260587-ad473dcc-4d9e-48b6-8d42-f3dd849d89e3.png">
<img width="650" alt="image" src="https://user-images.githubusercontent.com/81572478/192260623-7dd330fc-767b-4e03-bc93-7487166b491a.png">
<img width="645" alt="image" src="https://user-images.githubusercontent.com/81572478/192260692-1228875b-cb3d-4f59-89cd-b10bc3de0943.png">


<br>

- 단순하게 서버에 ETag만 보내서 같으면 유지, 다르면 다시 받기 
- **캐시 제어 로직을 서버에서 완전히 관리**
- 클라이언트는 단순히 이 값을 서버에 제공(클라이언트는 캐시 메커니즘 모름)

    ex) 서버는 배타 오픈 기간인 3일동안 파일이 변경되어도 ETag 동일하게 유지
    
    애플리케이션 배포주기에 맞추어 ETag 모두 갱신


<br><br>

### 캐시 제어 헤더

- Cache-Control : 캐시 제어
    - max-age: 캐시 유효 시간, 초단위
    - no-cache: 데이터는 캐시해도 되지만, 항상 origin(원) 서버에 검증하고 사용
    - no-store : 데이터에 민감한 정보 있으면 안됨(아예 저장x, 있으면 빨리 삭제)


- Pragma : 캐시 제어(하위 호환)

- Expires : 캐시 유효 기간(하위 호환)
    - 캐시 만료일을 정확한 날짜로 지정(but 더 유연한 max-age권장)
    - Cache-Control:max-age와 함께 사용하면 Expires는 무시


<br><Br>

## 프록시 캐시



<img width="587" alt="image" src="https://user-images.githubusercontent.com/81572478/192264390-ea976fc4-461c-4d6f-894b-769d37c79765.png">
: 응답 느림

<br>

<img width="644" alt="image" src="https://user-images.githubusercontent.com/81572478/192265049-b0cdd0f3-24ed-4eaf-9a39-261503697f79.png">

: 요청이 오면 원서버에 바로 가는게 아니라 프록시 서버에 먼저 접근하게 됨

ex) 유튜브에서 외국의 인기 없는 컨텐츠는 다운이 느리지만 인기 많은 컨텐츠는 다운 빠름(한국에 프록시 캐시 서버가 있어서..)

<BR>

### Cache-Control

- Cache-Control: public
    - 응답이 public 캐시에 저장되어도 됨

- Cache-Control: private
- 응답이 해당 사용자만을 위한 것, private 캐시에 저장되어야 함

- Cache-Control: s-maxage
    -  프록시 캐시에만 적용되는 max age

- Age:60(HTTP 헤더)
    - 오리진 서버에서 응답 후 프록시 캐시 내에 머문 시간(초)

#### ➕ Origin server : 진짜 자원과 리소스가 있는 서버

<BR><br>

## 캐시 무효화

### 확실한 캐시 무효화 응답
: 캐시가 절대로 되면 안될때 넣어 주면 됨

- Cache-Control: no-cache, no-store, must-revalidate

    - no-cache: 데이터는 캐시해도 되지만, 항상 원 서버에 검증하고 사용
    - no-store: 데이터에 민감한 정보가 있으므로 저장하면 안됨(메모리에서 사용하고 최대한 빨리 삭제)
    - must-revalidate: 캐시 만료 후 최초 조회시 **원 서버**에 검증해햐암
    
        원 서버 접근 실패시 반드시 오류가 발생해야 함(504 Gateway Timeout)

        must-revalidate는 캐시 유효 시간이라면 캐시를 사용함



- Pragma: no-cache(HTTP 1.0 하위 호환)

<br>

### ✅ no cache

<img width="631" alt="image" src="https://user-images.githubusercontent.com/81572478/192266448-4752ea1a-1108-4154-8fc6-2cb0d5e9292e.png">
: no-cache는 원서버에 검증 요청!

<br>

<img width="636" alt="image" src="https://user-images.githubusercontent.com/81572478/192266578-d1f0f417-93ed-439d-949c-61fcc9c0313e.png">

: 원 서버에 검증받으려 하는데 네트워크 다운,
오류 대신 오래된 데이터로 보여줄 수 있음(200 OK)

<br>

### ✅ must revalidate
<img width="626" alt="image" src="https://user-images.githubusercontent.com/81572478/192266900-80b58fa8-718c-41f2-834e-54e682f3c044.png">
: 원 서버에 검증받으려 하는데 네트워크 다운,
무조건 504 Timeout 때림