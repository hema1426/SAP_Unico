package com.winapp.saperpUNICO.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.winapp.saperpUNICO.R
import com.winapp.saperpUNICO.adapter.PurchaseInvoiceAdapterNew
import com.winapp.saperpUNICO.db.DBHelper
import com.winapp.saperpUNICO.fragments.CustomerFragment
import com.winapp.saperpUNICO.model.AppUtils
import com.winapp.saperpUNICO.model.CustomerDetails
import com.winapp.saperpUNICO.model.SalesOrderModel
import com.winapp.saperpUNICO.model.SalesOrderPrintPreviewModel
import com.winapp.saperpUNICO.model.SalesOrderPrintPreviewModel.SalesList
import com.winapp.saperpUNICO.model.SupplierModel
import com.winapp.saperpUNICO.model.UserListModel
import com.winapp.saperpUNICO.utils.BarCodeScanner
import com.winapp.saperpUNICO.utils.Constants
import com.winapp.saperpUNICO.utils.ImageUtil
import com.winapp.saperpUNICO.utils.SessionManager
import com.winapp.saperpUNICO.utils.SharedPreferenceUtil
import com.winapp.saperpUNICO.utils.UserAdapter
import com.winapp.saperpUNICO.utils.Utils
import com.winapp.saperpUNICO.zebraprinter.TSCPrinter
import com.winapp.saperpUNICO.zebraprinter.ZebraPrinterActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.Objects

class PurchaseInvoiceListActivity : SearchableSpinnerCustomDialog(),
    OnItemSelectedListener ,SearchableSpinnerCustomDialog.SupplierClickListener{
    private var salesOrderList: ArrayList<SalesOrderModel>? = null
    private var pDialog: SweetAlertDialog? = null
    private var session1: SessionManager? = null
    private var user1: HashMap<String, String>? = null
    private var companyId: String? = null
    var pageNo = 1
    private var behavior: BottomSheetBehavior<*>? = null

    //    private ArrayList<CustomerModel> customerList;
    //    private SelectCustomerAdapter customerNameAdapter;
    //    private RecyclerView customerView;
    //    public static TextView selectCustomer;
    var btnCancel: Button? = null

    //    public TextView customerName;
    //    public EditText customerNameEdittext;
    var dbHelper: DBHelper? = null
    var netTotalText: TextView? = null
    var netTotalApi = 0.00
    private var customerDetails: ArrayList<CustomerDetails>? = null
    var transLayout: LinearLayout? = null
    var customerLayout: View? = null
    private var supplierSpinner: TextView? = null
    private var selectSupplierName: String? = ""
    private var selectSuppliercode: String? = ""
    var salesOrderOptionLayout: View? = null
    var supplierList: ArrayList<SupplierModel>? = ArrayList()
    var soCustomerName: TextView? = null
    var soNumber: TextView? = null
    var optionCancel: TextView? = null
    var cancelSheet: TextView? = null
    var userName: String? = null
    var printPreview: FloatingActionButton? = null
    var locationCode1: String? = null
    var salesOrderStatus: String? = null
    var printPreviewLayout: LinearLayout? = null
    var isSearchCustomerNameClicked = false
    var addnewCustomer = false
    var searchFilterView: View? = null
//    var customerNameText: EditText? = null
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private val mHour = 0
    private val mMinute = 0
    var fromDate: EditText? = null
    var toDate: EditText? = null
    var searchButton: Button? = null
    var cancelSearch: Button? = null
    var salesOrderStatusSpinner: Spinner? = null
    var printSoNumber: String? = null
    var noOfCopy: String? = null
    private var sharedPreferenceUtil: SharedPreferenceUtil? = null
    private var salesOrderHeaderDetails: ArrayList<SalesOrderPrintPreviewModel>? = null
    private var salesPrintList: ArrayList<SalesList>? = null
    private var printerMacId: String? = null
    private var printerType: String? = null
    private var sharedPreferences: SharedPreferences? = null
    var progressLayout: View? = null
    var redirectInvoice = false
    var isFound = "true"
    private var createSalesOrder: Button? = null
    private val customerSelectCode = 13
    var createInvoiceSetting = "true"
    var editSo = "false"
    private val FILTER_CUSTOMER_CODE = 134
    var currentDate: String? = null
    private var usersList: ArrayList<UserListModel>? = null
    var searchableSupplierListl:ArrayList<String>? = null

    //    private Spinner salesManSpinner;
    private var selectedUser = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentFrameLayout = findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(R.layout.activity_purchase_invoice_list, contentFrameLayout)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("A/P Invoice")
        purchaseInvoiceView = findViewById(R.id.rv_purchase_list)
        dbHelper = DBHelper(this)
        session1 = SessionManager(this)
        user1 = session1!!.userDetails
        sharedPreferenceUtil = SharedPreferenceUtil(this)
        companyId = user1!!.get(SessionManager.KEY_COMPANY_CODE)
        userName = user1!!.get(SessionManager.KEY_USER_NAME)
        locationCode1 = user1!!.get(SessionManager.KEY_LOCATION_CODE)

//        customerView=findViewById(R.id.customerList);
        netTotalText = findViewById(R.id.net_total_List)
        //        customerNameEdittext=findViewById(R.id.customer_search);
        supplierSpinner = findViewById(R.id.supplier_name_spinner)
        transLayout = findViewById(R.id.trans_layout)
        customerDetails = dbHelper!!.customer
        customerLayout = findViewById(R.id.customer_layout)
        salesOrderOptionLayout = findViewById(R.id.sales_option)
        soCustomerName = findViewById(R.id.name)
        soNumber = findViewById(R.id.so_no)
        optionCancel = findViewById(R.id.option_cancel)
        cancelSheet = findViewById(R.id.cancel_sheet)
        printPreview = findViewById(R.id.print_preview)
        printPreviewLayout = findViewById(R.id.print_preview_layout)
        searchFilterView = findViewById(R.id.search_filter)
//        customerNameText = findViewById(R.id.customer_name_value)
        fromDate = findViewById(R.id.from_date)
        toDate = findViewById(R.id.to_date)
        salesOrderStatusSpinner = findViewById(R.id.invoice_status)
        emptyLayout = findViewById(R.id.empty_layout)
        cancelSearch = findViewById(R.id.btn_cancel)
        searchButton = findViewById(R.id.btn_search)
        outstandingLayout = findViewById(R.id.outstanding_layout)
        progressLayout = findViewById(R.id.progress_layout)
        createSalesOrder = findViewById(R.id.create_sales)
        //        salesManSpinner=findViewById(R.id.salesman_spinner);
//        salesManSpinner.setOnItemSelectedListener(this);
        shortCodeStr = sharedPreferenceUtil!!.getStringPreference(
            sharedPreferenceUtil!!.KEY_SHORT_CODE, ""
        )
        val c = Calendar.getInstance().time
        println("Current time => $c")
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = df.format(c)
        fromDate!!.setText(formattedDate)
        toDate!!.setText(formattedDate)
        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE)
        printerType = sharedPreferences!!.getString("printer_type", "")
        printerMacId = sharedPreferences!!.getString("mac_address", "")
        Log.w("Printer_Mac_Id:", printerMacId!!)
        Log.w("Printer_Type:", printerType!!)
        dbHelper!!.removeAllItems()
        dbHelper!!.removeAllInvoiceItems()
        supplierClickListenerPurchase = this
        try {
            allUsers
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        AppUtils.setProductsList(null)
//        if( Utils.getSupplierList() != null && Utils.getSupplierList().size > 0){
//            supplierList = Utils.getSupplierList()
//        }else{
//            getVendorList()
//        }
        getVendorList()

        supplierSpinner!!.setOnClickListener{
            if (supplierList!!.size > 0) {
                searchable_supplierDialog("Purchase", searchableSupplierListl!!, supplierList!!)
            }
        }

        val settings = dbHelper!!.settings
        if (settings != null) {
            if (settings.size > 0) {
                for (model in settings) {
                    if (model.settingName == "create_invoice_switch") {
                        Log.w("SettingName:", model.settingName)
                        Log.w("SettingValue:", model.settingValue)
                        createInvoiceSetting = if (model.settingValue == "1") {
                            "true"
                        } else {
                            "false"
                        }
                    } else if (model.settingName == "editSO") {
                        Log.w("SettingName:", model.settingName)
                        Log.w("SettingValue:", model.settingValue)
                        editSo = if (model.settingValue == "True") {
                            "true"
                        } else {
                            "false"
                        }
                    }
                }
            }
        }

        //dbHelper.removeAllProducts();


        /*    customerList=dbHelper.getAllCustomers();
        if (customerList!=null && customerList.size()>0){
            setAdapter(customerList);
        }else {
            getCustomers();
           // new GetCustomersTask().execute();
        }*/
        val c1 = Calendar.getInstance().time
        println("Current time => $c1")
        val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        currentDate = df1.format(c1)
        salesOrderList = ArrayList()
        try {
            getSalesOrderList(companyId, "1", currentDate, currentDate)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        purchaseInvoiceView!!.setHasFixedSize(true)
        purchaseInvoiceView!!.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        purchaseInvoiceAdapter = PurchaseInvoiceAdapterNew(
            this,
            purchaseInvoiceView,
            salesOrderList,
            object : PurchaseInvoiceAdapterNew.CallBack {
                override fun calculateNetTotal(salesList: ArrayList<SalesOrderModel>) {
                    setNettotalFun(salesList)
                }

                override fun showMoreOption(
                    salesorderId: String,
                    customerName: String,
                    status: String
                ) {
                    customerLayout!!.setVisibility(View.GONE)
                    salesOrderOptionLayout!!.setVisibility(View.VISIBLE)
                    soNumber!!.setText(salesorderId)
                    soCustomerName!!.setText(customerName)
                    salesOrderStatus = status
                    if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    // viewCloseBottomSheet();
                }
            })
        purchaseInvoiceView!!.setAdapter(purchaseInvoiceAdapter)

        /*salesOrderAdapter.setOnLoadMoreListener(new SalesOrderAdapterNew.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.e("haint", "Load More");
                salesOrderList.add(null);
                salesOrderAdapter.notifyItemInserted(salesOrderList.size() - 1);
                //Load more data for reyclerview
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("haint", "Load More 2");

                        //Remove loading item
                        salesOrderList.remove(salesOrderList.size() - 1);
                        salesOrderAdapter.notifyItemRemoved(salesOrderList.size());
                        //Load data
                        int index = salesOrderList.size();
                        int end = index + 20;
                        pageNo=pageNo+1;
                        //getSalesOrderList(companyId, String.valueOf(pageNo));
                    }
                }, 5000);
            }
        });*/
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior!!.addBottomSheetCallback(object : BottomSheetCallback() {
            @SuppressLint("LogNotTimber")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING -> Log.i(
                        "BottomSheetCallback",
                        "BottomSheetBehavior.STATE_DRAGGING"
                    )

                    BottomSheetBehavior.STATE_SETTLING -> Log.i(
                        "BottomSheetCallback",
                        "BottomSheetBehavior.STATE_SETTLING"
                    )

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED")
                        if (salesOrderOptionLayout!!.getVisibility() == View.VISIBLE) {
                            supportActionBar!!.setTitle("Select Option")
                        } else {
                            supportActionBar!!.setTitle("Select Customer")
                        }
                        transLayout!!.setVisibility(View.VISIBLE)
                        transLayout!!.setClickable(false)
                        transLayout!!.setEnabled(false)
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED")
                        supportActionBar!!.setTitle("A/P Invoice")
                        transLayout!!.setVisibility(View.GONE)
                        if (redirectInvoice) {
                            if (createInvoiceSetting == "true") {
                                val intent =
                                    Intent(applicationContext, CreateNewInvoiceActivity::class.java)
                                intent.putExtra("customerName", soCustomerName!!.getText().toString())
                            //    intent.putExtra("customerCode", selectedCustomerId.toString())
                                startActivity(intent)
                                finish()
                            } else {
                                CustomerFragment.isLoad = true
                                val intent = Intent(
                                    this@PurchaseInvoiceListActivity,
                                    AddInvoiceActivityOld::class.java
                                )
                              //  intent.putExtra("customerId", selectedCustomerId)
                                intent.putExtra("activityFrom", "SalesOrder")
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> Log.i(
                        "BottomSheetCallback",
                        "BottomSheetBehavior.STATE_HIDDEN"
                    )
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.i("BottomSheetCallback", "slideOffset: $slideOffset")
            }
        })

//        customerNameEdittext.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun afterTextChanged(editable: Editable) {
//                val cusname = editable.toString()
//                if (!cusname.isEmpty()) {
//                    filter(cusname)
//                }
//            }
//        })
//        optionCancel!!.setOnClickListener(View.OnClickListener { viewCloseBottomSheet() })
//        cancelSheet!!.setOnClickListener(View.OnClickListener { viewCloseBottomSheet() })
        createSalesOrder!!.setOnClickListener(View.OnClickListener {
            isSearchCustomerNameClicked = false
            customerLayout!!.setVisibility(View.VISIBLE)
            salesOrderOptionLayout!!.setVisibility(View.GONE)
            searchFilterView!!.setVisibility(View.GONE)
            //  viewCloseBottomSheet();
            if (behavior!!.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                behavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                behavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        })
//        customerNameText!!.setOnClickListener(View.OnClickListener {
//            isSearchCustomerNameClicked = true
//            // viewCloseBottomSheet();
//            val intent = Intent(applicationContext, FilterCustomerListActivity::class.java)
//            startActivityForResult(intent, FILTER_CUSTOMER_CODE)
//        })
        printPreview!!.setOnClickListener(View.OnClickListener { //  viewCloseBottomSheet();
            behavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
            val intent =
                Intent(this@PurchaseInvoiceListActivity, PurchasePrintPreviewActivity::class.java)
            intent.putExtra("soNumber", soNumber!!.getText().toString())
            startActivity(intent)
        })
        printPreviewLayout!!.setOnClickListener(View.OnClickListener { //  viewCloseBottomSheet();
            behavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
            val intent =
                Intent(this@PurchaseInvoiceListActivity, PurchasePrintPreviewActivity::class.java)
            intent.putExtra("soNumber", soNumber!!.getText().toString())
            startActivity(intent)
        })
        fromDate!!.setOnClickListener(View.OnClickListener { getDate(fromDate) })
        toDate!!.setOnClickListener(View.OnClickListener { getDate(toDate) })

        searchButton!!.setOnClickListener(View.OnClickListener {

            //val customer_name = customerNameText!!.getText().toString()
            val sdformat = SimpleDateFormat("dd/MM/yyyy")
            var d1: Date? = null
            var d2: Date? = null
            try {
                d1 = sdformat.parse(fromDate!!.getText().toString())
                d2 = sdformat.parse(toDate!!.getText().toString())
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            if (d1!!.compareTo(d2) > 0) {
                Toast.makeText(
                    applicationContext,
                    "From date should not be greater than to date",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                searchFilterView!!.setVisibility(View.GONE)
                isSearchCustomerNameClicked = true
                try {
                    val oldFromDate = fromDate!!.getText().toString()
                    val oldToDate = toDate!!.getText().toString()
                    val fromDate = SimpleDateFormat("dd/MM/yyyy").parse(oldFromDate)
                    val toDate = SimpleDateFormat("dd/MM/yyyy").parse(oldToDate)
                    // Use SimpleDateFormat#format() to format a Date into a String in a certain pattern.
                    val fromDateString = SimpleDateFormat("yyyyMMdd").format(fromDate)
                    val toDateString = SimpleDateFormat("yyyyMMdd").format(toDate)
                    println("$fromDateString-$toDateString") // 2011-01-18

                    var invoice_status = ""
                    var supplierCodel = ""

                    if (salesOrderStatusSpinner!!.getSelectedItem() == "ALL") {
                        invoice_status = ""
                    } else if (salesOrderStatusSpinner!!.getSelectedItem() == "CLOSED") {
                        invoice_status = "C"
                    } else if (salesOrderStatusSpinner!!.getSelectedItem() == "OPEN") {
                        invoice_status = "O"
                    }

                    setFilterSearch(
                        this@PurchaseInvoiceListActivity,
                        companyId,
                        selectSuppliercode,
                        invoice_status,
                        fromDateString,
                        toDateString
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                ///filterSearch(customer_name, salesOrderStatusSpinner.getSelectedItem().toString(),fromDate.getText().toString(),toDate.getText().toString());
                salesOrderStatusSpinner!!.setSelection(0)
            }
        })
        cancelSearch!!.setOnClickListener(View.OnClickListener {
            isSearchCustomerNameClicked = false
//            customerNameText.setText("")
            fromDate!!.setText(formattedDate)
            toDate!!.setText(formattedDate)
            searchFilterView!!.setVisibility(View.GONE)
            salesOrderStatusSpinner!!.setSelection(0)
            setFilterAdapeter()
        })
    }

    fun setCustomerDetails(customerId: String?) {
        val sharedPreferences = getSharedPreferences("customerPref", MODE_PRIVATE)
        val customerPredEdit = sharedPreferences.edit()
        customerPredEdit.putString("customerId", customerId)
        customerPredEdit.apply()
    }

    @get:Throws(JSONException::class)
    private val allUsers: Unit
        private get() {
            val requestQueue = Volley.newRequestQueue(this)
            val url = Utils.getBaseUrl(this) + "UserList"
            // Initialize a new JsonArrayRequest instance
            Log.w("Given_url_UserList:", url)
            usersList = ArrayList()
            val jsonObject = JSONObject()
            jsonObject.put("User", userName)
            val jsonObjectRequest: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, url, jsonObject,
                    Response.Listener { response: JSONObject ->
                        try {
                            Log.w("UserListResponse:", response.toString())
                            val statusCode = response.optString("statusCode")
                            val message = response.optString("statusMessage")
                            if (statusCode == "1") {
                                val responseData = response.getJSONArray("responseData")
                                for (i in 0 until responseData.length()) {
                                    val `object` = responseData.optJSONObject(i)
                                    val model = UserListModel()
                                    model.userName = `object`.optString("userName")
                                    model.gender = `object`.optString("sex")
                                    model.jobTitle = `object`.optString("jobTitle")
                                    usersList!!.add(model)
                                }
                            } else {
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                            if (usersList!!.size > 0) {
                                setUserListAdapter(usersList)
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "No User Found...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener { error: VolleyError ->
                        // Do something when error occurred
                        pDialog!!.dismiss()
                        Log.w("Error_throwing:", error.toString())
                    }) {
                    override fun getHeaders(): Map<String, String> {
                        val params = HashMap<String, String>()
                        val creds = String.format(
                            "%s:%s",
                            Constants.API_SECRET_CODE,
                            Constants.API_SECRET_PASSWORD
                        )
                        val auth =
                            "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                        params["Authorization"] = auth
                        return params
                    }
                }
            jsonObjectRequest.setRetryPolicy(object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 50000
                }

                override fun getCurrentRetryCount(): Int {
                    return 50000
                }

                @Throws(VolleyError::class)
                override fun retry(error: VolleyError) {
                }
            })
            // Add JsonArrayRequest to the RequestQueue
            requestQueue.add(jsonObjectRequest)
        }

    fun setUserListAdapter(usersList: ArrayList<UserListModel>?) {
        val customAdapter = UserAdapter(applicationContext, usersList)
        //   salesManSpinner.setAdapter(customAdapter);
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        selectedUser = usersList!![position].userName
        Log.w("UserSelected:", selectedUser)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedUser = ""
    }

    @Throws(JSONException::class)
    private fun getSalesOrderDetails(soNumber: String, copy: Int) {
        // Initialize a new RequestQueue instance
        val jsonObject = JSONObject()
        //  jsonObject.put("CompanyCode",companyId);
        jsonObject.put("InvoiceNo", soNumber)
        jsonObject.put("LocationCode", locationCode1)
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "PurchaseInvoiceDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_purchas:", url)
        //   pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        //   pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        //  pDialog.setTitleText("Generating Print Preview...");
        //  pDialog.setCancelable(false);
        //  pDialog.show();
        salesOrderHeaderDetails = ArrayList()
        salesPrintList = ArrayList()
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response: JSONObject ->
                    try {
                        Log.w("Sales_Details_list:", response.toString() + jsonObject)
                        val statusCode = response.optString("statusCode")
                        if (statusCode == "1") {
                            val responseData = response.getJSONArray("responseData")
                            val `object` = responseData.optJSONObject(0)
                            val model = SalesOrderPrintPreviewModel()
                            model.soNumber = `object`.optString("poNo")
                            model.soDate = `object`.optString("poDate")
                            model.customerCode = `object`.optString("vendorCode")
                            model.customerName = `object`.optString("vendorName")
                            model.address =
                                `object`.optString("address1") + `object`.optString("address2") + `object`.optString(
                                    "address3"
                                )
                            model.address1 = `object`.optString("address1")
                            model.address2 = `object`.optString("address2")
                            model.address3 = `object`.optString("address3")
                            model.addressstate =
                                (`object`.optString("block") + " " + `object`.optString("street") + " "
                                        + `object`.optString("city"))
                            model.addresssZipcode =
                                (`object`.optString("countryName") + " " + `object`.optString("state") + " "
                                        + `object`.optString("zipcode"))

                            // model.setDeliveryAddress(model.getAddress());
                            model.subTotal = `object`.optString("subTotal")
                            model.netTax = `object`.optString("taxTotal")
                            model.netTotal = `object`.optString("netTotal")
                            model.taxType = `object`.optString("taxType")
                            model.taxValue = `object`.optString("taxPerc")
                            model.outStandingAmount = `object`.optString("outstandingAmount")
                            model.billDiscount = `object`.optString("billDiscount")
                            model.itemDiscount = `object`.optString("totalDiscount")
                            Utils.setInvoiceMode("SalesOrder")
                            val signFlag = `object`.optString("signFlag")
                            Log.d("cg_signflag", "" + signFlag)
                            if (signFlag == "Y") {
                                val signature = `object`.optString("signature")
                                Utils.setSignature(signature)
                                createSignature()
                            } else {
                                Utils.setSignature("")
                            }
                            val detailsArray = `object`.optJSONArray("purchaseInvoiceDetails")
                            for (i in 0 until detailsArray.length()) {
                                val detailObject = detailsArray.optJSONObject(i)
                                var salesListModel = SalesList()
                                salesListModel.productCode = detailObject.optString("productCode")
                                salesListModel.description = detailObject.optString("productName")
                                salesListModel.lqty = detailObject.optString("unitQty")
                                salesListModel.cqty = detailObject.optString("cartonQty")
                                salesListModel.netQty = detailObject.optString("quantity")
                                salesListModel.cartonPrice = detailObject.optString("cartonPrice")
                                salesListModel.unitPrice = detailObject.optString("price")
                                val qty1 = detailObject.optString("quantity").toDouble()
                                val price1 = detailObject.optString("price").toDouble()
                                val nettotal1 = qty1 * price1
                                salesListModel.total = nettotal1.toString()
                                salesListModel.pricevalue = price1.toString()
                                salesListModel.uomCode = detailObject.optString("uomCode")
                                salesListModel.pcsperCarton = detailObject.optString("pcsPerCarton")
                                salesListModel.itemtax = detailObject.optString("totalTax")
                                salesListModel.subTotal = detailObject.optString("subTotal")
                                salesPrintList!!.add(salesListModel)
                                if (!detailObject.optString("ReturnQty")
                                        .isEmpty() && detailObject.optString("ReturnQty")
                                        .toDouble() > 0
                                ) {
                                    salesListModel = SalesList()
                                    salesListModel.productCode =
                                        detailObject.optString("ProductCode")
                                    salesListModel.description =
                                        detailObject.optString("ProductName")
                                    salesListModel.lqty = detailObject.optString("LQty")
                                    salesListModel.cqty = detailObject.optString("CQty")
                                    salesListModel.netQty =
                                        "-" + detailObject.optString("ReturnQty")
                                    val qty12 = detailObject.optString("ReturnQty").toDouble()
                                    val price12 = detailObject.optString("Price").toDouble()
                                    val nettotal12 = qty12 * price12
                                    salesListModel.total = nettotal12.toString()
                                    salesListModel.pricevalue = price12.toString()
                                    salesListModel.uomCode = detailObject.optString("uomCode")
                                    salesListModel.cartonPrice =
                                        detailObject.optString("CartonPrice")
                                    salesListModel.unitPrice = detailObject.optString("Price")
                                    salesListModel.pcsperCarton =
                                        detailObject.optString("PcsPerCarton")
                                    salesListModel.itemtax = detailObject.optString("Tax")
                                    salesListModel.subTotal = detailObject.optString("subTotal")
                                    salesPrintList!!.add(salesListModel)
                                }
                                model.salesList = salesPrintList
                                salesOrderHeaderDetails!!.add(model)
                            }
                            sentPrintDate(copy)
                            // pDialog.dismiss();
                        } else {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error: VolleyError ->
                    // Do something when error occurred
                    pDialog!!.dismiss()
                    Log.w("Error_throwing:", error.toString())
                }) {
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val creds = String.format(
                        "%s:%s",
                        Constants.API_SECRET_CODE,
                        Constants.API_SECRET_PASSWORD
                    )
                    val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                    params["Authorization"] = auth
                    return params
                }
            }
        jsonObjectRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    @Throws(JSONException::class)
    private fun getSalesOrderDetails(soNumber: String, action: String) {
        // Initialize a new RequestQueue instance
        val jsonObject = JSONObject()
        jsonObject.put("InvoiceNo", soNumber)
        val requestQueue = Volley.newRequestQueue(this)
        //    EditSODetails
        //    EditSODetailsWithFOC
        val url = Utils.getBaseUrl(this) + "APInvoiceDetails"
        Log.w("JsonValue:", jsonObject.toString())
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_salesEdit:", url)
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog!!.setTitleText("Getting A/P invoice Details...")
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response: JSONObject ->
                    try {
                        Log.w("So_Details_edit:", response.toString())
                        if (response.length() > 0) {
                            val statusCode = response.optString("statusCode")
                            if (statusCode == "1") {
                                val salesArray = response.optJSONArray("responseData")
                                val salesObject = salesArray.optJSONObject(0)
                                val salesorder_no = salesObject.optString("poNo")
                                val salesorder_code = salesObject.optString("code")
                                val order_no = salesObject.optString("customerReferenceNo")
                                val company_code = salesObject.optString("CompanyCode")
                                val customer_code = salesObject.optString("customerCode")
                                val customer_name = salesObject.optString("customerName")
                                val customerBill_Disc =
                                    salesObject.optString("customerDiscountPercentage")
                                val total = salesObject.optString("total")
                                val sub_total = salesObject.optString("subTotal")
                                val bill_discount = salesObject.optString("billDiscount")
                                val item_discount = salesObject.optString("ItemDiscount")
                                val tax = salesObject.optString("taxTotal")
                                val net_total = salesObject.optString("netTotal")
                                val currency_rate = salesObject.optString("CurrencyRate")
                                val currency_name = salesObject.optString("currencyName")
                                val tax_type = salesObject.optString("taxType")
                                val tax_perc = salesObject.optString("taxPerc")
                                val tax_code = salesObject.optString("taxCode")
                                val phone_no = salesObject.optString("DelPhoneNo")
                                val so_date = salesObject.optString("soDate")
                                val signFlag = salesObject.optString("signFlag")
                                Utils.setInvoiceMode("SalesOrder")
                                if (signFlag == "Y") {
                                    val signature = salesObject.optString("signature")
                                    Utils.setSignature(signature)
                                }
                                dbHelper!!.removeCustomer()
                                dbHelper!!.insertCustomer(
                                    customer_code,
                                    customer_name,
                                    phone_no,
                                    salesObject.optString("address1"),
                                    salesObject.optString("Address2"),
                                    salesObject.optString("Address3"),
                                    salesObject.optString("IsActive"),
                                    salesObject.optString("HaveTax"),
                                    salesObject.optString("taxType"),
                                    salesObject.optString("taxPerc"),
                                    salesObject.optString("taxCode"),
                                    salesObject.optString("CreditLimit"),
                                    "Singapore",
                                    salesObject.optString("currencyCode")
                                )
                                customerDetails = dbHelper!!.customer
                                dbHelper!!.removeAllItems()
                                dbHelper!!.removeAllInvoiceItems()
                                dbHelper!!.removeCustomerTaxes()
                                val model = CustomerDetails()
                                model.customerCode = customer_code
                                model.customerName = customer_name
                                model.customerAddress1 = salesObject.optString("address1")
                                model.taxPerc = salesObject.optString("taxPerc")
                                model.taxType = salesObject.optString("taxType")
                                model.taxCode = salesObject.optString("taxCode")
                                val taxList = ArrayList<CustomerDetails>()
                                taxList.add(model)
                                Log.w("TaxModelPrint::", model.toString())
                                dbHelper!!.insertCustomerTaxValues(taxList)
                                val products = salesObject.getJSONArray("salesOrderDetails")
                                for (i in 0 until products.length()) {
                                    val `object` = products.getJSONObject(i)
                                    var lqty: String? = "0.0"
                                    var cqty = "0.0"
                                    if (`object`.optString("unitQty") != "null") {
                                        lqty = `object`.optString("unitQty")
                                    }
                                    if (`object`.optString("quantity") != "null") {
                                        cqty = `object`.optString("quantity")
                                    }
                                    val priceValue = 0.0
                                    val return_qty = "0"
                                    val net_qty = cqty.toDouble() - return_qty.toDouble()
                                    val price_value = `object`.optString("price")
                                    //String price_value=object.optString("grossPrice");
                                    val return_amt = return_qty.toDouble() * price_value.toDouble()
                                    val total1 = net_qty * price_value.toDouble()
                                    val sub_total1 = total1 - return_amt
                                    val laterDate = System.currentTimeMillis()
                                    val millisec = 18000
                                    val original = Timestamp(laterDate)
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = original.time
                                    cal.add(Calendar.MILLISECOND, millisec)
                                    val timeStamp = Timestamp(cal.time.time)
                                    dbHelper!!.insertCreateInvoiceCartEdit(
                                        `object`.optString("productCode"),
                                        `object`.optString("productName"),
                                        `object`.optString("uomCode"),
                                        cqty,
                                        return_qty, net_qty.toString(),
                                        `object`.optString("focQty"),
                                        price_value,
                                        `object`.optString("stockInHand"),
                                        `object`.optString("total"),
                                        `object`.optString("subTotal"),
                                        `object`.optString("taxAmount"),
                                        `object`.optString("netTotal"),
                                        "",
                                        "",
                                        "",
                                        "",
                                        `object`.optString("focQty"),
                                        `object`.optString("minimumSellingPrice"),
                                        `object`.optString("stockInHand"), timeStamp.toString(),
                                        `object`.optString("itemAllowFOC")
                                    )
                                    Log.w(
                                        "ProductsLength:",
                                        products.length()
                                            .toString() + "" + `object`.optString("itemAllowFOC")
                                    )
                                    Log.w(
                                        "ActualPrintProducts:",
                                        dbHelper!!.numberOfRowsInInvoice().toString() + ""
                                    )
                                    if (products.length() == dbHelper!!.numberOfRowsInInvoice()) {
                                        redirectActivity(
                                            action,
                                            customer_code,
                                            customer_name,
                                            salesorder_code,
                                            order_no,
                                            customerBill_Disc
                                        )
                                        break
                                    }
                                }
                            } else {
                            }
                        }
                        pDialog!!.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error: VolleyError ->
                    // Do something when error occurred
                    pDialog!!.dismiss()
                    Log.w("Error_throwing:", error.toString())
                }) {
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val creds = String.format(
                        "%s:%s",
                        Constants.API_SECRET_CODE,
                        Constants.API_SECRET_PASSWORD
                    )
                    val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                    params["Authorization"] = auth
                    return params
                }
            }
        jsonObjectRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    private fun createSignature() {
        if (Utils.getSignature() != null && !Utils.getSignature().isEmpty()) {
            try {
                ImageUtil.saveStamp(this, Utils.getSignature(), "Signature")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE)
        printerType = sharedPreferences!!.getString("printer_type", "")
        printerMacId = sharedPreferences!!.getString("mac_address", "")
        super.onResume()
    }

    @Throws(IOException::class)
    private fun sentPrintDate(copy: Int) {
        if (Utils.validatePrinterConfiguration(this, printerType, printerMacId)) {
            if (printerType == "TSC Printer") {
                val printer =
                    TSCPrinter(this@PurchaseInvoiceListActivity, printerMacId, "SalesOrder")
                printer.printSalesOrder(copy, salesOrderHeaderDetails, salesPrintList)
                printer.setOnCompletionListener {
                    Utils.setSignature("")
                    Toast.makeText(
                        applicationContext,
                        "SalesOrder printed successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (printerType == "Zebra Printer") {
                val zebraPrinterActivity =
                    ZebraPrinterActivity(this@PurchaseInvoiceListActivity, printerMacId)
                zebraPrinterActivity.printSalesOrder(copy, salesOrderHeaderDetails, salesPrintList)
            }
        }
    }

    fun setSalesOrderAdapter(
        context: Context?,
        salesList: ArrayList<SalesOrderModel>?,
        invoiceStatus: String?
    ) {
        val filterdNames = ArrayList<SalesOrderModel>()
        /*   for (SalesOrderModel model: SalesOrderAdapterNew.getSalesOrderList()){
            switch (invoiceStatus) {
                case "ALL":
                    filterdNames.add(model);
                    break;
                case "OPEN":
                    if (model.getStatus().equals("0")) {
                        filterdNames.add(model);
                    }
                    break;
                case "CLOSED":
                    if (!model.getStatus().equals("0")) {
                        filterdNames.add(model);
                    }
                    break;
            }
        }*/purchaseInvoiceView!!.setHasFixedSize(true)
        purchaseInvoiceView!!.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        purchaseInvoiceAdapter = PurchaseInvoiceAdapterNew(
            context,
            purchaseInvoiceView,
            salesList,
            object : PurchaseInvoiceAdapterNew.CallBack {
                override fun calculateNetTotal(salesList: ArrayList<SalesOrderModel>) {
                    setNettotalFun(salesList)
                }

                override fun showMoreOption(
                    salesorderId: String,
                    customerName: String,
                    status: String
                ) {
                    customerLayout!!.visibility = View.GONE
                    salesOrderOptionLayout!!.visibility = View.VISIBLE
                    soNumber!!.text = salesorderId
                    soCustomerName!!.text = customerName
                    salesOrderStatus = status
                    if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    // viewCloseBottomSheet();
                }
            })
        purchaseInvoiceView!!.adapter = purchaseInvoiceAdapter
        if (salesOrderList!!.size > 0) {
            setNettotalFun(salesOrderList)
        }
        selectSuppliercode = ""
        selectSupplierName = ""
        supplierSpinner!!.setText("")


    }

    fun setNettotalFun(salesOrderList: ArrayList<SalesOrderModel>?) {
        var net_amount = 0.0
        for (model in salesOrderList!!) {
            if (model.netTotal != null && model.netTotal != "null") {
                net_amount = net_amount + model.netTotal.toDouble()
            }
        }
        netTotalText!!.text = "$ " + Utils.twoDecimalPoint(net_amount)
    }

    fun redirectActivity(
        action: String, customer_code: String?, customer_name: String?, salesorder_code: String?,
        order_no: String?, customerBill_Disc: String
    ) {
        //  if (products.length()==dbHelper.numberOfRowsInInvoice()){
        Log.w("acttionSO", "" + action)
        Utils.setCustomerSession(this@PurchaseInvoiceListActivity, customer_code)
        if (action == "Edit") {
            val intent = Intent(applicationContext, CreateNewInvoiceActivity::class.java)
            intent.putExtra("customerName", customer_name)
            intent.putExtra("customerCode", customer_code)
            intent.putExtra("editSoNumber", salesorder_code)
            intent.putExtra("customerBillDisc", customerBill_Disc)
            intent.putExtra("orderNo", order_no)
            intent.putExtra("from", "SalesEdit")
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(applicationContext, CreateNewInvoiceActivity::class.java)
            intent.putExtra("customerName", customer_name)
            intent.putExtra("customerCode", customer_code)
            intent.putExtra("editSoNumber", salesorder_code)
            intent.putExtra("orderNo", order_no)
            intent.putExtra("customerBillDisc", customerBill_Disc)
            Log.w("acttionSOdisc", "" + customerBill_Disc)
            intent.putExtra("from", "ConvertInvoice")
            startActivity(intent)
            finish()
        }
    }

    @Throws(JSONException::class)
    fun setFilterSearch(
        context: Context?,
        companyId: String?,
        customerCode: String?,
        status: String?,
        fromdate: String?,
        todate: String?
    ) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        // {"CustomerCode":"","ReceiptNo":"","StartDate":"","EndDate":,"CompanyCode":"1"}
        val jsonObject = JSONObject()
        //        if (selectedUser!=null && !selectedUser.isEmpty()){
//            jsonObject.put("User",selectedUser);
//        }else {
//            jsonObject.put("User",userName);
//        }
        jsonObject.put("CustomerCode", customerCode)
        jsonObject.put("FromDate", fromdate)
        jsonObject.put("ToDate", todate)
        jsonObject.put("DocStatus", status)
        // Initialize a new JsonArrayRequest instance
        val url = Utils.getBaseUrl(this) + "APinvoicelist/APInvoiceList"
        Log.w("Given_url_FilterSearch:", "$url-$jsonObject")
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog!!.setTitleText("Getting A/P invoice...")
        pDialog!!.setCancelable(false)
        pDialog!!.show()
        salesOrderList = ArrayList()
        val jsonArrayRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject ->
                try {
                    Log.w("Response_Filter:", response.toString())
                    pDialog!!.dismiss()
                    val statusCode = response.optString("statusCode")
                    if (statusCode == "1") {
                        val salesOrderArray = response.optJSONArray("responseData")
                        for (i in 0 until salesOrderArray.length()) {
                            val `object` = salesOrderArray.optJSONObject(i)
                            val model = SalesOrderModel()
                            model.name = `object`.optString("customerName")
                            model.date = `object`.optString("invoiceDate")
                            model.balance = `object`.optString("balance")
                            model.saleOrderNumber = `object`.optString("invoiceNumber")
                            model.netTotal = `object`.optString("netTotal")
                            model.status = `object`.optString("invoiceStatus")
                            model.salesOrderCode = `object`.optString("code")
                            model.remarks = `object`.optString("remark")
                            // netTotalApi +=Double.parseDouble(object.optString("netTotal"));
                            // netTotalText.setText("$ "+Utils.twoDecimalPoint(netTotalApi));
                            val salesLists = ArrayList<SalesList>()
                            model.salesList = salesLists
                            salesOrderList!!.add(model)
                        }
                        // salesOrderAdapter.notifyDataSetChanged();
                        //  salesOrderAdapter.setLoaded();
                        setShowHide()
                    } else {
                        setShowHide()
                        //Toast.makeText(getApplicationContext(),"Error in getting SalesOrder Data",Toast.LENGTH_LONG).show();
                    }
                    setSalesOrderAdapter(context, salesOrderList, status)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error: VolleyError ->
                pDialog!!.dismiss()
                // Do something when error occurred
                Log.w("Error_throwing:", error.toString())
                Toast.makeText(
                    applicationContext,
                    "Server Error,Please try again..",
                    Toast.LENGTH_LONG
                ).show()
            }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                val creds =
                    String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD)
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                params["Authorization"] = auth
                return params
            }
        }
        jsonArrayRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonArrayRequest)
    }

    @Throws(JSONException::class)
    fun getSalesOrderList(
        companyCode: String?,
        pageNo: String,
        fromdate: String?,
        todate: String?
    ) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        // Initialize a new JsonArrayRequest instance
        val jsonObject = JSONObject()
        jsonObject.put("CustomerCode", "")
        jsonObject.put("FromDate", fromdate)
        jsonObject.put("ToDate", todate)
        jsonObject.put("DocStatus", "")
        val url = Utils.getBaseUrl(this) + "APinvoicelist/APInvoiceList"
        Log.w("Given_url:", "$url-$jsonObject")
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog!!.setTitleText("Getting A/P invoice...")
        pDialog!!.setCancelable(false)
        if (pageNo == "1") {
            pDialog!!.show()
        }
        val jsonArrayRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response: JSONObject ->
                    try {
                        // {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"WinApp","customerName":"WinApp","soNumber":"5","soStatus":"O",
                        // "soDate":"06\/08\/2021","netTotal":"26.750000","balance":"26.750000","totalDiscount":"0.000000","paidAmount":"0.000000",
                        // "contactPersonCode":"","createDate":"07\/08\/2021","updateDate":"07\/08\/2021","remark":"Add sales order through mobile"},
                        // {"customerCode":"WinApp","customerName":"WinApp","soNumber":"4","soStatus":"O","soDate":"06\/08\/2021","netTotal":"26.750000",
                        // "balance":"26.750000","totalDiscount":"0.000000","paidAmount":"0.000000","contactPersonCode":"","createDate":"07\/08\/2021",
                        // "updateDate":"07\/08\/2021","remark":""},{"customerCode":"WinApp","customerName":"WinApp","soNumber":"3","soStatus":"O",
                        // "soDate":"06\/08\/2021","netTotal":"26.750000","balance":"26.750000","totalDiscount":"0.000000","paidAmount":"0.000000",
                        // "contactPersonCode":"","createDate":"07\/08\/2021","updateDate":"07\/08\/2021","remark":""},
                        // {"customerCode":"EC001","customerName":"Exclusive customer2","soNumber":"2","soStatus":"O","soDate":"07\/08\/2021",
                        // "netTotal":"45762284.350000","balance":"45762284.350000","totalDiscount":"432004.950000","paidAmount":"0.000000",
                        // "contactPersonCode":"0","createDate":"07\/08\/2021","updateDate":"07\/08\/2021","remark":""},
                        // {"customerCode":"CUS\/686","customerName":"VH FACTORY","soNumber":"1","soStatus":"O","soDate":"07\/08\/2021","netTotal":"524.300000",
                        // "balance":"524.300000","totalDiscount":"0.000000","paidAmount":"0.000000","contactPersonCode":"0","createDate":"07\/08\/2021",
                        // "updateDate":"07\/08\/2021","remark":""}]}
                        Log.w("API_For_SalesOrder:", response.toString())
                        pDialog!!.dismiss()
                        val statusCode = response.optString("statusCode")
                        if (statusCode == "1") {
                            val salesOrderArray = response.optJSONArray("responseData")
                            for (i in 0 until salesOrderArray.length()) {
                                val `object` = salesOrderArray.optJSONObject(i)
                                val model = SalesOrderModel()
                                model.name = `object`.optString("customerName")
                                model.date = `object`.optString("invoiceDate")
                                model.balance = `object`.optString("balance")
                                model.saleOrderNumber = `object`.optString("invoiceNumber")
                                model.netTotal = `object`.optString("netTotal")
                                model.status = `object`.optString("invoiceStatus")
                                model.salesOrderCode = `object`.optString("code")
                                model.remarks = `object`.optString("remark")
                                //  isFound=invoiceObject.optString("ErrorMessage");
                                val salesLists = ArrayList<SalesList>()
                                model.salesList = salesLists
                                salesOrderList!!.add(model)
                            }
                            purchaseInvoiceAdapter!!.setLoaded()
                            purchaseInvoiceAdapter!!.notifyDataSetChanged()
                            setShowHide()
                        } else {
                            setShowHide()
                            //Toast.makeText(getApplicationContext(),"Error in getting SalesOrder Data",Toast.LENGTH_LONG).show();
                        }
                        /*  if (response.length()>0) {
                            for (int i = 0; i < response.length(); i++) {
                                // Get current json object
                                JSONObject invoiceObject = response.getJSONObject(i);
                                SalesOrderModel model=new SalesOrderModel();
                                model.setName(invoiceObject.optString("CustomerName"));
                                model.setDate(invoiceObject.optString("SoDateString"));
                                model.setBalance(invoiceObject.optString("BalanceAmount"));
                                model.setSaleOrderNumber(invoiceObject.optString("SoNo"));
                                model.setAddress(invoiceObject.optString("Address1"));
                                model.setNetTotal(invoiceObject.optString("NetTotal"));
                                model.setStatus(invoiceObject.optString("Status"));
                                isFound=invoiceObject.optString("ErrorMessage");
                                ArrayList<SalesOrderPrintPreviewModel.SalesList> salesLists=new ArrayList<>();
                                model.setSalesList(salesLists);
                                salesOrderList.add(model);
                            }

                          */
                        /*  if (salesOrderList.size()>0){
                                salesOrdersView.setVisibility(View.VISIBLE);
                                emptyLayout.setVisibility(View.GONE);
                                outstandingLayout.setVisibility(View.VISIBLE);
                            }else {
                                salesOrdersView.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                                outstandingLayout.setVisibility(View.GONE);
                            }*/
                        /*

                            salesOrderAdapter.notifyDataSetChanged();
                            salesOrderAdapter.setLoaded();
                        }*/
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error: VolleyError ->
                    pDialog!!.dismiss()
                    // Do something when error occurred
                    Log.w("Error_throwing:", error.toString())
                    Toast.makeText(
                        applicationContext,
                        "Server Error,Please try again..",
                        Toast.LENGTH_LONG
                    ).show()
                }) {
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    val creds = String.format(
                        "%s:%s",
                        Constants.API_SECRET_CODE,
                        Constants.API_SECRET_PASSWORD
                    )
                    val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.DEFAULT)
                    params["Authorization"] = auth
                    return params
                }
            }
        jsonArrayRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonArrayRequest)
    }

    fun setShowHide() {
        if (salesOrderList!!.size > 0) {
            purchaseInvoiceView!!.visibility = View.VISIBLE
            outstandingLayout!!.visibility = View.VISIBLE
            emptyLayout!!.visibility = View.GONE
        } else {
            purchaseInvoiceView!!.visibility = View.GONE
            emptyLayout!!.visibility = View.VISIBLE
            outstandingLayout!!.visibility = View.GONE
        }
        if (intent != null) {
            printSoNumber = intent.getStringExtra("printSoNumber")
            noOfCopy = intent.getStringExtra("noOfCopy")
            if (printSoNumber != null && !printSoNumber!!.isEmpty()) {
                try {
                    getSalesOrderDetails(printSoNumber!!, noOfCopy!!.toInt())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setFilterAdapeter() {
        purchaseInvoiceView!!.visibility = View.VISIBLE
        emptyLayout!!.visibility = View.GONE
        outstandingLayout!!.visibility = View.VISIBLE
        purchaseInvoiceView!!.setHasFixedSize(true)
        purchaseInvoiceView!!.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        purchaseInvoiceAdapter = PurchaseInvoiceAdapterNew(
            this,
            purchaseInvoiceView,
            salesOrderList,
            object : PurchaseInvoiceAdapterNew.CallBack {
                override fun calculateNetTotal(salesList: ArrayList<SalesOrderModel>) {
                    setNettotalFun(salesList)
                }

                override fun showMoreOption(
                    salesorderId: String,
                    customerName: String,
                    status: String
                ) {
                    customerLayout!!.visibility = View.GONE
                    salesOrderOptionLayout!!.visibility = View.VISIBLE
                    soNumber!!.text = salesorderId
                    soCustomerName!!.text = customerName
                    salesOrderStatus = status
                    //viewCloseBottomSheet();
                    if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            })
        purchaseInvoiceView!!.adapter = purchaseInvoiceAdapter
        if (salesOrderList!!.size > 0) {
            setNettotalFun(salesOrderList)
        }
        /* salesOrderAdapter.setOnLoadMoreListener(new SalesOrderAdapterNew.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.e("haint", "Load More");
                salesOrderList.add(null);
                salesOrderAdapter.notifyItemInserted(salesOrderList.size() - 1);
                //Load more data for reyclerview
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("haint", "Load More 2");

                        //Remove loading item
                        salesOrderList.remove(salesOrderList.size() - 1);
                        salesOrderAdapter.notifyItemRemoved(salesOrderList.size());
                        //Load data
                        int index = salesOrderList.size();
                        int end = index + 20;
                        pageNo=pageNo+1;
                      //  getSalesOrderList(companyId, String.valueOf(pageNo));
                    }
                }, 5000);
            }
        });*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.sorting_menu, menu)
        val action_add = menu.findItem(R.id.action_add)
        action_add.setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { //finish();
            onBackPressed()

            /*   case R.id.action_remove:
                showRemoveAlert();
                break;*/
        } else if (item.itemId == R.id.action_customer_name) {
            Collections.sort(salesOrderList) { obj1, obj2 ->
                // ## Ascending order
                obj1.name.compareTo(obj2.name, ignoreCase = true) // To compare string values
                // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
            }
            purchaseInvoiceAdapter!!.notifyDataSetChanged()
        } else if (item.itemId == R.id.action_amount) {
            Collections.sort(salesOrderList) { obj1, obj2 ->
                // ## Ascending order
                //  return obj1.getNetTotal().compareToIgnoreCase(obj2.getNetTotal()); // To compare string values
                java.lang.Double.valueOf(obj1.netTotal)
                    .compareTo(java.lang.Double.valueOf(obj2.netTotal)) // To compare integer values

                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
            }
            purchaseInvoiceAdapter!!.notifyDataSetChanged()
        } else if (item.itemId == R.id.action_date) {
            try {
                Collections.sort(salesOrderList) { obj1, obj2 ->
                    val sdfo = SimpleDateFormat("yyyy-MM-dd")
                    // Get the two dates to be compared
                    var d1: Date? = null
                    var d2: Date? = null
                    try {
                        d1 = sdfo.parse(obj1.date)
                        d2 = sdfo.parse(obj2.date)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    // ## Ascending order
                    //  return obj1.getNetTotal().compareToIgnoreCase(obj2.getNetTotal()); // To compare string values
                    d1!!.compareTo(d2) // To compare integer values

                    // ## Descending order
                    // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                    // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                }
                purchaseInvoiceAdapter!!.notifyDataSetChanged()
            } catch (ex: Exception) {
                Log.w("Error:", ex.message!!)
            }
        } else if (item.itemId == R.id.action_add) {
            val intent = Intent(applicationContext, CustomerListActivity::class.java)
            intent.putExtra("from", "so")
            startActivityForResult(intent, customerSelectCode)

            /*  isSearchCustomerNameClicked=false;
            addnewCustomer=true;
            SharedPreferences sharedPreferences = getSharedPreferences("customerPref",MODE_PRIVATE);
                String selectCustomerId = sharedPreferences.getString("customerId", "");
                if (selectCustomerId!=null && !selectCustomerId.isEmpty()) {
                    customerDetails = dbHelper.getCustomer(selectCustomerId);
                    if (customerDetails.size()>0){
                        showCustomerDialog(this,customerDetails.get(0).getCustomerName(),customerDetails.get(0).getCustomerCode(),customerDetails.get(0).getCustomerAddress1());
                    }else {
                        customerLayout.setVisibility(View.VISIBLE);
                        searchFilterView.setVisibility(View.GONE);
                        salesOrderOptionLayout.setVisibility(View.GONE);
                        //viewCloseBottomSheet();
                        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                }else {
                    customerLayout.setVisibility(View.VISIBLE);
                    searchFilterView.setVisibility(View.GONE);
                    salesOrderOptionLayout.setVisibility(View.GONE);
                    //viewCloseBottomSheet();
                    if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }*/
        } else if (item.itemId == R.id.action_barcode) {
            val intent = Intent(applicationContext, BarCodeScanner::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.action_filter) {
            if (searchFilterView!!.visibility == View.VISIBLE) {
                searchFilterView!!.visibility = View.GONE
                selectSupplierName = ""
                selectSuppliercode = ""
                supplierSpinner!!.setText("")

                isSearchCustomerNameClicked = false
                if (behavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                //slideUp(searchFilterView);
            } else {
                selectSupplierName = ""
                selectSuppliercode = ""
                supplierSpinner!!.setText("")

                isSearchCustomerNameClicked = false
                searchFilterView!!.visibility = View.VISIBLE
                if (behavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                // slideDown(searchFilterView);
            }
        }
        return true
    }
//    fun setSupplierSpinner(spinnerlist: ArrayList<SupplierModel>) {
//        val spinnerlst = SupplierModel(
//            customerName = "Select Supplier",
//            customerCode = "", currencyCode = "", currencyName = "",
//            taxCode = "", taxName = "", taxPercentage = "", taxType = ""
//        )
//        spinnerlist.add(0, spinnerlst)
//        Log.w("spinnnVal", "" + spinnerlist)
//        val adapter = ArrayAdapter<String>(this, R.layout.cust_spinner_item)
//        for (i in spinnerlist.indices) {
//            adapter.add(spinnerlist[i].customerName)
//        }
//        supplierSpinner!!.adapter = adapter
//        supplierSpinner!!.setTitle("")
//        supplierSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(adapter: AdapterView<*>, v: View, position: Int, id: Long) {
//                // On selecting a spinner item
//                selectSuppliercode = spinnerlist[position].customerCode
//                selectSupplierName = spinnerlist[position].customerName
////                currencyCode = spinnerlist[position].currencyCode
////                taxCode = spinnerlist[position].taxCode
////                taxPercentage = spinnerlist[position].taxPercentage
////                taxType = spinnerlist[position].taxType
//                Log.w("spinnnVal1", "" + spinnerlist[position].taxType)
//
////                if (!selectSupplierName!!.equals("Select Supplier")) {
////                    serviceNameTxtl!!.isEnabled = true
////                    service_amount_Txt!!.isEnabled = true
////                }
//            }
//
//            override fun onNothingSelected(arg0: AdapterView<*>?) {}
//        }
//    }
    @Throws(JSONException::class)
    private fun getVendorList() {
        // CommonMethods.showProgressDialog(this)
        val jsonObject = JSONObject()
        //  jsonObject.put("WhsCode", whsCode)

        val requestQueue = Volley.newRequestQueue(this)

        val url = Utils.getBaseUrl(this) + "vendorList"
        Log.w("url_vendorlist:", url)

        supplierList = ArrayList()
    searchableSupplierListl =ArrayList()

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
                                    searchableSupplierListl!!.add(
                                        obj.optString("vendorName") + "~" + obj.optString(
                                            "vendorCode"
                                        )
                                    )

                                }

                                withContext(Dispatchers.Main) {
                                    if (supplierList!!.size > 0) {
                                        Utils.setSupplierList(supplierList!!)
                                        //setSupplierSpinner(supplierList!!)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == customerSelectCode) {
            if (resultCode == RESULT_OK) {
                val result = data!!.getStringExtra("customerCode")
                Utils.setCustomerSession(this, result)
                val intent =
                    Intent(this@PurchaseInvoiceListActivity, AddInvoiceActivityOld::class.java)
                intent.putExtra("customerId", result)
                intent.putExtra("activityFrom", "SalesOrder")
                startActivity(intent)
                // finish();
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        } else if (requestCode == FILTER_CUSTOMER_CODE && resultCode == RESULT_OK) {
          //  selectedCustomerId = data!!.getStringExtra("customerCode")
            val selectCustomerName = data!!.getStringExtra("customerName")
         //   customerNameText!!.setText(selectCustomerName)
        }
    } //onActivityResult

    fun closeView() {
        if (behavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            behavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    fun showProductDeleteAlert(customerId: String?) {
        val builder1 = AlertDialog.Builder(this)
        builder1.setTitle("Warning !")
        builder1.setMessage("Products in Cart will be removed..")
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "OK"
        ) { dialog, id ->
            dialog.cancel()
            dbHelper!!.removeAllItems()
            addnewCustomer = false
            setCustomerDetails(customerId)
//            viewCloseBottomSheet()
//            selectedCustomerId = customerId
            redirectInvoice = false
            // Intent intent=new Intent(SalesOrderListActivity.this,AddInvoiceActivity.class);
            // intent.putExtra("customerId",customerId);
            // intent.putExtra("activityFrom","SalesOrder");
            // startActivity(intent);
            // finish();
        }
        builder1.setNegativeButton(
            "CANCEL"
        ) { dialog, id -> dialog.cancel() }
        val alert11 = builder1.create()
        alert11.show()
    }

//    fun viewCloseBottomSheet() {
//        hideKeyboard()
//        if (isSearchCustomerNameClicked || addnewCustomer) {
//            customerLayout!!.visibility = View.VISIBLE
//            salesOrderOptionLayout!!.visibility = View.GONE
//            redirectInvoice = false
//        } else {
//            customerLayout!!.visibility = View.GONE
//            salesOrderOptionLayout!!.visibility = View.VISIBLE
//        }
//        if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
//            behavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
//        } else {
//            behavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
//        }
//        val customerList: = dbHelper!!.allCustomers
//        setAdapter(customerList)
//        // get the Customer name from the local db
//    }

    fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            // TODO: handle exception
        }
    }

    fun getDate(dateEditext: EditText?) {
        // Get Current Date
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this@PurchaseInvoiceListActivity,
            { view, year, monthOfYear, dayOfMonth ->
                dateEditext!!.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year) },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
    }

    override fun onBackPressed() {
        //Execute your code here
        // Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        // startActivity(intent);
        finish()
    }

    companion object {
        var purchaseInvoiceView: RecyclerView? = null
        var purchaseInvoiceAdapter: PurchaseInvoiceAdapterNew? = null
        var emptyLayout: LinearLayout? = null
        var outstandingLayout: LinearLayout? = null
//        var selectedCustomerId: String? = ""
        var shortCodeStr = ""
        var supplierClickListenerPurchase: PurchaseInvoiceListActivity? = null
        fun filterSearch(
            customerName: String,
            invoiceStatus: String?,
            fromdate: String?,
            todate: String?
        ) {
            try {
                val filterdNames = ArrayList<SalesOrderModel>()
                val sdf = SimpleDateFormat("dd-MM-yyyy")
                var from_date: Date? = null
                var to_date: Date? = null
                try {
                    from_date = sdf.parse(fromdate)
                    to_date = sdf.parse(todate)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                for (model in PurchaseInvoiceAdapterNew.getSalesOrderList()) {
                    val compareDate = sdf.parse(model.date)
                    if (from_date == to_date) {
                        if (from_date == compareDate) {
                            if (!customerName.isEmpty()) {
                                if (model.name.lowercase(Locale.getDefault()).contains(
                                        customerName.lowercase(
                                            Locale.getDefault()
                                        )
                                    )
                                ) {
                                    when (invoiceStatus) {
                                        "ALL" -> filterdNames.add(model)
                                        "OPEN" -> if (model.status == "0") {
                                            filterdNames.add(model)
                                        }

                                        "CLOSED" -> if (model.status != "0") {
                                            filterdNames.add(model)
                                        }
                                    }
                                    purchaseInvoiceAdapter!!.filterList(filterdNames)
                                }
                            } else {
                                when (invoiceStatus) {
                                    "ALL" -> filterdNames.add(model)
                                    "OPEN" -> if (model.status == "0") {
                                        filterdNames.add(model)
                                    }

                                    "CLOSED" -> if (model.status != "0") {
                                        filterdNames.add(model)
                                    }
                                }
                                purchaseInvoiceAdapter!!.filterList(filterdNames)
                            }
                        }
                    } else if (compareDate.compareTo(from_date) >= 0 && compareDate.compareTo(
                            to_date
                        ) <= 0
                    ) {
                        println("Compare date occurs after from date")
                        if (!customerName.isEmpty()) {
                            if (model.name.lowercase(Locale.getDefault()).contains(
                                    customerName.lowercase(
                                        Locale.getDefault()
                                    )
                                )
                            ) {
                                when (invoiceStatus) {
                                    "ALL" -> filterdNames.add(model)
                                    "OPEN" -> if (model.status == "0") {
                                        filterdNames.add(model)
                                    }

                                    "CLOSED" -> if (model.status != "0") {
                                        filterdNames.add(model)
                                    }
                                }
                                purchaseInvoiceAdapter!!.filterList(filterdNames)
                            }
                        } else {
                            when (invoiceStatus) {
                                "ALL" -> filterdNames.add(model)
                                "OPEN" -> if (model.status == "0") {
                                    filterdNames.add(model)
                                }

                                "CLOSED" -> if (model.status != "0") {
                                    filterdNames.add(model)
                                }
                            }
                            purchaseInvoiceAdapter!!.filterList(filterdNames)
                        }
                    }
                    purchaseInvoiceAdapter!!.filterList(filterdNames)
                }
                Log.w("FilteredSize:", filterdNames.size.toString() + "")
                if (filterdNames.size > 0) {
                    purchaseInvoiceView!!.visibility = View.VISIBLE
                    outstandingLayout!!.visibility = View.VISIBLE
                    emptyLayout!!.visibility = View.GONE
                    //  setNettotal(filterdNames);
                    // invoiceAdapter.filterList(filterdNames);
                } else {
                    purchaseInvoiceView!!.visibility = View.GONE
                    outstandingLayout!!.visibility = View.GONE
                    emptyLayout!!.visibility = View.VISIBLE
                }
            } catch (ex: Exception) {
                Log.e("Error_in_filter", ex.message!!)
            }
        }
    }

    override fun supplierSelected(supplierName: String) {
        var selectName = arrayOfNulls<String>(0)
        var supNamel: String? = ""
        selectName = supplierName.split("~".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        selectSuppliercode = selectName[1]
        supNamel = selectName[0]
        supplierSpinner!!.setText(supNamel)
    }
}