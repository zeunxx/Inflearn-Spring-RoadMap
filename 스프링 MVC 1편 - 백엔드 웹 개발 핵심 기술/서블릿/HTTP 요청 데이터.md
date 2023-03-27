## HTTP 요청 데이터


### 개요

HTTP 요청 메시지를 통해 클라이언트에서 서버로 데이터를 전달하는 방법

- 주로 다음 3가지를 사용!

1️⃣ **GET - 쿼리 파라미터**
- /url?username=hello&age=20
- 메시지 바디 없이, url의 쿼리 파라미터에 데이터를 포함해서 전달
- ex) 검색, 필터, 페이징 등에서 많이 사용하는 방식

<br>

2️⃣ **POST - HTML Form**

<img width="340" alt="image" src="https://user-images.githubusercontent.com/81572478/227778998-c37cb91f-188a-4efd-97bf-51151f323a69.png">


- content-type:application/x-www-form-urlencoded
- 메시지 바디에 쿼리 파라미터 형식으로 전달 `username=hello&age=20`
- ex) 회원가입, 상품 주문, HTML Form 사용

<Br>

3️⃣ **HTTP message body**
- HTTP API에서 주로 사용, JSON, XML, TEXT
- 데이터 형식은 주로 JSON 사용
- POST, PUT, PATCH

<BR><bR>


### HTTP 요청 데이터 - GET 쿼리 파라미터

메시지 바디 없이, URL의 쿼리 파라미터를 사용해서 데이터를 전달하자.
예) 검색, 필터, 페이징등에서 많이 사용하는 방식

- 쿼리 파라미터는 url에 다음과 같이  `?`을 시작으로 보냄! 추가 파라미터는 `&`로 구분

`http://localhost:8080/request-param?username=hello&age=20`

➡️ 서버에서는 HttpServletRequest 가 제공하는 다음 메서드를 통해 쿼리 파라미터를 편리하게 조회할 수 있음

```
@WebServlet(name="requestParamServlet", urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 전체 파리미터 조회
        System.out.println("[전체 파리미터 조회]- start");

        request.getParameterNames().asIterator()
                        .forEachRemaining(paramName -> System.out.println(paramName + " = " +request.getParameter(paramName)));
        System.out.println("[전체 파리미터 조회]- end");
        System.out.println();

        // 단일 파리미터 조회
        System.out.println("[단일 파리미터 조회]- start");
        String username = request.getParameter("username");
        String age = request.getParameter("age");

        System.out.println("username = " + username);
        System.out.println("age = " + age);

        System.out.println("[단일 파리미터 조회]- end");
        System.out.println();

        // 중복 파라미터 조회
        System.out.println("[이름이 같은 복수 파라미터 조회]");
        String[] usernames = request.getParameterValues("username");
        for (String name : usernames) {
            System.out.println("name = " + name);;
        }


        response.getWriter().write("ok");
    }

}
```

📌 복수 파라미터에서 단일 파라미터 조회

username=hello&username=kim 과 같이 파라미터 이름은 하나인데, 값이 중복이면 어떻게 될까?
*request.getParameter()* 는 **하나의 파라미터 이름에 대해서 단 하나의 값만 있을 때 사용**해야 한다. 
지금처럼 **중복일 때**는 *request.getParameterValues()* 를 사용해야 한다.
참고로 이렇게 중복일 때 request.getParameter() 를 사용하면 request.getParameterValues() 의
첫 번째 값을 반환한다.

<br><Br>

### HTTP 요청 데이터 - POST HTML Form

HTML의 Form을 사용해서 클라이언트에서 서버로 데이터를 전송해보자.
주로 회원 가입, 상품 주문 등에서 사용하는 방식이다.

<BR>

✅ 특징

- content-type: application/x-www-form-urlencoded
- 메시지 바디에 쿼리 파리미터 형식으로 데이터를 전달한다. username=hello&age=20

> 주의
> 웹 브라우저가 결과를 캐시하고 있어서, 과거에 작성했던 html 결과가 보이는 경우도 있다. 이때는 웹
브라우저의 새로 고침을 직접 선택해주면 된다. 물론 서버를 재시작 하지 않아서 그럴 수도 있다

<br>

POST의 HTML Form을 전송하면 웹 브라우저는 다음 형식으로 HTTP 메시지를 만든다. (웹 브라우저 개발자 모드 확인)
- 요청 URL: `http://localhost:8080/request-param`
- content-type: application/x-www-form-urlencoded
- message body: username=hello&age=20

<br>

`application/x-www-form-urlencoded` 형식은 앞서 GET에서 살펴본 쿼리 파라미터 형식과 같음

따라서 쿼리 파라미터 조회 메서드를 그대로 사용하면 됨

클라이언트(웹 브라우저) 입장에서는 두 방식에 차이가 있지만, 서버 입장에서는 둘의 형식이 동일하므로,
request.getParameter() 로 편리하게 구분없이 조회 가능!

 ➡️ 정리하면 request.getParameter() 는 **GET URL 쿼리 파라미터** 형식도 지원하고, *POST HTML Form** 
형식도 둘 다 지원!

<br>

 참고
> content-type은 HTTP 메시지 바디의 데이터 형식을 지정
> GET URL 쿼리 파라미터 형식으로 클라이언트에서 서버로 데이터를 전달할 때는 HTTP 메시지 바디를
사용하지 않기 때문에 content-type이 없음
> POST HTML Form 형식으로 데이터를 전달하면 HTTP 메시지 바디에 해당 데이터를 포함해서 보내기
때문에 바디에 포함된 데이터가 어떤 형식인지 content-type을 꼭 지정해야 함. 이렇게 폼으로 데이터를
전송하는 형식을 application/x-www-form-urlencoded 라 함

<br><Br>

### HTTP 요청 데이터 - API 메시지 바디 

#### 1️⃣ 단순 텍스트

HTTP message body에 데이터를 직접 담아서 요청
- HTTP API에서 주로 사용, JSON, XML, TEXT
- 데이터 형식은 주로 JSON 사용
- POST, PUT, PATCH

HTTP 메시지 바디의 데이터를 InputStream을 사용해서 직접 읽을 수 있음!

```
@WebServlet(name = "RequestBodyStringServlet", urlPatterns = "/request-body-string")
public class RequestBodyStringServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        System.out.println("messageBody = " + messageBody);
        response.getWriter().write("ok");
    }
}
```

> 참고
> inputStream은 byte 코드를 반환한다. byte 코드를 우리가 읽을 수 있는 문자(String)로 보려면 문자표
(Charset)를 지정해주어야 한다. 여기서는 UTF_8 Charset을 지정해주었다

<br>

✅ **문자 전송**
POST `http://localhost:8080/request-body-string`
content-type: text/plain
message body: hello
결과: messageBody = hello

<Br><br>

#### 2️⃣ JSON

HTTP API에서 주로 사용하는 JSON형식으로 데이터 전달!

✅ **JSON 형식 전송**
POST `http://localhost:8080/request-body-json`
content-type: application/json
message body: {"username": "hello", "age": 20}
결과: messageBody = {"username": "hello", "age": 20}

<BR>

```
@WebServlet(name="requestBodyJsonServlet", urlPatterns = "/request-body-json")
public class RequestBodyJsonServlet extends HttpServlet {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        System.out.println("messageBody = " + messageBody);
        // 여기까지 string과 동일

        HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);

        System.out.println("helloData.getUsername = " + helloData.getUsername());
        System.out.println("helloData.getAge = " + helloData.getAge());

        response.getWriter().write("ok");
    }
}
```
* HelloData는 DTO 클래스

<br>

> 참고
> JSON 결과를 파싱해서 사용할 수 있는 자바 객체로 변환하려면 Jackson, Gson 같은 JSON 변환
라이브러리를 추가해서 사용해야 한다. 스프링 부트로 Spring MVC를 선택하면 기본으로 Jackson 
라이브러리( ObjectMapper )를 함께 제공한다.

<br>

> 참고
> HTML form 데이터도 메시지 바디를 통해 전송되므로 직접 읽을 수 있다. 하지만 편리한 파리미터 조회
기능( request.getParameter(...) )을 이미 제공하기 때문에 파라미터 조회 기능을 사용하면 된다.