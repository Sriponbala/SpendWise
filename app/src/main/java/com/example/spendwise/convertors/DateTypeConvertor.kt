package com.example.spendwise.convertors

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateTypeConvertor {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}