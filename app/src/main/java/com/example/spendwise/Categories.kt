package com.example.spendwise

import com.example.spendwise.domain.Category
import com.example.spendwise.enums.RecordType

object Categories {

    val categoryList = listOf(
        Category("Food", R.drawable.baseline_fastfood_24 , RecordType.EXPENSE, R.color.food_color),
        Category("Shopping", R.drawable.baseline_shopping_bag_24, RecordType.EXPENSE, R.color.shopping_color),
        Category("Transport", R.drawable.baseline_directions_bus_24, RecordType.EXPENSE, R.color.transport_color),
        Category("Vehicle", R.drawable.baseline_directions_car_24, RecordType.EXPENSE, R.color.vehicle_color),
        Category("Entertainment", R.drawable.baseline_emoji_people_24, RecordType.EXPENSE, R.color.entertainment_color),
        Category("Pets", R.drawable.baseline_pets_24, RecordType.EXPENSE, R.color.pets_color),
        Category("Tourism", R.drawable.baseline_travel_explore_24, RecordType.EXPENSE, R.color.tourism_color),
        Category("Health", R.drawable.baseline_health_and_safety_24, RecordType.EXPENSE, R.color.health_color),
        Category("Education", R.drawable.baseline_school_24, RecordType.EXPENSE, R.color.education_color),
        Category("Household", R.drawable.baseline_house_24, RecordType.EXPENSE, R.color.household_color),

        Category("Salary", R.drawable.baseline_money_24, RecordType.INCOME, R.color.salary_color),
        Category("Rental", R.drawable.baseline_car_rental_24, RecordType.INCOME, R.color.rental_color),
        Category("Sale", R.drawable.baseline_sell_24, RecordType.INCOME, R.color.sale_color),
        Category("Gifts", R.drawable.baseline_card_giftcard_24, RecordType.INCOME, R.color.gifts_color),
        Category("Coupons", R.drawable.baseline_discount_24, RecordType.INCOME, R.color.coupons_color),
    )

}
