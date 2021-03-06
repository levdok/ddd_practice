package com.stringconcat.ddd.order.usecase.menu

import com.stringconcat.ddd.order.usecase.TestMealExtractor
import com.stringconcat.ddd.order.usecase.meal
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class GetMenuUseCaseTest {

    @Test
    fun `get menu - menu is empty`() {
        val mealExtractor = TestMealExtractor()
        val useCase = GetMenuUseCase(mealExtractor)
        val menu = useCase.execute()
        menu.shouldBeEmpty()
    }

    @Test
    fun `get menu`() {
        val meal = meal()
        val mealExtractor = TestMealExtractor().apply {
            this[meal.id] = meal
        }

        val useCase = GetMenuUseCase(mealExtractor)
        val menu = useCase.execute()
        menu shouldContainExactly listOf(
            MealInfo(
                id = meal.id,
                name = meal.name,
                description = meal.description,
                price = meal.price
            )
        )
    }
}