package com.winapp.saperpUNICO.model


import com.google.gson.annotations.SerializedName

data class AccountModel(
    @SerializedName("accountCode")
    var accountCode: String,
    @SerializedName("accountName")
    var accountName: String
)
