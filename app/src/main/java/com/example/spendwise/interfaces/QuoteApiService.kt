package com.example.spendwise.interfaces

import com.example.spendwise.domain.Quote
import retrofit2.Response
import retrofit2.http.GET

interface QuoteApiService {
//    @GET("quotes")
    @GET("Sriponbala/Quotes/main/QuotesForExpenseManagement.json")
    suspend fun getQuote(): Response<List<Quote>>

}
