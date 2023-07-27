
## 검증1 - Validation

웹 서비스는 폼 입력시 오류가 발생하면, 고객이
입력한 데이터를 유지한 상태로 어떤 오류가 발생했는지 친절하게 알려주어야 한다

<Br>

컨트롤러의 중요한 역할중 하나는 **HTTP 요청이 정상인지 검증**하는 것이다. 그리고 정상 로직보다 이런
검증 로직을 잘 개발하는 것이 어쩌면 더 어려울 수 있다.

<br>

**참고: 클라이언트 검증, 서버 검증**
- 클라이언트 검증은 조작할 수 있으므로 보안에 취약하다.
- 서버만으로 검증하면, 즉각적인 고객 사용성이 부족해진다.
- 둘을 적절히 섞어서 사용하되, 최종적으로 서버 검증은 필수
- API 방식을 사용하면 API 스펙을 잘 정의해서 검증 오류를 API 응답 결과에 잘 남겨주어야 함


먼저 검증을 직접 구현해보고, 뒤에서 스프링과 타임리프가 제공하는 검증 기능을 활용해보자.

<br>
<br>

### 검증 직접 처리 

<img width="324" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/ab7e8a2b-4ac8-481c-b623-a69db39bb7a7">

<img width="325" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/e58dd771-3f2e-4772-9a1a-7e20dedfbe7e">
