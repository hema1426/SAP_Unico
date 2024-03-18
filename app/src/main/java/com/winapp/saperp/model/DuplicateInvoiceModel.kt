package com.winapp.saperp.model

data class DuplicateInvoiceModel(
    var address1: String,
    var address2: String,
    var address3: String,
    var address: String,
    var attachmentEntry: String,
    var balanceAmount: String,
    var billDiscount: String,
    var block: String,
    var city: String,
    var companyCode: String,
    var contactPersonCode: String,
    var countryName: String,
    var createDate: String,
    var currencyCode: String,
    var currencyName: String,
    var customerCode: String,
    var customerName: String,
    var discountPercentage: String,
    var doDate: String,
    var doNumber: String,
    var docEntry: String,
    var fDocTotal: String,
    var fTaxAmount: String,
    var fTotal: String,
    var gstNo: String,
    var iPaidAmount: String,
    var iTotalDiscount: String,
    var invoiceDate: String,
    var invoiceDetails: ArrayList<DuplicateInvoiceDetail>,
    var invoiceNumber: String,
    var invoiceStatus: String,
    var isGSTIncl: String,
    var netTotal: String,
    var overAllTotal: String,
    var paidAmount: String,
    var paymentTerm: String,
    var phoneNo: String,
    var receivedAmount: String,
    var remark: String,
    var sR_Details: ArrayList<DuplicateSRDetails>,
    var signFlag: String,
    var signature: String,
    var soDate: String,
    var soNumber: String,
    var state: String,
    var street: String,
    var subTotal: String,
    var taxCode: String,
    var taxPer: String,
    var taxPerc: String,
    var taxTotal: String,
    var taxType: String,
    var total: String,
    var totalDiscount: String,
    var totalOutstandingAmount: String,
    var updateDate: String,
    var zipcode: String
) {
    constructor() : this(
        "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "","",
        "",
        ArrayList(), "", "", "", "", "", "", "",
        "", "", "",ArrayList(), "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "" , ""
    )
}