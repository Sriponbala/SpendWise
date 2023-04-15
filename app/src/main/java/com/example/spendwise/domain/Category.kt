package com.example.spendwise.domain

import android.graphics.Color
import android.os.Parcelable
import com.example.spendwise.enums.RecordType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(val title: String, val logo: Int, val recordType: RecordType, val bgColor: Int): Parcelable
