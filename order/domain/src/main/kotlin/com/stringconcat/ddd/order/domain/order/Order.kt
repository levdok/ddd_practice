package com.stringconcat.ddd.order.domain.order

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.stringconcat.ddd.common.types.base.AggregateRoot
import com.stringconcat.ddd.common.types.base.DomainEvent
import com.stringconcat.ddd.common.types.base.Version
import com.stringconcat.ddd.common.types.common.Count
import com.stringconcat.ddd.order.domain.cart.Cart
import com.stringconcat.ddd.order.domain.cart.CustomerId
import com.stringconcat.ddd.order.domain.rules.HasActiveOrderForCustomerRule
import com.stringconcat.ddd.order.domain.menu.MealId
import com.stringconcat.ddd.order.domain.menu.Price
import com.stringconcat.ddd.order.domain.providers.MealPriceProvider
import java.time.OffsetDateTime

class Order internal constructor(
    id: OrderId,
    val created: OffsetDateTime,
    val customerId: CustomerId,
    val orderItems: Set<OrderItem>,
    version: Version
) : AggregateRoot<OrderId>(id, version) {

    internal var state: OrderState = OrderState.WAITING_FOR_PAYMENT

    companion object {
        fun from(
            cart: Cart,
            idGenerator: OrderIdGenerator,
            activeOrder: HasActiveOrderForCustomerRule,
            priceProvider: MealPriceProvider
        ): Either<CreateOrderFromCartError, Order> {

            if (activeOrder.hasActiveOrder(cart.customerId)) {
                return CreateOrderFromCartError.AlreadyHasActiveOrder.left()
            }


            val meals = cart.meals()

            return if (meals.isNotEmpty()) {

                val items = meals.map {
                    val mealId = it.key
                    val count = it.value
                    val price = priceProvider.price(mealId)
                    OrderItem(mealId, price, count)
                }.toSet()

                val id = idGenerator.generate()
                Order(
                    id = id,
                    created = OffsetDateTime.now(),
                    customerId = cart.customerId,
                    orderItems = items,
                    version = Version.generate()
                ).apply { addEvent(OrderHasBeenCreatedEvent(id)) }.right()

            } else {
                CreateOrderFromCartError.EmptyCart.left()
            }
        }
    }

    fun confirm() = changeState(OrderState.CONFIRMED, OrderHasBeenConfirmedEvent(id))

    fun pay() = changeState(OrderState.PAID, OrderHasBeenPaidEvent(id))

    fun complete() = changeState(OrderState.COMPLETED, OrderHasBeenCompletedEvent(id))

    fun cancel() = changeState(OrderState.CANCELLED, OrderHasBeenCancelledEvent(id))


    private fun changeState(newState: OrderState, event: DomainEvent): Either<InvalidState, Unit> {
        return if (state.canChangeTo(newState)) {
            state = newState
            addEvent(event)
            Either.right(Unit)
        } else {
            Either.left(InvalidState)
        }
    }

    fun totalPrice(): Price {

    }

    fun isActive(): Boolean {
        return state.active
    }

    fun isCompleted(): Boolean {
        return state == OrderState.COMPLETED
    }

    fun isCancelled(): Boolean {
        return state == OrderState.CANCELLED
    }
}


class OrderItem(
    val mealId: MealId,
    val price: Price,
    val count: Count
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderItem

        if (mealId != other.mealId) return false

        return true
    }

    override fun hashCode(): Int {
        return mealId.hashCode()
    }
}


enum class OrderState(
    val active: Boolean,
    private val nextStates: Set<OrderState> = emptySet()
) {

    CANCELLED(false),
    COMPLETED(false),
    CONFIRMED(true, setOf(COMPLETED)),
    PAID(true, setOf(CONFIRMED, CANCELLED)),
    WAITING_FOR_PAYMENT(active = true, setOf(PAID));

    fun canChangeTo(state: OrderState) = nextStates.contains(state)
}

sealed class CreateOrderFromCartError {
    object EmptyCart : CreateOrderFromCartError()
    object AlreadyHasActiveOrder : CreateOrderFromCartError()
}


object InvalidState
