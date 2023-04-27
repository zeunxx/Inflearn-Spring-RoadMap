## 스프링 mvc 시작하기

### @RequestMapping
- RequestMappingHandlerMapping 
- RequestMappingHandlerAdapter
    : 가장 우선순위가 높은 핸들러 매핑과 핸들러 어댑터 

<br>

```
@Controller
public class SpringMemberFormControllerV1 {
 @RequestMapping("/springmvc/v1/members/new-form")
 public ModelAndView process() {
 return new ModelAndView("new-form");
 }
}

```
- `@Controller` : 
    - 스프링이 자동으로 스프링 빈으로 등록한다. (내부에 @Component 애노테이션이 있어서 **컴포넌트 스캔의 대상**이 됨)
    - 스프링 MVC에서 **애노테이션 기반 컨트롤러**로 인식한다.
- `@RequestMapping` : 요청 정보를 매핑한다. 해당 URL이 호출되면 이 메서드가 호출된다. 애노테이션을 기반으로 동작하기 때문에, 메서드의 이름은 임의로 지으면 된다.
- `ModelAndView` : 모델과 뷰 정보를 담아서 반환하면 된다.

<br>

> RequestMappingHandlerMapping 은 스프링 빈 중에서 @RequestMapping 또는 @Controller 가 클래스 레벨에 붙어 있는 경우에 매핑 정보로 인식한다

> @Controller 대신 @Component @RequestMapping을 입력해도 동일하게 동작함

<br>

#### 📌 주의! - 스프링 3.0 이상

스프링 부트 3.0(스프링 프레임워크 6.0)부터는 클래스 레벨에 @RequestMapping 이 있어도 스프링 컨트롤러로 인식하지 않는다. 

오직 @Controller 가 있어야 스프링 컨트롤러로 인식한다. 

<br><br>

### 컨트롤러 통합

```
@Controller
@RequestMapping("/spring/mvc/v2/members")
public class SpringMemberControllerV2 {

    MemberRepository memberRepository = MemberRepository.getInstance();

    @RequestMapping("/new-form")
    public ModelAndView newForm(){
        return new ModelAndView("new-form");
    }

    @RequestMapping
    public ModelAndView save() {
        List<Member> members = memberRepository.findAll();

        ModelAndView mv = new ModelAndView("members");
        mv.addObject("members", members);
        return mv;
    }

    @RequestMapping("/save")
    public ModelAndView members(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        ModelAndView mv = new ModelAndView("save-result");
        mv.addObject("member",member);
        return mv;
    }
}

```

- @RequestMapping 을 잘 보면 클래스 단위가 아니라 메서드 단위에 적용된 것을 확인할 수 있다.
- 따라서 컨트롤러 클래스를 유연하게 하나로 통합할 수 있다.

<br><Br>

### 스프링 mvc - 실용적인 방식

윗 버전은 ModelView를 개발자가 직접 생성해서 반환하므로 불편함!

- 현재 버전이 실무에서 주로 사용하는 방식!

```
@Controller
@RequestMapping("/springmvc/v3/members")
public class SpringMemberControllerV3 {
    MemberRepository memberRepository = MemberRepository.getInstance();

    @RequestMapping("/new-form")
    public String newForm(){
        return "new-form";
    }

    @GetMapping
    public String save(Model model) {
        List<Member> members = memberRepository.findAll();

        model.addAttribute("members", members);
        return "members";
    }

    @RequestMapping("/save")
    public String members(@RequestParam("username") String username,
                                @RequestParam("age") int age,
                                Model model) {
        Member member = new Member(username, age);
        memberRepository.save(member);

        model.addAttribute("member",member);
        return "save-result";
    }
}
```

- Model 파라미터
    - save() , members() 를 보면 Model을 파라미터로 받는 것을 확인할 수 있다. 
    - 스프링 MVC도 이런 편의 기능을 제공한다.
- ViewName 직접 반환
    - 뷰의 논리 이름을 반환할 수 있다.
- @RequestParam 사용
    - 스프링은 HTTP 요청 파라미터를 @RequestParam 으로 받을 수 있다.
    - @RequestParam("username") 은 request.getParameter("username") 와 거의 같은 코드라
    생각하면 된다.
    - 물론 GET 쿼리 파라미터, POST Form 방식을 모두 지원한다.
- @RequestMapping ➡️ @GetMapping, @PostMapping
    - @RequestMapping 은 URL만 매칭하는 것이 아니라, HTTP Method도 함께 구분할 수 있다.
    - 예를 들어서 URL이 /new-form 이고, HTTP Method가 GET인 경우를 모두 만족하는 매핑을 하려면 다음과 같이 처리하면 된다.
    `@RequestMapping(value = "/new-form", method = RequestMethod.GET)`

