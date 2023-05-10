package com.example.spendwise.viewmodel

import android.app.Application
import android.icu.util.Calendar
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class RecordViewModel(
    application: Application): AndroidViewModel(application) {

    private val repository = RecordRepository(SpendWiseDatabase.getInstance(application).recordDao)
    private val calendar = Calendar.getInstance()

    var isSearchViewFocused = false

    private var _records = MutableLiveData<List<Record>?>()
    val allRecords: LiveData<List<Record>?>
    get() = _records
    private var filterByCategory = false


    var amountError: String = ""
    var queryText: String = ""
    var newQueryText: String = ""
    val searchedList = MutableLiveData<List<Record>?>()

    val incomeStatsDone = MutableLiveData<Boolean>()

    /*var records: LiveData<List<Record>> = repository.getAllRecords(userId).also {
        Log.e("Records", it.value?.size.toString())
    }*/

    private var _filteredRecordsForDashboard = MutableLiveData<List<Record>?>()
    val filteredRecordsForDashboard: LiveData<List<Record>?>
        get() = _filteredRecordsForDashboard

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
                val record = Record(userId, category.trim(), Helper.formatDecimalToThreePlaces(BigDecimal(amount)), type.trim(), Helper.getDate(date.trim())).apply {
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
//            record.date.contains(monthYear)
            calendar.time = record.date
            calendar.get(Calendar.MONTH + 1) == month&& calendar.get(Calendar.YEAR) == year
        }
    }

    fun fetchRecords() {
        val localMonth = month.value
        val localYear = year.value
        val monthYear = "${month.value}/${year.value}"
        Log.e("Record", "${recordType.value} fetch recs above if")
        if(recordType.value != "All") {
            Log.e("Record", "$allRecords fetech records if")
            _filteredRecords.value = _records.value?.filter {record ->
                calendar.time = record.date
                Log.e("Coroutine", "${calendar.time}, month - ${calendar.get(Calendar.MONTH) + 1}, local month - $localMonth" +
                        " ${calendar.get(Calendar.YEAR)}, local year = $localYear" +
                        " ${recordType.value}, ${record.type}")
                calendar.get(Calendar.MONTH) + 1 == localMonth && calendar.get(Calendar.YEAR) == localYear && record.type == recordType.value
//                record.date.contains(monthYear) && record.type == recordType.value
            }
            Log.e("Coroutine", _filteredRecords.value.toString())
        } else {
            Log.e("Record", "${allRecords.value?.size} - allrecords, ${_records.value?.size} - mutablerecords fetchrec else")
            _filteredRecords.value = _records.value?.filter {record ->
                calendar.time = record.date
                calendar.get(Calendar.MONTH) + 1 == localMonth && calendar.get(Calendar.YEAR) == localYear
//                record.date.contains(monthYear)
            }
        }
    }

    fun getIncomeOfTheMonth() {
        var income = BigDecimal(0)
        filteredRecords.value?.filter { record ->
            record.type == RecordType.INCOME.value
        }.also {
            it?.forEach { record ->
                Log.e("Total Balance", "record amount income - ${record.amount}")
                income += (record.amount).toBigDecimal()
                Log.e("Total Balance", "incrementing income - $income")
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
        Log.e("Total Balance", "Income - ${incomeOfTheMonth.value}, Expense - ${expenseOfTheMonth.value}")
        val balance = expenseOfTheMonth.value?.let { incomeOfTheMonth.value?.minus(it) } ?: BigDecimal(0)
        totalBalanceOfTheMonth.value = balance
    }

    private var user = 0

    fun fetchAllRecords(userId: Int) {
        viewModelScope.launch {
            var fetchedRecords: List<Record>? = null
            fetchedRecords = repository.getAllUserRecords(userId)
            Log.e("Coroutine", "fetched rec delay")
            withContext(Dispatchers.Main) {
                _records.value = fetchedRecords.also {
                    Log.e("Coroutine", "record fetch all _records.value $it")
                }
                _filteredRecordsForDashboard.value = fetchedRecords
                user = userId
            }
        }
        /*viewModelScope.launch*//*(Dispatchers.IO)*//* {
            var fetchedRecords: List<Record>? = null
            val job = launch {
                Log.e("UserID", "record fetch all records " + userId.toString())
                fetchedRecords = repository.getAllUserRecords(userId)
                Log.e("Record", userId.toString() + " viewmodel getusers below fetchedrecords ${fetchedRecords?.size}")
                Log.e("Record", _record.value.toString() + " inside job " +_record.value?.recordId.toString())
            }
            job.join()
            withContext(Dispatchers.Main) {
                Log.e("rec ", "")
                Log.e("Record", userId.toString() + " viewmodel getusers above ${_records.value?.size}")
                _records.value = fetchedRecords.also {
                    Log.e("UserID", "record fetch all _records.value " + it.toString())
                }
                _filteredRecordsForDashboard.value = fetchedRecords
                 user = userId
                Log.e("Record", userId.toString() + " viewmodel getusers below ${_records.value?.size}")
            }
           *//* Log.e("Record", userId.toString() + " viewmodel getusers above ${_records.value?.size}")
            _records.value = repository.getAllUserRecords(userId)
            Log.e("Record", userId.toString() + " viewmodel getusers below ${_records.value?.size}")*//*

        }*/
//        Log.e("Record", _record.value.toString() + " fetchAll " +_record.value?.recordId.toString())
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
        transformedList.forEach{
            Log.e("Record", "pie data viewmodel - $it")
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
            Log.e("Record", "getTransformed data income")
//            incomeStatsDone.value = true
            /*isIncomeDataUpdated.value = true*/
            transformedDataForPieChart(list.filter { it.type == RecordType.INCOME.value }.also {
                Log.e("Record", "getTransformed data income ${it.size}")
            }, type)
        } else if (type == RecordType.EXPENSE){
            isExpenseDataUpdated.value = true
            transformedDataForPieChart(list.filter { it.type == RecordType.EXPENSE.value }.also {
                Log.e("Record", "getTransformed data expense ${it.size}")
            }, type)
        }
    }

    private val _record = MutableLiveData<Record?>()
    val record : LiveData<Record?>
    get() = _record

    fun setSelectedRecord(record: Record) {
        _record.value = record.also {
            Log.e("Test", "inside setSelectedRec in rec Viewmodel : rec - $record")
        }
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
        val record = record.value?.let {
            Record(it.userId, category.trim(), Helper.formatDecimalToThreePlaces(BigDecimal(amount)), type.trim(), Helper.getDate(date.trim())).apply {
                recordId = it.recordId
                this.note = note.trim()
                this.description = description.trim()
            }
        }
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
        Log.e("Record", record.toString() + " update " +record?.recordId.toString())
    }

    fun deleteRecord(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val job = launch {
                _record.value?.let {
                    repository.deleteRecord(it)
                    Log.e("Record", user.toString() + " delete job " +_record.value?.recordId.toString())
                    fetchAllRecords(userId)
                    //fetchAllRecords(it.userId)
                    Log.e("Record", _record.value.toString() + " delete job " +_record.value?.recordId.toString())
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                /*if(filterByCategory) {
                    fetchAllRecords(user)
                    fetchRecords()
                }*/
                _record.value = null
            }
        }
        Log.e("Record", _record.value.toString() + " delete " +_record.value?.recordId.toString())
    }

    private val _recordsOfTheCategory = MutableLiveData<List<Record>?>()
    val recordsOfTheCategory: LiveData<List<Record>?>
    get() = _recordsOfTheCategory

    fun fetchRecordsOfTheCategory(category: Category) {
        /*_filteredRecords.value?.forEach {
            Log.e("Record", it.toString())
        }*/
        filterByCategory = true
        when(period) {
            Period.MONTH -> {
                _recordsOfTheCategory.value = _filteredRecords.value?.filter {
                    Log.e("Record", "cat in fetchrecordofcat ${it.category}, ${category.title}, ${it.type}, ${recordType.value}")
                    it.category == category.title && it.type == recordType.value
                }
            }
            Period.ALL -> {
                _recordsOfTheCategory.value = _records.value?.filter {
                    Log.e("Record", "cat in fetchrecordofcat ${it.category}, ${category.title}, ${it.type}, ${recordType.value}")
                    it.category == category.title && it.type == recordType.value
                }
            }
        }
        /*_recordsOfTheCategory.value = _filteredRecords.value?.filter {
            Log.e("Record", "cat in fetchrecordofcat ${it.category}, ${category.title}, ${it.type}, ${recordType.value}")
            it.category == category.title && it.type == recordType.value
        }*/
        Log.e("Record", "---------")
        _recordsOfTheCategory.value?.forEach {
            Log.e("Record", it.toString() + "fetch cat")
        }

        /*if(category.value != "All") {
            Log.e("filtered in all", "$allRecords")
            _filteredRecords.value = allRecords.value?.filter {record ->
                record.date.contains(monthYear) && record.type == recordType.value
            }
        } else {
            Log.e("filtered 1", "${allRecords.value}")
            _filteredRecords.value = allRecords.value?.filter {record ->
                record.date.contains(monthYear)
            }
        }*/
    }

    fun updateFilteredRecords(list: List<Record>) {
        Log.e("Record", "update filteredRecords cat ${list}")
        _filteredRecords.value = list
        _filteredRecords.value?.forEach {
            Log.e("Record", it.toString() + " updatefr")
        }
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
        /*_dataForExpensePieChart.value = null
        _dataForIncomePieChart.value = null
        _dataForPieChart.value = null*/
    }

   /* val month = MutableLiveData<Int>()
    val year = MutableLiveData<Int>()
    val recordType = MutableLiveData<String>()
    val incomeOfTheMonth = MutableLiveData<Float>()
    val expenseOfTheMonth = MutableLiveData<Float>()
    val totalBalanceOfTheMonth = MutableLiveData<Float>()
    val isIncomeDataUpdated = MutableLiveData<Boolean>()
    val isExpenseDataUpdated = MutableLiveData<Boolean>()
    private var _dataForIncomePieChart = MutableLiveData<List<Pair<Category, Float>>>()
    val dataForIncomePieChart: LiveData<List<Pair<Category, Float>>>
        get() = _dataForIncomePieChart
    private var _dataForExpensePieChart = MutableLiveData<List<Pair<Category, Float>>>()
    val dataForExpensePieChart: LiveData<List<Pair<Category, Float>>>
        get() = _dataForExpensePieChart*/

    var isTempDataSet = false
    val tempData = MutableLiveData<Map<String, String>>()

    init{
        dates()
    }

    fun dates() {
        viewModelScope.launch {
            repository.getAprilRecords().also {
                Log.e("Coroutine", "april" + it.toString())
            }
        }
    }

}