## HTTP 요청

### HTTP 요청 - 기본, 헤더 조회
```
    @RequestMapping("/headers")
    public String headers(HttpServletRequest request,
                          HttpServletResponse response,
                          HttpMethod httpMethod,
                          Locale locale,
                          @RequestHeader MultiValueMap<String, String> headerMap,
                          @RequestHeader("host") String host,
                          @CookieValue(value = "myCookie", required = false) String cookie){
        log.info("request={}", request);
        log.info("response={}", response);
        log.info("httpMethod={}", httpMethod);
        log.info("locale={}", locale);
        log.info("headerMap={}", headerMap);
        log.info("header host={}", host);
        log.info("myCookie={}", cookie);
        return "ok";
    }
```

- HttpServletRequest
- HttpServletResponse
- HttpMethod : HTTP 메서드를 조회한다. org.springframework.http.HttpMethod
- Locale : Locale 정보를 조회한다.
- @RequestHeader MultiValueMap<String, String> headerMap
    - 모든 HTTP 헤더를 MultiValueMap 형식으로 조회한다.
- @RequestHeader("host") String host
    - 특정 HTTP 헤더를 조회한다.
    - 속성
        - 필수 값 여부: required
        - 기본 값 속성: defaultValue
- @CookieValue(value = "myCookie", required = false) String cookie
    - 특정 쿠키를 조회한다.
    - 속성
        - 필수 값 여부: required
        - 기본 값: defaultValue
<br>

> MultiValueMap

> MAP과 유사한데, 하나의 키에 여러 값을 받을 수 있다.
HTTP header, HTTP 쿼리 파라미터와 같이 하나의 키에 여러 값을 받을 때 사용한다. 
> ex)  keyA=value1&keyA=value

```
MultiValueMap<String, String> map = new LinkedMultiValueMap();
map.add("keyA", "value1");
map.add("keyA", "value2");
//[value1,value2]
List<String> values = map.get("keyA");
```

<br><Br>

### HTTP 요청 파라미터 - 쿼리 파라미터, HTML Form

1️⃣ http 요청 데이터 조회 - 개요

- 클라이언트에서 서버로 요청 데이터를 전달할때 사용하는 3가지 방법

1. GET - 쿼리 파라미터
    - /url?username=hello&age=20
    - 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달
    - 예) 검색, 필터, 페이징등에서 많이 사용하는 방식
2. POST - HTML Form
    - content-type: application/x-www-form-urlencoded
    - 메시지 바디에 쿼리 파리미터 형식으로 전달 username=hello&age=20
    - 예) 회원 가입, 상품 주문, HTML Form 사용
3. HTTP message body에 데이터를 직접 담아서 요청
    - HTTP API에서 주로 사용, JSON, XML, TEXT
    - 데이터 형식은 주로 JSON 사용
    - POST, PUT, PATCH

<br>

### 1️⃣ 요청 파라미터 - 쿼리 파라미터, HTML Form

**GET, 쿼리 파라미터 전송**
`http://localhost:8080/request-param?username=hello&age=20`

<br>

**POST, HTML Form 전송**
```
POST /request-param ...
content-type: application/x-www-form-urlencoded

username=hello&age=20
```

➡️ GET 쿼리 파리미터 전송 방식이든, POST HTML Form 전송 방식이든 둘다 형식이 같으므로 구분없이
조회할 수 있다.

이것을 간단히 **요청 파라미터(request parameter) 조회**라 한다

```
@Slf4j
@Controller
public class RequestParamController {

    @RequestMapping("/request-param-v1")
    public void requestParamV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        log.info("username ={}, age ={}", username, age);

        response.getWriter().write("ok");
    }
}

```

<br>

### 2️⃣ 요청 파라미터 - @RequestParam

```
    @ResponseBody
    @RequestMapping("/request-param-v2")
    public String requestParamV2(@RequestParam("username") String memberName,
                                 @RequestParam("age") int memberAge){
        log.info("username={}, age={}", memberName, memberAge);
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/request-param-v3")
    public String requestParamV3(@RequestParam String username,
                                 @RequestParam int age){
        log.info("username={}, age={}", username, age);
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/request-param-v4")
    public String requestParamV4(String username,int age){
        log.info("username={}, age={}", username, age);
        return "ok";
    }
```

- @RequestParam : 파라미터 이름으로 바인딩
- @ResponseBody : View 조회를 무시하고, HTTP message body에 직접 해당 내용 입력
- String , int , Integer 등의 단순 타입이면 @RequestParam 도 생략 가능

<br>

> 주의
> @RequestParam 애노테이션을 생략하면 스프링 MVC는 내부에서 required=false 를 적용한다.
required 옵션은 바로 다음에 설명한다.

<br>


> 참고
> 이렇게 애노테이션을 완전히 생략해도 되는데, 너무 없는 것도 약간 과하다는 주관적 생각이 있다.
> @RequestParam 이 있으면 명확하게 요청 파리미터에서 데이터를 읽는 다는 것을 알 수 있다.

<br>

#### 💡 파라미터 필수 여부

```
    @ResponseBody
    @RequestMapping("/request-param-required")
    public String requestParamRequired(@RequestParam(required = true) String username, //반드시 필요한 파라미터
                                       @RequestParam(required = false) Integer age){
        log.info("username={}, age={}", username, age);
        return "ok";
    }
```

- @RequestParam.required
    - 파라미터 필수 여부
    - 기본값이 파라미터 필수( true )이다.
- /request-param 요청
    - username 이 없으므로 400 예외가 발생한다.
- **주의! - 파라미터 이름만 사용**
    - /request-param?username=
    - 파라미터 이름만 있고 값이 없는 경우 빈문자로 통과
- **주의! - 기본형(primitive)에 null 입력**
    - /request-param 요청
    - @RequestParam(required = false) int age
    - null 을 int 에 입력하는 것은 불가능(500 예외 발생)
    - 따라서 null 을 받을 수 있는 Integer 로 변경하거나, 또는 다음에 나오는 defaultValue 사용

<br>

```
    @ResponseBody
    @RequestMapping("/request-param-default")
    public String requestParamDefault(@RequestParam(required = true, defaultValue = "guest") String username,
                                       @RequestParam(required = false, defaultValue = "-1") int age){
        log.info("username={}, age={}", username, age);
        return "ok";
    }
```
- username=hello, age=20
- username=guest, age=-1 (파라미터 안넘겼을때, 기본값 적용)
- defaultValue 는 빈 문자의 경우에도 설정한 기본 값이 적용된다.
    - /request-param-default?username=

<br>

#### 💡 파라미터 MAP으로 조회하기 - requestParamMap
```
@ResponseBody
@RequestMapping("/request-param-map")
public String requestParamMap(@RequestParam Map<String, Object> paramMap){
    log.info("username={}, age={}", paramMap.get("username"), paramMap.get("age"));
    return "ok";
}
```
파라미터를 Map, MultiValueMap으로 조회할 수 있다. 

- @RequestParam Map , 
    - Map(key=value)
- @RequestParam MultiValueMap
    - MultiValueMap(key=[value1, value2, ...] ex) (key=userIds, value=[id1, id2])

<br>
➡️ 파라미터의 값이 1개가 확실하다면 Map 을 사용해도 되지만, 그렇지 않다면 MultiValueMap 을 사용하자.

+ 근데 웬만하면 파라미터 값은 1개를 씀