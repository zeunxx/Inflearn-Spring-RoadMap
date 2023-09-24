## DB 락 개념 이해

세션1이 트랜잭션을 시작하고 데이터를 수정하는 동안 아직 커밋을 수행하지 않았는데, 세션2에서 동시에
같은 데이터를 수정하게 되면 여러가지 문제가 발생한다. 바로 트랜잭션의 원자성이 깨지는 것이다. 여기에
더해서 세션1이 중간에 롤백을 하게 되면 세션2는 잘못된 데이터를 수정하는 문제가 발생한다.


<BR>
이런 문제를 방지하려면, 세션이 트랜잭션을 시작하고 데이터를 수정하는 동안에는 커밋이나 롤백 전까지
다른 세션에서 해당 데이터를 수정할 수 없게 막아야 한다.


<BR>

<img width="419" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/207f4dfc-f673-430f-a5f5-b4ba611e8674">

- 세션1은 memberA 의 금액을 500원으로 변경하고 싶고, 세션2는 같은 memberA 의 금액을 1000원으로 변경하고 싶다.
- 데이터베이스는 이런 문제를 해결하기 위해 락(Lock)이라는 개념을 제공한다.
- 다음 예시를 통해 동시에 데이터를 수정하는 문제를 락으로 어떻게 해결하는지 자세히 알아보자.


<BR>

<img width="421" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/a884ea3a-49fc-4356-bb0b-09a41a6ce828">

1. 세션1은 트랜잭션을 시작한다.
2. 세션1은 memberA 의 money 를 500으로 변경을 시도한다. 이때 해당 로우의 락을 먼저 획득해야 한다. 락이 남아 있으므로 세션1은 락을 획득한다. (세션1이 세션2보다 조금 더 빨리 요청했다.)
3. 세션1은 락을 획득했으므로 해당 로우에 update sql을 수행한다.


<BR>

락2
<img width="419" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/0e736575-d585-4281-b8cf-427dbcd34706">

4. 세션2는 트랜잭션을 시작한다.
5. 세션2도 memberA 의 money 데이터를 변경하려고 시도한다. 이때 해당 로우의 락을 먼저 획득해야 한다. 락이 없으므로 락이 돌아올 때 까지 대기한다. 
    - 참고로 세션2가 락을 무한정 대기하는 것은 아니다. 락 대기 시간을 넘어가면 락 타임아웃 오류가 발생한다. 락 대기 시간은 설정할 수 있다.


<BR>

<img width="419" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/b1c1cf0f-edb4-4d10-a80b-583504715521">

6. 세션1은 커밋을 수행한다. 커밋으로 트랜잭션이 종료되었으므로 락도 반납한다.


<BR>

<img width="422" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/13449ec0-0ba6-44b1-99f3-13c03e9ed6e5">

- 락을 획득하기 위해 대기하던 세션2가 락을 획득한다


<BR>

<img width="422" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/c41ca4b0-0f28-4960-883e-4b12d9a9dd1a">

7. 세션2는 update sql을 수행한다.


<BR>

락6
<img width="418" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/d6a568cb-cb7e-4b3b-82b0-f6f428d49432">

8. 세션2는 커밋을 수행하고 트랜잭션이 종료되었으므로 락을 반납한다.


<BR><BR>

## DB 락 - 변경


1️⃣ 기본 데이터 입력

<img width="419" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/017ed5ca-c155-495f-b816-61feaa7a7d7f">

<img width="481" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/06cc1289-0ea4-4ba9-9007-ff254765d111">


### 변경과 락

<img width="416" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/39db6efb-0d79-4f80-9dbd-8b57de20e403">

```
set autocommit false;
update member set money=500 where member_id = 'memberA';
```

- 세션1이 트랜잭션을 시작하고, memberA 의 데이터를 500원으로 업데이트 했다. 아직 커밋은 하지 않았다.
- memberA 로우의 락은 세션1이 가지게 된다.



<br>

<img width="419" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/575a301d-9cc0-46ca-8267-9023a2e7e31a">

```
SET LOCK_TIMEOUT 60000;
set autocommit false;
update member set money=1000 where member_id = 'memberA';
```

- 세션2는 memberA 의 데이터를 1000원으로 수정하려 한다.
-세션1이 트랜잭션을 커밋하거나 롤백해서 종료하지 않았으므로 아직 세션1이 락을 가지고 있다. 따라서 세션2는 락을 획득하지 못하기 때문에 데이터를 수정할 수 없다. 세션2는 락이 돌아올 때 까지 대기하게 된다.
- SET LOCK_TIMEOUT 60000 : 락 획득 시간을 60초로 설정한다. 60초 안에 락을 얻지 못하면 예외가 발생
한다.
    - 참고로 H2 데이터베이스에서는 딱 60초에 예외가 발생하지는 않고, 시간이 조금 더 걸릴 수 있다.


<br>

### 세션2 락 획득

세션1을 커밋하면 세션1이 커밋되면서 락을 반납한다. 이후에 대기하던 세션2가 락을 획득하게 된다. 따라
서 락을 획득한 세션2의 업데이트가 반영되는 것을 확인할 수 있다. 물론 이후에 세션2도 커밋을 호출해서
락을 반납해야 한다.

<br>

락3
<img width="420" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/ac888130-a670-4237-a4bd-f653c2f96378">

6. 세션1은 커밋을 수행한다. 커밋으로 트랜잭션이 종료되었으므로 락도 반납한다.


**세션 1 커밋**
```
commit;
```

세션1이 커밋하면 이후에 락을 반납하고 다음 시나리오가 이어진다.



<br>

<img width="420" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/9649c9ff-60ac-4248-994d-52d0c5282cb8">

- 락을 획득하기 위해 대기하던 세션2가 락을 획득한다.


<br>

<img width="417" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/1de34fc9-14ac-4c8d-80db-4ffa33ff3d3b">

7. 세션2는 update sql을 정상 수행한다.

<img width="505" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/a88cf59b-3dde-49a1-9318-c9e6e0540e53">


<br>

<img width="423" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/8fd47e7b-be3b-4539-8cfe-8ef6df8dad08">

8. 세션2는 커밋을 수행하고 트랜잭션이 종료되었으므로 락을 반납한다.


**세션 2 커밋**
```
commit;
```


<img width="507" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/132fb390-a78f-4cdf-a743-1b2262330bc1">



<br>

### 세션 2 락 타임아웃 

- `SET LOCK_TIMEOUT <milliseconds>` : 락 타임아웃 시간을 설정한다.
- 예) SET LOCK_TIMEOUT 10000 10초, 세션2에 설정하면 세션2가 10초 동안 대기해도 락을 얻지 못하면 락 타임아웃 오류가 발생한다.

<img width="799" alt="image" src="https://github.com/zeunxx/Inflearn-Spring-RoadMap/assets/81572478/2f1e3f06-19c5-41d3-8664-db09d2f000c3">

- 세션1이 memberA 의 데이터를 변경하고, 트랜잭션을 아직 커밋하지 않았다. 따라서 세션2는 세션1이 트랜잭션을 커밋하거나 롤백할 때 까지 대기해야 한다. 기다리면 락 타임아웃 오류가 발생하는 것을 확인할 수 있다

<br>

