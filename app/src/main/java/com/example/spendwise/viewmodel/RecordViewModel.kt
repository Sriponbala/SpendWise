package com.example.spendwise.viewmodel

import android.app.Application
import android.icu.util.Calendar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.Category
import com.example.spendwise.domain.Record
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class RecordViewModel(
    application: Application): AndroidViewModel(application) {

    private val repository = RecordRepository(SpendWiseDatabase.getInstance(application).recordDao)
    private val calendar = Calendar.getInstance()

    var isSearchViewFocused = false

    var isAllFabVisible = false
    var isFabRotated = false

    private var _records = MutableLiveData<List<Record>?>()
    val allRecords: LiveData<List<Record>?>
    get() = _records
    private var filterByCategory = false


    var amountError: String = ""
    var titleError = ""

    var queryText: String = ""
    var newQueryText: String = ""
    val searchedList = MutableLiveData<List<Record>?>()

    private var _filteredRecordsForDashboard = MutableLiveData<List<Record>?>()

    private var _filteredRecords = MutableLiveData<List<Record>?>()
    val filteredRecords: LiveData<List<Record>?>
    get() = _filteredRecords

    val month = MutableLiveData<Int>()
    val year = MutableLiveData<Int>()
    val recordType = MutableLiveData<String>()
    val incomeOfTheMonth = MutableLiveData<BigDecimal?>()
    val expenseOfTheMonth = MutableLiveData<BigDecimal?>()
    val totalBalanceOfTheMonth = MutableLiveData<BigDecimal?>()

    fun insertRecord(userId: Int, category: String, amount: String, type: String, date: String, _note: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val job = launch {
                val record = Record(userId, category.trim(), Helper.formatDecimalToThreePlaces(BigDecimal(amount),), type.trim(), Helper.getDate(date.trim())).apply {
                    note = _note.trim()
                    description = desc.trim()
                }
                repository.insertRecord(record)
                fetchAllRecords(userId)
            }
        }
    }

    fun fetchRecords(month: Int, year: Int) {
        val monthYear = "$month/$year"
        _filteredRecords.value = allRecords.value?.filter {record ->
            calendar.time = record.date
            calendar.get(Calendar.MONTH + 1) == month&& calendar.get(Calendar.YEAR) == year
        }
    }

    fun fetchRecords() {
        val localMonth = month.value
        val localYear = year.value
        if(recordType.value != "All") {
            _filteredRecords.value = _records.value?.filter {record ->
                calendar.time = record.date
                calendar.get(Calendar.MONTH) + 1 == localMonth && calendar.get(Calendar.YEAR) == localYear && record.type == recordType.value
            }
        } else {
            _filteredRecords.value = _records.value?.filter {record ->
                calendar.time = record.date
                calendar.get(Calendar.MONTH) + 1 == localMonth && calendar.get(Calendar.YEAR) == localYear
            }
        }
    }

    fun getIncomeOfTheMonth() {
        var income = BigDecimal(0)
        filteredRecords.value?.filter { record ->
            record.type == RecordType.INCOME.value
        }.also {
            it?.forEach { record ->
                income += (record.amount).toBigDecimal()
            }
        }
        incomeOfTheMonth.value = income
    }

    fun getExpenseOfTheMonth() {
        var expense = BigDecimal(0)
        filteredRecords.value?.filter { record ->
            record.type == RecordType.EXPENSE.value
        }.also {
            it?.forEach { record ->
                expense += (record.amount).toBigDecimal()
            }
        }
        expenseOfTheMonth.value = expense
    }

    fun getTotalBalanceOfTheMonth() {
        val balance = expenseOfTheMonth.value?.let { incomeOfTheMonth.value?.minus(it) } ?: BigDecimal(0)
        totalBalanceOfTheMonth.value = balance
    }

    private var user = 0

    fun fetchAllRecords(userId: Int) {
        viewModelScope.launch {
            val fetchedRecords: List<Record>?
            fetchedRecords = repository.getAllUserRecords(userId)
            withContext(Dispatchers.Main) {
                _records.value = fetchedRecords
                _filteredRecordsForDashboard.value = fetchedRecords
                user = userId
            }
        }
    }

    private var _dataForPieChart = MutableLiveData<List<Pair<Category, BigDecimal>>?>()
    val dataForPieChart: LiveData<List<Pair<Category, BigDecimal>>?>
        get() = _dataForPieChart
    val isDataForPieChartUpdated = MutableLiveData<Boolean>()

    fun transformedDataForPieChart(list: List<Record>, type: RecordType = RecordType.ALL) {
        val transformedList = mutableListOf<Pair<Category, BigDecimal>>()
        val categories = mutableListOf<Category>()
        list.map { it.category }.toSet().forEach {categoryName ->
            Categories.categoryList.forEach {category ->
                if(category.title == categoryName) {
                    categories.add(category)
                }
            }
        }
        categories.forEach {category ->
            var amount = BigDecimal(0)
            list.filter {record ->
                record.category == category.title
            }.forEach {
                amount+= (it.amount).toBigDecimal()
            }
            transformedList.add(Pair(category, amount))
        }
        isDataForPieChartUpdated.value = true
        when(type) {
            RecordType.INCOME -> {
                _dataForIncomePieChart.value = transformedList
                isIncomeDataUpdated.value = true
            }
            RecordType.EXPENSE -> {
                _dataForExpensePieChart.value = transformedList
                isExpenseDataUpdated.value = true
            }
            else -> {
                _dataForPieChart.value = transformedList
                isDataForPieChartUpdated.value = true
            }
        }

    }

    val isIncomeDataUpdated = MutableLiveData<Boolean>()
    val isExpenseDataUpdated = MutableLiveData<Boolean>()
    private var _dataForIncomePieChart = MutableLiveData<List<Pair<Category, BigDecimal>>?>()
    val dataForIncomePieChart: LiveData<List<Pair<Category, BigDecimal>>?>
    get() = _dataForIncomePieChart
    private var _dataForExpensePieChart = MutableLiveData<List<Pair<Category, BigDecimal>>?>()
    val dataForExpensePieChart: LiveData<List<Pair<Category, BigDecimal>>?>
    get() = _dataForExpensePieChart

    fun getDataTransformed(list: List<Record>, type: RecordType) {
        if(type == RecordType.INCOME) {
            transformedDataForPieChart(list.filter { it.type == RecordType.INCOME.value }, type)
        } else if (type == RecordType.EXPENSE){
            isExpenseDataUpdated.value = true
            transformedDataForPieChart(list.filter { it.type == RecordType.EXPENSE.value }, type)
        }
    }

    private val _record = MutableLiveData<Record?>()
    val record : LiveData<Record?>
    get() = _record

    fun setSelectedRecord(record: Record) {
        _record.value = record
    }

    fun updateRecord(
        userId: Int,
        category: String,
        amount: String,
        type: String,
        date: String,
        note: String,
        description: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var updatedRecord: Record? = null
            val job = launch {
                _record.value?.let {
                    updatedRecord = Record(it.userId, category.trim(), Helper.formatDecimal(BigDecimal(amount)), type.trim(), Helper.getDate(date.trim())).apply {
                        recordId = it.recordId
                        this.note = note.trim()
                        this.description = description.trim()
                    }
                    repository.updateRecord(updatedRecord!!)
                    fetchAllRecords(userId)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _record.value = updatedRecord
            }
        }
    }

    fun deleteRecord(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val job = launch {
                _record.value?.let {
                    repository.deleteRecord(it)
                    fetchAllRecords(userId)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _record.value = null
            }
        }
    }

    private val _recordsOfTheCategory = MutableLiveData<List<Record>?>()
    val recordsOfTheCategory: LiveData<List<Record>?>
    get() = _recordsOfTheCategory

    fun fetchRecordsOfTheCategory(category: Category) {
        filterByCategory = true
        when(period) {
            Period.MONTH -> {
                _recordsOfTheCategory.value = _filteredRecords.value?.filter {
                    it.category == category.title && it.type == recordType.value
                }
            }
            Period.ALL -> {
                _recordsOfTheCategory.value = _records.value?.filter {
                    it.category == category.title && it.type == recordType.value
                }
            }
        }

    }

    fun updateFilteredRecords(list: List<Record>) {
        _filteredRecords.value = list
    }

    val category = MutableLiveData<Category?>()
    var period = Period.MONTH

    fun clear() {
        _records.value = null
        _filteredRecords.value = null
        _recordsOfTheCategory.value = null
        _record.value = null
        incomeOfTheMonth.value = null
        expenseOfTheMonth.value = null
        totalBalanceOfTheMonth.value = null
        _dataForPieChart.value = null
        _dataForIncomePieChart.value = null
        _dataForExpensePieChart.value = null
    }

    var isTempDataSet = false
    val tempData = MutableLiveData<Map<String, String>>()


}