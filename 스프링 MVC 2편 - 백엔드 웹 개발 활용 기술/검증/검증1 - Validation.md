
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

### 검증 오류 처리 - Binding Result

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