package com.tops.learnnew.Model

data class RateResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
