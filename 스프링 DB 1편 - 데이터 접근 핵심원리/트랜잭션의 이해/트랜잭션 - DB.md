# 트랜잭션 - DB 


## 트랜잭션 - DB 예제 1 - 개념 이해

**트랜잭션 사용법**

- 데이터 변경 쿼리를 실행하고 데이터베이스에 그 결과를 반영하려면 커밋 명령어인 **commit** 을 호출하고, 결과를 반영하고 싶지 않으면 롤백 명령어인 **rollback** 을 호출하면 된다.
- **커밋을 호출하기 전까지는 임시로 데이터를 저장**하는 것이다. 따라서 해당 트랜잭션을 시작한 세션(사용자)에게만 변경 데이터가 보이고 다른 세션(사용자)에게는 변경 데이터가 보이지 않는다.
- 등록, 수정, 삭제 모두 같은 원리로 동작한다. 앞으로는 등록, 수정, 삭제를 간단히 변경이라는 단어로 표현하겠다.


<BR>

<img width="435" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/d5c9b6d0-ad5a-47b0-8345-4ff6af3a58be">

- 세션1, 세션2 둘다 가운데 있는 기본 테이블을 조회하면 해당 데이터가 그대로 조회된다.



<img width="434" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/f40b04e6-7380-4bd6-8665-9764e5d7d963">

- 세션1은 트랜잭션을 시작하고 신규 회원1, 신규 회원2를 DB에 추가했다. 아직 커밋은 하지 않은 상태이다.
- 새로운 데이터는 임시 상태로 저장된다.
- 세션1은 select 쿼리를 실행해서 본인이 입력한 신규 회원1, 신규 회원2를 조회할 수 있다.
- 세션2는 select 쿼리를 실행해도 신규 회원들을 조회할 수 없다. 왜냐하면 **세션1이 아직 커밋을 하지 않았기** 때문이다. 


<BR>

**커밋하지 않은 데이터를 다른 곳에서 조회할 수 있으면 어떤 문제가 발생할까?**

- 예를 들어서 커밋하지 않는 데이터가 보인다면, 세션2는 데이터를 조회했을 때 신규 회원1, 2가 보일 것이다. 따라서 신규 회원1, 신규 회원2가 있다고 가정하고 어떤 로직을 수행할 수 있다. 그런데 세션1이 롤백을 수행하면 신규 회원1, 신규 회원2의 데이터가 사라지게 된다. 따라서 데이터 정합성에 큰 문제가 발생한다.
- 세션2에서 세션1이 아직 커밋하지 않은 변경 데이터가 보이다면, 세션1이 롤백 했을 때 심각한 문제가 발생할 수 있다. 따라서 커밋 전의 데이터는 다른 세션에서 보이지 않는다.


<bR>

<img width="414" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/a715066a-d292-4db6-960c-1f6677dbb9e2">

- 세션1이 신규 데이터를 추가한 후에 commit 을 호출했다.
- commit 으로 새로운 데이터가 실제 데이터베이스에 반영된다. 데이터의 상태도 임시 완료로 변경되었다.
- 이제 다른 세션에서도 회원 테이블을 조회하면 신규 회원들을 확인할 수 있다.


<BR>

세션1 신규 데이터 추가 후 rollback
<img width="410" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/1f5c180b-3b34-435b-b15d-83b1a522190b">

- 세션1이 신규 데이터를 추가한 후에 commit 대신에 rollback 을 호출했다.
- 세션1이 데이터베이스에 반영한 모든 데이터가 처음 상태로 복구된다.
- 수정하거나 삭제한 데이터도 rollback 을 호출하면 모두 트랜잭션을 시작하기 직전의 상태로 복구된다.



## 트랜잭션 - DB 예제 2 - 자동 커밋, 수동 커밋

### 자동 커밋

자동 커밋으로 설정하면 각각의 쿼리 실행 직후에 자동으로 커밋을 호출한다. 

따라서 커밋이나 롤백을 직접 호출하지 않아도 되는 편리함이 있다. 하지만 쿼리를 하나하나 실행할 때 마다 자동으로 커밋이 되어버리기 때문에 우리가 원하는 트랜잭션 기능을 제대로 사용할 수 없다.


**자동 커밋 설정**
```
set autocommit true; //자동 커밋 모드 설정
insert into member(member_id, money) values ('data1',10000); //자동 커밋
insert into member(member_id, money) values ('data2',10000); //자동 커밋
```

따라서 commit , rollback 을 직접 호출하면서 트랜잭션 기능을 제대로 수행하려면 자동 커밋을 끄고 수동 커밋을 사용해야 한다.


<br>

### 수동 커밋 설정
```
set autocommit false; //수동 커밋 모드 설정
insert into member(member_id, money) values ('data3',10000);
insert into member(member_id, money) values ('data4',10000);
commit; //수동 커밋
```

보통 자동 커밋 모드가 기본으로 설정된 경우가 많기 때문에, **수동 커밋 모드로 설정하는 것을 트랜잭션을 시작**한다고 표현할 수 있다.

수동 커밋 설정을 하면 이후에 꼭 commit , rollback 을 호출해야 한다.


> 참고로 수동 커밋 모드나 자동 커밋 모드는 한번 설정하면 해당 세션에서는 계속 유지된다. 중간에 변경하는 것은 가능하다.



## 트랜잭션 - DB 예제3 - 트랜잭션 실습

1️⃣ 기본 데이터 입력


<img width="422" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/1c316013-b0a3-4d4b-8d44-fa374c91bbca">


데이터 초기화 SQL
```
//데이터 초기화
set autocommit true;
delete from member;
insert into member(member_id, money) values ('oldId',10000);
```

- 자동 커밋 모드를 사용했기 때문에 별도로 커밋을 호출하지 않아도 된다


<br>

2️⃣ 신규데이터 추가 - 커밋 전

<img width="421" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/509c6717-9990-4a8f-be69-0d9dfed19549">


세션 1 신규 데이터 추가 SQL
```
//트랜잭션 시작
set autocommit false; //수동 커밋 모드
insert into member(member_id, money) values ('newId1',10000);
insert into member(member_id, money) values ('newId2',10000);

```


세션1, 세션2에서 다음 쿼리를 실행해서 결과를 확인하자.

`select * from member;`


결과를 이미지와 비교해보자. 아직 세션1이 커밋을 하지 않은 상태이기 때문에 세션1에서는 입력한 데이터
가 보이지만, 세션2에서는 입력한 데이터가 보이지 않는 것을 확인할 수 있다.


<img width="389" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/6e5391ab-7b45-4bbf-9f3c-4d79ae0a115d">


<bR>

3️⃣ 커밋 - commit 

<img width="420" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/9a301893-3b89-4742-9568-ed9fa5cfd4ee">



세션1에서 커밋을 호출해보자.
`commit; //데이터베이스에 반영`


세션1, 세션2에서 다음 쿼리를 실행해서 결과를 확인하자.
`select * from member;`


결과를 이미지와 비교해보자. 세션1이 트랜잭션을 커밋했기 때문에 데이터베이스에 실제 데이터가 반영된
다. 커밋 이후에는 모든 세션에서 데이터를 조회할 수 있다.


<img width="353" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/948df2c5-abb6-4113-a9a7-473cd4b25751">


<br>

4️⃣ 롤백 - rollback

- 우선 데이터를 초기화 후 신규 데이터 추가 
```
//데이터 초기화
set autocommit true;
delete from member;
insert into member(member_id, money) values ('oldId',10000);


//트랜잭션 시작
set autocommit false; //수동 커밋 모드
insert into member(member_id, money) values ('newId1',10000);
insert into member(member_id, money) values ('newId2',10000);

// 결과 확인
select * from member;
```

<img width="410" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/5180a9ad-5712-4b97-8c4d-63edfe1487bf">



**세션 1 신규 데이터 추가 후 rollback**

<img width="421" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/6295bd78-abae-424d-907c-42154b754689">

세션1에서 롤백을 호출해보자. 

```
rollback; //롤백으로 데이터베이스에 변경 사항을 반영하지 않는다.
```


<img width="355" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/80201a1e-3f5a-4674-9cc8-9ab704c6acb0">


<Br>


## 트랜잭션 - DB 예제4 - 계좌이체

- 계좌이체 정상
- 계좌이체 문제 상황 - 커밋
- 계좌이체 문제 상황 - 롤백

### 계좌이체 정상

계좌이체가 발생하는 정상 흐름을 알아보자.


<BR>

1️⃣ 기본데이터 입력

<img width="422" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/4f170453-a042-4950-aef2-7e92b5c2ee32">


- memberA 10000원
- memberB 10000원


<BR>

2️⃣ 계좌이체 실행


<img width="415" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/efb2733b-2b31-4f39-9af5-81e3775237d4">

- memberA 의 돈을 memberB 에게 2000원 계좌이체하는 트랜잭션을 실행해보자. 다음과 같은 2번의 update 쿼리가 수행되어야 한다.
- set autocommit false 로 설정한다.
- 아직 커밋하지 않았으므로 다른 세션에는 기존 데이터가 조회된다.

<BR>

계좌이체 실행 SQL - 성공
```
set autocommit false;
update member set money=10000 - 2000 where member_id = 'memberA';
update member set money=10000 + 2000 where member_id = 'memberB';
```

<img width="380" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/48dee5ec-78c6-4da2-9011-9a95e89b21f9">

<BR>

3️⃣ 커밋

<img width="418" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/39665079-1efe-4711-b6aa-575e59658c0f">


- commit 명령어를 실행하면 데이터베이스에 결과가 반영된다.
- 다른 세션에서도 memberA 의 금액이 8000원으로 줄어들고, memberB 의 금액이 12000원으로 증가한 것을 확인할 수 있다.


<img width="385" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/a0074727-4865-4e38-ab7d-668268230b42">



### 계좌이체 문제 상황 - 커밋


1️⃣ 기본 데이터 입력

- 기존과 동일


<br>

2️⃣ 계좌이체 실행

<img width="420" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/83748b5e-1c6c-42ca-bac0-67df91589f08">


- 계좌이체를 실행하는 도중에 SQL에 문제가 발생한다. 그래서 memberA 의 돈을 2000원 줄이는 것에는 성공했지만, memberB 의 돈을 2000원 증가시키는 것에 실패한다.
- 두 번째 SQL은 member_iddd 라는 필드에 오타가 있다. 두 번째 update 쿼리를 실행하면 SQL 오류가 발생하는 것을 확인할 수 있다.



계좌이체 실행 SQL - 오류
```
set autocommit false;
update member set money=10000 - 2000 where member_id = 'memberA'; //성공
update member set money=10000 + 2000 where member_iddd = 'memberB'; //쿼리 예외 발생
```

<br>

두 번째 SQL 실행시 발생하는 오류 메시지
```
Column "MEMBER_IDDD" not found; SQL statement:
update member set money=10000 + 2000 where member_iddd = 'memberB' [42122-200] 
42S22/42122
```

여기서 문제는 memberA 의 돈은 2000원 줄어들었지만, memberB 의 돈은 2000원 증가하지 않았다는 점이다. 결과적으로 계좌이체는 실패하고 memberA 의 돈만 2000원 줄어든 상황이다.


<img width="619" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/ab5910c6-4be1-489a-bc11-92b89f7cdc47">



<Br>

3️⃣ 강제 커밋

<img width="419" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/8ed9c485-f9dc-4167-81dd-b0aebdd0e1c1">


만약 이 상황에서 강제로 commit 을 호출하면 어떻게 될까?
계좌이체는 실패하고 memberA 의 돈만 2000원 줄어드는 아주 심각한 문제가 발생한다.


<img width="630" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/dcb928c9-886b-43f9-8791-8bda44855a7b">


이렇게 중간에 문제가 발생했을 때는 커밋을 호출하면 안된다. 롤백을 호출해서 데이터를 트랜잭션 시작 시점으로 원복해야 한다.


<br>

### 계좌이체 문제 상황 - 롤백

중간에 문제가 발생했을 때 롤백을 호출해서 트랜잭션 시작 시점으로 데이터를 원복해보자.


1️⃣ 기본 데이터 입력

- 기존과 동일


<br>

2️⃣ 계좌이체 실행

<img width="420" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/83748b5e-1c6c-42ca-bac0-67df91589f08">


이럴 때는 롤백을 호출해서 트랜잭션을 시작하기 전 단계로 데이터를 복구해야 한다.
롤백을 사용한 덕분에 계좌이체를 실행하기 전 상태로 돌아왔다. memberA 의 돈도 이전 상태인 10000원으로 돌아오고, memberB 의 돈도 10000 원으로 유지되는 것을 확인할 수 있다.


<img width="635" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/43e54a5f-9e3b-48c0-bd91-613692944b9e">



3️⃣ 롤백


<img width="421" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/78ef7918-2aa0-4b56-a972-237eeeaed062">


<br><Br>


## 정리 

- 원자성: 트랜잭션 내에서 실행한 작업들은 마치 하나의 작업인 것처럼 모두 성공 하거나 모두 실패해야 한
다. 
    - 트랜잭션의 원자성 덕분에 여러 SQL 명령어를 마치 하나의 작업인 것 처럼 처리할 수 있었다. 성공하면 한번에 반영하고, 중간에 실패해도 마치 하나의 작업을 되돌리는 것 처럼 간단히 되돌릴 수 있다.
    
- 오토 커밋 : 만약 오토 커밋 모드로 동작하는데, 계좌이체 중간에 실패하면 어떻게 될까? 쿼리를 하나 실행할 때 마다 바
로바로 커밋이 되어버리기 때문에 memberA 의 돈만 2000원 줄어드는 심각한 문제가 발생한다.

- 트랜잭션 시작 : 따라서 이런 종류의 작업은 꼭 수동 커밋 모드를 사용해서 수동으로 커밋, 롤백 할 수 있도록 해야 한다. 보통이렇게 자동 커밋 모드에서 수동 커밋 모드로 전환 하는 것을 트랜잭션을 시작한다고 표현한다.