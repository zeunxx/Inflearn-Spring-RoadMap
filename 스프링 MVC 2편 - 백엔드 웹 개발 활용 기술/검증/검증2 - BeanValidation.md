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

<br><br>

### Bean Validation - 오브젝트 오류

Bean Validation에서 특정 필드( FieldError )가 아닌 해당 오브젝트 관련 오류( ObjectError )는
어떻게 처리할 수 있을까?
다음과 같이 @ScriptAssert() 를 사용하면 된다.

```
@Data
@ScriptAssert(lang = "javascript", script="_this.price * _this.quantity >= 10000", message="총 합이 10000원이 넘게 입력해주세요")
public class Item {

    private Long id;
    ...

}
```

➡️ 그런데 실제 사용해보면 제약이 많고 복잡하다. 그리고 실무에서는 검증 기능이 해당 객체의 범위를 넘어서는 경우들도 종종 등장하는데, 그런 경우 대응이 어렵다

<br>

오브젝트 오류(글로벌 오류)의 경우 오브젝트 오류 관련 부분만 **직접 자바 코드로 작성**하는 것을 권장한다.

<br><Br>

```
@PostMapping("/add")
public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
    
    // 특정 필드가 아닌 복합 룰 검증
    if(item.getPrice()!=null && item.getQuantity()!=null){
        int resultPrice = item.getPrice() * item.getQuantity();
        if(resultPrice < 10000){
            bindingResult.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
        }
    }

    // 검증에 실패하면 다시 입력 폼으로
    if(bindingResult.hasErrors()){
        log.info("errors = {}",bindingResult);
        return "validation/v3/addForm";
    }

    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v3/items/{itemId}";
}
```

<br><Br>

### Bean Validation - 한계

데이터를 등록할 때와 수정할 때는 요구사항이 다를 수 있다

#### 💡 등록시 기존 요구사항
- 타입 검증
    - 가격, 수량에 문자가 들어가면 검증 오류 처리
- 필드 검증
    - 상품명: 필수, 공백X
    - 가격: 1000원 이상, 1백만원 이하
    - 수량: 최대 9999
- 특정 필드의 범위를 넘어서는 검증
    - 가격 * 수량의 합은 10,000원 이상

<br>

#### 💡 수정시 요구사항
- 등록시에는 quantity 수량을 최대 9999까지 등록할 수 있지만 수정시에는 **수량을 무제한으로 변경**할 수 있다.
- 등록시에는 id 에 값이 없어도 되지만, 수정시에는 **id 값이 필수**이다.

```
@Data
public class Item {

    @NotNull // 수정 요구사항 반영
    private Long id;

    @NotBlank(message = "공백 X")
    private String itemName;

    @NotNull
    @Range(min=1000, max=1000000 )
    private Integer price;

    @NotNull
//    @Max(9999) // 수정 요구사항 반영
    private Integer quantity;
```
- 수정 요구사항 반영시 등록할땐 등록 요구사항에 반하게 됨

<br>

> 참고
> 현재 구조에서는 수정시 item 의 id 값은 항상 들어있도록 로직이 구성되어 있다. 그래서 검증하지 않아도
된다고 생각할 수 있다. 그런데 HTTP 요청은 언제든지 악의적으로 변경해서 요청할 수 있으므로 서버에서
항상 검증해야 한다. 예를 들어서 HTTP 요청을 변경해서 item 의 id 값을 삭제하고 요청할 수도 있다. 
따라서 최종 검증은 서버에서 진행하는 것이 안전한다.

<br>

결과적으로 item 은 등록과 수정에서 검증 조건의 충돌이 발생하고, 등록과 수정은 같은 BeanValidation 을 적용할 수 없다. 이 문제를 어떻게 해결할 수 있을까?

<br><Br>

### Bean Validation - groups

동일한 모델 객체를 등록할 때와 수정할 때 각각 다르게 검증하는 방법을 알아보자

<br>

#### 📌 방법 2가지
- BeanValidation의 groups 기능을 사용한다.
- Item을 직접 사용하지 않고, ItemSaveForm, ItemUpdateForm 같은 폼 전송을 위한 별도의 모델 객체를 만들어서 사용한다.

<br>

#### BeanValidation groups 기능 사용
이런 문제를 해결하기 위해 Bean Validation은 groups라는 기능을 제공한다.

예를 들어서 등록시에 검증할 기능과 수정시에 검증할 기능을 각각 그룹으로 나누어 적용할 수 있다.

<br>

```
✅ domain

public interface SaveCheck {
}
public interface UpdateCheck {
}


@Data
public class Item {

    @NotNull(groups = UpdateCheck.class)
    private Long id;

    @NotBlank(message = "공백 X", groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;

    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    @Range(min=1000, max=1000000 )
    private Integer price;

    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    @Max(value = 9999, groups = SaveCheck.class)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}


✅ controller

@PostMapping("/add")
public String addItemV2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

    // 특정 필드가 아닌 복합 룰 검증
    if(item.getPrice()!=null && item.getQuantity()!=null){
        int resultPrice = item.getPrice() * item.getQuantity();
        if(resultPrice < 10000){
            bindingResult.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
        }
    }

    // 검증에 실패하면 다시 입력 폼으로
    if(bindingResult.hasErrors()){
        log.info("errors = {}",bindingResult);
        return "validation/v3/addForm";
    }

    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v3/items/{itemId}";
}


@PostMapping("/{itemId}/edit")
public String editV2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {

    // 특정 필드가 아닌 복합 룰 검증
    if(item.getPrice()!=null && item.getQuantity()!=null){
        int resultPrice = item.getPrice() * item.getQuantity();
        if(resultPrice < 10000){
            bindingResult.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
        }
    }

    if(bindingResult.hasErrors()){
        log.info("errors={}", bindingResult);
        return "validation/v3/editForm";
    }

    itemRepository.update(itemId, item);
    return "redirect:/validation/v3/items/{itemId}";
}
```

#### 정리
groups 기능을 사용해서 등록과 수정시에 각각 다르게 검증을 할 수 있었다. 그런데 groups 기능을
사용하니 Item 은 물론이고, 전반적으로 복잡도가 올라갔다.

사실 groups 기능은 실제 잘 사용되지는 않는데, 그 이유는 실무에서는 주로 다음에 등장하는 등록용 폼
객체와 수정용 폼 객체를 분리해서 사용하기 때문이다.

<br><Br>

#### Form 전송 객체 분리 - 소개

실무에서는 groups 를 잘 사용하지 않는데, 그 이유가 다른 곳에 있다. 바로 등록시 폼에서 전달하는 데이터가 Item 도메인 객체와 딱 맞지 않기 때문이다.

<br>

실무에서는 회원 등록시 회원과 관련된 데이터만 전달받는 것이 아니라, 약관 정보도 추가로 받는 등 Item 과 관계없는 수 많은 부가 데이터가 넘어온다.

<br>

그래서 보통 Item 을 직접 전달받는 것이 아니라, 복잡한 폼의 데이터를 컨트롤러까지 전달할 별도의
객체를 만들어서 전달한다. 예를 들면 ItemSaveForm 이라는 폼을 전달받는 전용 객체를 만들어서
@ModelAttribute 로 사용한다. 이것을 통해 컨트롤러에서 폼 데이터를 전달 받고, 이후 컨트롤러에서
필요한 데이터를 사용해서 Item 을 생성한다.

<br>

1. 폼 데이터 전달에 Item 도메인 객체 사용
    `HTML Form -> Item -> Controller -> Item -> Repository`
    - 장점: Item 도메인 객체를 컨트롤러, 리포지토리 까지 직접 전달해서 중간에 Item을 만드는 과정이 없어서 간단하다.

    - 단점: 간단한 경우에만 적용할 수 있다. 수정시 검증이 중복될 수 있고, groups를 사용해야 한다

<br>

2. 폼 데이터 전달을 위한 별도의 객체 사용
    `HTML Form -> ItemSaveForm -> Controller -> Item 생성 -> Repository`
    - 장점: 전송하는 폼 데이터가 복잡해도 거기에 맞춘 별도의 폼 객체를 사용해서 데이터를 전달 받을 수 있다. 보통 등록과, 수정용으로 별도의 폼 객체를 만들기 때문에 검증이 중복되지 않는다.
    - 단점: 폼 데이터를 기반으로 컨트롤러에서 Item 객체를 생성하는 변환 과정이 추가된다.

<br>

수정의 경우 등록과 수정은 완전히 다른 데이터가 넘어온다. 생각해보면 회원 가입시 다루는 데이터와
수정시 다루는 데이터는 범위에 차이가 있다. 예를 들면 등록시에는 로그인id, 주민번호 등등을 받을 수
있지만, 수정시에는 이런 부분이 빠진다. 그리고 검증 로직도 많이 달라진다. 

그래서 ItemUpdateForm 이라는 별도의 객체로 데이터를 전달받는 것이 좋다.

<br>

따라서 이렇게 폼 **데이터 전달을 위한 별도의 객체**를 사용하고, 등록, 수정용 폼 객체를 나누면 등록, 수정이 완전히 분리되기 때문에 groups 를 적용할 일은 드물다.

<Br>

<Br>

**Q: 이름은 어떻게 지어야 하나요?**

이름은 의미있게 지으면 된다. ItemSave 라고 해도 되고, ItemSaveForm , ItemSaveRequest , ItemSaveDto 등으로 사용해도 된다. 중요한 것은 일관성이다.

<Br>

**Q: 등록, 수정용 뷰 템플릿이 비슷한데 합치는게 좋을까요?**

한 페이지에 그러니까 뷰 템플릿 파일을 등록과 수정을 합치는게 좋을지 고민이 될 수 있다. 각각 장단점이
있으므로 고민하는게 좋지만, 어설프게 합치면 수 많은 분기문(등록일 때, 수정일 때) 때문에 나중에
유지보수에서 고통을 맛본다.

이런 어설픈 분기문들이 보이기 시작하면 분리해야 할 신호이다.

<br><Br>

### Form 전송 객체 분리 - 개발


```
✅ save form

@Data
public class ItemSaveForm {

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min=1000,max=1000000)
    private Integer price;

    @NotNull
    @Max(9999)
    private Integer quantity;
}


✅ update form

@Data
public class ItemUpdateForm {

    @NotNull
    private int id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min=1000,max=1000000)
    private Integer price;

    // 수정시 수량은 자유롭게 변경할 수 있다
    private Integer quantity;
}


✅ controller - 저장,수정 동일 방식

@PostMapping("/add")
public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

    ... // 검증 로직

    // 성공 로직
    Item item = new Item();
    item.setItemName(form.getItemName());
    item.setPrice(form.getPrice());
    item.setQuantity(form.getQuantity());

    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v4/items/{itemId}";
}
```

<br><Br>

### Bean Validation - HTTP 메시지 컨버터

@Valid , @Validated 는 HttpMessageConverter (@RequestBody )에도 적용할 수 있다

<br>

> 참고
> @ModelAttribute 는 HTTP 요청 파라미터(URL 쿼리 스트링, POST Form)를 다룰 때 사용한다.
> @RequestBody 는 HTTP Body의 데이터를 객체로 변환할 때 사용한다. 주로 API JSON 요청을 다룰 때
사용한다.

```
@PostMapping("/add")
public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult){
    log.info("API 컨트롤러 호출");

    if(bindingResult.hasErrors()){
        log.info("검증 오류 발생 = {}", bindingResult);
        return bindingResult.getAllErrors();
    }

    log.info("성공 로직 실행");
    return form;
}
```

#### API의 경우 3가지 경우를 나누어 생각해야 한다.
- 성공 요청: 성공
- 실패 요청: JSON을 객체로 생성하는 것 자체가 실패함 - String 필드에 int가 들어가는 등
- 검증 오류 요청: JSON을 객체로 생성하는 것은 성공했고, 검증에서 실패함

<br>


#### 검증 오류 결과
```
[
    {
        "codes": [
            "Max.itemSaveForm.quantity",
            "Max.quantity",
            "Max.java.lang.Integer",
            "Max"
        ],
        "arguments": [
            {
                "codes": [
                    "itemSaveForm.quantity",
                    "quantity"
                ],
                "arguments": null,
                "defaultMessage": "quantity",
                "code": "quantity"
            },
            9999
        ],
        "defaultMessage": "9999 이하여야 합니다",
        "objectName": "itemSaveForm",
        "field": "quantity",
        "rejectedValue": 100000,
        "bindingFailure": false,
        "code": "Max"
    }
]
```

return bindingResult.getAllErrors(); 는 ObjectError 와 FieldError 를 반환한다. 스프링이 이
객체를 JSON으로 변환해서 클라이언트에 전달했다. 

여기서는 예시로 보여주기 위해서 검증 오류 객체들을
그대로 반환했다. 

실제 개발할 때는 이 객체들을 그대로 사용하지 말고, 필요한 데이터만 뽑아서 별도의 API 스펙을 정의하고 그에 맞는 객체를 만들어서 반환해야 한다.



<br>

#### @ModelAttribute vs @RequestBody

HTTP 요청 파리미터를 처리하는 @ModelAttribute 는 각각의 필드 단위로 세밀하게 적용된다. 그래서 특정 필드에 타입이 맞지 않는 오류가 발생해도 나머지 필드는 정상 처리할 수 있었다.


HttpMessageConverter 는 @ModelAttribute 와 다르게 각각의 필드 단위로 적용되는 것이 아니라, 
전체 객체 단위로 적용된다.


따라서 메시지 컨버터의 작동이 성공해서 ItemSaveForm 객체를 만들어야 @Valid , @Validated 가 적용된다.

<br>

- @ModelAttribute 는 필드 단위로 정교하게 바인딩이 적용된다. 특정 필드가 바인딩 되지 않아도 나머지
필드는 정상 바인딩 되고, Validator를 사용한 검증도 적용할 수 있다.
- @RequestBody 는 HttpMessageConverter 단계에서 JSON 데이터를 객체로 변경하지 못하면 이후
단계 자체가 진행되지 않고 예외가 발생한다. 컨트롤러도 호출되지 않고, Validator도 적용할 수 없다

<br>

> 참고
> HttpMessageConverter 단계에서 실패하면 예외가 발생한다. 예외 발생시 원하는 모양으로 예외를 처리하는 방법은 예외 처리 부분에서 다룬다.