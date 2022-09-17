# HTTP 메서드

## HTTP API
<br>

- 요구 사항 : 회원정보 관리 API
    - 회원 목록 조회
    - 회원 조회
    - 회원 등록
    - 회원 수정
    - 회원 삭제

<BR>

### 💡 URI 설계에서 가장 중요한 것은  **리소스 식별** 

→ 리소스란 ?

회원을 등록/수정/조회하는게 리소스가 아님!!

**회원**이라는 개념 = 리소스 

→ 리소스를 어떻게 식별하는 게 좋은가?

회원을 등록/수정/조회하는 것 모두 배제!!

회원이라는 리소스만 식별 ➡️ **회원 리소스를 URI에 매핑**

<BR>
➡️➡️➡️

- 요구 사항 : 회원정보 관리 API
    - **회원** 목록 조회  /members
    - **회원** 조회  /members/{id} → 구분 방법?
    - **회원** 등록 /members/{id} → 구분 방법?
    - **회원** 수정 /members/{id} → 구분 방법?
    - **회원** 삭제 /members/{id} → 구분 방법?

<BR><BR>

1️⃣ URI는 리소스만 식별!

```/members/{id}```

2️⃣ 리소스와 행위를 분리!

- 리소스 : 회원(명사)
- 행위 : 조회, 등록, 삭제, 변경(동사) ➡️ 행위(메소드)는 어떻게 구분?

<BR><bR>

## HTTP 메소드

<BR>
🔎 주요 메소드 

- GET: 리소스 조회
- POST: 요청 데이터 처리, 주로 등록에 사용
- PUT: 리소스를 대체, 해당 리소스가 없으면 생성
- PATCH: 리소스 부분 변경
- DELETE: 리소스 삭제

<BR>

### GET

- **리소스 조회**
- 서버에 전달하고 싶은 데이터는 **query(쿼리 스트링)**을 통해 전달
- 메시지 바디를 사용해서 데이터를 전달할 수 있지만, 지원하지 않는 곳이 많아 권장x

<img width="580" alt="image" src="https://user-images.githubusercontent.com/81572478/190861756-0a9428e5-ffb4-4e1d-9161-d19506844762.png">
<img width="592" alt="image" src="https://user-images.githubusercontent.com/81572478/190861797-6325e2ae-4984-47e1-9993-60ff10ddca54.png">

1. 클라이언트가 서버에 /members/100 조회 요청
2. 서버에서 get 요청 확인하고 json 응답 생성
3. 서버가 클라이언트에 응답 메세지 전송(200 OK)


<BR>

### POST

- 요청 데이터 처리
- **메시지 바디를 통해 서버로 요청 데이터 전달**
- 서버는 요청 데이터 처리
    - 메시지 바디를 통해 들어온 데이터를 처리하는 모든 기능 수행
- 주로 전달된 데이터로 신규 리소스 등록, 프로세스 처리에 사용

<img width="509" alt="image" src="https://user-images.githubusercontent.com/81572478/190862110-0b163a31-d7dd-409f-a36f-fb56eeb39190.png">
<img width="585" alt="image" src="https://user-images.githubusercontent.com/81572478/190862118-f0ad917c-bca4-4229-8497-2709265dd94f.png">
<img width="581" alt="image" src="https://user-images.githubusercontent.com/81572478/190862130-9d9a9dc2-1b8e-4a45-9074-e89e8eef1b92.png">

리소스 URI에 POST 요청이 오면 요청 데이터를 **어떻게 처리할지** 리소스마다 따로 정해야 함
<BR>

#### 🔎 POST 쓰임새

- 새 리소스 생성(등록) : 서버가 아직 식별하지 않은 새 리소스 생성
- 요청 데이터 처리 : 
    - 단순 데이터 생성/변경을 넘어서 프로세스 처리
    
        ex) 주문에서 ```결제완료 → 배달 시작 → 배달 완료``` 처럼 단순히 값 변경을 넘어 프로셋스의 상태가 변경되는 경우

    - POST의 결과로 새로운 리소스가 생성되지 않을 수 있음
   
        ex) POST /order/{orderId}/start-delivery(컨트롤 URI = 동사형의 URI, 리소스만으로 URI 설계 불가할때!)

- 다른 메소드로 처리하기 애매한 경우 

    ex) json으로 조회 데이터 넘겨야 하는데, GET 메소드 사용하기 어려운 경우

<BR><bR>

### PUT

- 리소스를 대체
    - 리소스가 있으면 대체, 없으면 생성 = 쉽게 말해 덮어버림

⭐ 클라이언트가 리소스를 식별

: 클라이언트가 리소스 위치를 알고 URI 지정 = POST와 차이점

- 생성하는 역할도 하지만 리소스의 정확한 위치를 안다!

<img width="580" alt="image" src="https://user-images.githubusercontent.com/81572478/190862537-6369638c-56fb-42ef-ad42-2e0fc9f077d8.png">
<img width="580" alt="image" src="https://user-images.githubusercontent.com/81572478/190862552-217224de-b60e-431b-bc5b-b735c5c1f8cc.png">
<img width="518" alt="image" src="https://user-images.githubusercontent.com/81572478/190862566-f64ddcef-b4ce-4a21-a316-db86676734a2.png">
<img width="584" alt="image" src="https://user-images.githubusercontent.com/81572478/190862575-638baa6a-361c-4ec2-9a0d-0f48e0931695.png">
<img width="608" alt="image" src="https://user-images.githubusercontent.com/81572478/190862620-67ef65b6-bd29-4901-93e2-d74311d4aa0c.png">
<img width="582" alt="image" src="https://user-images.githubusercontent.com/81572478/190862633-4cabc296-9c12-4725-9a58-7927b4b0083a.png">

리소스를 잘못 덮어버린다면? 리소스 수정하기 어려움!

이때, PATCH를 쓰면 좋다!

<BR><bR>

### PATCH

<img width="582" alt="image" src="https://user-images.githubusercontent.com/81572478/190862676-4d0b4dfc-81bc-4ccc-8cad-978d324f8b2f.png">
<img width="590" alt="image" src="https://user-images.githubusercontent.com/81572478/190862685-55ea42ef-3299-4066-903b-41a9c637555b.png">
: 부분적으로 리소스의 데이터를 변경할 수 있음


<BR><bR>

### DELETE

<img width="592" alt="image" src="https://user-images.githubusercontent.com/81572478/190862727-54085916-3ba9-47fc-a0de-bfe3254b559a.png">

: /members/100 제거됨

<br><Br>

### HTTP 메소드의 속성
<BR>

1️⃣ 안전

- 호출해도 리소스를 변경하지 않는다
- 안전은 해당 리소스만 고려, 로그쌓여서 장애 발생 등은 고려하지 않음
- GET, HEAD

<BR>

2️⃣ 멱등
- 한 번 호출하든 100번 호출하든 결과가 똑같다
- 멱등 메소드
    - GET
    - PUT: 결과를 대체, 같은 요청을 여러번 해도 최종 결과는 같음
    - DELETE: 결과 삭제, 같은 요청을 여러번 해도 삭제된 결과는 똑같음

⭐ **POST는 멱등하지 않음!** 두번 호출하면 같은 결제가 중복해서 발생할 수 있음

    ➡️ 멱등은 언제 사용?

    : 서버가 정상응답을 못줬을때, 클라이언트가 같은 요청을 다시 해도 되는가? (판단근거)

- 멱등은 외부 요인으로 중간에 리소스가 변경되는 것은 고려하지 않음

 <bR>

3️⃣ 캐시가능

- 응답 결과 리소스를 캐시해서 사용해도 되는가?
- GET, HEAD, POST, PATCH
- 실제로는 GET, HEAD정도만 캐시로 사용