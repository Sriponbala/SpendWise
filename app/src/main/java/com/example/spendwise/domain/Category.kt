package com.example.spendwise.domain

import android.os.Parcelable
import com.example.spendwise.enums.RecordType
import kotlinx.android.parcel.Parcelize

@Parcelize // because to send this reference as arg to a fragment
data class Category(val title: String, val logo: Int, val recordType: RecordType, val bgColor: Int): Parcelable
