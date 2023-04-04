## MVC 패턴 적용

서블릿을 컨트롤러로 사용하고, JSP를 뷰로 사용해서 MVC 패턴을 적용해보자.
Model은 HttpServletRequest 객체를 사용한다. request는 내부에 데이터 저장소를 가지고 있는데,
request.setAttribute() , request.getAttribute() 를 사용하면 데이터를 보관하고, 조회할 수 있다. 

```
@WebServlet(name="mvcMemberFormServlet", urlPatterns = "/servlet-mvc/members/new-form")
public class MvcMemberFormServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String viewPath = "/WEB-INF/views/new-form.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request,response);
    }
}
```

- dispatcher.forward() : 다른 서블릿이나 JSP로 이동할 수 있는 기능이다. 서버 내부에서 다시 호출이
발생한다.

<br>

> `/WEB-INF`
> 이 경로안에 JSP가 있으면 외부에서 직접 JSP를 호출할 수 없다. 우리가 기대하는 것은 항상 컨트롤러를
통해서 JSP를 호출하는 것이다.

> redirect vs forward
> 리다이렉트는 실제 클라이언트(웹 브라우저)에 응답이 나갔다가, 클라이언트가 redirect 경로로 다시
요청한다. 따라서 클라이언트가 인지할 수 있고, URL 경로도 실제로 변경된다. 반면에 포워드는 서버
내부에서 일어나는 호출이기 때문에 클라이언트가 전혀 인지하지 못한다

<br><br>

## MVC 패턴 - 한계

MVC 패턴을 적용한 덕분에 컨트롤러의 역할과 뷰를 렌더링 하는 역할을 명확하게 구분할 수 있다.
특히 뷰는 화면을 그리는 역할에 충실한 덕분에, 코드가 깔끔하고 직관적이다. 단순하게 모델에서 필요한
데이터를 꺼내고, 화면을 만들면 된다.


그런데 컨트롤러는 딱 봐도 중복이 많고, 필요하지 않는 코드들도 많이 보인다.

<BR><Br>

### 🔎 MVC 컨트롤러의 단점

view로 이동하는 코드가 항상 중복 호출됨. 물론 이 부분을 메서드로 공통화해도 되지만, 해당 메서드도 항상 직접 호출해야 함!

1. 포워드 중복

    ```
    String viewPath = "/WEB-INF/views/save-result.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request,response);

    ```

2. viewPath에 중복

    `String viewPath = "/WEB-INF/views/new-form.jsp";`

    - prefix: /WEB-INF/views/
    - suffix: .jsp
    그리고 만약 jsp가 아닌 thymeleaf 같은 다른 뷰로 변경한다면 전체 코드를 다 변경해야 한다.


3. 사용하지 않는 코드

    다음 코드를 사용할 때도 있고, 사용하지 않을 때도 있음. 특히 response는 현재 코드에서 사용되지 않음

    `HttpServletRequest request, HttpServletResponse response`

    추가로, 그리고 이런 HttpServletRequest , HttpServletResponse 를 사용하는 코드는 테스트 케이스를
    작성하기도 어렵다.

4. 공통 처리가 어려움

    기능이 복잡해질 수 록 컨트롤러에서 공통으로 처리해야 하는 부분이 점점 더 많이 증가할 것이다. 단순히
    공통 기능을 메서드로 뽑으면 될 것 같지만, 결과적으로 해당 메서드를 항상 호출해야 하고, 실수로 호출하지
    않으면 문제가 될 것이다. 그리고 호출하는 것 자체도 중복이다.


<br>

### ✅ 정리하자면 공통 처리가 어렵단 문제 발생!


이 문제를 해결하려면 컨트롤러 호출 전에 먼저 공통 기능을 처리해야 한다. 소위 **수문장 역할**을 하는 기능이
필요하다.


**프론트 컨트롤러(Front Controller) 패턴**을 도입하면 이런 문제를 깔끔하게 해결할 수 있다.
(입구를 하나로!)


스프링 MVC의 핵심도 바로 이 프론트 컨트롤러에 있다.