package com.example.spendwise.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwise.domain.Quote
import com.example.spendwise.interfaces.QuoteApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuoteViewModel : ViewModel() {

    private val _quote = MutableLiveData<Quote?>()
    val quote: LiveData<Quote?> = _quote.also {
        Log.e("Quote", it.value.toString())
    }

    fun getRandomQuote() {
        viewModelScope.launch {
            try {
                val response = Retrofit.Builder()
//                    .baseUrl("https://type.fit/api/")
                    .baseUrl("https://raw.githubusercontent.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(QuoteApiService::class.java)
                    .getQuote()
                Log.e("Quote", response.toString())
                if (response.isSuccessful) {
                    Log.e("Quote", "inside if")
                    val quotes = response.body() ?: emptyList()
                    if (quotes.isNotEmpty()) {
                        val randomQuote = quotes.random()
                        _quote.value = randomQuote
                    } else {
                        _quote.value = null//"No expense-related quotes available"
                    }
                } else {
                    _quote.value = null//"Error occurred while fetching quote"
                }
            } catch (e: Exception) {
                Log.e("Quote", e.toString())
                _quote.value = null//"Error occurred while fetching quote"
            }
        }
    }
}

