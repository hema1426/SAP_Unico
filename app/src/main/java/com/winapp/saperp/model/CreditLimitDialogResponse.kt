package com.winapp.saperp.model

data class CreditLimitDialogResponse(
    val balance: String,
    val creditLine: String,
    val customerCode: String,
    val customerName: String
)