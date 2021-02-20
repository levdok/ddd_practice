package com.stringconcat.ddd.kitchen.usecase.order

import com.stringconcat.ddd.kitchen.domain.order.KitchenOrderHasBeenCookedEvent
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.matchers.collections.shouldNotContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class CookOrderUseCaseTest {

    private val order = order()
    private val persister = TestKitchenOrderPersister()
    private val extractor = TestKitchenOrderExtractor().apply {
        this[order.id] = order
    }

    @Test
    fun `successfully complete`() {
        val useCase = CookOrderUseCase(extractor, persister)
        val result = useCase.cookOrder(order.id.value)
        result.shouldBeRight()

        val savedOrder = persister[order.id]
        savedOrder shouldBe order
        order.popEvents() shouldNotContainExactly listOf(KitchenOrderHasBeenCookedEvent(order.id))
    }

    @Test
    fun `order not found`() {
        extractor.clear()
        val useCase = CookOrderUseCase(extractor, persister)
        val result = useCase.cookOrder(order.id.value)
        result shouldBeLeft CookOrderUseCaseError.OrderNotFound
    }
}