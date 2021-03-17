package com.stringconcat.dev.course.app.configuration

import com.stringconcat.ddd.kitchen.usecase.order.CreateOrderHandler
import com.stringconcat.ddd.order.usecase.menu.MealExtractor
import com.stringconcat.ddd.order.usecase.order.CrmProvider
import com.stringconcat.ddd.order.usecase.order.CustomerOrderExtractor
import com.stringconcat.dev.course.app.event.EventPublisherImpl
import com.stringconcat.dev.course.app.listeners.SendOrderToCRMAfterPaymentRule
import com.stringconcat.dev.course.app.listeners.SendOrderToKitchenAfterConfirmationRule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ContextsIntegration {

    @Bean
    fun customerOrderConfirmedListener(
        customerOrderExtractor: CustomerOrderExtractor,
        mealExtractor: MealExtractor,
        createOrderHandler: CreateOrderHandler,
        domainEventPublisher: EventPublisherImpl
    ): SendOrderToKitchenAfterConfirmationRule {

        val listener = SendOrderToKitchenAfterConfirmationRule(
            customerOrderExtractor = customerOrderExtractor,
            mealExtractor = mealExtractor,
            createOrder = createOrderHandler
        )

        domainEventPublisher.registerListener(listener)
        return listener
    }

    @Bean
    fun orderSentToCrmListener(
        customerOrderExtractor: CustomerOrderExtractor,
        crmProvider: CrmProvider,
        domainEventPublisher: EventPublisherImpl
    ): SendOrderToCRMAfterPaymentRule {

        val listener = SendOrderToCRMAfterPaymentRule(
            customerOrderExtractor = customerOrderExtractor,
            crmProvider = crmProvider
        )

        domainEventPublisher.registerListener(listener)
        return listener
    }
}