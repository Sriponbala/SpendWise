package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.Categories
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.Category
import com.example.spendwise.domain.Record
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordViewModel(
    application: Application): AndroidViewModel(application) {

    private val repository = RecordRepository(SpendWiseDatabase.getInstance(application).recordDao)

    private var _records = MutableLiveData<List<Record>?>()
    val allRecords: LiveData<List<Record>?>
    get() = _records
    private var filterByCategory = false

    val incomeStatsDone = MutableLiveData<Boolean>()

    /*var records: LiveData<List<Record>> = repository.getAllRecords(userId).also {
        Log.e("Records", it.value?.size.toString())
    }*/

    private var _filteredRecords = MutableLiveData<List<Record>>()
    val filteredRecords: LiveData<List<Record>?>
    get() = _filteredRecords

    val month = MutableLiveData<Int>()
    val year = MutableLiveData<Int>()
    val recordType = MutableLiveData<String>()
    val incomeOfTheMonth = MutableLiveData<Float>()
    val expenseOfTheMonth = MutableLiveData<Float>()
    val totalBalanceOfTheMonth = MutableLiveData<Float>()

    fun insertRecord(userId: Int, category: String, amount: String, type: String, date: String, _note: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val job = launch {
                val record = Record(userId, category, amount.toFloat(), type, date).apply {
                    note = _note
                    description = desc
                }
                repository.insertRecord(record)
                fetchAllRecords(userId)
            }
        }
    }

    fun fetchRecords(month: Int, year: Int) {
        val monthYear = "$month/$year"
        _filteredRecords.value = allRecords.value?.filter {record ->
            record.date.contains(monthYear)
        }
    }

    fun fetchRecords() {
        val monthYear = "${month.value}/${year.value}"
        Log.e("Record", "${recordType.value} fetch recs above if")
        if(recordType.value != "All") {
            Log.e("Record", "$allRecords fetech records if")
            _filteredRecords.value = _records.value?.filter {record ->
                record.date.contains(monthYear) && record.type == recordType.value
            }
        } else {
            Log.e("Record", "${allRecords.value?.size} - allrecords, ${_records.value?.size} - mutablerecords fetchrec else")
            _filteredRecords.value = _records.value?.filter {record ->
                record.date.contains(monthYear)
            }
        }
    }

    fun getIncomeOfTheMonth() {
        var income = 0f
        filteredRecords.value?.filter { record ->
            record.type == RecordType.INCOME.value
        }.also {
            it?.forEach { record ->
                income += record.amount
            }
        }
        incomeOfTheMonth.value = income
    }

    fun getExpenseOfTheMonth() {
        var expense: Float = 0f
        filteredRecords.value?.filter { record ->
            record.type == RecordType.EXPENSE.value
        }.also {
            it?.forEach { record ->
                expense += record.amount
            }
        }
        expenseOfTheMonth.value = expense
    }

    fun getTotalBalanceOfTheMonth() {
        val balance = expenseOfTheMonth.value?.let { incomeOfTheMonth.value?.minus(it) } ?: 0f
        totalBalanceOfTheMonth.value = balance
    }

    private var user = 0

    fun fetchAllRecords(userId: Int) {
        viewModelScope.launch/*(Dispatchers.IO)*/ {
            var fetchedRecords: List<Record>? = null
            val job = launch {
                fetchedRecords = repository.getAllUserRecords(userId)
                Log.e("Record", userId.toString() + " viewmodel getusers below fetchedrecords ${fetchedRecords?.size}")
                Log.e("Record", _record.value.toString() + " inside job " +_record.value?.recordId.toString())
            }
            job.join()
            withContext(Dispatchers.Main) {
                Log.e("rec ", "")
                Log.e("Record", userId.toString() + " viewmodel getusers above ${_records.value?.size}")
                _records.value = fetchedRecords
                 user = userId
                Log.e("Record", userId.toString() + " viewmodel getusers below ${_records.value?.size}")
            }
           /* Log.e("Record", userId.toString() + " viewmodel getusers above ${_records.value?.size}")
            _records.value = repository.getAllUserRecords(userId)
            Log.e("Record", userId.toString() + " viewmodel getusers below ${_records.value?.size}")*/

        }
        Log.e("Record", _record.value.toString() + " fetchAll " +_record.value?.recordId.toString())
    }

    private var _dataForPieChart = MutableLiveData<List<Pair<Category, Float>>>()
    val dataForPieChart: LiveData<List<Pair<Category, Float>>>
        get() = _dataForPieChart
    val isDataForPieChartUpdated = MutableLiveData<Boolean>()

    fun transformedDataForPieChart(list: List<Record>, type: RecordType = RecordType.ALL) {
        val transformedList = mutableListOf<Pair<Category, Float>>()
        val categories = mutableListOf<Category>()
        list.map { it.category }.toSet().forEach {categoryName ->
            Categories.categoryList.forEach {category ->
                if(category.title == categoryName) {
                    categories.add(category)
                }
            }
        }
        categories.forEach {category ->
            var amount = 0f
            list.filter {record ->
                record.category == category.title
            }.forEach {
                amount+=it.amount
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
    private var _dataForIncomePieChart = MutableLiveData<List<Pair<Category, Float>>>()
    val dataForIncomePieChart: LiveData<List<Pair<Category, Float>>>
    get() = _dataForIncomePieChart
    private var _dataForExpensePieChart = MutableLiveData<List<Pair<Category, Float>>>()
    val dataForExpensePieChart: LiveData<List<Pair<Category, Float>>>
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
        _record.value = record
    }

    fun updateRecord(
        userId: Int,
        category: String,
        amount: Float,
        type: String,
        date: String,
        note: String,
        description: String
    ) {
        val record = record.value?.let {
            Record(it.userId, category, amount, type, date).apply {
                recordId = it.recordId
                this.note = note
                this.description = description
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            var updatedRecord: Record? = null
            val job = launch {
                _record.value?.let {
                    updatedRecord = Record(it.userId, category, amount, type, date).apply {
                        recordId = it.recordId
                        this.note = note
                        this.description = description
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
}