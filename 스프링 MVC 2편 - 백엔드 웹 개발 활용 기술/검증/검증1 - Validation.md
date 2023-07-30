
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

```
✅ controller

@PostMapping("/add")
public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

    // 검증 오류 결과 보관
    Map<String, String> errors = new HashMap<>();

    // 검증 로직
    if (!StringUtils.hasText(item.getItemName())){
        errors.put("itemName","상품 이름은 필수입니다.");
    }
    if(item.getPrice()==null || item.getPrice()<1000 || item.getPrice() > 1000000){
        errors.put("price","가격은 1,000 ~ 1,000,000 까지 허용합니다.");
    }
    if(item.getQuantity()==null || item.getQuantity() >= 9999){
        errors.put("quantity","수량은 최대 9,999까지 허용합니다.");
    }

    // 특정 필드가 아닌 복합 룰 검증
    if(item.getPrice()!=null && item.getQuantity()!=null){
        int resultPrice = item.getPrice() * item.getQuantity();
        if(resultPrice < 10000){
            errors.put("globalError","가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = "+resultPrice);
        }
    }

    // 검증에 실패하면 다시 입력 폼으로
    if(!errors.isEmpty()){
        log.info("errors = {}",errors);
        model.addAttribute("errors",errors);
        return "validation/v1/addForm";
    }

    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v1/items/{itemId}";
}

✅ html
<label for="itemName" th:text="#{label.item.itemName}">상품명</label>
<input type="text" id="itemName" th:field="*{itemName}"
        th:class="${errors?.containsKey('itemName')} ? 'form-control field-error' :'form-control'"
        class="form-control" placeholder="이름을 입력하세요">
<div class="field-error" th:if="${errors.containsKey('itemName')}" th:text="${errors['itemName']}">
    상품명 오류
</div>
```

<img width="465" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/af887912-3da1-4aaa-8dce-927bf303c040">


> 참고 Safe Navigation Operator
> 만약 여기에서 errors 가 null 이라면 어떻게 될까?
> 생각해보면 등록폼에 진입한 시점에는 errors 가 없다.
> 따라서 errors.containsKey() 를 호출하는 순간 NullPointerException 이 발생한다.
>
> errors?. 은 errors 가 null 일때 NullPointerException 이 발생하는 대신, null 을 반환하는
문법이다.
> th:if 에서 null 은 실패로 처리되므로 오류 메시지가 출력되지 않는다.
> 이것은 스프링의 SpringEL이 제공하는 문법이다. 자세한 내용은 다음을 참고하자.

<br>

#### 필드 오류 처리
```
<input type="text" th:classappend="${errors?.containsKey('itemName')} ? 'field-error' : _" class="form-control">

```

classappend 를 사용해서 해당 필드에 오류가 있으면 field-error 라는 클래스 정보를 더해서 폼의
색깔을 빨간색으로 강조한다. 만약 값이 없으면 _ (No-Operation)을 사용해서 아무것도 하지 않는다.

<br><BR>

### 검증 오류 처리 - Binding Result1

```
@PostMapping("/add")
public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult,RedirectAttributes redirectAttributes, Model model) {
    
    // 검증 로직
    if (!StringUtils.hasText(item.getItemName())){
        bindingResult.addError(new FieldError("item","itemName","상품 이름은 필수입니다."));
    }
    if(item.getPrice()==null || item.getPrice()<1000 || item.getPrice() > 1000000){
        bindingResult.addError(new FieldError("item","price","가격은 1,000 ~ 1,000,000 까지 허용합니다."));
    }
    if(item.getQuantity()==null || item.getQuantity() >= 9999){
        bindingResult.addError(new FieldError("item","quantity","수량은 최대 9,999까지 가능합니다"));
    }

    // 특정 필드가 아닌 복합 룰 검증
    if(item.getPrice()!=null && item.getQuantity()!=null){
        int resultPrice = item.getPrice() * item.getQuantity();
        if(resultPrice < 10000){
            bindingResult.addError(new ObjectError("item","가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = "+resultPrice));
        }
    }

    // 검증에 실패하면 다시 입력 폼으로
    if(bindingResult.hasErrors()){
        log.info("errors = {}",bindingResult);
        return "validation/v2/addForm";
    }

    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v2/items/{itemId}";
}
```

#### 필드 오류 - FieldError
```
if (!StringUtils.hasText(item.getItemName())) {
 bindingResult.addError(new FieldError("item", "itemName", "상품 이름은
필수입니다."));
}
```
<br>

#### FieldError 생성자 요약
```
public FieldError(String objectName, String field, String defaultMessage) {}
```
필드에 오류가 있으면 FieldError 객체를 생성해서  bindingResult 에 담아두면 된다.
- objectName : @ModelAttribute 이름
- field : 오류가 발생한 필드 이름
- defaultMessage : 오류 기본 메시지

<Br>

#### 글로벌 오류 - ObjectError
```
bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야
합니다. 현재 값 = " + resultPrice));
```


#### ObjectError 생성자 요약
```public ObjectError(String objectName, String defaultMessage) {}```

특정 필드를 넘어서는 오류가 있으면 ObjectError 객체를 생성해서 bindingResult 에 담아두면 된다.
- objectName : @ModelAttribute 의 이름
- defaultMessage : 오류 기본 메시지

<br>

#### 타임리프 스프링 검증 오류 통합 기능
타임리프는 스프링의 BindingResult 를 활용해서 편리하게 검증 오류를 표현하는 기능을 제공한다.
- #fields : #fields 로 BindingResult 가 제공하는 검증 오류에 접근할 수 있다.
- th:errors : 해당 필드에 오류가 있는 경우에 태그를 출력한다. th:if 의 편의 버전이다.
- th:errorclass : th:field 에서 지정한 필드에 오류가 있으면 class 정보를 추가한다

<Br>

#### 글로벌 오류 처리
```<div th:if="${#fields.hasGlobalErrors()}">
 <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="$
{err}">전체 오류 메시지</p>
</div>
```

#### 필드 오류 처리
```
<input type="text" id="itemName" th:field="*{itemName}"
 th:errorclass="field-error" class="form-control" placeholder="이름을
입력하세요">
<div class="field-error" th:errors="*{itemName}">
 상품명 오류
</div>
```

<br><br>


### 검증 오류 처리 - Binding Result2

- 스프링이 제공하는 검증 오류를 보관하는 객체이다. 검증 오류가 발생하면 여기에 보관하면 된다.

- BindingResult 가 있으면 @ModelAttribute 에 데이터 바인딩 시 오류가 발생해도 컨트롤러가 호출된다!

<Br>

예) @ModelAttribute에 바인딩 시 타입 오류가 발생하면?
- BindingResult 가 없으면 400 오류가 발생하면서 컨트롤러가 호출되지 않고, 오류 페이지로 이동한다.
- BindingResult 가 있으면 ➡️ 오류 정보( FieldError )를 BindingResult 에 담아서 컨트롤러를
정상 호출한다

<Br>


#### BindingResult에 검증 오류를 적용하는 3가지 방법
1. @ModelAttribute 의 객체에 타입 오류 등으로 바인딩이 실패하는 경우 스프링이 FieldError 생성해서
BindingResult 에 넣어준다.

2. 개발자가 직접 넣어준다.
3. Validator 사용 이것은 뒤에서 설명

<Br>

#### 타입 오류 확인
숫자가 입력되어야 할 곳에 문자를 입력해서 타입을 다르게 해서 BindingResult 를 호출하고
bindingResult 의 값을 확인해보자.

<Br>

#### 주의
- BindingResult 는 검증할 대상 바로 다음에 와야한다. 순서가 중요하다. 예를 들어서 @ModelAttribute 
Item item , 바로 다음에 BindingResult 가 와야 한다.
- BindingResult 는 Model에 자동으로 포함된다.

<br>

#### BindingResult와 Errors
`org.springframework.validation.Errors`
`org.springframework.validation.BindingResult`

<br>
BindingResult 는 인터페이스이고, Errors 인터페이스를 상속받고 있다.

실제 넘어오는 구현체는 BeanPropertyBindingResult 라는 것인데, 둘다 구현하고 있으므로
BindingResult 대신에 Errors 를 사용해도 된다. 

Errors 인터페이스는 단순한 오류 저장과 조회
기능을 제공한다. BindingResult 는 여기에 더해서 추가적인 기능들을 제공한다. 

addError() 도 BindingResult 가 제공하므로 여기서는 BindingResult 를 사용하자. 주로 관례상 BindingResult 를
많이 사용한다

<br><br>

#### 정리
BindingResult , FieldError , ObjectError 를 사용해서 오류 메시지를 처리하는 방법을 알아보았다.

그런데 **오류가 발생하는 경우** 고객이 입력한 내용이 모두 사라진다. 이 문제를 해결해보자.

<br><Br>

### FieldError, ObjectError

#### FieldError 생성자
FieldError 는 두 가지 생성자를 제공한다.
```
public FieldError(String objectName, String field, String defaultMessage);

public FieldError(String objectName, String field, @Nullable Object 
rejectedValue, boolean bindingFailure, @Nullable String[] codes, @Nullable
Object[] arguments, @Nullable String defaultMessage)
```
- 파라미터 목록
    - objectName : 오류가 발생한 객체 이름
    - field : 오류 필드
    - rejectedValue : 사용자가 입력한 값(거절된 값)
    - bindingFailure : 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값
    - codes : 메시지 코드
    - arguments : 메시지에서 사용하는 인자
    - defaultMessage : 기본 오류 메시지

<Br>

#### 오류 발생시 사용자 입력 값 유지
`new FieldError("item", "price", item.getPrice(), false, null, null, "가격은 1,000 ~ 1,000,000 까지 허용합니다.")`

사용자의 입력 데이터가 컨트롤러의 @ModelAttribute 에 바인딩되는 시점에 오류가 발생하면 모델 객체에 사용자 입력 값을 유지하기 어렵다. 

예를 들어서 가격에 숫자가 아닌 문자가 입력된다면 가격은
Integer 타입이므로 문자를 보관할 수 있는 방법이 없다. 

그래서 오류가 발생한 경우 사용자 입력 값을 보관하는 별도의 방법이 필요하다. 그리고 이렇게 보관한 사용자 입력 값을 검증 오류 발생시 화면에 다시 출력하면 된다.


FieldError 는 **오류 발생시 사용자 입력 값을 저장하는 기능**을 제공한다

<Br>

여기서 rejectedValue 가 바로 오류 발생시 사용자 입력 값을 저장하는 필드다.

bindingFailure 는 타입 오류 같은 바인딩이 실패했는지 여부를 적어주면 된다. 여기서는 바인딩이 실패한 것은 아니기 때문에 false 를 사용한다.

<br>


#### 타임리프의 사용자 입력 값 유지
`th:field="*{price}"`

타임리프의 th:field 는 매우 똑똑하게 동작하는데, 정상 상황에는 모델 객체의 값을 사용하지만, 

오류가 발생하면 FieldError 에서 보관한 값을 사용해서 값을 출력한다.

<br>

#### 스프링의 바인딩 오류 처리
타입 오류로 바인딩에 실패하면 스프링은 FieldError 를 생성하면서 사용자가 입력한 값을 넣어둔다. 

그리고 해당 오류를 BindingResult 에 담아서 컨트롤러를 호출한다. 

따라서 타입 오류 같은 바인딩 실패시에도 사용자의 오류 메시지를 정상 출력할 수 있다.

<br><br>

### 오류 코드와 메시지 처리1

FieldError , ObjectError 의 생성자는 codes , arguments 를 제공한다. 이것은 오류 발생시 오류 코드로 메시지를 찾기 위해 사용된다.

<br>

먼저 스프링 부트가 해당 메시지 파일을 인식할 수 있게 다음 설정을 추가한다. 이렇게하면 messages.properties , errors.properties 두 파일을 모두 인식한다.

(생략하면 messages.properties 를 기본으로 인식한다.)

<br>

#### 스프링 부트 메시지 설정 추가
```
✅ application.properties

spring.messages.basename=messages,errors

```

```
✅ controller
// 검증 로직
if (!StringUtils.hasText(item.getItemName())){
    bindingResult.addError(new FieldError("item","itemName",item.getItemName(), false, new String[]{"required.item.itemName"},null,null));
}
if(item.getPrice()==null || item.getPrice()<1000 || item.getPrice() > 1000000){
    bindingResult.addError(new FieldError("item","price", item.getPrice(),false, new String[]{"range.item.price"}, new Object[]{1000,1000000}, null));
}
if(item.getQuantity()==null || item.getQuantity() >= 9999){
    bindingResult.addError(new FieldError("item","quantity", item.getQuantity(),false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
}

// 특정 필드가 아닌 복합 룰 검증
if(item.getPrice()!=null && item.getQuantity()!=null){
    int resultPrice = item.getPrice() * item.getQuantity();
    if(resultPrice < 10000){
        bindingResult.addError(new ObjectError("item",new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
    }
}

// 검증에 실패하면 다시 입력 폼으로
if(bindingResult.hasErrors()){
    log.info("errors = {}",bindingResult);
    return "validation/v2/addForm";
}
```

<br>

- codes : required.item.itemName 를 사용해서 메시지 코드를 지정한다. 메시지 코드는 하나가 아니라 배열로 여러 값을 전달할 수 있는데, 순서대로 매칭해서 처음 매칭되는 메시지가 사용된다.
- arguments : Object[]{1000, 1000000} 를 사용해서 코드의 {0} , {1} 로 치환할 값을 전달한다

<br><Br>

### 오류 코드와 메시지 처리2

컨트롤러에서 BindingResult 는 검증해야 할 객체인 target 바로 다음에 온다. 따라서
BindingResult 는 이미 본인이 검증해야 할 객체인 target 을 알고 있다

<br>

#### rejectValue() , reject()
BindingResult 가 제공하는 rejectValue() , reject() 를 사용하면 FieldError , ObjectError 를
직접 생성하지 않고, 깔끔하게 검증 오류를 다룰 수 있다.

rejectValue() , reject() 를 사용해서 기존 코드를 단순화해보자.

```
// 검증 로직
if (!StringUtils.hasText(item.getItemName())){
    bindingResult.rejectValue("itemName","required");
}
if(item.getPrice()==null || item.getPrice()<1000 || item.getPrice() > 1000000){
    bindingResult.rejectValue("price","range",new Object[]{1000,1000000},null);
}
if(item.getQuantity()==null || item.getQuantity() >= 9999){
    bindingResult.rejectValue("quantity","max",new Object[]{9999},null);
}

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
    return "validation/v2/addForm";
}
```

<br>

#### rejectValue()

```
void rejectValue(@Nullable String field, String errorCode,
@Nullable Object[] errorArgs, @Nullable String defaultMessage);
```
- field : 오류 필드명
- errorCode : 오류 코드(이 오류 코드는 메시지에 등록된 코드가 아니다. 뒤에서 설명할 messageResolver를 위한 오류 코드이다.)
- errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값
- defaultMessage : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지

<br>

`bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null)`


앞에서 BindingResult 는 어떤 객체를 대상으로 검증하는지 target을 이미 알고 있다고 했다. 따라서
target(item)에 대한 정보는 없어도 된다. 오류 필드명은 동일하게 price 를 사용했다.
<Br>

#### 축약된 오류 코드
FieldError() 를 직접 다룰 때는 오류 코드를 range.item.price 와 같이 모두 입력했다. 

그런데 rejectValue() 를 사용하고 부터는 오류 코드를 range 로 간단하게 입력했다. 그래도 오류 메시지를 잘
찾아서 출력한다. 

무언가 규칙이 있는 것 처럼 보인다. 이 부분을 이해하려면 MessageCodesResolver 를 이해해야 한다. 

<br>

```
✅ errors.properties
range.item.price=가격은 {0} ~ {1} 까지 허용합니다.
```

#### reject()
```
void reject(String errorCode, @Nullable Object[] errorArgs, @Nullable String 
defaultMessage);
```

<br><br>

### 오류 코드와 메시지 처리3

오류 코드를 만들 때 다음과 같이 자세히 만들 수도 있고,
- required.item.itemName : 상품 이름은 필수 입니다.
- range.item.price : 상품의 가격 범위 오류 입니다.

<br>

또는 다음과 같이 단순하게 만들 수도 있다.
- required : 필수 값 입니다.
- range : 범위 오류 입니다

<Br>

단순하게 만들면 범용성이 좋아서 여러곳에서 사용할 수 있지만, 메시지를 세밀하게 작성하기 어렵다. 

반대로 너무 자세하게 만들면 범용성이 떨어진다. 

가장 좋은 방법은 범용성으로 사용하다가, 세밀하게
작성해야 하는 경우에는 세밀한 내용이 적용되도록 메시지에 단계를 두는 방법이다

<br>

오류 메시지에 required.item.itemName 와 같이 객체명과 필드명을 조합한 세밀한 메시지
코드가 있으면 이 메시지를 높은 우선순위로 사용하는 것이다.

```
#Level1
required.item.itemName: 상품 이름은 필수 입니다.

#Level2
required: 필수 값 입니다
```

물론 이렇게 객체명과 필드명을 조합한 메시지가 있는지 우선 확인하고, 없으면 좀 더 범용적인 메시지를
선택하도록 추가 개발을 해야겠지만, 범용성 있게 잘 개발해두면, 메시지의 추가 만으로 매우 편리하게 오류
메시지를 관리할 수 있을 것이다.


스프링은 MessageCodesResolver 라는 것으로 이러한 기능을 지원한다.