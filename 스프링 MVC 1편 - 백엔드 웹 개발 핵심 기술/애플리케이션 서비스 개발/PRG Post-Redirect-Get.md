## PRG Post/Redirect/Get 

<img width="406" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/ad63db44-dfc4-46dd-a792-eb2242f451fa">


사실 지금까지 진행한 상품 등록 처리 컨트롤러는 심각한 문제가 있다. (addItemV1 ~ addItemV4)
- 상품 등록을 완료하고 웹 브라우저의 새로고침 버튼을 클릭해보자.
- 상품이 계속해서 중복 등록되는 것을 확인할 수 있다

<img width="448" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/b306c373-b866-48d8-8fe5-a681425a6874">

<br>

<img width="405" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/04d6c4ba-092a-4978-b9c7-04f40dd07ba3">

- 웹 브라우저의 새로 고침은 마지막에 서버에 전송한 데이터를 다시 전송한다.

- 상품 등록 폼에서 데이터를 입력하고 저장을 선택하면 POST /add + 상품 데이터를 서버로 전송한다.

- 이 상태에서 새로 고침을 또 선택하면 마지막에 전송한 POST /add + 상품 데이터를 서버로 다시 전송하게
된다.

- 그래서 내용은 같고, ID만 다른 상품 데이터가 계속 쌓이게 된다.

<br>

### POST, Redirect GET

<img width="302" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/a3a989f9-5b2c-484f-98c1-75cbe30f30b4">

- 웹 브라우저의 새로 고침은 마지막에 서버에 전송한 데이터를 다시 전송한다.
- 새로 고침 문제를 해결하려면 상품 저장 후에 뷰 템플릿으로 이동하는 것이 아니라, 상품 상세 화면으로 리다이렉트를 호출해주면 된다.
- 웹 브라우저는 리다이렉트의 영향으로 상품 저장 후에 실제 상품 상세 화면으로 다시 이동한다. = 상품 상세화면 새로 요청하게 함

- 따라서 마지막에 호출한 내용이 상품 상세 화면인 GET /items/{id} 가 되는 것이다.
    - 이후 새로고침을 해도 상품 상세 화면으로 이동하게 되므로 새로 고침 문제를 해결할 수 있다

<br>

> 주의
> "redirect:/basic/items/" + item.getId() redirect에서 +item.getId() 처럼 URL에 변수를
더해서 사용하는 것은 URL 인코딩이 안되기 때문에 위험하다. 다음에 설명하는 RedirectAttributes 를 사용하자.

<br>

### RedirectAttributes

저장이 잘 되었으면 상품 상세 화면에 "저장되었습니다"라는 메시지를 보여달라는 요구사항이 왔다

```
    @PostMapping("/add")
    public String addItemV5(Item item, RedirectAttributes redirectAttributes){
        // Item -> item이 modelAttribute에 담기게 됨
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId",savedItem.getId());
        redirectAttributes.addAttribute("status",true); // 쿼리 파라미터로 넘어가게 됨

        return "redirect:/basic/items/{itemId}";
    }
```

### RedirectAttributes
RedirectAttributes 를 사용하면 URL 인코딩도 해주고, pathVarible , 쿼리 파라미터까지 처리해준다.
- redirect:/basic/items/{itemId}
    - pathVariable 바인딩: {itemId}
    - 나머지는 쿼리 파라미터로 처리: `?status=true`

➕ 뷰 템플릿에 status가 true일 경우 저장완료 뜨게 th:if 추가

<img width="636" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/dddd4743-1cdf-473b-b4ae-707204b3ab98">