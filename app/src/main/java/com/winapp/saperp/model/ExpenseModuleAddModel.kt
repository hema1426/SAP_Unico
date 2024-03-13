package com.winapp.saperp.model


import com.google.gson.annotations.SerializedName

data class ExpenseModuleAddModel(
    var expenseName: String,
    var expenseAmt: Double,
    var supplierCode:String,
    var accountCode:String
    )
