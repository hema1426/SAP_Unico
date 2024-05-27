package com.winapp.saperp.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import com.winapp.saperp.R
import com.winapp.saperp.adapter.ExpenseModuleAddAdapter
import com.winapp.saperp.model.AccountModel
import com.winapp.saperp.model.ExpenseModuleAddModel
import com.winapp.saperp.model.ExpensePurchaseInvoiceDetail
import com.winapp.saperp.model.ExpenseSaveRequestMaster
import com.winapp.saperp.model.SupplierModel
import com.winapp.saperp.utils.CommonMethodKotl
import com.winapp.saperp.utils.CommonMethodKotl.showError_dialog
import com.winapp.saperp.utils.Constants
import com.winapp.saperp.utils.SessionManager
import com.winapp.saperp.utils.Utils
import com.winapp.saperp.utils.Utils.showKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewExpenseModuleAddActivity : AppCompatActivity() , ExpenseModuleAddAdapter.EditClickListener ,
 ExpenseModuleAddAdapter.DeleteClickListener{

    private var companyCode: String? = null
    private var username: String? = null
    private var user: HashMap<String, String>? = null
    private var session: SessionManager? = null
    private val pDialog: SweetAlertDialog? = null
    private var locationCode: String? = null
    var supplierList: ArrayList<SupplierModel>? = ArrayList()
    var accountList: ArrayList<AccountModel>? = ArrayList()
    var expenseSaveReq: ArrayList<ExpenseSaveRequestMaster>? = ArrayList()
    var expenseCartAddList: ArrayList<ExpenseModuleAddModel> = ArrayList()
    var expenseSaveProductReq: ArrayList<ExpensePurchaseInvoiceDetail> = ArrayList()
    private var supplierSpinner: SearchableSpinner? = null
    private var accountSpinner: Spinner? = null
    private var dateTxt: TextView? = null
    private var serviceNameTxtl: EditText? = null
    private var service_amount_Txt: EditText? = null
    var currentDate: String? = null
    private var mYear = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var printerMacId: String? = null
    private var printerType: String? = null
    private var sharedPreferences: SharedPreferences? = null
    private var itemSize: TextView? = null
    private var expense_rv: RecyclerView? = null
    private var selectSupplierName: String? = ""
    private var selectSuppliercode: String? = ""
    private var selectAccountName: String? = ""
    private var selectAccountcode: String? = ""
    private var add_pdt_btnl: Button? = null
    var save_btn: MenuItem? = null
    var isback = false

    var expenseAddlist: ArrayList<ExpenseModuleAddModel> = ArrayList()
    var expenseAddAdapter: ExpenseModuleAddAdapter? = null
    private var empty_product_text: TextView? = null
    private var allTotaltxt: TextView? = null
    private var expenseLay: LinearLayout? = null
    var expens_name = ""
    var currencyCode = ""
    var taxPercentage = ""
    var taxType = ""
    var taxCode = ""
    var selectedServices = ""
    private var isEdit: Boolean = false

    private var cancelButton: Button? = null
    private var okButton: Button? = null
    var amountText: EditText? = null
    private var alert: AlertDialog? = null
    private val signatureAlert: AlertDialog? = null
    var invoicePrintCheck: CheckBox? = null
    var saveTitle: TextView? = null
    private var saveMessage: TextView? = null
    var signatureCapture: ImageView? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense_module_add)

        supportActionBar!!.title = "Expense"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        session = SessionManager(this)
        user = session!!.userDetails
        companyCode = user!!.get(SessionManager.KEY_COMPANY_CODE)
        username = user!!.get(SessionManager.KEY_USER_NAME)
        locationCode = user!!.get(SessionManager.KEY_LOCATION_CODE)

        supplierSpinner = findViewById(R.id.supplier_spinner_add) as SearchableSpinner
        accountSpinner = findViewById(R.id.account_spinner_add) as Spinner
        dateTxt = findViewById(R.id.invoice_date_add)
        serviceNameTxtl = findViewById(R.id.serviceNameTxt)
        service_amount_Txt = findViewById(R.id.amount_add)
        itemSize = findViewById(R.id.itemSize_add)
        add_pdt_btnl = findViewById(R.id.add_pdt_btn)
        expense_rv = findViewById(R.id.expense_list_rv)
        empty_product_text = findViewById(R.id.no_product_text)
        allTotaltxt = findViewById(R.id.allTotalExpense)
        expenseLay = findViewById(R.id.expenseAdd_lay)

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE)
        printerType = sharedPreferences!!.getString("printer_type", "")
        printerMacId = sharedPreferences!!.getString("mac_address", "")
        expenseAddlist = ArrayList()

        val c = Calendar.getInstance().time
        println("Current time => $c")
        val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        currentDate = df1.format(c)

        val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = df.format(c)
        dateTxt!!.setText(formattedDate)

        dateTxt!!.setOnClickListener(View.OnClickListener {
            getDate(dateTxt!!)
        })

        getAccountList()
        supplierSpinner!!.isEnabled = true
        accountSpinner!!.isEnabled = true

        add_pdt_btnl!!.setOnClickListener {
            if (selectSuppliercode!!.isNotEmpty() && !selectAccountcode!!.equals("0")) {

                if (isEdit) {
                    existPdtadd(selectedServices)
                } else {
                    addDetail()
                }
            }
            else {
                Toast.makeText(applicationContext, "Select supplier and account!", Toast.LENGTH_SHORT).show()
            }
        }

        serviceNameTxtl!!.isEnabled = false
        service_amount_Txt!!.isEnabled = false
        serviceNameTxtl!!.clearFocus()
        service_amount_Txt!!.clearFocus()
        empty_product_text!!.visibility = View.VISIBLE

        //    setexpenseAdapter(expenseAddlist)

        serviceNameTxtl!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    btnView()
                } else {
                    add_pdt_btnl!!.setAlpha(0.4f)
                    add_pdt_btnl!!.setEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(query: CharSequence, start: Int, before: Int, count: Int) {}
        })

        service_amount_Txt!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    btnView()
                } else {
                    add_pdt_btnl!!.setAlpha(0.4f)
                    add_pdt_btnl!!.setEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(query: CharSequence, start: Int, before: Int, count: Int) {}
        })

    }

    fun createJson() {
        if (expenseAddAdapter!!.getList().size > 0) {
            var expenseSaveReqModel = ExpenseSaveRequestMaster()

            expenseCartAddList = expenseAddAdapter!!.getList()
            Log.w("exp_Model:", ""+expenseCartAddList)

            if (expenseCartAddList.size > 0) {

                for (i in expenseCartAddList.indices) {
                    var expenseSaveProductModel = ExpensePurchaseInvoiceDetail()

                    expenseSaveProductModel.productName = expenseCartAddList[i].expenseName
                    expenseSaveProductModel.price = expenseCartAddList[i].expenseAmt.toString()
                    expenseSaveProductModel.createUser = username!!
                    expenseSaveProductModel.taxType = taxType
                    expenseSaveProductModel.taxPerc = taxPercentage
                    expenseSaveProductModel.netTotal = allTotaltxt!!.text.toString().toDouble()
                    expenseSaveProductModel.AccountCode = expenseCartAddList[i].accountCode.toString()!!

                    Log.w("exp_Modelval:", ""+expenseCartAddList[i].accountCode)
                    expenseSaveProductReq.add(expenseSaveProductModel)
                }

            }

            expenseSaveReqModel.InvType = "Cash"
            expenseSaveReqModel.POStatus = "O"
            expenseSaveReqModel.status = "O"
            expenseSaveReqModel.Mode = ""
            expenseSaveReqModel.poDate = currentDate
            expenseSaveReqModel.VendorCode = selectSuppliercode
            expenseSaveReqModel.VendorName = selectSupplierName
            expenseSaveReqModel.companyCode = companyCode
            expenseSaveReqModel.locationCode = locationCode
            expenseSaveReqModel.createUser = username
            expenseSaveReqModel.taxType = taxType
            expenseSaveReqModel.netTotal = allTotaltxt!!.text.toString()
            expenseSaveReqModel.total = allTotaltxt!!.text.toString()
            expenseSaveReqModel.taxPerc = taxPercentage
            expenseSaveReqModel.currencyCode = currencyCode
            expenseSaveReqModel.PurchaseInvoiceDetails = expenseSaveProductReq

            expenseSaveReq!!.add(expenseSaveReqModel)

            saveExpense(expenseSaveReqModel)
            Log.w("expens_save_Model:", "" + expenseSaveReqModel)

        } else {
            Toast.makeText(this, "Add Service Detail", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveExpense(jsonObj: ExpenseSaveRequestMaster) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "PostingAPInvoiceWithPayment"

        Log.w("expens_save_URL:", "" + url)
        Log.w("json_objj", ".." + jsonObj)
        Log.w("gsonsavvv", ".." + Gson().toJson(jsonObj).toString())

        CommonMethodKotl.showProgressDialog(this)

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, null,
            Response.Listener { response: JSONObject ->

                Log.e("EXPENsave_res:", response.toString())

                try {
//                    GlobalScope.launch {
//                        withContext(Dispatchers.Main) {
//
//                            Log.e("GRAsave_res:", response.toString())
                    val statusCode = response.optString("statusCode")
                    val statusMsg = response.optString("statusMessage")
//
                    if (statusCode == "1") {

                        val responseData = response.optJSONObject("responseData")!!
                        Log.e("expenssavraa", "")
                        if (responseData.length() > 0) {

                            Toast.makeText(this, statusMsg, Toast.LENGTH_SHORT).show()
                            finish()

                            startActivity(
                                Intent(this, NewExpenseModuleListActivity::class.java)
                            )
                        }
                    } else {
                        val responseData = response.optJSONObject("responseData")!!
                        if (responseData.length() > 0) {
                            val errorMsg = responseData.optString("error")

                            showError_dialog(this, errorMsg)
                        }

                    }
                    CommonMethodKotl.cancelProgressDialog()
                } catch (e: Exception) {
                    Log.e("Error_throwing11:", e.localizedMessage.toString())
                }

            },
            Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                Log.e("Error_throwing:", error.toString())

                CommonMethodKotl.cancelProgressDialog()

            }) {
            override fun getBody(): ByteArray {
                return Gson().toJson(jsonObj).toString().toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                val creds =
                    String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD)
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                params["Authorization"] = auth
                return params
            }
        }
        jsonObjectRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }
    fun showAlertDialogForSave() {
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("Information...!")

        builder1.setMessage("Are you sure want to save ?")
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "Yes"
        ) { dialog, id ->
            Log.w("expen_sav","")
            createJson()
        }
        builder1.setNegativeButton(
            "No"
        ) { dialog, id -> dialog.cancel() }
        val alert11 = builder1.create()
        alert11.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDate(dateEditext: TextView) {
        // Get Current Date
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                dateEditext.text = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                currentDate = convertDate(dateEditext.text.toString())
              //  Log.w("CurrentDateView:", CreateNewInvoiceActivity.currentDate)
            }, mYear, mMonth, mDay
        )
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun convertDate(strDate: String): String? {
        @SuppressLint("SimpleDateFormat") val inputFormat: DateFormat =
            SimpleDateFormat("dd-MM-yyyy")
        @SuppressLint("SimpleDateFormat") val outputFormat: DateFormat =
            SimpleDateFormat("yyyyMMdd")
        var resultDate: String? = ""
        try {
            resultDate = outputFormat.format(inputFormat.parse(strDate))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return resultDate
    }

    @Throws(JSONException::class)
    private fun getAccountList() {
        CommonMethodKotl.showProgressDialog(this)

        val jsonObject = JSONObject()
        //  jsonObject.put("WhsCode", whsCode)

        val requestQueue = Volley.newRequestQueue(this)

        val url = Utils.getBaseUrl(this) + "AccountCodeList"
        Log.w("url_accountlist:", url)

        accountList = ArrayList()

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response: JSONObject ->
                try {
                    GlobalScope.launch {

                        Log.w("url_accountlist_res:", response.toString())
                        val statusCode = response.optString("statusCode")
                        val statusMsg = response.optString("statusMessage")

                        if (statusCode == "1") {
                            val responseData = response.optJSONArray("responseData")!!

                            if (responseData!!.length() > 0) {
                                for (i in 0 until responseData.length()) {
                                    val obj = responseData.optJSONObject(i)

                                    val model = AccountModel(
                                        obj.optString("accountCode"),
                                        obj.optString("accountName")
                                    )

                                    accountList!!.add(model)
                                }

                                withContext(Dispatchers.Main) {
                                    if (accountList!!.size > 0) {
                                        setAccountSpinner(accountList!!)
                                    }
                                }
                            }
                        }
                        CommonMethodKotl.cancelProgressDialog()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                getVendorList()

            }, Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                CommonMethodKotl.cancelProgressDialog()

                Log.w("Error_throwing:", error.toString())
            }) {
            override fun getHeaders(): Map<String, String> {
                val params = java.util.HashMap<String, String>()
                val creds = java.lang.String.format(
                    "%s:%s",
                    Constants.API_SECRET_CODE,
                    Constants.API_SECRET_PASSWORD
                )
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                params["Authorization"] = auth
                return params
            }
        }
        jsonObjectRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    @Throws(JSONException::class)
    private fun getVendorList() {
        // CommonMethods.showProgressDialog(this)
        val jsonObject = JSONObject()
        //  jsonObject.put("WhsCode", whsCode)

        val requestQueue = Volley.newRequestQueue(this)

        val url = Utils.getBaseUrl(this) + "vendorList"
        Log.w("url_vendorlist:", url)

        supplierList = ArrayList()

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response: JSONObject ->
                try {
                    GlobalScope.launch {

                        Log.w("url_vendorlist_res:", response.toString())
                        val statusCode = response.optString("statusCode")
                        val statusMsg = response.optString("statusMessage")

                        if (statusCode == "1") {
                            val responseData = response.optJSONArray("responseData")!!

                            if (responseData!!.length() > 0) {
                                for (i in 0 until responseData.length()) {
                                    val obj = responseData.optJSONObject(i)

                                    val model = SupplierModel(
                                        obj.optString("vendorCode"),
                                        obj.optString("vendorName"),
                                        obj.optString("currencyCode"),
                                        obj.optString("currencyName"),
                                        obj.optString("taxType"),
                                        obj.optString("taxCode"),
                                        obj.optString("taxName"),
                                        obj.optString("taxPercentage")
                                    )

                                    supplierList!!.add(model)
                                }

                                withContext(Dispatchers.Main) {
                                    if (supplierList!!.size > 0) {
                                        setSupplierSpinner(supplierList!!)
                                    }
                                }
                            }
                        }
                        //  CommonMethods.cancelProgressDialog()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                //  CommonMethods.cancelProgressDialog()
                Log.w("Error_throwing:", error.toString())
            }) {
            override fun getHeaders(): Map<String, String> {
                val params = java.util.HashMap<String, String>()
                val creds = java.lang.String.format(
                    "%s:%s",
                    Constants.API_SECRET_CODE,
                    Constants.API_SECRET_PASSWORD
                )
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                params["Authorization"] = auth
                return params
            }
        }
        jsonObjectRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    fun setAccountSpinner(accountlist: ArrayList<AccountModel>) {
        val spinnerlst = AccountModel("0","Select Account"
        )
        accountlist.add(0, spinnerlst)
        Log.w("spinnAcc", "" + accountlist)
        val adapter = ArrayAdapter<String>(this, R.layout.cust_spinner_item)
        for (i in accountlist.indices) {
            adapter.add(accountlist[i].accountName)
        }
        accountSpinner!!.adapter = adapter
        accountSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>, v: View, position: Int, id: Long) {
                // On selecting a spinner item
                Log.w("spinnnAcc1", "" + accountlist[position].accountName)
                selectAccountName = accountlist[position].accountName
                selectAccountcode = accountlist[position].accountCode

                if (!selectAccountName!!.equals("Select Account")) {
                    showKeyboard(this@NewExpenseModuleAddActivity, serviceNameTxtl)
                    serviceNameTxtl!!.requestFocus()
                    serviceNameTxtl!!.isEnabled = true
                    service_amount_Txt!!.isEnabled = true
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
    }

    fun setSupplierSpinner(spinnerlist: ArrayList<SupplierModel>) {
        val spinnerlst = SupplierModel(
            customerName = "Select Supplier",
            customerCode = "", currencyCode = "", currencyName = "",
            taxCode = "", taxName = "", taxPercentage = "", taxType = ""
        )
        spinnerlist.add(0, spinnerlst)
        Log.w("spinnnVal", "" + spinnerlist)
        val adapter = ArrayAdapter<String>(this, R.layout.cust_spinner_item)
        for (i in spinnerlist.indices) {
            adapter.add(spinnerlist[i].customerName)
        }
        supplierSpinner!!.adapter = adapter
        supplierSpinner!!.setTitle("")
        supplierSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>, v: View, position: Int, id: Long) {
                // On selecting a spinner item
                selectSuppliercode = spinnerlist[position].customerCode
                selectSupplierName = spinnerlist[position].customerName
                currencyCode = spinnerlist[position].currencyCode
                taxCode = spinnerlist[position].taxCode
                taxPercentage = spinnerlist[position].taxPercentage
                taxType = spinnerlist[position].taxType
                Log.w("spinnnVal1", "" + spinnerlist[position].taxType)

                if (!selectSupplierName!!.equals("Select Supplier")) {
                    serviceNameTxtl!!.isEnabled = true
                    service_amount_Txt!!.isEnabled = true
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
    }

    fun addDetail() {
        var isSearched = false
        var currentIndex = 0
        if (serviceNameTxtl!!.text.toString().isNotEmpty() && service_amount_Txt!!.text.toString()
                .isNotEmpty()
        ) {
            if (selectAccountName!!.isNotEmpty() && selectSupplierName!!.isNotEmpty()) {

                if (isProductAlreadyExist(serviceNameTxtl!!.text.toString())) {
                    showExistingProductAlert(serviceNameTxtl!!.text.toString())
                } else {

                    val expenseModel = ExpenseModuleAddModel(
                        serviceNameTxtl!!.text.toString(),
                        service_amount_Txt!!.text.toString().toDouble(),
                        selectSuppliercode!!,
                        selectAccountcode!!
                    )

                    expenseAddlist!!.add(expenseModel)

                    itemSize!!.setText(expenseAddlist.size.toString() + " Services" )

                    totalAll()

                    if (expenseAddAdapter != null) {

                        expenseAddAdapter!!.updatePdt(expenseModel, true)
                        isSearched = true
                        expenseAddAdapter!!.updateList(expenseAddlist!!)

                        supplierSpinner!!.isEnabled = false
                     //   accountSpinner!!.isEnabled = false

                        clearTxt()

                        currentIndex = expenseAddlist!!.size - 1
                    } else {
                        setexpenseAdapter(expenseAddlist!!)
                        totalAll()
                        if (expenseAddAdapter != null) {
                            expenseAddAdapter!!.updatePdt(expenseModel, true)
                        }
                        clearTxt()

                    }

                    if (isSearched) {
                        expense_rv!!.scrollToPosition(currentIndex)
                    }

//                    expenseAddlist.add(
//                        ExpenseModuleAddModel(
//                            serviceNameTxtl!!.text.toString(), amount_addTxt!!.text.toString(),
//                            selectSuppliercode!!, selectAccountcode!!
//                        )
//                    )
//
//                    expenseAddAdapter!!.updateList(expenseAddlist)
//                    supplierSpinner!!.isEnabled = false
//                    accountSpinner!!.isEnabled = false
////                supplierSpinner!!.setAlpha(0.9f)
////                accountSpinner!!.setAlpha(0.9f)
//                    clearTxt()
                }
            } else {
                Toast.makeText(this, "Select supplier & account detail", Toast.LENGTH_SHORT).show()
            }

            //  clearTxt()
        } else {
            Toast.makeText(this, "Enter service name & amount", Toast.LENGTH_SHORT).show()
        }
    }

    fun existPdtadd(expenseNameStr: String) {
        var isSearched = false
        var currentIndex = 0
        if (isProductAlreadyExist(expenseNameStr)) {
//            var addlist = addAdapter!!.getCartList().filter { it.productCode == product_code }
            for ((index, model) in expenseAddlist!!.withIndex()) {
                if (model.expenseName == expenseNameStr) {

                    val expenseModel = ExpenseModuleAddModel(
                        serviceNameTxtl!!.text.toString(),
                        service_amount_Txt!!.text.toString().toDouble(),
                        selectSuppliercode!!,
                        selectAccountcode!!
                    )

                    Log.w("ggExpens", "..")
                    expenseAddlist!!.set(index, expenseModel)
                    itemSize!!.setText(expenseAddlist.size.toString() +" Services")
                    totalAll()
                    if (expenseAddAdapter != null) {
                        expenseAddAdapter!!.updatePdt(expenseModel, true)

                        Toast.makeText(this, "Product Updated..!", Toast.LENGTH_SHORT).show()
                        clearTxt()
                        btnView()
                        isSearched = true
                        isEdit = false
                        currentIndex = index
                        add_pdt_btnl!!.setText("Add")
                    }
                }
            }
            if (expenseAddAdapter != null) {
                expenseAddAdapter!!.updateList(expenseAddlist!!)
            }
            if (isSearched) {
                expense_rv!!.scrollToPosition(currentIndex)
            }
        }
    }

    fun totalAll() {
        if (expenseAddlist!!.size > 0) {
            val totalVal = expenseAddlist!!.sumOf {
                it.expenseAmt!!
            }
            allTotaltxt!!.setText(Utils.twoDecimalPoint(totalVal.toString().toDouble()))
        }
        else {
            allTotaltxt!!.setText("")
        }
    }

    fun btnView() {
            if (serviceNameTxtl!!.getText().toString()
                    .isNotEmpty() && service_amount_Txt!!.text.toString().length > 0.0
            ) {
                add_pdt_btnl!!.setAlpha(0.9f)
                add_pdt_btnl!!.setEnabled(true)
                Log.w("entryy11", "");
            } else {
                add_pdt_btnl!!.setAlpha(0.4f)
                add_pdt_btnl!!.setEnabled(false)
                Log.w("entryy22", "");
//        Toast.makeText(applicationContext, "Price is Empty..!", Toast.LENGTH_SHORT).show()
            }
    }

    fun clearTxt() {
        supplierSpinner!!.isEnabled = false
      //  accountSpinner!!.isEnabled = false
        serviceNameTxtl!!.isEnabled = true
//        selectAccountName= ""
//        selectAccountcode = ""
//        selectSuppliercode = ""
//        selectSupplierName = ""
        serviceNameTxtl!!.setText("")
        service_amount_Txt!!.setText("")
        serviceNameTxtl!!.clearFocus()
        service_amount_Txt!!.clearFocus()

        Utils.hideKeyboard(this, expenseLay)

    }

    fun isProductAlreadyExist(expenseNameStr: String): Boolean {
        var isCheck = false
        if (expenseAddAdapter != null) {
            for (model in expenseAddAdapter!!.getList()) {
                if (model.expenseName == expenseNameStr) {
                    isCheck = true
                    break
                }
            }
        }
        return isCheck
    }

    fun showExistingProductAlert(productName: String) {
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("Warning !")
        builder1.setMessage("$productName - Already Exist Do you want to replace ? ")
        builder1.setCancelable(false)
        builder1.setPositiveButton("YES") { dialog, id ->
            dialog.cancel()
            existPdtadd(serviceNameTxtl!!.text.toString())
        }
        builder1.setNegativeButton(
            "NO"
        ) { dialog, id ->
            dialog.cancel()
            clearTxt()
        }
        val alert11 = builder1.create()
        alert11.show()
    }

    private fun setexpenseAdapter(arrayList: ArrayList<ExpenseModuleAddModel>) {
        empty_product_text!!.visibility = View.GONE
        expense_rv!!.visibility = View.VISIBLE
        itemSize!!.setText(arrayList.size.toString() +" Services")

        expenseAddAdapter = ExpenseModuleAddAdapter(
            this, arrayList ,this ,this
        )
        expense_rv!!.setHasFixedSize(true)
        expense_rv!!.layoutManager = LinearLayoutManager(this)
        expense_rv!!.adapter = expenseAddAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (expenseAddlist.size > 0) {
                    isback = true
                    backAlertdialog()
                }
                    true
            }

            R.id.action_save -> {
                if(expenseAddAdapter != null ){
                    if (allTotaltxt!!.text.toString().toDouble() > 0) {
                        save_btn!!.setEnabled(true)
                        showAlertDialogForSave()
                    }
                    else{
                        Toast.makeText(
                            applicationContext,
                            " Nettotal should be great than zero",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else{
                    Toast.makeText(this, "add product!", Toast.LENGTH_SHORT).show()
                }
//                val localCart: java.util.ArrayList<CreateInvoiceModel> =
//                    dbHelper.getAllInvoiceProducts()
//                if (localCart.size > 0) {
//                    if (activityFrom == "iv" || activityFrom == "ConvertInvoice") {
//                        if (createInvoiceValidation()) {
//                            showSaveAlert()
//                        } else {
//                            showAlertForCreditLimit()
//                        }
//                    } else {
//                        showSaveAlert()
//                    }
//                } else {
//                    Toast.makeText(
//                        applicationContext,
//                        "Add the Product First...!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
        //  return super.onOptionsItemSelected(item);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.save_menu, menu)
        save_btn = menu.findItem(R.id.action_save)
        return true
    }

    override fun editSelected(item: ExpenseModuleAddModel) {
        serviceNameTxtl!!.setText(item.expenseName)
        service_amount_Txt!!.setText(item.expenseAmt.toString())
        service_amount_Txt!!.setSelection(service_amount_Txt!!.text.length)
        serviceNameTxtl!!.isEnabled = false
        service_amount_Txt!!.requestFocus()
        showKeyboard(this,service_amount_Txt)

        add_pdt_btnl!!.setText("Update")
        selectedServices = item.expenseName
        isEdit = true
    }
    override fun deleteSelected(position: Int) {
        if (add_pdt_btnl!!.text.contains("Update")) {
            Toast.makeText(this, "Update previous product", Toast.LENGTH_SHORT).show()
        } else {
            showdialog_delete(position)
        }
    }
    private fun showdialog_delete(position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete")
        alertDialogBuilder.setMessage("Are you sure you want to Delete?")
        alertDialogBuilder.setPositiveButton(
            "Yes"
        ) { dialog, which ->

                expenseAddlist.removeAt(position)
                expenseAddAdapter!!.notifyDataSetChanged()
                itemSize!!.setText(expenseAddlist.size.toString() +" Services")
                totalAll()
                clearTxt()
                //  btnView()

                add_pdt_btnl!!.setText("Add")
                add_pdt_btnl!!.setAlpha(0.4f)
                add_pdt_btnl!!.isEnabled = true

            if(expenseAddlist.size > 0 ){
                supplierSpinner!!.isEnabled = false
                //accountSpinner!!.isEnabled = false
            }
            else{
                supplierSpinner!!.isEnabled = true
              //  accountSpinner!!.isEnabled = true
            }

            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton(
            "No"
        ) { dialog, which -> dialog.dismiss() }
        alertDialogBuilder.show()
    }

//    fun showSaveAlert() {
//        try {
//            // create an alert builder
//            val builder = AlertDialog.Builder(this@NewExpenseModuleAddActivity)
//            // set the custom layout
//            builder.setCancelable(false)
//            val customLayout = layoutInflater.inflate(R.layout.invoice_save_option, null)
//            builder.setView(customLayout)
//            // add a button
//            okButton = customLayout.findViewById<Button>(R.id.btn_ok)
//            cancelButton = customLayout.findViewById<Button>(R.id.btn_cancel)
//            invoicePrintCheck = customLayout.findViewById<CheckBox>(R.id.invoice_print_check)
//            saveMessage = customLayout.findViewById<TextView>(R.id.save_message)
//            saveTitle = customLayout.findViewById<TextView>(R.id.save_title)
//            signatureCapture = customLayout.findViewById<ImageView>(R.id.signature_capture)
//            val noOfCopy = customLayout.findViewById<TextView>(R.id.no_of_copy)
//            val copyPlus = customLayout.findViewById<Button>(R.id.increase)
//            val copyMinus = customLayout.findViewById<Button>(R.id.decrease)
//            val signatureButton = customLayout.findViewById<Button>(R.id.btn_signature)
//            val copyLayout = customLayout.findViewById<LinearLayout>(R.id.print_layout)
//            //invoicePrintCheck.setVisibility(View.GONE);
//            if (activityFrom == "so" || activityFrom == "SalesEdit") {
//                saveTitle.setText("Save SalesOrder")
//                saveMessage.setText("Are you sure want to save SalesOrder?")
//                invoicePrintCheck.setText("SalesOrder Print")
//                if (isCheckedSalesPrint) {
//                    invoicePrintCheck.setChecked(true)
//                    CreateNewInvoiceActivity.isPrintEnable = true
//                    copyLayout.visibility = View.VISIBLE
//                } else {
//                    invoicePrintCheck.setChecked(false)
//                    CreateNewInvoiceActivity.isPrintEnable = false
//                    copyLayout.visibility = View.GONE
//                }
//            } else if (activityFrom == "do" || activityFrom == "doEdit") {
//                saveTitle.setText("Save Delivery Order")
//                saveMessage.setText("Are you sure want to save Delivery Order?")
//                invoicePrintCheck.setText("Delivery Order Print")
//                invoicePrintCheck.setVisibility(View.GONE)
//                if (isCheckedSalesPrint) {
//                    invoicePrintCheck.setChecked(true)
//                    CreateNewInvoiceActivity.isPrintEnable = true
//                    copyLayout.visibility = View.VISIBLE
//                } else {
//                    invoicePrintCheck.setChecked(false)
//                    CreateNewInvoiceActivity.isPrintEnable = false
//                    copyLayout.visibility = View.GONE
//                }
//            } else {
//                if (isCheckedInvoicePrint) {
//                    invoicePrintCheck.setChecked(true)
//                    CreateNewInvoiceActivity.isPrintEnable = true
//                    copyLayout.visibility = View.VISIBLE
//                } else {
//                    invoicePrintCheck.setChecked(false)
//                    CreateNewInvoiceActivity.isPrintEnable = false
//                    copyLayout.visibility = View.GONE
//                }
//            }
//            signatureButton.setOnClickListener { showSignatureAlert() }
//            invoicePrintCheck.setOnClickListener(View.OnClickListener {
//                if (invoicePrintCheck.isChecked()) {
//                    CreateNewInvoiceActivity.isPrintEnable = true
//                    copyLayout.visibility = View.VISIBLE
//                } else {
//                    CreateNewInvoiceActivity.isPrintEnable = false
//                    copyLayout.visibility = View.GONE
//                }
//            })
//            copyPlus.setOnClickListener {
//                val copyvalue = noOfCopy.text.toString()
//                var copy = copyvalue.toInt()
//                copy++
//                noOfCopy.text = copy.toString() + ""
//            }
//            copyMinus.setOnClickListener {
//                if (noOfCopy.text.toString() != "1") {
//                    val copyvalue = noOfCopy.text.toString()
//                    var copy = copyvalue.toInt()
//                    copy--
//                    noOfCopy.text = copy.toString() + ""
//                }
//            }
//
//            // create and show the alert dialog
//            alert = builder.create()
//            alert!!.show()
//            okButton!!.setOnClickListener(View.OnClickListener { view1: View? ->
//                try {
//                    if (companyCode == "AADHI INTERNATIONAL PTE LTD") {
//                        if (activityFrom == "iv" || activityFrom == "ConvertInvoice") {
//                            if (CreateNewInvoiceActivity.signatureString != null && !CreateNewInvoiceActivity.signatureString.isEmpty()) {
//                                alert.dismiss()
//                                if (activityFrom == "iv" || activityFrom == "ConvertInvoice") {
//                                    createInvoiceJson(noOfCopy.text.toString().toInt())
//                                } else if (activityFrom == "so" || activityFrom == "SalesEdit") {
//                                    createSalesOrderJson(noOfCopy.text.toString().toInt())
//                                } else if (activityFrom == "do" || activityFrom == "doEdit") {
//                                    createDOJson(noOfCopy.text.toString().toInt())
//                                }
//                            } else {
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Add Signature to Save Invoice",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        } else {
//                            alert.dismiss()
//                            if (activityFrom == "iv" || activityFrom == "ConvertInvoice") {
//                                createInvoiceJson(noOfCopy.text.toString().toInt())
//                            } else if (activityFrom == "so" || activityFrom == "SalesEdit") {
//                                createSalesOrderJson(noOfCopy.text.toString().toInt())
//                            } else if (activityFrom == "do" || activityFrom == "doEdit") {
//                                createDOJson(noOfCopy.text.toString().toInt())
//                            }
//                        }
//                    } else {
//                        alert.dismiss()
//                        if (activityFrom == "iv" || activityFrom == "ConvertInvoice") {
//                            createInvoiceJson(noOfCopy.text.toString().toInt())
//                        } else if (activityFrom == "so" || activityFrom == "SalesEdit") {
//                            createSalesOrderJson(noOfCopy.text.toString().toInt())
//                        } else if (activityFrom == "do" || activityFrom == "doEdit") {
//                            createDOJson(noOfCopy.text.toString().toInt())
//                        }
//                    }
//                } catch (exception: java.lang.Exception) {
//                }
//            })
//            cancelButton.setOnClickListener(View.OnClickListener {
//                save_btn!!.setEnabled(true)
//                alert.cancel()
//            })
//        } catch (exception: java.lang.Exception) {
//        }
//    }

    override fun onBackPressed() {
        if (expenseAddlist.size > 0) {
            isback = true
            backAlertdialog()
        } else {
            this@NewExpenseModuleAddActivity.onSuperBackPressed()
        }
    }

    fun backAlertdialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning..!")
        builder.setMessage("All Products Will be cleared ,Are you sure want to back?")
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, id ->
//
//                if(expenseAddAdapter != null && expenseAddlist.size > 0){
//                    createJson()
//                }
//                else{
//                    Toast.makeText(this, "add product!", Toast.LENGTH_SHORT).show()
//                }
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("No") { dialog, id ->
//                if (isback) {
//                    this@NewExpenseModuleAddActivity.onSuperBackPressed()
//                } else {
//                    dialog.dismiss()
//                }
                dialog.dismiss()

            }
        val alert = builder.create()
        alert.show()
    }
    fun onSuperBackPressed() {
        super.onBackPressed()
    }

}