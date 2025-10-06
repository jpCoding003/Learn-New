package com.tops.learnnew


import com.tops.learnnew.Model.RateResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {
    @GET("latest")
    fun getExchangeRate(
        @Query("from") from: String,
        @Query("to") to: String
    ): Call<RateResponse>
}
