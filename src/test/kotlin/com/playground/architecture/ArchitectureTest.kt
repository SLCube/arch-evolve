package com.playground.architecture

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class ArchitectureTest {
    private val allClasses = ClassFileImporter().importPackages("com.playground") // 모든 클래스 임포트

    // 프로덕션 클래스만 필터링 (테스트 클래스 제외)
    private val productionClasses = allClasses.that(JavaClass.Predicates.resideOutsideOfPackage("..test.."))

    // 1. Domain 계층 규칙: Common 및 표준 라이브러리에만 의존해야 한다.
    @Test
    fun `Domain 계층 의존성 규칙 검사`() {
        classes()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "kotlin..",
                "org.jetbrains..", // Kotlin 컴파일러 관련 어노테이션
                "..domain..", // Domain 내부 요소
                "..common..", // Common 유틸리티, BusinessException, ErrorCode 등
            ).check(productionClasses)
    }

    // 새로운 규칙 추가 (부정적 의존성)
    @Test
    fun `Domain 계층은 Application, Persistence, Presentation 계층에 의존해서는 안 된다`() {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..application..", "..persistence..", "..presentation..")
            .check(productionClasses)
    }

    // 2. Application 계층 (Ports) 규칙: Domain, Common, Spring 표준 라이브러리에만 의존해야 한다.
    // Ports는 인터페이스이므로, 구체적인 서비스 구현체나 어댑터에 의존해서는 안 된다.
    @Test
    fun `Application Port 계층 의존성 규칙 검사`() {
        classes()
            .that()
            .resideInAPackage("..application.port..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "kotlin..",
                "org.springframework..", // @Transactional 등 Spring 어노테이션 사용 가능
                "org.jetbrains..",
                "..domain..",
                "..common..",
                "..application.port..", // Port 내의 다른 Port (in/out), Command/Query DTO
                "..application.service.result..", // Query UseCase의 반환 타입으로 사용되는 Result DTO
            ).check(productionClasses)
    }

    // 3. Application 계층 (Services) 규칙: Domain, Application Port, Common, Spring 표준 라이브러리, Application 내부 요소에만 의존해야 한다.
    // Services는 Persistence나 Presentation에 직접 의존해서는 안 된다.
    @Test
    fun `Application Service 계층 의존성규칙 검사`() {
        classes()
            .that()
            .resideInAPackage("..application.service..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "kotlin..",
                "org.springframework..", // @Service, @Transactional 등 Spring 어노테이션 사용 가능
                "org.jetbrains..",
                "..domain..",
                "..common..",
                "..application..",
            ).check(productionClasses)
    }

    // 4. Persistence 계층 규칙: Domain, Application Port (Outbound), Common, Persistence 자체, Spring 표준 라이브러리에만 의존해야 한다.
    // Persistence 어댑터는 Application Port (Outbound)를 구현해야 하므로 이에 의존한다.
    @Test
    fun `Persistence 계층 의존성 규칙 검사`() {
        classes()
            .that()
            .resideInAPackage("..persistence..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "kotlin..",
                "org.springframework..",
                "org.jetbrains..",
                "..persistence..",
                "..domain..",
                "..common..",
                "..application.port.outbound..",
            ).check(productionClasses)
    }

    // 5. Presentation 계층 규칙: Application Port (Inbound), Application Service Result, Domain, Common, Presentation 자체, Spring, Jakarta, Kotlin 표준 라이브러리에만 의존해야 한다.
    // Presentation은 Application Port (Inbound)를 호출한다.
    @Test
    fun `Presentation 계층 의존성 규칙 검사`() {
        classes()
            .that()
            .resideInAPackage("..presentation..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "kotlin..",
                "org.springframework..", // @RestController, @RequestMapping 등 Spring 어노테이션 사용 가능
                "jakarta..", // @Valid, @NotBlank 등 Jakarta Validation 어노테이션 사용 가능
                "org.jetbrains..",
                "..presentation..", // Presentation 내부 DTO, Controller 등
                "..application.port.inbound..", // Inbound Port (UseCase 인터페이스) 호출
                "..application.service.result..", // Application Service에서 반환하는 Result DTO 사용
                "..domain..", // Domain Enum, Value Object 등 사용
                "..common..", // Common 유틸리티, PagedResponse 등 사용
                "kotlin.jvm.functions..", // Kotlin 람다 등 컴파일러 생성 클래스
                "..auth..",
            ).check(productionClasses)
    }

    @Test
    fun `Service 클래스 패키지 위치 검사`() {
        classes()
            .that()
            .haveSimpleNameEndingWith("Service")
            .should()
            .resideInAPackage("..application.service..")
            .check(productionClasses)
    }

    @Test
    fun `Port 인터페이스 패키지 위치 검사`() {
        classes()
            .that()
            .haveSimpleNameEndingWith("Port")
            .and()
            .areInterfaces()
            .should()
            .resideInAPackage("..application.port..")
            .check(productionClasses)
    }

    @Test
    fun `Adapter 클래스 패키지 위치 검사`() {
        classes()
            .that()
            .haveSimpleNameEndingWith("Adapter")
            .should()
            .resideInAPackage("..adapter..")
            .check(productionClasses)
    }

    @Test
    fun `Controller 클래스 패키지 위치 검사`() {
        classes()
            .that()
            .haveSimpleNameEndingWith("Controller")
            .should()
            .resideInAPackage("..presentation.web..")
            .check(productionClasses)
    }
}
