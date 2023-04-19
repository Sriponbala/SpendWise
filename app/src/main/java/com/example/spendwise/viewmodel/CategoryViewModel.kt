package com.example.spendwise.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spendwise.domain.Category

class CategoryViewModel: ViewModel() {

    val category = MutableLiveData<Category?>()

    var queryText: String = ""

    val filteredCategoryList = MutableLiveData<List<Category>>()

    val searchedList = MutableLiveData<List<Category>>()

}