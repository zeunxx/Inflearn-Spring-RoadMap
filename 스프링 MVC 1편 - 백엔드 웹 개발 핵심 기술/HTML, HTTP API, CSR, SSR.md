## HTML, HTTP API, CSR, SSR

### 정적 리소스

- 고정된 HTML파일, CSS/JS/이미지/영상 등을 제공
- 주로 웹 브라우저

### HTML 페이지

- 동적으로 필요한 HTML 파일을 생성해서 전달
- 웹 브라우저 : HTML 해석

<img width="411" alt="image" src="https://user-images.githubusercontent.com/81572478/227249240-3c921d4f-a193-40ae-b796-b2c635752df8.png">

<BR>

### HTTP API

- HTML이 아니라 데이터를 전달
- 주로 JSON 형식 사용
- 다양한 시스템에서 호출 

<img width="407" alt="image" src="https://user-images.githubusercontent.com/81572478/227249637-db79bd61-2f4f-49cb-a50a-d0e6a436bb15.png">

- 다양한 시스템에서 호출
- 데이터만 주고 받음, UI 화면이 필요하면, 클라이언트가 별도 처리
- 앱, 웹 클라이언트, 서버 TO 서버

<img width="435" alt="image" src="https://user-images.githubusercontent.com/81572478/227250157-8afe63e0-9fcc-48d5-b982-e287969417fa.png">

<BR>

#### 다양한 시스템 연동

- 주로 JSON 형태로 데이터 통신

- UI 클라이언트 접점
    - 앱 클라이언트(아이폰, 안드로이드, PC 앱)
    - 웹 브라우저에서 자바스크립트를 통한 HTTP API 호출
    - React, Vue.js 같은 웹 클라이언트

- 서버 to 서버
    - 주문 서버 -> 결제 서버
    - 기업간 데이터 통신

<BR><BR>

### 서버사이드 렌더링, 클라이언트 사이드 렌더링

#### SSR - 서버 사이드 렌더링

- HTML 최종 결과를 서버에서 만들어서 웹 브라우저에 전달
- 주로 정적인 화면에 사용
- 관련기술: JSP, 타임리프 -> 백엔드 개발자

<img width="421" alt="image" src="https://user-images.githubusercontent.com/81572478/227251271-59b5fd4a-7a37-4496-a09b-5d3d2aae286f.png">

<BR>

#### CSR - 클라이언트 사이드 렌더링

- HTML 결과를 자바스크립트를 사용해 웹 브라우저에서 동적으로 생성해서 적용
- 주로 동적인 화면에 사용, 웹 환경을 마치 앱 처럼 필요한 부분부분 변경할 수 있음
- 예) 구글 지도, Gmail, 구글 캘린더
- 관련기술: React, Vue.js -> 웹 프론트엔드 개발자

<img width="432" alt="image" src="https://user-images.githubusercontent.com/81572478/227252359-6d986ff0-5555-4f43-86f9-e01f301141f6.png">

<BR>

> 참고
> React, Vue.js를 CSR + SSR 동시에 지원하는 웹 프레임워크도 있음
> SSR을 사용하더라도, 자바스크립트를 사용해서 화면 일부를 동적으로 변경 가능

<BR><bR>

### 백엔드 개발자 입장에서 UI 기술

- 백엔드 - 서버 사이드 렌더링 기술
    - JSP, 타임리프
    - 화면이 정적이고, 복잡하지 않을 때 사용
    - 백엔드 개발자는 서버 사이드 렌더링 기술 학습 필수

-  웹 프론트엔드 - 클라이언트 사이드 렌더링 기술
    - React, Vue.js
    - 복잡하고 동적인 UI 사용
    - 웹 프론트엔드 개발자의 전문 분야
    
- 선택과 집중
    - 백엔드 개발자의 웹 프론트엔드 기술 학습은 옵션
    - 백엔드 개발자는 서버, DB, 인프라 등등 수 많은 백엔드 기술을 공부해야 한다.
    - 웹 프론트엔드도 깊이있게 잘 하려면 숙련에 오랜 시간이 필요하다