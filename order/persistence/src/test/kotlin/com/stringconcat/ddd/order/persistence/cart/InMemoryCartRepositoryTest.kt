package com.stringconcat.ddd.order.persistence.cart

import com.stringconcat.ddd.order.domain.cart.MealAddedToCartDomainEvent
import com.stringconcat.ddd.order.persistence.TestEventPublisher
import com.stringconcat.ddd.order.persistence.cart
import com.stringconcat.ddd.order.persistence.cartWithEvents
import com.stringconcat.ddd.order.persistence.customerId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

class InMemoryCartRepositoryTest {

    @Test
    fun `saving cart - cart doesn't exist`() {
        val eventPublisher = TestEventPublisher()
        val repository = InMemoryCartRepository(eventPublisher)
        val cart = cartWithEvents()

        repository.save(cart)

        val storedCart = repository.storage[cart.forCustomer]
        storedCart shouldBeSameInstanceAs cart
        eventPublisher.storage.shouldHaveSize(1)

        val event = eventPublisher.storage.first()
        event.shouldBeInstanceOf<MealAddedToCartDomainEvent>()
        event.cartId shouldBe cart.id
    }

    @Test
    fun `saving cart - cart exists`() {

        val customerId = customerId()
        val existingCart = cart(customerId = customerId)

        val eventPublisher = TestEventPublisher()
        val repository = InMemoryCartRepository(eventPublisher)
        repository.storage[customerId] = existingCart

        val updatedCart = cartWithEvents()
        repository.save(updatedCart)
        repository.storage[customerId] = updatedCart

        val event = eventPublisher.storage.first()
        event.shouldBeInstanceOf<MealAddedToCartDomainEvent>()
        event.cartId shouldBe updatedCart.id
    }

    @Test
    fun `get by id - cart exists`() {
        val customerId = customerId()
        val existingCart = cart(customerId = customerId)

        val eventPublisher = TestEventPublisher()
        val repository = InMemoryCartRepository(eventPublisher)
        repository.storage[customerId] = existingCart

        val cart = repository.getCart(customerId)
        cart shouldBeSameInstanceAs existingCart
    }

    @Test
    fun `get by id - cart doesn't exist`() {
        val eventPublisher = TestEventPublisher()
        val repository = InMemoryCartRepository(eventPublisher)
        val cart = repository.getCart(customerId())
        cart.shouldBeNull()
    }

    @Test
    fun `delete cart - cart exists`() {

        val existingCart = cart()
        val eventPublisher = TestEventPublisher()
        val repository = InMemoryCartRepository(eventPublisher)
        repository.storage[existingCart.forCustomer] = existingCart

        repository.deleteCart(existingCart)
        repository.storage.shouldBeEmpty()
    }

    @Test
    fun `delete cart - cart doesn't exist`() {

        val existingCart = cart()
        val eventPublisher = TestEventPublisher()
        val repository = InMemoryCartRepository(eventPublisher)

        repository.deleteCart(existingCart)
        repository.storage.shouldBeEmpty()
    }
}