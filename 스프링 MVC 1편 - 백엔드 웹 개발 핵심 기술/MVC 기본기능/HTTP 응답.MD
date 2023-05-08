## HTTP 응답

### HTTP 응답 - 정적 리소스, 뷰 템플릿

스프링(서버)에서 응답 데이터를 만드는 방법은 크게 3가지이다.

1. 정적 리소스
    - 예) 웹 브라우저에 정적인 HTML, css, js를 제공할 때는, 정적 리소스를 사용한다.
2. 뷰 템플릿 사용
    - 예) 웹 브라우저에 동적인 HTML을 제공할 때는 뷰 템플릿을 사용한다.
3. HTTP 메시지 사용
    - HTTP API를 제공하는 경우에는 HTML이 아니라 데이터를 전달해야 하므로, HTTP 메시지 바디에 JSON 같은 형식으로 데이터를 실어 보낸다

<BR>

### 정적 리소스 

스프링 부트는 클래스패스의 다음 디렉토리에 있는 정적 리소스를 제공한다.
`/static` , `/public` , `/resources` , `/META-INF/resources`

- src/main/resources 는 리소스를 보관하는 곳이고, 또 클래스패스의 시작 경로이다.
따라서 다음 디렉토리에 리소스를 넣어두면 스프링 부트가 정적 리소스로 서비스를 제공한다

<BR>

- 정적 리소스 경로 : `src/main/resources/static`

    - 다음 경로에 파일이 들어있으면 `src/main/resources/static/basic/hello-form.html` 웹 브라우저에서 다음과 같이 실행하면 된다.

    - `http://localhost:8080/basic/hello-form.html`
- 정적 리소스는 해당 파일을 변경 없이 그대로 서비스하는 것이다

<BR>

### 뷰 템플릿

- 뷰 템플릿 경로 : `src/main/resources/templates`

```
@RequestMapping("/response-view-v1")
public ModelAndView responseViewV1(){
    ModelAndView mav = new ModelAndView("response/hello")
            .addObject("data","hello");

    return mav;
}


// string을 반환하는 경우
@RequestMapping("/response-view-v2")
public String responseViewV2(Model model){
    model.addAttribute("data","hello");
    return "response/hello";
}

// void를 반호나하는 경우
@RequestMapping("/response/hello")
public void responseViewV3(Model model){
    model.addAttribute("data","hello");
}
```

✅ **String을 반환하는 경우 - View or HTTP 메시지**
- @ResponseBody 가 없으면 response/hello 로 뷰 리졸버가 실행되어서 뷰를 찾고, 렌더링 한다.
- @ResponseBody 가 있으면 뷰 리졸버를 실행하지 않고, HTTP 메시지 바디에 직접 response/hello 라는
문자가 입력된다. (string 그자체 반환)
- 여기서는 뷰의 논리 이름인 response/hello 를 반환하면 다음 경로의 뷰 템플릿이 렌더링 되는 것을
확인할 수 있다.
- 실행: templates/response/hello.html

<br>

✅ **void를 반환하는 경우**
- @Controller 를 사용하고, HttpServletResponse , OutputStream(Writer) 같은 HTTP 메시지 바디를 처리하는 파라미터가 없으면 요청 URL을 참고해서 논리 뷰 이름으로 사용
    - 요청 URL: /response/hello
    - 실행: templates/response/hello.html
- 참고로 이 방식은 명시성이 너무 떨어지고 이렇게 딱 맞는 경우도 많이 없어서, 권장하지 않는다.

<br><Br>


### HTTP 응답 - HTTP API,메시지 바디에 직접 입력

HTTP API를 제공하는 경우에는 HTML이 아니라 데이터를 전달해야 하므로, HTTP 메시지 바디에 JSON 
같은 형식으로 데이터를 실어 보낸다.

HTTP 요청에서 응답까지 대부분 다루었으므로 이번시간에는 정리를 해보자.

```
@GetMapping("/response-body-string-v1")
public void responseBodyV1(HttpServletResponse response) throws IOException {
    response.getWriter().write("ok");
}

@GetMapping("/response-body-string-v2")
public ResponseEntity<String> responseBodyV2() throws IOException {
    return new ResponseEntity<>("ok", HttpStatus.OK);
}

@ResponseBody
@GetMapping("/response-body-string-v3")
public String responseBodyV3() {
    return "ok";
}

@GetMapping("/response-body-json-v1")
public ResponseEntity<HelloData> responseBodyJsonV1(){
    HelloData helloData = new HelloData();
    helloData.setUsername("hello");
    helloData.setAge(10);
    return new ResponseEntity<>(helloData, HttpStatus.OK);
}

@ResponseStatus(HttpStatus.OK)
@ResponseBody
@GetMapping("/response-body-json-v2")
public HelloData responseBodyJsonV2(){
    HelloData helloData = new HelloData();
    helloData.setUsername("hello");
    helloData.setAge(10);
    return helloData;
}
```

- responseBodyV1
    - 서블릿을 직접 다룰 때 처럼 HttpServletResponse 객체를 통해서 HTTP 메시지 바디에 직접 ok 응답 메시지를 전달한다.
    - response.getWriter().write("ok")
- responseBodyV2
    - ResponseEntity 엔티티는 HttpEntity 를 상속 받았는데, HttpEntity는 HTTP 메시지의 헤더, 바디 정보를 가지고 있다. ResponseEntity 는 여기에 더해서 HTTP 응답 코드를 설정할 수 있다.
    - HttpStatus.CREATED 로 변경하면 201 응답이 나가는 것을 확인할 수 있다.
- responseBodyV3
    - @ResponseBody 를 사용하면 view를 사용하지 않고, HTTP 메시지 컨버터를 통해서 HTTP 메시지를 직접 입력할 수 있다. ResponseEntity 도 동일한 방식으로 동작한다.
<br>

- responseBodyJsonV1
    - ResponseEntity 를 반환한다. HTTP 메시지 컨버터를 통해서 JSON 형식으로 변환되어서 반환된다.
- responseBodyJsonV2
    - ResponseEntity 는 HTTP 응답 코드를 설정할 수 있는데, @ResponseBody 를 사용하면 이런 것을 설정하기 까다롭다.
    - @ResponseStatus(HttpStatus.OK) 애노테이션을 사용하면 응답 코드도 설정할 수 있다.
<br>

<br>

### HTTP 메시지 컨버터

<img width="405" alt="image" src="https://user-images.githubusercontent.com/81572478/236772958-990c93b1-3577-4401-83a3-0968dc916297.png">

- @ResponseBody 를 사용
    - HTTP의 BODY에 문자 내용을 직접 반환
    - viewResolver 대신에 HttpMessageConverter 가 동작
    - 기본 문자처리: StringHttpMessageConverter (STRING 반환)
    - 기본 객체처리: MappingJackson2HttpMessageConverter (객체 반환)

<br>

**스프링 MVC는 다음의 경우에 HTTP 메시지 컨버터를 적용한다.**
HTTP 요청: `@RequestBody` , `HttpEntity(RequestEntity)` , 
HTTP 응답: `@ResponseBody` , `HttpEntity(ResponseEntity)`

<br>

스프링 부트는 다양한 메시지 컨버터를 제공하는데, 대상 클래스 타입과 미디어 타입 둘을 체크해서
사용여부를 결정한다. 만약 만족하지 않으면 다음 메시지 컨버터로 우선순위가 넘어간다.

<br>

몇가지 주요한 메시지 컨버터를 알아보자.
- ByteArrayHttpMessageConverter : byte[] 데이터를 처리한다.
    - 클래스 타입: byte[] , 미디어타입: */* ,
    - 요청 예) @RequestBody byte[] data
    - 응답 예) @ResponseBody return byte[] 쓰기 미디어타입 application/octet-stream
- StringHttpMessageConverter : String 문자로 데이터를 처리한다.
    - 클래스 타입: String , 미디어타입: */*
    - 요청 예) @RequestBody String data
    - 응답 예) @ResponseBody return "ok" 쓰기 미디어타입 text/plain
- MappingJackson2HttpMessageConverter : application/json
    - 클래스 타입: 객체 또는 HashMap , 미디어타입 application/json 관련
    - 요청 예) @RequestBody HelloData data
    - 응답 예) @ResponseBody return helloData 쓰기 미디어타입 application/json 관련

<br>

### HTTP 요청 데이터 읽기
- HTTP 요청이 오고, 컨트롤러에서 @RequestBody , HttpEntity 파라미터를 사용한다.
- 메시지 컨버터가 메시지를 읽을 수 있는지 확인하기 위해 canRead() 를 호출한다.
    - 대상 클래스 타입을 지원하는가.
        - 예) @RequestBody 의 대상 클래스 ( byte[] , String , HelloData )
    - HTTP 요청의 **Content-Type** 미디어 타입을 지원하는가.
        - 예) text/plain , application/json , */*
- canRead() 조건을 만족하면 read() 를 호출해서 객체 생성하고, 반환한다.

### HTTP 응답 데이터 생성
- 컨트롤러에서 @ResponseBody , HttpEntity 로 값이 반환된다. 
- 메시지 컨버터가 메시지를 쓸 수 있는지 확인하기 위해 canWrite() 를 호출한다.
    - 대상 클래스 타입을 지원하는가.
        - 예) return의 대상 클래스 ( byte[] , String , HelloData )
    - HTTP 요청의 **Accept** 미디어 타입을 지원하는가.(더 정확히는 @RequestMapping 의 produces )
        - 예) text/plain , application/json , */*
- canWrite() 조건을 만족하면 write() 를 호출해서 HTTP 응답 메시지 바디에 데이터를 생성한다.

<br><br>

### 요청 매핑 핸들러 어댑터 구조

<img width="385" alt="image" src="https://user-images.githubusercontent.com/81572478/236778813-33c0efd7-d74c-4fa6-9412-e0f5ac3a5aca.png">

- 모든 비밀은 애노테이션 기반의 컨트롤러, 그러니까 @RequestMapping 을 처리하는 핸들러 어댑터인
`RequestMappingHandlerAdapter` (요청 매핑 헨들러 어뎁터)에 있다.

<img width="382" alt="image" src="https://user-images.githubusercontent.com/81572478/236779022-85d83187-04f5-4820-8d07-d83d5b274d81.png">


**ArgumentResolver**

HttpServletRequest , Model,  @RequestParam , @ModelAttribute 같은 애노테이션
그리고 @RequestBody , HttpEntity 같은 HTTP 메시지를 처리하는 부분까지 컨트롤러는 매우 큰 유연함을
보여주었다.

이렇게 파라미터를 유연하게 처리할 수 있는 이유가 바로 ArgumentResolver 덕분이다.

<br>

➡️ 애노테이션 기반 컨트롤러를 처리하는 RequestMappingHandlerAdapter 는 바로 이
**ArgumentResolver 를 호출해서 컨트롤러(핸들러)가 필요로 하는 다양한 파라미터의 값(객체)을 생성**한다. 

그리고 이렇게 파리미터의 값이 모두 준비되면 컨트롤러를 호출하면서 값을 넘겨준다.

<br>

**ReturnValueHandler**

HandlerMethodReturnValueHandler 를 줄여서 ReturnValueHandler 라 부른다.

ArgumentResolver 와 비슷한데, 이것은 응답 값을 변환하고 처리한다.

컨트롤러에서 String으로 뷰 이름을 반환해도, 동작하는 이유가 바로 ReturnValueHandler 덕분이다.

스프링은 10여개가 넘는 ReturnValueHandler 를 지원한다.

예) ModelAndView , @ResponseBody , HttpEntity , String

<br>

<img width="383" alt="image" src="https://user-images.githubusercontent.com/81572478/236781346-f54a6d8c-fcc1-4a84-a437-b28a8e1a63ef.png">


- **요청의 경우** @RequestBody 를 처리하는 ArgumentResolver 가 있고, HttpEntity 를 처리하는
ArgumentResolver 가 있다. 이 ArgumentResolver 들이 HTTP 메시지 컨버터를 사용해서 필요한 객체를 생성하는 것이다. 

- **응답의 경우** @ResponseBody 와 HttpEntity 를 처리하는 ReturnValueHandler 가 있다. 그리고
여기에서 HTTP 메시지 컨버터를 호출해서 응답 결과를 만든다.

<br>