## 검증2 - Bean Validation


검증 기능을 지금처럼 매번 코드로 작성하는 것은 상당히 번거롭다. 

특히 특정 필드에 대한 검증 로직은
대부분 빈 값인지 아닌지, 특정 크기를 넘는지 아닌지와 같이 매우 일반적인 로직이다.

<Br>

이런 검증 로직을 모든 프로젝트에 적용할 수 있게 공통화하고, 표준화 한 것이 바로 Bean Validation 
이다.

Bean Validation을 잘 활용하면, 애노테이션 하나로 검증 로직을 매우 편리하게 적용할 수 있다

### Bean Validation 이란?
먼저 Bean Validation은 특정한 구현체가 아니라 Bean Validation 2.0(JSR-380)이라는 기술 표준이다. 
쉽게 이야기해서 검증 애노테이션과 여러 인터페이스의 모음이다. 마치 JPA가 표준 기술이고 그 구현체로
하이버네이트가 있는 것과 같다.
<br>

Bean Validation을 구현한 기술중에 일반적으로 사용하는 구현체는 하이버네이트 Validator이다. 이름이
하이버네이트가 붙어서 그렇지 ORM과는 관련이 없다

<br>


```
@Data
public class Item {

    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min=1000, max=1000000 )
    private Integer price;

    @NotNull
    @Max(9999)
    private Integer quantity;
```

💡 검증 애노테이션
- @NotBlank : 빈값 + 공백만 있는 경우를 허용하지 않는다.
- @NotNull : null 을 허용하지 않는다.
- @Range(min = 1000, max = 1000000) : 범위 안의 값이어야 한다.
- @Max(9999) : 최대 9999까지만 허용한다

<br>

### Bean Validation - 스프링 적용

#### 스프링 MVC는 어떻게 Bean Validator를 사용?
스프링 부트가 spring-boot-starter-validation 라이브러리를 넣으면 자동으로 Bean Validator를 인지하고 스프링에 통합한다

<BR>

#### 스프링 부트는 자동으로 글로벌 Validator로 등록한다.
LocalValidatorFactoryBean 을 글로벌 Validator로 등록한다. 이 Validator는 @NotNull 같은 애노테이션을 보고 검증을 수행한다. 

이렇게 글로벌 Validator가 적용되어 있기 때문에, **@Valid , @Validated** 만 적용하면 된다.

<BR>

검증 오류가 발생하면, FieldError , ObjectError 를 생성해서 BindingResult 에 담아준다.

<BR>

📌 **주의!**
다음과 같이 직접 글로벌 Validator를 직접 등록하면 스프링 부트는 Bean Validator를 글로벌 Validator 로 등록하지 않는다. 따라서 애노테이션 기반의 빈 검증기가 동작하지 않는다. 

다음 부분은 제거하자.

```
@SpringBootApplication
public class ItemServiceApplication implements WebMvcConfigurer {
 // 글로벌 검증기 추가
@Override
public Validator getValidator() {
return new ItemValidator();
}
 // ...
}
```

<BR>

#### 검증 순서
1. @ModelAttribute 각각의 필드에 타입 변환 시도
    1. 성공하면 다음으로
    2. 실패하면 typeMismatch 로 FieldError 추가
2. Validator 적용

<BR>

#### 바인딩에 성공한 필드만 Bean Validation 적용
BeanValidator는 바인딩에 실패한 필드는 BeanValidation을 적용하지 않는다.
생각해보면 타입 변환에 성공해서 바인딩에 성공한 필드여야 BeanValidation 적용이 의미 있다.
(일단 모델 객체에 바인딩 받는 값이 정상으로 들어와야 검증도 의미가 있다.)
<BR>

@ModelAttribute 각각의 필드 타입 변환시도 변환에 성공한 필드만 BeanValidation 적용

<BR>

예)
- itemName 에 문자 "A" 입력 타입 변환 성공 itemName 필드에 BeanValidation 적용
- price 에 문자 "A" 입력 "A"를 숫자 타입 변환 시도 실패 typeMismatch FieldError 추가 ➡️ price 필드는 BeanValidation 적용 X

<BR><bR>

### Bean Validation - 에러 코드

Bean Validation이 기본으로 제공하는 오류 메시지를 좀 더 자세히 변경하고 싶으면?

<BR>

NotBlank 라는 오류 코드를 기반으로 에러가 나면  MessageCodesResolver 를 통해 다양한 메시지 코드가 순서대로
생성된다.

<BR>

- @NotBlank
    - NotBlank.item.itemName
    - NotBlank.itemName
    - NotBlank.java.lang.String
    - NotBlank
<BR>

- @Range
    - Range.item.price
    - Range.price
    - Range.java.lang.Integer
    - Range 

<BR>

#### 메시지 등록
```
#Bean Validation 추가
NotBlank={0} 공백X 
Range={0}, {2} ~ {1} 허용
Max={0}, 최대 {1}
```
➡️ {0} 은 필드명이고, {1} , {2} ...은 각 애노테이션 마다 다르다.

<BR>

#### BeanValidation 메시지 찾는 순서
1. 생성된 메시지 코드 순서대로 messageSource 에서 메시지 찾기
2. 애노테이션의 message 속성 사용 @NotBlank(message = "공백! {0}")
3. 라이브러리가 제공하는 기본 값 사용 : 공백일 수 없습니다.

<BR>

**애노테이션의 message 사용 예**
```
@NotBlank(message = "공백은 입력할 수 없습니다.")
private String itemName;
```

