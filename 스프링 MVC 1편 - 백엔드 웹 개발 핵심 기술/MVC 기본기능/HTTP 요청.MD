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

<br><Br>

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

<br><br>

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

<br><Br>

### 2️⃣ 요청 파라미터 - @ModelAttribute
```
@RequestParam String username;
@RequestParam int age;
HelloData data = new HelloData();
data.setUsername(username);
data.setAge(age);
```
- spring은 이 과정을 완전히 자동화 해주는 @ModelAttribute 기능 제공

<br>

```
    @ResponseBody
    @RequestMapping("/model-attribute-v1")
    public String modelAttribute(@ModelAttribute HelloData helloData){

        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());

        return "ok";
    }

```

➡️ 스프링MVC는 @ModelAttribute 가 있으면 다음을 실행한다

1. HelloData 객체를 생성한다.
2. 요청 파라미터의 이름으로 HelloData 객체의 프로퍼티를 찾는다. 
3. 해당 프로퍼티의 setter를
호출해서 파라미터의 값을 입력(바인딩) 한다.
- 예) 파라미터 이름이 username 이면 setUsername() 메서드를 찾아서 호출하면서 값을 입력한다.

<br>

💡 **프로퍼티**
- 객체에 getUsername() , setUsername() 메서드가 있으면, 이 객체는 username 이라는 프로퍼티를
가지고 있다.
- username 프로퍼티의 값을 변경하면 setUsername() 이 호출되고, 조회하면 getUsername() 이
호출된다.
    ```
    class HelloData {
        getUsername();
        setUsername();
    }
    ```

<br>

@ModelAttribute 생략 가능!!
```
    @ResponseBody
    @RequestMapping("/model-attribute-v2")
    public String modelAttributeV2(HelloData helloData){

        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());

        return "ok";
    }
```
<br>

💡 **@ModelAttribute와 @RequestParam 모두 생략 가능!**
- 스프링은 해당 생략시 다음과 같은 규칙을 적용한다.
- String , int , Integer 같은 단순 타입 = @RequestParam
- 나머지 = @ModelAttribute (argument resolver 로 지정해둔 타입 외)

<br><br>

### 4️⃣ 요청메시지 - 단순 텍스트

- HTTP message body에 데이터를 직접 담아서 요청
    - HTTP API에 주로 사용, JSON, XML, TEXT
    - 데이터 형식은 주로 JSON 사용
    - POST, PUT, PATCH

요청 파라미터와 다르게, HTTP 메시지 바디를 통해 데이터가 직접 데이터로 넘어오는 경우, @RequestParam, @ModelAddtribute를 사용할 수 없음
- 물론 HTML Form 형식으로 전달되는 경우는 요청 파라미터로
인정됨


<br>

```
@PostMapping("/request-body-string-v1")
public void requestBodyString(HttpServletRequest request, HttpServletResponse response) throws IOException{
    ServletInputStream inputStream = request.getInputStream();

    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

    log.info("messageBody={}", messageBody);
    response.getWriter().write("ok");
}

@PostMapping("/request-body-string-v2")
public void requestBodyStringV2(InputStream inputStream, Writer responseWriter) throws IOException{
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

    log.info("messageBody={}", messageBody);
    responseWriter.write("ok");
}
```

- 가장 단순한 텍스트 메시지를 http 메시지 바디에 담아서 전송하고 읽음
- HTTP 메시지 바디의 데이터를 InputStream 을 사용해서 직접 읽을 수 있음

<br>

```
@PostMapping("/request-body-string-v3")
public HttpEntity<String> requestBodyStringV3(HttpEntity<String> httpEntity) throws IOException{

    String body = httpEntity.getBody();
    log.info("messageBody={}", body);
    return new HttpEntity<>("ok");
}
```

- HttpEntity: HTTP header, body 정보를 편리하게 조회
    - 메시지 바디 정보를 직접 조회
    - 요청 파라미터를 조회하는 기능과 관계 없음 @RequestParam X, @ModelAttribute X
- HttpEntity는 응답에도 사용 가능
    - 메시지 바디 정보 직접 반환
    - 헤더 정보 포함 가능
    - view 조회X

<br>

HttpEntity 를 상속받은 다음 객체들도 같은 기능을 제공한다.
- RequestEntity
    - HttpMethod, url 정보가 추가, 요청에서 사용
- ResponseEntity
    - HTTP 상태 코드 설정 가능, 응답에서 사용
    - return new ResponseEntity<String>("Hello World", responseHeaders, HttpStatus.CREATED)

<br>

#### 최종 
```
@ResponseBody
@PostMapping("/request-body-string-v4")
public String requestBodyStringV4(@RequestBody String messageBody) throws IOException{
    log.info("messageBody={}", messageBody);
    return "ok";
}
```

- @RequestBody
    - @RequestBody 를 사용하면 HTTP 메시지 바디 정보를 편리하게 조회할 수 있다. 
    - 참고로 헤더 정보가 필요하다면 HttpEntity 를 사용하거나 @RequestHeader 를 사용하면 된다.
    - 이렇게 메시지 바디를 직접 조회하는 기능은 요청 파라미터를 조회하는 @RequestParam , @ModelAttribute 와는 전혀 관계가 없다.

<br>

- 요청 파라미터 vs HTTP 메시지 바디
    - 요청 파라미터를 조회하는 기능: @RequestParam , @ModelAttribute
    - HTTP 메시지 바디를 직접 조회하는 기능: @RequestBody

- @ResponseBody
    - @ResponseBody 를 사용하면 응답 결과를 HTTP 메시지 바디에 직접 담아서 전달할 수 있다.
    - 물론 이 경우에도 view를 사용하지 않는다.

<br><Br>

### 5️⃣ 요청 메시지 - JSON

```
// 최종 JSON 사용 코드
@ResponseBody
@PostMapping("/request-body-json-v3")
public String requestBodyJsonV3(@RequestBody HelloData helloData) {
    
    log.info("username ={}, age ={}", helloData.getUsername(),helloData.getAge());

    return "ok";
}

// 반환도 객체 반환 가능!
@ResponseBody
    @PostMapping("/request-body-json-v5")
    public HelloData requestBodyJsonV5(@RequestBody HelloData helloData) {

        log.info("username ={}, age ={}", helloData.getUsername(),helloData.getAge());

        return helloData;
    }
```

- **@RequestBody 객체 파라미터**
    - @RequestBody HelloData data
    - @RequestBody 에 직접 만든 객체를 지정할 수 있다.
    - HttpEntity , @RequestBody 를 사용하면 HTTP 메시지 컨버터가 HTTP 메시지 바디의 내용을 우리가 원하는 문자나 객체 등으로 변환해줌
    - 생략 불가능!

<br>

➡️ HelloData에 @RequestBody 를 생략하면 @ModelAttribute 가 적용되어버린다.

HelloData data @ModelAttribute HelloData data
따라서 생략하면 HTTP 메시지 바디가 아니라 요청 파라미터를 처리하게 된다

<br>

> 주의
> HTTP 요청시에 content-type이 application/json인지 꼭! 확인해야 한다. 그래야 JSON을 처리할 수
있는 HTTP 메시지 컨버터가 실행된다.

<br>

#### 🔎 @RequestBody 요청
: JSON 요청 → HTTP 메시지 컨버터 → 객체

#### 🔎 @ResponseBody 응답
: 객체 → HTTP 메시지 컨버터 → JSON 응답