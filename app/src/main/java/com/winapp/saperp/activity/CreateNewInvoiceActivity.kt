package com.winapp.saperp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.winapp.saperp.BuildConfig
import com.winapp.saperp.R
import com.winapp.saperp.adapter.DuplicateInvoiceAdapter
import com.winapp.saperp.adapter.NewProductSummaryAdapter
import com.winapp.saperp.adapter.SelectProductAdapter
import com.winapp.saperp.db.DBHelper
import com.winapp.saperp.model.AppUtils
import com.winapp.saperp.model.CreateInvoiceModel
import com.winapp.saperp.model.CustomerDetails
import com.winapp.saperp.model.CustomerModel
import com.winapp.saperp.model.DeliveryAddressModel
import com.winapp.saperp.model.DuplicateInvoiceDetail
import com.winapp.saperp.model.DuplicateInvoiceModel
import com.winapp.saperp.model.DuplicateSRDetails
import com.winapp.saperp.model.HomePageModel
import com.winapp.saperp.model.InvoicePrintPreviewModel
import com.winapp.saperp.model.ItemGroupList
import com.winapp.saperp.model.ProductSummaryModel
import com.winapp.saperp.model.ProductsModel
import com.winapp.saperp.model.SalesOrderPrintPreviewModel
import com.winapp.saperp.model.SalesOrderPrintPreviewModel.SalesList
import com.winapp.saperp.model.SettingsModel
import com.winapp.saperp.model.UomModel
import com.winapp.saperp.receipts.ReceiptPrintPreviewModel
import com.winapp.saperp.receipts.ReceiptPrintPreviewModel.ReceiptsDetails
import com.winapp.saperp.thermalprinter.PrinterUtils
import com.winapp.saperp.utils.BarCodeScanner
import com.winapp.saperp.utils.CaptureSignatureView
import com.winapp.saperp.utils.Constants
import com.winapp.saperp.utils.FileCompressor
import com.winapp.saperp.utils.ImageUtil
import com.winapp.saperp.utils.LocationTrack
import com.winapp.saperp.utils.SessionManager
import com.winapp.saperp.utils.SettingUtils
import com.winapp.saperp.utils.SharedPreferenceUtil
import com.winapp.saperp.utils.Utils
import com.winapp.saperp.utils.Utils.twoDecimalPoint
import com.winapp.saperp.zebraprinter.TSCPrinter
import com.winapp.saperp.zebraprinter.ZebraPrinterActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects


class CreateNewInvoiceActivity : AppCompatActivity() , OnClickListener {

    private var returnLayout: LinearLayout? = null
    private var showHideButton: ImageView? = null
    private var productSummaryAdapter: NewProductSummaryAdapter? = null
    private val productSummaryList: ArrayList<ProductSummaryModel>? = null
    private var productSummaryView: RecyclerView? = null
    private var companyCode: String? = null
    private var username: String? = null
    private var negativeStockStr: String? = "No"
    private var user: HashMap<String, String>? = null
    private var session: SessionManager? = null
    private var pDialog: SweetAlertDialog? = null
    private var receiptsHeaderDetails: java.util.ArrayList<ReceiptPrintPreviewModel>? = null
    private var receiptsList: ArrayList<ReceiptsDetails> =  ArrayList()

    private var productListView: RecyclerView? = null
    private var selectProductAdapter: SelectProductAdapter? = null
    private var btnCancel: Button? = null
    private var searchProduct: ImageView? = null
    private var productName_bottomEd: EditText? = null
    private var transLayout: LinearLayout? = null
    private var totalProducts: TextView? = null
    private var products: ArrayList<String>? = null
    private var productAutoComplete: AutoCompleteTextView? = null
    private var autoCompleteAdapter: ArrayAdapter<String>? = null
    private var cartonPrice: EditText? = null
    private var loosePrice: EditText? = null
    private var uomText: EditText? = null
    private var pcsPerCarton: EditText? = null
    private var stockCount: EditText? = null
    private var qtyValue: EditText? = null
    private var returnQtyText: EditText? = null
    private var customerNameText: EditText? = null
    private var priceText: EditText? = null
    private var subTotalValue: TextView? = null
    private var taxValueText: TextView? = null
    private var netTotalValue: TextView? = null
    private var customerDetails: ArrayList<CustomerDetails>? = null
    private var dbHelper: DBHelper? = null
    var editTimeStamp: String? = null
    private var taxTitle: TextView? = null
    private var addProduct: Button? = null
    private var productsModel: ProductsModel? = null
    private var itemCount: TextView? = null
    private var noproductText: TextView? = null
    private var productId: String? = null
    private var productEditId: String? = null
    private var productName: String? = null
    var isCartonQtyEdit = false
    var isQtyEdit = false
    var net_total_value = 0.0
    var minimumSellingPriceM = "0.0"
    var valueGreat = false

    private var sharedPreferences: SharedPreferences? = null
    private var sharedPref_billdisc: SharedPreferences? = null
    private var myEdit: SharedPreferences.Editor? = null
    private var sharedPreferenceUtil: SharedPreferenceUtil? = null
    private val qtyTextWatcher: TextWatcher? = null
    private val cartonTextWatcher: TextWatcher? = null
    private val lqtyTextWatcher: TextWatcher? = null
    private val cartonPriceTextWatcher: TextWatcher? = null
    private var loosePriceTextWatcher: TextWatcher? = null
    private var focSwitch: Switch? = null
    private var exchangeSwitch: Switch? = null
    private var returnSwitch: Switch? = null
    var summaryModel = CreateInvoiceModel()
    private var focEditText: EditText? = null
    private var downArrow_inv_img: ImageView? = null
    private var hide_disc_layl: LinearLayout? = null
    private var exchangeEditext: EditText? = null
    private var discountEditext: EditText? = null
    private var item_discount_ed: EditText? = null
    private var discount_layInv: LinearLayout? = null
    private var bill_disc_percent_ed: EditText? = null
    private var bill_disc_amt_ed: EditText? = null
    private var bill_disc_layl: View? = null
    var isBillDiscountTouch = false
    var isBillPercentageTouch = false
    var isreturnTouch = false
    var isUomTouch = false
    protected var billDiscAmountTextWatcher: TextWatcher? = null
    protected var billDiscPercentageTextWatcher: TextWatcher? = null
    var billDiscountPercentage = "0"
    var billDiscountAmount = "0.00"
    private var downarrow_billLayl: ImageView? = null
    private var prod_bottom_layout: LinearLayout? = null
    private var bill_disc_subtotal_txtl: TextView? = null
    private var disc_limit_txtl: TextView? = null
    private var exchange_inv: EditText? = null
    private var discountStr = "0"
    private val exchangeStr = "0"
    var bottomSheetDialog: BottomSheetDialog? = null
    private var returnEditext: EditText? = null
    private var scanProduct: ImageView? = null
    var RESULT_CODE = 12
    private var locationCode: String? = null
    private var stockQtyValue: TextView? = null
    private var stockLayout: LinearLayout? = null
    private var attachmentLayInv: LinearLayout? = null
    var selectImageInv: TextView? = null
    var inv_cash_CollectLayl: LinearLayout? = null
    private val cqtyTW: TextWatcher? = null
    private val lqtyTW: TextWatcher? = null
    private var qtyTW: TextWatcher? = null
    private val beforeLooseQty: String? = null
    private val ss_Cqty: String? = null
    var isAllowLowStock = false
    var isItemFOCApi = "false"
    var isCheckedCreditLimit = false
    private var pdtStockVal: String? = "0.00"
    private var pdtStockEditVal: String? = "0.00"
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_GALLERY_PHOTO = 2
    var mPhotoFile: File? = null
    var mCompressor: FileCompressor? = null
    private var focLayout: LinearLayout? = null
    private var sortButton: FloatingActionButton? = null
    private var emptyLayout: LinearLayout? = null
    private var productLayout: LinearLayout? = null
    private val isViewShown = false
    var selectCustomerId: String? = null
    var isReverseCalculationEnabled = true
    var isCartonPriceEdit = true
    var isPriceEdit = true
    var salesReturn: CheckBox? = null
    var salesReturnText: TextView? = null
    var minimumSellingPriceText: TextView? = null
    private val cancelSheet: ImageView? = null
    private var cancelButton: Button? = null
    private var okButton: Button? = null
    private var alert: AlertDialog? = null
    private var signatureAlert: AlertDialog? = null
    var invoicePrintCheck: CheckBox? = null
    var doInvprintcheck: CheckBox? = null
    var cash_collectedCheck: CheckBox? = null
    var receipt_printCheck: CheckBox? = null
    var emailCheck: CheckBox? = null
    var isCashCollectCheck = false
    var isReceiptPrint = false
    var isInvoicePrint = false
    var isDOPrint = false
    var receiptNoApi: String? = ""
    var companyName: String? = null
    var remarkStr: String? = null
    var billDiscSetAmt_api: String? = "0.00"
    var billDiscSetPercent_api: String? = "0.00"
    var billDisPercent_share_api: String? = "0"
    private var printerMacId: String? = ""
    private var settingUOMInvval: String? = "PCS"
    private var settingUOMSOval: String? = "PCS"
    private var isSettlementByNextDate: String? = "false"
    private var isLastPrice: String? = "false"
    private var shortCodeStr: String? = ""
    private var settingUOM_DOval: String? = "PCS"
    private var printerType: String? = null
    private var pref_bill_disc_amt: String? = "0.00"
    private var pref_bill_disc_percent: String? = "0.00"
    private var remarkText: EditText? = null
    var saveTitle: TextView? = null
    private var saveMessage: TextView? = null
    private var editSoNumber: String? = ""
    private var duplicateInvNo: String? = ""
    private var duplicateInvDate: String? = ""
    private var editDoNumber: String? = ""
    private var orderNo: String? = ""
    private var saleable_editVal: String? = ""
    private var damage_editVal: String? = ""
    var returnAdj: TextView? = null
    var returnLayoutView: LinearLayout? = null
    var cancelReturn: Button? = null
    var saveReturn: Button? = null
    var netReturnQty: TextView? = null
    var expiryReturnQty: EditText? = null
    var damageReturnQty: EditText? = null
    var expiryQtyTextWatcher: TextWatcher? = null
    var returnQtyTextWatcher: TextWatcher? = null
    var damageQtyTextWatcher: TextWatcher? = null
    var invoiceDateInv: TextView? = null
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private val mHour = 0
    private val mMinute = 0
    private var itemGroup: ArrayList<ItemGroupList>? = null
    private var groupspinner: Spinner? = null
    private var currentSaveDateTime: String? = ""
    private var invoiceHeaderDetails: ArrayList<InvoicePrintPreviewModel>? = null
    private var invoicePrintList: ArrayList<InvoicePrintPreviewModel.InvoiceList>? = null
    private var salesReturnList: ArrayList<InvoicePrintPreviewModel.SalesReturnList>? = null
    private var salesOrderHeaderDetails: ArrayList<SalesOrderPrintPreviewModel>? = null

    private var duplicateInvoiceModel: ArrayList<DuplicateInvoiceModel>? = null
    private var duplicateInvoiceDetailList: ArrayList<DuplicateInvoiceDetail>? = null
    private var duplicateInvSRList: ArrayList<DuplicateSRDetails>? = null

    private var salesPrintList: ArrayList<SalesList>? = null
    private val signatureLayout: LinearLayout? = null

    var signatureCapture: ImageView? = null
    private var creditLimitAmount = "0.00"
    private var outstandingAmount = "0.00"
    var isCheckedInvoicePrint = false
    var isCheckedSalesPrint = false
    var isEmailSetting = false
    var isUomSetting = true
    var ischangeUOM = false
    var isDeliveryAddrSetting = false
    var isSignatureSetting = false
    var isDiscountSetting = false
    var isDiscountApiSetting = false
    var minimum_disc_amt = 0.00
    var maximum_disc_amt = 0.00
    var isEditPrice = false

    var settings: ArrayList<SettingsModel>? = null
    var save_btn: MenuItem? = null
    var scannedBarcode: String? = ""

    var down_menu: MenuItem? = null
    var bill_disc_savel: Button? = null
    var b = false
    private var uomSpinner: Spinner? = null
    private var delivery_addrSpinner: Spinner? = null
    private var deliveryAddr_layl: LinearLayout? = null
    private var uomList: ArrayList<UomModel>? = null
    private var uomListEdit: ArrayList<UomModel>? = null
    private var deliveryAddrList: ArrayList<DeliveryAddressModel>? = null
    private val uomCode = ""
    private var uomName = ""
    private var uomPriceEd = ""
    private var deliverAddrNameStr = ""
    var uomTextView: TextView? = null
    private var isEditItem = false
    private var orderNoText: EditText? = null
    private var uomSpinnerLayl: LinearLayout? = null
    private var bill_disclayInvl: LinearLayout? = null
    var duplicateAdapter: DuplicateInvoiceAdapter? = null
    private var customerCodeStr: String? = ""
    private var insertStatusDup: Boolean? = false
    var uomTxtTitl: TextView? = null
    var ed_uomTxtl: TextView? = null
    var billdisc_displayl: TextView? = null
    var uomChangel: TextView? = null
    private var billDiscApiStr: String = "0.00"
    private var isFOCStr: String? = ""
    private var isMailId: String? = ""

    @SuppressLint("LogNotTimber", "ClickableViewAccessibility", "SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_invoice)

        supportActionBar!!.title = "Invoice"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        session = SessionManager(this)
        user = session!!.userDetails
        dbHelper = DBHelper(this)
        progressDialog = ProgressDialog(this)
        mCompressor = FileCompressor(this)
        sharedPreferenceUtil = SharedPreferenceUtil(this)

        companyCode = user!!.get(SessionManager.KEY_COMPANY_CODE)
        companyName = user!!.get(SessionManager.KEY_COMPANY_NAME)
        username = user!!.get(SessionManager.KEY_USER_NAME)
        negativeStockStr = user!!.get(SessionManager.KEY_NEGATIVE_STOCK)
        locationCode = user!!.get(SessionManager.KEY_LOCATION_CODE)

        signatureString = ""

        returnLayout = findViewById(R.id.return_layout)
        showHideButton = findViewById(R.id.show_hide)
        productSummaryView = findViewById(R.id.product_summary)
        productListView = findViewById(R.id.productList)
        btnCancel = findViewById(R.id.cancel_sheet)
        searchProduct = findViewById(R.id.search_product)
        productName_bottomEd = findViewById(R.id.product_search)
        transLayout = findViewById(R.id.trans_layout)
        totalProducts = findViewById(R.id.total_products)
        productAutoComplete = findViewById(R.id.product_name)
        groupspinner = findViewById(R.id.spinner_group)
        cartonPrice = findViewById(R.id.carton_price)
        priceText = findViewById(R.id.price)
        loosePrice = findViewById(R.id.loose_price)
        pcsPerCarton = findViewById(R.id.pcs_per_ctn)
        uomText = findViewById(R.id.uom)
        uomTxtTitl = findViewById(R.id.uomTxtTitle)
        stockCount = findViewById(R.id.stock_count)
        qtyValue = findViewById(R.id.qty)
        subTotalValue = findViewById(R.id.balance_valueInv)
        taxValueText = findViewById(R.id.taxInv)
        netTotalValue = findViewById(R.id.net_totalInv)
        taxTitle = findViewById(R.id.tax_title)
        taxTitle = findViewById(R.id.tax_title)
        addProduct = findViewById(R.id.add_product)
        itemCount = findViewById(R.id.item_count)
        noproductText = findViewById(R.id.no_product_text)
        focSwitch = findViewById(R.id.foc_switch)
        exchangeSwitch = findViewById(R.id.exchange_switch)
        returnSwitch = findViewById(R.id.return_switch)
        focEditText = findViewById(R.id.foc)
        exchangeEditext = findViewById(R.id.exchange_text)
        returnEditext = findViewById(R.id.return_text)
        discountEditext = findViewById(R.id.discount_text)
        scanProduct = findViewById(R.id.scan_product)
        stockQtyValue = findViewById(R.id.stock_qty)
        stockLayout = findViewById(R.id.stock_layout)
        focLayout = findViewById(R.id.foc_layout)
        emptyLayout = findViewById(R.id.empty_layout)
        sortButton = findViewById(R.id.fab)
        productLayout = findViewById(R.id.product_layout)
        salesReturn = findViewById(R.id.sales_return)
        salesReturnText = findViewById(R.id.sales_return_text)
        minimumSellingPriceText = findViewById(R.id.minimum_selling_price)
        barcodeText = findViewById(R.id.barcode_textInv)
        barCodeLayl = findViewById(R.id.barCodeLay)

        returnQtyText = findViewById(R.id.return_qty)
        customerNameText = findViewById(R.id.customer_name_text)
        remarkText = findViewById(R.id.remarks)
        returnAdj = findViewById(R.id.return_adj)
        returnLayoutView = findViewById(R.id.return_layout_view)
        cancelReturn = findViewById(R.id.cancel_return)
        saveReturn = findViewById(R.id.save_return)
        netReturnQty = findViewById(R.id.net_return_qty)
        expiryReturnQty = findViewById(R.id.saleable_return_qty)
        damageReturnQty = findViewById(R.id.damage_qty)
        invoiceDateInv = findViewById(R.id.invoice_date_inv)
        uomSpinner = findViewById(R.id.uom_spinner)
        delivery_addrSpinner = findViewById(R.id.delivery_addr_spinner)
        deliveryAddr_layl = findViewById(R.id.deliveryAddr_lay)
        uomTextView = findViewById(R.id.uom_txt)
        orderNoText = findViewById(R.id.order_no)
        uomChangel = findViewById(R.id.uomChange)
        ed_uomTxtl = findViewById(R.id.ed_uomTxt)
        billdisc_displayl = findViewById(R.id.billdisc_display)
        bill_disclayInvl = findViewById(R.id.bill_disclayInv)
        bill_disc_savel = findViewById(R.id.bill_disc_save)
        //signatureLayout=findViewById(R.id.signature_layout);
        uomSpinnerLayl = findViewById(R.id.uomSpinnerLay)
        downArrow_inv_img = findViewById(R.id.downArrow_inv_lay)
        hide_disc_layl = findViewById(R.id.hide_disc_lay)
        item_discount_ed = findViewById(R.id.discount_invoice)
        exchange_inv = findViewById(R.id.exchange_invoice)
        discount_layInv = findViewById(R.id.discount_layInv)
        disc_limit_txtl = findViewById(R.id.disc_limit_txt)
        bill_disc_layl = findViewById(R.id.bill_discLay_Inv)
        downarrow_billLayl = findViewById(R.id.downarrow_billLay)
        prod_bottom_layout = findViewById(R.id.prod_bottom_layout)
        bill_disc_percent_ed = findViewById(R.id.bill_disc_percent_inv)
        bill_disc_amt_ed = findViewById(R.id.bill_disc_amt_inv)
        bill_disc_subtotal_txtl = findViewById(R.id.bill_disc_subtotal_txt)

        bill_disc_savel!!.setOnClickListener(this);
        settings = ArrayList()
        settings = dbHelper!!.settings
        Log.w("compcode..", "" + companyCode)
        Log.w(
            "salesmann..", "" + user!!.get(SessionManager.KEY_SALESMAN_NAME) + ".." +
                    user!!.get(SessionManager.KEY_NEGATIVE_STOCK)
        );

        products = ArrayList()
        productAutoComplete!!.clearFocus()
        imageString = ""
      //  barcodeText!!.requestFocus()
        getCurrentLocation()

        sharedPref_billdisc = getSharedPreferences("BillDiscPref", MODE_PRIVATE)
        myEdit = sharedPref_billdisc!!.edit()

        pref_bill_disc_amt = sharedPref_billdisc!!.getString("billDisc_amt", "")
        pref_bill_disc_percent = sharedPref_billdisc!!.getString("billDisc_percent", "")

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE)
        printerType = sharedPreferences!!.getString("printer_type", "")
        printerMacId = sharedPreferences!!.getString("mac_address", "")
        Log.w("macIdd..",""+printerMacId +"type.."+printerType)
        Log.w("billDiscInvoice..",""+pref_bill_disc_percent)

        settingUOMInvval = sharedPreferenceUtil!!.getStringPreference(sharedPreferenceUtil!!.KEY_SETTING_INV_UOM,
            "")
        settingUOMSOval = sharedPreferenceUtil!!.getStringPreference(sharedPreferenceUtil!!.KEY_SETTING_SO_UOM,
            "")
        isSettlementByNextDate = sharedPreferenceUtil!!.getStringPreference(sharedPreferenceUtil!!
            .KEY_SETTLEMENT_NEXT_DATE,"")

        shortCodeStr = sharedPreferenceUtil!!.getStringPreference(sharedPreferenceUtil!!
            .KEY_SHORT_CODE,"")

        isLastPrice = sharedPreferenceUtil!!.getStringPreference(sharedPreferenceUtil!!
            .KEY_LAST_PRICE,"")

        // else if (model.settingName == "HAVESETTLEMENTBYDATE") {
//            Log.w("SettingNamedate:", model.settingName)
//            Log.w("SettingValuedate:", model.settingValue)
//            if (model.settingValue.equals("True", ignoreCase = true)) {
//                isSettlementByNextDate = true
//            } else {
//                isSettlementByNextDate = false
//            }
//        }


            Log.w("settingUOMinv..",""+settingUOMInvval+"lastPrice.."+isLastPrice)
        Log.w("settingUOMso..",""+settingUOMSOval+"shortcodd.."+shortCodeStr)

        productAutoComplete!!.setEnabled(false)
        item_discount_ed!!.isEnabled = false
        exchange_inv!!.isEnabled = false

            downArrow_inv_img!!.setOnClickListener(View.OnClickListener {
            if (downArrow_inv_img!!.getTag() == "hide") {
                hide_disc_layl!!.setVisibility(View.VISIBLE)
                downArrow_inv_img!!.setTag("show")
                Utils.slideUp(hide_disc_layl)
                downArrow_inv_img!!.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            } else {
                Utils.slideDown(hide_disc_layl)
                hide_disc_layl!!.setVisibility(View.GONE)
                downArrow_inv_img!!.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                downArrow_inv_img!!.setTag("hide")
            }
        })
        val printerUtils = PrinterUtils(this, printerMacId)
        //  printerUtils.connectPrinter();
        focEditText!!.setEnabled(false)
        exchangeEditext!!.setEnabled(false)
        discountEditext!!.setEnabled(false)
        returnEditext!!.setEnabled(false)
        //        qtyValue.setEnabled(false);
//        priceText.setEnabled(false);
        val c = Calendar.getInstance().time
        println("Current time => $c")
        val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        currentDate = df1.format(c)
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = df.format(c)
        invoiceDateInv!!.setText(formattedDate)
        if (intent != null) {
            customerNameText!!.setText(intent.getStringExtra("customerName"))
            customerCode = intent.getStringExtra("customerCode")
            activityFrom = intent.getStringExtra("from")
            editSoNumber = intent.getStringExtra("editSoNumber")
            duplicateInvNo = intent.getStringExtra("duplicateInvNo")
            duplicateInvDate = intent.getStringExtra("duplicateInvDate")

            customerCodeStr = intent.getStringExtra("customerDupCode")
            isFOCStr = intent.getStringExtra("isFOC")
            if(intent.getStringExtra("isMailId") != null &&
                !intent.getStringExtra("isMailId").equals(null) &&
                !intent.getStringExtra("isMailId").equals("")){
                isMailId = intent.getStringExtra("isMailId")
            }

            if (intent.getStringExtra("customerBillDisc") != null) {
                billDiscApiStr = intent.getStringExtra("customerBillDisc")!!
                val amount: Double = percentageToAmount(billDiscApiStr.toDouble())
                myEdit!!.putString("billDisc_percent", billDiscApiStr)
                myEdit!!.putString("billDisc_amt", amount.toString())
                myEdit!!.apply()
                billdisc_displayl!!.setText(billDiscApiStr)
                Log.w("billdiscpi_inv::", billDiscApiStr!!+amount)

//                val subtotal: Double = getSubtotal()
//                if (netTotalValue!!.text.toString().toDouble() > 0.00) {
//                    val amount: Double = percentageToAmount(billDiscApiStr!!.toDouble())
//
//                    val net_subtotal: Double =
//                        subtotal - amount
//                    setCalculationSummaryView(net_subtotal)
//                }

                pref_bill_disc_percent = sharedPref_billdisc!!.getString("billDisc_percent", "")
                pref_bill_disc_amt = sharedPref_billdisc!!.getString("billDisc_amt", "")

            }

            currentSaveDateTime = intent.getStringExtra("currentDateTime")
            editDoNumber = intent.getStringExtra("editDoNumber")
            orderNo = intent.getStringExtra("orderNo")
            Log.w("GivenActivityFrom::", activityFrom.toString())
            Log.w("cust_Code_Name::", customerCode+".."+intent.getStringExtra("customerName"))

            if (activityFrom == "iv") {
                supportActionBar!!.setTitle("Invoice")
             //   priceText!!.setEnabled(true)
                downArrow_inv_img!!.setVisibility(View.VISIBLE)
                exchange_inv!!.visibility = View.VISIBLE
                bill_disclayInvl!!.visibility = View.VISIBLE

            } else if (activityFrom == "ConvertInvoice") {
               // priceText!!.setEnabled(true)
                orderNoText!!.setText(orderNo)
                downArrow_inv_img!!.setVisibility(View.VISIBLE)
                supportActionBar!!.setTitle("Covert Invoice -$editSoNumber")
                exchange_inv!!.visibility = View.VISIBLE
                bill_disclayInvl!!.visibility = View.VISIBLE

            }
            else if (activityFrom == "ConvertInvoiceFromDO") {
              //  priceText!!.setEnabled(true)
                orderNoText!!.setText(orderNo)
                downArrow_inv_img!!.setVisibility(View.VISIBLE)
                supportActionBar!!.setTitle("Covert Invoice -$editDoNumber")
                exchangeEditext!!.visibility = View.VISIBLE

                bill_disc_amt_ed!!.setText(pref_bill_disc_amt)
                bill_disc_percent_ed!!.setText(pref_bill_disc_percent)
                bill_disclayInvl!!.visibility = View.VISIBLE
               // billdisc_displayl!!.setText(pref_bill_disc_percent)

                Log.w("billDiscDO",""+pref_bill_disc_amt)

            }
            else if (activityFrom == "SalesEdit") {
              //  priceText!!.setEnabled(true)
                orderNoText!!.setText(orderNo)
                returnQtyText!!.setVisibility(View.GONE)
                supportActionBar!!.setTitle("SalesOrder Edit")
                exchange_inv!!.visibility = View.VISIBLE

            } else if (activityFrom == "so") {
              //  priceText!!.setEnabled(true)
                returnQtyText!!.setVisibility(View.GONE)
                supportActionBar!!.setTitle("SalesOrder")
                exchange_inv!!.visibility = View.VISIBLE

            } else if (activityFrom == "do") {
             //   priceText!!.setEnabled(true)
                returnQtyText!!.setVisibility(View.GONE)
                returnAdj!!.setVisibility(View.GONE)
                supportActionBar!!.setTitle("Delivery Order")
                exchange_inv!!.visibility = View.GONE

            } else if (activityFrom == "doEdit") {
              //  priceText!!.setEnabled(true)
                orderNoText!!.setText(orderNo)
                returnQtyText!!.setVisibility(View.GONE)
                returnAdj!!.setVisibility(View.GONE)
                supportActionBar!!.setTitle("Delivery Order Edit")
                exchange_inv!!.visibility = View.GONE

            } else if (activityFrom == "Duplicate") {
               // priceText!!.setEnabled(true)
                downArrow_inv_img!!.setVisibility(View.VISIBLE)

                bill_disc_amt_ed!!.setText(pref_bill_disc_amt)
                bill_disc_percent_ed!!.setText(pref_bill_disc_percent)
                Log.w("billDiscDupli",""+pref_bill_disc_amt)
                bill_disclayInvl!!.visibility = View.VISIBLE

                exchange_inv!!.visibility = View.VISIBLE
                //  getDuplicateInvoiceDetails(duplicateInvNo!!)
                // orderNoText.setText(orderNo);
//                returnQtyText.setVisibility(View.GONE);
//                returnAdj.setVisibility(View.GONE);
                supportActionBar!!.setTitle("Invoice - $duplicateInvNo")
            }
            else if (activityFrom == "ReOrderSales") {
              //  priceText!!.setEnabled(true)
               // downArrow_inv_img!!.setVisibility(View.VISIBLE)
               supportActionBar!!.setTitle("Re-SalesOrders ")
                //Objects.requireNonNull(supportActionBar)!!.title = "Re-" + R.string.sales_orders
                returnQtyText!!.setVisibility(View.GONE)
                exchange_inv!!.visibility = View.VISIBLE
            }
            else if (activityFrom == "ReOrderInvoice") {
               // priceText!!.setEnabled(true)
                downArrow_inv_img!!.setVisibility(View.VISIBLE)
                supportActionBar!!.setTitle("Re-Invoices ")
                //Objects.requireNonNull(supportActionBar)!!.title = "Re-" + R.string.invoices

                bill_disc_amt_ed!!.setText(pref_bill_disc_amt)
                bill_disc_percent_ed!!.setText(pref_bill_disc_percent)
                Log.w("billDiscDupliz",""+pref_bill_disc_amt)
                bill_disclayInvl!!.visibility = View.VISIBLE

                exchange_inv!!.visibility = View.VISIBLE
                //  getDuplicateInvoiceDetails(duplicateInvNo!!)
                // orderNoText.setText(orderNo);
//                returnQtyText.setVisibility(View.GONE);
//                returnAdj.setVisibility(View.GONE);
            }

            Log.w("isFOCStrvv",""+isFOCStr+isMailId)
        } else {
            orderNoText!!.setText("")
        }
        val settings = dbHelper!!.settings
        if (settings != null) {
            if (settings.size > 0) {
                for (model in settings) {
                    if (model.settingName == "invSwitch") {
                        Log.w("SettingNameI:", model.settingName)
                        Log.w("SettingValueI:", model.settingValue)
                        isCheckedInvoicePrint = if (model.settingValue == "1") {
                            true
                        } else {
                            false
                        }
                    } else if (model.settingName == "salesSwitch") {
                        Log.w("SettingNameI:", model.settingName)
                        Log.w("SettingValueI:", model.settingValue)
                        isCheckedSalesPrint = if (model.settingValue == "1") {
                            true
                        } else {
                            false
                        }
                    } else if (model.settingName == "sentEmailSwitch") {
                        Log.w("SettingNameI:", model.settingName)
                        Log.w("SettingValueI:", model.settingValue)
                        isEmailSetting = if (model.settingValue == "1") {
                            true
                        } else {
                            false
                        }
                    } else if (model.settingName == "UomSwitch") {
                        Log.w("SettingNameI:", model.settingName)
                        Log.w("SettingValueI:", model.settingValue)
                        isUomSetting = if (model.settingValue == "1") {
                            true
                        } else {
                            true
                        }
                    } else if (model.settingName == "deliveryAddressSwitch") {
                        Log.w("SettingNameI:", model.settingName)
                        Timber.w(model.settingValue)
                        isDeliveryAddrSetting = if (model.settingValue == "1") {
                            true
                        } else {
                            false
                        }
                    } else if (model.settingName == "discountSwitch") {
                        Log.w("SettingNameDis:", model.settingName)
                        Log.w("SettingValueDis:", model.settingValue)
                        isDiscountSetting = if (model.settingValue == "1") {
                            true
                        } else {
                            false
                        }
                    } else if (model.settingName == "signatureSwitch") {
                        Log.w("SettingNameSign:", model.settingName)
                        Log.w("SettingValueSign:", model.settingValue)
                        isSignatureSetting = if (model.settingValue == "1") {
                            true
                        } else {
                            false
                        }
                    } else if (model.settingName == "showDiscountAmount") {
                        Log.w("SettingNamedisc:", model.settingName)
                        Log.w("SettingValuedisc:", model.settingValue)
                        if (model.settingValue.equals("True", ignoreCase = true)) {
                            isDiscountApiSetting = true
                        } else {
                            isDiscountApiSetting = false
                        }
                    } else if (model.settingName == "discountAmountValidationFrom") {
                        Log.w("SettingNamediscmini:", model.settingName)
                        Log.w("SettingValuediscmini:", model.settingValue)
                        minimum_disc_amt = model.settingValue.toDouble()
                    } else if (model.settingName == "discountAmountValidationTo") {
                        Log.w("SettingNamediscmax:", model.settingName)
                        Log.w("SettingValuediscmax:", model.settingValue)
                        maximum_disc_amt = model.settingValue.toDouble()
                    }  else if (model.settingName == "haveEditPrice") {
                        Log.w("SettingName_edPrice:", model.settingName)
                        Log.w("SettingValue_edPrice:", model.settingValue)
                        if (model.settingValue.equals("True", ignoreCase = true)) {
                            isEditPrice = true
                        } else {
                            isEditPrice = false
                        }
                    }
                }
            }
        }
        Log.w("settingValdiss", "$maximum_disc_amt  .. $minimum_disc_amt" + isDiscountApiSetting)

        Log.w("settingVal", "$isDiscountSetting  .. $isSignatureSetting")
        if (activityFrom == "iv" || activityFrom == "ConvertInvoice"
            || activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
            || activityFrom == "ReOrderInvoice" ) {
            downArrow_inv_img!!.setVisibility(View.VISIBLE)
            if (isDiscountSetting) {
                discount_layInv!!.setVisibility(View.VISIBLE)
            } else {
                discount_layInv!!.setVisibility(View.GONE)
            }
        }
        if (isDiscountApiSetting) {
//            disc_limit_txtl!!.setText("Mini value "+minimum_disc_amt+" to maxi value "+maximum_disc_amt)
            disc_limit_txtl!!.setText("Discount value " + minimum_disc_amt + " to " + maximum_disc_amt)

        } else {
            item_discount_ed!!.isEnabled = false
            disc_limit_txtl!!.setText("No discount")
        }
        if (isUomSetting) {
            uomSpinnerLayl!!.setVisibility(View.VISIBLE)
            uomTxtTitl!!.visibility = View.VISIBLE
        } else {
            uomSpinnerLayl!!.setVisibility(View.GONE)
            uomTxtTitl!!.visibility = View.INVISIBLE
        }
        if (isDeliveryAddrSetting) {
            deliveryAddr_layl!!.setVisibility(View.VISIBLE)
        } else {
            deliveryAddr_layl!!.setVisibility(View.GONE)
        }
        if(isEditPrice){
            priceText!!.isEnabled = true
        }
        else{
            priceText!!.isEnabled = false
        }

        /* if (AddInvoiceActivity.invoice_delivery_date!=null && !AddInvoiceActivity.invoice_delivery_date.isEmpty()){
                DateFormat inputFormat;
                if (isDateFormat(AddInvoiceActivity.invoice_delivery_date)){
                    inputFormat = new SimpleDateFormat("dd/MM/yyyy");
                }else {
                    inputFormat = new SimpleDateFormat("dd-MM-yyyy");
                }
                SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String doDate = df1.format(inputFormat.parse(AddInvoiceActivity.invoice_delivery_date));
                dueDateEdittext.setText(doDate);
            }else if (Utils.getDeliveryDate()!=null && !Utils.getDeliveryDate().isEmpty()){
                dueDateEdittext.setText(Utils.getDeliveryDate());
            } else {
                Utils.setDeliveryDate(currentDate);
                dueDateEdittext.setText(currentDate);
            }*/
        if(!shortCodeStr.equals("TRAN")) {
            if (isSettlementByNextDate.equals("True", ignoreCase = true)) {
                try {
                    getSettlementDateApi()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        Log.w("settleInvDate",""+isSettlementByNextDate)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("User", username)
            jsonObject.put("CardCode", customerCode)
            jsonObject.put("LocationCode", locationCode)

            getAllProducts(jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.w("Erroraa:", Objects.requireNonNull(e.message!!))
        }
        val jsonObj = JSONObject()
        try {
            jsonObj.put("CustomerCode", customerCode)
            getDeliveryAddress(jsonObj)
        } catch (e: JSONException) {
            throw RuntimeException(e)
            Log.w("Errorw:", Objects.requireNonNull(e.message!!))
        }
        try {
            grouplist
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.w("Errorp:", Objects.requireNonNull(e.message!!))
        }
        val sharedPreferences = getSharedPreferences("customerPref", MODE_PRIVATE)
        selectCustomerId = sharedPreferences.getString("customerId", "")
        if (selectCustomerId != null && !selectCustomerId!!.isEmpty()) {
            customerDetails = dbHelper!!.getCustomer(selectCustomerId)
            getCustomerDetails(selectCustomerId, false, "")
        }
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet.setBackgroundColor(Color.parseColor("#F2F2F2"))
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
                        transLayout!!.setVisibility(View.VISIBLE)
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED")
                        transLayout!!.setVisibility(View.GONE)
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

        /*  signatureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignatureAlert();
            }
        });*/btnCancel!!.setOnClickListener(View.OnClickListener { viewCloseBottomSheet() })
        searchProduct!!.setOnClickListener(View.OnClickListener { viewCloseBottomSheet() })

        downarrow_billLayl!!.setOnClickListener {
//            bill_disc_amt_ed!!.setText(pref_bill_disc_amt)
//            bill_disc_percent_ed!!.setText(pref_bill_disc_percent)

            billDischide()
        }

        bill_disc_amt_ed!!.setOnTouchListener(View.OnTouchListener
        { view, motionEvent ->
            isBillDiscountTouch = true
            isBillPercentageTouch = false
            bill_disc_percent_ed!!.removeTextChangedListener(billDiscPercentageTextWatcher)
            bill_disc_amt_ed!!.addTextChangedListener(billDiscAmountTextWatcher)
            false
        })
//
//
        bill_disc_percent_ed!!.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            isBillPercentageTouch = true
            isBillDiscountTouch = false
            bill_disc_amt_ed!!.removeTextChangedListener(billDiscAmountTextWatcher)
            bill_disc_percent_ed!!.addTextChangedListener(billDiscPercentageTextWatcher)
            false
        })


        billDiscAmountTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {

                val subtotal: Double = getSubtotal()
                Log.w("SubTotalAmtdisc:", subtotal.toString() + "")
                if (isBillDiscountTouch) {
                    if (!editable.toString().isEmpty()) {
                        if (editable.toString() == ".") {
                            bill_disc_amt_ed!!.setText("0.")
                            bill_disc_amt_ed!!.setSelection(bill_disc_amt_ed!!.getText().length)
                            setPercentageAmount("0".toDouble())
                            val net_subtotal = subtotal - 0
                            setCalculationSummaryView(net_subtotal)
                            billDiscPref()
                        } else {
                                setPercentageAmount(editable.toString().toDouble())
                                val net_subtotal = subtotal - editable.toString().toDouble()
                                setCalculationSummaryView(net_subtotal)
                                billDiscPref()
                        }
                    } else {
                        setPercentageAmount(0.0)
                        setCalculationSummaryView(subtotal)
                        billDiscPref()
                    }
                }
            }
        }
        bill_disc_amt_ed!!.addTextChangedListener(billDiscAmountTextWatcher)


        billDiscPercentageTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }
            override fun afterTextChanged(editable: Editable) {

                val subtotal: Double = getSubtotal()
                if (isBillPercentageTouch) {
                    Log.w("PercentageAff:", "")
                    if (!editable.toString().isEmpty()) {
                        if (editable.toString() == ".") {
                            bill_disc_percent_ed!!.setText("0.")
                            bill_disc_percent_ed!!.setSelection( bill_disc_percent_ed!!.getText().length)
                            billdisc_displayl!!.setText("0.");

                            editable.toString().toDouble()
                            val amount: Double = percentageToAmount("0".toDouble())
                            bill_disc_amt_ed!!.removeTextChangedListener(billDiscAmountTextWatcher)
                            bill_disc_amt_ed!!.setText(twoDecimalPoint(amount) + "")

                            bill_disc_amt_ed!!.addTextChangedListener(billDiscAmountTextWatcher)
                            val net_subtotal: Double =
                                subtotal - bill_disc_amt_ed!!.getText().toString().toDouble()

                                setCalculationSummaryView(net_subtotal)
                                billDiscPref()
                        } else {
                                val amount: Double =
                                    percentageToAmount(editable.toString().toDouble())
                                billdisc_displayl!!.setText(editable.toString())

                                Log.w("PercentageAmtdisc:", amount.toString() + "")
                                bill_disc_amt_ed!!.removeTextChangedListener(
                                    billDiscAmountTextWatcher
                                )
                                bill_disc_amt_ed!!.setText(twoDecimalPoint(amount) + "")

                                bill_disc_amt_ed!!.addTextChangedListener(billDiscAmountTextWatcher)
                                val net_subtotal: Double =
                                    subtotal - bill_disc_amt_ed!!.getText().toString().toDouble()
                                setCalculationSummaryView(net_subtotal)
                                billDiscPref()
                        }
                    } else {
                        bill_disc_amt_ed!!.removeTextChangedListener(billDiscAmountTextWatcher)
                        bill_disc_amt_ed!!.setText("")
                        billdisc_displayl!!.setText("0.0")
                        bill_disc_amt_ed!!.addTextChangedListener(billDiscAmountTextWatcher)
                        setCalculationSummaryView(subtotal)
                        billDiscPref()
                    }
                }
            }
        }
        bill_disc_percent_ed!!.addTextChangedListener(billDiscPercentageTextWatcher)

        exchangeTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val disc = editable.toString()
                if (editable.toString() == ".") {
                    item_discount_ed!!.setText("0.")
                }
                else {
                    if (!disc.isEmpty()) {
                        addProduct!!.setAlpha(0.9f)
                        addProduct!!.setEnabled(true)
                        if (editable.toString() == ".") {
                            exchange_inv!!.removeTextChangedListener(exchangeTextWatcher)
                            exchange_inv!!.setText("0.")
                            exchange_inv!!.setSelection(exchange_inv!!.getText().length)
                            exchange_inv!!.addTextChangedListener(exchangeTextWatcher)
                        }
                    } else {
                        addProduct!!.setAlpha(0.9f)
                        addProduct!!.setEnabled(true)
                        exchange_inv!!.removeTextChangedListener(exchangeTextWatcher)
                        exchange_inv!!.setText("")
                        exchange_inv!!.setSelection( exchange_inv!!.getText().length)
                        exchange_inv!!.addTextChangedListener(exchangeTextWatcher)
                    }
                }
            }
        }
        exchange_inv!!.addTextChangedListener(exchangeTextWatcher)


        discTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val disc = editable.toString()
                if (editable.toString() == ".") {
                    item_discount_ed!!.setText("0.")
                }
                else {
                    if (!disc.isEmpty()) {
                        Log.w("diszz", "")
                        if (isInRange(minimum_disc_amt, maximum_disc_amt, disc.toDouble())) {
                            discountStr = item_discount_ed!!.getText().toString()
                        } else {
                            item_discount_ed!!.removeTextChangedListener(discTextWatcher)
                            item_discount_ed!!.setText("")
                            discountStr = "0"
                            item_discount_ed!!.addTextChangedListener(discTextWatcher)

                            //   Toast.makeText(getApplicationContext(), "Minimum discount amonut $1 !", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        item_discount_ed!!.removeTextChangedListener(discTextWatcher)
                        item_discount_ed!!.setText("")
                        discountStr = "0"
                        item_discount_ed!!.addTextChangedListener(discTextWatcher)

                        //   Toast.makeText(getApplicationContext(), "Minimum discount amonut $1 !", Toast.LENGTH_SHORT).show();
                    }
                    setCalculationView()
                }
            }
        }
        if (item_discount_ed!!.isFocusable()) {
            item_discount_ed!!.addTextChangedListener(discTextWatcher)
        }
        productName_bottomEd!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val product = editable.toString()
                if (!product.isEmpty()) {
                    filter(product)
                } else {
                    setAdapter(AppUtils.getProductsList())
                }
            }
        })

        returnQtyText!!.setOnTouchListener(View.OnTouchListener
        { view, motionEvent ->
            isreturnTouch = true
            returnQtyText!!.addTextChangedListener(billDiscAmountTextWatcher)
            false
        })


        returnQtyText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (isreturnTouch) {
                    Log.w("entyrett","")
                    setCalculationView()
                    if (!editable.toString().isEmpty()) {
                        expiryReturnQty!!.setText(editable.toString())
                    } else {
                        expiryReturnQty!!.setText("")
                    }
                }
                else{
                    expiryReturnQty!!.setText(saleable_editVal)
                    damageReturnQty!!.setText(damage_editVal)
                }
            }
        })


//        returnQtyTextWatcher = object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun afterTextChanged(editable: Editable) {
//                setCalculationView()
//                if (!editable.toString().isEmpty()) {
//                    expiryReturnQty!!.removeTextChangedListener(expiryQtyTextWatcher)
//                    expiryReturnQty!!.setText(editable.toString())
//                    expiryReturnQty!!.addTextChangedListener(expiryQtyTextWatcher)
//                } else {
//                    expiryReturnQty!!.removeTextChangedListener(expiryQtyTextWatcher)
//                    expiryReturnQty!!.setText("")
//                    expiryReturnQty!!.addTextChangedListener(expiryQtyTextWatcher)                }
//            }
//        }
//        returnEditext!!.addTextChangedListener(returnQtyTextWatcher)


        expiryQtyTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!returnQtyText!!.getText().toString().isEmpty()) {
                    val netreturnqty = returnQtyText!!.getText().toString()
                    val net_qty = netreturnqty.toDouble()
                    // net 10 damag 5
                    if (!s.toString().isEmpty()) {
                        val damageqty = s.toString().toDouble()
                        if (net_qty > damageqty) {
                            //10 >5
                            val value = net_qty - damageqty   // 5
                            damageReturnQty!!.removeTextChangedListener(damageQtyTextWatcher)
                            damageReturnQty!!.setText(value.toString())
                            damageReturnQty!!.addTextChangedListener(damageQtyTextWatcher)
                        } else if (net_qty == damageqty) { //10 == 10
                            damageReturnQty!!.removeTextChangedListener(damageQtyTextWatcher)
                            damageReturnQty!!.setText("0")
                            damageReturnQty!!.addTextChangedListener(damageQtyTextWatcher)
                        } else if (net_qty < damageqty) { //10<5
                            expiryReturnQty!!.setText("0")
                            damageReturnQty!!.removeTextChangedListener(damageQtyTextWatcher)
                            damageReturnQty!!.setText(netreturnqty)
                            damageReturnQty!!.addTextChangedListener(damageQtyTextWatcher)
                            Toast.makeText(
                                applicationContext,
                                "Return Qty Exceed...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        damageReturnQty!!.removeTextChangedListener(damageQtyTextWatcher)
                        damageReturnQty!!.setText(netreturnqty)
                        damageReturnQty!!.addTextChangedListener(damageQtyTextWatcher)
                    }
                }
            }
        }
        expiryReturnQty!!.addTextChangedListener(expiryQtyTextWatcher)

        /*  expiryReturnQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!returnQtyText.getText().toString().isEmpty()){
                    String netreturnqty=returnQtyText.getText().toString();
                    int net_qty=Integer.parseInt(netreturnqty);
                    if(!s.toString().isEmpty()){
                        int damageqty=Integer.parseInt(s.toString());
                        if (net_qty > damageqty){
                            int value=net_qty-damageqty;
                            damageReturnQty.setText(String.valueOf(value));
                        }else if (net_qty==damageqty){
                            damageReturnQty.setText("0");
                        }else if (net_qty < damageqty){
                            expiryReturnQty.setText("");
                            damageReturnQty.setText(netreturnqty);
                            Toast.makeText(getApplicationContext(),"Return Qty Exceed...",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        damageReturnQty.setText(netreturnqty);
                    }
                }
            }
        });*/damageQtyTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!returnQtyText!!.getText().toString().isEmpty()) {
                    val netreturnqty = returnQtyText!!.getText().toString()
                    val net_qty = netreturnqty.toDouble()
                    if (!s.toString().isEmpty()) {
                        val damageqty = s.toString().toDouble()
                        if (net_qty > damageqty) {
                            val value = net_qty - damageqty
                            expiryReturnQty!!.removeTextChangedListener(expiryQtyTextWatcher)
                            expiryReturnQty!!.setText(value.toString())
                            expiryReturnQty!!.addTextChangedListener(expiryQtyTextWatcher)
                        } else if (net_qty.toDouble() == damageqty) {
                            expiryReturnQty!!.removeTextChangedListener(expiryQtyTextWatcher)
                            expiryReturnQty!!.setText("0")
                            expiryReturnQty!!.addTextChangedListener(expiryQtyTextWatcher)
                        } else if (net_qty < damageqty) {
                            damageReturnQty!!.setText("0")
                            expiryReturnQty!!.removeTextChangedListener(expiryQtyTextWatcher)
                            expiryReturnQty!!.setText(netreturnqty)
                            expiryReturnQty!!.addTextChangedListener(expiryQtyTextWatcher)
                            Toast.makeText(
                                applicationContext,
                                "Return Qty Exceed...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        expiryReturnQty!!.removeTextChangedListener(expiryQtyTextWatcher)
                        expiryReturnQty!!.setText(netreturnqty)
                        expiryReturnQty!!.addTextChangedListener(expiryQtyTextWatcher)
                    }
                }
            }
        }
        damageReturnQty!!.addTextChangedListener(damageQtyTextWatcher)


        /*        damageReturnQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!returnQtyText.getText().toString().isEmpty()){
                    String netreturnqty=returnQtyText.getText().toString();
                    int net_qty=Integer.parseInt(netreturnqty);
                    if(!s.toString().isEmpty()){
                        int damageqty=Integer.parseInt(s.toString());
                        if (net_qty > damageqty){
                            int value=net_qty-damageqty;
                            expiryReturnQty.setText(String.valueOf(value));
                        }else if (net_qty==damageqty){
                            expiryReturnQty.setText("0");
                        }else if (net_qty < damageqty){
                            damageReturnQty.setText("");
                            expiryReturnQty.setText(netreturnqty);
                            Toast.makeText(getApplicationContext(),"Return Qty Exceed...",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        expiryReturnQty.setText(netreturnqty);
                    }
                }
            }
        });*/

        loosePriceTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                setCalculationView()
            }
        }
        priceText!!.addTextChangedListener(loosePriceTextWatcher)

        qtyTW = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                lowStockSetting
                setCalculationView()
                if (companyCode == "SUPERSTAR TRADERS PTE LTD") {
                    if (activityFrom == "iv" || activityFrom == "so" || activityFrom == "SalesEdit" ||
                        activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
                        || activityFrom == "ReOrderInvoice" || activityFrom == "ReOrderSales"
                    ) {
                        //  if (!isAllowLowStock){
                        //negativeStock no only stock check
                        if (negativeStockStr.equals("No",ignoreCase = true)){
                        checkLowStock()
                        }
                    }
                } else {
                    if (companyCode == "AADHI INTERNATIONAL PTE LTD") {
                        if (activityFrom == "iv" || activityFrom == "so"
                            || activityFrom == "SalesEdit" || activityFrom == "ReOrderSales" ) {
                          //  if (!isAllowLowStock) {
                            if (negativeStockStr.equals("No",ignoreCase = true)){
                                checkLowStock()
                            }
                        }
                    } else {
                        if (activityFrom == "iv" || activityFrom == "Duplicate"
                            || activityFrom == "ConvertInvoiceFromDO" || activityFrom == "ReOrderInvoice") {
                          //  if (!isAllowLowStock) {
                            if (negativeStockStr.equals("No",ignoreCase = true)){
                                checkLowStock()
                            }
                        }
                    }
                }
            }
        }
        qtyValue!!.addTextChangedListener(qtyTW)
        productAutoComplete!!.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, rowId ->

            val selectProduct = parent.getItemAtPosition(position) as String
            val product =
                selectProduct.split("~".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            productId = product[1].trim { it <= ' ' }
            productName = product[0].trim { it <= ' ' }
            qtyValue!!.requestFocus()
            Log.w("Selected_product", product[1])
            setProductResult(product[1].trim { it <= ' ' })
            /*  try {
                        getProductPrice(productId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
        })
        focEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                //setCalculation();
              //  setCalculationView()

                //negativeStock no only stock check
                if (negativeStockStr.equals("No",ignoreCase = true)){
                    checkLowStockFOC()
                }
                if (!editable.toString().isEmpty()) {
                    addProduct!!.setAlpha(0.9f)
                    addProduct!!.setEnabled(true)
                } else {
                    addProduct!!.setAlpha(0.9f)
                    addProduct!!.setEnabled(true)
//                    addProduct!!.setAlpha(0.4f)
//                    addProduct!!.setEnabled(false)
                }
                //  setButtonView();
            }
        })

        addProduct!!.setOnClickListener(View.OnClickListener {
            val s = productAutoComplete!!.getText().toString()
            Log.w("addpdt", "" + productAutoComplete!!.getText().toString())
            if (!productAutoComplete!!.getText().toString().isEmpty()) {
                if (!qtyValue!!.getText().toString()
                        .isEmpty() && !s.isEmpty() && qtyValue!!.getText()
                        .toString() != "0" && qtyValue!!.getText().toString() != "00" &&
                    qtyValue!!.getText().toString() != "000" && qtyValue!!.getText()
                        .toString() != "0000" &&
                    netTotalValue!!.getText().toString() != "0.00"
                ) {
                    if (addProduct!!.getText().toString() == "Update") {
                        // if (!cartonPrice.getText().toString().isEmpty() && !cartonPrice.getText().toString().equals("0.00") && !cartonPrice.getText().toString().equals("0.0") && !cartonPrice.getText().toString().equals("0")){
                        if (priceText!!.getText() != null && !priceText!!.getText().toString()
                                .isEmpty()
                        ) {
                            if (priceText!!.getText().toString().toDouble() > 0) {
                                Log.w("mmimnimPrice","");
                                if (!minimumSellingPriceText!!.text.toString().isEmpty()) {
                                    minimumSellingPriceM = minimumSellingPriceText!!.text.toString()
                                }
                                val minimumsellingprice =
                                    minimumSellingPriceM.toDouble()
                                if (minimumsellingprice <= priceText!!.getText().toString()
                                        .toDouble()
                                ) {
                                    Log.w("mmimnimPriceVal",""+minimumsellingprice);

                                    insertProducts()

                                    qtyValue!!.setEnabled(true)
                                   // priceText!!.setEnabled(true)
                                    if(isEditPrice){
                                        priceText!!.isEnabled = true
                                    }
                                    else{
                                        priceText!!.isEnabled = false
                                    }

                                } else {
                                    showMinimumSellingpriceAlert(
                                        minimumSellingPriceText!!.getText().toString()
                                    )
                                }
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Price should not be zero",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Enter the price",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        if (priceText!!.getText().toString() != null && !priceText!!.getText()
                                .toString().isEmpty()
                        ) {
                            if (priceText!!.getText().toString().toDouble() > 0) {
                                Log.w("mmimnimPrice1","");

                                val minimumsellingprice =
                                    minimumSellingPriceText!!.getText().toString().toDouble()
                                if (minimumsellingprice <= priceText!!.getText().toString()
                                        .toDouble()
                                ) {
                                    Log.w("mmimnimPriceVal1",""+minimumsellingprice);

                                    addProduct("Add")
                                    qtyValue!!.setEnabled(true)
                                    if(isEditPrice){
                                        priceText!!.isEnabled = true
                                    }
                                    else{
                                        priceText!!.isEnabled = false
                                    }

                                    //   priceText!!.setEnabled(true)
                                } else {
                                    showMinimumSellingpriceAlert(
                                        minimumSellingPriceText!!.getText().toString()
                                    )
                                }
                            } else {
                                Log.w("mmimnimPrice2","");

                                if ((focEditText!!.getText() != null && !focEditText!!.getText()
                                        .toString().isEmpty() && focEditText!!.getText()
                                        .toString() != "0") || (returnQtyText!!.getText() != null && !returnQtyText!!.getText()
                                        .toString().isEmpty() && returnQtyText!!.getText()
                                        .toString() != "0") || (exchange_inv!!.getText() != null && !exchange_inv!!.getText()
                                        .toString().isEmpty() && exchange_inv!!.getText()
                                        .toString() != "0")
                                ) {
                                    addProduct("Add")
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Price should not be zero",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Enter the price",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    if ((focEditText!!.getText() != null && !focEditText!!.getText().toString()
                            .isEmpty() && focEditText!!.getText()
                            .toString() != "0") || (returnQtyText!!.getText() != null && !returnQtyText!!.getText()
                            .toString().isEmpty() && returnQtyText!!.getText().toString() != "0") ||
                        (exchange_inv!!.getText() != null && !exchange_inv!!.getText()
                            .toString().isEmpty() && exchange_inv!!.getText()
                            .toString() != "0")
                    ) {
                        addProduct("Add")
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Price should not be zero",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Select product", Toast.LENGTH_SHORT).show()
            }
        })
        returnAdj!!.setOnClickListener(View.OnClickListener { returnLayoutView!!.setVisibility(View.VISIBLE) })
        cancelReturn!!.setOnClickListener(View.OnClickListener {
            returnLayoutView!!.setVisibility(
                View.GONE
            )
        })
        uomChangel!!.setOnClickListener(){
            if (isUomSetting) {
                ischangeUOM = true
                uomChangel!!.visibility = View.VISIBLE
                ed_uomTxtl!!.visibility = View.GONE
                uomSpinnerLayl!!.visibility = View.VISIBLE

                val jsonObject = JSONObject()
                if(isEditItem){
                    try {
                        Log.w("pdtediyyy",""+productEditId);
                        jsonObject.put("CustomerCode", selectCustomerId)
                        jsonObject.put("ItemCode",productEditId)
                        getUOM(jsonObject)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.w("Errort:", Objects.requireNonNull(e.message!!))
                    }
                }else{
                    try {
                        jsonObject.put("CustomerCode", selectCustomerId)
                        jsonObject.put("ItemCode",productId)
                        getUOM(jsonObject)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.w("Errort:", Objects.requireNonNull(e.message!!))
                    }
                }

                        }
            else{
                Toast.makeText(this, "UOM settings not enabled", Toast.LENGTH_LONG).show()

            }
        }


        saveReturn!!.setOnClickListener(View.OnClickListener {
            /* if (!expiryReturnQty.getText().toString().isEmpty() && !damageReturnQty.getText().toString().isEmpty()){
                    if (!damageReturnQty.getText().toString().isEmpty()){
             //           if (Integer.parseInt(damageReturnQty.getText().toString())> 0){
                            if (Integer.parseInt(expiryReturnQty.getText().toString()) == 0){
                                dbHelper.updateReturnQty("Delete","0","Expiry",productId);
                                dbHelper.updateReturnQty("Update",damageReturnQty.getText().toString(),"Damage",productId);
                            }else if (Integer.parseInt(damageReturnQty.getText().toString())==0){
                                dbHelper.updateReturnQty("Delete","0","Damage",productId);
                                dbHelper.updateReturnQty("Update",expiryReturnQty.getText().toString(),"Expiry",productId);
                            }else {
                                dbHelper.updateReturnQty("Update",damageReturnQty.getText().toString(),"Damage",productId);
                                dbHelper.updateReturnQty("Update",expiryReturnQty.getText().toString(),"Expiry",productId);
                            }
                      //  }
                    }
                }*/
            expiryReturnQty!!.clearFocus()
            damageReturnQty!!.clearFocus()
            returnLayoutView!!.setVisibility(View.GONE)
        })
        invoiceDateInv!!.setOnClickListener(View.OnClickListener { getDate(invoiceDateInv) })
        getProducts()
        // Setting the sorting
        sortButton!!.setOnClickListener(View.OnClickListener {
            val menuItemView = findViewById<View>(R.id.fab)
            val popupMenu = PopupMenu(this@CreateNewInvoiceActivity, menuItemView)
            popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_all -> {
                        setProductsDisplay("All Products")
                        true
                    }

                    R.id.action_stock -> {
                        setProductsDisplay("In Stock")
                        true
                    }

                    R.id.action_outstock -> {
                        setProductsDisplay("Out of Stock")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        })
    }

    /* if (!qtyValue.getText().toString().isEmpty() && !s.isEmpty() && !qtyValue.getText().toString().equals("0") && !qtyValue.getText().toString().equals("00") && !qtyValue.getText().toString().equals("000") && !qtyValue.getText().toString().equals("0000") && !netTotalValue.getText().toString().equals("0.00")){
        if (priceText.getText().toString() != null && !priceText.getText().toString().isEmpty()) {
            double minimumsellingprice = Double.parseDouble(minimumSellingPriceText.getText().toString());
            if (minimumsellingprice <= Double.parseDouble(priceText.getText().toString())) {
                addProduct("Add");
            } else {
                showMinimumSellingpriceAlert(minimumSellingPriceText.getText().toString());
            }
        }else {
            Toast.makeText(getApplicationContext(), "Enter the price", Toast.LENGTH_SHORT).show();
        }*/

    fun showSignatureAlert() {
        val alertDialog = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.signature_layout, null)
        alertDialog.setView(customLayout)
        val acceptButton = customLayout.findViewById<Button>(R.id.buttonYes)
        val cancelButton = customLayout.findViewById<Button>(R.id.buttonNo)
        val clearButton = customLayout.findViewById<Button>(R.id.buttonClear)
        val mContent = customLayout.findViewById<LinearLayout>(R.id.signature_layout)
        acceptButton.isEnabled = false
        acceptButton.alpha = 0.4f

        val mSig = CaptureSignatureView(this, null, CaptureSignatureView.OnSignatureDraw {
            Log.w("SignatureString:", acceptButton.isEnabled.toString())
            acceptButton.isEnabled = true
            acceptButton.alpha = 1f
        })
        mContent.addView(
            mSig,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        acceptButton.setOnClickListener {
            // byte[] signature = captureSignatureView.getBytes();
            if(it.isEnabled) {
                signatureString = ""
                val signature = mSig.bitmap
                signatureCapture!!.setImageBitmap(signature)
                signatureString = ImageUtil.convertBimaptoBase64(signature)
                Utils.setSignature(signatureString)
                signatureAlert!!.dismiss()
                Log.w("SignatureString_data:", signatureString!!)
            }
        }
        cancelButton.setOnClickListener {
            signatureString = ""
            Utils.setSignature("")
            signatureAlert!!.dismiss()
        }
        clearButton.setOnClickListener {
            acceptButton.isEnabled = false
            acceptButton.alpha = 0.4f

            signatureString = ""
            Utils.setSignature("")
            mSig.ClearCanvas()
        }
        signatureAlert = alertDialog.create()
        signatureAlert!!.setCanceledOnTouchOutside(false)
        signatureAlert!!.show()
    }

    fun setProductsDisplay(action: String) {
        try {
            // productsAdapterNew.setWithFooter(false);
            // sortAdapter.resetPosition();
            val filterdNames = ArrayList<ProductsModel>()
            //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
            for (s in AppUtils.getProductsList()) {
                if (action == "In Stock") {
                    if (s.stockQty != null && s.stockQty != "null") {
                        if (s.stockQty.toDouble() > 0) {
                            filterdNames.add(s)
                        }
                    }
                } else if (action == "Out of Stock") {
                    if (s.stockQty != null && s.stockQty != "null") {
                        if (s.stockQty.toDouble() < 0 || s.stockQty.toDouble() == 0.0) {
                            filterdNames.add(s)
                        }
                    }
                } else {
                    filterdNames.add(s)
                }
            }

            // Setting filter option in Localdb
            if (action == "In Stock") {
                dbHelper!!.insertSettings("stock_view", "1")
            } else if (action == "Out of Stock") {
                dbHelper!!.insertSettings("stock_view", "0")
            } else {
                dbHelper!!.insertSettings("stock_view", "2")
            }
            //calling a method of the adapter class and passing the filtered list
            if (filterdNames.size > 0) {
                /* for (ProductsModel model:filterdNames){
                    Log.w("products_Option_values:",model.getProductName());
                }*/
                if (selectProductAdapter != null) {
                    selectProductAdapter!!.filterList(filterdNames)
                }
                selectProductAdapter!!.filterList(filterdNames)
                productLayout!!.visibility = View.VISIBLE
                emptyLayout!!.visibility = View.GONE
                totalProducts!!.text = filterdNames.size.toString() + " Products"
            } else {
                productLayout!!.visibility = View.GONE
                emptyLayout!!.visibility = View.VISIBLE
                totalProducts!!.text = filterdNames.size.toString() + " Products"
            }
        } catch (ex: Exception) {
            Log.e("Error_in_filter", Objects.requireNonNull(ex.message!!))
        }
    }


    fun percentageToAmount(percentage: Double): Double {

        return getSubtotal() / 100.0f * percentage
    }

    fun calculatePercentage(obtained: Double, total: Double): Double {
        return obtained * 100 / total
    }

    fun setPercentageAmount(value: Double) {
        val per: Double = calculatePercentage(value, getSubtotal())
        Log.w("PercentageTxt:", per.toString() + "")
        //billDiscAmount.removeTextChangedListener(billDiscAmountTextWatcher);
        if (per == 0.0) {
            bill_disc_percent_ed!!.removeTextChangedListener(billDiscPercentageTextWatcher)
            bill_disc_percent_ed!!.setText("")
            bill_disc_percent_ed!!.addTextChangedListener(billDiscPercentageTextWatcher)
        } else {
            bill_disc_percent_ed!!.removeTextChangedListener(billDiscPercentageTextWatcher)
            bill_disc_percent_ed!!.setText(twoDecimalPoint(per) + "")
            bill_disc_percent_ed!!.addTextChangedListener(billDiscPercentageTextWatcher)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDate(dateEditext: TextView?) {
        // Get Current Date
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(
            this@CreateNewInvoiceActivity,
            { view, year, monthOfYear, dayOfMonth ->
                dateEditext!!.text = dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year
                currentDate = convertDate(
                    dateEditext.text.toString()
                )
                Log.w("CurrentDateView:", currentDate!!)
            }, mYear, mMonth, mDay
        )
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show()
    }

    fun setProductResult(productId: String) {
        try {
            for (model in AppUtils.getProductsList()) {
                Log.w("product_Code:", model.productCode)

                if (model.productCode == productId.trim { it <= ' ' }) {
                    if (model.stockQty != null && model.stockQty != "null") {
                        if (model.stockQty.toDouble() == 0.0 || model.stockQty.toDouble() < 0) {
                            stockQtyValue!!.text = model.stockQty
                            stockQtyValue!!.setTextColor(Color.parseColor("#D24848"))
                        } else if (model.stockQty.toDouble() > 0) {
                            stockQtyValue!!.setTextColor(Color.parseColor("#2ECC71"))
                            stockQtyValue!!.text = model.stockQty
                        }
                    }
                    // stockLayout.setVisibility(View.VISIBLE);
                    if (model.stockQty.toDouble() == 0.0 || model.stockQty.toDouble() < 0) {
//                        if (isAllowLowStock) {
                        if (negativeStockStr.equals("No",ignoreCase = true)){

                            productAutoComplete!!.clearFocus()
                            cartonPrice!!.setText(model.unitCost)
                            if(shortCodeStr.equals("FUXIN",true)) {
                                if (isLastPrice.equals("True", true)) {
                                    priceText!!.setText(model.lastPrice)
                                } else {
                                    priceText!!.setText(model.unitCost)
                                }
                            }
                            else{
                                if (model.lastPrice != null && !model.lastPrice.isEmpty() &&
                                    model.lastPrice.toDouble() > 0.00
                                ) {
                                    priceText!!.setText(model.lastPrice)
                                } else {
                                    priceText!!.setText(model.unitCost)
                                }
                            }

                            loosePrice!!.setText(model.unitCost)
                            // uomText.setText(model.getUomCode());
                            stockCount!!.setText(model.stockQty)
                            pcsPerCarton!!.setText(model.pcsPerCarton)
                            if(isFOCStr.equals("Yes",true)){
                                if(model.isItemFOC.equals("Yes",true)) {
                                    focEditText!!.isEnabled = true
                                } else{
                                    focEditText!!.isEnabled = false
                                }
                            }
                            else{
                                focEditText!!.isEnabled = false
                            }
                            exchangeEditext!!.isEnabled = true
                            discountEditext!!.isEnabled = true
                            returnEditext!!.isEnabled = true
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Low Stock Please check",
                                Toast.LENGTH_SHORT
                            ).show()
                            productAutoComplete!!.clearFocus()
                            productAutoComplete!!.setText("")
                            qtyValue!!.isEnabled = false
                        }
                        qtyValue!!.isEnabled = true
                        qtyValue!!.setText("")
                        qtyValue!!.requestFocus()
                        openKeyborard(qtyValue)
                        qtyValue!!.setSelection(qtyValue!!.text.length)
                    } else {
                        productAutoComplete!!.clearFocus()
                        cartonPrice!!.setText(model.unitCost)
                        if(shortCodeStr.equals("FUXIN",true)) {
                            if (isLastPrice.equals("True", true)) {
                                priceText!!.setText(model.lastPrice)
                            } else {
                                priceText!!.setText(model.unitCost)
                            }
                        }
                        else{
                            if (model.lastPrice != null && !model.lastPrice.isEmpty() &&
                                model.lastPrice.toDouble() > 0.00
                            ) {
                                priceText!!.setText(model.lastPrice)
                            } else {
                                priceText!!.setText(model.unitCost)
                            }
                        }
                        loosePrice!!.setText(model.unitCost)
                        // uomText.setText(model.getUomCode());
                        stockCount!!.setText(model.stockQty)
                        pcsPerCarton!!.setText(model.pcsPerCarton)
                        qtyValue!!.isEnabled = true
                        qtyValue!!.setText("")
                        qtyValue!!.requestFocus()
                        openKeyborard(qtyValue)
                        qtyValue!!.setSelection(qtyValue!!.text.length)
                        if(isFOCStr.equals("Yes")){
                            if(model.isItemFOC.equals("Yes",true)) {
                                focEditText!!.isEnabled = true
                            } else{
                                focEditText!!.isEnabled = false
                            }                        }
                        else{
                            focEditText!!.isEnabled = false
                        }
                        exchangeEditext!!.isEnabled = true
                        discountEditext!!.isEnabled = true
                        returnEditext!!.isEnabled = true
                    }
                } else {
                    Log.w("Not_found", "Product")
                }
                qtyValue!!.isEnabled = true
                qtyValue!!.requestFocus()
                openKeyborard(qtyValue)
                qtyValue!!.setSelection(qtyValue!!.text.length)
            }
        } catch (ex: Exception) {
        }
    }

    fun showAlert() {
        SweetAlertDialog(applicationContext, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Warning !")
            .setContentText("Net total should not be zero or Atleast add one Qty")
            .setConfirmText("Cancel")
            .setConfirmClickListener { sDialog -> sDialog.dismissWithAnimation() }.show()
    }

    fun showAlertForDeleteProduct() {
        SweetAlertDialog(applicationContext, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Warning !")
            .setContentText("Products Will be cleared are you sure want to back?")
            .setConfirmText("Cancel")
            .setConfirmClickListener { sDialog ->
                dbHelper!!.removeAllInvoiceItems()
                sDialog.dismissWithAnimation()
            }.show()
    }

    fun showMinimumSellingpriceAlert(sellingPrice: String) {
        val builder1 = AlertDialog.Builder(this@CreateNewInvoiceActivity)
        builder1.setTitle("Warning !")
        builder1.setMessage(
            productName + "-" + "Minimum Selling Price is : $ " + Utils.twoDecimalPoint(
                sellingPrice.toDouble()
            )
        )
        builder1.setCancelable(false)
        builder1.setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
        val alert11 = builder1.create()
        alert11.show()
    }

    fun addProduct(action: String?) {
        Log.w("ProductIdView222:",""+ isProductExist(productId))

        if (isProductExist(productId)) {
            Log.w("ProductIdView:", productId!!)
            if (!qtyValue!!.text.toString().isEmpty() && qtyValue!!.text.toString()
                    .toDouble() > 0
            ) {
                showExistingProductAlert(productId, productName)
            }
//            else {
//                insertProducts()
//            }
        } else {
            Log.w("ProductIdView11:", productId!!)
            insertProducts()
        }
    }

    fun showExistingProductAlert(productId: String?, productName: String?) {
        val builder1 = AlertDialog.Builder(this@CreateNewInvoiceActivity)
        builder1.setTitle("Warning !")
        builder1.setMessage("$productName - $productId\nAlready Exist Do you want to replace ? ")
        builder1.setCancelable(false)
        builder1.setPositiveButton("YES") { dialog, id ->
            dialog.cancel()
            insertProducts()
        }
        builder1.setNegativeButton("NO") { dialog, id ->
            dialog.cancel()
            clearFields()
        }
        val alert11 = builder1.create()
        alert11.show()
    }

    fun insertProducts() {
       try {
            var focType = "pcs"
            var exchangeType = "pcs"
            var returnType = "pcs"
            var discount = "0"
            var return_qty = "0"
            val lPriceCalc = "0"
            var itemdiscc = "0"
            var billdiscc = "0"
            var minimumSellingPricel = "0"
            var foc = "0"
            var uom = "PCS"
            var saleable = "0"
            var damaged = "0"
            var price_value = "0"
            var exchange = "0"

//            var summaryList: ArrayList<CreateInvoiceModel>? = ArrayList()
//
//            summaryModel.saleableQty  = expiryReturnQty!!.text.toString()
//            summaryModel.damagedQty = damageReturnQty!!.text.toString()
//
//           // summaryList!!.add(summaryModel)

            if (focSwitch!!.isChecked) {
                focType = "ctn"
            }
            if (exchangeSwitch!!.isChecked) {
                exchangeType = "ctn"
            }
            if (returnSwitch!!.isChecked) {
                returnType = "ctn"
            }
            val ctn_qty = "0"
            var qty_value = "0"
            if (!qtyValue!!.text.toString().isEmpty()) {
                qty_value = qtyValue!!.text.toString()
            }
            if (!discountEditext!!.text.toString().isEmpty()) {
                discount = discountEditext!!.text.toString()
            }
            if (!item_discount_ed!!.text.toString().isEmpty()) {
                itemdiscc = item_discount_ed!!.text.toString()
            }
            if (!returnQtyText!!.text.toString().isEmpty()) {
                return_qty = returnQtyText!!.text.toString()
            }
            if (!priceText!!.text.toString().isEmpty()) {
                price_value = priceText!!.text.toString()
            }
            if (!focEditText!!.text.toString().isEmpty()) {
                foc = focEditText!!.text.toString()
            }
            if (!uomText!!.text.toString().isEmpty()) {
                uom = uomText!!.text.toString()
            }
            if (!exchange_inv!!.text.toString().isEmpty()) {
                exchange = exchange_inv!!.text.toString()
            }
            if (!expiryReturnQty!!.text.toString().isEmpty()) {
                saleable = expiryReturnQty!!.text.toString()
            }
            if (!damageReturnQty!!.text.toString().isEmpty()) {
                damaged = damageReturnQty!!.text.toString()
            }
            if (!bill_disc_amt_ed!!.text.toString().isEmpty()) {
                billdiscc = bill_disc_amt_ed!!.text.toString()
            }
            if (!minimumSellingPriceText!!.text.toString().isEmpty()) {
                minimumSellingPricel = minimumSellingPriceText!!.text.toString()
            }

           var timeStamp: String? = Calendar.getInstance().timeInMillis.toString()
           if(isEditItem){
               timeStamp = editTimeStamp
           }else{
               timeStamp = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date())
           }

           val priceValue = 0.0
            val net_qty = qty_value.toDouble() - return_qty.toDouble()
            val return_amt = return_qty.toDouble() * price_value.toDouble()
            val total = net_qty * price_value.toDouble()
            val sub_total = total - return_amt - discount.toDouble()
            val insertStatus = dbHelper!!.insertCreateInvoiceCart(
                productId.toString().trim { it <= ' ' },
                productName,
                uom,
                uom,
                qty_value,
                return_qty,
                qty_value,
                foc,
                price_value,
                stockQtyValue!!.text.toString(),
                total.toString(),
                subTotalValue!!.text.toString(),
                taxValueText!!.text.toString(),
                netTotalValue!!.text.toString(),
                itemdiscc,
                "",
                saleable,
                damaged,
                exchange,
                minimumSellingPricel,pdtStockVal,timeStamp,isItemFOCApi
            )
            Log.w("itemds_inv",""+exchange+".. "+
                    sharedPref_billdisc!!.getString("billDisc_amt", ""))

            Log.w("total_inv",""+netTotalValue!!.text.toString()+".. "+subTotalValue!!.text.toString())
            Log.w("timeupdat",""+timeStamp)

            // Adding Return Qty Table values
            if (return_qty.toDouble() > 0) {
                dbHelper!!.updateReturnQty("Delete", "0", "Saleable Return", productId)
                dbHelper!!.updateReturnQty("Delete", "0", "Damaged/Expired", productId)
                dbHelper!!.insertReturnProduct(
                    productId,
                    productName,
                    expiryReturnQty!!.text.toString(),
                    "Saleable Return"
                )
                dbHelper!!.insertReturnProduct(
                    productId,
                    productName,
                    damageReturnQty!!.text.toString(),
                    "Damaged/Expired"
                )
            }
            if (insertStatus) {
                subTotalValue!!.text = "0.0"
                taxValueText!!.text = "0.00"
                netTotalValue!!.text = "0.0"
                productAutoComplete!!.setText("")
                priceText!!.setText("0.00")
                qtyValue!!.setText("")
                uomText!!.setText("")
                uomTextView!!.text = ""
                stockCount!!.setText("")
                stockQtyValue!!.text = ""
                qtyValue!!.clearFocus()
                ischangeUOM = false
                editTimeStamp = ""
//                pdtStockVal="0.00"
                uomChangel!!.visibility = View.GONE
                ed_uomTxtl!!.visibility = View.GONE
                uomSpinnerLayl!!.visibility = View.VISIBLE
                bill_disc_percent_ed!!.setText("0.00")
                bill_disc_amt_ed!!.setText("0.00")
                isEditItem = false
                productAutoComplete!!.clearFocus()
                focEditText!!.setText("")
                exchangeEditext!!.setText("")
                discountEditext!!.setText("")
                returnQtyText!!.setText("")
                item_discount_ed!!.setText("")
                exchange_inv!!.setText("")
                isreturnTouch = false
                focSwitch!!.isChecked = false
                exchangeSwitch!!.isChecked = false
                returnSwitch!!.isChecked = false
                stockLayout!!.visibility = View.GONE
                // priceText.setEnabled(false);
                // qtyValue.setEnabled(false);
                focEditText!!.isEnabled = false
                exchangeEditext!!.isEnabled = false
                discountEditext!!.isEnabled = false
                item_discount_ed!!.isEnabled = false
                exchange_inv!!.isEnabled = false

                addProduct!!.text = "Add"
                searchProduct!!.isEnabled = true
                down_menu!!.isVisible = true
                hideKeyboard()
                getProducts()
            } else {
                Toast.makeText(applicationContext, "Error in Add product", Toast.LENGTH_LONG).show()
            }
        } catch (ec: Exception) {
            Log.e("Error_InsertProduct:", ec.message!!)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getProducts() {
        val products = dbHelper!!.allInvoiceProducts
        Log.w("activityfr1",""+activityFrom)
//        Log.w("pdtArraylis",""+products.get(0).netTotal)

        if (products.size > 0) {
            itemCount!!.text = "Products ( " + products.size + " )"
            productSummaryView!!.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            productSummaryAdapter = NewProductSummaryAdapter(
                this,
                products,
                object : NewProductSummaryAdapter.CallBack {
                    override fun searchCustomer(letter: String, pos: Int) {}
                    override fun removeItem(pid: String,updateTime: String) {
                        showRemoveItemAlert(pid , updateTime)
                    }

                    override fun editItem(model: CreateInvoiceModel) {
                        isEditItem = true
                        salesReturn!!.isChecked = false
                        salesReturn!!.isEnabled = false
                        searchProduct!!.isEnabled = false
                        down_menu!!.isVisible = false
                        item_discount_ed!!.isEnabled = true

                        productId = model.productCode
                        var stockVal = 0.0
                        var qtyVal = 0.0
                        var finalStockVal = 0.0
                        var stockStr = ""

                        productName = model.productName
                        uomText!!.setText(model.uomCode)
                        uomName = model.uomCode
                        editTimeStamp = model.updateTime
                        uomPriceEd = model.price
                        damage_editVal = model.damagedQty
                        saleable_editVal = model.saleableQty
//                        pdtStockEditVal = model.stockProductQty
                        pdtStockVal = model.stockProductQty
                        var pdtStock = 0.0;
                        productEditId = model.productCode

                        val jsonObject = JSONObject()
                        jsonObject.put("CustomerCode", selectCustomerId)
                        jsonObject.put("ItemCode",productEditId)
                        getUOMEdit(jsonObject,model.uomCode,model.stockProductQty)


                        Log.w("pdtuomentryy", "" + model.stockProductQty)
                        Log.w("pdtstockk", "" + model.stockQty)
                        Log.w("timeupdat_edit:", model.saleableQty+".."+model.updateTime)
                        Log.w("miniSell_txt:",model.minimumSellingPrice)

//                        if (isUomSetting) {
//                            val jsonObject = JSONObject()
//                            try {
//                                jsonObject.put("CustomerCode", selectCustomerId)
//                                jsonObject.put("ItemCode",productId)
//                                getUOM(jsonObject)
//
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                                Log.w("Errort:", Objects.requireNonNull(e.message!!))
//                            }
//                        }
                        Log.w("EditUomText:", model.uomCode)
//                        setUOMCode(uomList!!)
                        uomTextView!!.text = model.uomText
                        qtyValue!!.setText("")
                        val netqty = model.netQty.toDouble()
                        if (model.minimumSellingPrice != null && !model.minimumSellingPrice.isEmpty()) {
                            minimumSellingPriceText!!.text = model.minimumSellingPrice
                        } else {
                            minimumSellingPriceText!!.text = "0.00"
                        }

                        qtyValue!!.removeTextChangedListener(qtyTW)
                       qtyValue!!.setText(Utils.getQtyValue(netqty.toString()))
                        Log.w("editqty11",""+model.netQty+"..mm"+Utils.getQtyValue(netqty.toString()))
                        qtyValue!!.addTextChangedListener(qtyTW)
                        productAutoComplete!!.setText(model.productName + "-" + model.productCode)
                        priceText!!.setText(model.price)
                        item_discount_ed!!.setText(model.itemDisc)
                        expiryReturnQty!!.setText(model.saleableQty)
                        damageReturnQty!!.setText(model.damagedQty)
                        exchange_inv!!.setText(model.exchangeQty)

                        addProduct!!.text = "Update"
                        qtyValue!!.requestFocus()
                        qtyValue!!.setSelectAllOnFocus(true)
                        qtyValue!!.setSelection(qtyValue!!.text.length)
                        qtyValue!!.isEnabled = true
                        if (model.focQty != null && !model.focQty.isEmpty() && model.focQty != "null") {
                            focEditText!!.setText(model.focQty)
                        } else {
                            focEditText!!.setText("0")
                        }
                        if (model.returnQty != null && !model.returnQty.isEmpty() && model.returnQty != "null") {
                            returnQtyText!!.setText(model.returnQty.toString())
                        } else {
                            returnQtyText!!.setText("0")
                        }
                        if(isFOCStr.equals("Yes")){
                            if(model.isItemFOC.equals("Yes",true)) {
                                focEditText!!.isEnabled = true
                            } else{
                                focEditText!!.isEnabled = false
                            }                            }
                        else{
                            focEditText!!.isEnabled = false
                        }

                        returnQtyText!!.isEnabled = true
                        exchange_inv!!.isEnabled = true
                        isItemFOCApi = model.isItemFOC

                        uomValueVisible(model.uomCode)


                        stockCount!!.setText(model.stockQty)
                        minimumSellingPriceText!!.setText(model.minimumSellingPrice)

                        /* for (ProductsModel m:productList) {
                        if (m.getProductCode().equals(productId)) {
                            if (m.getStockQty()!=null && !m.getStockQty().equals("null")){
                                if (Double.parseDouble(m.getStockQty()) == 0 || Double.parseDouble(m.getStockQty()) < 0) {
                                    stockQtyValue.setText(m.getStockQty());
                                    stockQtyValue.setTextColor(Color.parseColor("#D24848"));
                                    stockCount.setText(m.getStockQty());
                                } else if (Double.parseDouble(m.getStockQty()) > 0) {
                                    stockQtyValue.setTextColor(Color.parseColor("#2ECC71"));
                                    stockQtyValue.setText(m.getStockQty());
                                    stockCount.setText(m.getStockQty());
                                }
                            }
                        }
                    }*/

                        //  setCalculationView();
                    }
                })
            productSummaryView!!.adapter = productSummaryAdapter
            noproductText!!.visibility = View.GONE
            productSummaryView!!.visibility = View.VISIBLE
            itemCount!!.visibility = View.VISIBLE
        } else {
            itemCount!!.visibility = View.GONE
            noproductText!!.visibility = View.VISIBLE
            productSummaryView!!.visibility = View.GONE
        }
        Utils.refreshActionBarMenu(this)
        hideKeyboard()
        setSummaryTotal()
    }


    fun billDiscGreater(subTotal: Double , editText: Double) : Boolean {
        if(subTotal > editText){
            valueGreat = true
        }
        else{
            valueGreat = false
        }
        return valueGreat
    }
    fun showRemoveItemAlert(pid: String?,updateTime:String) {
        try {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE) // .setTitleText("Are you sure?")
                .setContentText("Are you sure want to remove this item ?")
                .setConfirmText("YES")
                .setConfirmClickListener { sDialog ->
                    dbHelper!!.deleteInvoiceProductNew(pid,updateTime)
                    clearFields()
                    sDialog.dismissWithAnimation()
                    getProducts()
                    setSummaryTotal()
                    addProduct!!.text = "Add"
                    searchProduct!!.isEnabled = true
                    down_menu!!.isVisible = true

                    Utils.refreshActionBarMenu(this@CreateNewInvoiceActivity)
                }
                .showCancelButton(true)
                .setCancelText("No")
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .show()
        } catch (ex: Exception) {
        }
    }

    fun hideKeyboard() {
        try {
            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("Errorj1:", Objects.requireNonNull(e.message!!))
        }
    }

    fun setSummaryTotal() {
        try {
            val localCart: ArrayList<CreateInvoiceModel>
            localCart = dbHelper!!.allInvoiceProducts
            var net_sub_total = 0.0
            var net_tax = 0.0
            var net_total = 0.0
            if (localCart.size > 0) {
                for (model in localCart) {
                    if (model.subTotal != null && !model.subTotal.isEmpty()) {
                        net_sub_total += model.subTotal.toDouble()
                    }
                    if (model.gstAmount != null && !model.gstAmount.isEmpty()) {
                        net_tax += model.gstAmount.toDouble()
                    }
                    if (model.netTotal != null && !model.netTotal.isEmpty()) {
                        net_total += model.netTotal.toDouble()
                    }
                }
                val sharedPreferences = getSharedPreferences("customerPref", MODE_PRIVATE)
                selectCustomerId = sharedPreferences.getString("customerId", "")
                customerDetails = dbHelper!!.getCustomer(selectCustomerId)
                val taxValue = customerDetails!!.get(0).taxPerc
                val taxType = customerDetails!!.get(0).taxType
                Log.w("GivenEditTax:", net_tax.toString() + "")
                Log.w("GivenNetTotalValues:", net_total.toString() + "")


                /*     if (customerDetails.get(0).getTaxType().equals("I")){
                    subTotal.setText(Utils.twoDecimalPoint(net_total));
                    taxText.setText(Utils.twoDecimalPoint(net_tax));
                    netTotalText.setText(Utils.twoDecimalPoint(net_total));
                    itemDiscountText.setText(Utils.twoDecimalPoint(net_discount));

                    double sub_total=net_total - net_tax;
                    totalText.setText(Utils.twoDecimalPoint(sub_total));
                    double sub_total1=sub_total + net_tax;

                    // subTotalValue=Utils.twoDecimalPoint(sub_total1);
                    subTotalValue=Utils.twoDecimalPoint(net_total);

                    taxValue=Utils.twoDecimalPoint(net_tax);
                    netTotalValue=Utils.twoDecimalPoint(net_total);
                    itemDiscountAmount=Utils.twoDecimalPoint(net_discount);
                    totalValue=Utils.twoDecimalPoint(total_value);

                }else {
                    subTotal.setText(Utils.twoDecimalPoint(net_sub_total));
                    taxText.setText(Utils.twoDecimalPoint(net_tax));
                    totalText.setText(Utils.twoDecimalPoint(net_sub_total));
                    netTotalText.setText(Utils.twoDecimalPoint(net_total));
                    itemDiscountText.setText(Utils.twoDecimalPoint(net_discount));

                    subTotalValue=Utils.twoDecimalPoint(net_sub_total);
                    taxValue=Utils.twoDecimalPoint(net_tax);
                    netTotalValue=Utils.twoDecimalPoint(net_total);
                    itemDiscountAmount=Utils.twoDecimalPoint(net_discount);
                    totalValue=Utils.twoDecimalPoint(total_value);
                }*/

//                if (bill_disc_amt_ed!!.getText().toString().isNotEmpty()) {
//                    billDiscountAmount = bill_disc_amt_ed!!.getText().toString()
//                } else {
//                    billDiscountAmount = "0"
//                }
                if (taxType == "I") {
                    setCalculationSummaryView(net_total)
                } else {
                    setCalculationSummaryView(net_sub_total)
                }
            }
        } catch (ex: Exception) {
        }
    }
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
                mPhotoFile = photoFile
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    /**
     * Select image fro gallery
     */
    private fun dispatchGalleryIntent() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO)
    }
    fun showImage() {
        val builder = AlertDialog.Builder(this@CreateNewInvoiceActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.image_view_layout, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.invoice_image)
        Glide.with(this)
            .load(mPhotoFile)
            .error(R.drawable.no_image_found)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            }).into(imageView)
        builder.setCancelable(false)
        builder.setTitle("Invoice Image")
        builder.setView(dialogView)
        builder.setNeutralButton(
            "NEW IMAGE"
        ) { dialogInterface, i -> selectImage() }
        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            selectImageInv!!.setTag("view_image")
            selectImageInv!!.setText("View Image")
            dialog.dismiss()
        }.create().show()
    }

    fun selectImage() {
        val items = arrayOf<CharSequence>(
            "Take Photo",  /* "Choose from Library",*/
            "Cancel"
        )
        val builder = AlertDialog.Builder(this@CreateNewInvoiceActivity)
        builder.setItems(
            items
        ) { dialog: DialogInterface, item: Int ->
            if (items[item] == "Take Photo") {
                requestStoragePermission(true)
            } //else if (items[item].equals("Choose from Library")) {
            else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    /**
     * Requesting multiple permissions (storage and camera) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private fun requestStoragePermission(isCamera: Boolean) {
        var permission = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
        }
        Dexter.withContext(this)
            .withPermissions(
                permission
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        } else {
                            dispatchGalleryIntent()
                        }
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener { error: DexterError? ->
                Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT)
                    .show()
            }
            .onSameThread()
            .check()
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage(
            "This app needs permission to use this feature. You can grant them in app settings."
        )
        builder.setPositiveButton("GOTO SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, 101)
    }

    /**
     * Create file with current timestamp name
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }

    /**
     * Get real file path from URI
     */
    fun getRealPathFromUri(contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri!!, proj, null, null, null)
            assert(cursor != null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }
    fun clearFields() {
        subTotalValue!!.text = "0.0"
        taxValueText!!.text = "0.00"
        netTotalValue!!.text = "0.0"
        uomChangel!!.visibility = View.GONE
        ed_uomTxtl!!.visibility = View.GONE
        uomSpinnerLayl!!.visibility = View.VISIBLE
        ischangeUOM = false
//        pdtStockVal = "0.00"
        productAutoComplete!!.setText("")
        priceText!!.setText("0.00")
        qtyValue!!.setText("")
        isEditItem = false
        pcsPerCarton!!.setText("")
        uomText!!.setText("")
        uomTextView!!.text = ""
        editTimeStamp = ""
        stockCount!!.setText("")
        exchange_inv!!.setText("")
        item_discount_ed!!.setText("")
        qtyValue!!.clearFocus()
        productAutoComplete!!.clearFocus()
        focEditText!!.setText("")
        returnQtyText!!.setText("")
        stockLayout!!.visibility = View.GONE
        // priceText.setEnabled(false);
        getProducts()
        setSummaryTotal()
    }

    fun isProductExist(productId: String?): Boolean {
        var isExist = false
       // try {
            val localCart = dbHelper!!.allInvoiceProducts

            if (localCart.size > 0) {

                for (cart in localCart) {
                Log.w("localPdt1",""+cart.productCode)

                    if (cart.productCode != null) {
                        if (cart.productCode == productId) {
                            Log.w("ProductIdPrint:", cart.productCode)
                            isExist = true
                            break
                        }
                    }
                }
            }
//        } catch (ex: Exception) {
//            Log.e("Exp_to_check_product:", Objects.requireNonNull(ex.message!!))
//        }
        return isExist
    }

    fun checkLowStock() {
        var stock = 0.0
        var qty = 0.0
        if (!stockQtyValue!!.text.toString().isEmpty()) {
            if (!stockQtyValue!!.text.toString().isEmpty()) {
                stock = stockQtyValue!!.text.toString().toDouble()
            }
            if (!qtyValue!!.text.toString().isEmpty()) {
                qty = qtyValue!!.text.toString().toDouble()
            }
            if (stock < qty) {
                Toast.makeText(applicationContext, "Low Stock please check", Toast.LENGTH_SHORT)
                    .show()
                qtyValue!!.removeTextChangedListener(qtyTW)
                qtyValue!!.setText("")
                qtyValue!!.addTextChangedListener(qtyTW)
                setCalculationView()
            }
        }
    }

    fun checkLowStockFOC() {
        var stock = 0.0
        var focl = 0.0
        if (!stockQtyValue!!.text.toString().isEmpty()) {
            if (!stockQtyValue!!.text.toString().isEmpty()) {
                stock = stockQtyValue!!.text.toString().toDouble()
            }
            if (!focEditText!!.text.toString().isEmpty()) {
                focl = focEditText!!.text.toString().toDouble()
            }
            if (stock < focl) {
                Toast.makeText(applicationContext, "Low Stock please check", Toast.LENGTH_SHORT)
                    .show()
              //  focEditText!!.removeTextChangedListener()
                focEditText!!.setText("")
             //   focEditText!!.addTextChangedListener()
              //  setCalculationView()
            }
        }
    }

    fun getSubtotal(): Double {
        try {
            val localCart: ArrayList<CreateInvoiceModel>
            localCart = dbHelper!!.allInvoiceProducts
            var net_sub_total = 0.0
            var net_tax = 0.0
            var net_total = 0.0
            var discount = 0.0

            if (localCart.size > 0) {
                for (model in localCart) {
                    if (model.subTotal != null && !model.subTotal.isEmpty()) {
                        net_sub_total += model.subTotal.toDouble()
                    }
                    if (model.gstAmount != null && !model.gstAmount.isEmpty()) {
                        net_tax += model.gstAmount.toDouble()
                    }
                    if (model.netTotal != null && !model.netTotal.isEmpty()) {
                        net_total += model.netTotal.toDouble()
                    }
                    if (model.itemDisc != null && !model.itemDisc.isEmpty()) {
                        discount += model.itemDisc.toDouble()
                    }
                }
                val sharedPreferences = getSharedPreferences("customerPref", MODE_PRIVATE)
                selectCustomerId = sharedPreferences.getString("customerId", "")
                customerDetails = dbHelper!!.getCustomer(selectCustomerId)
                val taxValue = customerDetails!!.get(0).taxPerc
                val taxType = customerDetails!!.get(0).taxType

                net_total_value = net_sub_total
                Log.w("subtotall",""+net_total_value);

//                if (taxType == "I") {
//                    net_total_value = net_total + discount
//                } else {
//                    net_total_value = net_sub_total + discount
//                }
            }

        } catch (ex: Exception) {
        }
        return net_total_value
    }

    val lowStockSetting: Unit
        /**
         * Get = the low stock invoice setting to allow the Negative stock allow the invoice
         */
        get() {
            val settings = dbHelper!!.settings
            if (settings.size > 0) {
                for (model in settings) {
                    if (model.settingName == "allow_negative_switch") {
                        isAllowLowStock = model.settingValue == "1"
                    } else if (model.settingName == "creditLimitSwitch") {
                        Log.w("SettingNameI:", model.settingName)
                        Log.w("SettingValueI:", model.settingValue)
                        if (model.settingValue == "1") {
                            isCheckedCreditLimit = true
                        } else {
                            isCheckedCreditLimit = false
                        }
                    }

                }
            } else {
                isAllowLowStock = false
            }
        }

    fun setCalculationSummaryView(subTotal: Double) {
        try {
            var taxAmount1 = 0.0
            var netTotal1 = 0.0
            val sharedPreferences = getSharedPreferences("customerPref", MODE_PRIVATE)
            val selectCustomerId = sharedPreferences.getString("customerId", "")
            if (selectCustomerId != null && !selectCustomerId.isEmpty()) {
                customerDetails = dbHelper!!.getCustomer(selectCustomerId)
            }
            val taxValue = customerDetails!![0].taxPerc
            val taxType = customerDetails!![0].taxType
            Log.w("TaxType-Summary:", taxType)
            Log.w("TaxValue12-Summary:", taxValue)
            Log.w("SubTotalValues:", subTotal.toString())
            var Prodtotal = "0.0" ;
            if(shortCodeStr!!.equals("FUXIN",true)) {
                 Prodtotal = Utils.fourDecimalPoint(subTotal)
            }else{
                 Prodtotal = Utils.twoDecimalPoint(subTotal)

            }
            if (!taxType.matches("".toRegex()) && !taxValue.matches("".toRegex())) {
                val taxValueCalc = taxValue.toDouble()
                if (taxType.matches("E".toRegex())) {
                    taxAmount1 = subTotal * taxValueCalc / 100
                    netTotal1 = subTotal + taxAmount1
                    taxTitle!!.text = "GST ( Exc ) : " + taxValue.toDouble().toInt() + " % "

                    if(shortCodeStr!!.equals("FUXIN",true)){
                        val prodTax = Utils.fourDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax

                        val ProdNetTotal = Utils.fourDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.fourDecimalPoint(subTotal)
                    }else{
                        val prodTax = Utils.twoDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax

                        val ProdNetTotal = Utils.twoDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                    }
                    bill_disc_subtotal_txtl!!.text =Utils.twoDecimalPoint(netTotal1)

                } else if (taxType.matches("I".toRegex())) {
                    taxAmount1 = subTotal * taxValueCalc / (100 + taxValueCalc)
                    netTotal1 = subTotal
                    val dTotalIncl = netTotal1 - taxAmount1
                    val totalIncl = Utils.twoDecimalPoint(dTotalIncl)
                    Log.d("totalIncl", "" + totalIncl)
                    taxTitle!!.text = "GST ( Inc ) : " + taxValue.toDouble().toInt() + " % "

                    if(shortCodeStr!!.equals("FUXIN",true)){
                        val prodTax = Utils.fourDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax
                        val sub_total = subTotal - taxAmount1

                        val ProdNetTotal = Utils.fourDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.fourDecimalPoint(sub_total)
                    }else{
                        val prodTax = Utils.twoDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax
                        val sub_total = subTotal - taxAmount1

                        val ProdNetTotal = Utils.twoDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.twoDecimalPoint(sub_total)
                    }


                    // netTotal1 = subTotal + taxAmount1;
                    bill_disc_subtotal_txtl!!.text =Utils.twoDecimalPoint(netTotal1)

                } else if (taxType.matches("Z".toRegex())) {
                    netTotal1 = subTotal

                    if(shortCodeStr!!.equals("FUXIN",true)){
                        taxValueText!!.text = "0.0000"
                        val ProdNetTotal = Utils.fourDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.fourDecimalPoint(subTotal)

                    }else{
                        taxValueText!!.text = "0.0"
                        val ProdNetTotal = Utils.twoDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)

                    }
                    // netTotal1 = subTotal + taxAmount;
                    bill_disc_subtotal_txtl!!.text =Utils.twoDecimalPoint(netTotal1)
                    taxTitle!!.text = "GST ( Zero )"
                } else {
                    if(shortCodeStr!!.equals("FUXIN",true)) {
                        taxValueText!!.text = "0.0000"
                        subTotalValue!!.text = Utils.fourDecimalPoint(subTotal)
                    }else{
                        taxValueText!!.text = "0.0"
                        subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)

                    }
                    netTotalValue!!.text = "" + Prodtotal
                    bill_disc_subtotal_txtl!!.text =Prodtotal
                    taxTitle!!.text = "GST ( Zero )"
                }
            } else if (taxValue.matches("".toRegex())) {
                taxValueText!!.text = "0.0"
                netTotalValue!!.text = "" + Prodtotal
                if(shortCodeStr!!.equals("FUXIN",true)) {
                    subTotalValue!!.text = Utils.fourDecimalPoint(subTotal)
                }else{
                    subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                }
                bill_disc_subtotal_txtl!!.text =Prodtotal
                taxTitle!!.text = "GST ( Zero )"
            } else {
                taxValueText!!.text = "0.0"
                netTotalValue!!.text = "" + Prodtotal
                if(shortCodeStr!!.equals("FUXIN",true)) {
                    subTotalValue!!.text = Utils.fourDecimalPoint(subTotal)
                }else{
                    subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                }
                bill_disc_subtotal_txtl!!.text =Prodtotal

                taxTitle!!.text = "GST ( Zero )"
            }

            if (subTotal < 0) {
                bill_disc_amt_ed!!.setText("")
                bill_disc_percent_ed!!.setText("")
                //billDiscAmount.clearFocus()
                //billDiscPercentage.clearFocus()
                Toast.makeText(this,  "Discount amount exceed..!", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            Log.w("Error_Throwing1::", e.message!!)

        }
    }

    fun setCalculationView() {
        try {
            val taxAmount = 0.0
            val netTotal = 0.0
            var taxAmount1 = 0.0
            var netTotal1 = 0.0
            var return_qty = 0.0
            val pcspercarton = 0.0
            val cqtyCalc = 0.0
            val lqtyCalc = 0.0
            var net_qty = 0.0
            val sharedPreferences = getSharedPreferences("customerPref", MODE_PRIVATE)
            val selectCustomerId = sharedPreferences.getString("customerId", "")
            if (selectCustomerId != null && !selectCustomerId.isEmpty()) {
                customerDetails = dbHelper!!.getCustomer(selectCustomerId)
            }
            val taxValue = customerDetails!![0].taxPerc
            val taxType = customerDetails!![0].taxType
            Log.w("TaxType12:", taxType)
            Log.w("TaxValue12:", taxValue)
            var price = priceText!!.text.toString()
            Log.w("priceIn2:", price)
            var qty = qtyValue!!.text.toString()
            var disc = item_discount_ed!!.text.toString()
            var exchang = exchange_inv!!.text.toString()
            if (disc.matches("".toRegex())) {
                disc = "0"
            }
            if (exchang.matches("".toRegex())) {
                exchang = "0"
            }
            if (price.matches("".toRegex())) {
                price = "0"
            }
            if (qty.matches("".toRegex())) {
                qty = "0"
            }
            val cPriceCalc = price.toDouble()
            if (!returnQtyText!!.text.toString()
                    .isEmpty() && returnQtyText!!.text.toString() != "null"
            ) {
                return_qty = returnQtyText!!.text.toString().toDouble()
                netReturnQty!!.text = return_qty.toString()
            } else {
                netReturnQty!!.text = "0"
                expiryReturnQty!!.setText("")
                damageReturnQty!!.setText("")
            }
            var Prodtotal = "0.0" ;
            net_qty = qty.toDouble() - return_qty
            var tt = net_qty * cPriceCalc - discountStr.toDouble()
            Log.w("dis_invStr", "$tt..$disc")
            Log.w("TOTALVALUES:", tt.toString())
            if(shortCodeStr!!.equals("FUXIN",true)) {
                Prodtotal = Utils.fourDecimalPoint(tt)
            }else{
                 Prodtotal = Utils.twoDecimalPoint(tt)
            }
            var subTotal = 0.0
            val itemDisc = discountEditext!!.text.toString()
            subTotal = if (!itemDisc.matches("".toRegex())) {
                val itmDisc = itemDisc.toDouble()
                tt - itmDisc
            } else {
                tt
            }
            val sbTtl = Utils.twoDecimalPoint(subTotal)


            /*  if (return_qty!=0){
                double return_amt=0.0;
                return_amt=(return_qty*cPriceCalc);
                subTotal=subTotal-return_amt;
            }*/

            // sl_total_inclusive.setText("" + sbTtl);
            tt = subTotal
            Log.w("SubTotalValues:", subTotal.toString())
            if (!taxType.matches("".toRegex()) && !taxValue.matches("".toRegex())) {
                val taxValueCalc = taxValue.toDouble()

                if (taxType.matches("E".toRegex())) {
                    taxAmount1 = subTotal * taxValueCalc / 100
                    netTotal1 = subTotal + taxAmount1
                    taxTitle!!.text = "GST ( Exc ) :" + taxValue.toDouble().toInt() + " % "

                    if(shortCodeStr!!.equals("FUXIN",true)){
                        val prodTax = Utils.fourDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax
                        val ProdNetTotal = Utils.fourDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.fourDecimalPoint(subTotal)
                        bill_disc_subtotal_txtl!!.text = ProdNetTotal

                    }else{
                        val prodTax = Utils.twoDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax
                        val ProdNetTotal = Utils.twoDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                        bill_disc_subtotal_txtl!!.text = ProdNetTotal
                    }

                } else if (taxType.matches("I".toRegex())) {
                    taxAmount1 = subTotal * taxValueCalc / (100 + taxValueCalc)
                    netTotal1 = subTotal
                    taxTitle!!.text = "GST ( Inc ) : " + taxValue.toDouble().toInt() + " % "

                    // netTotal1 = subTotal + taxAmount1;
                    if(shortCodeStr!!.equals("FUXIN",true)){
                        val prodTax = Utils.fourDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax
                        val ProdNetTotal = Utils.fourDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        val dTotalIncl = netTotal1 - taxAmount1
                        val totalIncl = Utils.fourDecimalPoint(dTotalIncl)
                        val sub_total = subTotal - taxAmount1
                        subTotalValue!!.text = Utils.fourDecimalPoint(sub_total)
                        bill_disc_subtotal_txtl!!.text = ProdNetTotal
                    }else{
                        val prodTax = Utils.twoDecimalPoint(taxAmount1)
                        taxValueText!!.text = "" + prodTax
                        val ProdNetTotal = Utils.twoDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        val dTotalIncl = netTotal1 - taxAmount1
                        val totalIncl = Utils.twoDecimalPoint(dTotalIncl)
                        val sub_total = subTotal - taxAmount1
                        subTotalValue!!.text = Utils.twoDecimalPoint(sub_total)
                        bill_disc_subtotal_txtl!!.text = ProdNetTotal
                    }

                } else if (taxType.matches("Z".toRegex())) {
                    taxValueText!!.text = "0.0"
                    // netTotal1 = subTotal + taxAmount;
                    netTotal1 = subTotal
                    if(shortCodeStr!!.equals("FUXIN",true)){
                        val ProdNetTotal = Utils.fourDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.fourDecimalPoint(subTotal)
                        bill_disc_subtotal_txtl!!.text = ProdNetTotal
                    }else{
                        val ProdNetTotal = Utils.twoDecimalPoint(netTotal1)
                        netTotalValue!!.text = "" + ProdNetTotal
                        subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                        bill_disc_subtotal_txtl!!.text = ProdNetTotal
                    }
                    taxTitle!!.text = "GST ( Zero )"
                } else {
                    if(shortCodeStr!!.equals("FUXIN",true)){
                        taxValueText!!.text = "0.0000"

                    }else{
                        taxValueText!!.text = "0.0"
                    }
                    netTotalValue!!.text = "" + Prodtotal
                    subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                    bill_disc_subtotal_txtl!!.text = Prodtotal
                    taxTitle!!.text = "GST ( Zero )"
                }
            } else if (taxValue.matches("".toRegex())) {
                if(shortCodeStr!!.equals("FUXIN",true)){
                    taxValueText!!.text = "0.0000"

                }else {
                    taxValueText!!.text = "0.0"
                }
                netTotalValue!!.text = "" + Prodtotal
                subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                bill_disc_subtotal_txtl!!.text =Prodtotal

                taxTitle!!.text = "GST ( Zero )"
            } else {
                if(shortCodeStr!!.equals("FUXIN",true)){
                    taxValueText!!.text = "0.0000"

                }else {
                    taxValueText!!.text = "0.0"
                }
                netTotalValue!!.text = "" + Prodtotal
                subTotalValue!!.text = Utils.twoDecimalPoint(subTotal)
                bill_disc_subtotal_txtl!!.text =Prodtotal

                taxTitle!!.text = "GST ( Zero )"
            }
            setButtonView()
        } catch (e: Exception) {
            Log.w("Error_Throwing::", e.message!!)
        }
    }


    fun setButtonView() {
        val products = dbHelper!!.allInvoiceProducts

        if (!focEditText!!.text.toString().isEmpty() && focEditText!!.text.toString() != "0") {
            addProduct!!.alpha = 0.9f
            addProduct!!.isEnabled = true
        }
//        if ((netTotalValue!!.text.toString().toDouble() > 0.00 )&& (products!!.size>0)) {
//            down_menu!!.isVisible = true
//        }
//        else{
//            down_menu!!.isVisible = false
//        }
        if (netTotalValue!!.text.toString().toDouble() > 0.00) {
            addProduct!!.alpha = 0.9f
            addProduct!!.isEnabled = true
        } else {
            if (!productName_bottomEd!!.text.toString().isEmpty()) {
                //  if ((!focEditText.getText().toString().isEmpty() && !focEditText.getText().toString().equals("0")) || (!returnQtyText.getText().toString().isEmpty() && !returnQtyText.getText().toString().equals("0"))) {
                if (!qtyValue!!.text.toString()
                        .isEmpty() && qtyValue!!.text.toString() != "0" && priceText!!.text.toString()
                        .toInt() > 0
                ) {
                    if (!priceText!!.text.toString().isEmpty() && priceText!!.text.toString()
                            .toDouble() > 0.00
                    ) {
                        addProduct!!.alpha = 0.9f
                        addProduct!!.isEnabled = true
                    } else {
                        addProduct!!.alpha = 0.4f
                        addProduct!!.isEnabled = false
                        Toast.makeText(applicationContext, "Price is Empty..!", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    addProduct!!.alpha = 0.9f
                    addProduct!!.isEnabled = true
                }
                //         }

//                else {
//                    addProduct.setAlpha(0.4F);
//                    addProduct.setEnabled(false);
//                }
            } else {
                addProduct!!.alpha = 0.4f
                addProduct!!.isEnabled = false
            }
        }
    }

    //    public void setButtonView() {
    //        Log.w("focentr22","");
    //        if (Double.parseDouble(netTotalValue.getText().toString()) > 0.00) {
    //            addProduct.setAlpha(0.9F);
    //            addProduct.setEnabled(true);
    //        } else {
    //            if (!productNameEditext.getText().toString().isEmpty()) {
    //                if ((!focEditText.getText().toString().isEmpty() && !focEditText.getText().toString().equals("0"))) {
    //                    Log.w("focentr","");
    //                    addProduct.setAlpha(0.9F);
    //                    addProduct.setEnabled(true);
    //                }
    //                else {
    //                    if (!qtyValue.getText().toString().isEmpty() && !qtyValue.getText().toString().equals("0") && Integer.parseInt(priceText.getText().toString()) > 0) {
    //                        if (!priceText.getText().toString().isEmpty() && Double.parseDouble(priceText.getText().toString()) > 0.00) {
    //                            addProduct.setAlpha(0.9F);
    //                            addProduct.setEnabled(true);
    //                        } else {
    //                            addProduct.setAlpha(0.4F);
    //                            addProduct.setEnabled(false);
    //                            Toast.makeText(getApplicationContext(), "Price is Empty..!", Toast.LENGTH_SHORT).show();
    //                        }
    //                    } else {
    //                        addProduct.setAlpha(0.9F);
    //                        addProduct.setEnabled(true);
    //                    }
    //                }
    //            }
    //            else{
    //                addProduct.setAlpha(0.4F);
    //                addProduct.setEnabled(false);
    //            }
    //        }
    //    }
    fun getDeliveryAddress(jsonObject: JSONObject) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "CustomerAddress"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_delive_addrs_URL:", url + jsonObject.toString())
        deliveryAddrList = ArrayList()
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.setTitleText("Loading Address...")
        pDialog.setCancelable(false)
        pDialog.show()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject ->
                try {
                    Log.w("Res_delive_addrs:", response.toString())
                    // Loop through the array elements
                    val uomArray = response.optJSONArray("responseData")
                    if (uomArray != null && uomArray.length() > 0) {
                        for (j in 0 until uomArray.length()) {
                            val uomObject = uomArray.getJSONObject(j)
                            val model = DeliveryAddressModel()
                            model.deliveryAddress = uomObject.optString("address")
                            //                        model.setDeliveryAddressCode(uomObject.optString("uomName"));
                            deliveryAddrList!!.add(model)
                        }
                    }
                    Log.w("deliveryTxt:", uomArray.toString())
                    pDialog.dismiss()
                    if (deliveryAddrList!!.size > 0) {
//                    runOnUiThread(() -> {
                        setDeliveryAddrList(deliveryAddrList!!)
                        //  });
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w("Error:", Objects.requireNonNull(e.message!!))
                }
            },
            Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                pDialog.dismiss()
                Log.w("Error_throwing:", error.toString())
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

    fun getSettlementDateApi() {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "settlementByDate"
        // Initialize a new JsonArrayRequest instance
        val jsonObject = JSONObject()
        try {
            jsonObject.put("User", username)
            jsonObject.put("LocationCode", locationCode)
            jsonObject.put("FromDate", "")
            jsonObject.put("ToDate", "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.w("Given_settlem_URL:", url + jsonObject.toString())
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.setTitleText("Loading ...")
        pDialog.setCancelable(false)
       // pDialog.show()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject ->
                try {
                    Log.w("Res_settlemDat:", response.toString())
                    // Loop through the array elements
                    val resData = response.optString("responseData")
                    if (resData.equals("True", ignoreCase = true)) {
                        val invoiceDate: String = Utils.getNextInvoiceDate(invoiceDateInv!!.getText().toString())
                        Log.w("GivenPrintInvoiceDate:", invoiceDate)
                        val invoiceDay: String = Utils.getDayOfTheDate(invoiceDate)
                        Log.w("GivenPrintInvoiceDay:", invoiceDay)
                        //                           if (invoiceDay.equals("Sunday")){
//                               String inv_date=Utils.getNextInvoiceDate(invoiceDate);
//                               dateEditext.setText(inv_date);
//                               dateEditext.setEnabled(false);
//                           }else {
                        Utils.setInvoiceDate(invoiceDate)
                        invoiceDateInv!!.setText(invoiceDate)
                        invoiceDateInv!!.setEnabled(false)
                        //  }
                    } else {
                        val invoiceDay: String = Utils.getDayOfTheDate(invoiceDateInv!!.getText().toString())
                        Log.w("GivenPrintInvoiceDay:", invoiceDay)
                        //if (invoiceDay.equals("Sunday")){
//                                String inv_date=Utils.getNextInvoiceDate(dateEditext.getText().toString());
//                                dateEditext.setText(inv_date);
//                                dateEditext.setEnabled(false);
                        // }
                    }

                    pDialog.dismiss()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w("Errory:", Objects.requireNonNull(e.message!!))
                }
            },
            Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                pDialog.dismiss()
                Log.w("Error_throwing:", error.toString())
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
    fun getUOM(jsonObject: JSONObject) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "ItemUOMDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_UOM_URL:", url + jsonObject.toString())
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.setTitleText("Loading UOM...")
        pDialog.setCancelable(false)
        pDialog.show()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject ->
                try {
                    uomList = ArrayList()
                    Log.w("Res_UOM:", response.toString())
                    // Loop through the array elements
                    val uomArray = response.optJSONArray("responseData")
                    if (uomArray != null && uomArray.length() > 0) {
                        for (j in 0 until uomArray.length()) {
                            val uomObject = uomArray.getJSONObject(j)
                            val uomModel = UomModel()
                            uomModel.uomCode = uomObject.optString("uomCode")
                            uomModel.uomName = uomObject.optString("uomName")
                            uomModel.uomEntry = uomObject.optString("uomEntry")
                            uomModel.altQty = uomObject.optString("altQty")
                            uomModel.baseQty = uomObject.optString("baseQty")
                            uomModel.price = uomObject.optString("price")
                            uomList!!.add(uomModel)
                        }
                    }
                    Log.w("UOM_TEXT:", uomArray.toString())
                    pDialog.dismiss()
                    if (uomList!!.size > 0) {
                        runOnUiThread {
                            if(ischangeUOM) {
                                setUomList(uomList!!)
                            }else{
                                defaultUOMset(uomList!!)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w("Errory:", Objects.requireNonNull(e.message!!))
                }
            },
            Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                pDialog.dismiss()
                Log.w("Error_throwing:", error.toString())
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

    fun getUOMEdit(jsonObject: JSONObject, uomcode:String, pdtStockStr :String) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "ItemUOMDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_UOM_URL:", url + jsonObject.toString())
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.setTitleText("Loading UOM...")
        pDialog.setCancelable(false)
        pDialog.show()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject ->
                try {
                    uomListEdit = ArrayList()
                    Log.w("Res_UOM:", response.toString())
                    // Loop through the array elements
                    val uomArray = response.optJSONArray("responseData")
                    if (uomArray != null && uomArray.length() > 0) {
                        for (j in 0 until uomArray.length()) {
                            val uomObject = uomArray.getJSONObject(j)
                            val uomModel = UomModel()
                            uomModel.uomCode = uomObject.optString("uomCode")
                            uomModel.uomName = uomObject.optString("uomName")
                            uomModel.uomEntry = uomObject.optString("uomEntry")
                            uomModel.altQty = uomObject.optString("altQty")
                            uomModel.baseQty = uomObject.optString("baseQty")
                            uomModel.price = uomObject.optString("price")
                            uomListEdit!!.add(uomModel)
                        }
                    }
                    Log.w("UOM_TEXT:", uomArray.toString())
                    pDialog.dismiss()
                    var pdtStockl = 0.0
                    if (uomListEdit!!.size > 0) {
                        for (i in uomListEdit!!.indices) {
                            if (uomcode.equals("CTN", true)) {
                                stockLayout!!.visibility = View.VISIBLE
                                var baseCtnQty = uomListEdit!![i].baseQty.toDouble()
                                pdtStockl = pdtStockStr.toDouble()
                                var ctnStockVal = pdtStockl / baseCtnQty!!
                                stockQtyValue!!.setTextColor(Color.parseColor("#2ECC71"))

                                stockQtyValue!!.setText(twoDecimalPoint(ctnStockVal).toString())
                                Log.w("baseCtnQtyEditaa", "" + baseCtnQty)
                            } else {
                                stockLayout!!.visibility = View.VISIBLE
                                stockQtyValue!!.setTextColor(Color.parseColor("#2ECC71"))

                                stockQtyValue!!.text = pdtStockStr
                            }
                        }
//                        runOnUiThread {
//                            if(ischangeUOM) {
//                                setUomList(uomList!!)
//                            }else{
//                                defaultUOMset(uomList!!)
//                            }
//                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w("Errory:", Objects.requireNonNull(e.message!!))
                }
            },
            Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                pDialog.dismiss()
                Log.w("Error_throwing:", error.toString())
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
    fun billDiscPref(){
        var pref_bill_disc_amt1: String = ""
        var pref_bill_disc_percent1: String = ""

        if(bill_disc_amt_ed!!.text.toString().isNotEmpty()){
             pref_bill_disc_amt1 = bill_disc_amt_ed!!.text.toString()
        }
        if(bill_disc_percent_ed!!.text.toString().isNotEmpty()){
             pref_bill_disc_percent1 = bill_disc_percent_ed!!.text.toString()
        }

        myEdit!!.putString("billDisc_amt", pref_bill_disc_amt1)
        myEdit!!.putString("billDisc_percent", pref_bill_disc_percent1)
        myEdit!!.apply()

        pref_bill_disc_amt = sharedPref_billdisc!!.getString("billDisc_amt", "")
        pref_bill_disc_percent = sharedPref_billdisc!!.getString("billDisc_percent", "")
    }

    fun getAllProducts(jsonObject: JSONObject) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "CustomerProductList"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_SAP_PROUCT_URL:", url + jsonObject.toString())
        productList = ArrayList()
        products = ArrayList()
        //  SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        //  pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        //  pDialog.setTitleText("Loading Products...");
        //  pDialog.setCancelable(false);
        //  pDialog.show();
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject ->
                try {
                    //   progressDialog.dismiss();
                    //  {"productCode":"FG\/001245","productName":"RUM","companyName":"","supplierCategoryNumber":"","uomCode":"Ctn",
                    //  "uomName":"Carton","cartonPrice":"3000.000000","piecePrice":"0.000000","pcsPerCarton":"100.000000",
                    //  "price":"100.000000","taxType":"E","havTax":"Y","taxCode":"SR","taxRate":"7.000000","barCode":"",
                    //  "isActive":"N","createUser":"1","createDate":"13\/07\/2021","modifyDate":"29\/07\/2021","remarks":"",
                    //  "warehouse":"01","stockInHand":"0.000000","averagePrice":0,"manageBatchOrSerial":"None","manageBatchNumber":"N",
                    //  "manageSerialNumber":"N","batchNumber":"","expiryDate":null,"manufactureDate":"31\/07\/2021","imageURL":""}

                    Log.w("Response_SAP_PRODUCTS:", response.toString())
                    // Loop through the array elements
                    val productArray = response.optJSONArray("responseData")
                    for (i in 0 until Objects.requireNonNull(productArray).length()) {
                        // Get current json object
                        val productObject = productArray.getJSONObject(i)
                        val product = ProductsModel()
                        if (productObject.optString("isActive") == "N") {
                            product.companyCode = "1"
                            // Adding bp name for products
                            if (productObject.optString("bP_Description") != null && !productObject.optString(
                                    "bP_Description"
                                ).isEmpty() && productObject.optString("bP_Description") != "null"
                            ) {
                                product.productName = productObject.optString("bP_Description")
                            } else {
                                product.productName = productObject.optString("productName")
                            }
                            if (productObject.optString("bP_CatalogNo") != null) {
                                product.customerItemCode = productObject.optString("bP_CatalogNo")
                            }
                            product.productCode = productObject.optString("productCode")
                            product.weight = ""
                            product.productImage = productObject.optString("imageURL")
                            product.wholeSalePrice = "0.00"
                            product.retailPrice = productObject.optDouble("retailPrice")
                            product.barcode = productObject.optString("barCode")
                            product.cartonPrice = productObject.optString("cartonPrice")
                            product.pcsPerCarton = productObject.optString("pcsPerCarton")
                            product.unitCost = productObject.optString("price")
                            product.lastPrice = productObject.optString("lastSalesPrice")
                            product.minimumSellingPrice =
                                productObject.optString("minimumSellingPrice")
                            product.defaultUom = productObject.optString("defaultSalesUOM")
                            if (productObject.optString("stockInHand") != "null") {
                                product.stockQty = productObject.optString("stockInHand")
                            } else {
                                product.stockQty = "0"
                            }
                            product.uomCode = productObject.optString("uomCode")
                            product.isItemFOC = productObject.optString("itemAllowFOC")
                            //  product.setProductBarcode(productObject.optString("BarCode")); Add values In Futue
                           // product.productBarcode = ""

                            /*  ArrayList<UomModel> uomList=new ArrayList<>();
                                JSONArray uomArray=productObject.optJSONArray("uomDetails");
                                if (uomArray!=null && uomArray.length() > 0){
                                    for (int j = 0; j< uomArray.length(); j++){
                                        JSONObject uomObject = uomArray.getJSONObject(j);
                                        UomModel uomModel =new UomModel();
                                        uomModel.setUomCode(uomObject.optString("uomCode"));
                                        uomModel.setUomName(uomObject.optString("uomName"));
                                        uomModel.setUomEntry(uomObject.optString("uomEntry"));
                                        uomModel.setAltQty(uomObject.optString("altQty"));
                                        uomModel.setBaseQty(uomObject.optString("baseQty"));

                                        uomList.add(uomModel);
                                    }
                                }
                                product.setUomText(uomList.toString());
                                product.setProductUOMList(uomList);*/

                            productList!!.add(product)
                        }
                    }
                    HomePageModel.productsList = ArrayList()
//                    productList!!.get(0).barcode = "1234566"
//                    productList!!.get(1).barcode = "888801566968"
                    setAdapter(productList)
                    HomePageModel.productsList.addAll(productList!!)
                    // pDialog.dismiss();
                    if (productList!!.size > 0) {
                        runOnUiThread {
                            AppUtils.setProductsList(productList)

                           // setProductsDisplay("All Products")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w("Errorn:", Objects.requireNonNull(e.message!!))                }
            },
            Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                // pDialog.dismiss();
                Log.w("Error_throwing:", error.toString())
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

    fun isInRange(miniVal: Double, maxVal: Double, edTxt: Double): Boolean {
        return if (maxVal > miniVal) edTxt in miniVal..maxVal else edTxt in maxVal..miniVal
    }

    @SuppressLint("SetTextI18n")
    private fun setAdapter(productList: ArrayList<ProductsModel>?) {
        // Get the Settings to Display the Product list with the Settings
        if (settings!!.size > 0) {
            for (model in settings!!) {
                if (model.settingName == "stock_view") {
                    if (model.settingValue == "1") {
                        stockProductView = "1"
                    } else if (model.settingValue == "2") {
                        stockProductView = "2"
                    } else {
                        stockProductView = "0"
                    }
                }
            }
        }

        // Filter the products list depending the Settings
        val filteredProducts = ArrayList<ProductsModel>()
        //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
        if (productList != null && productList.size > 0) {
            for (product in productList) {
                if (stockProductView == "1") {
                    if (product.stockQty != null && product.stockQty != "null") {
                        if (product.stockQty.toDouble() > 0) {
                            filteredProducts.add(product)
                        }
                    }
                } else if (stockProductView == "0") {
                    if (product.stockQty != null && product.stockQty != "null") {
                        if (product.stockQty.toDouble() < 0 || product.stockQty.toDouble() == 0.0) {
                            filteredProducts.add(product)
                        }
                    }
                } else {
                    filteredProducts.add(product)
                }
            }
        }

        if (filteredProducts.size > 0) {
            productLayout!!.visibility = View.VISIBLE
            emptyLayout!!.visibility = View.GONE
        } else {
            productLayout!!.visibility = View.GONE
            emptyLayout!!.visibility = View.VISIBLE
        }
        productListView!!.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        selectProductAdapter = SelectProductAdapter(
            this,
            filteredProducts
        ) { model -> // Need to implement the Product price Later
            /* try {
                        getProductPrice(model.getProductCode());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
            productAutoComplete!!.setText(model.productName + " - " + model.productCode)
            productId = model.productCode
            productName = model.productName
            if (isUomSetting) {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("CustomerCode", selectCustomerId)
                    jsonObject.put("ItemCode", model.productCode)
                    getUOM(jsonObject)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.w("Errors:", Objects.requireNonNull(e.message!!))                }
            }
            // setUomList(model.getProductUOMList());
            uomTextView!!.text = model.uomText
            if(shortCodeStr.equals("FUXIN",true)) {
                if (isLastPrice.equals("True", true)) {
                    priceText!!.setText(model.lastPrice)
                } else {
                    priceText!!.setText(model.unitCost)
                }
            }
            else{
                if (model.lastPrice != null && !model.lastPrice.isEmpty() &&
                    model.lastPrice.toDouble() > 0.00
                ) {
                    priceText!!.setText(model.lastPrice)
                } else {
                    priceText!!.setText(model.unitCost)
                }
            }

            //  uomText.setText(model.getUomCode());
            stockCount!!.setText(model.stockQty)
            // looseQtyValue.setEnabled(true);
            qtyValue!!.setText("")
            focEditText!!.isEnabled = true
            exchangeEditext!!.isEnabled = true
            discountEditext!!.isEnabled = true
            returnQtyText!!.isEnabled = true
            item_discount_ed!!.isEnabled = true
            exchange_inv!!.isEnabled = true

            if (model.minimumSellingPrice != null && !model.minimumSellingPrice.isEmpty()) {
                minimumSellingPriceText!!.text = model.minimumSellingPrice
            } else {
                minimumSellingPriceText!!.text = "0.00"
            }
            stockLayout!!.visibility = View.VISIBLE
            if (model.stockQty != null && model.stockQty != "null") {
                pdtStockVal = model.stockQty
                if (model.stockQty.toDouble() == 0.0 || model.stockQty.toDouble() < 0) {
                    stockQtyValue!!.text = model.stockQty
                    stockQtyValue!!.setTextColor(Color.parseColor("#D24848"))
                } else if (model.stockQty.toDouble() > 0) {
                    stockQtyValue!!.setTextColor(Color.parseColor("#2ECC71"))
                    stockQtyValue!!.text = model.stockQty
                }
            }
            qtyValue!!.requestFocus()
            openKeyborard(qtyValue)
            viewCloseBottomSheet()
        }

        // products.add(productObject.optString("ProductName")+" - "+productObject.optString("ProductCode"));
        products = ArrayList()
        if (filteredProducts != null && filteredProducts.size > 0) {
            for (product in filteredProducts) {
                products!!.add(product.productName + " ~ " + product.productCode)
            }
        }
        productListView!!.adapter = selectProductAdapter
        autoCompleteAdapter =
            object : ArrayAdapter<String>(this, android.R.layout.select_dialog_item, products!!) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getView(position, convertView, parent)
                    (v as TextView).textSize = 14f
                    /*    Typeface Type = getFont () ;  // custom method to get a font from "assets" folder
                ((TextView) v).setTypeface(Type);
                ((TextView) v).setTextColor(YourColor);*/
                    v.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                    return v
                }
            }
        productAutoComplete!!.threshold = 1
        productAutoComplete!!.setAdapter(autoCompleteAdapter)
        totalProducts!!.text = filteredProducts.size.toString() + " Products"
        //dialog.dismiss();
    }

    fun createSalesOrderJson(copy: Int) {

        // JSONObject rootJsonObject = new JSONObject();
        val rootJsonObject = JSONObject()
        val invoiceDetailsArray = JSONArray()
        var invoiceObject = JSONObject()

        //  {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"CUS\/686","customerName":"VH FACTORY","groupCode":"100",
        //  "contactPerson":"","creditLimit":"150.000000","currencyCode":"SGD","currencyName":"Singapore Dollar","taxType":"","taxCode":"SR",
        //  "taxName":"Sales Standard Rated Supplier SR","taxPercentage":"7.000000","balance":"21.600000","outstandingAmount":"128.400000",
        //  "address":"","street":"","city":"","state":"","zipCode":"","country":"","createDate":"13\/07\/2021","updateDate":"30\/07\/2021",
        //  "active":"N","remark":""}]}
        val detailsArray = customerResponse.optJSONArray("responseData")
        val `object` = detailsArray.optJSONObject(0)
        try {
            // Sales Header Add values
            /*  if (activityFrom.equals("InvoiceEdit")){
                rootJsonObject.put("invoiceNumber", AddInvoiceActivity.editInvoiceNumber);
                rootJsonObject.put("mode", "E");
            }else if (activityFrom.equals("ConvertInvoiceFromDO")){
                rootJsonObject.put("mode", "I");
                rootJsonObject.put("soNo","");
                rootJsonObject.put("doNo",AddInvoiceActivity.editDoNumber);
                rootJsonObject.put("invoiceNumber", "");
            }
            else if (activityFrom.equals("ConvertInvoice")){
                rootJsonObject.put("mode", "I");
                rootJsonObject.put("soNo",AddInvoiceActivity.editSoNumber);
                rootJsonObject.put("invoiceNumber", "");
            }else {
                rootJsonObject.put("invoiceNumber", "");
                rootJsonObject.put("mode", "I");
            }*/
            if (activityFrom == "SalesEdit") {
                rootJsonObject.put("soNumber", editSoNumber.toString())
                rootJsonObject.put("mode", "E")
                rootJsonObject.put("status", "O")
            } else {
                rootJsonObject.put("soNumber", "")
                rootJsonObject.put("mode", "I")
                rootJsonObject.put("status", "")
            }
            if (currentSaveDateTime == null || currentSaveDateTime!!.isEmpty()) {
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val currentDateandTime = sdf.format(Date())
                currentSaveDateTime = currentDateandTime
            }
            rootJsonObject.put("customerReferenceNo", orderNoText!!.text.toString())
            rootJsonObject.put("soDate", currentDate)
            rootJsonObject.put("currentDateTime", currentSaveDateTime)
            rootJsonObject.put("customerCode", `object`["customerCode"])
            rootJsonObject.put("customerName", `object`["customerName"])
            rootJsonObject.put("address", `object`["address"])
            rootJsonObject.put("street", `object`["street"])
            rootJsonObject.put("city", `object`["city"])
            rootJsonObject.put("creditLimit", `object`["creditLimit"])
            rootJsonObject.put("remark", remarkStr)
            rootJsonObject.put("currencyName", "Singapore Dollar")
            rootJsonObject.put("taxTotal", taxValueText!!.text.toString())
            rootJsonObject.put("subTotal", subTotalValue!!.text.toString())
            rootJsonObject.put("total", subTotalValue!!.text.toString())
            rootJsonObject.put("netTotal", netTotalValue!!.text.toString())
            rootJsonObject.put("itemDiscount", "0.00")
            rootJsonObject.put("billDiscount", "0.00")
            rootJsonObject.put("totalDiscount", "0")
            rootJsonObject.put("billDiscountPercentage", "0.00")
            rootJsonObject.put("deliveryCode", SettingUtils.getDeliveryAddressCode())
            rootJsonObject.put("delCustomerName", "")
            rootJsonObject.put("delAddress1", deliverAddrNameStr)
            rootJsonObject.put("delAddress2 ", `object`.optString("delAddress2"))
            rootJsonObject.put("delAddress3 ", `object`.optString("delAddress3"))
            rootJsonObject.put("delPhoneNo", `object`.optString("contactNo"))
            rootJsonObject.put("remark", `object`.optString("remark"))
            rootJsonObject.put("haveTax", `object`.optString("haveTax"))
            rootJsonObject.put("taxType", `object`.optString("taxType"))
            rootJsonObject.put("taxPerc", `object`.optString("taxPercentage"))
            rootJsonObject.put("taxCode", `object`.optString("taxCode"))
            rootJsonObject.put("currencyCode", `object`.optString("currencyCode"))
            rootJsonObject.put("currencyValue", "")
            rootJsonObject.put("CurrencyRate", "1")
            rootJsonObject.put("postalCode", `object`.optString("postalCode"))
            rootJsonObject.put("createUser", username)
            rootJsonObject.put("modifyUser", username)
            rootJsonObject.put("companyName", companyName)
            rootJsonObject.put("stockUpdated", "1")
            rootJsonObject.put("invoiceType", "M")
            rootJsonObject.put("companyCode", companyCode)
            rootJsonObject.put("locationCode", locationCode)
            rootJsonObject.put("latitude", current_latitude)
            rootJsonObject.put("longitude", current_longitude)
            rootJsonObject.put("CurrentAddress", currentAddressl)
            rootJsonObject.put("image", imageString)
            rootJsonObject.put("signature", signatureString)

            // Sales Details Add to the Objects
            val localCart = dbHelper!!.allInvoiceProducts
            var index = 1
            for (model in localCart) {
                invoiceObject = JSONObject()
                /*   if (activityFrom.equals("InvoiceEdit")){
                    rootJsonObject.put("invoiceNumber", AddInvoiceActivity.editInvoiceNumber);
                }else {
                    rootJsonObject.put("invoiceNumber", "");
                }*/invoiceObject.put("companyCode", companyCode)
                invoiceObject.put("invoiceDate", currentDate)
                invoiceObject.put("slNo", index)
                invoiceObject.put("productCode", model.productCode)
                invoiceObject.put("productName", model.productName)
                invoiceObject.put("qty", model.actualQty.toString())
                // convert into int
                invoiceObject.put("price", Utils.fourDecimalPoint(model.price.toDouble()))
                invoiceObject.put("total", Utils.twoDecimalPoint(model.total.toDouble()))
                invoiceObject.put("itemDiscount", "0.00")
                invoiceObject.put("totalTax", Utils.twoDecimalPoint(model.gstAmount.toDouble()))
                invoiceObject.put("subTotal", Utils.twoDecimalPoint(model.subTotal.toDouble()))
                invoiceObject.put("netTotal", Utils.twoDecimalPoint(model.netTotal.toDouble()))
                invoiceObject.put("taxType", `object`.optString("taxType"))
                invoiceObject.put("taxPerc", `object`.optString("taxPercentage"))
                var return_subtotal = 0.0
                if (model.returnQty != null && !model.returnQty.isEmpty() && model.returnQty != "null") {
                    return_subtotal = model.returnQty.toDouble() * model.price.toDouble()
                }
                assert(model.returnQty != null)
                if (!model.returnQty.isEmpty() && model.returnQty.toString() != "null") {
                    invoiceObject.put("returnLQty", model.returnQty)
                    invoiceObject.put("returnQty", model.returnQty)
                } else {
                    invoiceObject.put("returnLQty", "0")
                    invoiceObject.put("returnQty", "0")
                }
                if (!model.focQty.toString().isEmpty() && model.focQty != "null") {
                    invoiceObject.put("focQty", model.focQty)
                } else {
                    invoiceObject.put("focQty", "0")
                }
                Log.w("cartPricssOO", "" + twoDecimalPoint((model.price.toDouble())))

                invoiceObject.put("returnSubTotal", return_subtotal.toString() + "")
                invoiceObject.put("returnNetTotal", return_subtotal.toString() + "")
                invoiceObject.put("taxCode", `object`.optString("taxCode"))
                invoiceObject.put("uomCode", model.uomCode)
                invoiceObject.put("itemRemarks", "")
                invoiceObject.put("locationCode", locationCode)
                invoiceObject.put("createUser", username)
                invoiceObject.put("modifyUser", username)
                invoiceDetailsArray.put(invoiceObject)
                index++
            }
            rootJsonObject.put("PostingSalesOrderDetails", invoiceDetailsArray)
            Log.w("RootJsonForSaveSO:", rootJsonObject.toString())
            saveSalesOrder(rootJsonObject, "SalesOrder", copy)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.w("Errore:", Objects.requireNonNull(e.message!!))
        }
    }

    fun billDiscountBottomSheet() {
        hideKeyboard()
        prod_bottom_layout!!.visibility = View.GONE
        bill_disc_layl!!.visibility = View.VISIBLE
        //  hideKeyboard();
        if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior!!.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        } else {
            behavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }


    }

//    private fun showBottom_BillDialog(pos: Int) {
//        bottomSheetDialog = BottomSheetDialog(this)
//        bottomSheetDialog.setContentView(R.layout.bottom_sheet_billdisc_view)
//        delete_dob = bottomSheetDialog.findViewById<TextView>(R.id.delete_do)
//
//        val bottomSheet: FrameLayout = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
//        val closeImg: ImageView = bottomSheetDialog.findViewById<ImageView>(R.id.dialog_close_do)
//        closeImg.setOnClickListener { bottomSheetDialog.dismiss() }
//
//        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        bottomSheetBehavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//        bottomSheetDialog.show()
//    }


    fun viewCloseBottomSheet() {
        // hideKeyboard();

        prod_bottom_layout!!.visibility = View.VISIBLE
        bill_disc_layl!!.visibility = View.GONE
        item_discount_ed!!.isEnabled = true
        exchange_inv!!.isEnabled = true
        bill_disc_percent_ed!!.setText("0.00")
        bill_disc_amt_ed!!.setText("0.00")

        setSummaryTotal()

        if(selectProductAdapter != null){
        selectProductAdapter!!.notifyDataSetChanged()}
        if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)

            productName_bottomEd!!.setText("")
            productName_bottomEd!!.requestFocus()
            openKeyborard(productName_bottomEd!!)
        } else {
            behavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }

        // Showing the Products Depending the Settings Values
        val settings = dbHelper!!.settings
        if (settings.size > 0) {
            for (model in settings) {
                if (model.settingName == "stock_view") {
                    if (model.settingValue == "1") {
                        stockProductView = "1"
                    } else if (model.settingValue == "2") {
                        stockProductView = "2"
                    } else {
                        stockProductView = "0"
                    }
                }
            }
        }
        //pdt todo
        // Filter the products list depending the Settings
        val filteredProducts = ArrayList<ProductsModel>()
        //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
        if (AppUtils.getProductsList() != null && AppUtils.getProductsList().size > 0) {
            for (product in AppUtils.getProductsList()) {
                //     Log.w("apiPdtDefa_uom", "" + product.defaultUom)
                if (stockProductView == "1") {
                    if (product.stockQty != null && product.stockQty != "null") {
                        if (product.stockQty.toDouble() > 0) {
                            Log.w("apiPdtUOm", "" + product.uomCode + product.uomText)
                            filteredProducts.add(product)
                        }
                    }
                } else if (stockProductView == "0") {
                    if (product.stockQty != null && product.stockQty != "null") {
                        if (product.stockQty.toDouble() < 0 || product.stockQty.toDouble() == 0.0) {
                            filteredProducts.add(product)
                        }
                    }
                } else {
                    filteredProducts.add(product)
                }
            }
        }
        if (filteredProducts.size > 0) {
            productLayout!!.visibility = View.VISIBLE
            emptyLayout!!.visibility = View.GONE
        } else {
            productLayout!!.visibility = View.GONE
            emptyLayout!!.visibility = View.VISIBLE
        }
        productListView!!.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        selectProductAdapter = SelectProductAdapter(this, filteredProducts) { model ->
            productsModel = model
            productId = productsModel!!.productCode
            Log.w("pdtsInv", "" + model.productName + "  .. " + model.barcode)
            Log.w("pdtsInvStoc", "" + model.stockQty)

            // setUomList(model.getProductUOMList());
            uomTextView!!.text = model.uomText
            if (isUomSetting) {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("CustomerCode", selectCustomerId)
                    jsonObject.put("ItemCode", model.productCode)
                    getUOM(jsonObject)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.w("Errord:", Objects.requireNonNull(e.message!!))
                }
            }

            // Need to implement the product price concept in SAP
            /*  try {
                        getProductPrice(productId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/if (model.minimumSellingPrice != null && !model.minimumSellingPrice.isEmpty()) {
            minimumSellingPriceText!!.text = model.minimumSellingPrice
        } else {
            minimumSellingPriceText!!.text = "0.00"
        }
            productName = productsModel!!.productName
            productAutoComplete!!.setText(model.productName + " - " + model.productCode)
            //  cartonPrice.setText(model.getUnitCost()+"");
            //  loosePrice.setText(model.getUnitCost());
            if(shortCodeStr.equals("FUXIN",true)) {
                if (isLastPrice.equals("True", true)) {
                    priceText!!.setText(model.lastPrice)
                } else {
                    priceText!!.setText(model.unitCost)
                }
            }
            else{
                if (model.lastPrice != null && !model.lastPrice.isEmpty() &&
                    model.lastPrice.toDouble() > 0.00
                ) {
                    priceText!!.setText(model.lastPrice)
                } else {
                    priceText!!.setText(model.unitCost)
                }
            }

            // uomText.setText(model.getUomCode());
            stockCount!!.setText(model.stockQty)
            pcsPerCarton!!.setText(model.pcsPerCarton)
            qtyValue!!.isEnabled = true
            qtyValue!!.requestFocus()
            //  behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            openKeyborard(qtyValue)

            // looseQtyValue.setEnabled(true);
            cartonPrice!!.isEnabled = true
            if(isFOCStr.equals("Yes",true)){
                if(model.isItemFOC.equals("Yes",true)) {
                    focEditText!!.isEnabled = true
                }else{
                    focEditText!!.isEnabled = false
                }
            }
            else{
                focEditText!!.isEnabled = false
            }
            exchangeEditext!!.isEnabled = true
            discountEditext!!.isEnabled = true
            returnEditext!!.isEnabled = true
            isItemFOCApi = model.isItemFOC

            stockLayout!!.visibility = View.VISIBLE
            if (model.stockQty != null && model.stockQty != "null") {
                pdtStockVal = model.stockQty
                if (model.stockQty.toDouble() == 0.0 || model.stockQty.toDouble() < 0) {
                    stockQtyValue!!.text = model.stockQty
                    stockQtyValue!!.setTextColor(Color.parseColor("#D24848"))
                } else if (model.stockQty.toDouble() > 0) {
                    stockQtyValue!!.setTextColor(Color.parseColor("#2ECC71"))
                    stockQtyValue!!.text = model.stockQty
                }
            }
            viewCloseBottomSheet()
        }
        productListView!!.adapter = selectProductAdapter
        totalProducts!!.text = filteredProducts.size.toString() + " Products"
        // get the Customer name from the local db
    }

    private fun setDeliveryAddrList(addrList: ArrayList<DeliveryAddressModel>) {
        val addressModel = DeliveryAddressModel()
//        addressModel.deliveryAddress = "Select Delivery Address"
//        addressModel.deliveryAddressCode = ""

//        Log.w("deladdrList:", addrList.toString());
//        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cust_spinner_item);

//        for (int i = 0; i < addrList.size(); i++) {
//            adapter.add(addrList.get(i).getDeliveryAddress());
//        }
        val deliveryAddressModels = ArrayList<DeliveryAddressModel>()
     //   deliveryAddressModels.add(0, addressModel)
        deliveryAddressModels.addAll(addrList)
        val adapter = ArrayAdapter(this, R.layout.cust_spinner_item, deliveryAddressModels)
        delivery_addrSpinner!!.adapter = adapter
        if (productsModel != null) {
            //setuom
            Log.w("cg_addrsiz1", addrList.size.toString() + "")
            for (i in addrList.indices) {
                Log.w("cg_addrsiz", addrList.size.toString() + "")
                delivery_addrSpinner!!.setSelection(i)

//                if (addrList.get(i).getDeliveryAddress().equals(productsModel.getDefaultUom())) {
//                    Log.d("cg_addr_", productsModel.getDefaultUom());
//                    delivery_addrSpinner.setSelection(i);
//                    break;
//                }
            }
        }
        delivery_addrSpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                if (isEditItem) {
                    var index = 0
                    for (model in deliveryAddressModels) {
                        if (model.deliveryAddress.equals(deliverAddrNameStr, ignoreCase = true)) {
                            delivery_addrSpinner!!.setSelection(index)
                            deliverAddrNameStr = model.deliveryAddress
                            //                            uomText.setText(model.getUomCode());
//                            priceText.setText(model.getPrice());
                            Log.w("selectAddrEd :", deliverAddrNameStr + "")
                            break
                        }
                        index++
                    }
                } else {
                    deliverAddrNameStr = delivery_addrSpinner!!.selectedItem.toString()
                    //                    priceText.setText(uomList.get(position).getPrice());

                 //   Log.w("deliAddr :", deliveryAddressModels[position].deliveryAddress)
                 //   Log.w("SelectAddr:", deliverAddrNameStr + "")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun defaultUOMset(uomList: ArrayList<UomModel>){
        if (activityFrom == "iv" || activityFrom == "ConvertInvoice" ||
            activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
            || activityFrom == "ReOrderInvoice"
        ) {
            uomValueVisible(settingUOMInvval!!)
            UOMdetail1(settingUOMInvval!!,uomList);
        }
        if (activityFrom == "so" || activityFrom == "SalesEdit" || activityFrom == "ReOrderSales" ) {
            uomValueVisible(settingUOMSOval!!)
            UOMdetail1(settingUOMSOval!!,uomList)
        }
        if (activityFrom == "do" || activityFrom == "doEdit" ) {
            uomValueVisible(settingUOM_DOval!!)
            UOMdetail1(settingUOM_DOval!!,uomList)
        }
    }

    private fun setUomList(uomList: ArrayList<UomModel>) {
        Log.w("UOMList:", uomList.toString())
        val adapter = ArrayAdapter(this, R.layout.cust_spinner_item, uomList)
        uomSpinner!!.adapter = adapter
        setUOMCode(uomList)

//        if (productsModel != null) {
//            //setuom
//            Log.d("cg_uomsiz1", uomList.size.toString() + "")
//                Log.w("cg_uomsiz", uomList.size.toString() + "")
//                if (activityFrom == "iv" || activityFrom == "ConvertInvoice" ||
//                    activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
//                    || activityFrom == "ReOrderInvoice"
//                ) {
//                    UOMdetail(settingUOMInvval!!,uomList)
//                }
//            if (activityFrom == "so" || activityFrom == "SalesEdit" || activityFrom == "ReOrderSales" ) {
//                UOMdetail(settingUOMSOval!!,uomList)
//            }
//
//            }

    }
    fun uomValueVisible(uomcode:String){
        ed_uomTxtl!!.setText(uomcode)
        uomChangel!!.visibility = View.VISIBLE
        ed_uomTxtl!!.visibility = View.VISIBLE
        uomSpinnerLayl!!.visibility = View.GONE
    }
//    fun uomValueGone(){
//        uomChangel!!.visibility = View.VISIBLE
//        ed_uomTxtl!!.visibility = View.GONE
//        uomSpinnerLayl!!.visibility = View.VISIBLE
//    }
    fun UOMdetail1(settingUOMval:String, uomList: ArrayList<UomModel>){
        for (i in uomList.indices) {
            if (settingUOMval!!.isNotEmpty()) {
                if (uomList[i].uomCode == settingUOMval) {
                    Log.w("cg_uoomcode_", settingUOMval)
//                    uomSpinner!!.setSelection(i)
                    uomText!!.setText(uomList[i].uomCode)
                    priceText!!.setText(uomList[i].price)
                        if (settingUOMval.equals("CTN", true)) {
                            var ctnStockVal = 0.00
                            var baseCtnQty = uomList[i].baseQty.toDouble()
                            var pdtStock = pdtStockVal!!.toDouble()

                            ctnStockVal = pdtStock / baseCtnQty
                            stockQtyValue!!.setText(twoDecimalPoint(ctnStockVal).toString())
                            Log.w(
                                "ctnStockkk",
                                "" + ctnStockVal + ".." + twoDecimalPoint(ctnStockVal).toString()
                            )
                            Log.w("ctnStock11", "" + baseCtnQty)
                        } else {
                            stockQtyValue!!.setText(pdtStockVal.toString())
                        }
                }
                uomText!!.setText(settingUOMval)
            }
        }
    }

//    fun UOMdetail(settingUOMval:String, uomList: ArrayList<UomModel>){
//        for (i in uomList.indices) {
//            if (settingUOMval!!.isEmpty()) {
//                if (uomList[i].uomCode == productsModel!!.defaultUom) {
//                    Log.w("cg_uoomcode_", productsModel!!.defaultUom)
//                    uomSpinner!!.setSelection(i)
//                    uomText!!.setText(uomList[i].uomCode)
//                    priceText!!.setText(uomList[i].price)
//                    if (stockQtyValue!!.text.toString().isNotEmpty()) {
//                        if (uomList[i].uomCode.equals("CTN", true)) {
//                            var ctnStockVal = 0.00
//                            var baseCtnQty = uomList[i].baseQty.toDouble()
//                            var pdtStock = pdtStockVal!!.toDouble()
//
//                            ctnStockVal = pdtStock / baseCtnQty
//                            stockQtyValue!!.setText(twoDecimalPoint(ctnStockVal).toString())
//                            Log.w(
//                                "ctnStockkk",
//                                "" + ctnStockVal + ".." + twoDecimalPoint(ctnStockVal).toString()
//                            )
//                            Log.w("ctnStock11", "" + baseCtnQty)
//                        } else {
//                            stockQtyValue!!.setText(pdtStockVal.toString())
//                        }
//                    }
//                }
//            } else {
//                if (uomList[i].uomCode == settingUOMval) {
//                    Log.w("cg_uoomcode_1",settingUOMval)
//
//                    uomSpinner!!.setSelection(i)
//                    uomText!!.setText(uomList[i].uomCode)
//                    priceText!!.setText(uomList[i].price)
//                    if (stockQtyValue!!.text.toString().isNotEmpty()) {
//                        if (uomList[i].uomCode.equals("CTN", true)) {
//                            var ctnStockVal = 0.00
//                            var baseCtnQty = uomList[i].baseQty.toDouble()
//                            var pdtStock = pdtStockVal!!.toDouble()
//
//                            ctnStockVal = pdtStock / baseCtnQty
//                            stockQtyValue!!.setText(twoDecimalPoint(ctnStockVal).toString())
//                            Log.w(
//                                "ctnStockkk11",
//                                "" + ctnStockVal + ".." + twoDecimalPoint(ctnStockVal).toString()
//                            )
//                            Log.w("ctnStock11aa", "" + baseCtnQty)
//                        } else {
//                            stockQtyValue!!.setText(pdtStockVal.toString())
//                        }
//                    }
//                    break
//                }
//            }
//        }
//
//    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_CODE) {
                barCodeLayl!!.visibility = View.VISIBLE
                val barcodeTxt = data!!.extras!!.getString("Contents")
                barcodeText!!.setText(barcodeTxt)
                Log.w("BarcodeTextInv:", barcodeTxt!!)
                val mp = MediaPlayer.create(this, R.raw.beep) // sound is inside res/raw/mysound
                mp.start()
                scannedBarcode = barcodeTxt
                searchAndSendActivity(barcodeTxt)
            } else if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    mPhotoFile = mCompressor!!.compressToFile(mPhotoFile)
                    imageString = ImageUtil.getBase64StringImage(mPhotoFile)
                    //  Log.w("GivenImage1:",imageString);
                    Utils.w("GivenImage1Inv", imageString)
                    showImage()
                } catch (e: IOException) {
                    e.printStackTrace()
                }


                /* Glide.with(MainActivity.this)
                        .load(mPhotoFile)
                        .apply(new RequestOptions().centerCrop()
                                .circleCrop()
                                .placeholder(R.drawable.profile_pic_place_holder))
                        .into(imageViewProfilePic);*/
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                val selectedImage = data!!.data
                try {
                    mPhotoFile =
                        mCompressor!!.compressToFile(File(getRealPathFromUri(selectedImage)))
                    selectImageInv!!.setText(selectedImage.toString())
                    imageString = ImageUtil.getBase64StringImage(mPhotoFile)
                    // Log.w("GivenImage2:",imageString);
                    Utils.w("GivenImage2Inv", imageString)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun searchAndSendActivity(barcode: String?) {
        try {
            val model = getProductData(barcode)
            if (model != null) {
                if (productSummaryList != null && productSummaryList.size > 0) {
                    if (!isAlreadyExist(barcode)) {
                        Toast.makeText(applicationContext, "Product Found...", Toast.LENGTH_SHORT)
                            .show()
                        Log.w("entypdt",""+model.barcode)
                        setProductDetails(model)
                        //  addItem(model);

                    }
                } else {
                    Toast.makeText(applicationContext, "Product Found...", Toast.LENGTH_SHORT)
                        .show()
                    Log.w("entypdt2",""+model.barcode)
                    //addItem(model);
                    setProductDetails(model)
                    // setProductDetails(model)
                }
            } else {
                //  showBarcodeAlert(barcode);
                Toast.makeText(applicationContext, "No Product found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
        }
    }

    private fun setProductDetails(model: ProductsModel) {
        productsModel = model
        productId = productsModel!!.productCode
        Log.w("pdtsInv1", "" + model.productName + "  .. " + model.barcode)
        // setUomList(model.getProductUOMList());
        uomTextView!!.text = model.uomText
        if (isUomSetting) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("CustomerCode", selectCustomerId)
                jsonObject.put("ItemCode", model.productCode)
                getUOM(jsonObject)
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.w("Errord:", Objects.requireNonNull(e.message!!))
            }
        }

        // Need to implement the product price concept in SAP
        /*  try {
                    getProductPrice(productId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
        if (model.minimumSellingPrice != null && !model.minimumSellingPrice.isEmpty()) {
            minimumSellingPriceText!!.text = model.minimumSellingPrice
        } else {
            minimumSellingPriceText!!.text = "0.00"
        }
        productName = productsModel!!.productName
        productAutoComplete!!.setText(model.productName + " - " + model.productCode)
        //  cartonPrice.setText(model.getUnitCost()+"");
        //  loosePrice.setText(model.getUnitCost());
        if(shortCodeStr.equals("FUXIN",true)) {
            if (isLastPrice.equals("True", true)) {
                priceText!!.setText(model.lastPrice)
            } else {
                priceText!!.setText(model.unitCost)
            }
        }
        else{
            if (model.lastPrice != null && !model.lastPrice.isEmpty() &&
                model.lastPrice.toDouble() > 0.00
            ) {
                priceText!!.setText(model.lastPrice)
            } else {
                priceText!!.setText(model.unitCost)
            }
        }
        // uomText.setText(model.getUomCode());
        stockCount!!.setText(model.stockQty)
        pcsPerCarton!!.setText(model.pcsPerCarton)
        qtyValue!!.isEnabled = true
        qtyValue!!.requestFocus()
        //  behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        openKeyborard(qtyValue)

        // looseQtyValue.setEnabled(true);
        cartonPrice!!.isEnabled = true
        if(isFOCStr.equals("Yes")){
            if(model.isItemFOC.equals("Yes",true)) {
                focEditText!!.isEnabled = true
            }else{
                focEditText!!.isEnabled = false
            }
        }
        else{
            focEditText!!.isEnabled = false
        }

        isItemFOCApi = model.isItemFOC

        exchangeEditext!!.isEnabled = true
        discountEditext!!.isEnabled = true
        returnEditext!!.isEnabled = true
        stockLayout!!.visibility = View.VISIBLE
        if (model.stockQty != null && model.stockQty != "null") {
            pdtStockVal = model.stockQty
            if (model.stockQty.toDouble() == 0.0 || model.stockQty.toDouble() < 0) {
                stockQtyValue!!.text = model.stockQty
                stockQtyValue!!.setTextColor(Color.parseColor("#D24848"))
            } else if (model.stockQty.toDouble() > 0) {
                stockQtyValue!!.setTextColor(Color.parseColor("#2ECC71"))
                stockQtyValue!!.text = model.stockQty
            }
        }
    }

    fun getProductData(keyId: String?): ProductsModel? {
        var index = 0
        if (AppUtils.getProductsList() != null && AppUtils.getProductsList().size > 0) {
            for (model in AppUtils.getProductsList()) {
             //   Log.w("pdtsbarcodd", "" + keyId + "  .. "+AppUtils.getProductsList().get(1).barcode)

                if (keyId == model.barcode || keyId == model.productCode) {
                    Log.w("pdtsbarcodd1", "" + keyId + "  .. "+model.barcode)

                    return model
                }
                index++
            }
        }
        return null
    }
    private fun isAlreadyExist(scannedBarcode: String?): Boolean {
        var check = false
        try {
            for (pdt in productSummaryAdapter!!.getList()) {
                if (pdt.productBarCode.trim { it <= ' ' } == scannedBarcode!!.trim { it <= ' ' }) {
                    Toast.makeText(
                        applicationContext,
                        "This Products Already Added..",
                        Toast.LENGTH_SHORT
                    ).show()
                    check = true
                    break
                }
            }
        } catch (exception: Exception) {
        }
        return check
    }

    private fun setUOMCode(uomList: ArrayList<UomModel>) {
        uomSpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
//                if (isEditItem) {
//                    selectValue(uomList, uomName, uomPriceEd!!)
//                    Log.w("uomentyy:", uomName + "kk"+uomPriceEd)
//
//                } else {
                    uomName = uomSpinner!!.selectedItem.toString()
                uomText!!.setText(uomName)
                Log.w("uomentyy:", uomName+pdtStockVal)
                if(uomName.equals("CTN",true)) {
                    var ctnStockVal = 0.00
                    var baseCtnQty = uomList[position].baseQty.toDouble()
                    var pdtStock = pdtStockVal!!.toDouble()

                    ctnStockVal = pdtStock / baseCtnQty
                    stockQtyValue!!.setText(twoDecimalPoint(ctnStockVal).toString())
                    Log.w("ctnStockaa",""+ctnStockVal+".."+twoDecimalPoint(ctnStockVal).toString())
                    Log.w("ctnStock22",""+baseCtnQty)
                }
                else{
                    stockQtyValue!!.setText(pdtStockVal.toString())
                }
                    uomText!!.setText(uomList[position].uomCode)
                    priceText!!.setText(uomList[position].price)
                    Log.w("uomentyy1:", uomName)

                    Log.w("UOMQtyValue:", uomList[position].uomEntry)
                    Log.w("SelectedUOM:", uomName + "")
               // }
                qtyValue!!.setText("")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun selectValue(uomList: ArrayList<UomModel>, value: String ,price: String) {
        try {
            var index = 0
            for (model in uomList) {
                if (model.uomCode.equals(value, ignoreCase = true)) {
                    uomSpinner!!.setSelection(index)
                    uomName = model.uomCode
                    uomText!!.setText(model.uomCode)
                    priceText!!.setText(price)
                    Log.w("SelectedUOMEd:", uomName + ""+price)
                    break
                }
                index++
            }
             //isEditItem = false

        } catch (exception: Exception) {
        }
    }

    private fun selectValueEdit(uomList: ArrayList<UomModel>, value: String) {
        try {
            var index = 0
            for (model in uomList) {
                if (model.uomCode.equals(value, ignoreCase = true)) {
                    uomSpinner!!.setSelection(index)
                    uomName = model.uomCode
                    uomText!!.setText(model.uomCode)
//                    priceText!!.setText(model.price)

                    Log.w("SelectedUOMEdit:", uomName + "")
                    break
                }
                index++
            }
        } catch (exception: Exception) {
        }
    }

    fun openKeyborard(editText: EditText?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun filter(text: String) {
        try {
            settings = dbHelper!!.settings
            if (settings!!.size > 0) {
                for (model in settings!!) {
                    if (model.settingName == "stock_view") {
                        if (model.settingValue == "1") {
                            stockProductView = "1"
                        } else if (model.settingValue == "2") {
                            stockProductView = "2"
                        } else {
                            stockProductView = "0"
                        }
                    }
                }
            }
            //new array list that will hold the filtered data
            val filterProducts = ArrayList<ProductsModel>()
            //looping through existing elements
            //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
            for (s in AppUtils.getProductsList()) {
                //if the existing elements contains the search input
                if (s.productName.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault())) || s.productCode.lowercase(
                        Locale.getDefault()
                    ).contains(text.lowercase(Locale.getDefault()))
                ) {
                    //adding the element to filtered list
                    if (stockProductView == "1") {
                        if (s.stockQty != null && s.stockQty != "null") {
                            if (s.stockQty.toDouble() > 0) {
                                filterProducts.add(s)
                            }
                        }
                    } else if (stockProductView == "0") {
                        if (s.stockQty != null && s.stockQty != "null") {
                            if (s.stockQty.toDouble() < 0 || s.stockQty.toDouble() == 0.0) {
                                filterProducts.add(s)
                            }
                        }
                    } else {
                        filterProducts.add(s)
                    }
                }
            }
            //calling a method of the adapter class and passing the filtered list
            selectProductAdapter!!.filterList(filterProducts)

            //setAdapter(filterProducts);
            totalProducts!!.text = filterProducts.size.toString() + " Products"
        } catch (ex: Exception) {
            Log.e("Error_in_filter", Objects.requireNonNull(ex.message!!))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                /*   Intent intent = new Intent(CreateNewInvoiceActivity.this, CustomerListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
                val count = dbHelper!!.numberOfRowsInInvoice()
                if (count > 0) {
                    showDeleteAlert()
                } else {
                    finish()
                }
                true
            }

            R.id.action_down -> {
                bill_disc_savel!!.visibility = View.GONE

                val products = dbHelper!!.allInvoiceProducts
                val localCart = dbHelper!!.allInvoiceProducts

                if (localCart.size > 0) {
                if (netTotalValue!!.text.toString().toDouble() > 0.00 && products!!.size>0) {
                   // bill_disc_percent_ed!!.setText(billDiscApiStr)
                    bill_disc_amt_ed!!.setText(pref_bill_disc_amt)
                    bill_disc_percent_ed!!.setText(pref_bill_disc_percent)
                    billdisc_displayl!!.setText(pref_bill_disc_percent)

                    isBillPercentageTouch = true
                    billDischide()

                    val subtotal: Double = getSubtotal()
                    if (isBillPercentageTouch) {
                        Log.w("PercentageAff:", "")
                        if (!billDiscApiStr.isEmpty()) {
                            if (billDiscApiStr == ".") {
                                bill_disc_percent_ed!!.setText("0.")
                                bill_disc_percent_ed!!.setSelection(bill_disc_percent_ed!!.getText().length)
                                val amount: Double = percentageToAmount("0".toDouble())
                                bill_disc_amt_ed!!.removeTextChangedListener(
                                    billDiscAmountTextWatcher
                                )
                                bill_disc_amt_ed!!.setText(twoDecimalPoint(amount) + "")
                                bill_disc_amt_ed!!.addTextChangedListener(billDiscAmountTextWatcher)
                                val net_subtotal: Double =
                                    subtotal - bill_disc_amt_ed!!.getText().toString().toDouble()
                                setCalculationSummaryView(net_subtotal)
                            } else {
                                if(billDiscApiStr != null || !billDiscApiStr.equals("null") ) {
                                    val amount: Double = percentageToAmount(billDiscApiStr.toDouble())
                                    bill_disc_percent_ed!!.setText(billDiscApiStr)
                                    Log.w("PercentageAmtdishc:", amount.toString() + "")
                                    bill_disc_amt_ed!!.removeTextChangedListener(
                                        billDiscAmountTextWatcher
                                    )
                                    bill_disc_amt_ed!!.setText(twoDecimalPoint(amount) + "")
                                    bill_disc_amt_ed!!.addTextChangedListener(
                                        billDiscAmountTextWatcher
                                    )
                                    var bill_discc = "0.00"
                                    if(bill_disc_amt_ed!!.getText().toString().isNotEmpty()){
                                       bill_discc =  bill_disc_amt_ed!!.getText().toString()
                                    }
                                    val net_subtotal: Double =
                                        subtotal - bill_discc.toDouble()
                                    setCalculationSummaryView(net_subtotal)
                                }
                            }
                        } else {
                            bill_disc_amt_ed!!.removeTextChangedListener(billDiscAmountTextWatcher)
                            bill_disc_amt_ed!!.setText("0.0")
                            bill_disc_amt_ed!!.addTextChangedListener(billDiscAmountTextWatcher)
                            setCalculationSummaryView(subtotal)
                        }
                    }


                    bill_disc_percent_ed!!.requestFocus()
                    Log.w("billdiscpi_aa::", billDiscApiStr!!)
                }

                }
                else {
                    Toast.makeText(
                        applicationContext,
                        "Add the Product First...!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                // billDiscountBottomSheet()
                true
            }
            R.id.action_scan_menu -> {
                scannedBarcode = ""
                val intent = Intent(this@CreateNewInvoiceActivity, BarCodeScanner::class.java)
                startActivityForResult(intent, RESULT_CODE)
                true
            }
            R.id.action_save -> {
                save_btn!!.setEnabled(b)
                val localCart = dbHelper!!.allInvoiceProducts
                if (localCart.size > 0) {

                    save_btn!!.setEnabled(true)
                    isBillPercentageTouch = true

                    if (netTotalValue!!.text.toString().toDouble() > 0) {
                        save_btn!!.setEnabled(true)
                        if (activityFrom == "iv" || activityFrom == "ConvertInvoice" ||
                            activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
                            || activityFrom == "ReOrderInvoice"
                        ) {
//                        showSaveAlert()
                            if (billDiscApiStr.isNotEmpty() && billDiscApiStr.toDouble() > 0.0) {
                                Log.w("enrty1www", "")
                                bill_disc_savel!!.visibility = View.VISIBLE
                                billDischide()
                            }else{
                                Log.w("enrty1qqq", "")
                                bill_disc_savel!!.visibility = View.GONE

                            if (isCheckedCreditLimit) {
                                if (createInvoiceValidation()) {
                                    Log.w("enrty1a", "")
                                    showSaveAlert();
                                } else {
                                    Log.w("enrty1b", "")
                                    showAlertForCreditLimit();
                                }
                            } else {
                                Log.w("enrty1c", "")
                                showSaveAlert();
                            }
                        }
                        } else {
                            Log.w("enrty1v", "")
                            showSaveAlert()
                        }
                    }
                    else {
                        Toast.makeText(
                            applicationContext,
                            " Nettotal should be great than zero",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                else {
                    Toast.makeText(
                        applicationContext,
                        "Add the Product First...!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
        //  return super.onOptionsItemSelected(item);
    }

    fun billDischide(){
        if (bill_disc_layl!!.getVisibility() == View.VISIBLE) {
            bill_disc_layl!!.setVisibility(View.GONE)
            bill_disc_amt_ed!!.setText(pref_bill_disc_amt)
            bill_disc_percent_ed!!.setText(pref_bill_disc_percent)

            if (behavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            //slideUp(searchFilterView);
        } else {
            bill_disc_layl!!.setVisibility(View.VISIBLE)
            bill_disc_amt_ed!!.setText(pref_bill_disc_amt)
            bill_disc_percent_ed!!.setText(pref_bill_disc_percent)

            if (behavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    fun createInvoiceValidation(): Boolean {
        var canCreateInvoice = false
        if (creditLimitAmount.toDouble() == 0.00) {
            canCreateInvoice = true
        } else if (creditLimitAmount.toDouble() > 0.00) {
            canCreateInvoice = if (outstandingAmount.toDouble() < creditLimitAmount.toDouble()) {
                val remainingAmount = creditLimitAmount.toDouble() - outstandingAmount.toDouble()
                val net_total = netTotalValue!!.text.toString().toDouble()
                if (remainingAmount >= net_total) {
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
        return canCreateInvoice
    }

    fun showAlertForCreditLimit() {
        val builder1 = AlertDialog.Builder(this@CreateNewInvoiceActivity)
        builder1.setTitle("Information")
        builder1.setMessage(
            customerNameText!!.getText()
                .toString() + "- Your Credit Limit exceed Can't create Invoice"
        )
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "Ok"
        ) { dialog, id ->
            dialog.cancel()
        }
        val alert11 = builder1.create()
        alert11.show()
    }

    fun showSaveAlert() {
       // try {
            // create an alert builder
            val builder = AlertDialog.Builder(this@CreateNewInvoiceActivity)
            // set the custom layout
            builder.setCancelable(false)
            val customLayout = layoutInflater.inflate(R.layout.invoice_save_option, null)
            builder.setView(customLayout)
            // add a button
            okButton = customLayout.findViewById(R.id.btn_ok)
            cancelButton = customLayout.findViewById(R.id.btn_cancel)
            invoicePrintCheck = customLayout.findViewById(R.id.invoice_print_check)
            doInvprintcheck =  customLayout.findViewById(R.id.invoice_print_DO_check)
            attachmentLayInv = customLayout.findViewById(R.id.attachement_layoutInv)
        receipt_printCheck = customLayout.findViewById(R.id.receipt_print_inv)
        cash_collectedCheck = customLayout.findViewById(R.id.cash_collected_inv)
        emailCheck = customLayout.findViewById(R.id.email_inv_check)
            selectImageInv = customLayout.findViewById(R.id.select_imageInv)
        inv_cash_CollectLayl = customLayout.findViewById(R.id.inv_cash_CollectLay)
            saveMessage = customLayout.findViewById(R.id.save_message)
            saveTitle = customLayout.findViewById(R.id.save_title)
            signatureCapture = customLayout.findViewById(R.id.signature_capture)
            val noOfCopy = customLayout.findViewById<TextView>(R.id.no_of_copy)
            val copyPlus = customLayout.findViewById<Button>(R.id.increase)
            val copyMinus = customLayout.findViewById<Button>(R.id.decrease)
            val signatureButton = customLayout.findViewById<Button>(R.id.btn_signature)
            val copyLayout = customLayout.findViewById<LinearLayout>(R.id.print_layout)
        receipt_printCheck!!.isChecked = false
        cash_collectedCheck!!.isChecked = false
        doInvprintcheck!!.isChecked = false


        doInvprintcheck!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (b) {
              isPrintEnable = true
                isDOPrint = true
            } else {
                isPrintEnable = false
                isDOPrint = false
            }
        })
        cash_collectedCheck!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (b) {
                isCashCollectCheck = true
                //showSaveAlertForReceiptPrint();
                receipt_printCheck!!.setEnabled(true)
                // Toast.makeText(getActivity(),"Cash Collected Activated...",Toast.LENGTH_SHORT).show();
            } else {
                isCashCollectCheck = false
                isReceiptPrint = false
                receipt_printCheck!!.setChecked(false)
                receipt_printCheck!!.setEnabled(false)
                // Toast.makeText(getActivity(),"Cash Collected DeActivated...",Toast.LENGTH_SHORT).show();
            }
        })
        receipt_printCheck!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (validatePrinterConfiguration()) {
                if (b) {
                    isPrintEnable = true
                    isReceiptPrint = true
                } else {
                    isReceiptPrint = false
                }
            } else {
                receipt_printCheck!!.setChecked(false)
            }
        })
        emailCheck!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    isEmailEnable = true
                } else {
                    isEmailEnable = false
                }
        })
        selectImageInv!!.setOnClickListener {
                if (selectImageInv!!.getTag() == "view_image") {
                    showImage()
                } else {
                    selectImage()
                }
            }

            if (mPhotoFile != null && mPhotoFile!!.length() > 0) {
                selectImageInv!!.setText("View Image")
                selectImageInv!!.setTag("view_image")
            } else {
                selectImageInv!!.setText("Select Image")
                selectImageInv!!.setTag("select_image")
            }
            //invoicePrintCheck.setVisibility(View.GONE);
            if (activityFrom == "iv" || activityFrom == "ConvertInvoice" ||
                activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO" || activityFrom == "ReOrderInvoice"
            ) {
//                        showSaveAlert()
                invoicePrintCheck!!.visibility = View.VISIBLE
                inv_cash_CollectLayl!!.visibility = View.VISIBLE
                if(!isMailId.equals("")) {
                    if (isEmailSetting) {
                        emailCheck!!.isChecked = true
                        isEmailEnable = true
                    }
                    emailCheck!!.visibility = View.VISIBLE
                }
                else{
                    isEmailEnable = false
                    emailCheck!!.visibility = View.GONE
                }
            }
                if (activityFrom == "so" || activityFrom == "SalesEdit" || activityFrom == "ReOrderSales" ) {
                saveTitle!!.setText("Save SalesOrder")
                saveMessage!!.setText("Are you sure want to save SalesOrder?")
                invoicePrintCheck!!.setText("SalesOrder Print")
                if (isCheckedSalesPrint) {
                    invoicePrintCheck!!.setChecked(true)
                    isPrintEnable = true
                    copyLayout.visibility = View.VISIBLE
                } else {
                    invoicePrintCheck!!.setChecked(false)
                    isPrintEnable = false
                    copyLayout.visibility = View.GONE
                }
            } else if (activityFrom == "do" || activityFrom == "doEdit") {
                saveTitle!!.setText("Save Delivery Order")
                saveMessage!!.setText("Are you sure want to save Delivery Order?")
                invoicePrintCheck!!.setText("Delivery Order Print")
                invoicePrintCheck!!.setVisibility(View.GONE)
                if (isCheckedSalesPrint) {
                    invoicePrintCheck!!.setChecked(true)
                    isPrintEnable = true
                    copyLayout.visibility = View.VISIBLE
                } else {
                    invoicePrintCheck!!.setChecked(false)
                    isPrintEnable = false
                    copyLayout.visibility = View.GONE
                }
            } else {
                if (isCheckedInvoicePrint) {
                    invoicePrintCheck!!.setChecked(true)
                    isPrintEnable = true
                    isInvoicePrint = true
                    copyLayout.visibility = View.VISIBLE
                } else {
                    invoicePrintCheck!!.setChecked(false)
                    isPrintEnable = false
                    isInvoicePrint = false
                    copyLayout.visibility = View.GONE
                }
            }
            signatureButton.setOnClickListener { showSignatureAlert()
            }
            invoicePrintCheck!!.setOnClickListener(View.OnClickListener {
                if (invoicePrintCheck!!.isChecked()) {
                    isInvoicePrint = true
                    isPrintEnable = true
                    copyLayout.visibility = View.VISIBLE
                } else {
                    isPrintEnable = false
                    isInvoicePrint = false
                    copyLayout.visibility = View.GONE
                }
            })
            copyPlus.setOnClickListener {
                val copyvalue = noOfCopy.text.toString()
                var copy = copyvalue.toInt()
                copy++
                noOfCopy.text = copy.toString() + ""
            }
            copyMinus.setOnClickListener {
                if (noOfCopy.text.toString() != "1") {
                    val copyvalue = noOfCopy.text.toString()
                    var copy = copyvalue.toInt()
                    copy--
                    noOfCopy.text = copy.toString() + ""
                }
            }

            // create and show the alert dialog
            alert = builder.create()
            alert!!.show()
            okButton!!.setOnClickListener(View.OnClickListener { view1: View? ->
               try {
                    Log.w("entyyinv",""+activityFrom)
//                    if (companyCode == "AADHI INTERNATIONAL PTE LTD") {
//                        if (activityFrom == "iv" || activityFrom == "ConvertInvoice"
//                            || activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
//                            || activityFrom == "ReOrderInvoice"
//                        ) {
//                            if (signatureString != null && !signatureString!!.isEmpty()) {
//                                alert!!.dismiss()
//                                if (activityFrom == "iv" || activityFrom == "ConvertInvoice"
//                                    || activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
//                                    || activityFrom == "ReOrderInvoice"
//                                ) {
//                                    createInvoiceJson(noOfCopy.text.toString().toInt())
//                                } else if (activityFrom == "so" || activityFrom == "SalesEdit" || activityFrom == "ReOrderSales" ) {
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
//                            alert!!.dismiss()
//                            if (activityFrom == "iv" || activityFrom == "ConvertInvoice"
//                                || activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
//                                || activityFrom == "ReOrderInvoice"
//                            ) {
//                                Log.w("convet","")
//                                createInvoiceJson(noOfCopy.text.toString().toInt())
//                            } else if (activityFrom == "so" || activityFrom == "SalesEdit" || activityFrom == "ReOrderSales" ) {
//                                createSalesOrderJson(noOfCopy.text.toString().toInt())
//                            } else if (activityFrom == "do" || activityFrom == "doEdit") {
//                                createDOJson(noOfCopy.text.toString().toInt())
//                            }
//                        }
//                    } else {
                        alert!!.dismiss()
                        if (activityFrom == "iv" || activityFrom == "ConvertInvoice"
                            || activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO"
                            || activityFrom == "ReOrderInvoice"
                        ) {
                            Log.w("signavail", "" + isSignatureSetting + ".." + signatureString)

                            if (isSignatureSetting) {
                                if (signatureString != null && !signatureString!!.isEmpty()) {
                                    createInvoiceJson(noOfCopy.text.toString().toInt())
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Add Signature to Save Invoice",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                createInvoiceJson(noOfCopy.text.toString().toInt())
                            }
                        } else if (activityFrom == "so" || activityFrom == "SalesEdit" || activityFrom == "ReOrderSales" ) {
                            createSalesOrderJson(noOfCopy.text.toString().toInt())
                        } else if (activityFrom == "do" || activityFrom == "doEdit") {
                            createDOJson(noOfCopy.text.toString().toInt())
                        }
                    //}
                } catch (exception: Exception) {
                }
            })
            cancelButton!!.setOnClickListener(View.OnClickListener {
                save_btn!!.setEnabled(true)
                alert!!.cancel()
            })
//        } catch (exception: Exception) {
//        }
    }

    fun saveSalesOrder(jsonBody: JSONObject, action: String, copy: Int) {
        try {
            pDialog =
                SweetAlertDialog(this@CreateNewInvoiceActivity, SweetAlertDialog.PROGRESS_TYPE)
            pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
            if (action == "SalesOrder") {
                pDialog!!.setTitleText("Saving Sales Order...")
            } else if (action == "DeliveryOrder") {
                pDialog!!.setTitleText("Saving Delivery Order...")
            } else {
                pDialog!!.setTitleText("Saving Invoice...")
            }
            pDialog!!.setCancelable(false)
            pDialog!!.show()
            val requestQueue = Volley.newRequestQueue(this)
            Log.w("GivenSalesOrdReq :", jsonBody.toString())
            var URL = ""
            URL = if (action == "SalesOrder") {
                Utils.getBaseUrl(this) + "PostingSalesOrder"
            } else if (action == "DeliveryOrder") {
                Utils.getBaseUrl(this) + "PostingDeliveryOrder"
            } else {
                Utils.getBaseUrl(this) + "PostingInvoice"
            }
            Log.w("Given_InvoiceApi:", URL)
            //    {"statusCode":2,"statusMessage":"Failed","responseData":{"docNum":null,"error":"Invoice :One of the base documents has already been closed  [INV1.BaseEntry][line: 1]"}}
            val salesOrderRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                URL,
                jsonBody,
                Response.Listener { response: JSONObject ->
                    Log.w("Invoice_ResponseSap:", response.toString())
                    Utils.clearCustomerSession(this)
                    AppUtils.setProductsList(null)
                    dbHelper!!.removeAllReturn()
                    // dbHelper.removeCustomer();
                    // {"statusCode":1,"statusMessage":"Invoice Created Successfully","responseData":{"docNum":"35","error":null}}
                    pDialog!!.dismiss()
                    save_btn!!.setEnabled(true)
                    val statusCode = response.optString("statusCode")
                    val message = response.optString("statusMessage")
                    var responseData: JSONObject? = null
                    try {
                        responseData = response.getJSONObject("responseData")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.w("Errorc:", Objects.requireNonNull(e.message!!))                    }
                    if (statusCode == "1") {
                        signatureString = ""
                        if (action == "SalesOrder" || action == "SalesEdit") {
                            if (isPrintEnable) {
                                try {
                                    dbHelper!!.removeAllInvoiceItems()
                                    val `object` = response.optJSONObject("responseData")
                                    val doucmentNo = `object`.optString("docNum")
                                    //   String result=object.optString("Result");
                                    if (!doucmentNo.isEmpty()) {
                                       //  getSalesOrderDetails(doucmentNo, copy);
                                        val intent =
                                            Intent(this, SalesOrderListActivity::class.java)
                                        intent.putExtra("printSoNumber", doucmentNo)
                                        intent.putExtra("noOfCopy", copy.toString())
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error in getting printing data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        redirectActivity()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.w("Error1k:", Objects.requireNonNull(e.message!!))
                                }
                            } else {
                                dbHelper!!.removeAllInvoiceItems()
                                redirectActivity()
                            }
                            isPrintEnable = false
                        } else if (action == "DeliveryOrder" || action == "DeliveryOrderEdit") {
                            val intent =
                                Intent(applicationContext, DeliveryOrderListActivity::class.java)
                            startActivity(intent)
                            finish()
                            /*  if (isPrintEnable){
                            // {"statusCode":1,"statusMessage":"Delivery Order Created Successfully","responseData":{"docNum":"11","error":null}}
                            try {
                                dbHelper.removeAllItems();
                                JSONObject object=response.optJSONObject("responseData");
                                String doucmentNo =object.optString("docNum");
                                //   String result=object.optString("Result");
                                if (!doucmentNo.isEmpty()) {
                                    getDoDetails(doucmentNo, copy);
                                }else {
                                    Toast.makeText(getApplicationContext(),"Error in getting printing data",Toast.LENGTH_SHORT).show();
                                    redirectActivity();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else {
                            dbHelper.removeAllItems();
                            redirectActivity();
                        }*/isPrintEnable = false
                        }else {
                            //  if (isPrintEnable){
                                try {
                                    //updateStockQty();
                                    dbHelper!!.removeAllInvoiceItems()
                                    val `object` = response.optJSONObject("responseData")
                                    val doucmentNo = `object`.optString("docNum")
                                    val receiptNo = `object`.optString("receiptNo")
                                    receiptNoApi = receiptNo

                                    //   String result=object.optString("Result");
                                    if ( isReceiptPrint && isCashCollectCheck) {
                                        if (receiptNoApi!!.isNotEmpty() && receiptNoApi != null
                                            && !receiptNoApi.equals("null")
                                        ) {

                                            Log.w("entryPrint1", "");
                                            getReceiptsDetails(receiptNoApi!!)
                                        }
                                    }
                                        if (!doucmentNo.isEmpty()) {
                                            if (isPrintEnable) {
                                                if (isInvoicePrint && isDOPrint) {
                                                    getInvoicePrintDetails(doucmentNo, copy, true)
                                                    val intent = Intent(
                                                        applicationContext,
                                                        NewInvoiceListActivity::class.java
                                                    )
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    if (!doucmentNo.isEmpty()) {
                                                        if (isInvoicePrint) {
                                                            getInvoicePrintDetails(
                                                                doucmentNo,
                                                                copy,
                                                                false
                                                            )
                                                            val intent = Intent(
                                                                applicationContext,
                                                                NewInvoiceListActivity::class.java
                                                            )
                                                            startActivity(intent)
                                                            finish()

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Error in getting printing data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        redirectActivity()
                                    }
                                    if (!isReceiptPrint || !isPrintEnable) {
                                        dbHelper!!.removeAllInvoiceItems()
                                        redirectActivity()
                                    }

                                    } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.w("Errora:", Objects.requireNonNull(e.message!!))
                                }
//                            } else {
//                                dbHelper!!.removeAllInvoiceItems()
//                                redirectActivity()
//                            }
                            isPrintEnable = false

                            myEdit!!.putString("billDisc_amt","0.0")
                            myEdit!!.putString("billDisc_percent","0.0")
                            myEdit!!.apply()
                        }
                    } else {
//                    Log.w("ErrorValues:",responseData.optString("error"));
                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("User", username)
                            jsonObject.put("CardCode", customerCode)
                            jsonObject.put("ItemGroupCode", "All")
                            getAllProducts(jsonObject)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.w("Error9:", Objects.requireNonNull(e.message!!))
                        }
                        if (responseData != null) {
                            Toast.makeText(
                                applicationContext,
                                responseData.optString("error"),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Error in Saving Data...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    Log.w("SalesOrder_Response:", error.toString())
                    save_btn!!.setEnabled(true)
                    Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    pDialog!!.dismiss()
                }) {
                /* @Override
                 public byte[] getBody() {
                     return jsonBody.toString().getBytes();
                 }*/
                override fun getBodyContentType(): String {
                    return "application/json"
                }

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
            salesOrderRequest.setRetryPolicy(object : RetryPolicy {
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
            requestQueue.add(salesOrderRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("Error8:", Objects.requireNonNull(e.message!!))        }
    }

    @Throws(JSONException::class)
    private fun getReceiptsDetails(receiptNumber: String) {
        // Initialize a new RequestQueue instance
        val jsonObject = JSONObject()
        //   jsonObject.put("CompanyCode", companyId);
        jsonObject.put("ReceiptNo", receiptNumber)
        jsonObject.put("LocationCode", locationCode)
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "ReceiptDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_recei:", url)
        Log.w("JsonPrint_recei:", jsonObject.toString())
        // pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        //  pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        //  pDialog.setTitleText("Getting Printing Data..");
        // pDialog.setCancelable(false);
        // pDialog.show();
        receiptsHeaderDetails = ArrayList()
        receiptsList = ArrayList()
      //  receiptsPrintList = ArrayList()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
            url,
            jsonObject,
            Response.Listener<JSONObject> { response: JSONObject ->
                try {
                    Log.w("receipt_Details:", response.toString())

                    //  {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"C1001","customerName":"BRINDAS PTE LTD",
                    //  "receiptNumber":"9","receiptStatus":"O","receiptDate":"18\/8\/2021 12:00:00 am","netTotal":"5.350000",
                    //  "balanceAmount":5.35,"totalDiscount":0,"paidAmount":0,"contactPersonCode":"","createDate":"18\/8\/2021 12:00:00 am",
                    //  "updateDate":"18\/8\/2021 12:00:00 am","remark":"","fDocTotal":0,"fTaxAmount":0,"receivedAmount":0,"total":5.35,
                    //  "fTotal":0,"iTotalDiscount":0,"taxTotal":0.35,"iPaidAmount":0,"currencyCode":"SGD","currencyName":"Singapore Dollar",
                    //  "companyCode":"AATHI_LIVE_DB","docEntry":"9","address1":null,"taxPercentage":null,"discountPercentage":null,
                    //  "subTotal":5,"taxType":"I","taxCode":"IN","taxPerc":"7.000000","billDiscount":0,"signFlag":null,"signature":null,
                    //
                    //  "receiptDetails":[{"paymentNo":"9","paymentDate":"29\/8\/2021 12:00:00 am","cardCode":"C1001",
                    //  "cardName":"BRINDAS PTE LTD","paymentTot":225.02,"invoiceNo":"25","invoiceDate":"24\/8\/2021 12:00:00 am",
                    //  "slpName":"-No Sales Employee-","invoiceTot":225.02,"totDiscount":0,"invoiceAcct":null}]}]}
                    val statusCode = response.optString("statusCode")
                    if (statusCode == "1") {
                        val responseArray = response.optJSONArray("responseData")
                        val responseObject = responseArray.optJSONObject(0)
                        val model = ReceiptPrintPreviewModel()
                        model.receiptNumber = responseObject.optString("receiptNumber")
                        model.receiptDate = responseObject.optString("receiptDate")
                        model.payMode = responseObject.optString("payMode")
                        model.address =
                            responseObject.optString("address1") + responseObject.optString(
                                "address2"
                            ) + responseObject.optString("address3")
                        model.address1 = responseObject.optString("address1")
                        model.address2 = responseObject.optString("address2")
                        model.address3 = responseObject.optString("address3")
                        model.addressstate =
                            (responseObject.optString("block") + " " + responseObject.optString("street") + " "
                                    + responseObject.optString("city"))
                        model.addresssZipcode =
                            (responseObject.optString("countryName") + " " + responseObject.optString(
                                "state"
                            ) + " "
                                    + responseObject.optString("zipcode"))
                        model.customerCode = responseObject.optString("customerCode")
                        model.customerName = responseObject.optString("customerName")
                        model.totalAmount = responseObject.optString("netTotal")
                        model.paymentType = responseObject.optString("paymentType")
                        model.bankCode = responseObject.optString("bankCode")
                        model.bankName = responseObject.optString("bankName")
                        model.chequeDate = responseObject.optString("checkDueDate")
                        model.chequeNo = responseObject.optString("checkNumber")
                        model.bankTransferDate = responseObject.optString("bankTransferDate")
                        model.balanceAmount = responseObject.optString("balanceAmount")
                        model.creditAmount = responseObject.optString("totalDiscount")
                        // model.setSignFlag(responseObject.optString("signFlag"));
                        // String signFlag = responseObject.optString("signFlag");
                        if (responseObject.optString("signature") != null && responseObject.optString(
                                "signature"
                            ) != "null" && !responseObject.optString("signature").isEmpty()
                        ) {
                            val signature = responseObject.optString("signature")
                            Utils.setSignature(signature)
                            createSignature()
                        } else {
                            Utils.setSignature("")
                        }
                        val detailsArray = responseObject.optJSONArray("receiptDetails")
                        for (i in 0 until detailsArray.length()) {
                            val `object` = detailsArray.getJSONObject(i)
                            val invoiceListModel = ReceiptsDetails()
                            invoiceListModel.invoiceNumber = `object`.optString("invoiceNo")
                            invoiceListModel.invoiceDate = `object`.optString("invoiceDate")
                            invoiceListModel.amount = `object`.optString("paidAmount")
                            invoiceListModel.discountAmount = `object`.optString("discountAmount")
                            invoiceListModel.balanceAmount = `object`.optString("balanceAmount")
                            invoiceListModel.creditAmount = `object`.optString("creditAmount")

                            receiptsList.add(invoiceListModel)
                        }
                        model.setReceiptsDetailsList(receiptsList)
                        receiptsHeaderDetails!!.add(model)
                    } else {
                    }
                    printReceipt()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                // pDialog.dismiss();
                Log.w("Error_throwing:", error.toString())
            }) {
            override fun getHeaders(): Map<String, String> {
                val params = java.util.HashMap<String, String>()
                val creds =
                    String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD)
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


    fun printReceipt() {
        if (validatePrinterConfiguration()) {

            try {
// Bluetooth is enabled
                //  TSCPrinter tscPrinter = new TSCPrinter(ReceiptsPrintPreview.this, printerMacId);
                //  tscPrinter.printInvoice(receiptsHeaderDetails, receiptsList);
                val printer = TSCPrinter(this, printerMacId, "Receipt")
                // try {
                // try {
                Log.w("entryprintRecp","");
                printer.printReceipts(1, receiptsHeaderDetails, receiptsList)

                printer.setOnCompletionListener {
                    Utils.setSignature("")
                    Toast.makeText(
                        applicationContext,
                        "Receipt printed successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

//            Utils.setSignature("")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("Error1:", Objects.requireNonNull(e.message!!))
            }
        }
        else{
            Toast.makeText(applicationContext, "Please configure the Printer", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun getCustomerDetails(customerCode: String?, isloader: Boolean, from: String?) {
        // Initialize a new RequestQueue instance
        val requestQueue = Volley.newRequestQueue(this)
        // Initialize a new JsonArrayRequest instance
        val jsonObject = JSONObject()
        try {
            jsonObject.put("CustomerCode", customerCode)
            // jsonObject.put("CompanyCode",companyCode);
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.w("Error7:", Objects.requireNonNull(e.message!!))
        }
        Log.w("JsonValueForCustomer:", jsonObject.toString())
        val url = Utils.getBaseUrl(applicationContext) + "Customer"
        Log.w("Given_urlCusto:", url+ jsonObject.toString())

        val progressDialog = ProgressDialog(applicationContext)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Customer Details Loading...")
        if (isloader) {
            progressDialog.show()
        }
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response: JSONObject ->
                    try {
                        progressDialog.dismiss()
                        Log.w("res_custPdt:", response.toString())
                        val customerList = ArrayList<CustomerModel>()
                        val statusCode = response.optString("statusCode")
                        if (statusCode == "1") {
                            customerResponse = response
                            val customerDetailArray = response.optJSONArray("responseData")
                            for (i in 0 until customerDetailArray.length()) {
                                val `object` = customerDetailArray.optJSONObject(i)
                                //  if (customerObject.optBoolean("IsActive")) {
                                val model = CustomerModel()
                                model.customerCode = `object`.optString("customerCode")
                                model.customerName = `object`.optString("customerName")
                                model.address1 = `object`.optString("address")
                                model.address2 = `object`.optString("street")
                                model.address3 = `object`.optString("city")
                                model.customerAddress = `object`.optString("address")
                                model.haveTax = `object`.optString("HaveTax")
                                model.taxType = `object`.optString("taxType")
                                model.taxPerc = `object`.optString("taxPercentage")
                                model.taxCode = `object`.optString("taxCode")
                                model.allowFOC = `object`.optString("allowFOC")
                                model.creditLimitAmount = `object`.optString("creditLimit")
                                creditLimitAmount = `object`.optString("creditLimit")
                                outstandingAmount = `object`.optString("outstandingAmount")
                                if(`object`.optString("allowFOC").equals("Yes",true)){
                                    focEditText!!.isEnabled = true
                                }
                                else{
                                    focEditText!!.isEnabled = false
                                }
                                //  model.setCustomerBarcode(object.optString("BarCode"));
                                // model.setCustomerBarcode(String.valueOf(i));
                                if (`object`.optString("outstandingAmount") == "null" || `object`.optString(
                                        "outstandingAmount"
                                    ).isEmpty()
                                ) {
                                    model.outstandingAmount = "0.00"
                                } else {
                                    model.outstandingAmount =
                                        `object`.optString("outstandingAmount")
                                }
                                customerList.add(model)
                                // }
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Error,in getting Customer list",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        // pDialog.dismiss();
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.w("Error6:", Objects.requireNonNull(e.message!!))
                    }
                }, Response.ErrorListener { error: VolleyError ->
                    // Do something when error occurred
                    //  pDialog.dismiss();
                    Log.d("Error_throwing:", error.toString())
                    progressDialog.dismiss()
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
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    private fun setupGroup(itemGroupLists: ArrayList<ItemGroupList>) {
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemGroupLists)
        groupspinner!!.adapter = myAdapter
        groupspinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                val jsonObject = JSONObject()
                try {
                    val itemCode = itemGroup!![i].groupCode
                    Log.e("selectspinn", "" + itemCode)
                    //  if (!itemCode.equals("Select Brand")){
                    jsonObject.put("User", username)
                    jsonObject.put("CardCode", customerCode)
                    jsonObject.put("ItemGroupCode", itemCode)
                    getAllProducts(jsonObject)
                    //  }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.w("Error5:", Objects.requireNonNull(e.message!!))
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                return
            }
        }
    }

    @get:Throws(JSONException::class)
    private val grouplist: ArrayList<ItemGroupList>
        private get() {
            val requestQueue = Volley.newRequestQueue(this)
            val url = Utils.getBaseUrl(applicationContext) + "ItemGroupList"
            // Initialize a new JsonArrayRequest instance
            Log.w("Given_url_gup_brand:", url)
            //    pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            //   pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            //   pDialog.setTitleText("Loading Groups...");
            //  pDialog.setCancelable(false);
            //  pDialog.show();
            itemGroup = ArrayList()
            //  itemGroup.add(new ItemGroupList("Select Brand", "Select Brand"));
            val jsonObjectRequest: JsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url, null,
                    Response.Listener { response: JSONObject ->
                        try {
                            Log.w("group_brandlist:", response.toString())

                            // pDialog.dismiss();
                            val statusCode = response.optString("statusCode")
                            val statusMessage = response.optString("statusMessage")
                            if (statusCode == "1") {
                                val groupArray = response.optJSONArray("responseData")
                                for (i in 0 until groupArray.length()) {
                                    val jsonObject = groupArray.getJSONObject(i)
                                    val groupName = jsonObject.getString("itemGroupName")
                                    val groupCode = jsonObject.getString("itemGroupCode")
                                    val itemGroupList = ItemGroupList(groupCode, groupName)
                                    itemGroup!!.add(itemGroupList)
                                }
                                if (itemGroup!!.size > 0) {
                                    setupGroup(itemGroup!!)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.w("Error4:", Objects.requireNonNull(e.message!!))
                        }
                    }, Response.ErrorListener { error: VolleyError ->
                        // Do something when error occurred
                        //  pDialog.dismiss();
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
            return itemGroup!!
        }

    fun redirectActivity() {
        orderNoText!!.setText("")
        val intent: Intent
        intent = if (activityFrom == "iv" || activityFrom == "Duplicate"
            || activityFrom == "ConvertInvoiceFromDO" || activityFrom == "ReOrderInvoice") {
            Intent(this@CreateNewInvoiceActivity, NewInvoiceListActivity::class.java)
        } else {
            Intent(this@CreateNewInvoiceActivity, SalesOrderListActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
    fun getCurrentLocation() {
        locationTrack = LocationTrack(this@CreateNewInvoiceActivity)
        if (locationTrack!!.canGetLocation()) {
            val longitude: Double = locationTrack!!.getLongitude()
            val latitude: Double = locationTrack!!.getLatitude()
            current_latitude = latitude.toString()
            current_longitude = longitude.toString()
            Log.w("longInv",""+longitude)
            if(current_latitude != null && current_latitude.isNotEmpty()) {
                val currentAddress =
                    Utils.getCompleteAddress(this@CreateNewInvoiceActivity, latitude, longitude)
                if (currentAddress != null && !currentAddress.isEmpty()) {
                    currentAddressl = currentAddress
                    Log.w("longAddrInv", "" + currentAddressl)

                    // locationText.setText(currentAddress)
                }
            }
        } else {
            // locationTrack.showSettingsAlert();
        }
    }

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        currentLocationLatitude = latitude
        currentLocationLongitude = longitude
        val currentAddress = Utils.getCompleteAddress(this@CreateNewInvoiceActivity, latitude, longitude)
        if (currentAddress != null && !currentAddress.isEmpty()) {
           // locationText.setText(currentAddress)
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val count = dbHelper!!.numberOfRowsInInvoice()
            if (count > 0) {
                showDeleteAlert()
            } else {
                finish()
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.save_menu, menu)
        save_btn = menu.findItem(R.id.action_save)
        down_menu = menu.findItem(R.id.action_down)
        if (activityFrom == "iv" || activityFrom == "ConvertInvoice"
            || activityFrom == "Duplicate" || activityFrom == "ConvertInvoiceFromDO" || activityFrom == "ReOrderInvoice") {
            down_menu!!.isVisible = true
        }
        return true
    }

    fun showDeleteAlert() {
        val builder1 = AlertDialog.Builder(this@CreateNewInvoiceActivity)
        builder1.setMessage("Data Will be Cleared are you sure want to back?")
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "Yes"
        ) { dialog, id ->
            dbHelper!!.removeAllInvoiceItems()
            dbHelper!!.removeAllReturn()
            finish()
            dialog.cancel()
        }
        builder1.setNegativeButton(
            "No"
        ) { dialog, id -> dialog.cancel() }
        val alert11 = builder1.create()
        alert11.show()
    }

    @Throws(JSONException::class)
    private fun getInvoicePrintDetails(invoiceNumber: String, copy: Int , isDoPrint: Boolean) {
        // Initialize a new RequestQueue instance
        val jsonObject = JSONObject()
        // jsonObject.put("CompanyCode", companyId);
        jsonObject.put("InvoiceNo", invoiceNumber)
        jsonObject.put("LocationCode", locationCode)
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "InvoiceDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_urlInv:", "$url ..$jsonObject")
        invoiceHeaderDetails = ArrayList()
        invoicePrintList = ArrayList()
        salesReturnList = ArrayList()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener { response: JSONObject ->
                try {
                    Log.w("DetailsResponse:: ", response.toString())
                    val statusCode = response.optString("statusCode")
                    if (statusCode == "1") {
                        val responseData = response.getJSONArray("responseData")
                        val `object` = responseData.optJSONObject(0)
                        val model = InvoicePrintPreviewModel()
                        model.invoiceNumber = `object`.optString("invoiceNumber")
                        model.invoiceDate = `object`.optString("invoiceDate")
                        model.customerCode = `object`.optString("customerCode")
                        model.customerName = `object`.optString("customerName")
                        model.overAllTotal = `object`.optString("overAllTotal")
                        response
                        model.address =
                            `object`.optString("address1") + `object`.optString("address2") + `object`.optString(
                                "address3"
                            )
                        model.address1 = `object`.optString("address1")
                        model.address2 = `object`.optString("address2")
                        model.address3 = `object`.optString("address3")
                        model.addressstate =
                            (`object`.optString("street") + " "
                                    + `object`.optString("block") + " " +`object`.optString("city"))
                        model.addresssZipcode =
                            (`object`.optString("countryName") + " " + `object`.optString("state") + " "
                                    + `object`.optString("zipcode"))

                        // model.setDeliveryAddress(model.getAddress());
                        model.subTotal = `object`.optString("subTotal")
                        model.netTax = `object`.optString("taxTotal")
                        model.netTotal = `object`.optString("netTotal")
                        model.paymentTerm = `object`.optString("paymentTerm")
                        model.taxType = `object`.optString("taxType")
                        model.taxValue = `object`.optString("taxPerc")
                        model.outStandingAmount = `object`.optString("totalOutstandingAmount")
                        model.balanceAmount = `object`.optString("balanceAmount")
                        Utils.setInvoiceOutstandingAmount(`object`.optString("balanceAmount"))
                        Utils.setInvoiceMode("Invoice")
                        model.billDiscount = `object`.optString("billDiscount")
                        model.itemDiscount = `object`.optString("totalDiscount")
                        model.soNumber = `object`.optString("soNumber")
                        model.soDate = `object`.optString("soDate")
                        model.doDate = `object`.optString("doDate")
                        model.doNumber = `object`.optString("doNumber")
                        model.currentAddress = `object`.optString("CurrentAddress")
                        model.deliveryAddress =
                            `object`.optString("shipAddress2") + `object`.optString("shipAddress3") + `object`.optString(
                                "shipStreet"
                            )

                        model.allowDeliveryAddress = `object`.optString("showShippingAddress")
                        val signFlag = `object`.optString("signFlag")
                        if (signFlag == "Y") {
                            val signature = `object`.optString("signature")
                            Utils.setSignature(signature)
                            createSignature()
                        } else {
                            Utils.setSignature("")
                        }
                        val detailsArray = `object`.optJSONArray("invoiceDetails")
                        for (i in 0 until detailsArray.length()) {
                            val detailObject = detailsArray.optJSONObject(i)
                            if (detailObject.optString("quantity").toDouble() > 0) {
                            val invoiceListModel = InvoicePrintPreviewModel.InvoiceList()
                            invoiceListModel.productCode = detailObject.optString("productCode")
                            invoiceListModel.description = detailObject.optString("productName")
                            invoiceListModel.lqty = detailObject.optString("unitQty")
                            invoiceListModel.cqty = detailObject.optString("cartonQty")
                            invoiceListModel.netQty = detailObject.optString("quantity")
                            invoiceListModel.netQuantity = detailObject.optString("netQuantity")
                            invoiceListModel.focQty = detailObject.optString("foc_Qty")
                            invoiceListModel.returnQty = detailObject.optString("returnQty")
                            invoiceListModel.cartonPrice = detailObject.optString("cartonPrice")
                            invoiceListModel.unitPrice = detailObject.optString("price")
                                invoiceListModel.excQty = detailObject.optString("exc_Qty")
                                invoiceListModel.stockQty = detailObject.optString("stockInHand")
                                invoiceListModel.saleType =""
                            if (detailObject.optString("bP_CatalogNo") != null) {
                                invoiceListModel.customerItemCode = detailObject.optString("bP_CatalogNo")
                            }
                            val qty1 = detailObject.optString("quantity").toDouble()
                            val price1 = detailObject.optString("price").toDouble()
                            val nettotal1 = qty1 * price1
                            invoiceListModel.total = detailObject.optString("total")
                            invoiceListModel.pricevalue = price1.toString()
                            invoiceListModel.uomCode = detailObject.optString("uomCode")
                            invoiceListModel.pcsperCarton = detailObject.optString("pcsPerCarton")
                            invoiceListModel.itemtax = detailObject.optString("totalTax")
                            invoiceListModel.subTotal = detailObject.optString("subTotal")
                            invoicePrintList!!.add(invoiceListModel)
                        }
                            if (detailObject.optString("foc_Qty").toDouble() > 0) {
                                val invoiceListModel = InvoicePrintPreviewModel.InvoiceList()
                                invoiceListModel.productCode = detailObject.optString("productCode")
                                invoiceListModel.description = detailObject.optString("productName")
                                invoiceListModel.lqty = detailObject.optString("unitQty")
                                invoiceListModel.cqty = detailObject.optString("cartonQty")
                                invoiceListModel.netQty = detailObject.optString("quantity")
                                invoiceListModel.netQuantity = detailObject.optString("foc_Qty")
                                invoiceListModel.focQty = detailObject.optString("foc_Qty")
                                invoiceListModel.returnQty = detailObject.optString("returnQty")
                                invoiceListModel.cartonPrice = detailObject.optString("cartonPrice")
                                invoiceListModel.unitPrice = detailObject.optString("price")
                                invoiceListModel.saleType ="FOC"
                                invoiceListModel.excQty = detailObject.optString("exc_Qty")
                                invoiceListModel.stockQty = detailObject.optString("stockInHand")

                                if (detailObject.optString("bP_CatalogNo") != null) {
                                    invoiceListModel.customerItemCode = detailObject.optString("bP_CatalogNo")
                                }
                                val qty1 = detailObject.optString("quantity").toDouble()
                                val price1 = detailObject.optString("price").toDouble()
                                val nettotal1 = qty1 * price1
                                invoiceListModel.total = "0.00"
                                invoiceListModel.pricevalue = "0.00"
                                invoiceListModel.uomCode = detailObject.optString("uomCode")
                                invoiceListModel.pcsperCarton = detailObject.optString("pcsPerCarton")
                                invoiceListModel.itemtax = detailObject.optString("totalTax")
                                invoiceListModel.subTotal = detailObject.optString("subTotal")
                                invoicePrintList!!.add(invoiceListModel)
                            }

                            if (detailObject.optString("returnQty").toDouble() > 0) {
                                val invoiceListModel = InvoicePrintPreviewModel.InvoiceList()
                                invoiceListModel.productCode = detailObject.optString("productCode")
                                invoiceListModel.description = detailObject.optString("productName")
                                invoiceListModel.lqty = detailObject.optString("unitQty")
                                invoiceListModel.cqty = detailObject.optString("cartonQty")
                                invoiceListModel.netQty = detailObject.optString("quantity")
                                invoiceListModel.netQuantity = detailObject.optString("returnQty")
                                invoiceListModel.focQty = detailObject.optString("foc_Qty")
                                invoiceListModel.returnQty = detailObject.optString("returnQty")
                                invoiceListModel.cartonPrice = detailObject.optString("cartonPrice")
                                invoiceListModel.unitPrice = detailObject.optString("price")
                                invoiceListModel.excQty = detailObject.optString("exc_Qty")
                                invoiceListModel.saleType ="Return"
                                invoiceListModel.stockQty = detailObject.optString("stockInHand")

                                if (detailObject.optString("bP_CatalogNo") != null) {
                                    invoiceListModel.customerItemCode = detailObject.optString("bP_CatalogNo")
                                }
                                val qty1 = detailObject.optString("quantity").toDouble()
                                val price1 = detailObject.optString("price").toDouble()
                                val nettotal1 = qty1 * price1
                                invoiceListModel.total = "0.00"
                                invoiceListModel.pricevalue = "0.00"
                                invoiceListModel.uomCode = detailObject.optString("uomCode")
                                invoiceListModel.pcsperCarton = detailObject.optString("pcsPerCarton")
                                invoiceListModel.itemtax = detailObject.optString("totalTax")
                                invoiceListModel.subTotal = detailObject.optString("subTotal")
                                invoicePrintList!!.add(invoiceListModel)
                            }

                            if (detailObject.optString("exc_Qty").toDouble() > 0) {
                                val invoiceListModel = InvoicePrintPreviewModel.InvoiceList()
                                invoiceListModel.productCode = detailObject.optString("productCode")
                                invoiceListModel.description = detailObject.optString("productName")
                                invoiceListModel.lqty = detailObject.optString("unitQty")
                                invoiceListModel.cqty = detailObject.optString("cartonQty")
                                invoiceListModel.netQty = detailObject.optString("quantity")
                                invoiceListModel.netQuantity = detailObject.optString("exc_Qty")
                                invoiceListModel.focQty = detailObject.optString("foc_Qty")
                                invoiceListModel.returnQty = detailObject.optString("returnQty")
                                invoiceListModel.cartonPrice = detailObject.optString("cartonPrice")
                                invoiceListModel.unitPrice = detailObject.optString("price")
                                invoiceListModel.saleType ="Exchange"
                                invoiceListModel.excQty = detailObject.optString("exc_Qty")
                                invoiceListModel.stockQty = detailObject.optString("stockInHand")

                                if (detailObject.optString("bP_CatalogNo") != null) {
                                    invoiceListModel.customerItemCode = detailObject.optString("bP_CatalogNo")
                                }
                                val qty1 = detailObject.optString("quantity").toDouble()
                                val price1 = detailObject.optString("price").toDouble()
                                val nettotal1 = qty1 * price1
                                invoiceListModel.total = "0.00"
                                invoiceListModel.pricevalue = "0.00"
                                invoiceListModel.uomCode = detailObject.optString("uomCode")
                                invoiceListModel.pcsperCarton = detailObject.optString("pcsPerCarton")
                                invoiceListModel.itemtax = detailObject.optString("totalTax")
                                invoiceListModel.subTotal = detailObject.optString("subTotal")
                                invoicePrintList!!.add(invoiceListModel)
                            }

                            model.invoiceList = invoicePrintList
                            invoiceHeaderDetails!!.add(model)
                        }
                        val SRArray = `object`.optJSONArray("sR_Details")!!
                        if (SRArray.length() > 0) {
                            val SRoblect = SRArray.optJSONObject(0)
                            val salesReturnModel = InvoicePrintPreviewModel.SalesReturnList()
                            salesReturnModel.salesReturnNumber =
                                SRoblect.optString("salesReturnNumber")
                            salesReturnModel.setsRSubTotal(SRoblect.optString("sR_SubTotal"))
                            salesReturnModel.setsRTaxTotal(SRoblect.optString("sR_TaxTotal"))
                            salesReturnModel.setsRNetTotal(SRoblect.optString("sR_NetTotal"))
                            salesReturnList!!.add(salesReturnModel)
                        }
                        model.salesReturnList = salesReturnList
                        invoiceHeaderDetails!!.add(model)

                        printInvoice(copy,isDoPrint)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Error in printing Data...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w("Error3:", Objects.requireNonNull(e.message!!))
                }
            },
            Response.ErrorListener { error: VolleyError ->
                // Do something when error occurred
                //  pDialog.dismiss();
                Log.w("Error_throwing:", error.toString())
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
    fun validatePrinterConfiguration(): Boolean {
        var printetCheck = false
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "This device does not support bluetooth", Toast.LENGTH_SHORT)
                .show()
        } else if (!mBluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled :)
            Toast.makeText(this, "Enable bluetooth and connect the printer", Toast.LENGTH_SHORT)
                .show()
        } else {
            // Bluetooth is enabled
            if (!printerType!!.isEmpty() && !printerMacId!!.isEmpty()) {
                printetCheck = true
            } else {
                Toast.makeText(this, "Please configure Printer", Toast.LENGTH_SHORT).show()
            }
        }
        return printetCheck
    }

    fun printInvoice(copy: Int,isDoPrint: Boolean) {
        if (validatePrinterConfiguration()) {

            try {
                /* if (pDialog!=null && pDialog.isShowing()){
                pDialog.dismiss();
            }*/
// Bluetooth is enabled

                    PrinterUtils(this, printerMacId).printInvoice(
                        copy,
                        invoiceHeaderDetails,
                        invoicePrintList,
                        isDoPrint.toString()
                    )

//            Utils.setSignature("")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("Error1:", Objects.requireNonNull(e.message!!))
            }
        }
        else{
            Toast.makeText(applicationContext, "Please configure the Printer", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun createSignature() {
        if (Utils.getSignature() != null && !Utils.getSignature().isEmpty()) {
            try {
                ImageUtil.saveStamp(this, Utils.getSignature(), "Signature")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.w("Error2:", Objects.requireNonNull(e.message!!))            }
        }
    }

    @Throws(JSONException::class)
    private fun getSalesOrderDetails(soNumber: String, copy: Int) {
        // Initialize a new RequestQueue instance
        val jsonObject = JSONObject()
        //  jsonObject.put("CompanyCode",companyId);
        jsonObject.put("SalesOrderNo", soNumber)
        // jsonObject.put("LocationCode",locationCode);
        val requestQueue = Volley.newRequestQueue(this)
        val url = Utils.getBaseUrl(this) + "SalesOrderDetails"
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:", url)
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
                        Log.w("Sales_Details_add:", response.toString()+jsonObject)
                        val statusCode = response.optString("statusCode")
                        if (statusCode == "1") {
                            val responseData = response.getJSONArray("responseData")
                            val `object` = responseData.optJSONObject(0)
                            val model = SalesOrderPrintPreviewModel()
                            model.soNumber = `object`.optString("soNumber")
                            model.soDate = `object`.optString("soDate")
                            model.customerCode = `object`.optString("customerCode")
                            model.customerName = `object`.optString("customerName")
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
                            if (signFlag == "Y") {
                                val signature = `object`.optString("signature")
                                Utils.setSignature(signature)
                                createSignature()
                            } else {
                                Utils.setSignature("")
                            }
                            val detailsArray = `object`.optJSONArray("salesOrderDetails")
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
                                    salesListModel.uomCode = detailObject.optString("UOMCode")
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
                            sentSalesOrderDataPrint(copy)
                            // pDialog.dismiss();
                        } else {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.w("Error1r:", Objects.requireNonNull(e.message!!))
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

    fun createInvoiceJson(copy: Int) {
        // JSONObject rootJsonObject = new JSONObject();
        val rootJsonObject = JSONObject()
        val invoiceDetailsArray = JSONArray()
        var returnProductArray = JSONArray()
        var returnProductObject = JSONObject()
        var invoiceObject = JSONObject()

        if(bill_disc_amt_ed!!.text.toString().isEmpty() ||
            bill_disc_amt_ed!!.text.toString().toDouble() > 0.00){
            billDiscSetAmt_api = "0.00"
        }
        else{
            billDiscSetAmt_api = bill_disc_amt_ed!!.text.toString()
        }
        if(sharedPref_billdisc!!.getString("billDisc_percent", "")!!.isEmpty()){
            billDisPercent_share_api = "0.00"
        }
        else{
            billDisPercent_share_api = sharedPref_billdisc!!.getString("billDisc_percent", "")
        }

        Log.w("customerResponsekk",""+customerResponse.optJSONArray("responseData"))
        val detailsArray = customerResponse.optJSONArray("responseData")
        val `object` = detailsArray.optJSONObject(0)
//        if(customerResponse.optJSONArray("responseData")!!.equals("null")){
//            Toast.makeText(this, "Customer detail is empty!", Toast.LENGTH_SHORT).show()
//        }
      try {
            Log.w("enrtyj1","")

            // Sales Header Add values
            /*  if (activityFrom.equals("InvoiceEdit")){
                rootJsonObject.put("invoiceNumber", AddInvoiceActivity.editInvoiceNumber);
                rootJsonObject.put("mode", "E");
            }else if (activityFrom.equals("ConvertInvoiceFromDO")){
                rootJsonObject.put("mode", "I");
                rootJsonObject.put("soNo","");
                rootJsonObject.put("doNo",AddInvoiceActivity.editDoNumber);
                rootJsonObject.put("invoiceNumber", "");
            }
            else if (activityFrom.equals("ConvertInvoice")){
                rootJsonObject.put("mode", "I");
                rootJsonObject.put("soNo",AddInvoiceActivity.editSoNumber);
                rootJsonObject.put("invoiceNumber", "");
            }else {
                rootJsonObject.put("invoiceNumber", "");
                rootJsonObject.put("mode", "I");
            }*/
          val inputFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
          val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
          var inDate: String? = null
          var invDate: String? = null
          val deldate: String? = null
          val orderdate: String? = null
          var fromDatel: Date? = null
          var oldToDatel: String? = ""

          if (Utils.getInvoiceDate() != null && !Utils.getInvoiceDate().isEmpty()) {
              oldToDatel = invoiceDateInv!!.getText().toString()

//              try {
//                  fromDatel = SimpleDateFormat("dd/MM/yyyy").parse(oldToDatel)
//              } catch (e: ParseException) {
//                  throw java.lang.RuntimeException(e)
//              }
              // inDate = SimpleDateFormat("yyyyMMdd").format(oldToDatel)
              if(!shortCodeStr.equals("TRAN")) {
                  inDate = df1.format(inputFormat.parse(Utils.getInvoiceDate()))
              }
              else{
                  inDate = currentDate
              }
             invDate = Utils.getInvoiceDate()
          } else {
              inDate = currentDate
//              invDate = currentDate
          }
          if (activityFrom == "ConvertInvoice") {
                rootJsonObject.put("mode", "I")
                rootJsonObject.put("soNo", editSoNumber)
                rootJsonObject.put("invoiceNumber", "")
                rootJsonObject.put("doNo", "")
              rootJsonObject.put("invoiceDate", inDate)

          } else if (activityFrom == "ConvertInvoiceFromDO") {
                rootJsonObject.put("mode", "I")
                rootJsonObject.put("soNo", "")
                rootJsonObject.put("doNo", editDoNumber)
                rootJsonObject.put("invoiceNumber", "")
              rootJsonObject.put("invoiceDate", inDate)

          } else if (activityFrom == "EditDo") {
                rootJsonObject.put("mode", "E")
                rootJsonObject.put("soNo", "")
                rootJsonObject.put("doNo", editDoNumber)
                rootJsonObject.put("invoiceNumber", "")
              rootJsonObject.put("invoiceDate", currentDate)

          } else {
                rootJsonObject.put("invoiceNumber", "")
                rootJsonObject.put("mode", "I")
                rootJsonObject.put("soNo", "")
                rootJsonObject.put("doNo", "")
              rootJsonObject.put("invoiceDate", inDate)

          }
            if (currentSaveDateTime == null || currentSaveDateTime!!.isEmpty()) {
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val currentDateandTime = sdf.format(Date())
                currentSaveDateTime = currentDateandTime
            }
            remarkStr = remarkText!!.text.toString()
            Log.w("remarkkc  ", "..$remarkStr")
            rootJsonObject.put("customerReferenceNo", orderNoText!!.text.toString())
            rootJsonObject.put("currentDateTime", currentSaveDateTime)
            rootJsonObject.put("customerCode", `object`["customerCode"])
            rootJsonObject.put("customerName", `object`["customerName"])
            rootJsonObject.put("address", `object`["address"])
            rootJsonObject.put("street", `object`["street"])
            rootJsonObject.put("city", `object`["city"])
            rootJsonObject.put("creditLimit", `object`["creditLimit"])
            rootJsonObject.put("Remark", remarkStr)
            rootJsonObject.put("currencyName", "Singapore Dollar")
            rootJsonObject.put("taxTotal", taxValueText!!.text.toString())
            rootJsonObject.put("subTotal", subTotalValue!!.text.toString())
            rootJsonObject.put("total", subTotalValue!!.text.toString())
            rootJsonObject.put("netTotal", netTotalValue!!.text.toString())
            rootJsonObject.put("itemDiscount", discountStr)
            rootJsonObject.put("billDiscount", sharedPref_billdisc!!.getString("billDisc_amt", ""))
            rootJsonObject.put("discountPercentage", billDisPercent_share_api)
            rootJsonObject.put("ChequeDateString", "")
            rootJsonObject.put("BankCode", "")
            rootJsonObject.put("AccountNo", "")
            rootJsonObject.put("ChequeNo", "")
          if(isEmailEnable) {
              rootJsonObject.put("SendMail", "Yes")
          }
          else{
              rootJsonObject.put("SendMail", "No")
          }
          if (isCashCollectCheck || isReceiptPrint) {
                  rootJsonObject.put("Paymode", "Cash")
          }else{
                  rootJsonObject.put("Paymode", "")
          }

          /*  if (customerResponse.optString("CurrencyCode").equals("SGD")){
              rootJsonObject.put("FTotal", totalValue);
              rootJsonObject.put("FItemDiscount", itemDiscountAmount);
              rootJsonObject.put("FBillDiscount", billDiscountAmount);
              rootJsonObject.put("FSubTotal", subTotalValue);
              rootJsonObject.put("FTax", taxValue);
              rootJsonObject.put("FNetTotal", netTotalValue);
          }else {
              rootJsonObject.put("FTotal", "0");
              rootJsonObject.put("FItemDiscount", "0");
              rootJsonObject.put("FBillDiscount", "0");
              rootJsonObject.put("FSubTotal", "0");
              rootJsonObject.put("FTax", "0");
              rootJsonObject.put("FNetTotal", "0");
          }*/rootJsonObject.put("totalDiscount", "0")
            rootJsonObject.put("billDiscountPercentage", billDisPercent_share_api)
            rootJsonObject.put("deliveryCode", SettingUtils.getDeliveryAddressCode())
            rootJsonObject.put("delCustomerName", "")
            rootJsonObject.put("delAddress1", deliverAddrNameStr)
            rootJsonObject.put("delAddress2 ", `object`.optString("delAddress2"))
            rootJsonObject.put("delAddress3 ", `object`.optString("delAddress3"))
            rootJsonObject.put("delPhoneNo", `object`.optString("contactNo"))
          rootJsonObject.put("remark", `object`.optString("remark"))
            rootJsonObject.put("haveTax", `object`.optString("haveTax"))
            rootJsonObject.put("taxType", `object`.optString("taxType"))
            rootJsonObject.put("taxPerc", `object`.optString("taxPercentage"))
            rootJsonObject.put("taxCode", `object`.optString("taxCode"))
            rootJsonObject.put("currencyCode", `object`.optString("currencyCode"))
            rootJsonObject.put("currencyValue", "")
            rootJsonObject.put("CurrencyRate", "1")
            rootJsonObject.put("status", "0")
            rootJsonObject.put("postalCode", `object`.optString("postalCode"))
            rootJsonObject.put("createUser", username)
            rootJsonObject.put("modifyUser", username)
            rootJsonObject.put("companyName", companyName)
            rootJsonObject.put("stockUpdated", "1")
            rootJsonObject.put("invoiceType", "M")
            rootJsonObject.put("companyCode", companyCode)
            rootJsonObject.put("locationCode", locationCode)
          rootJsonObject.put("latitude", current_latitude)
          rootJsonObject.put("longitude", current_longitude)
          rootJsonObject.put("CurrentAddress", currentAddressl)
          rootJsonObject.put("image", imageString)
          rootJsonObject.put("signature", signatureString)


            // Sales Details Add to the Objects
            val localCart = dbHelper!!.allInvoiceProducts
            var index = 1
            for (model in localCart) {
                invoiceObject = JSONObject()
                /*   if (activityFrom.equals("InvoiceEdit")){
                    rootJsonObject.put("invoiceNumber", AddInvoiceActivity.editInvoiceNumber);
                }else {
                    rootJsonObject.put("invoiceNumber", "");
                }*/invoiceObject.put("invoiceNumber", "")
                invoiceObject.put("companyCode", companyCode)
                invoiceObject.put("invoiceDate", currentDate)
                invoiceObject.put("slNo", index)
                invoiceObject.put("productCode", model.productCode)
                invoiceObject.put("productName", model.productName)
                // convert into int
                invoiceObject.put("price", Utils.fourDecimalPoint(model.price.toDouble()))
                invoiceObject.put("total", Utils.twoDecimalPoint(model.total.toDouble()))
                var itemval = "0"
                if(model.itemDisc.isNotEmpty()){
                    itemval = model.itemDisc
                }
                invoiceObject.put("itemDiscount",itemval)
                invoiceObject.put("totalTax", Utils.twoDecimalPoint(model.gstAmount.toDouble()))
                invoiceObject.put("subTotal", Utils.twoDecimalPoint(model.subTotal.toDouble()))
                invoiceObject.put("netTotal", Utils.twoDecimalPoint(model.netTotal.toDouble()))
                invoiceObject.put("taxType", `object`.optString("taxType"))
                invoiceObject.put("taxPerc", `object`.optString("taxPercentage"))
                var return_subtotal = 0.0
                if (model.returnQty != null && !model.returnQty.isEmpty() && model.returnQty != "null") {
                    return_subtotal = model.returnQty.toDouble() * model.price.toDouble()
                }
                if (!model.returnQty.isEmpty() && model.returnQty.toString() != "null") {
                    invoiceObject.put("returnLQty", model.returnQty)
                    invoiceObject.put("returnQty", model.returnQty)
                    invoiceObject.put("qty", model.actualQty.toString())
                } else {
                    invoiceObject.put("returnLQty", "0")
                    invoiceObject.put("returnQty", "0")
                    invoiceObject.put("qty", model.actualQty.toString())
                }
                if (!model.focQty.toString().isEmpty() && model.focQty != "null") {
                    invoiceObject.put("focQty", model.focQty)
                } else {
                    invoiceObject.put("focQty", "0")
                }
                if (!model.focQty.toString().isEmpty() && model.focQty != "null") {
                    invoiceObject.put("ExcQty", model.exchangeQty)
                } else {
                    invoiceObject.put("ExcQty", "0")
                }
                invoiceObject.put("returnSubTotal", Utils.twoDecimalPoint(return_subtotal))
                invoiceObject.put("returnNetTotal", Utils.twoDecimalPoint(return_subtotal))
                invoiceObject.put("taxCode", `object`.optString("taxCode"))
                invoiceObject.put("returnReason", "")
                invoiceObject.put("uomCode", model.uomCode)
                invoiceObject.put("itemRemarks", "")
                invoiceObject.put("locationCode", locationCode)
                invoiceObject.put("createUser", username)
                invoiceObject.put("modifyUser", username)
                val returnProducts = dbHelper!!.getReturnProducts(model.productCode)
                Log.w("returnarray1",""+returnProducts.size)

                returnProductArray = JSONArray()
                if (returnProducts.size > 0) {
                    for (returnProductsModel in returnProducts) {
                        Log.w(
                            "ReturnProductsValues:",
                            returnProductsModel.productCode + "-" +
                                    returnProductsModel.productName + "--" + returnProductsModel.returnQty
                        )
                        if (returnProductsModel.returnQty.toDouble() > 0.0) {
                            returnProductObject = JSONObject()
                            returnProductObject.put(
                                "ReturnReason",
                                returnProductsModel.returnReason
                            )
                            returnProductObject.put("ReturnQty", returnProductsModel.returnQty)
                            returnProductArray.put(returnProductObject)
                      }
                    }
                }
                invoiceObject.put("ReturnDetails", returnProductArray)
                invoiceDetailsArray.put(invoiceObject)
                Log.w("returnarra",""+returnProductArray)
                index++
            }

            rootJsonObject.put("PostingInvoiceDetails", invoiceDetailsArray)
            Log.w("RootJsonSaveInv:", rootJsonObject.toString())
          Log.w("billds_inv",""+".. "+ sharedPref_billdisc!!.getString("billDisc_amt", ""))

          // Timber.d("APIRESPON: " + rootJsonObject.toString())

            saveSalesOrder(rootJsonObject, "Invoice", copy)
        }

        catch (e: JSONException) {
            e.printStackTrace()
            Log.w("Error1s:", Objects.requireNonNull(e.message!!))
        }
    }

    fun createDOJson(copy: Int) {
        val rootJsonObject = JSONObject()
        val saleDetailsArray = JSONArray()
        var deliveryObject = JSONObject()
        try {
            // Sales Header Add values
            val detailsArray = customerResponse.optJSONArray("responseData")
            val `object` = detailsArray.optJSONObject(0)
            if (activityFrom == "doEdit") {
                rootJsonObject.put("doNumber", editDoNumber.toString())
                rootJsonObject.put("mode", "E")
                rootJsonObject.put("status", "O")
            } else {
                rootJsonObject.put("doNumber", "")
                rootJsonObject.put("mode", "I")
                rootJsonObject.put("status", "")
            }
            rootJsonObject.put("doDate", currentDate)
            rootJsonObject.put("customerCode", `object`.optString("customerCode"))
            rootJsonObject.put("customerName", `object`.optString("customerName"))
            rootJsonObject.put("address", `object`.optString("address"))
            rootJsonObject.put("street", `object`.optString("street"))
            rootJsonObject.put("city", `object`.optString("city"))
            rootJsonObject.put("creditLimit", `object`.optString("creditLimit"))
            rootJsonObject.put("remark", remarkStr)
            rootJsonObject.put("currencyName", "Singapore Dollar")
            rootJsonObject.put("total", subTotalValue!!.text.toString())
            rootJsonObject.put("itemDiscount", "0.00")
            rootJsonObject.put("billDiscount", "0.00")
            rootJsonObject.put("billDiscountPercentage", "0")
            rootJsonObject.put("subTotal", subTotalValue!!.text.toString())
            rootJsonObject.put("taxTotal", taxValueText!!.text.toString())
            rootJsonObject.put("netTotal", netTotalValue!!.text.toString())
            rootJsonObject.put("delCustomerName", "")
            rootJsonObject.put("delAddress1", deliverAddrNameStr)
            rootJsonObject.put("delAddress2 ", `object`.optString("delAddress2"))
            rootJsonObject.put("delAddress3 ", `object`.optString("delAddress3"))
            rootJsonObject.put("delPhoneNo", `object`.optString("contactNo"))
            rootJsonObject.put("haveTax", `object`.optString("haveTax"))
            rootJsonObject.put("taxType", `object`.optString("taxType"))
            rootJsonObject.put("taxPerc", `object`.optString("taxPercentage"))
            rootJsonObject.put("taxCode", `object`.optString("taxCode"))
            rootJsonObject.put("currencyCode", `object`.optString("currencyCode"))
            rootJsonObject.put("currencyValue", "")
            rootJsonObject.put("currencyRate", "1")
            rootJsonObject.put("postalCode", `object`.optString("postalCode"))
            rootJsonObject.put("createUser", username)
            rootJsonObject.put("modifyUser", username)
            rootJsonObject.put("companyCode", companyCode)
            rootJsonObject.put("locationCode", locationCode)
            rootJsonObject.put("latitude", current_latitude)
            rootJsonObject.put("longitude", current_longitude)
            rootJsonObject.put("CurrentAddress", currentAddressl)
            rootJsonObject.put("image", imageString)
            rootJsonObject.put("signature", signatureString)

            // Sales Details Add to the Objects
            val localCart = dbHelper!!.allInvoiceProducts
            var index = 1
            Log.w("Given_local_cart_size:", localCart.size.toString())
            for (model in localCart) {
                deliveryObject = JSONObject()
                /*   if (activityFrom.equals("InvoiceEdit")){
                    rootJsonObject.put("invoiceNumber", AddInvoiceActivity.editInvoiceNumber);
                }else {
                    rootJsonObject.put("invoiceNumber", "");
                }*/deliveryObject.put("invoiceNumber", "")
                deliveryObject.put("companyCode", companyCode)
                deliveryObject.put("invoiceDate", currentDate)
                deliveryObject.put("slNo", index)
                deliveryObject.put("productCode", model.productCode)
                deliveryObject.put("productName", model.productName)
                // convert into int
                deliveryObject.put("price", Utils.twoDecimalPoint(model.price.toDouble()))
                deliveryObject.put("total", Utils.twoDecimalPoint(model.total.toDouble()))
                deliveryObject.put("itemDiscount", "0.00")
                deliveryObject.put("totalTax", Utils.twoDecimalPoint(model.gstAmount.toDouble()))
                deliveryObject.put("subTotal", Utils.twoDecimalPoint(model.subTotal.toDouble()))
                deliveryObject.put("netTotal", Utils.twoDecimalPoint(model.netTotal.toDouble()))
                deliveryObject.put("taxType", `object`.optString("taxType"))
                deliveryObject.put("taxPerc", `object`.optString("taxPercentage"))
                var return_subtotal = 0.0
                if (model.returnQty != null && !model.returnQty.isEmpty() && model.returnQty != "null") {
                    return_subtotal = model.returnQty.toDouble() * model.price.toDouble()
                }
                assert(model.returnQty != null)
                if (!model.returnQty.isEmpty() && model.returnQty.toString() != "null") {
                    deliveryObject.put("returnLQty", model.returnQty)
                    deliveryObject.put("returnQty", model.returnQty)
                    deliveryObject.put("qty", model.actualQty.toString())
                } else {
                    deliveryObject.put("returnLQty", "0")
                    deliveryObject.put("returnQty", "0")
                    deliveryObject.put("qty", model.actualQty.toString())
                }
                if (!model.focQty.toString().isEmpty() && model.focQty != "null") {
                    deliveryObject.put("focQty", model.focQty)
                } else {
                    deliveryObject.put("focQty", "0")
                }
                deliveryObject.put("returnSubTotal", Utils.twoDecimalPoint(return_subtotal))
                deliveryObject.put("returnNetTotal", Utils.twoDecimalPoint(return_subtotal))
                deliveryObject.put("taxCode", `object`.optString("taxCode"))
                deliveryObject.put("returnReason", "")
                deliveryObject.put("uomCode", model.uomCode)
                deliveryObject.put("itemRemarks", "")
                deliveryObject.put("locationCode", locationCode)
                deliveryObject.put("createUser", username)
                deliveryObject.put("modifyUser", username)
                saleDetailsArray.put(deliveryObject)
                index++
            }
            rootJsonObject.put("PostingDeliveryOrderDetails", saleDetailsArray)
            Log.w("RootSaveJson:", rootJsonObject.toString())
            saveSalesOrder(rootJsonObject, "DeliveryOrder", copy)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.w("Error1a:", Objects.requireNonNull(e.message!!))
        }
    }

    @Throws(IOException::class)
    private fun sentSalesOrderDataPrint(copy: Int) {
        if (validatePrinterConfiguration()) {

            try {
                if (printerType == "TSC Printer") {
                    val printer =
                        TSCPrinter(this@CreateNewInvoiceActivity, printerMacId, "SalesOrder")
                    printer.printSalesOrder(copy, salesOrderHeaderDetails, salesPrintList)
                    Utils.setSignature("")
                } else if (printerType == "Zebra Printer") {
                    val zebraPrinterActivity =
                        ZebraPrinterActivity(this@CreateNewInvoiceActivity, printerMacId)
                    zebraPrinterActivity.printSalesOrder(
                        copy,
                        salesOrderHeaderDetails,
                        salesPrintList
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("Error1:", Objects.requireNonNull(e.message!!))
            }
        }
    }

    fun setNewPrint() {
        val printerUtils = PrinterUtils(this, printerMacId)
        printerUtils.printLabel()
    }

    companion object {
        var productList: ArrayList<ProductsModel>? = null
        var behavior: BottomSheetBehavior<*>? = null
        var discTextWatcher: TextWatcher? = null
        var exchangeTextWatcher: TextWatcher? = null
        var progressDialog: ProgressDialog? = null
        var stockProductView = "2"
        var barcodeText: TextView? = null
        var barCodeLayl: LinearLayout? = null
        var customerCode: String? = null
        var isPrintEnable = false
        var isEmailEnable = false
        var selectedBank: TextView? = null
        var amountText: EditText? = null
        var currentLocationLatitude = 0.0
        var currentLocationLongitude = 0.0
        var signatureString: String? = ""
        var imageString: String? = ""
        var current_latitude = "0.00"
        var current_longitude = "0.00"
        var currentAddressl = " "
        var currentDate: String? = null
        var customerResponse = JSONObject()
        var locationTrack: LocationTrack? = null
        var activityFrom: String? = ""

        @RequiresApi(api = Build.VERSION_CODES.O)
        private fun convertDate(strDate: String): String {
            @SuppressLint("SimpleDateFormat") val inputFormat: DateFormat =
                SimpleDateFormat("dd-MM-yyyy")
            @SuppressLint("SimpleDateFormat") val outputFormat: DateFormat =
                SimpleDateFormat("yyyyMMdd")
            var resultDate = ""
            try {
                resultDate = outputFormat.format(inputFormat.parse(strDate))
            } catch (e: ParseException) {
                e.printStackTrace()
                Log.w("Errorff:", Objects.requireNonNull(e.message!!))
            }
            return resultDate
        }
    }

    override fun onClick(view: View?) {
        if (view!!.id == R.id.bill_disc_save) {

            if (netTotalValue!!.text.toString().toDouble() > 0) {
                if (activityFrom == "iv" || activityFrom == "ConvertInvoice" || activityFrom == "ConvertInvoiceFromDO"
                    || activityFrom == "ReOrderInvoice" ) {
//                        showSaveAlert()
                    if (isCheckedCreditLimit) {
                        if (createInvoiceValidation()) {
                            Log.w("enrty1aa", "")
                            showSaveAlert();
                        } else {
                            Log.w("enrty1bq", "")
                            showAlertForCreditLimit();
                        }
                    } else {
                        Log.w("enrty1cc", "")
                        showSaveAlert();
                    }
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    " Nettotal should be great than zero",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        Log.i("TAGView", "" + keyCode);
//        return true
//    }
    var barcode = ""
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        try {
            if (event.action == KeyEvent.ACTION_DOWN) {
                val pressedKey = event.unicodeChar.toChar()
                barcode += pressedKey
            }
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                Toast.makeText(this, barcode.toString(), Toast.LENGTH_SHORT).show()
                barcodeText!!.text = barcode
                barCodeLayl!!.visibility = View.VISIBLE
                searchAndSendActivity(barcode)
                barcode = ""
            }

//            if (event.keyCode === KeyEvent.KEYCODE_ENTER) {
//                if (event.action === KeyEvent.ACTION_UP) {
//                   // setEnter()
//                   // searchAndSendActivity(barcodeText)
//
//                    return true
//                }
//            }
//            if (event.keyCode == KeyEvent.KEYCODE_BACK) {
//                false
//            }
//            val keyaction: Int = event.getAction()
//            if (keyaction == KeyEvent.ACTION_DOWN) {
//                val keycode: Int = event.getKeyCode()
//                val keyunicode: Int = event.getUnicodeChar(event.getMetaState())
//                val character = keyunicode.toChar()
//                if (keycode==66){
//                    keyEnterFunction()
//                }
//                if (keycode==4){
//                    hideKeyboard()
//                }
//                println("DEBUG MESSAGE KEY=$character KEYCODE=$keycode")
//            }
        }catch (ex:Exception){
            Log.w("DispatchKeyEventError:",ex.localizedMessage.toString())
        }
        return super.dispatchKeyEvent(event)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (locationTrack != null) {
            locationTrack!!.stopListener()
        }
    }

}