## ORM과 투명한 영속성 - Entity 생명주기와 식별자
[전체 목차로](../README.md)

### 목차 <a id="0"></a>
1. 되돌아보기
2. [Ref Obj의 별칭 - ENTITY](#2)
3. [Entity의 생명 주기](#3)
4. [Entity의 식별자(Identity)](#4)

### 되돌아보기

지금까지 고객이 원하는 주문 시스템을 위해 다음의 작업을 했다.

1. 도메인 구성하는 중요 개념의 식별
2. 클래스 다이어그램의 클래스와 속성, 책임 용어 설정
    - 고객과 개발자들이 주문 도메인을 동일한 언어와 의미로 바라볼 수 있게 함
3. 시스템 구현 - 도메인 개념 (고객, 주문, 주문 항목 ETC) 반영
4. 영속성 보장을 위한 RDB 도입
5. DB접근에 의한 파급 제한, 시스템 결합도를 낮추기 위해 DI 사용
    - DI 지원하는 Spring framework 사용
    - DB 로직을 캡슐화하기 위한 Repository 리팩토링

#### DDD 개념 되짚기
- DDD는 도메인 모델과 SW모델, 즉 코드 간의 표현적 차이를 최소화하기 위한 접근 방법이다. DDD는 기술이 SW 개발을 주도하여 발생되는 문제점을 해결하기 위해 기술 주도적 방식이 아닌 도메인 주도적 방식으로 SW를 개발할 것을 주장한다.

#### DDD를 위해 갖춰야 할 것

1. 고객과 개발자들 사이 공통 용어(**UBIQUITOUS LANGUAGE**)의 사용
    - 의사소통 단절 및 오해로 인한 SW 오개발 방지
2. **MODEL-DRIVEN DESIGN**으로 분석, 설계, 구현의 모든 단계를 관통하는 하나의 개념의 생성
    - 표현적 차이를 줄임으로 SW가 도메인의 모습을 투명하게 만듦
    - 이를 통해 하위의 인프라를 변경할 경우에도 도메인 모델은 변경되지 않음
    - 모델 주도 설계를 적용하기 위해서는 표현적 차이가 적은 객체지향 언어를 사용하고 POJO 기반 경량 프레임워크 사용이 적합

지금까지는 주문 시스템에 고객의 용어를 반영하고 표현적 차이를 줄일 수 있도록 노력했다. 또한, 인프라 스트럭쳐의 변경이 핵심 도메인 모델에 영향을 미치지 않도록 여러 원칙들을 적용해왔다. 그러나 rdb를 도입하기 위해서는 여기에 몇 가지 더 필요한 것이 있다.

### Ref Obj의 별칭 - ENTITY <a id="2"></a>
[목차로](#0)

#### Reference Object의 특성
- Ref Obj는 시스템 내 유일하게 존재하며 상태 추적이 가능한 도메인 객체이다. 
- Ref Obj는 유일성과 추적성을 만족시키기 위해 식별자를 가지며 동일 객체를 다른 이름으로 참조할 수 있도록 별칭을 허용한다. 
- 별칭은 여러 문제를 야기하지만 그와 동시에 시스템 모든 부분이 Ref Obj의 상태를 공유할 수 있도록 하여 도메인 무결성을 유지하도록 한다.
- Ref Obj의 동일함을 테스트하기 위해서는 "=="를 사용하여 식별자를 비교하며 대개의 경우 객체가 위치한 메모리 주소를 식별자로 사용한다.
- Ref Obj는 도메인 객체의 서로 다른 두 가지 측면을 설명하기 위해 사용된다.
    1. 의미론적 측면: Ref Obj의 의미론적 특성은 REPOSITORY AGGREGATE와 같은 다른 도메인 요소에 영향을 미친다.
    2. 기술적인 측면: Ref Objㅇ는 생성 시 메모리 주소 기반 식별자를 할당 받으며 "==" 연산자의 경우 식별자를 사용해 객체 동일성 판단한다는 것을 가정
    - Ref Obj의 의미론과 기술론은 충돌하는 영역이 존재한다. 의미론은 도메인의 특성이며 기술론은 언어 자체의 특성이다. 

의미론이 도메인의 본질적인 특성이라면 기술론은 비본질적 특성이다. 본질적 특성은 변하지 않는 반면 비본질적 특성은 언어, 환경, 기술에 따라 영향을 받을 수 있다. SW 개발의 복잡성은 본질적 특성에 기인한 것이다. 그러므로 SW 문제의 본질을 다루는 도전, 복잡한 개념적 구조의 체계화를 우선적으로 고려해야 한다.

도메인의 본질적 특성을 강조하고 기술에 종속된 비본질적 개념을 감추기 위해 Ref Obj를 대체할 수 있는 용어가 필요하다. 우리는 Ref Obj의 기술적 측면을 캡슐화하고 도메인 측면만을 추상화하기 위해 ENTITY라는 용어를 사용한다.

#### Entity
Entity의 개념은 Ref Obj와 동일하다. 식별자를 가지고 추적가능하며 영속성을 가지는 도메인 개념이다. Entity는 속성이 아닌 식별자로 구분된다. 반면 VO는 식별자가 아닌 속성으로 구별되고 일반적으로 VO의 생명주기는 Entity 종속적이다.

Entity는 도메인 내에 존재하는 개념의 연속성을 강조한다. 따라서 Entity라는 용어는 단순 시스템 내 존재하는 객체만을 지칭하지 않는다. 이는 표현 매체 특성과 기술에 독립적이다.

> 고객의 신규 가입을 처리하기 위해 시스템은 고객 정보를 상태로 가지는 객체를 생성한다. 생성된 고객 객체는 비즈니스 로직을 처리하기 위해 사용된 후 영구 저장소에 보관된다. 대부분의 경우 영구 저장소로 관계형 데이터베이스를 사용할 것이며 고객 객체는 데이터베이스 테이블의 한 레코드로 저장될 것이다. 시스템은 다시 고객 정보가 필요한 경우 데이터베이스로부터 레코드를 읽어 고객 객체의 상태를 복원하고 비즈니스 로직을 처리한 후 변경된 상태를 다시 데이터베이스에 저장한다. 시스템은 주기적으로 고객 정보를 공유하는 제3의 시스템에 해당 고객 정보를 전송해야 할 수도 있다. 시스템은 고객 정보를 바이트 스트림으로 변경한 후 네트워크를 통해 다른 시스템에 전송한다. 고객 정보를 수신한 시스템은 다시 바이트 스트림을 고객 객체로 복원하고 비즈니스 로직을 처리한 후 적합한 데이터베이스에 저장한다.

위 예에서 신규 가입 고객의 정보는 객체, 데이터베이스 레코드, 네트워크 전송을 위한 바이트 스트림, 타 시스템 내의 객체, 타 시스템 내의 데이터베이스 레코드와 같이 다양한 형태로 변경된다. 그러나 이들 형태와 무관하게 이들은 한가지 동일한 속성을 공유한다. 즉, 도메인 내에 존재하는 동일한 고객의 상태를 표현한다는 것이다. 이것이 ENTITY의 본질이다.

Entity는 도메인 객체의 표현 상태를 초월해 동일한 도메인 개념의 추적성과 유일성을 강조한다. Entity라는 용어를 통해 메모리 상 고객 객체와 DB 저장된 고객 테이블의 레코드를 동일 대상으로 바라볼 수 있다. 고객 객체는 GC에 의해 소멸되더라도 DB를 통해 영속적 추적이 가능하다. Entity 용어는 도메인 개념의 연속성과 생명주기를 구현 기술에 독립적인 문맥 상에서 이해할 수 있도록 한다. 이것이 자칫 도메인 객체의 생명주기를 객체 자체 생명주기와 혼동할 여지가 있는 Ref Obj라는 용어를 사용하지 않음으로 얻게 되는 궁극적 효과이다.

Entity가 Ref Obj를 표현 형태와 기술에 독립적 개념으로 확장한다면 Ref Obj의 생명 주기와 식별자의 의미에도 적지 않은 변화를 수반한다.

### Entity의 생명주기 <a id="3"></a>
[목차로](#0)

엔터프라이즈 어플리케이션을 구성하는 도메인 객체의 생명 주기를 바라보는 시각은 크게 두 가지로 나눌 수 있다.

#### 구현 기술에 종속적으로 바라보는 시각
퍼시스턴트 메커니즘으로 JDBC를 직접 사용하는 TRANSACTION SCRIPT 패턴 기반 어플리케이션을 생각해보자. 이 경우 도메인 로직은 TRANSACTION SCRIPT 내에 절차적 방식으로 구현되며 도메인 개념들은 getter/setter만을 가지는 Anemic Domain Model로 구성된다. Anemic Domain Model은 도메인 개념을 반영한 클래스명과 속성을 가지지만 단순 레이어 간 데이터 전달만을 위해 사용되는 가짜 객체를 의미한다. Anemic Domain Model을 적용한 도메인 객체는 상태는 가지지만 행위는 포함하지 않는다. 

이 경우 정보를 DB에 저장하기 위해서는
1. 속성이 설정된 도메인 객체를 생성
2. JDBC API를 사용하여 DB에 정보 저장
3. 사용 종료된 도메인 객체를 GC에 넘겨 소멸시킴

DB에 저장된 데이터가 필요한 경우
1. JDBC API를 사용하여 쿼리를 실행
2. 새로운 도메인 객체 생성 후 반환된 결과 셋을 도메인 객체로 변환
3. 사용 종료된 도메인 객체를 GC에 넘겨 소멸시킴

이런 시각은 RDB와 객체 지향이라는 구현 기술에 지나치게 종속되어 있다. 도메인 객체들의 생명주기는 DB와의 상호작용 관점에서 파악된다. 도메인 개념의 유일성과 추적성은 기술에 파묻혀 그 의미를 상실하며 도메인 객체는 단지 DB 테이블 구조를 반영한 메모리 저장소로 파악된다. 이 시각을 가진 대부분 사람들은 도메인 객체 생명주기를 DB 상호작용 중심으로 파악하는 경향이 강하다.

#### ENTITY 관점에서 바라보는 시각
고객이 신규 가입한 경우 시스템에는 새로운 고객 정보를 갖는 새로운 ENTITY가 생성된다. 
1. 고객 ENTITY는 잠시 메모리 객체 형태로 시스템 내 존재한다. 
2. 비즈니스 로직 처리 이후 고객 ENTITY는 형태를 바꾸어 DB 레코드 형태로 존재한다. 
3. 다른 비즈니스 로직이 고객 정보를 필요로 할 경우 다시 메모리 객체로 형태를 바꾸어 로직을 처리한다.
4. 고객이 탈퇴를 하는 경우 ENTITY 정보가 소멸된다.
    - 탈퇴 이후로는 고객 정보가 DB 형태로든 메모리 객체 형태로든 시스템 내에 존재하지 않게 된다.

여기서 도메인 객체는 단순 DB와 상호작용을 위해 필요한 데이터 저장소가 아니다. 도메인 객체는 ENTITY의 한 형태이다. GC에 의한 도메인 객체의 소멸은 도메인 개념 소멸이 아닌 불필요 메모리의 해제이다. 도메인 개념 자체는 사라지지 않는다. 이런 시각은 도메인 분석 시 발견된 연속성, 추적성을 그대로 구현 레벨에 반영하기 위한 발상의 전환을 가져온다.

#### Entity개념에서의 시각을 위한 Repository
도메인을 시스템 개발의 주도적 위치로 격상시키기 위해서는 도메인 객체를 ENTITY 개념에서 바라볼 필요가 있다. 그리고 이를 위한 유용한 도구가 REPOSITORY이다.

고객이 신규 주문을 입력한 경우 
- OrderRepository는 새로운 Order Entity를 내부적으로 등록하고 관리한다. 저장된 Order가 필요한 경우 
- 클라이언트는 OrderRepository에 저장된 Order Entity를 찾아달라고 요청하며 OrderRepository는 관리하고 있던 Order Entity를 찾아 반환한다.
고객이 주문을 취소한 경우
- 등록된 Order Entity가 더 이상 필요하지 않으므로 OrderRepository에게 Order Entity를 제거하라고 요청한다. OrderRepository는 해당 Order Entity를 제거한다.

이 시나리오에서 Order Entity는 새로운 주문 정보가 추가되는 시점에 단 한번 생성된다. 이후로는 동일한 Order Entity가 동일한 주문 정보를 표현하며 최종적으로 해당 주문 정보가 도메인에서 사라지는 시점에 Order Entity도 소멸된다. 따라서 Order Entity 생명주기는 실제 도메인 내에서 일어나는 주문 생명주기와 일치한다. 이 관점에서는 Order Entity의 생명주기 중간에 새로운 객체가 생성된다고 생각하지 않는다. 단지 Repository가 이미 등록되어 있던 Order Entity를 반환할 뿐이다.

REPOSITORY는 ENTITY의 생성과 소멸 시점 사이를 책임지는 생명주기 관리 객체이다.
1. 우리는 REPOSITORY를 통해 동일한 ENTITY를 얻을 수 있다. 즉, ENTITY의 유일성을 보장할 수 있다.
2. 시스템 모든 부분이 REPOSITORY를 통해 ENTTITY에 접근하기에 변경된 정보는 시스템 전체에 전파된다.
3. REPOSITORY는 도메인 객체들이 메모리 내에서 관리된다는 신기루를 만들어냄으로써 도메인 모델 전체의 복잡성을 낮춘다.
4. REPOSITORY가 실제로 ENTITY를 어디에서 관리하는지 무관하게 REPOSITORY는 내부구현을 캡슐화하여 도메인 레이어에 객체지향적 컬렉션 관리 인터페이스를 제공한다.

지금까지 개발한 시스템에서
**OrderRepository**는 메모리 내 컬렉션을 관리하는 **Registrar**을 사용해서 **Order Entity**를 관리한다. 외부 도메인 객체는 **OrderRepository**가 이전에 등록한 동일 **Order Entity**를 메모리에 계속 저장하고 있다고 생각한다. 따라서 도메인 객체는 **OrderRepository**에게 해당 **Order Entity**를 찾아 달라고 요구하고 반환된 **Order Entity**는 이전에 등록된 그 **Order Entity**라고 생각한다.

이제 OrderRepository가 메모리가 아닌 DB에 Order Entity를 저장하도록 내부 구현을 변경하는 경우를 생각해 본다. 이전에 행한 인터페이스와 구현 클래스 분리, 의존성 주입을 위한 Spring Framework 도입 덕에 OrderRepository 내부 구현이 변경되더라도 도메인 레이어에는 영향을 미치지 않게 되었다. OrderReposiotory는 이제 내부적으로 DB에 접근하기 위해 다른 인프라스트럭쳐를 사용하는 클래스를 의존 주입하도록 수정되었지만 이는 외부에서 알 수 없다. 도메인 객체들은 여전히 OrderRepository가 메모리 컬렉션 내에서 동일한 Order Entity를 관리하고 있다고 가정할 수 있다. OrderRepository가 내부적으로 저장 메커니즘을 어떻게 변경해도 메모리 컬렉션 관점을 수정할 필요는 없다. 이것에 Repository를 통한 추상화의 장점이자 인터페이스를 통한 캡슐화의 힘이다.

그러나 OrderRepository가 DB를 사용할 경우 한 가지 문제가 발생한다. 지금까지 Ref Obj의 동일성을 테스트하기 위해 사용하는 == 연산자가 메모리 주소를 비교한다는 것이다. Repository가 저장소로 DB를 사용한다면 Entity를 룩업할 때마다 새로운 객체가 생성된다. 이는 동일한 Entity일지라도 동일한 메모리 주소를 갖지 않기에 식별자를 비교하는 == 테스트는 실패하게 된다. 추적성을 보장하기 위해서는 객체 지향 시스템이 제공하는 Ref Obj의 식별자를 그대로 사용할 수 없다. 다양한 형태로 변하는 Entity 특성을 고려하여 새로운 식별자의 개념이 필요해진다.

### Entity의 식별자(Identity) <a id="4"></a>
[목차로](#0)

Entity는 추적해야할 도메인 개념이 시간, 장소에 따라 다양한 형태를 지닐 수 있다는 개념을 도메인 모델에 도입한다. 따라서 변화되는 Entity의 모든 형태가 공유할 수 있는 일반적 식별자 개념이 필요하다. 즉, 생명주기에 걸쳐 구현 기술과 무관한 Entity의 유일성을 보장할 수 있는 속성을 식별자로 삼아야 한다.

#### Entity의 식별자

그렇다면 Entity의 식별자로 적당한 속성 특징은 무엇일까? 이는 DB 테이블의 후보 키 식별 방법으로부터 얻을 수 있다.

테이블의 후보 키 특징
- 후보 키의 모든 컬럼 값은 NULL이 아니어야 한다.
- 각 row는 유일한 값을 가진다.
- 값이 결코 변경되어서는 안된다.

DB 모델링에서는 이런 특성을 가진 후보 키중 가장 적절한 것을 PK로 삼는다. 나머지 후보 키들은 테이블의 UK(Unique key)로 선택된다.

동일한 특성은 Entity 식별자에도 적용될 수 있다. DB 테이블에 저장된 레코드는 Entity의 특별한 형태다. Entity가 여러 형태를 거쳐 소멸될 때까지 유일성, 추적성을 보장하는 방법은 영속성 메커니즘에 동일하게 적용될 수 있는 식별자를 사용하는 것이다.

이런 관점에서 DB의 모델링 과정을 Entity 중심 프로세스로 설명할 수 있다. 시스템에서 지속적으로 관리해야 할 Entity를 식별하고 해당 Entity의 유일성, 추적성을 보장하는 속성들을 찾아 Entity 후보 식별자로 선택한다. 후보 식별자 중 가장 적절한 것을 선택해 최종 식별자로 삼는다. 이 모델로부터 DB의 논리/물리 모델을 도출하고 주 키와 유일 키를 식별한다. 이는 Entity 개념을 중심으로 데이터 모델링 3단계인 개념, 논리, 물리 모델링을 확장한 것으로 볼 수 있다.

기반 모델의 정제를 통해 도메인 모델을 발전시켜가며 선택된 식별자를 Entity 속성으로 모델링하며 이는 테이블 주 키와 동일할 것이다. Entity 생명주기 중 XML형태로 표현될 필요가 있다면 Entity 식별자는 XML 엘리먼트로 표현될 것이다.

식별자에 대한 이런 관점은 MODEL-DRIVEN DESIGN 개념을 기반으로 한다. 즉, 통일된 하나의 기반 모델을 사용하여 SW 개발을 주도하는 것이다. 이를 통해 SW 개발 과정을 분리된 개별 활동이 아닌 하나의 큰 흐름으로 볼 수 있도록 틀을 제공한다.

#### Entity 식별자의 적용 - IDENTITY FIELD 패턴

이는 이론이 아닌 실용적 관점에서도 동일하다. 대부분 엔터프라이즈 어플리케이션은 영속성 메커니즘으로 RDB를 사용한다. 따라서 ENTITY 관정에서 영속성, 추적성, 유일성을 보장받을 수 있는 가장 좋은 방법은 DB 테이블 주 키를 식별자로 사용하는 것이다. 메모리 내 생성된 ENTITY가 DB의 한 ROW로 맵핑될 수 없다면 DB를 영속성 저장소로 사용하는 시스템에서 ENTITY를 추적할 수 없다. 따라서 ENTITY는 객체와 DB 레코드와의 추적성을 보장하기 위해 PK를 속성으로 포함해야 하며 DB 맵핑을 위해 이 속성을 사용해야 한다. 이처럼 모델 작성 시 DB PK를 ENTITY 식별자로 포함시키는 것을 **INDENTITY FIELD** 패턴이라 한다.

#### 정리
지금까지 공부한 도메인 객체를 식별하기 위해 사용되는 **3가지 식별자**의 개념은 다음과 같다.

- 두 오브젝트가 동일한 메모리 주소를 공유한다면 이들은 동일하며 "=="연산자를 사용해 확인할 수 있다. 이를 **객체 식별자**라 한다. Ref Obj를 살펴보며 객체 식별자 개념을 공부했다.
- 두 오브젝트가 동일한 값을 가진다면 이들은 동등하다. 이것은 equals 메소드를 사용해 확인할 수 있다. 이를 **동등성(equality)** 이라 한다. VO를 살펴보며 동등성 개념을 공부했다.
- 두 오브젝트가 DB의 동일 row에 저장되어 있을 경우(동일한 테이블과 동일한 주 키 공유) 오브젝트는 동일하다. 이를 **DB식별자**라 한다.

DB 식별자를 Entity 식별자로 사용하기 위해서는 객체 동일성 확인을 위해 Identity field를 비교해야 한다. 따라서 단순히 Ref Obj처럼 "=="를 사용하여 동일성 비교가 불가능하며 VO처럼 equals()를 오버라이딩 해야한다. 차이점은 VO가 모든 속성을 비교한다면 Entity는 DB 키를 비교한다는 점이다. 단, DB 테이블에서 surrogate key를 주 키로 사용하는 경우 대리 키 대신 natural key를 비교해야 한다. 대리 키를 비교할 경우 영속성 객체가 *비영속 상태*나 *준영속 상태*일 때 해쉬 기반 처리가 오작동할 우려가 있기 때문이다.

이처럼 테이블 주 키로 대리키가 사용될 경우 동등성 비교를 위해 자연 키를 사용하는 것을 비즈니스 키 동등성이라 한다. equals() 오버라이딩 시 hashCode()또한 오버라이딩 해야함을 지으면 안된다. 두 오브젝트가 동일하다면 동일한 해쉬코드를 가져야한다.

다음 장에서는 REPOSITORY 구현에 필요한 ORM 기술에 관해 살펴본다.

 ---
출처: 이터너티님의 블로그

[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 1부](http://aeternum.egloos.com/1366968)  
[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 2부](http://aeternum.egloos.com/1380433)  
[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 3부](http://aeternum.egloos.com/1386122)  
[Domain-Driven Design의 적용-4.ORM과 투명한 영속성 4부](http://aeternum.egloos.com/1421145)  