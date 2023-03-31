package com.example.spendwise.enums

enum class Category(var type: RecordType) {

    //Expense
    FOOD(RecordType.EXPENSE),
    SHOPPING(RecordType.EXPENSE),
    TRANSPORT(RecordType.EXPENSE),
    VEHICLE(RecordType.EXPENSE),
    ENTERTAINMENT(RecordType.EXPENSE),
    PETS(RecordType.EXPENSE),
    TOURISM(RecordType.EXPENSE),
    HEALTH(RecordType.EXPENSE),
    EDUCATION(RecordType.EXPENSE),
    HOUSEHOLD(RecordType.EXPENSE),
    VARIANT(RecordType.EXPENSE),

    //Income
    SALARY(RecordType.INCOME),
    RENTAL_INCOME(RecordType.INCOME),
    SALE(RecordType.INCOME),
    GIFTS(RecordType.INCOME),
    COUPONS(RecordType.INCOME),
    OTHER(RecordType.INCOME)
}