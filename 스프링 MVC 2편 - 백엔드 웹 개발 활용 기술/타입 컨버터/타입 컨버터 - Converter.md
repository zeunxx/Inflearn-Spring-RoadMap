## 타입 컨버터 - Converter

타입 컨버터를 사용하려면 `org.springframework.core.convert.converter.Converter`
인터페이스를 구현하면 된다.

```
@Slf4j
public class StringToIntegerConverter implements Converter<String,Integer> {

    @Override
    public Integer convert(String source) {

        log.info("convert source={}", source);
        return Integer.valueOf(source);
    }
}


```

String Integer 로 변환하기 때문에 소스가 String 이 된다. 이 문자를
Integer.valueOf(source) 를 사용해서 숫자로 변경한 다음에 변경된 숫자를 반환하면 된다

<br>

```
@Test
void stringToInteger(){
    StringToIntegerConverter converter = new StringToIntegerConverter();
    Integer result = converter.convert("10");

    assertThat(result).isEqualTo(10);
}
```


```
@Getter
@EqualsAndHashCode
public class IpPort {

    private String ip;
    private int port;

    public IpPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}

@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {
    @Override
    public IpPort convert(String source) {

        log.info("convert source={}",source);
        //127.0.0.1:8080 -> IpPort 객체
        String[] split = source.split(":");

        String ip = split[0];
        int port = Integer.parseInt(split[1]);
        return new IpPort(ip,port);
    }
}
```

타입 컨버터 인터페이스가 단순해서 이해하기 어렵지 않을 것이다.
그런데 이렇게 타입 컨버터를 하나하나 직접 사용하면, 개발자가 직접 컨버팅 하는 것과 큰 차이가 없다.
타입 컨버터를 등록하고 관리하면서 편리하게 변환 기능을 제공하는 역할을 하는 무언가가 필요하다.



> 참고
> 스프링은 용도에 따라 다양한 방식의 타입 컨버터를 제공한다. 
>
> Converter 기본 타입 컨버터
> ConverterFactory 전체 클래스 계층 구조가 필요할 때
> GenericConverter 정교한 구현, 대상 필드의 애노테이션 정보 사용 가능
> ConditionalGenericConverter 특정 조건이 참인 경우에만 실행

<br><Br>

### 컨버전 서비스 - ConversionService

이렇게 타입 컨버터를 하나하나 직접 찾아서 타입 변환에 사용하는 것은 매우 불편하다. 

그래서 스프링은 개별 컨버터를 모아두고 그것들을 묶어서 편리하게 사용할 수 있는 기능을 제공하는데, 
이것이 바로 컨버전 서비스( ConversionService )이다


```
@Test
void conversionService(){
    //등록
    DefaultConversionService conversionService = new DefaultConversionService();
    conversionService.addConverter(new StringToIntegerConverter());
    conversionService.addConverter(new IntegerToStringConverter());
    conversionService.addConverter(new IpPortToStringConverter());
    conversionService.addConverter(new StringToIpPortConverter());

    //사용
    Assertions.assertThat(conversionService.convert("10", Integer.class)).isEqualTo(10);
    Assertions.assertThat(conversionService.convert(10, String.class)).isEqualTo("10");
    Assertions.assertThat(conversionService.convert("127.0.0.1:8080", IpPort.class)).isEqualTo(new IpPort("127.0.0.1",8080));
    Assertions.assertThat(conversionService.convert(new IpPort("127.0.0.1",8080), String.class)).isEqualTo("127.0.0.1:8080");

}
```
- DefaultConversionService 는 ConversionService 인터페이스를 구현했는데, 추가로 컨버터를 등록하는 기능도 제공한다.


<br>

#### 등록과 사용 분리

컨버터를 등록할 때는 `StringToIntegerConverter` 같은 타입 컨버터를 명확하게 알아야 한다. 
반면에 컨버터를 사용하는 입장에서는 타입 컨버터를 전혀 몰라도 된다. 

타입 컨버터들은 모두 컨버전 서비스 내부에 숨어서 제공된다. 따라서 타입을 변환을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존하면 된다. 

물론 컨버전 서비스를 등록하는 부분과 사용하는 부분을 분리하고 의존관계 주입을 사용해야 한다.

<br>

####  컨버전 서비스 사용

`Integer value = conversionService.convert("10", Integer.class)`


<br>

#### 인터페이스 분리 원칙 - ISP(Interface Segregation Principle)

인터페이스 분리 원칙은 클라이언트가 자신이 이용하지 않는 메서드에 의존하지 않아야 한다.


`DefaultConversionService` 는 다음 두 인터페이스를 구현했다.
- ConversionService : 컨버터 사용에 초점
- ConverterRegistry : 컨버터 등록에 초점

<br>

이렇게 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와 컨버터를 등록하고 관리하는 클라이언트의 관심사를 명확하게 분리할 수 있다. 

특히 컨버터를 사용하는 클라이언트는 ConversionService 만
의존하면 되므로, 컨버터를 어떻게 등록하고 관리하는지는 전혀 몰라도 된다. 

결과적으로 컨버터를 사용하는 클라이언트는 꼭 필요한 메서드만 알게된다. 이렇게 인터페이스를 분리하는 것을 ISP 라 한다.


<br>

스프링은 내부에서 ConversionService 를 사용해서 타입을 변환한다. 예를 들어서 앞서 살펴본
@RequestParam 같은 곳에서 이 기능을 사용해서 타입을 변환한다.

<br><br>

### 스프링에 Converter 적용하기

```
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new IntegerToStringConverter());
        registry.addConverter(new IpPortToStringConverter());
        registry.addConverter(new StringToIpPortConverter());
    }
}
```

스프링은 내부에서 ConversionService 를 제공한다. 우리는 WebMvcConfigurer 가 제공하는
addFormatters() 를 사용해서 추가하고 싶은 컨버터를 등록하면 된다. 

이렇게 하면 스프링은 내부에서 사용하는 ConversionService 에 컨버터를 추가해준다.


```
@GetMapping("/hello-v2")
public String helloV2(@RequestParam Integer data){
    System.out.println("data = " + data);
    return "ok";
}
```
<img width="360" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/a64877c8-9ab1-4461-94f7-3d227894f93a">


?data=10 의 쿼리 파라미터는 문자이고 이것을 Integer data 로 변환하는 과정이 필요하다.
실행해보면 직접 등록한 StringToIntegerConverter 가 작동하는 로그를 확인할 수 있다.


그런데 생각해보면 StringToIntegerConverter 를 등록하기 전에도 이 코드는 잘 수행되었다. 그것은
스프링이 내부에서 수 많은 기본 컨버터들을 제공하기 때문이다. 컨버터를 추가하면 추가한 컨버터가 기본 컨버터 보다 높은 우선순위를 가진다.



```
@GetMapping("/ip-port")
public String ipPort(@RequestParam IpPort ipPort){
    System.out.println("ipPort.getIp() = " + ipPort.getIp());
    System.out.println("ipPort.getPort() = " + ipPort.getPort());
    return "ok";
}
```
<img width="176" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/e52acaf5-b18e-45c4-83c3-de10d629a8e0">


?ipPort=127.0.0.1:8080 쿼리 스트링이 
@RequestParam IpPort ipPort 에서 객체 타입으로 잘 변환 된 것을 확인할 수 있다.



#### 처리 과정
@RequestParam 은 @RequestParam 을 처리하는 `ArgumentResolver `인 `RequestParamMethodArgumentResolver` 에서 
ConversionService 를 사용해서 타입을 변환한다. 

부모 클래스와 다양한 외부 클래스를 호출하는 등 복잡한 내부 과정을 거치기 때문에 대략 이렇게 처리되는
것으로 이해해도 충분하다. 만약 더 깊이있게 확인하고 싶으면 IpPortConverter 에 디버그 브레이크
포인트를 걸어서 확인해보자.


<br><br>


### 뷰 템플릿에 컨버터 적용하기

이번에는 뷰 템플릿에 컨버터를 적용하는 방법을 알아보자.
타임리프는 렌더링 시에 컨버터를 적용해서 렌더링 하는 방법을 편리하게 지원한다.
이전까지는 문자를 객체로 변환했다면, 이번에는 그 반대로 객체를 문자로 변환하는 작업을 확인할 수 있다.



```
@Controller
public class ConverterController {

    @GetMapping("/converter-view")
    public String converterView(Model model){
        model.addAttribute("number",10000);
        model.addAttribute("ipPort",new IpPort("127.0.0.1",8080));
        return "converter-view";
    }
}

```



> 타임리프는 ${{...}} 를 사용하면 자동으로 컨버전 서비스를 사용해서 변환된 결과를 출력해준다. 
> 물론 스프링과 통합 되어서 스프링이 제공하는 컨버전 서비스를 사용하므로, 우리가 등록한 컨버터들을 사용할 수 있다.

- 변수 표현식 : ${...}
- 컨버전 서비스 적용 : ${{...}}


<img width="340" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/38493cdf-e1eb-4199-aab4-b7d3b449d1c4">


<img width="579" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/75cbb355-2001-4b19-ab23-7c1be30a90b6">


- ${{number}} : 뷰 템플릿은 데이터를 문자로 출력한다. 따라서 컨버터를 적용하게 되면 Integer 타입인
10000 을 String 타입으로 변환하는 컨버터인 IntegerToStringConverter 를 실행하게 된다. 이
부분은 컨버터를 실행하지 않아도 타임리프가 숫자를 문자로 자동으로 변환히기 때문에 컨버터를 적용할
때와 하지 않을 때가 같다.

- ${{ipPort}} : 뷰 템플릿은 데이터를 문자로 출력한다. 따라서 컨버터를 적용하게 되면 IpPort 타입을
String 타입으로 변환해야 하므로 IpPortToStringConverter 가 적용된다. 그 결과 127.0.0.1:8080가 출력된다.

<br><br>



<img width="277" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/29d1abcc-c763-43e8-88c9-30f79a4b975c">


타임리프의 th:field 는 앞서 설명했듯이 id , name 를 출력하는 등 다양한 기능이 있는데, 여기에
컨버전 서비스도 함께 적용된다.

- th:field 가 자동으로 컨버전 서비스를 적용해주어서 ${ipPort} 로 해도 ${{ipPort}} 처럼 적용이 되었다. 따라서 IpPort String 으로 변환된다.

<br><Br>

