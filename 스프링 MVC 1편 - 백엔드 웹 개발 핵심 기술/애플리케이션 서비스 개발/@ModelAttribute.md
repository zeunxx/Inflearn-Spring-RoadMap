# @ModelAttribute

```
    @PostMapping("/add")
    public String addItemV1(@RequestParam String itemName,
                       @RequestParam int price,
                       @RequestParam Integer quantity,
                       Model model){
        Item item = new Item();
        item.setItemName(itemName);
        item.setPrice(price);
        item.setQuantity(quantity);

        itemRepository.save(item);
        model.addAttribute("item",item);

        return "basic/item";
    }

    @PostMapping("/add")
    public String addItemV2(@ModelAttribute("item") Item item, Model model){
        itemRepository.save(item);
//        model.addAttribute("item",item); // 자동 추가, 생략 가능
        return "basic/item";
    }

```
- 위 두 로직 = 동일 기능 수행

### 요청 파라미터 처리
Item 객체를 생성하고, 요청 파라미터의 값을 프로퍼티 접근법으로 (setXXX)으로 입력해준다.  

<BR>

### @ModelAttribute-Model 추가
Model에 @ModelAttribute로 지정한 객체를 자동으로 넣어줌!
`//        model.addAttribute("item",item); // 자동 추가, 생략 가능` 를 통해 확인 가능

<br>

모델에 데이터를 담을때, 이름이 필요한데 이름은 은 @ModelAttribute 에 지정한 name(value) 속성을
사용

- 만약 @ModelAttribute의 이름을 다르게 지정하면 다른 이름이 모델에 포함됨 
    - `@ModelAttribute("hello") Item item` 이름을 hello 로 지정
    - `model.addAttribute("hello", item);` 모델에 hello 이름으로 저장

<br>

### @ModelAttribute 의 이름을 생략할 수 있다

```
    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, Model model){
        // Item -> item이 modelAttribute에 담기게 됨
        itemRepository.save(item);
        
        return "basic/item";
    }
```

**주의**
@ModelAttribute 의 이름을 생략하면 모델에 저장될 때 클래스명을 사용한다. 이때 클래스의 첫글자만
소문자로 변경해서 등록한다.
- 예) @ModelAttribute 클래스명 모델에 자동 추가되는 이름
    - Item -> item
    - HelloWorld -> helloWorld

<br>

### ModelAttribute 전체 생략
```
    @PostMapping("/add")
    public String addItemV4(Item item, Model model){
        // Item -> item이 modelAttribute에 담기게 됨
        itemRepository.save(item);

        return "basic/item";
    }
```
- model에 담기는 것은 V3와 동일

