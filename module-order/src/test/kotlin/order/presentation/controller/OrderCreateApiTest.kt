package order.presentation.controller

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.OrderQueryUseCase
import com.playground.order.presentation.web.OrderController
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest

@WebMvcTest(OrderController::class)
@Suppress("NonAsciiCharacters")
class OrderCreateApiTest {
    private val orderCommandUseCase: OrderCommandUseCase = mockk()
    private val orderQueryUseCase: OrderQueryUseCase = mockk()

}