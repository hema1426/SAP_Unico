package com.winapp.saperp.salesreturn;

import static com.winapp.saperp.utils.Utils.twoDecimalPoint;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.winapp.saperp.R;
import com.winapp.saperp.activity.CreateNewInvoiceActivity;
import com.winapp.saperp.activity.NewInvoiceListActivity;
import com.winapp.saperp.activity.SalesOrderListActivity;
import com.winapp.saperp.adapter.NewProductSummaryAdapter;
import com.winapp.saperp.adapter.NewSalesReturnProductAdapter;
import com.winapp.saperp.adapter.SelectProductAdapter;
import com.winapp.saperp.db.DBHelper;
import com.winapp.saperp.model.AppUtils;
import com.winapp.saperp.model.CartModel;
import com.winapp.saperp.model.CreateInvoiceModel;
import com.winapp.saperp.model.CustomerDetails;
import com.winapp.saperp.model.CustomerModel;
import com.winapp.saperp.model.HomePageModel;
import com.winapp.saperp.model.InvoicePrintPreviewModel;
import com.winapp.saperp.model.ItemGroupList;
import com.winapp.saperp.model.ProductSummaryModel;
import com.winapp.saperp.model.ProductsModel;
import com.winapp.saperp.model.ReturnProductsModel;
import com.winapp.saperp.model.SalesOrderPrintPreviewModel;
import com.winapp.saperp.model.SettingsModel;
import com.winapp.saperp.thermalprinter.PrinterUtils;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.ImageUtil;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.SettingUtils;
import com.winapp.saperp.utils.Utils;
import com.winapp.saperp.zebraprinter.TSCPrinter;
import com.winapp.saperp.zebraprinter.ZebraPrinterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NewSalesReturnProductAddActivity extends AppCompatActivity {

    LinearLayout returnLayout;
    ImageView showHideButton;
    private NewSalesReturnProductAdapter productSummaryAdapter;
    private ArrayList<ProductSummaryModel> productSummaryList;
    RecyclerView productSummaryView;
    String companyCode;
    String username;
    HashMap<String ,String> user;
    SessionManager session;
    SweetAlertDialog pDialog;
    public static ArrayList<ProductsModel> productList;
    private RecyclerView productListView;
    private SelectProductAdapter selectProductAdapter;
    Button btnCancel;
    public static BottomSheetBehavior behavior;
    ImageView searchProduct;
    EditText productNameEditext;
    LinearLayout transLayout;
    TextView totalProducts;
    ArrayList<String> products;
    AutoCompleteTextView productAutoComplete;
    ArrayAdapter<String> autoCompleteAdapter;
    EditText cartonPrice;
    EditText loosePrice;
    EditText uomText;
    EditText pcsPerCarton;
    EditText stockCount;
    EditText qtyValue;
    EditText returnQtyText;
    EditText customerNameText;
    EditText priceText;
    TextView subTotalValue;
    TextView taxValueText;
    TextView netTotalValue;
    ArrayList<CustomerDetails> customerDetails;
    private DBHelper dbHelper;
    TextView taxTitle;
    Button addProduct;
    ProductsModel productsModel;
    TextView itemCount;
    TextView noproductText;
    String productId;
    String productName;
    boolean isCartonQtyEdit=false;
    boolean isQtyEdit=false;
    TextWatcher qtyTextWatcher;
    TextWatcher cartonTextWatcher;
    TextWatcher lqtyTextWatcher;
    TextWatcher cartonPriceTextWatcher;
    TextWatcher loosePriceTextWatcher;
    Switch focSwitch;
    Switch exchangeSwitch;
    Switch returnSwitch;
    EditText focEditText;
    EditText exchangeEditext;
    EditText discountEditext;
    EditText returnEditext;
    ImageView scanProduct;
    int RESULT_CODE=12;
    String locationCode;
    TextView stockQtyValue;
    LinearLayout stockLayout;
    private TextWatcher cqtyTW, lqtyTW, qtyTW;
    String beforeLooseQty,ss_Cqty;
    boolean isAllowLowStock=false;
    LinearLayout focLayout;
    FloatingActionButton sortButton;
    LinearLayout emptyLayout;
    LinearLayout productLayout;

    private boolean isViewShown = false;
    public String selectCustomerId;
    static ProgressDialog progressDialog;
    public static String stockProductView="2";

    boolean isReverseCalculationEnabled=true;
    boolean isCartonPriceEdit=true;
    boolean isPriceEdit=true;
    public CheckBox salesReturn;
    public TextView salesReturnText;
    public TextView minimumSellingPriceText;
    public static EditText barcodeText;
    public static String customerCode;
    public static boolean isPrintEnable=false;
    private ImageView cancelSheet;
    public static TextView selectedBank;
    private Button cancelButton;
    private Button okButton;
    public static EditText amountText;
    private AlertDialog alert;
    public CheckBox invoicePrintCheck;

    static double currentLocationLatitude=0.0;
    static double currentLocationLongitude=0.0;
    public static String signatureString="";
    public static String imageString;

    public static String current_latitude="0.00";
    public static String current_longitude="0.00";
    public static String currentDate;
    public String companyName;
    public static JSONObject customerResponse=new JSONObject();

    private String printerMacId;
    private String printerType;
    private SharedPreferences sharedPreferences;
    private EditText remarkText;
    private String activityFrom="";
    public TextView saveTitle;
    private TextView saveMessage;
    private String editSoNumber="";
    public TextView returnAdj;
    public LinearLayout returnLayoutView;
    public Button cancelReturn;
    public Button saveReturn;
    public TextView netReturnQty;
    public EditText expiryReturnQty;
    public EditText damageReturnQty;
    public TextWatcher expiryQtyTextWatcher;
    public TextWatcher damageQtyTextWatcher;
    public TextView invoiceDate;
    private int mYear, mMonth, mDay, mHour, mMinute;
    ArrayList<ItemGroupList> itemGroup;
    Spinner groupspinner;
    String currentSaveDateTime="";
    private ArrayList<InvoicePrintPreviewModel> invoiceHeaderDetails;
    private ArrayList<InvoicePrintPreviewModel.InvoiceList> invoicePrintList;
    private ArrayList<InvoicePrintPreviewModel.SalesReturnList> salesReturnList ;
    private ArrayList<SalesOrderPrintPreviewModel> salesOrderHeaderDetails;
    private ArrayList<SalesOrderPrintPreviewModel.SalesList> salesPrintList;
    private static String currentDateString;
    public static String salesReturnNo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sales_return_product_add);

        getSupportActionBar().setTitle("Sales Return");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        session=new SessionManager(this);
        user=session.getUserDetails();
        dbHelper=new DBHelper(this);
        progressDialog =new ProgressDialog(this);
        companyCode=user.get(SessionManager.KEY_COMPANY_CODE);
        companyName=user.get(SessionManager.KEY_COMPANY_NAME);
        username=user.get(SessionManager.KEY_USER_NAME);
        locationCode=user.get(SessionManager.KEY_LOCATION_CODE);
        returnLayout=findViewById(R.id.return_layout);
        showHideButton=findViewById(R.id.show_hide);
        productSummaryView=findViewById(R.id.product_summary);
        productListView=findViewById(R.id.productList);
        btnCancel=findViewById(R.id.cancel_sheet);
        searchProduct=findViewById(R.id.search_product);
        productNameEditext=findViewById(R.id.product_search);
        transLayout=findViewById(R.id.trans_layout);
        totalProducts=findViewById(R.id.total_products);
        productAutoComplete=findViewById(R.id.product_name);
        groupspinner=findViewById(R.id.spinner_group);
        cartonPrice=findViewById(R.id.carton_price);
        priceText=findViewById(R.id.price);
        loosePrice=findViewById(R.id.loose_price);
        pcsPerCarton=findViewById(R.id.pcs_per_ctn);
        uomText=findViewById(R.id.uom);
        stockCount=findViewById(R.id.stock_count);
        qtyValue=findViewById(R.id.qty);
        subTotalValue=findViewById(R.id.balance_value);
        taxValueText=findViewById(R.id.tax);
        netTotalValue=findViewById(R.id.net_total);
        taxTitle=findViewById(R.id.tax_title);
        addProduct=findViewById(R.id.add_product);
        itemCount=findViewById(R.id.item_count);
        noproductText=findViewById(R.id.no_product_text);
        focSwitch=findViewById(R.id.foc_switch);
        exchangeSwitch=findViewById(R.id.exchange_switch);
        returnSwitch=findViewById(R.id.return_switch);
        focEditText=findViewById(R.id.foc);
        exchangeEditext=findViewById(R.id.exchange_text);
        returnEditext=findViewById(R.id.return_text);
        discountEditext=findViewById(R.id.discount_text);
        scanProduct=findViewById(R.id.scan_product);
        stockQtyValue=findViewById(R.id.stock_qty);
        stockLayout=findViewById(R.id.stock_layout);
        focLayout=findViewById(R.id.foc_layout);
        emptyLayout=findViewById(R.id.empty_layout);
        sortButton=findViewById(R.id.fab);
        productLayout=findViewById(R.id.product_layout);
        salesReturn=findViewById(R.id.sales_return);
        salesReturnText=findViewById(R.id.sales_return_text);
        minimumSellingPriceText=findViewById(R.id.minimum_selling_price);
        barcodeText=findViewById(R.id.barcode_text);
        returnQtyText=findViewById(R.id.return_qty);
        customerNameText=findViewById(R.id.customer_name_text);
        remarkText=findViewById(R.id.remarks);
        returnAdj=findViewById(R.id.return_adj);
        returnLayoutView=findViewById(R.id.return_layout_view);
        cancelReturn=findViewById(R.id.cancel_return);
        saveReturn=findViewById(R.id.save_return);
        netReturnQty=findViewById(R.id.net_return_qty);
        expiryReturnQty=findViewById(R.id.saleable_return_qty);
        damageReturnQty=findViewById(R.id.damage_qty);
        invoiceDate=findViewById(R.id.invoice_date);
        products=new ArrayList<>();
        productAutoComplete.clearFocus();
        barcodeText.requestFocus();

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType=sharedPreferences.getString("printer_type","");
        printerMacId=sharedPreferences.getString("mac_address","");

        productAutoComplete.setEnabled(false);

        //  PrinterUtils printerUtils=new PrinterUtils(this,printerMacId);
        //  printerUtils.connectPrinter();

        focEditText.setEnabled(false);
        exchangeEditext.setEnabled(false);
        discountEditext.setEnabled(false);
        returnEditext.setEnabled(false);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        currentDate = df1.format(c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        invoiceDate.setText(formattedDate);


        if (getIntent()!=null){
            customerNameText.setText(getIntent().getStringExtra("customerName"));
            customerCode=getIntent().getStringExtra("customerCode");
            activityFrom=getIntent().getStringExtra("from");
            editSoNumber=getIntent().getStringExtra("editSoNumber");
            currentSaveDateTime=getIntent().getStringExtra("currentDateTime");
            Log.w("GivenActivityFrom::",activityFrom.toString());
            getSupportActionBar().setTitle("Sales Return");
        }

        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("User",username);
            jsonObject.put("CardCode",customerCode);
            getAllProducts(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            getGrouplist();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("customerPref",MODE_PRIVATE);
        selectCustomerId = sharedPreferences.getString("customerId", "");
        if (selectCustomerId != null && !selectCustomerId.isEmpty()) {
            customerDetails = dbHelper.getCustomer(selectCustomerId);
            getCustomerDetails(selectCustomerId,false,"");
        }

        View bottomSheet =findViewById(R.id.design_bottom_sheet);
        bottomSheet.setBackgroundColor(Color.parseColor("#F2F2F2"));
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_SETTLING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED");
                        transLayout.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                        transLayout.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_HIDDEN");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i("BottomSheetCallback", "slideOffset: " + slideOffset);
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
            }
        });

        searchProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
            }
        });

        productNameEditext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                String product =editable.toString();
                if (!product.isEmpty()){
                    filter(product);
                }else {
                    setAdapter(AppUtils.getProductsList());
                }
            }
        });

        returnQtyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setCalculationView();
                if (!editable.toString().isEmpty()){
                    expiryReturnQty.setText(editable.toString());
                }else {
                    expiryReturnQty.setText("");
                }
            }
        });

        expiryQtyTextWatcher =new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!qtyValue.getText().toString().isEmpty()){
                    String netreturnqty=qtyValue.getText().toString();
                    int net_qty=Integer.parseInt(netreturnqty);
                    if(!s.toString().isEmpty()){
                        int damageqty=Integer.parseInt(s.toString());
                        if (net_qty > damageqty){
                            int value=net_qty-damageqty;
                            damageReturnQty.removeTextChangedListener(damageQtyTextWatcher);
                            damageReturnQty.setText(String.valueOf(value));
                            damageReturnQty.addTextChangedListener(damageQtyTextWatcher);
                        }else if (net_qty==damageqty){
                            damageReturnQty.removeTextChangedListener(damageQtyTextWatcher);
                            damageReturnQty.setText("0");
                            damageReturnQty.addTextChangedListener(damageQtyTextWatcher);
                        }else if (net_qty < damageqty){
                            expiryReturnQty.setText("0");
                            damageReturnQty.removeTextChangedListener(damageQtyTextWatcher);
                            damageReturnQty.setText(netreturnqty);
                            damageReturnQty.addTextChangedListener(damageQtyTextWatcher);
                            Toast.makeText(getApplicationContext(),"Return Qty Exceed...",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        damageReturnQty.removeTextChangedListener(damageQtyTextWatcher);
                        damageReturnQty.setText(netreturnqty);
                        damageReturnQty.addTextChangedListener(damageQtyTextWatcher);
                    }
                }
            }
        };
        expiryReturnQty.addTextChangedListener(expiryQtyTextWatcher);

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
        });*/


        damageQtyTextWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!qtyValue.getText().toString().isEmpty()){
                    String netreturnqty=qtyValue.getText().toString();
                    int net_qty=Integer.parseInt(netreturnqty);
                    if(!s.toString().isEmpty()){
                        int damageqty=Integer.parseInt(s.toString());
                        if (net_qty > damageqty){
                            int value=net_qty-damageqty;
                            expiryReturnQty.removeTextChangedListener(expiryQtyTextWatcher);
                            expiryReturnQty.setText(String.valueOf(value));
                            expiryReturnQty.addTextChangedListener(expiryQtyTextWatcher);
                        }else if (net_qty==damageqty){
                            expiryReturnQty.removeTextChangedListener(expiryQtyTextWatcher);
                            expiryReturnQty.setText("0");
                            expiryReturnQty.addTextChangedListener(expiryQtyTextWatcher);
                        }else if (net_qty < damageqty){
                            damageReturnQty.setText("0");
                            expiryReturnQty.removeTextChangedListener(expiryQtyTextWatcher);
                            expiryReturnQty.setText(netreturnqty);
                            expiryReturnQty.addTextChangedListener(expiryQtyTextWatcher);
                            Toast.makeText(getApplicationContext(),"Return Qty Exceed...",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        expiryReturnQty.removeTextChangedListener(expiryQtyTextWatcher);
                        expiryReturnQty.setText(netreturnqty);
                        expiryReturnQty.addTextChangedListener(expiryQtyTextWatcher);
                    }
                }
            }
        };
        damageReturnQty.addTextChangedListener(damageQtyTextWatcher);

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



        focEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //setCalculation();
                setCalculationView();
                //setButtonView();
            }
        });


        loosePriceTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setCalculationView();
            }
        };
        priceText.addTextChangedListener(loosePriceTextWatcher);


        qtyTW = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String qty = qtyValue.getText().toString();

                double stock=0.0;
                getLowStockSetting();

                if (!qty.matches("")) {

                    if (!stockQtyValue.getText().toString().isEmpty()){
                        stock=Double.parseDouble(stockQtyValue.getText().toString());
                    }

                    setCalculationView();
                    if (stock < Double.parseDouble(qty)){
                        // Toast.makeText(getActivity(),"Stock is Low please check",Toast.LENGTH_SHORT).show();
                        //  qtyValue.setText("");
                    }else {
                        setCalculationView();
                    }
                }

                int length = qtyValue.getText().length();
                if (length == 0) {
                    if (qtyValue.getText().toString().matches("0")) {

                    } else {
                        setCalculationView();
                    }
                    setCalculationView();
                }
             /*   if (activityFrom.equals("iv")){
                    if (!isAllowLowStock){
                        checkLowStock();
                    }
                }*/

                if (!s.toString().isEmpty()){
                    expiryReturnQty.setText(s.toString());
                }else {
                    expiryReturnQty.setText("");
                }

            }
        };

        qtyValue.addTextChangedListener(qtyTW);

        productAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                String selectProduct = (String)parent.getItemAtPosition(position);
                String[] product = selectProduct.split("~", 0);
                productId=product[1].trim();
                productName=product[0].trim();
                qtyValue.requestFocus();
                Log.w("Selected_product",product[1]);
                setProductResult(product[1].trim());
              /*  try {
                    getProductPrice(productId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

            }
        });


        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s=productAutoComplete.getText().toString();
                if (!qtyValue.getText().toString().isEmpty() && !s.isEmpty() && !qtyValue.getText().toString().equals("0") && !qtyValue.getText().toString().equals("00") && !qtyValue.getText().toString().equals("000") && !qtyValue.getText().toString().equals("0000") && !netTotalValue.getText().toString().equals("0.00")){
                    if (addProduct.getText().toString().equals("Update")){
                        // if (!cartonPrice.getText().toString().isEmpty() && !cartonPrice.getText().toString().equals("0.00") && !cartonPrice.getText().toString().equals("0.0") && !cartonPrice.getText().toString().equals("0")){
                        if (priceText.getText() != null && !priceText.getText().toString().isEmpty()) {
                            if (Double.parseDouble(priceText.getText().toString()) > 0) {
                                double minimumsellingprice=Double.parseDouble(minimumSellingPriceText.getText().toString());
                                if (minimumsellingprice <= Double.parseDouble(priceText.getText().toString())){
                                    insertProducts();
                                }else {
                                    showMinimumSellingpriceAlert(minimumSellingPriceText.getText().toString());
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Price should not be zero", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Enter the price", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        if (priceText.getText().toString() != null && !priceText.getText().toString().isEmpty()) {
                            if (Double.parseDouble(priceText.getText().toString()) > 0) {
                                double minimumsellingprice=Double.parseDouble(minimumSellingPriceText.getText().toString());
                                if (minimumsellingprice <= Double.parseDouble(priceText.getText().toString())){
                                    addProduct("Add");
                                }else {
                                    showMinimumSellingpriceAlert(minimumSellingPriceText.getText().toString());
                                }
                            } else {
                                if ((focEditText.getText() != null && !focEditText.getText().toString().isEmpty() && !focEditText.getText().toString().equals("0")) || (returnQtyText.getText() != null && !returnQtyText.getText().toString().isEmpty() && !returnQtyText.getText().toString().equals("0"))) {
                                    addProduct("Add");
                                } else {
                                    Toast.makeText(getApplicationContext(), "Price should not be zero", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Enter the price", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    if ((focEditText.getText() != null && !focEditText.getText().toString().isEmpty() && !focEditText.getText().toString().equals("0")) || (returnQtyText.getText() != null && !returnQtyText.getText().toString().isEmpty() && !returnQtyText.getText().toString().equals("0"))) {
                        addProduct("Add");
                    } else {
                        Toast.makeText(getApplicationContext(), "Price should not be zero", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        returnAdj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLayoutView.setVisibility(View.VISIBLE);
            }
        });

        cancelReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLayoutView.setVisibility(View.GONE);
            }
        });

        saveReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                expiryReturnQty.clearFocus();
                damageReturnQty.clearFocus();
                returnLayoutView.setVisibility(View.GONE);
            }
        });

        invoiceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate(invoiceDate);
            }
        });
        getProducts();


        // Setting the sorting
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View menuItemView = findViewById(R.id.fab);
                PopupMenu popupMenu = new PopupMenu(NewSalesReturnProductAddActivity.this, menuItemView);
                popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_all:
                                setProductsDisplay("All Products");
                                return true;
                            case R.id.action_stock:
                                setProductsDisplay("In Stock");
                                return true;
                            case R.id.action_outstock:
                                setProductsDisplay("Out of Stock");
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
    }


    public void setProductsDisplay(String action){
        try {
            // productsAdapterNew.setWithFooter(false);
            // sortAdapter.resetPosition();
            ArrayList<ProductsModel> filterdNames = new ArrayList<>();
            //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
            for (ProductsModel s : AppUtils.getProductsList()) {
                if (action.equals("In Stock")){
                    if (s.getStockQty()!=null && !s.getStockQty().equals("null")){
                        if (Double.parseDouble(s.getStockQty())>0){
                            filterdNames.add(s);
                        }
                    }
                }else if (action.equals("Out of Stock")){
                    if (s.getStockQty()!=null && !s.getStockQty().equals("null")) {
                        if (Double.parseDouble(s.getStockQty()) < 0 || Double.parseDouble(s.getStockQty()) == 0) {
                            filterdNames.add(s);
                        }
                    }
                }else {
                    filterdNames.add(s);
                }
            }

            // Setting filter option in Localdb
            if (action.equals("In Stock")){
                dbHelper.insertSettings("stock_view","1");
            }else if (action.equals("Out of Stock")){
                dbHelper.insertSettings("stock_view","0");
            }else {
                dbHelper.insertSettings("stock_view","2");
            }
            //calling a method of the adapter class and passing the filtered list
            if (filterdNames.size()>0){
               /* for (ProductsModel model:filterdNames){
                    Log.w("products_Option_values:",model.getProductName());
                }*/
                if (selectProductAdapter!=null){
                    selectProductAdapter.filterList(filterdNames);
                }
                selectProductAdapter.filterList(filterdNames);
                productLayout.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
                totalProducts.setText(filterdNames.size()+" Products");
            }else {
                productLayout.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);
                totalProducts.setText(filterdNames.size()+" Products");
            }
        }catch (Exception ex){
            Log.e("Error_in_filter", Objects.requireNonNull(ex.getMessage()));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String convertDate(String strDate) {
        @SuppressLint("SimpleDateFormat")
        DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
        @SuppressLint("SimpleDateFormat")
        DateFormat outputFormat = new SimpleDateFormat("yyyyMMdd");
        String resultDate = "";
        try {
            resultDate=outputFormat.format(inputFormat.parse(strDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultDate;
    }

    public void getDate(TextView dateEditext){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(NewSalesReturnProductAddActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateEditext.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        currentDate=convertDate(dateEditext.getText().toString());
                        Log.w("CurrentDateView:",currentDate);
                    }
                }, mYear, mMonth, mDay);
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public void setProductResult(String productId){
        try {
            getLowStockSetting();
            for (ProductsModel model:AppUtils.getProductsList()) {
                Log.w("Product_Code:", model.getProductCode());
                if (model.getProductCode().equals(productId.trim())) {
                    if (model.getStockQty() != null && !model.getStockQty().equals("null")) {
                        if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0) {
                            stockQtyValue.setText(model.getStockQty());
                            stockQtyValue.setTextColor(Color.parseColor("#D24848"));
                        } else if (Double.parseDouble(model.getStockQty()) > 0) {
                            stockQtyValue.setTextColor(Color.parseColor("#2ECC71"));
                            stockQtyValue.setText(model.getStockQty());
                        }
                    }
                    // stockLayout.setVisibility(View.VISIBLE);

                    if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0) {
                        if (isAllowLowStock){
                            productAutoComplete.clearFocus();
                            cartonPrice.setText(model.getUnitCost());
                            priceText.setText(model.getUnitCost());
                            loosePrice.setText(model.getUnitCost());
                            uomText.setText(model.getUomCode());
                            stockCount.setText(model.getStockQty());
                            pcsPerCarton.setText(model.getPcsPerCarton());

                            qtyValue.setEnabled(true);
                            qtyValue.setText("");

                            qtyValue.requestFocus();
                            openKeyborard(qtyValue);
                            qtyValue.setSelection(qtyValue.getText().length());

                            focEditText.setEnabled(true);
                            exchangeEditext.setEnabled(true);
                            discountEditext.setEnabled(true);
                            returnEditext.setEnabled(true);

                        }else {
                            Toast.makeText(getApplicationContext(), "Low Stock Please check", Toast.LENGTH_SHORT).show();
                            productAutoComplete.clearFocus();
                            productAutoComplete.setText("");
                            qtyValue.setEnabled(false);
                        }
                    } else {
                        productAutoComplete.clearFocus();
                        cartonPrice.setText(model.getUnitCost());
                        priceText.setText(model.getUnitCost());
                        loosePrice.setText(model.getUnitCost());
                        uomText.setText(model.getUomCode());
                        stockCount.setText(model.getStockQty());
                        pcsPerCarton.setText(model.getPcsPerCarton());

                        qtyValue.setEnabled(true);
                        priceText.setEnabled(true);
                        qtyValue.setText("");
                        qtyValue.requestFocus();
                        openKeyborard(qtyValue);
                        qtyValue.setSelection(qtyValue.getText().length());
                        focEditText.setEnabled(true);
                        exchangeEditext.setEnabled(true);
                        discountEditext.setEnabled(true);
                        returnEditext.setEnabled(true);
                    }
                } else {
                    Log.w("Not_found", "Product");
                }
            }
        }catch (Exception ex){
        }

    }

    public void showAlert(){
        new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Warning !")
                .setContentText("Net total should not be zero or Atleast add one Qty")
                .setConfirmText("Cancel")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                }).show();
    }

    public void showAlertForDeleteProduct(){
        new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Warning !")
                .setContentText("Products Will be cleared are you sure want to back?")
                .setConfirmText("Cancel")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        dbHelper.removeAllInvoiceItems();
                        sDialog.dismissWithAnimation();
                    }
                }).show();
    }

    public void showMinimumSellingpriceAlert(String sellingPrice){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getApplicationContext());
        builder1.setTitle("Warning !");
        builder1.setMessage(productName+"-"+"Minimum Selling Price is : $ "+ Utils.twoDecimalPoint(Double.parseDouble(sellingPrice)));
        builder1.setCancelable(false);
        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void addProduct(String action){
        if (isProductExist(productId)){
            Log.w("ProductIdView:",productId);
            if (!qtyValue.getText().toString().isEmpty() && Double.parseDouble(qtyValue.getText().toString()) > 0){
                showExistingProductAlert(productId,productName);
            }else {
                insertProducts();
            }
        }else {
            insertProducts();
        }
    }

    public void showExistingProductAlert(String productId,String productName){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getApplicationContext());
        builder1.setTitle("Warning !");
        builder1.setMessage(productName+" - "+productId+ "\nAlready Exist Do you want to replace ? ");
        builder1.setCancelable(false);
        builder1.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                insertProducts();
            }
        });
        builder1.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                clearFields();
            }
        });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void insertProducts(){
        try {
            String focType="pcs";
            String exchangeType="pcs";
            String returnType="pcs";
            String discount="0";
            String return_qty="0";
            String lPriceCalc="0";
            String foc="0";
            String price_value ="0";

            if (focSwitch.isChecked()){
                focType="ctn";
            }
            if (exchangeSwitch.isChecked()){
                exchangeType="ctn";
            }
            if (returnSwitch.isChecked()){
                returnType="ctn";
            }

            String ctn_qty="0";
            String qty_value ="0";

            if (!qtyValue.getText().toString().isEmpty()){
                qty_value =qtyValue.getText().toString();
            }

            if (!discountEditext.getText().toString().isEmpty()){
                discount=discountEditext.getText().toString();
            }
            if (!returnQtyText.getText().toString().isEmpty()){
                return_qty=returnQtyText.getText().toString();
            }


            if (!priceText.getText().toString().isEmpty()){
                price_value =priceText.getText().toString();
            }

            if (!focEditText.getText().toString().isEmpty()){
                foc=focEditText.getText().toString();
            }


            double priceValue=0.0;
            double net_qty=Double.parseDouble(qty_value) - Double.parseDouble(return_qty);

            double return_amt=(Double.parseDouble(return_qty)*Double.parseDouble(price_value));
            double total=(net_qty * Double.parseDouble(price_value));
            double sub_total=total-return_amt-Double.parseDouble(discount);


            boolean insertStatus=
                    dbHelper.insertCreateInvoiceCart(
                            productId.toString().trim(),
                            productName,
                            uomText.getText().toString(),
                            "",
                            qty_value,
                            return_qty,
                            String.valueOf(net_qty),
                            foc,
                            price_value,
                            stockQtyValue.getText().toString(),
                            String.valueOf(total),
                            subTotalValue.getText().toString(),
                            taxValueText.getText().toString(),
                            netTotalValue.getText().toString()
                    );

            // Adding Return Qty Table values
            if (Integer.parseInt(qty_value) > 0){
                dbHelper.updateReturnQty("Delete","0","Saleable Return",productId);
                dbHelper.updateReturnQty("Delete","0","Damaged/Expired",productId);
                dbHelper.insertReturnProduct(productId,productName,expiryReturnQty.getText().toString(),"Saleable Return");
                dbHelper.insertReturnProduct(productId,productName,damageReturnQty.getText().toString(),"Damaged/Expired");
            }

            if (insertStatus){
                subTotalValue.setText("0.0");
                taxValueText.setText("0.00");
                netTotalValue.setText("0.0");
                productAutoComplete.setText("");
                priceText.setText("0.00");
                qtyValue.setText("");
                uomText.setText("");
                stockCount.setText("");
                stockQtyValue.setText("");
                qtyValue.clearFocus();
                productAutoComplete.clearFocus();
                focEditText.setText("");
                exchangeEditext.setText("");
                discountEditext.setText("");
                returnQtyText.setText("");
                focSwitch.setChecked(false);
                exchangeSwitch.setChecked(false);
                returnSwitch.setChecked(false);
                stockLayout.setVisibility(View.GONE);
                 priceText.setEnabled(false);
                 qtyValue.setEnabled(false);
                focEditText.setEnabled(false);
                exchangeEditext.setEnabled(false);
                discountEditext.setEnabled(false);
                hideKeyboard();
                getProducts();
            }else {
                Toast.makeText(getApplicationContext(),"Error in Add product",Toast.LENGTH_LONG).show();
            }
        }catch (Exception ec){
            Log.e("Error_InsertProduct:",ec.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    private void getProducts() {
        ArrayList<CreateInvoiceModel> products=dbHelper.getAllInvoiceProducts();
        if (products.size()>0){
            itemCount.setText("Products ( "+products.size()+" )");
            productSummaryView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
            productSummaryAdapter=new NewSalesReturnProductAdapter(this,products, new NewSalesReturnProductAdapter.CallBack() {
                @Override
                public void searchCustomer(String letter, int pos) {
                }
                @Override
                public void removeItem(String pid) {
                    showRemoveItemAlert(pid);
                }
                @Override
                public void editItem(CreateInvoiceModel model) {
                    salesReturn.setChecked(false);
                    salesReturn.setEnabled(false);
                    productId=model.getProductCode();
                    productName=model.getProductName();
                    qtyValue.setText("");

                    double netqty=Double.parseDouble(model.getNetQty());

                  /*  if (model.getMinimumSellingPrice()!=null && !model.getMinimumSellingPrice().isEmpty()){
                        minimumSellingPriceText.setText(model.getMinimumSellingPrice());
                    }else {
                        minimumSellingPriceText.setText("0.00");
                    }*/

                    qtyValue.removeTextChangedListener(qtyTW);
                    qtyValue.setText(Utils.getQtyValue(String.valueOf(netqty)));
                    qtyValue.addTextChangedListener(qtyTW);

                    productAutoComplete.setText(model.getProductName()+"-"+model.getProductCode());

                    priceText.setText(model.getPrice());
                    addProduct.setText("Update");
                    qtyValue.requestFocus();
                    qtyValue.setSelectAllOnFocus(true);
                    qtyValue.setSelection(qtyValue.getText().length());
                    qtyValue.setEnabled(true);
                    priceText.setEnabled(true);

                    if (model.getFocQty()!=null && !model.getFocQty().isEmpty() && !model.getFocQty().equals("null")){
                        focEditText.setText(model.getFocQty());
                    }else {
                        focEditText.setText("0");
                    }

                    if (model.getReturnQty()!=null && !model.getReturnQty().isEmpty() && !model.getReturnQty().equals("null")){
                        returnQtyText.setText(model.getReturnQty());
                    }else {
                        returnQtyText.setText("0");
                    }

                    focEditText.setEnabled(true);
                    returnQtyText.setEnabled(true);

                    stockLayout.setVisibility(View.VISIBLE);
                    stockQtyValue.setTextColor(Color.parseColor("#2ECC71"));
                    stockQtyValue.setText(model.getStockQty());
                    stockCount.setText(model.getStockQty());

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
            });
            productSummaryView.setAdapter(productSummaryAdapter);
            noproductText.setVisibility(View.GONE);
            productSummaryView.setVisibility(View.VISIBLE);
            itemCount.setVisibility(View.VISIBLE);
        }else {
            itemCount.setVisibility(View.GONE);
            noproductText.setVisibility(View.VISIBLE);
            productSummaryView.setVisibility(View.GONE);
        }
        Utils.refreshActionBarMenu(this);
        hideKeyboard();
        setSummaryTotal();
    }

    public void showRemoveItemAlert(final String pid){

        try {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    // .setTitleText("Are you sure?")
                    .setContentText("Are you sure want to remove this item ?")
                    .setConfirmText("YES")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            dbHelper.deleteInvoiceProduct(pid);
                            clearFields();
                            sDialog.dismissWithAnimation();
                            getProducts();
                            setSummaryTotal();
                            Utils.refreshActionBarMenu(NewSalesReturnProductAddActivity.this);
                        }
                    })
                    .showCancelButton(true)
                    .setCancelText("No")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.cancel();
                        }
                    })
                    .show();
        }catch (Exception ex){}

    }

    public void hideKeyboard(){
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
        }
    }

    public void setSummaryTotal(){
        try {
            ArrayList<CreateInvoiceModel> localCart;
            localCart = dbHelper.getAllInvoiceProducts();
            double net_sub_total=0.0;
            double net_tax=0.0;
            double net_total=0.0;
            if (localCart.size()>0){
                for (CreateInvoiceModel model:localCart){
                    if (model.getSubTotal()!=null &&!model.getSubTotal().isEmpty()){
                        net_sub_total+=Double.parseDouble(model.getSubTotal());
                    }
                    if (model.getGstAmount()!=null && !model.getGstAmount().isEmpty()){
                        net_tax+=Double.parseDouble(model.getGstAmount());
                    }
                    if (model.getNetTotal()!=null && !model.getNetTotal().isEmpty()){
                        net_total+=Double.parseDouble(model.getNetTotal());
                    }
                }
                SharedPreferences sharedPreferences = getSharedPreferences("customerPref",MODE_PRIVATE);
                selectCustomerId = sharedPreferences.getString("customerId", "");
                customerDetails=dbHelper.getCustomer(selectCustomerId);
                String taxValue =customerDetails.get(0).getTaxPerc();
                String taxType=customerDetails.get(0).getTaxType();

                Log.w("GivenEditTax:",net_tax+"");
                Log.w("GivenNetTotalValues:",net_total+"");

                if (taxType.equals("I")){
                    setCalculationSummaryView(net_total);
                }else {
                    setCalculationSummaryView(net_sub_total);
                }

            }
        }catch (Exception ex){}
    }

    public void clearFields(){
        subTotalValue.setText("0.0");
        taxValueText.setText("0.00");
        netTotalValue.setText("0.0");
        productAutoComplete.setText("");
        priceText.setText("0.00");
        qtyValue.setText("");
        pcsPerCarton.setText("");
        uomText.setText("");
        stockCount.setText("");
        stockQtyValue.setText("");
        qtyValue.clearFocus();
        productAutoComplete.clearFocus();
        focEditText.setText("");
        returnQtyText.setText("");
        stockLayout.setVisibility(View.GONE);
        // priceText.setEnabled(false);
        getProducts();
        setSummaryTotal();
    }

    public boolean isProductExist(String productId) {
        boolean isExist=false;
        try {
            ArrayList<CartModel> localCart = dbHelper.getAllCartItems();
            if (localCart.size() > 0) {
                for (CartModel cart : localCart) {
                    if (cart.getCART_COLUMN_PID()!=null){
                        if (cart.getCART_COLUMN_PID().equals(productId)) {
                            Log.w("ProductIdPrint:",cart.getCART_COLUMN_PID());
                            isExist = true;
                            break;
                        }
                    }
                }
            }
        }catch (Exception ex){
            Log.e("Exp_to_check_product:", Objects.requireNonNull(ex.getMessage()));
        }
        return isExist;
    }

    public void checkLowStock(){
        double stock=0.0;
        double qty=0.0;
        if (!stockQtyValue.getText().toString().isEmpty()){
            if (!stockQtyValue.getText().toString().isEmpty()){
                stock=Double.parseDouble(stockQtyValue.getText().toString());
            }
            if (!qtyValue.getText().toString().isEmpty()){
                qty=Double.parseDouble(qtyValue.getText().toString());
            }
            if (stock < qty){
                Toast.makeText(getApplicationContext(),"Low Stock please check",Toast.LENGTH_SHORT).show();
                qtyValue.removeTextChangedListener(qtyTW);
                qtyValue.setText("");
                qtyValue.addTextChangedListener(qtyTW);
                setCalculationView();
            }
        }

    }

    /**
     * Get = the low stock invoice setting to allow the Negative stock allow the invoice
     */
    public void getLowStockSetting(){
        ArrayList<SettingsModel> settings=dbHelper.getSettings();
        if (settings.size()>0) {
            for (SettingsModel model : settings) {
                if (model.getSettingName().equals("allow_negative_switch")) {
                    isAllowLowStock= model.getSettingValue().equals("1");
                }
            }
        }else {
            isAllowLowStock=false;
        }
    }

    public void setCalculationSummaryView(double subTotal) {
        try {
            double taxAmount1 = 0.0, netTotal1 = 0.0;

            SharedPreferences sharedPreferences = getSharedPreferences("customerPref",MODE_PRIVATE);
            String selectCustomerId = sharedPreferences.getString("customerId", "");
            if (selectCustomerId != null && !selectCustomerId.isEmpty()) {
                customerDetails = dbHelper.getCustomer(selectCustomerId);
            }
            String taxValue=customerDetails.get(0).getTaxPerc();
            String taxType=customerDetails.get(0).getTaxType();
            Log.w("TaxType-Summary:",taxType);
            Log.w("TaxValue12-Summary:",taxValue);

            Log.w("SubTotalValues:",String.valueOf(subTotal));

            String Prodtotal = twoDecimalPoint(subTotal);

            if (!taxType.matches("") && !taxValue.matches("")) {
                double taxValueCalc = Double.parseDouble(taxValue);

                if (taxType.matches("E")) {

                    taxAmount1 = (subTotal * taxValueCalc) / 100;
                    String prodTax = twoDecimalPoint(taxAmount1);
                    taxValueText.setText("" + prodTax);

                    netTotal1 = subTotal + taxAmount1;
                    String ProdNetTotal = twoDecimalPoint(netTotal1);
                    netTotalValue.setText("" + ProdNetTotal);
                    taxTitle.setText("GST ( Exc )");
                    subTotalValue.setText(Utils.twoDecimalPoint(subTotal));

                } else if (taxType.matches("I")) {

                    taxAmount1 = (subTotal * taxValueCalc) / (100 + taxValueCalc);
                    String prodTax = twoDecimalPoint(taxAmount1);

                    taxValueText.setText("" + prodTax);
                    // netTotal1 = subTotal + taxAmount1;
                    netTotal1 = subTotal;
                    String ProdNetTotal = twoDecimalPoint(netTotal1);
                    netTotalValue.setText("" + ProdNetTotal);

                    double dTotalIncl = netTotal1 - taxAmount1;
                    String totalIncl = twoDecimalPoint(dTotalIncl);
                    Log.d("totalIncl", "" + totalIncl);

                    double sub_total=subTotal-taxAmount1;
                    taxTitle.setText("GST ( Inc )");
                    subTotalValue.setText(Utils.twoDecimalPoint(sub_total));


                } else if (taxType.matches("Z")) {

                    taxValueText.setText("0.0");
                    // netTotal1 = subTotal + taxAmount;
                    netTotal1 = subTotal;
                    String ProdNetTotal = twoDecimalPoint(netTotal1);
                    netTotalValue.setText("" + ProdNetTotal);
                    subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                    taxTitle.setText("GST ( Zero )");

                } else {
                    taxValueText.setText("0.0");
                    netTotalValue.setText("" + Prodtotal);
                    subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                    taxTitle.setText("GST ( Zero )");
                }

            } else if (taxValue.matches("")) {
                taxValueText.setText("0.0");
                netTotalValue.setText("" + Prodtotal);
                subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                taxTitle.setText("GST ( Zero )");
            } else {
                taxValueText.setText("0.0");
                netTotalValue.setText("" + Prodtotal);
                subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                taxTitle.setText("GST ( Zero )");
            }
        } catch (Exception e) {

        }
    }



    public void setCalculationView() {
        try {
            double taxAmount = 0.0, netTotal = 0.0;
            double taxAmount1 = 0.0, netTotal1 = 0.0;
            double return_qty=0;
            double pcspercarton=0.0;
            double cqtyCalc=0;
            double lqtyCalc=0;
            double net_qty=0.0;

            SharedPreferences sharedPreferences = getSharedPreferences("customerPref",MODE_PRIVATE);
            String selectCustomerId = sharedPreferences.getString("customerId", "");
            if (selectCustomerId != null && !selectCustomerId.isEmpty()) {
                customerDetails = dbHelper.getCustomer(selectCustomerId);
            }
            String taxValue=customerDetails.get(0).getTaxPerc();
            String taxType=customerDetails.get(0).getTaxType();
            Log.w("TaxType12:",taxType);
            Log.w("TaxValue12:",taxValue);

            String price = priceText.getText().toString();
            String qty = qtyValue.getText().toString();


            if (price.matches("")) {
                price = "0";
            }

            if (qty.matches("")) {
                qty = "0";
            }

            double cPriceCalc = Double.parseDouble(price);

            if (!returnQtyText.getText().toString().isEmpty() && !returnQtyText.getText().toString().equals("null")){
                return_qty=Double.parseDouble(returnQtyText.getText().toString());
                netReturnQty.setText(String.valueOf(return_qty));
            }else {
                netReturnQty.setText("0");
                expiryReturnQty.setText("");
                damageReturnQty.setText("");
            }

            net_qty=(Double.parseDouble(qty) - return_qty);

            double tt = net_qty * cPriceCalc;
            Log.w("TOTALVALUES:",String.valueOf(tt));

            String Prodtotal = twoDecimalPoint(tt);

            double subTotal = 0.0;

            String itemDisc = discountEditext.getText().toString();
            if (!itemDisc.matches("")) {
                double itmDisc = Double.parseDouble(itemDisc);
                subTotal = tt - itmDisc;
            } else {
                subTotal = tt;
            }

            String sbTtl = twoDecimalPoint(subTotal);


          /*  if (return_qty!=0){
                double return_amt=0.0;
                return_amt=(return_qty*cPriceCalc);
                subTotal=subTotal-return_amt;
            }*/

            // sl_total_inclusive.setText("" + sbTtl);
            tt=subTotal;
            Log.w("SubTotalValues:",String.valueOf(subTotal));

            if (!taxType.matches("") && !taxValue.matches("")) {
                double taxValueCalc = Double.parseDouble(taxValue);

                if (taxType.matches("E")) {

                    taxAmount1 = (subTotal * taxValueCalc) / 100;
                    String prodTax = twoDecimalPoint(taxAmount1);
                    taxValueText.setText("" + prodTax);

                    netTotal1 = subTotal + taxAmount1;
                    String ProdNetTotal = twoDecimalPoint(netTotal1);
                    netTotalValue.setText("" + ProdNetTotal);
                    taxTitle.setText("GST ( Exc )");
                    subTotalValue.setText(Utils.twoDecimalPoint(subTotal));

                } else if (taxType.matches("I")) {

                    taxAmount1 = (subTotal * taxValueCalc) / (100 + taxValueCalc);
                    String prodTax = twoDecimalPoint(taxAmount1);

                    taxValueText.setText("" + prodTax);
                    netTotal1 = subTotal;
                    String ProdNetTotal = twoDecimalPoint(netTotal1);
                    netTotalValue.setText("" + ProdNetTotal);

                    double dTotalIncl = netTotal1 - taxAmount1;
                    String totalIncl = twoDecimalPoint(dTotalIncl);
                    Log.d("totalIncl", "" + totalIncl);

                    double sub_total=subTotal-taxAmount1;
                    taxTitle.setText("GST ( Inc )");
                    subTotalValue.setText(Utils.twoDecimalPoint(sub_total));


                } else if (taxType.matches("Z")) {

                    taxValueText.setText("0.00");
                    netTotal1 = subTotal;
                    String ProdNetTotal = twoDecimalPoint(netTotal1);
                    netTotalValue.setText("" + ProdNetTotal);
                    subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                    taxTitle.setText("GST ( Zero )");

                } else {
                    taxValueText.setText("0.00");
                    netTotalValue.setText("" + Prodtotal);
                    subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                    taxTitle.setText("GST ( Zero )");
                }

            } else if (taxValue.matches("")) {
                taxValueText.setText("0.00");
                netTotalValue.setText("" + Prodtotal);
                subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                taxTitle.setText("GST ( Zero )");
            } else {
                taxValueText.setText("0.00");
                netTotalValue.setText("" + Prodtotal);
                subTotalValue.setText(Utils.twoDecimalPoint(subTotal));
                taxTitle.setText("GST ( Zero )");
            }
            setButtonView();
        } catch (Exception e) {
            Log.w("Error_Throwing::",e.getMessage());
        }
    }


    public void setButtonView(){
        if (Double.parseDouble(netTotalValue.getText().toString()) > 0){
            addProduct.setAlpha(0.9F);
            addProduct.setEnabled(true);
        }else {
            if ((!focEditText.getText().toString().isEmpty() && !focEditText.getText().toString().equals("0")) || (!returnQtyText.getText().toString().isEmpty() && !returnQtyText.getText().toString().equals("0"))){
                addProduct.setAlpha(0.9F);
                addProduct.setEnabled(true);
            }else {
                addProduct.setAlpha(0.4F);
                addProduct.setEnabled(false);
            }
        }
    }

    public void getAllProducts(JSONObject jsonObject){
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"CustomerProductList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_SAP_PROUCT_URL:",url);
        productList=new ArrayList<>();
        products=new ArrayList<>();
        progressDialog =new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Getting Customer Products List...");
        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try{
                        progressDialog.dismiss();
                        //  {"productCode":"FG\/001245","productName":"RUM","companyName":"","supplierCategoryNumber":"","uomCode":"Ctn",
                        //  "uomName":"Carton","cartonPrice":"3000.000000","piecePrice":"0.000000","pcsPerCarton":"100.000000",
                        //  "price":"100.000000","taxType":"E","havTax":"Y","taxCode":"SR","taxRate":"7.000000","barCode":"",
                        //  "isActive":"N","createUser":"1","createDate":"13\/07\/2021","modifyDate":"29\/07\/2021","remarks":"",
                        //  "warehouse":"01","stockInHand":"0.000000","averagePrice":0,"manageBatchOrSerial":"None","manageBatchNumber":"N",
                        //  "manageSerialNumber":"N","batchNumber":"","expiryDate":null,"manufactureDate":"31\/07\/2021","imageURL":""}
                        Log.w("Response_SAP_PRODUCTS:",response.toString());
                        // Loop through the array elements
                        JSONArray productArray=response.optJSONArray("responseData");
                        for(int i=0;i<productArray.length();i++){
                            // Get current json object
                            JSONObject productObject = productArray.getJSONObject(i);
                            ProductsModel product =new ProductsModel();
                            if (productObject.optString("isActive").equals("N")) {
                                product.setCompanyCode("1");
                                // Adding bp name for products
                                if (productObject.optString("bP_Description")!=null && !productObject.optString("bP_Description").isEmpty() && !productObject.optString("bP_Description").equals("null")){
                                    product.setProductName(productObject.optString("bP_Description"));
                                }else {
                                    product.setProductName(productObject.optString("productName"));
                                }
                                product.setProductCode(productObject.optString("productCode"));
                                product.setWeight("");
                                product.setProductImage(productObject.optString("imageURL"));
                                product.setWholeSalePrice("0.00");
                                product.setRetailPrice(productObject.optDouble("retailPrice"));
                                product.setCartonPrice(productObject.optString("cartonPrice"));
                                product.setPcsPerCarton(productObject.optString("pcsPerCarton"));
                               // product.setUnitCost(productObject.optString("price"));
                                product.setUnitCost(productObject.optString("lastSalesPrice"));
                                product.setMinimumSellingPrice(productObject.optString("minimumSellingPrice"));
                                if (!productObject.optString("stockInHand").equals("null")){
                                    product.setStockQty(productObject.optString("stockInHand"));
                                }else {
                                    product.setStockQty("0");
                                }

                                product.setUomCode(productObject.optString("uomCode"));
                                //  product.setProductBarcode(productObject.optString("BarCode")); Add values In Futue
                                product.setProductBarcode("");
                                productList.add(product);
                            }
                        }
                        HomePageModel.productsList=new ArrayList<>();
                        HomePageModel.productsList.addAll(productList);
                        setAdapter(productList);
                        HomePageModel.productsList.addAll(productList);
                        if (productList.size()>0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AppUtils.setProductsList(productList);
                                    progressDialog.dismiss();
                                    // dbHelper.insertProducts(getActivity(),productList);
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            Log.w("Error_throwing:",error.toString());
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                String creds = String.format("%s:%s", Constants.API_SECRET_CODE,Constants.API_SECRET_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    @SuppressLint("SetTextI18n")
    private void setAdapter(ArrayList<ProductsModel> productList) {
        // Get the Settings to Display the Product list with the Settings
        ArrayList<SettingsModel> settings=dbHelper.getSettings();
        if (settings.size()>0) {
            for (SettingsModel model : settings) {
                if (model.getSettingName().equals("stock_view")) {
                    if (model.getSettingValue().equals("1")){
                        stockProductView="1";
                    }else if (model.getSettingValue().equals("2")){
                        stockProductView="2";
                    }else {
                        stockProductView="0";
                    }
                }
            }
        }
        // Filter the products list depending the Settings
        ArrayList<ProductsModel> filteredProducts = new ArrayList<>();
        //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
        if (productList!=null && productList.size()>0){
            for (ProductsModel product : productList) {
                if (stockProductView.equals("1")){
                    if (product.getStockQty()!=null && !product.getStockQty().equals("null")){
                        if (Double.parseDouble(product.getStockQty())>0){
                            filteredProducts.add(product);
                        }
                    }
                }else if (stockProductView.equals("0")){
                    if (product.getStockQty()!=null && !product.getStockQty().equals("null")) {
                        if (Double.parseDouble(product.getStockQty()) < 0 || Double.parseDouble(product.getStockQty()) == 0) {
                            filteredProducts.add(product);
                        }
                    }
                }else {
                    filteredProducts.add(product);
                }
            }
        }

        if (filteredProducts.size()>0){
            productLayout.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }else {
            productLayout.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }

        productListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        selectProductAdapter=new SelectProductAdapter(this,filteredProducts, new SelectProductAdapter.CallBack() {
            @Override
            public void searchProduct(ProductsModel model) {
                // Need to implement the Product price Later
               /* try {
                    getProductPrice(model.getProductCode());
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                productAutoComplete.setText(model.getProductName()+" - "+model.getProductCode());
                productId=model.getProductCode();
                productName=model.getProductName();
                priceText.setText(Utils.twoDecimalPoint(Double.parseDouble(model.getUnitCost())));
                uomText.setText(model.getUomCode());
                stockCount.setText(model.getStockQty());
                // looseQtyValue.setEnabled(true);
                qtyValue.setText("");
                priceText.setEnabled(true);
                focEditText.setEnabled(true);
                exchangeEditext.setEnabled(true);
                discountEditext.setEnabled(true);
                returnQtyText.setEnabled(true);

                if (model.getMinimumSellingPrice()!=null && !model.getMinimumSellingPrice().isEmpty()){
                    minimumSellingPriceText.setText(model.getMinimumSellingPrice());
                }else {
                    minimumSellingPriceText.setText("0.00");
                }

                // stockLayout.setVisibility(View.VISIBLE);
                if (model.getStockQty()!=null && !model.getStockQty().equals("null")) {
                    if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0) {
                        stockQtyValue.setText(model.getStockQty());
                        stockQtyValue.setTextColor(Color.parseColor("#D24848"));
                    } else if (Double.parseDouble(model.getStockQty()) > 0) {
                        stockQtyValue.setTextColor(Color.parseColor("#2ECC71"));
                        stockQtyValue.setText(model.getStockQty());
                    }
                }
                qtyValue.requestFocus();
                openKeyborard(qtyValue);
                viewCloseBottomSheet();
            }
        });

        // products.add(productObject.optString("ProductName")+" - "+productObject.optString("ProductCode"));
        products=new ArrayList<>();
        if (filteredProducts!=null && filteredProducts.size()>0){
            for (ProductsModel product:filteredProducts){
                products.add(product.getProductName()+" ~ "+product.getProductCode());
            }
        }

        productListView.setAdapter(selectProductAdapter);
        autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, products){
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextSize(14);
            /*    Typeface Type = getFont () ;  // custom method to get a font from "assets" folder
                ((TextView) v).setTypeface(Type);
                ((TextView) v).setTextColor(YourColor);*/
                ((TextView) v) .setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
                return v;
            }
        };
        productAutoComplete.setThreshold(1);
        productAutoComplete.setAdapter(autoCompleteAdapter);
        totalProducts.setText(filteredProducts.size()+" Products");
        //dialog.dismiss();
    }

    public void createSalesOrderJson(int copy) {

        // JSONObject rootJsonObject = new JSONObject();
        JSONObject rootJsonObject = new JSONObject();
        JSONArray invoiceDetailsArray =new JSONArray();
        JSONObject invoiceObject =new JSONObject();

        //  {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"CUS\/686","customerName":"VH FACTORY","groupCode":"100",
        //  "contactPerson":"","creditLimit":"150.000000","currencyCode":"SGD","currencyName":"Singapore Dollar","taxType":"","taxCode":"SR",
        //  "taxName":"Sales Standard Rated Supplier SR","taxPercentage":"7.000000","balance":"21.600000","outstandingAmount":"128.400000",
        //  "address":"","street":"","city":"","state":"","zipCode":"","country":"","createDate":"13\/07\/2021","updateDate":"30\/07\/2021",
        //  "active":"N","remark":""}]}

        JSONArray detailsArray=customerResponse.optJSONArray("responseData");
        JSONObject object=detailsArray.optJSONObject(0);

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
            if (activityFrom.equals("SalesEdit")){
                rootJsonObject.put("soNumber", editSoNumber.toString());
                rootJsonObject.put("mode", "E");
                rootJsonObject.put("status", "O");
            }else {
                rootJsonObject.put("soNumber", "");
                rootJsonObject.put("mode", "I");
                rootJsonObject.put("status", "");
            }


            rootJsonObject.put("soDate", currentDate);
            rootJsonObject.put("currentDateTime",currentSaveDateTime);
            rootJsonObject.put("customerCode", object.get("customerCode"));
            rootJsonObject.put("customerName", object.get("customerName"));
            rootJsonObject.put("address", object.get("address"));
            rootJsonObject.put("street", object.get("street"));
            rootJsonObject.put("city", object.get("city"));
            rootJsonObject.put("creditLimit", object.get("creditLimit"));
            rootJsonObject.put("remark", remarkText.getText().toString());
            rootJsonObject.put("currencyName", "Singapore Dollar");

            rootJsonObject.put("taxTotal", taxValueText.getText().toString());
            rootJsonObject.put("subTotal", subTotalValue.getText().toString());
            rootJsonObject.put("total", subTotalValue.getText().toString());
            rootJsonObject.put("netTotal", netTotalValue.getText().toString());
            rootJsonObject.put("itemDiscount", "0.00");
            rootJsonObject.put("billDiscount", "0.00");


            rootJsonObject.put("totalDiscount","0");
            rootJsonObject.put("billDiscountPercentage", "0.00");
            rootJsonObject.put("deliveryCode", SettingUtils.getDeliveryAddressCode());
            rootJsonObject.put("delCustomerName", "");
            rootJsonObject.put("delAddress1", object.optString("delAddress1"));
            rootJsonObject.put("delAddress2 ", object.optString("delAddress2"));
            rootJsonObject.put("delAddress3 ", object.optString("delAddress3"));
            rootJsonObject.put("delPhoneNo", object.optString("contactNo"));
            rootJsonObject.put("remark", object.optString("remark"));
            rootJsonObject.put("haveTax", object.optString("haveTax"));
            rootJsonObject.put("taxType", object.optString("taxType"));
            rootJsonObject.put("taxPerc", object.optString("taxPercentage"));
            rootJsonObject.put("taxCode", object.optString("taxCode"));
            rootJsonObject.put("currencyCode", object.optString("currencyCode"));
            rootJsonObject.put("currencyValue","");
            rootJsonObject.put("CurrencyRate", "1");
            rootJsonObject.put("postalCode", object.optString("postalCode"));
            rootJsonObject.put("createUser",username);
            rootJsonObject.put("modifyUser",username);
            rootJsonObject.put("companyName",companyName);
            rootJsonObject.put("stockUpdated","1");
            rootJsonObject.put("invoiceType","M");
            rootJsonObject.put("companyCode",companyCode);
            rootJsonObject.put("locationCode",locationCode);
            rootJsonObject.put("signature",signatureString);
            rootJsonObject.put("latitude",current_latitude);
            rootJsonObject.put("longitude",current_longitude);

            // Sales Details Add to the Objects
            ArrayList<CreateInvoiceModel> localCart=dbHelper.getAllInvoiceProducts();
            int index=1;
            for (CreateInvoiceModel model:localCart){
                invoiceObject=new JSONObject();
             /*   if (activityFrom.equals("InvoiceEdit")){
                    rootJsonObject.put("invoiceNumber", AddInvoiceActivity.editInvoiceNumber);
                }else {
                    rootJsonObject.put("invoiceNumber", "");
                }*/
                invoiceObject.put("companyCode",companyCode);
                invoiceObject.put("invoiceDate", currentDate);
                invoiceObject.put("slNo",index);
                invoiceObject.put("productCode",model.getProductCode());
                invoiceObject.put("productName",model.getProductName());
                invoiceObject.put("qty",String.valueOf(model.getActualQty()));
                // convert into int
                invoiceObject.put("price",Utils.twoDecimalPoint(Double.parseDouble(model.getPrice())));
                invoiceObject.put("total",Utils.twoDecimalPoint(Double.parseDouble(model.getTotal())));
                invoiceObject.put("itemDiscount","0.00");
                invoiceObject.put("totalTax",Utils.twoDecimalPoint(Double.parseDouble(model.getGstAmount())));
                invoiceObject.put("subTotal",Utils.twoDecimalPoint(Double.parseDouble(model.getSubTotal())));
                invoiceObject.put("netTotal",Utils.twoDecimalPoint(Double.parseDouble(model.getNetTotal())));
                invoiceObject.put("taxType",object.optString("taxType"));
                invoiceObject.put("taxPerc",object.optString("taxPercentage"));

                double return_subtotal=0;
                if (model.getReturnQty()!=null && !model.getReturnQty().isEmpty()  && !model.getReturnQty().equals("null")){
                    return_subtotal=Double.parseDouble(model.getReturnQty()) * Double.parseDouble(model.getPrice());
                }

                assert model.getReturnQty() != null;
                if (!model.getReturnQty().isEmpty() && !model.getReturnQty().toString().equals("null")){
                    invoiceObject.put("returnLQty", model.getReturnQty());
                    invoiceObject.put("returnQty", model.getReturnQty());
                }else {
                    invoiceObject.put("returnLQty", "0");
                    invoiceObject.put("returnQty", "0");
                }

                if (!model.getFocQty().toString().isEmpty() &&!model.getFocQty().equals("null")){
                    invoiceObject.put("focQty", model.getFocQty());
                }else {
                    invoiceObject.put("focQty", "0");
                }


                invoiceObject.put("returnSubTotal",return_subtotal+"");
                invoiceObject.put("returnNetTotal", return_subtotal+"");
                invoiceObject.put("taxCode",object.optString("taxCode"));
                invoiceObject.put("uomCode",model.getUomCode());
                invoiceObject.put("itemRemarks","");
                invoiceObject.put("locationCode",locationCode);
                invoiceObject.put("createUser",username);
                invoiceObject.put("modifyUser",username);
                invoiceDetailsArray.put(invoiceObject);
                index++;
            }

            rootJsonObject.put("PostingSalesOrderDetails", invoiceDetailsArray);

            Log.w("RootJsonForSave:",rootJsonObject.toString());

            saveSalesOrder(rootJsonObject,"SalesOrder",copy);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.w("Given_Error:", Objects.requireNonNull(e.getMessage()));
        }
    }


    public void viewCloseBottomSheet(){
        // hideKeyboard();
        productNameEditext.setText("");
        selectProductAdapter.notifyDataSetChanged();
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        // Showing the Products Depending the Settings Values
        ArrayList<SettingsModel> settings=dbHelper.getSettings();
        if (settings.size()>0) {
            for (SettingsModel model : settings) {
                if (model.getSettingName().equals("stock_view")) {
                    if (model.getSettingValue().equals("1")){
                        stockProductView="1";
                    }else if (model.getSettingValue().equals("2")){
                        stockProductView="2";
                    }else {
                        stockProductView="0";
                    }
                }
            }
        }
        // Filter the products list depending the Settings
        ArrayList<ProductsModel> filteredProducts = new ArrayList<>();
        //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
        if (AppUtils.getProductsList()!=null && AppUtils.getProductsList().size()>0){
            for (ProductsModel product : AppUtils.getProductsList()) {
                if (stockProductView.equals("1")){
                    if (product.getStockQty()!=null && !product.getStockQty().equals("null")){
                        if (Double.parseDouble(product.getStockQty())>0){
                            filteredProducts.add(product);
                        }
                    }
                }else if (stockProductView.equals("0")){
                    if (product.getStockQty()!=null && !product.getStockQty().equals("null")) {
                        if (Double.parseDouble(product.getStockQty()) < 0 || Double.parseDouble(product.getStockQty()) == 0) {
                            filteredProducts.add(product);
                        }
                    }
                }else {
                    filteredProducts.add(product);
                }
            }
        }


        if (filteredProducts.size()>0){
            productLayout.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }else {
            productLayout.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }

        productListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        selectProductAdapter=new SelectProductAdapter(this,filteredProducts, new SelectProductAdapter.CallBack() {
            @Override
            public void searchProduct(ProductsModel model) {
                productsModel=model;
                productId=productsModel.getProductCode();

                // Need to implement the product price concept in SAP
              /*  try {
                    getProductPrice(productId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

                if (model.getMinimumSellingPrice()!=null && !model.getMinimumSellingPrice().isEmpty()){
                    minimumSellingPriceText.setText(model.getMinimumSellingPrice());
                }else {
                    minimumSellingPriceText.setText("0.00");
                }
                productName=productsModel.getProductName();
                productAutoComplete.setText(model.getProductName()+" - "+model.getProductCode());
                //  cartonPrice.setText(model.getUnitCost()+"");
                //  loosePrice.setText(model.getUnitCost());
                priceText.setText(model.getUnitCost());
                priceText.setEnabled(true);
                uomText.setText(model.getUomCode());
                stockCount.setText(model.getStockQty());
                pcsPerCarton.setText(model.getPcsPerCarton());
                qtyValue.setEnabled(true);
                qtyValue.requestFocus();
                //  behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                openKeyborard(qtyValue);

                // looseQtyValue.setEnabled(true);
                cartonPrice.setEnabled(true);
                focEditText.setEnabled(true);
                exchangeEditext.setEnabled(true);
                discountEditext.setEnabled(true);
                returnEditext.setEnabled(true);

                //stockLayout.setVisibility(View.VISIBLE);
                if (model.getStockQty()!=null && !model.getStockQty().equals("null")) {
                    if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0) {
                        stockQtyValue.setText(model.getStockQty());
                        stockQtyValue.setTextColor(Color.parseColor("#D24848"));
                    } else if (Double.parseDouble(model.getStockQty()) > 0) {
                        stockQtyValue.setTextColor(Color.parseColor("#2ECC71"));
                        stockQtyValue.setText(model.getStockQty());
                    }
                }
                viewCloseBottomSheet();
            }
        });
        productListView.setAdapter(selectProductAdapter);
        totalProducts.setText(filteredProducts.size()+" Products");
        // get the Customer name from the local db
    }

    public void openKeyborard(EditText editText){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void filter(String text) {
        try {
            //new array list that will hold the filtered data
            ArrayList<ProductsModel> filterProducts = new ArrayList<>();
            //looping through existing elements
            //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
            for (ProductsModel s : AppUtils.getProductsList()) {
                //if the existing elements contains the search input
                if (s.getProductName().toLowerCase().contains(text.toLowerCase()) || s.getProductCode().toLowerCase().contains(text.toLowerCase())) {
                    //adding the element to filtered list
                    filterProducts.add(s);
                }
            }
            //calling a method of the adapter class and passing the filtered list
            selectProductAdapter.filterList(filterProducts);

            //setAdapter(filterProducts);
            totalProducts.setText(filterProducts.size()+" Products");

        }catch (Exception ex){
            Log.e("Error_in_filter", Objects.requireNonNull(ex.getMessage()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
             /*   Intent intent = new Intent(CreateNewInvoiceActivity.this, CustomerListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
                finish();
                return true;

            case R.id.action_save:
                ArrayList<CreateInvoiceModel> localCart=dbHelper.getAllInvoiceProducts();
                if (localCart.size()>0){
                    showSaveAlert();
                }else {
                    Toast.makeText(getApplicationContext(),"Add the Product First...!",Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        //  return super.onOptionsItemSelected(item);
    }

    public void createInvoiceJson(int copy) {

        // JSONObject rootJsonObject = new JSONObject();
        JSONObject rootJsonObject = new JSONObject();
        JSONArray invoiceDetailsArray =new JSONArray();
        JSONArray returnProductArray=new JSONArray();
        JSONObject returnProductObject=new JSONObject();
        JSONObject invoiceObject =new JSONObject();

        //  {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"CUS\/686","customerName":"VH FACTORY","groupCode":"100",
        //  "contactPerson":"","creditLimit":"150.000000","currencyCode":"SGD","currencyName":"Singapore Dollar","taxType":"","taxCode":"SR",
        //  "taxName":"Sales Standard Rated Supplier SR","taxPercentage":"7.000000","balance":"21.600000","outstandingAmount":"128.400000",
        //  "address":"","street":"","city":"","state":"","zipCode":"","country":"","createDate":"13\/07\/2021","updateDate":"30\/07\/2021",
        //  "active":"N","remark":""}]}

        JSONArray detailsArray=customerResponse.optJSONArray("responseData");
        JSONObject object=detailsArray.optJSONObject(0);

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
            if (activityFrom.equals("ConvertInvoice")){
                rootJsonObject.put("mode", "I");
                rootJsonObject.put("soNo",editSoNumber);
                rootJsonObject.put("invoiceNumber", "");
            }else {
                rootJsonObject.put("invoiceNumber", "");
                rootJsonObject.put("mode", "I");
            }
            rootJsonObject.put("currentDateTime",currentSaveDateTime);
            rootJsonObject.put("invoiceDate", currentDate);
            rootJsonObject.put("customerCode", object.get("customerCode"));
            rootJsonObject.put("customerName", object.get("customerName"));
            rootJsonObject.put("address", object.get("address"));
            rootJsonObject.put("street", object.get("street"));
            rootJsonObject.put("city", object.get("city"));
            rootJsonObject.put("creditLimit", object.get("creditLimit"));
            rootJsonObject.put("remark", remarkText.getText().toString());
            rootJsonObject.put("currencyName", "Singapore Dollar");

            rootJsonObject.put("taxTotal", taxValueText.getText().toString());
            rootJsonObject.put("subTotal", subTotalValue.getText().toString());
            rootJsonObject.put("total", subTotalValue.getText().toString());
            rootJsonObject.put("netTotal", netTotalValue.getText().toString());
            rootJsonObject.put("itemDiscount", "0.00");
            rootJsonObject.put("billDiscount", "0.00");
            rootJsonObject.put("Paymode","");
            rootJsonObject.put("ChequeDateString","");
            rootJsonObject.put("BankCode","");
            rootJsonObject.put("AccountNo","");
            rootJsonObject.put("ChequeNo","");

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
            }*/

            rootJsonObject.put("totalDiscount","0");
            rootJsonObject.put("billDiscountPercentage", "0.00");
            rootJsonObject.put("deliveryCode", SettingUtils.getDeliveryAddressCode());
            rootJsonObject.put("delCustomerName", "");
            rootJsonObject.put("delAddress1", object.optString("delAddress1"));
            rootJsonObject.put("delAddress2 ", object.optString("delAddress2"));
            rootJsonObject.put("delAddress3 ", object.optString("delAddress3"));
            rootJsonObject.put("delPhoneNo", object.optString("contactNo"));
            rootJsonObject.put("remark", object.optString("remark"));
            rootJsonObject.put("haveTax", object.optString("haveTax"));
            rootJsonObject.put("taxType", object.optString("taxType"));
            rootJsonObject.put("taxPerc", object.optString("taxPercentage"));
            rootJsonObject.put("taxCode", object.optString("taxCode"));
            rootJsonObject.put("currencyCode", object.optString("currencyCode"));
            rootJsonObject.put("currencyValue","");
            rootJsonObject.put("CurrencyRate", "1");
            rootJsonObject.put("status", "0");
            rootJsonObject.put("postalCode", object.optString("postalCode"));
            rootJsonObject.put("createUser",username);
            rootJsonObject.put("modifyUser",username);
            rootJsonObject.put("companyName",companyName);
            rootJsonObject.put("stockUpdated","1");
            rootJsonObject.put("invoiceType","M");
            rootJsonObject.put("companyCode",companyCode);
            rootJsonObject.put("locationCode",locationCode);
            rootJsonObject.put("signature",signatureString);
            rootJsonObject.put("latitude",current_latitude);
            rootJsonObject.put("longitude",current_longitude);

            // Sales Details Add to the Objects
            ArrayList<CreateInvoiceModel> localCart=dbHelper.getAllInvoiceProducts();
            int index=1;
            for (CreateInvoiceModel model:localCart){
                invoiceObject=new JSONObject();
             /*   if (activityFrom.equals("InvoiceEdit")){
                    rootJsonObject.put("invoiceNumber", AddInvoiceActivity.editInvoiceNumber);
                }else {
                    rootJsonObject.put("invoiceNumber", "");
                }*/
                invoiceObject.put("invoiceNumber", "");
                invoiceObject.put("companyCode",companyCode);
                invoiceObject.put("invoiceDate", currentDate);
                invoiceObject.put("slNo",index);
                invoiceObject.put("productCode",model.getProductCode());
                invoiceObject.put("productName",model.getProductName());
                // convert into int
                invoiceObject.put("price",Utils.twoDecimalPoint(Double.parseDouble(model.getPrice())));
                invoiceObject.put("total",Utils.twoDecimalPoint(Double.parseDouble(model.getTotal())));
                invoiceObject.put("itemDiscount","0.00");
                invoiceObject.put("totalTax",Utils.twoDecimalPoint(Double.parseDouble(model.getGstAmount())));
                invoiceObject.put("subTotal",Utils.twoDecimalPoint(Double.parseDouble(model.getSubTotal())));
                invoiceObject.put("netTotal",Utils.twoDecimalPoint(Double.parseDouble(model.getNetTotal())));
                invoiceObject.put("taxType",object.optString("taxType"));
                invoiceObject.put("taxPerc",object.optString("taxPercentage"));

                double return_subtotal=0;
                if (model.getReturnQty()!=null && !model.getReturnQty().isEmpty()  && !model.getReturnQty().equals("null")){
                    return_subtotal=Double.parseDouble(model.getReturnQty()) * Double.parseDouble(model.getPrice());
                }

                assert model.getReturnQty() != null;
                if (!model.getReturnQty().isEmpty() && !model.getReturnQty().toString().equals("null")){
                    invoiceObject.put("returnLQty", model.getReturnQty());
                    invoiceObject.put("returnQty", model.getReturnQty());
                    invoiceObject.put("qty",String.valueOf(model.getActualQty()));
                }else {
                    invoiceObject.put("returnLQty", "0");
                    invoiceObject.put("returnQty", "0");
                    invoiceObject.put("qty",String.valueOf(model.getActualQty()));
                }

                if (!model.getFocQty().toString().isEmpty() &&!model.getFocQty().equals("null")){
                    invoiceObject.put("focQty", model.getFocQty());
                }else {
                    invoiceObject.put("focQty", "0");
                }


                invoiceObject.put("returnSubTotal",Utils.twoDecimalPoint(return_subtotal));
                invoiceObject.put("returnNetTotal", Utils.twoDecimalPoint(return_subtotal));
                invoiceObject.put("taxCode",object.optString("taxCode"));
                invoiceObject.put("returnReason","");
                invoiceObject.put("uomCode",model.getUomCode());
                invoiceObject.put("itemRemarks","");
                invoiceObject.put("locationCode",locationCode);
                invoiceObject.put("createUser",username);
                invoiceObject.put("modifyUser",username);


                ArrayList<ReturnProductsModel> returnProducts=dbHelper.getReturnProducts(model.getProductCode());
                returnProductArray=new JSONArray();
                if (returnProducts.size() > 0){
                    for (ReturnProductsModel returnProductsModel:returnProducts){
                        Log.w("ReturnProductsValues:",returnProductsModel.getProductCode()+"-"+returnProductsModel.getProductName()+"--"+returnProductsModel.getReturnQty());
                        if (Integer.parseInt(returnProductsModel.getReturnQty()) > 0){
                            returnProductObject=new JSONObject();
                            returnProductObject.put("ReturnReason",returnProductsModel.getReturnReason());
                            returnProductObject.put("ReturnQty",returnProductsModel.getReturnQty());
                            returnProductArray.put(returnProductObject);
                        }
                    }
                }
                invoiceObject.put("ReturnDetails",returnProductArray);
                invoiceDetailsArray.put(invoiceObject);
                index++;
            }

            rootJsonObject.put("PostingInvoiceDetails", invoiceDetailsArray);

            Log.w("RootJsonForSave:",rootJsonObject.toString());

            saveSalesOrder(rootJsonObject,"Invoice",copy);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.w("Given_Error:", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void showSaveAlert(){
        try {
            // create an alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // set the custom layout
            builder.setCancelable(false);
            final View customLayout = getLayoutInflater().inflate(R.layout.invoice_save_option, null);
            builder.setView(customLayout);
            // add a button
            okButton=customLayout.findViewById(R.id.btn_ok);
            cancelButton=customLayout.findViewById(R.id.btn_cancel);
            invoicePrintCheck=customLayout.findViewById(R.id.invoice_print_check);
            saveMessage=customLayout.findViewById(R.id.save_message);
            saveTitle=customLayout.findViewById(R.id.save_title);
            //invoicePrintCheck.setVisibility(View.GONE);
            if (activityFrom.equals("sales_return")){
                saveTitle.setText("Save Sales Return");
                saveMessage.setText("Are you sure want to save Sales Return?");
                invoicePrintCheck.setText("Sales Return Print");
                invoicePrintCheck.setChecked(false);
                isPrintEnable=false;
            }else {
                invoicePrintCheck.setChecked(true);
                isPrintEnable=true;
            }

            invoicePrintCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (invoicePrintCheck.isChecked()){
                        isPrintEnable=true;
                    }else {
                        isPrintEnable=false;
                    }
                }
            });
            okButton.setOnClickListener(view1 -> {
                try {
                    createSalesReturnJson();
                    alert.dismiss();
                }catch (Exception exception){}
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                }
            });
            // create and show the alert dialog
            alert = builder.create();
            alert.show();
        }catch (Exception exception){}
    }


    public  void saveSalesOrder(JSONObject jsonBody,String action,int copy){
        try {
            pDialog = new SweetAlertDialog(NewSalesReturnProductAddActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            if (action.equals("SalesOrder")){
                pDialog.setTitleText("Saving Sales Order...");
            }else if (action.equals("DeliveryOrder")){
                pDialog.setTitleText("Saving Delivery Order...");
            } else {
                pDialog.setTitleText("Saving Invoice...");
            }
            pDialog.setCancelable(false);
            pDialog.show();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            Log.w("GivenInvoiceRequest:",jsonBody.toString());
            String URL="";
            if (action.equals("SalesOrder")){
                URL=Utils.getBaseUrl(this)+"PostingSalesOrder";
            }else if (action.equals("DeliveryOrder")){
                URL=Utils.getBaseUrl(this)+"PostingDeliveryOrder";
            } else {
                URL=Utils.getBaseUrl(this)+"PostingInvoice";
            }
            Log.w("Given_InvoiceApi:",URL);
            //    {"statusCode":2,"statusMessage":"Failed","responseData":{"docNum":null,"error":"Invoice :One of the base documents has already been closed  [INV1.BaseEntry][line: 1]"}}
            JsonObjectRequest salesOrderRequest = new JsonObjectRequest(Request.Method.POST, URL,jsonBody, response -> {
                Log.w("Invoice_ResponseSap:",response.toString());
                Utils.clearCustomerSession(this);
                AppUtils.setProductsList(null);
                dbHelper.removeAllReturn();
                // dbHelper.removeCustomer();
                // {"statusCode":1,"statusMessage":"Invoice Created Successfully","responseData":{"docNum":"35","error":null}}
                pDialog.dismiss();
                String statusCode=response.optString("statusCode");
                String message=response.optString("statusMessage");
                JSONObject responseData = null;
                try {
                    responseData=response.getJSONObject("responseData");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (statusCode.equals("1")){
                    if (action.equals("SalesOrder") || action.equals("SalesEdit")){
                        if (isPrintEnable){
                            try {
                                dbHelper.removeAllInvoiceItems();
                                JSONObject object=response.optJSONObject("responseData");
                                String doucmentNo =object.optString("docNum");
                                //   String result=object.optString("Result");
                                if (!doucmentNo.isEmpty()) {
                                    // getSalesOrderDetails(doucmentNo, copy);
                                    Intent intent=new Intent(this, SalesOrderListActivity.class);
                                    intent.putExtra("printSoNumber",doucmentNo);
                                    intent.putExtra("noOfCopy",String.valueOf(copy));
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Toast.makeText(getApplicationContext(),"Error in getting printing data",Toast.LENGTH_SHORT).show();
                                    redirectActivity();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else {
                            dbHelper.removeAllInvoiceItems();
                            redirectActivity();
                        }
                        isPrintEnable=false;
                    }/*else if (action.equals("DeliveryOrder") || action.equals("DeliveryOrderEdit")){
                        if (isPrintEnable){
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
                        }
                        isPrintEnable=false;
                    }*/ else {
                        if (isPrintEnable){
                            try {
                                //updateStockQty();
                                dbHelper.removeAllInvoiceItems();
                                JSONObject object=response.optJSONObject("responseData");
                                String doucmentNo =object.optString("docNum");
                                //   String result=object.optString("Result");
                                if (!doucmentNo.isEmpty()) {
                                    getInvoicePrintDetails(doucmentNo,copy);
                                    Intent intent=new Intent(getApplicationContext(), NewInvoiceListActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Toast.makeText(getApplicationContext(),"Error in getting printing data",Toast.LENGTH_SHORT).show();
                                    redirectActivity();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else {
                            dbHelper.removeAllInvoiceItems();
                            redirectActivity();
                        }
                        isPrintEnable=false;
                    }
                }else {
//                    Log.w("ErrorValues:",responseData.optString("error"));
                    if (responseData!=null){
                        Toast.makeText(getApplicationContext(),responseData.optString("error"),Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(),"Error in Saving Data...",Toast.LENGTH_SHORT).show();
                    }
                }
            }, error -> {
                Log.w("SalesOrder_Response:",error.toString());
                pDialog.dismiss();
            }) {
                /* @Override
                 public byte[] getBody() {
                     return jsonBody.toString().getBytes();
                 }*/
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> params = new HashMap<>();
                    String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                    String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                    params.put("Authorization", auth);
                    return params;
                }
            };
            salesOrderRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }
                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }
                @Override
                public void retry(VolleyError error) throws VolleyError {
                }
            });
            requestQueue.add(salesOrderRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void getCustomerDetails(String customerCode,boolean isloader,String from) {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        // Initialize a new JsonArrayRequest instance
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("CustomerCode",customerCode);
            // jsonObject.put("CompanyCode",companyCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.w("JsonValueForCustomer:",jsonObject.toString());
        String url= Utils.getBaseUrl(getApplicationContext()) +"Customer";
        Log.w("Given_url:",url);
        ProgressDialog progressDialog=new ProgressDialog(getApplicationContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Customer Details Loading...");
        if (isloader){
            progressDialog.show();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    try{
                        progressDialog.dismiss();
                        Log.w("SAP-response_customer:",response.toString());
                        ArrayList<CustomerModel> customerList=new ArrayList<>();
                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            customerResponse=response;
                            JSONArray customerDetailArray=response.optJSONArray("responseData");
                            for (int i=0;i<customerDetailArray.length();i++){
                                JSONObject object=customerDetailArray.optJSONObject(i);
                                //  if (customerObject.optBoolean("IsActive")) {
                                CustomerModel model = new CustomerModel();
                                model.setCustomerCode(object.optString("customerCode"));
                                model.setCustomerName(object.optString("customerName"));
                                model.setAddress1(object.optString("address"));
                                model.setAddress2(object.optString("street"));
                                model.setAddress3(object.optString("city"));
                                model.setCustomerAddress(object.optString("address"));
                                model.setHaveTax(object.optString("HaveTax"));
                                model.setTaxType(object.optString("taxType"));
                                model.setTaxPerc(object.optString("taxPercentage"));
                                model.setTaxCode(object.optString("taxCode"));
                                //  model.setCustomerBarcode(object.optString("BarCode"));
                                // model.setCustomerBarcode(String.valueOf(i));
                                if (object.optString("outstandingAmount").equals("null") || object.optString("outstandingAmount").isEmpty()) {
                                    model.setOutstandingAmount("0.00");
                                } else {
                                    model.setOutstandingAmount(object.optString("outstandingAmount"));
                                }
                                customerList.add(model);
                                // }
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),"Error,in getting Customer list",Toast.LENGTH_LONG).show();
                        }
                        // pDialog.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            //  pDialog.dismiss();
            Log.w("Error_throwing:",error.toString());
            progressDialog.dismiss();
            // Toast.makeText(getActivity(),error.toString(),Toast.LENGTH_LONG).show();
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }




    private void setupGroup(ArrayList<ItemGroupList> itemGroupLists) {
        ArrayAdapter<ItemGroupList> myAdapter = new ArrayAdapter<ItemGroupList>(this, android.R.layout.simple_list_item_1, itemGroupLists);
        groupspinner.setAdapter(myAdapter);
        groupspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject jsonObject=new JSONObject();
                try {
                    String itemCode=itemGroup.get(i).getGroupCode();
                    Log.e("selectspinn",""+ itemCode);
                    if (!itemCode.equals("Select Brand")){
                        jsonObject.put("User",username);
                        jsonObject.put("CardCode",customerCode);
                        jsonObject.put("ItemGroupCode",itemCode);
                        getAllProducts(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    private ArrayList<ItemGroupList> getGrouplist() throws JSONException {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(getApplicationContext()) +"ItemGroupList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_group:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading Groups...");
        pDialog.setCancelable(false);
        pDialog.show();
        itemGroup =new ArrayList<>();
        itemGroup.add(new ItemGroupList("Select Brand", "Select Brand"));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try{
                        Log.w("grouplist:",response.toString());

                        pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            JSONArray  groupArray=response.optJSONArray("responseData");

                            for (int i = 0; i < groupArray.length(); i++) {
                                JSONObject jsonObject = groupArray.getJSONObject(i);
                                String groupName = jsonObject.getString("itemGroupName");
                                String groupCode = jsonObject.getString("itemGroupCode");
                                ItemGroupList itemGroupList = new ItemGroupList(groupCode, groupName);
                                itemGroup.add(itemGroupList);
                            }
                            if (itemGroup.size()>0){
                                setupGroup(itemGroup);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            pDialog.dismiss();
            Log.w("Error_throwing:",error.toString());
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
        return itemGroup;
    }

    public void redirectActivity(){
        Intent intent;
        if (activityFrom.equals("iv") || activityFrom.equals("ConvertInvoice")){
            intent = new Intent(NewSalesReturnProductAddActivity.this, NewInvoiceListActivity.class);
        }else {
            intent = new Intent(NewSalesReturnProductAddActivity.this, SalesOrderListActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            int count=dbHelper.numberOfRowsInInvoice();
            if (count>0){
                showDeleteAlert();
            }else {
                finish();
            }
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_HOME){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    public void showDeleteAlert(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(NewSalesReturnProductAddActivity.this);
        builder1.setMessage("Data Will be Cleared are you sure want to back?");
        builder1.setCancelable(false);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHelper.removeAllInvoiceItems();
                        dbHelper.removeAllReturn();
                        finish();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void getInvoicePrintDetails(String invoiceNumber,int copy) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject = new JSONObject();
        // jsonObject.put("CompanyCode", companyId);
        jsonObject.put("InvoiceNo", invoiceNumber);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "InvoiceDetails";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:", url);
        //  pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        // pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        // pDialog.setTitleText("Processing please wait...");
        // pDialog.setCancelable(false);
        // pDialog.show();
        invoiceHeaderDetails = new ArrayList<>();
        invoicePrintList = new ArrayList<>();
        salesReturnList=new ArrayList<>();
        // {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"WinApp","customerName":"WinApp","invoiceNumber":"33",
        // "invoiceStatus":"O","invoiceDate":"6\/8\/2021 12:00:00 am","netTotal":"26.750000","balanceAmount":"26.750000","totalDiscount":
        // "0.000000","paidAmount":"0.000000","contactPersonCode":"","createDate":"6\/8\/2021 12:00:00 am","updateDate":"6\/8\/2021 12:00:00 am",
        // "remark":"","fDocTotal":"0.000000","fTaxAmount":"0.000000","receivedAmount":"0.000000","total":"26.750000","fTotal":"0.000000",
        // "iTotalDiscount":"0.000000","taxTotal":"1.750000","iPaidAmount":"0.000000","currencyCode":"SGD","currencyName":"Singapore Dollar",
        // "companyCode":"WINAPP_DEMO","docEntry":"20","invoiceDetails":[{"slNo":"1","companyCode":"WINAPP_DEMO","invoiceNo":"33",
        // "productCode":"FG\/001245","productName":"RUM","quantity":"5.000000","price":"5.000000","currency":"SGD","taxRate":"0.000000",
        // "discountPercentage":"0.000000","lineTotal":"26.750000","fRowTotal":"0.000000","warehouseCode":"01","salesEmployeeCode":"-1",
        // "accountCode":"400000","taxStatus":"Y","unitPrice":"5.000000","customerCategoryNo":"","barCodes":"","totalTax":"1.750000",
        // "fTaxAmount":"0.000000","taxCode":"","taxType":"Y","taxPerc":"0.000000","uoMCode":null,"invoiceDate":"6\/8\/2021 12:00:00 am",
        // "dueDate":"6\/8\/2021 12:00:00 am","createDate":"6\/8\/2021 12:00:00 am","updateDate":"6\/8\/2021 12:00:00 am","createdUser":"manager"}]}]}
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        Log.w("DetailsResponse::", response.toString());
                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray responseData=response.getJSONArray("responseData");
                            JSONObject object=responseData.optJSONObject(0);

                            InvoicePrintPreviewModel model = new InvoicePrintPreviewModel();
                            model.setInvoiceNumber(object.optString("invoiceNumber"));
                            model.setInvoiceDate(object.optString("invoiceDate"));
                            model.setCustomerCode(object.optString("customerCode"));
                            model.setCustomerName(object.optString("customerName"));
                            model.setOverAllTotal(object.optString("overAllTotal"));
                          //  model.setAddress(object.optString("street"));
                            model.setAddress(object.optString("address1") + object.optString("address2") + object.optString("address3"));
                            model.setAddress2(object.optString("address2"));
                            model.setAddress3(object.optString("address3"));
                            // model.setDeliveryAddress(model.getAddress());
                            model.setSubTotal(object.optString("subTotal"));
                            model.setNetTax(object.optString("taxTotal"));
                            model.setNetTotal(object.optString("netTotal"));
                            model.setPaymentTerm(object.optString("paymentTerm"));
                            model.setTaxType(object.optString("taxType"));
                            model.setTaxValue(object.optString("taxPerc"));
                            model.setOutStandingAmount(object.optString("totalOutstandingAmount"));
                            model.setBalanceAmount(object.optString("balanceAmount"));
                            Utils.setInvoiceOutstandingAmount(object.optString("balanceAmount"));
                            Utils.setInvoiceMode("Invoice");
                            model.setBillDiscount(object.optString("billDiscount"));
                            model.setItemDiscount(object.optString("totalDiscount"));
                            model.setSoNumber(object.optString("soNumber"));
                            model.setSoDate(object.optString("soDate"));
                            model.setDoDate(object.optString("doDate"));
                            model.setDoNumber(object.optString("doNumber"));
                            String signFlag=object.optString("signFlag");
                            if (signFlag.equals("Y")){
                                String signature=object.optString("signature");
                                Utils.setSignature(signature);
                                createSignature();
                            }else {
                                Utils.setSignature("");
                            }

                            JSONArray detailsArray=object.optJSONArray("invoiceDetails");
                            for (int i=0;i<detailsArray.length();i++){
                                JSONObject detailObject=detailsArray.optJSONObject(i);
                                InvoicePrintPreviewModel.InvoiceList invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();                                        invoiceListModel.setProductCode(detailObject.optString("productCode"));
                                invoiceListModel.setDescription(detailObject.optString("productName"));
                                invoiceListModel.setLqty(detailObject.optString("unitQty"));
                                invoiceListModel.setCqty(detailObject.optString("cartonQty"));
                                invoiceListModel.setNetQty(detailObject.optString("quantity"));
                                invoiceListModel.setNetQuantity(detailObject.optString("netQuantity"));
                                invoiceListModel.setFocQty(detailObject.optString("foc_Qty"));
                                invoiceListModel.setReturnQty(detailObject.optString("returnQty"));
                                invoiceListModel.setCartonPrice(detailObject.optString("cartonPrice"));
                                invoiceListModel.setUnitPrice(detailObject.optString("price"));

                                double qty1 = Double.parseDouble(detailObject.optString("quantity"));
                                double price1 = Double.parseDouble(detailObject.optString("price"));
                                double nettotal1 = qty1 * price1;
                                invoiceListModel.setTotal(detailObject.optString("lineTotal"));
                                invoiceListModel.setPricevalue(String.valueOf(price1));

                                invoiceListModel.setUomCode(detailObject.optString("uomCode"));
                                invoiceListModel.setPcsperCarton(detailObject.optString("pcsPerCarton"));
                                invoiceListModel.setItemtax(detailObject.optString("totalTax"));
                                invoiceListModel.setSubTotal(detailObject.optString("subTotal"));
                                invoicePrintList.add(invoiceListModel);

                                model.setInvoiceList(invoicePrintList);
                                invoiceHeaderDetails.add(model);
                            }

                            JSONArray SRArray=object.optJSONArray("sR_Details");
                            assert SRArray != null;
                            if (SRArray.length() > 0){
                                JSONObject SRoblect = SRArray.optJSONObject(0);
                                InvoicePrintPreviewModel.SalesReturnList salesReturnModel = new InvoicePrintPreviewModel.SalesReturnList();
                                salesReturnModel.setSalesReturnNumber(SRoblect.optString("salesReturnNumber"));
                                salesReturnModel.setsRSubTotal(SRoblect.optString("sR_SubTotal"));
                                salesReturnModel.setsRTaxTotal(SRoblect.optString("sR_TaxTotal"));
                                salesReturnModel.setsRNetTotal(SRoblect.optString("sR_NetTotal"));
                                salesReturnList.add(salesReturnModel);
                            }

                            model.setSalesReturnList(salesReturnList);
                            invoiceHeaderDetails.add(model);
                            printInvoice(copy);
                        }else {
                            Toast.makeText(getApplicationContext(),"Error in printing Data...",Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            //  pDialog.dismiss();
            Log.w("Error_throwing:", error.toString());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private void createSignature(){
        if (Utils.getSignature()!=null && !Utils.getSignature().isEmpty()){
            try {
                ImageUtil.saveStamp(this,Utils.getSignature(),"Signature");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void printInvoice(int copy){
        try {
           /* if (pDialog!=null && pDialog.isShowing()){
                pDialog.dismiss();
            }*/
            PrinterUtils printerUtils=new PrinterUtils(this,printerMacId);
            printerUtils.printInvoice(copy, invoiceHeaderDetails, invoicePrintList, "false");
            Utils.setSignature("");
        }catch (Exception e){}

    }

    private void getSalesOrderDetails(String soNumber,int copy) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        //  jsonObject.put("CompanyCode",companyId);
        jsonObject.put("SalesOrderNo", soNumber);
        // jsonObject.put("LocationCode",locationCode);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"SalesOrderDetails";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url);
        //   pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        //   pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        //  pDialog.setTitleText("Generating Print Preview...");
        //  pDialog.setCancelable(false);
        //  pDialog.show();
        salesOrderHeaderDetails =new ArrayList<>();
        salesPrintList =new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    try{
                        // {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"WinApp","customerName":"WinApp","soNumber":"3",
                        // "soStatus":"O","soDate":"6\/8\/2021 12:00:00 am","netTotal":"26.750000","balanceAmount":"26.750000",
                        // "totalDiscount":"0.000000","paidAmount":"0.000000","contactPersonCode":"","createDate":"7\/8\/2021 12:00:00 am",
                        // "updateDate":"7\/8\/2021 12:00:00 am","remark":"","fDocTotal":"0.000000","fTaxAmount":"0.000000",
                        // "receivedAmount":"0.000000","total":"26.750000","fTotal":"0.000000","iTotalDiscount":"0.000000",
                        // "taxTotal":"1.750000","iPaidAmount":"0.000000","currencyCode":"SGD","currencyName":"Singapore Dollar",
                        // "companyCode":"WINAPP_DEMO","docEntry":"3","address1":"SingaporeShipTo1   Changi 890323 SG","taxPercentage":"0.000000",
                        // "discountPercentage":"0.000000",
                        //
                        //
                        // "salesOrderDetails":[{"slNo":"1","companyCode":"WINAPP_DEMO","soNo":"3",
                        // "productCode":"FG\/001245","productName":"Milk","quantity":"5.000000","cartonQty":"1.000000",
                        // "price":"5.000000","currency":"SGD","taxRate":"0.000000","discountPercentage":"0.000000",
                        // "lineTotal":"26.750000","fRowTotal":"0.000000","warehouseCode":"01","salesEmployeeCode":"-1","accountCode":"400000",
                        // "taxStatus":"Y","unitPrice":"5.000000","customerCategoryNo":"","barCodes":"","totalTax":"1.750000",
                        // "fTaxAmount":"0.000000","taxCode":"","taxType":"E","taxPerc":"0.000000","uoMCode":null,"soDate":"6\/8\/2021 12:00:00 am",
                        // "dueDate":"6\/8\/2021 12:00:00 am","createDate":"7\/8\/2021 12:00:00 am","updateDate":"7\/8\/2021 12:00:00 am",
                        // "createdUser":"manager","uomCode":"Ctn","uoMName":"Carton","cartonPrice":"3000.000000","piecePrice":"0.000000",
                        // "pcsPerCarton":"100.000000","lPrice":"100.000000","unitQty":"1.000000","retailPrice":"100.000000"}]}]}
                        Log.w("Sales_Details:",response.toString());
                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray responseData=response.getJSONArray("responseData");
                            JSONObject object=responseData.optJSONObject(0);

                            SalesOrderPrintPreviewModel model=new SalesOrderPrintPreviewModel();
                            model.setSoNumber(object.optString("soNumber"));
                            model.setSoDate(object.optString("soDate"));
                            model.setCustomerCode(object.optString("customerCode"));
                            model.setCustomerName(object.optString("customerName"));
                            model.setAddress(object.optString("address1") + object.optString("address2") + object.optString("address3"));
                            model.setAddress2(object.optString("address2"));
                            model.setAddress3(object.optString("address3"));
                            // model.setDeliveryAddress(model.getAddress());
                            model.setSubTotal(object.optString("subTotal"));
                            model.setNetTax(object.optString("taxTotal"));
                            model.setNetTotal(object.optString("netTotal"));
                            model.setTaxType(object.optString("taxType"));
                            model.setTaxValue(object.optString("taxPerc"));
                            model.setOutStandingAmount(object.optString("outstandingAmount"));
                            model.setBillDiscount(object.optString("billDiscount"));
                            model.setItemDiscount(object.optString("totalDiscount"));
                            Utils.setInvoiceMode("SalesOrder");
                            String signFlag=object.optString("signFlag");
                            if (signFlag.equals("Y")){
                                String signature=object.optString("signature");
                                Utils.setSignature(signature);
                                createSignature();
                            }else {
                                Utils.setSignature("");
                            }

                            JSONArray detailsArray=object.optJSONArray("salesOrderDetails");
                            for (int i=0;i<detailsArray.length();i++){
                                JSONObject detailObject=detailsArray.optJSONObject(i);

                                SalesOrderPrintPreviewModel.SalesList salesListModel = new SalesOrderPrintPreviewModel.SalesList();
                                salesListModel.setProductCode(detailObject.optString("productCode"));
                                salesListModel.setDescription(detailObject.optString("productName"));
                                salesListModel.setLqty(detailObject.optString("unitQty"));
                                salesListModel.setCqty(detailObject.optString("cartonQty"));
                                salesListModel.setNetQty(detailObject.optString("quantity"));
                                salesListModel.setCartonPrice(detailObject.optString("cartonPrice"));
                                salesListModel.setUnitPrice(detailObject.optString("price"));

                                double qty1 = Double.parseDouble(detailObject.optString("quantity"));
                                double price1 = Double.parseDouble(detailObject.optString("price"));
                                double nettotal1 = qty1 * price1;
                                salesListModel.setTotal(String.valueOf(nettotal1));
                                salesListModel.setPricevalue(String.valueOf(price1));

                                salesListModel.setUomCode(detailObject.optString("uomCode"));
                                salesListModel.setPcsperCarton(detailObject.optString("pcsPerCarton"));
                                salesListModel.setItemtax(detailObject.optString("totalTax"));
                                salesListModel.setSubTotal(detailObject.optString("subTotal"));
                                salesPrintList.add(salesListModel);


                                if (!detailObject.optString("ReturnQty").isEmpty() && Double.parseDouble(detailObject.optString("ReturnQty")) > 0) {
                                    salesListModel = new SalesOrderPrintPreviewModel.SalesList();
                                    salesListModel.setProductCode(detailObject.optString("ProductCode"));
                                    salesListModel.setDescription(detailObject.optString("ProductName"));
                                    salesListModel.setLqty(detailObject.optString("LQty"));
                                    salesListModel.setCqty(detailObject.optString("CQty"));
                                    salesListModel.setNetQty("-"+detailObject.optString("ReturnQty"));

                                    double qty12 = Double.parseDouble(detailObject.optString("ReturnQty"));
                                    double price12 = Double.parseDouble(detailObject.optString("Price"));
                                    double nettotal12 = qty12 * price12;
                                    salesListModel.setTotal(String.valueOf(nettotal12));
                                    salesListModel.setPricevalue(String.valueOf(price12));

                                    salesListModel.setUomCode(detailObject.optString("UOMCode"));
                                    salesListModel.setCartonPrice(detailObject.optString("CartonPrice"));
                                    salesListModel.setUnitPrice(detailObject.optString("Price"));
                                    salesListModel.setPcsperCarton(detailObject.optString("PcsPerCarton"));
                                    salesListModel.setItemtax(detailObject.optString("Tax"));
                                    salesListModel.setSubTotal(detailObject.optString("subTotal"));
                                    salesPrintList.add(salesListModel);
                                }

                                model.setSalesList(salesPrintList);
                                salesOrderHeaderDetails.add(model);
                            }
                            sentSalesOrderDataPrint(copy);
                            // pDialog.dismiss();
                        }else {

                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            pDialog.dismiss();
            Log.w("Error_throwing:",error.toString());
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<>();
                String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }



    public void createSalesReturnJson() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        currentDateString = df.format(c);

        SimpleDateFormat df3 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        currentDate = df3.format(c);

        JSONObject rootJsonObject = new JSONObject();
        JSONObject srHeaderDetails = new JSONObject();
        JSONObject srBatchDetails=new JSONObject();
        JSONArray returnProductArray=new JSONArray();
        JSONArray saleDetailsArray =new JSONArray();
        JSONObject returnProductObject=new JSONObject();
        JSONArray srBatchArray=new JSONArray();
        JSONObject saleObject =new JSONObject();

      //  salesReturnNo = SalesReturnList.salesReturnNo;


        // {"SRNumber":"","Mode":"I","SRDate":"20210827","customerCode":"C1006","customerName":"A S KUMAR","address":"","street":"","city":"",
        // "creditLimit":"0.000000","remark":"","currencyName":"Singapore Dollar","taxTotal":"3.50","subTotal":"50.00","total":"50.00",
        // "netTotal":"53.50","itemDiscount":"0.00","billDiscount":"0.00","totalDiscount":null,"billDiscountPercentage":"0","deliveryCode":"0",
        // "DelCustomerName":"","delAddress1":"","delAddress2":null,"delAddress3":null,"delPhoneNo":"","haveTax":"","taxType":"Z","taxPerc":"",
        // "taxCode":"ZR","currencyCode":"SGD","currencyValue":"","CurrencyRate":"1","status":"","postalCode":"","createUser":"User1",
        // "modifyUser":"User1","companyName":null,"stockUpdated":null,"SOType":null,"companyCode":"AATHI_LIVE_DB","locationCode":"HQ","DONo":null,
        // "signature":"",
        //
        // "PostingDeliveryOrderDetails":[{"companyCode":"AATHI_LIVE_DB","SRDate":"20210827","slNo":"1","productCode":"SKU-AAFZ-0001",
        // "productName":"ASHOKA BHATURA 325 GM","cartonQty":"5","unitQty":"0","qty":"5.0","pcsPerCarton":"1","price":"10.00","cartonPrice":"10.00",
        // "retailPrice":"10.00","total":"50.0","itemDiscount":"0","totalTax":"3.50","subTotal":"50.00","netTotal":"53.50","taxType":"Z","taxPerc":"",
        // "returnLQty":"0","returnQty":"0","focQty":"0","exchangeQty":"0","returnSubTotal":"0.0","returnNetTotal":"0.0","taxCode":"ZR","uomCode":"PCS",
        // "itemRemarks":"","locationCode":"01","createUser":"User1","modifyUser":"User1"}]}


        try {
            // Sales Header Add values

            JSONArray detailsArray=customerResponse.optJSONArray("responseData");
            JSONObject object=detailsArray.optJSONObject(0);

           /* if (activityFrom.equals("SREdit")){
                rootJsonObject.put("SRNumber", AddInvoiceActivity.editSoNumber);
                rootJsonObject.put("mode", "E");
                rootJsonObject.put("status", "O");
            }else {
                rootJsonObject.put("soNumber", "");
                rootJsonObject.put("mode", "I");
                rootJsonObject.put("status", "");
            }*/

            rootJsonObject.put("SRNumber", "");
            rootJsonObject.put("mode", "I");
            rootJsonObject.put("status", "");

            rootJsonObject.put("SRDate", currentDateString);
            rootJsonObject.put("currentDateTime",currentSaveDateTime);
            rootJsonObject.put("customerCode", object.optString("customerCode"));
            rootJsonObject.put("customerName", object.optString("customerName"));
            rootJsonObject.put("address", object.optString("address"));
            rootJsonObject.put("street", object.optString("street"));
            rootJsonObject.put("city", object.optString("city"));
            rootJsonObject.put("creditLimit", object.optString("creditLimit"));
            rootJsonObject.put("remark", "");
            rootJsonObject.put("currencyName", "Singapore Dollar");
            rootJsonObject.put("total", netTotalValue.getText().toString());
            rootJsonObject.put("itemDiscount", "0.00");
            rootJsonObject.put("billDiscount", "0.00");
            rootJsonObject.put("billDiscountPercentage", "0.00");
            rootJsonObject.put("subTotal", subTotalValue.getText().toString());
            rootJsonObject.put("taxTotal", taxValueText.getText().toString());
            rootJsonObject.put("netTotal", netTotalValue.getText().toString());
            rootJsonObject.put("DeliveryCode", SettingUtils.getDeliveryAddressCode());
            rootJsonObject.put("delCustomerName", "");
            rootJsonObject.put("currentDateTime",Utils.getCurrentDateTime());
            rootJsonObject.put("delAddress1", object.optString("delAddress1"));
            rootJsonObject.put("delAddress2 ", object.optString("delAddress2"));
            rootJsonObject.put("delAddress3 ", object.optString("delAddress3"));
            rootJsonObject.put("delPhoneNo", object.optString("contactNo"));
            rootJsonObject.put("haveTax", object.optString("haveTax"));
            rootJsonObject.put("taxType", object.optString("taxType"));
            rootJsonObject.put("taxPerc", object.optString("taxPercentage"));
            rootJsonObject.put("taxCode", object.optString("taxCode"));
            rootJsonObject.put("currencyCode", object.optString("currencyCode"));
            rootJsonObject.put("currencyValue","");
            rootJsonObject.put("currencyRate", "1");
            rootJsonObject.put("postalCode", object.optString("postalCode"));
            rootJsonObject.put("createUser",username);
            rootJsonObject.put("modifyUser",username);
            rootJsonObject.put("companyCode",companyCode);
            rootJsonObject.put("locationCode",locationCode);
            rootJsonObject.put("signature",signatureString);
            rootJsonObject.put("latitude",current_latitude);
            rootJsonObject.put("longitude",current_longitude);

            // Sales Details Add to the Objects
            ArrayList<CreateInvoiceModel> localCart=dbHelper.getAllInvoiceProducts();

            Log.w("Given_local_cart_size:", String.valueOf(localCart.size()));

            //  "PostingSalesOrderDetails":[{"companyCode":"WINAPP_DEMO","soDate":"20210806","slNo":1,"productCode":"FG\/001245","productName":
            //  "RUM","cartonQty":"0","unitQty":"5","qty":"5.0","pcsPerCarton":"100","price":"5.00","cartonPrice":"500","retailPrice":"500",
            //  "total":"25.0","itemDiscount":"0","totalTax":"0.0","subTotal":"25.0","netTotal":"25.00","taxType":"I","taxPerc":"","returnLQty":"0",
            //  "returnQty":"0","focQty":"0","exchangeQty":"0","returnSubTotal":"0.0","returnNetTotal":"0.0","taxCode":"","uomCode":"Ctn",
            //  "itemRemarks":"","locationCode":"01","createUser":"User1","modifyUser":"User1"}]}

            int index=1;
            for (CreateInvoiceModel model:localCart){
                saleObject=new JSONObject();

                saleObject.put("companyCode",companyCode);
                saleObject.put("slNo",index);
                saleObject.put("productCode",model.getProductCode());
                saleObject.put("productName",model.getProductName());
                saleObject.put("cartonQty",model.getActualQty());
                saleObject.put("unitQty",model.getActualQty());
                saleObject.put("qty",String.valueOf(model.getActualQty()));
                saleObject.put("price",Utils.twoDecimalPoint(Double.parseDouble(model.getPrice())));
                saleObject.put("pcsPerCarton","1");
                saleObject.put("cartonPrice","0.00");
                saleObject.put("total",Utils.twoDecimalPoint(Double.parseDouble(model.getTotal())));
                saleObject.put("itemDiscount","0.00");
                saleObject.put("itemDiscountPercentage","0");
                saleObject.put("totalTax",model.getGstAmount());
                saleObject.put("subTotal",Utils.twoDecimalPoint(Double.parseDouble(model.getSubTotal())));
                saleObject.put("netTotal",Utils.twoDecimalPoint(Double.parseDouble(model.getNetTotal())));
                saleObject.put("taxType",object.optString("taxType"));
                saleObject.put("taxPerc",object.optString("taxPercentage"));
                saleObject.put("focQty", "0");

                double return_subtotal=0;
                if (model.getActualQty()!=null && !model.getActualQty().isEmpty()  && !model.getActualQty().equals("null")){
                    return_subtotal=Double.parseDouble(model.getActualQty()) * Double.parseDouble(model.getPrice());
                }

                assert model.getActualQty() != null;
                if (!model.getActualQty().isEmpty() && !model.getActualQty().toString().equals("null")){
                    saleObject.put("returnLQty", model.getActualQty());
                    saleObject.put("returnQty", model.getActualQty());
                }else {
                    saleObject.put("returnLQty", "0");
                    saleObject.put("returnQty", "0");
                }
                saleObject.put("exchangeQty", "0");

                saleObject.put("returnSubTotal",return_subtotal+"");
                saleObject.put("returnNetTotal", return_subtotal+"");

                saleObject.put("taxCode",object.optString("taxCode"));
                saleObject.put("uomCode",model.getUomCode());
                saleObject.put("retailPrice","0.00");
                saleObject.put("DamageStock","");
                saleObject.put("itemRemarks","");
                saleObject.put("locationCode",locationCode);
                saleObject.put("createUser",username);
                saleObject.put("modifyUser",username);


                ArrayList<ReturnProductsModel> returnProducts=dbHelper.getReturnProducts(model.getProductCode());
                returnProductArray=new JSONArray();
                if (returnProducts.size() > 0){
                    for (ReturnProductsModel returnProductsModel:returnProducts){
                        Log.w("ReturnProductsValues:",returnProductsModel.getProductCode()+"-"+returnProductsModel.getProductName()+"--"+returnProductsModel.getReturnQty());
                        if (returnProductsModel.getReturnQty()!=null && !returnProductsModel.getReturnQty().isEmpty() && Integer.parseInt(returnProductsModel.getReturnQty()) > 0){
                            returnProductObject=new JSONObject();
                            returnProductObject.put("ReturnReason",returnProductsModel.getReturnReason());
                            returnProductObject.put("ReturnQty",returnProductsModel.getReturnQty());
                            returnProductArray.put(returnProductObject);
                        }
                    }
                }
                saleObject.put("ReturnDetails",returnProductArray);
                saleDetailsArray.put(saleObject);
                index++;
            }

            rootJsonObject.put("PostingSalesReturnDetails",saleDetailsArray);
            Log.w("RootSalesReturn:",rootJsonObject.toString());

            saveSalesReturn(rootJsonObject,1);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.w("Given_Error:", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void saveSalesReturn(JSONObject jsonBody,int noofCopyPrint){
        try {
            pDialog = new SweetAlertDialog(NewSalesReturnProductAddActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Saving Sales Return...");
            pDialog.setCancelable(false);
            pDialog.show();
            RequestQueue requestQueue = Volley.newRequestQueue(NewSalesReturnProductAddActivity.this);
            Log.w("GivenSalesReturn:",jsonBody.toString());
            String URL=Utils.getBaseUrl(getApplicationContext())+"PostingSalesReturn";
            Log.w("Given_SalesReturnApi:",URL);
            JsonObjectRequest salesOrderRequest = new JsonObjectRequest(Request.Method.POST, URL,jsonBody, response -> {
                Log.w("Sales_returnResponse:",response.toString());
                try {
                    //   {"statusCode":1,"statusMessage":"Sales Return Created Successfully","responseData":{"docNum":"1000005","error":null}}
                    String statusCode=response.optString("statusCode");
                    String  statusMessage=response.optString("statusMessage");
                    JSONObject jsonObject=response.optJSONObject("responseData");
                    String salesReturnNumber=jsonObject.optString("docNum");
                    String errorMessage="";
                    String error=jsonObject.optString("error");
                    if (!error.equals("null")){
                        errorMessage=error;
                    }
                    if (statusCode.equals("1")){
                        dbHelper.removeAllItems();
                        dbHelper.removeCustomer();
                        dbHelper.removeAllReturn();
                        Utils.clearCustomerSession(getApplicationContext());
                        AppUtils.setProductsList(null);
                        pDialog.dismiss();
                        SalesReturnList.isEdit=false;
                        if (isPrintEnable){
                            Intent intent=new Intent(getApplicationContext(),NewSalesReturnListActivity.class);
                            intent.putExtra("srNumber",salesReturnNumber);
                            intent.putExtra("noOfCopy",String.valueOf(noofCopyPrint));
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent=new Intent(getApplicationContext(),NewSalesReturnListActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(),statusMessage+" : "+errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, error -> {
                Log.w("SR_Error:",error.toString());
                pDialog.dismiss();
            }) {
                @Override
                public byte[] getBody() {
                    return jsonBody.toString().getBytes();
                }
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> params = new HashMap<>();
                    String creds = String.format("%s:%s", Constants.API_SECRET_CODE, Constants.API_SECRET_PASSWORD);
                    String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                    params.put("Authorization", auth);
                    return params;
                }
            };
            salesOrderRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }
                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }
                @Override
                public void retry(VolleyError error) throws VolleyError {
                }
            });
            requestQueue.add(salesOrderRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private void sentSalesOrderDataPrint(int copy) throws IOException {
        if (printerType.equals("TSC Printer")){
            TSCPrinter printer=new TSCPrinter(NewSalesReturnProductAddActivity.this,printerMacId,"SalesOrder");
            printer.printSalesOrder(copy,salesOrderHeaderDetails,salesPrintList);
            printer.setOnCompletionListener(new TSCPrinter.OnCompletionListener() {
                @Override
                public void onCompleted() {
                    Utils.setSignature("");
                    Toast.makeText(getApplicationContext(),"SalesOrder printed successfully!",Toast.LENGTH_SHORT).show();
                }
            });
        }else if (printerType.equals("Zebra Printer")){
            ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(NewSalesReturnProductAddActivity.this,printerMacId);
            zebraPrinterActivity.printSalesOrder(copy,salesOrderHeaderDetails,salesPrintList);
        }
    }

    public void setNewPrint() {
        PrinterUtils printerUtils = new PrinterUtils(this, printerMacId);
        printerUtils.printLabel();
    }
}