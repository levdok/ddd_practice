package com.stringconcat.ddd.order.usecase.order

import com.stringconcat.ddd.order.domain.order.CustomerOrderHasBeenConfirmedEvent
import com.stringconcat.ddd.order.usecase.menu.TestCustomerOrderExtractor
import com.stringconcat.ddd.order.usecase.menu.TestCustomerOrderPersister
import com.stringconcat.ddd.order.usecase.menu.orderNotReadyForConfirm
import com.stringconcat.ddd.order.usecase.menu.orderReadyForConfirm
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

internal class ConfirmOrderUseCaseTest {

    private val order = orderReadyForConfirm()
    private val extractor = TestCustomerOrderExtractor().apply {
        this[order.id] = order
    }
    private val persister = TestCustomerOrderPersister()

    @Test
    fun `successfully confirmed`() {
        val useCase = CancelOrderUseCase(extractor, persister)
        val result = useCase.cancelOrder(orderId = order.id.value)

        result.shouldBeRight()

        val customerOrder = persister[order.id]
        customerOrder.shouldNotBeNull()
        customerOrder.popEvents() shouldContainExactly listOf(CustomerOrderHasBeenConfirmedEvent(order.id))
    }

    @Test
    fun `invalid state`() {

        val order = orderNotReadyForConfirm()
        extractor[order.id] = order

        val useCase = CancelOrderUseCase(extractor, persister)
        val result = useCase.cancelOrder(orderId = order.id.value)
        result shouldBeLeft ConfirmOrderUseCaseError.InvalidOrderState
    }

    @Test
    fun `order not found`() {
        extractor.clear()
        val useCase = CancelOrderUseCase(extractor, persister)
        val result = useCase.cancelOrder(orderId = order.id.value)
        result shouldBeLeft ConfirmOrderUseCaseError.OrderNotFound
    }
}