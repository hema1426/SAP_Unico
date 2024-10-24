
package com.winapp.saperp.activity;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cete.dynamicpdf.Document;
import com.cete.dynamicpdf.Font;
import com.cete.dynamicpdf.Grayscale;
import com.cete.dynamicpdf.Page;
import com.cete.dynamicpdf.PageOrientation;
import com.cete.dynamicpdf.PageSize;
import com.cete.dynamicpdf.TextAlign;
import com.cete.dynamicpdf.VAlign;
import com.cete.dynamicpdf.pageelements.Cell2;
import com.cete.dynamicpdf.pageelements.Label;
import com.cete.dynamicpdf.pageelements.Row2;
import com.cete.dynamicpdf.pageelements.Table2;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.winapp.saperp.BuildConfig;
import com.winapp.saperp.R;
import com.winapp.saperp.adapter.InvoiceAdapter;
import com.winapp.saperp.adapter.SelectCustomerAdapter;
import com.winapp.saperp.adapter.SortAdapter;
import com.winapp.saperp.db.DBHelper;
import com.winapp.saperp.fragments.AllInvoices;
import com.winapp.saperp.fragments.CustomerFragment;
import com.winapp.saperp.fragments.PaidInvoices;
import com.winapp.saperp.fragments.UnpaidInvoices;
import com.winapp.saperp.model.AppUtils;
import com.winapp.saperp.model.CustomerDetails;
import com.winapp.saperp.model.CustomerModel;
import com.winapp.saperp.model.InvoiceModel;
import com.winapp.saperp.model.InvoicePrintPreviewModel;
import com.winapp.saperp.model.SettingsModel;
import com.winapp.saperp.model.UserListModel;
import com.winapp.saperp.model.UserRoll;
import com.winapp.saperp.printpreview.DOPrintPreview;
import com.winapp.saperp.printpreview.InvoicePrintPreviewActivity;
import com.winapp.saperp.printpreview.NewInvoicePrintPreviewActivity;
import com.winapp.saperp.thermalprinter.PrinterUtils;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.FileDownloader;
import com.winapp.saperp.utils.ImageUtil;
import com.winapp.saperp.utils.Pager;
import com.winapp.saperp.utils.PdfUtils;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.SharedPreferenceUtil;
import com.winapp.saperp.utils.UserAdapter;
import com.winapp.saperp.utils.Utils;
import com.winapp.saperp.zebraprinter.TSCPrinter;
import com.winapp.saperp.zebraprinter.ZebraPrinterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NewInvoiceListActivity extends NavigationActivity
        implements View.OnClickListener, TabLayout.OnTabSelectedListener, OnPageChangeListener,
        OnLoadCompleteListener, AdapterView.OnItemSelectedListener {

    // Define the Button Variables
    private TextView allInvoiceButton;
    private TextView paidInvoiceButton;
    private TextView outstandingInvoiceButton;
    private View invoiceView;
    private View paidView;
    private String selectedTag;
    private Menu menu;
    private RecyclerView invoiceListView;
    private InvoiceAdapter invoiceAdapter;
    private ArrayList<InvoiceModel> invoiceList;
    private SweetAlertDialog pDialog;
    int pageNo = 1;
    private SessionManager session;
    private HashMap<String, String> user;
    private String companyId;
    //This is our tablayout
    private TabLayout tabLayout;
    //This is our viewPager
    private ViewPager viewPager;
    private LinearLayout operationLayout;
    private LinearLayout emptyLayout;
    private Toolbar toolbar;
    private RecyclerView lettersRecyclerview;
    private RecyclerView customerView;
    private SortAdapter adapter;
    private ArrayList<String> letters;
    private SearchableSpinner PartSpinner;
    private ArrayList<String> PartName;
    private List<String> PartId;
    public static TextView selectCustomer;
    private Button btnCancel;
    private TextView customerName;
    static BottomSheetBehavior behavior;
    private EditText customerNameEdittext;
    private SelectCustomerAdapter customerNameAdapter;
    private TextView dateText;
    private TextView userName;
    private String companyCode;
    private String customerId;
    private DBHelper dbHelper;
    private ArrayList<CustomerModel> customerList;
    private TextView totalCustomers;
    private Button cancelSheet;
    private ArrayList<CustomerDetails> customerDetails;
    private TextView netTotalText;
    static View customerLayout;
    static View salesOrderOptionLayout;
    private TextView optionCancel;
    private LinearLayout transLayout;
    private View searchFilterView;
    private EditText customerNameText;
    private EditText fromDate;
    private LinearLayout fromDatelay;
    private EditText toDate;
    private Spinner invoiceStatus;
    private int mYear, mMonth, mDay, mHour, mMinute;
    public static boolean isSearchCustomerNameClicked = false;
    private Button searchButton;
    private Button cancelSearch;
    static View invoiceOptionLayout;
    public static TextView invoiceCustomerName;
    public static String invoiceDate;
    public static String invoiceStatusValue;
    public static TextView invoiceNumber;
    private FloatingActionButton editInvoice;
    private FloatingActionButton deleteInvoice;
    private FloatingActionButton cashCollection;
    private FloatingActionButton printPreview;
    private FloatingActionButton invoicePrint;
    private FloatingActionButton deliveryOrderPrint;
    private FloatingActionButton doPrintPreview;
    private FloatingActionButton cancelInvoice;
    public static String invoiceCustomerCodeValue;
    public static String invoiceCustomerValue;
    public static String invoiceNumberValue;
    public static LinearLayout editInvoiceLayout;
    public static LinearLayout deleteInvoiceLayout;
    public static LinearLayout cashCollectionLayout;
    public static LinearLayout printPreviewLayout;
    public static LinearLayout duplicateInvoiceLayout;
    public static LinearLayout invoicePrintLayout;
    public static LinearLayout doPrintLayout;
    private static LinearLayout cancelInvoiceLayout;
    private FrameLayout rootLayout;
    private LinearLayout mainLayout;
    private ArrayList<InvoicePrintPreviewModel> invoiceHeaderDetails;
    private ArrayList<InvoicePrintPreviewModel.InvoiceList> invoicePrintList;
    public String printInvoiceNumber;
    public String noOfCopy;
    public String DOPrint = "false";
    private String printerMacId;
    public static String isLastSales = "false";
    private String printerType;
    private SharedPreferenceUtil sharedPreferenceUtil;

    private SharedPreferences sharedPreferences;

    private SharedPreferences sharedPref_billdisc;
    private SharedPreferences.Editor myEdit;

    private View progressLayout;
    private static String visibleFragment = "invoices";
    public boolean redirectInvoice = false;
    public static String selectCustomerId = "";
    public static String selectCustomerName = "";
    private int customerSelectCode = 23;

    public static int REQUEST_PERMISSIONS = 154;

    private static String FILE;
    String[] invoiceTitlelistArray, invoicepricelistArray, invoiceQNTlistArray, invoiceNOlistArray;
    String invoiceNO = "";
    TextView path_pdf;
    File storagePath;
    private static final int PERMISSION_REQUEST_CODE = 100;
    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    static View pdfViewLayout;
    File pdfFile;

    private String company_name;
    private String company_address1;
    private String company_address2;
    private String company_address3;
    private ProgressDialog pdfGenerateDialog;
    public String oustandingAmount = "0.0";
    public TextView emptyTextView;

    public boolean isInvoicePrint = true;
    private String username;
    private ProgressDialog pd;
    LinearLayout shareLayout;
    LinearLayout printLayout;
    Button cancelButton;
    public String shareMode = "Share";
    public String createInvoiceSetting = "false";
    private ArrayList<InvoicePrintPreviewModel.SalesReturnList> salesReturnList;

    private String selectCustomerCode = "";

    public static String invStatus;
    public static String customerCodeStr = "";
    public static String customerNameStr = "";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE,
            MANAGE_EXTERNAL_STORAGE
    };

    private int FILTER_CUSTOMER_CODE = 134;
    private Spinner salesManSpinner;
    private String selectedUser = "";
    private ArrayList<UserListModel> usersList;
    public String locationCode;
    public static String shortCodeStr = "" ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_new_invoice_list, contentFrameLayout);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Invoices");

        pd = new ProgressDialog(NewInvoiceListActivity.this);
        pd.setMessage("Downloading Product Image, please wait ...");
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        pd.setProgressNumberFormat("%1d KB/%2d KB");

        pdfGenerateDialog = new ProgressDialog(this);
        pdfGenerateDialog.setCancelable(false);
        pdfGenerateDialog.setMessage("Invoice Pdf generating please wait...");

        allInvoiceButton = findViewById(R.id.btn_all_invoice);
        paidInvoiceButton = findViewById(R.id.btn_paid_invoice);
        outstandingInvoiceButton = findViewById(R.id.btn_outstanding_invoice);
        allInvoiceButton.setOnClickListener(this);
        paidInvoiceButton.setOnClickListener(this);
        outstandingInvoiceButton.setOnClickListener(this);
        invoiceView = findViewById(R.id.invoice_view);
        paidView = findViewById(R.id.paid_view);
        invoiceListView = findViewById(R.id.invoiceList);
        session = new SessionManager(this);
        user = session.getUserDetails();
        companyId = user.get(SessionManager.KEY_COMPANY_CODE);
        username = user.get(SessionManager.KEY_USER_NAME);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);

        sharedPref_billdisc = getSharedPreferences("BillDiscPref", MODE_PRIVATE);
        myEdit = sharedPref_billdisc.edit();

        visibleFragment = "invoices";

        operationLayout = findViewById(R.id.operation_layout);
        emptyLayout = findViewById(R.id.empty_layout);
        emptyTextView = findViewById(R.id.empty_text);
        customerNameEdittext = findViewById(R.id.customer_search);
        netTotalText = findViewById(R.id.net_total_value);

        session = new SessionManager(this);
        dbHelper = new DBHelper(this);
        companyCode = user.get(SessionManager.KEY_COMPANY_CODE);
        locationCode = user.get(SessionManager.KEY_LOCATION_CODE);

        Log.w("bankDetail ",""+user.get(SessionManager.KEY_PAY_NOW)+" bank "+user.get(SessionManager.KEY_BANK)
                +" cheque "+user.get(SessionManager.KEY_CHEQUE));

        shortCodeStr = sharedPreferenceUtil.getStringPreference(sharedPreferenceUtil
            .KEY_SHORT_CODE,"");
        customerView = findViewById(R.id.customerList);
        totalCustomers = findViewById(R.id.total_customers);
        cancelSheet = findViewById(R.id.cancel_sheet);
        viewPager = findViewById(R.id.pager);
        searchFilterView = findViewById(R.id.search_filter);
        invoiceStatus = findViewById(R.id.invoice_status);
        customerNameText = findViewById(R.id.customer_name_value);
        fromDate = findViewById(R.id.from_date);
        fromDatelay = findViewById(R.id.fromdateLay);
        toDate = findViewById(R.id.to_date);
        searchButton = findViewById(R.id.btn_search);
        transLayout = findViewById(R.id.trans_layout);
        customerLayout = findViewById(R.id.customer_layout);
        invoiceOptionLayout = findViewById(R.id.invoice_option);
        optionCancel = findViewById(R.id.option_cancel);
        cancelSheet = findViewById(R.id.cancel_sheet);
        cancelSearch = findViewById(R.id.btn_cancel);
        invoiceCustomerName = findViewById(R.id.invoice_name);
        invoiceNumber = findViewById(R.id.sr_no);
        editInvoice = findViewById(R.id.edit_invoice);
        deleteInvoice = findViewById(R.id.delete_invoice);
        cashCollection = findViewById(R.id.cash_collection);
        printPreview = findViewById(R.id.print_preview);
        editInvoiceLayout = findViewById(R.id.edit_invoice_layout);
        deleteInvoiceLayout = findViewById(R.id.delete_invoice_layout);
        cashCollectionLayout = findViewById(R.id.cash_collection_layout);
        printPreviewLayout = findViewById(R.id.preview_invoice_layout);
        duplicateInvoiceLayout = findViewById(R.id.duplicate_inv_layout);
        cancelInvoiceLayout = findViewById(R.id.cancel_invoice_layout);
        cancelInvoice = findViewById(R.id.cancel_invoice);
        rootLayout = findViewById(R.id.rootLayout);
        mainLayout = findViewById(R.id.main_layout);
        progressLayout = findViewById(R.id.progress_layout);
        pdfView = findViewById(R.id.pdfView);
        pdfViewLayout = findViewById(R.id.pdf_layout);
        invoicePrint = findViewById(R.id.invoice_print);
        invoicePrintLayout = findViewById(R.id.invoice_print_layout);
        doPrintLayout = findViewById(R.id.do_print_layout);
        deliveryOrderPrint = findViewById(R.id.do_print);
        doPrintPreview = findViewById(R.id.do_print_preview);
        salesManSpinner = findViewById(R.id.salesman_spinner);
        salesManSpinner.setOnItemSelectedListener(this);

        shareLayout = findViewById(R.id.share_layout);
        printLayout = findViewById(R.id.print_layout);
        cancelButton = findViewById(R.id.cancel);

        myEdit.putString("billDisc_amt","0.0");
        myEdit.putString("billDisc_percent","0.0");
        myEdit.apply();

        company_name = user.get(SessionManager.KEY_COMPANY_NAME);
        company_address1 = user.get(SessionManager.KEY_ADDRESS1);
        company_address2 = user.get(SessionManager.KEY_ADDRESS2);
        company_address3 = user.get(SessionManager.KEY_ADDRESS3);

        Log.w("salenamm", user.get(SessionManager.KEY_SALESMAN_NAME));
        Log.w("salephon", user.get(SessionManager.KEY_SALESMAN_PHONE));
        Log.w("salemail", user.get(SessionManager.KEY_SALESMAN_EMAIL));


        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType = sharedPreferences.getString("printer_type", "");
        printerMacId = sharedPreferences.getString("mac_address", "");

        isLastSales = sharedPreferenceUtil.getStringPreference(sharedPreferenceUtil.KEY_TOTAL_SALES, "");

        // PrinterUtils printerUtils=new PrinterUtils(this,printerMacId);
        //   printerUtils.connectPrinter();

        emptyTextView.setVisibility(View.VISIBLE);

        dbHelper.removeAllItems();
        dbHelper.removeAllInvoiceItems();
        AddInvoiceActivityOld.order_no = "";

        Log.w("Printer_Mac_Id:", printerMacId);
        Log.w("Printer_Type:", printerType);

        try {
            getAllUsers();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AppUtils.setProductsList(null);
        verifyStoragePermissions(this);
        checkPermission();

        ArrayList<SettingsModel> settings = dbHelper.getSettings();
        if (settings != null) {
            if (settings.size() > 0) {
                for (SettingsModel model : settings) {
                    if (model.getSettingName().equals("create_invoice_switch")) {
                        Log.w("SettingNameInv:", model.getSettingName());
                        Log.w("SettingValueInv:", model.getSettingValue());
                        if (model.getSettingValue().equals("1")) {
                            createInvoiceSetting = "true";
                        } else {
                            createInvoiceSetting = "false";
                        }
                    }
                    else if (model.getSettingName().equals("showSignature")) {
                        Log.w("SettingNamesign:", model.getSettingName());
                        Log.w("SettingValuesign:", model.getSettingValue());
                    }
                }
            }
        }

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("ALL"));
        tabLayout.addTab(tabLayout.newTab().setText("PAID"));
        tabLayout.addTab(tabLayout.newTab().setText("OUTSTANDING"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);
        //Creating our pager adapter
        Pager adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
        //Adding adapter to pager
        viewPager.setAdapter(adapter);
        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setOffscreenPageLimit(3);
        //  dbHelper.removeAllProducts();

        View bottomSheet = findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        fromDate.setText(formattedDate);
        toDate.setText(formattedDate);

        // This function for print the invoice details
        if (getIntent() !=null){
            printInvoiceNumber=getIntent().getStringExtra("printInvoiceNumber");
            noOfCopy=getIntent().getStringExtra("noOfCopy");
//            if(getIntent().getStringExtra("DOPrint").equals("null") &&
//                    getIntent().getStringExtra("DOPrint") != null) {
//                DOPrint = getIntent().getStringExtra("DOPrint");
         //   }
            if (printInvoiceNumber!=null && !printInvoiceNumber.isEmpty()){
                try {
                    getInvoicePrintDetails(printInvoiceNumber,Integer.parseInt(noOfCopy),DOPrint);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        /*customerList=dbHelper.getAllCustomers();
        if (customerList!=null && customerList.size()>0){
            emptyTextView.setVisibility(View.GONE);
            setAdapter(customerList);
        }else {
            getCustomers();
            //new GetCustomersTask().execute();
        }*/

        //  getCustomers();

        ArrayList<UserRoll> userRolls = dbHelper.getUserPermissions();
        if (userRolls.size() > 0) {
            for (UserRoll roll : userRolls) {
                if (roll.getFormName().equals("Edit Invoice")) {
                    if (roll.getHavePermission().equals("true")) {
                        editInvoiceLayout.setVisibility(View.GONE);
                    } else {
                        editInvoiceLayout.setVisibility(View.GONE);
                    }
                } else if (roll.getFormName().equals("Delete Invoice")) {
                    if (roll.getHavePermission().equals("true")) {
                        deleteInvoiceLayout.setVisibility(View.GONE);
                    } else {
                        deleteInvoiceLayout.setVisibility(View.GONE);
                    }
                }
            }
        }

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
                        if (invoiceOptionLayout.getVisibility() == View.VISIBLE) {
                            getSupportActionBar().setTitle("Select Option");
                        } else {
                            getSupportActionBar().setTitle("Invoices");
                        }
                        transLayout.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                        getSupportActionBar().setTitle("Invoices");
                        transLayout.setVisibility(View.GONE);
                        if (redirectInvoice) {
                            CustomerFragment.isLoad = true;
                            Intent intent = new Intent(NewInvoiceListActivity.this, AddInvoiceActivityOld.class);
                            intent.putExtra("customerId", selectCustomerId);
                            intent.putExtra("activityFrom", "Invoice");
                            startActivity(intent);
                            finish();
                        }
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

        duplicateInvoiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
                if (!invStatus.equals("Closed") && !invStatus.equals("InProgress Invoice")) {
                    try {
                        getInvoiceEditDetails(invoiceNumberValue);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    //   redirectActivity(customerNameStr, customerCodeStr, invoiceNumberValue);
                } else {
                    Toast.makeText(getApplicationContext(), "This Invoice already Closed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        customerNameEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String cusname = editable.toString();
                if (!cusname.isEmpty()) {
                    filter(cusname);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
            }
        });

        cancelSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
            }
        });

        invoiceStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLUE);
                    ((TextView) parent.getChildAt(0)).setTextSize(12);
                     if (invoiceStatus.getSelectedItem().equals("UNPAID")) {
                      //  invoice_status = "O";
                         fromDatelay.setAlpha(0.4F);
                         fromDatelay.setEnabled(false);
                     } else {
                         fromDatelay.setAlpha(0.9F);
                         fromDatelay.setEnabled(true);
                     }
                } catch (Exception ed) {
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        customerNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  isSearchCustomerNameClicked=true;
                //  viewCloseBottomSheet();
                Intent intent = new Intent(getApplicationContext(), FilterCustomerListActivity.class);
                startActivityForResult(intent, FILTER_CUSTOMER_CODE);
            }
        });

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(fromDate);
            }
        });

        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(toDate);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String customer_name = customerNameText.getText().toString();
                SimpleDateFormat sdformat = new SimpleDateFormat("dd/MM/yyyy");
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = sdformat.parse(fromDate.getText().toString());
                    d2 = sdformat.parse(toDate.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (d1.compareTo(d2) > 0) {
                    Toast.makeText(getApplicationContext(), "From date should not be greater than to date", Toast.LENGTH_SHORT).show();
                } else {
                    searchFilterView.setVisibility(View.GONE);
                    isSearchCustomerNameClicked = true;
                    // ((AllInvoices)getSupportFragmentManager().findFragmentById(R.id.fragment_container)).filterValidation();
                    AllInvoices invoices = new AllInvoices();
                    try {
                        String oldFromDate = fromDate.getText().toString();
                        String oldToDate = toDate.getText().toString();
                        Date fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(oldFromDate);
                        Date toDate = new SimpleDateFormat("dd/MM/yyyy").parse(oldToDate);
                        // Use SimpleDateFormat#format() to format a Date into a String in a certain pattern.

                        String fromDateString = new SimpleDateFormat("yyyyMMdd").format(fromDate);
                        String toDateString = new SimpleDateFormat("yyyyMMdd").format(toDate);
                        System.out.println(fromDateString + "-" + toDateString); // 2011-01-18
                        String invoice_status = "";
                        if (invoiceStatus.getSelectedItem().equals("ALL")) {
                            invoice_status = "";
                        } else if (invoiceStatus.getSelectedItem().equals("PAID")) {
                            invoice_status = "C";
                        } else if (invoiceStatus.getSelectedItem().equals("UNPAID")) {
                            invoice_status = "O";
                            fromDateString = "" ;
                        }
                        if (selectedUser != null && !selectedUser.isEmpty()) {
                            username = selectedUser;
                        }
                        invoices.filterSearch(NewInvoiceListActivity.this, username, selectCustomerCode, invoice_status, fromDateString, toDateString, locationCode);
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                    // AllInvoices.filterSearch(customer_name,invoiceStatus.getSelectedItem().toString(),fromDate.getText().toString(),toDate.getText().toString());
                    invoiceStatus.setSelection(0);
                }
            }
        });


        cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSearchCustomerNameClicked = false;
                selectCustomerId = "";
                customerNameText.setText("");
                invoiceStatus.setSelection(0);
                fromDate.setText(formattedDate);
                toDate.setText(formattedDate);
                searchFilterView.setVisibility(View.GONE);
                invoiceStatus.setSelection(0);
                AllInvoices invoices = new AllInvoices();
                invoices.filterCancel();
            }
        });


        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                sharePdfView(pdfFile);
            }
        });


        optionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
            }
        });

        editInvoiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    viewCloseBottomSheet();
                    getInvoiceDetails(invoiceNumberValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        editInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    viewCloseBottomSheet();
                    getInvoiceDetails(invoiceNumberValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        deleteInvoiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRemoveAlert(invoiceNumberValue);
            }
        });

        deleteInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRemoveAlert(invoiceNumberValue);
            }
        });

        cashCollectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
                Intent intent = new Intent(NewInvoiceListActivity.this, CashCollectionActivity.class);
                intent.putExtra("customerCode", invoiceCustomerCodeValue);
                intent.putExtra("customerName", invoiceCustomerValue);
                startActivity(intent);
                finish();
            }
        });

        cashCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
                Intent intent = new Intent(NewInvoiceListActivity.this, CashCollectionActivity.class);
                intent.putExtra("customerCode", invoiceCustomerCodeValue);
                intent.putExtra("customerName", invoiceCustomerValue);
                startActivity(intent);
                finish();
            }
        });

        printPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
                if (createInvoiceSetting.equals("true")) {
                    Intent intent = new Intent(NewInvoiceListActivity.this, NewInvoicePrintPreviewActivity.class);
                    intent.putExtra("invoiceNumber", invoiceNumberValue);
                    intent.putExtra("outstandingAmount", oustandingAmount);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(NewInvoiceListActivity.this, InvoicePrintPreviewActivity.class);
                    intent.putExtra("invoiceNumber", invoiceNumberValue);
                    intent.putExtra("outstandingAmount", oustandingAmount);
                    startActivity(intent);
                }
            }
        });

        doPrintPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
                Intent intent = new Intent(NewInvoiceListActivity.this, DOPrintPreview.class);
                intent.putExtra("invoiceNumber", invoiceNumberValue);
                intent.putExtra("outstandingAmount", oustandingAmount);
                startActivity(intent);
            }
        });

        printPreviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewCloseBottomSheet();
                if (createInvoiceSetting.equals("true")) {
                    Intent intent = new Intent(NewInvoiceListActivity.this, NewInvoicePrintPreviewActivity.class);
                    intent.putExtra("invoiceNumber", invoiceNumberValue);
                    intent.putExtra("outstandingAmount", oustandingAmount);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(NewInvoiceListActivity.this, InvoicePrintPreviewActivity.class);
                    intent.putExtra("invoiceNumber", invoiceNumberValue);
                    intent.putExtra("outstandingAmount", oustandingAmount);
                    startActivity(intent);
                }
            }
        });

        printLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (printInvoiceNumber != null && !printInvoiceNumber.isEmpty()) {
                    try {
                        getInvoicePrintDetails(printInvoiceNumber, 1,"false");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        invoicePrintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    viewCloseBottomSheet();
                    isInvoicePrint = true;
                    getInvoicePrintDetails(invoiceNumberValue, 1,"false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        invoicePrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    viewCloseBottomSheet();
                    isInvoicePrint = true;
                    getInvoicePrintDetails(invoiceNumberValue, 1,"false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

      /*  doPrintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    viewCloseBottomSheet();
                    isInvoicePrint=false;
                    getInvoicePrintDetails(invoiceNumberValue,1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
*/
        deliveryOrderPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    viewCloseBottomSheet();
                    isInvoicePrint = false;
                    getInvoicePrintDetails(invoiceNumberValue, 1,"false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        cancelInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveAlert(invoiceNumberValue);
            }
        });

        cancelInvoiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveAlert(invoiceNumberValue);
            }
        });

        loadFragment(new AllInvoices());

    }

    private void getAllUsers() throws JSONException {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "UserList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_UserList:", url);
        usersList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("User", username);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Getting Users...");
        pDialog.setCancelable(false);
        pDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        Log.w("UserListResponse:", response.toString());
                        String statusCode = response.optString("statusCode");
                        String message = response.optString("statusMessage");
                        if (statusCode.equals("1")) {
                            JSONArray responseData = response.getJSONArray("responseData");
                            for (int i = 0; i < responseData.length(); i++) {
                                JSONObject object = responseData.optJSONObject(i);
                                UserListModel model = new UserListModel();
                                model.setUserName(object.optString("userName"));
                                model.setGender(object.optString("sex"));
                                model.setJobTitle(object.optString("jobTitle"));
                                usersList.add(model);
                            }
                            if (usersList.size() > 0) {
                                setUserListAdapter(usersList);
                            } else {
                                Toast.makeText(getApplicationContext(), "No Users Found...", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                        pDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            pDialog.dismiss();
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

    public void setUserListAdapter(ArrayList<UserListModel> usersList) {
        UserAdapter customAdapter = new UserAdapter(getApplicationContext(), usersList);
        salesManSpinner.setAdapter(customAdapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
        // PdfDocument.Meta meta = pdfView.getDocumentMeta();
        // printBookmarksTree(pdfView.getTableOfContents(), "-");

    }


    @Override
    protected void onResume() {
        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType = sharedPreferences.getString("printer_type", "");
        printerMacId = sharedPreferences.getString("mac_address", "");
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_all_invoice) {
            allInvoiceButton.setBackgroundResource(R.drawable.button_order);
            paidInvoiceButton.setBackgroundResource(R.drawable.button_unselect);
            outstandingInvoiceButton.setBackgroundResource(R.drawable.button_unselect);

            allInvoiceButton.setTextColor(Color.parseColor("#FFFFFF"));
            paidInvoiceButton.setTextColor(Color.parseColor("#212121"));
            outstandingInvoiceButton.setTextColor(Color.parseColor("#212121"));

            invoiceView.setVisibility(View.GONE);
            paidView.setVisibility(View.VISIBLE);

            invalidateOptionsMenu();

            if (!visibleFragment.equals("invoices"))
                loadFragment(new AllInvoices());
            visibleFragment = "invoices";

        } else if (view.getId() == R.id.btn_paid_invoice) {
            allInvoiceButton.setBackgroundResource(R.drawable.button_unselect);
            paidInvoiceButton.setBackgroundResource(R.drawable.button_order);
            outstandingInvoiceButton.setBackgroundResource(R.drawable.button_unselect);

            allInvoiceButton.setTextColor(Color.parseColor("#212121"));
            paidInvoiceButton.setTextColor(Color.parseColor("#FFFFFF"));
            outstandingInvoiceButton.setTextColor(Color.parseColor("#212121"));

            invoiceView.setVisibility(View.GONE);
            paidView.setVisibility(View.GONE);

            invalidateOptionsMenu();
            if (!visibleFragment.equals("paid"))
                loadFragment(new PaidInvoices());
            visibleFragment = "paid";
        } else if (view.getId() == R.id.btn_outstanding_invoice) {
            allInvoiceButton.setBackgroundResource(R.drawable.button_unselect);
            paidInvoiceButton.setBackgroundResource(R.drawable.button_unselect);
            outstandingInvoiceButton.setBackgroundResource(R.drawable.button_order);

            allInvoiceButton.setTextColor(Color.parseColor("#212121"));
            paidInvoiceButton.setTextColor(Color.parseColor("#212121"));
            outstandingInvoiceButton.setTextColor(Color.parseColor("#FFFFFF"));

            invoiceView.setVisibility(View.VISIBLE);
            paidView.setVisibility(View.GONE);


            invalidateOptionsMenu();
            if (!visibleFragment.equals("unpaid"))
                loadFragment(new UnpaidInvoices());
            visibleFragment = "unpaid";
        }
    }


    public void showRemoveAlert(String InvoiceId) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                // .setTitleText("Are you sure?")
                .setContentText("Are you sure want Cancel this Invoice ?")
                .setConfirmText("YES")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        try {
                            sDialog.dismiss();
                            viewCloseBottomSheet();
                            String date = Utils.changeDateFormat(invoiceDate);
                            setDeleteInvoice(InvoiceId, invoiceStatusValue, date);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .showCancelButton(true)
                .setCancelText("No")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                }).show();
    }

    public static void showInvoiceOption(String invoiceCustomerCode, String customerName, String invoiceNum, String invoiceStatus, String date) {
        Log.w("InvoiceStatusView:", invoiceStatus);
        invStatus = invoiceStatus;
        customerCodeStr = invoiceCustomerCode;
        customerNameStr = customerName;

        isSearchCustomerNameClicked = false;
        // searchFilterView.setVisibility(View.GONE);
        if (invoiceStatus.equals("Paid") || visibleFragment.equals("paid")) {
            //   editInvoiceLayout.setVisibility(View.GONE);
            cashCollectionLayout.setVisibility(View.GONE);
            deleteInvoiceLayout.setVisibility(View.GONE);
            invoiceStatusValue = "P";

        } else if (invoiceStatus.equals("Partial")) {
            //   editInvoiceLayout.setVisibility(View.GONE);
            cashCollectionLayout.setVisibility(View.VISIBLE);
            deleteInvoiceLayout.setVisibility(View.GONE);
            invoiceStatusValue = "PR";
        } else if (invoiceStatus.equals("Open") || invoiceStatus.equals("O")) {
            // editInvoiceLayout.setVisibility(View.VISIBLE);
            cashCollectionLayout.setVisibility(View.VISIBLE);
            duplicateInvoiceLayout.setVisibility(View.VISIBLE);
            invoiceStatusValue = "O";
            // deleteInvoiceLayout.setVisibility(View.VISIBLE);
        }
        invoiceCustomerCodeValue = invoiceCustomerCode;
        invoiceCustomerValue = customerName;
        invoiceNumberValue = invoiceNum;
        invoiceDate = date;
        customerLayout.setVisibility(View.GONE);
        invoiceOptionLayout.setVisibility(View.VISIBLE);
        invoiceNumber.setText(invoiceNum);
        invoiceCustomerName.setText(invoiceCustomerValue);

        viewCloseBottomSheet();
    }

    private void getInvoiceDetails(String invoiceNumber) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject = new JSONObject();
        ///jsonObject.put("CompanyCode",companyId);
        jsonObject.put("InvoiceNo", invoiceNumber);
        jsonObject.put("LocationCode", locationCode);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "InvoiceDetails";
        // Initialize a new JsonArrayRequest instance
        Log.w("invoicDetail",""+url+jsonObject);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Getting Invoice Details...");
        pDialog.setCancelable(false);
        pDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        Log.w("Invoice_DetailsSAP:", response.toString());
                        if (response.length() > 0) {

                            // {"statusCode":1,"statusMessage":"Success","responseData":[{"customerCode":"WinApp","customerName":"WinApp",
                            // "invoiceNumber":"41","invoiceStatus":"O","invoiceDate":"6\/8\/2021 12:00:00 am","netTotal":"26.750000",
                            // "balanceAmount":"26.750000","totalDiscount":"0.000000","paidAmount":"0.000000","contactPersonCode":"",
                            // "createDate":"13\/8\/2021 12:00:00 am","updateDate":"13\/8\/2021 12:00:00 am","remark":"",
                            // "fDocTotal":"0.000000","fTaxAmount":"0.000000","receivedAmount":"0.000000","total":"26.750000",
                            // "fTotal":"0.000000","iTotalDiscount":"0.000000","taxTotal":"1.750000","iPaidAmount":"0.000000",
                            // "currencyCode":"SGD","currencyName":"Singapore Dollar","companyCode":"WINAPP_DEMO","docEntry":"28",
                            // "taxPer":null,"discountPercentage":null,"subTotal":"25.000000","taxType":"I","taxCode":"","taxPerc":"",
                            // "billDiscount":"0.000000",
                            //
                            // "invoiceDetails":[{"slNo":"1","companyCode":"WINAPP_DEMO","invoiceNo":"41",
                            // "productCode":"FG\/001245","productName":"Milk","quantity":"5.000000","price":"5.000000","currency":"SGD",
                            // "taxRate":"0.000000","discountPercentage":"0.000000","lineTotal":"26.750000","fRowTotal":"0.000000",
                            // "warehouseCode":"01","salesEmployeeCode":"-1","accountCode":"400000","taxStatus":"Y","unitPrice":"5.000000",
                            // "customerCategoryNo":"","barCodes":"","totalTax":"1.750000","fTaxAmount":"0.000000","taxCode":"SR",
                            // "taxType":"Y","taxPerc":"0.000000","uoMCode":null,"invoiceDate":"6\/8\/2021 12:00:00 am",
                            // "dueDate":"6\/8\/2021 12:00:00 am","createDate":"13\/8\/2021 12:00:00 am","updateDate":"13\/8\/2021 12:00:00 am",
                            // "createdUser":"manager","uomCode":"Ctn","uoMName":"Carton","cartonPrice":"3000.000000","piecePrice":"0.000000",
                            // "pcsPerCarton":"100.000000","lPrice":"100.000000","unitQty":"1.000000","retailPrice":"100.000000",
                            // "cartonQty":"1.000000","netTotal":"25.000000","subTotal":"25.00000000000","purchaseTaxPerc":"0.000000",
                            // "purchaseTaxRate":"7.000000","taxAmount":"1.750000","purchaseTaxCode":"SR","total":"26.750000",
                            // "itemDiscount":"0.000000"}]}]}

                            String statusCode = response.optString("statusCode");
                            if (statusCode.equals("1")) {
                                JSONArray salesArray = response.optJSONArray("responseData");
                                JSONObject salesObject = salesArray.optJSONObject(0);
                                String invoice_number = salesObject.optString("invoiceNumber");
                                String invoice_code = salesObject.optString("code");
                                String company_code = salesObject.optString("CompanyCode");
                                String customer_code = salesObject.optString("customerCode");
                                String customer_name = salesObject.optString("customerName");
                                String total = salesObject.optString("total");
                                String sub_total = salesObject.optString("subTotal");
                                String bill_discount = salesObject.optString("billDiscount");
                                String item_discount = salesObject.optString("ItemDiscount");
                                Utils.setInvoiceOutstandingAmount(salesObject.optString("balanceAmount"));
                                String tax = salesObject.optString("taxTotal");
                                String net_total = salesObject.optString("netTotal");
                                String currency_rate = salesObject.optString("CurrencyRate");
                                String currency_name = salesObject.optString("currencyName");
                                String tax_type = salesObject.optString("taxType");
                                String tax_perc = salesObject.optString("taxPerc");
                                String tax_code = salesObject.optString("taxCode");
                                String phone_no = salesObject.optString("DelPhoneNo");
                                String so_date = salesObject.optString("soDate");

                                dbHelper.removeCustomer();
                                dbHelper.insertCustomer(
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
                                        salesObject.optString("currencyCode"));
                                dbHelper.removeAllItems();

                                dbHelper.removeCustomerTaxes();
                                CustomerDetails model = new CustomerDetails();
                                model.setCustomerCode(customer_code);
                                model.setCustomerName(customer_name);
                                model.setCustomerAddress1(salesObject.optString("address1"));
                                model.setTaxPerc(salesObject.optString("taxPercentage"));
                                model.setTaxType(salesObject.optString("taxType"));
                                model.setTaxCode(salesObject.optString("taxCode"));

                                ArrayList<CustomerDetails> taxList = new ArrayList<>();
                                taxList.add(model);
                                dbHelper.insertCustomerTaxValues(taxList);

                                JSONArray products = salesObject.getJSONArray("invoiceDetails");
                                for (int i = 0; i < products.length(); i++) {
                                    JSONObject object = products.getJSONObject(i);

                                    //  "salesOrderDetails":[{"slNo":"1","companyCode":"WINAPP_DEMO",
                                    //  "soNo":"8","productCode":"FG\/001245","productName":"Milk","quantity":"5.000000","cartonQty":"1.000000",
                                    //  "price":"99.000000","currency":"SGD","taxRate":"0.000000","discountPercentage":"1.000000","lineTotal":"529.650000",
                                    //  "fRowTotal":"0.000000","warehouseCode":"01","salesEmployeeCode":"-1","accountCode":"400000","taxStatus":"Y",
                                    //  "unitPrice":"100.000000","customerCategoryNo":"","barCodes":"","totalTax":"34.650000","fTaxAmount":"0.000000",
                                    //  "taxCode":"SR","taxType":"E","taxPerc":"0.000000","uoMCode":null,"soDate":"12\/8\/2021 12:00:00 am",
                                    //  "dueDate":"12\/8\/2021 12:00:00 am","createDate":"12\/8\/2021 12:00:00 am","updateDate":"12\/8\/2021 12:00:00 am",
                                    //  "createdUser":"manager","uomCode":"Ctn","uoMName":"Carton","cartonPrice":"3000.000000","piecePrice":"0.000000",
                                    //  "pcsPerCarton":"100.000000","lPrice":"100.000000","unitQty":"1.000000","retailPrice":"100.000000",
                                    //  "netTotal":"495.000000","subTotal":"500.00000000000","purchaseTaxPerc":"1.000000","purchaseTaxRate":"7.000000",
                                    //  "taxAmount":"34.650000","purchaseTaxCode":"SR","total":"529.650000","itemDiscount":"5.000000"}]}]}

                                    String lqty = "0.0";
                                    String cqty = "0.0";
                                    if (!object.optString("unitQty").equals("null")) {
                                        lqty = object.optString("unitQty");
                                    }

                                    if (!object.optString("quantity").equals("null")) {
                                        cqty = object.optString("quantity");
                                    }
//                                    if (object.optString("bP_CatalogNo") != null) {
//                                        invoiceListModel.setCustomerItemCode(object.optString("bP_CatalogNo"));
//                                    }

                                    double actualPrice = Double.parseDouble(object.optString("unitPrice"));

                                    if (createInvoiceSetting.equals("true")) {

                                      /*  double priceValue=0.0;
                                        double net_qty=Double.parseDouble(qty_value) - Double.parseDouble(return_qty);

                                        double return_amt=(Double.parseDouble(return_qty)*Double.parseDouble(price_value));
                                        double total=(net_qty * Double.parseDouble(price_value));
                                        double sub_total=total-return_amt-Double.parseDouble(discount);


                                        boolean insertStatus=dbHelper.insertCreateInvoiceCart(
                                                productId.toString().trim(),
                                                productName,
                                                qty_value,
                                                return_qty,
                                                String.valueOf(net_qty),
                                                foc,
                                                price_value,
                                                String.valueOf(total),
                                                subTotalValue.getText().toString(),
                                                taxValueText.getText().toString(),
                                                netTotalValue.getText().toString()
                                        );*/

                                    } else {
                                        dbHelper.insertCart(
                                                object.optString("productCode"),
                                                object.optString("productName"),
                                                cqty,
                                                "0",
                                                object.optString("subTotal"),
                                                "",
                                                object.optString("lineTotal"),
                                                "weight",
                                                String.valueOf(actualPrice),
                                                object.optString("subTotal"),
                                                object.optString("pcsPerCarton"),
                                                object.optString("totalTax"),
                                                object.optString("subTotal"),
                                                object.optString("taxType"),
                                                object.optString("FOCQty"),
                                                "",
                                                object.optString("ExchangeQty"),
                                                "",
                                                "0.00",
                                                object.optString("ReturnQty"),
                                                "", "", object.optString("total"),
                                                object.optString("stockInHand"),
                                                object.optString("uomCode"),
                                                object.optString("minimumSellingPrice"),
                                                object.optString("stockInHand")
                                        );
                                    }


                                    int count = dbHelper.numberOfRows();
                                    if (products.length() == count) {
                                        Utils.setCustomerSession(NewInvoiceListActivity.this, customer_code);
                                        if (createInvoiceSetting.equals("true")) {
                                            Intent intent = new Intent(getApplicationContext(), CreateNewInvoiceActivity.class);
                                            intent.putExtra("from", "invoice");
                                            intent.putExtra("customerName", customer_name);
                                            intent.putExtra("customerCode", customer_code);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Intent intent = new Intent(NewInvoiceListActivity.this, AddInvoiceActivityOld.class);
                                            intent.putExtra("billDiscount", bill_discount);
                                            intent.putExtra("itemDiscount", item_discount);
                                            intent.putExtra("subTotal", sub_total);
                                            intent.putExtra("customerId", customer_code);
                                            intent.putExtra("invoiceNumber", invoice_code);
                                            intent.putExtra("activityFrom", "InvoiceEdit");
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                                int count = dbHelper.numberOfRows();
                                if (count == 0) {
                                    Toast.makeText(getApplicationContext(), "No Products Details Found,Try again!", Toast.LENGTH_SHORT).show();
                                }

                            } else {

                            }
                        }

                     /*   Log.w("Invoice_Details:",response.toString());
                        if (response.length()>0){
                            String invoice_no=response.optString("InvoiceNo");
                            String company_code=response.optString("CompanyCode");
                            String customer_code=response.optString("CustomerCode");
                            String customer_name=response.optString("CustomerName");
                            String total=response.optString("Total");
                            String sub_total=response.optString("SubTotal");
                            String tax=response.optString("Tax");
                            String net_total=response.optString("NetTotal");
                            String bill_discount=response.optString("BillDIscount");
                            String item_discount=response.optString("ItemDiscount");
                            String currency_rate=response.optString("CurrencyRate");
                            String currency_name=response.optString("CurrencyName");
                            String tax_type=response.optString("TaxType");
                            String tax_perc=response.optString("TaxPerc");
                            String tax_code=response.optString("TaxCode");
                            String phone_no=response.optString("DelPhoneNo");
                            String deliveryCode=response.optString("DeliveryCode");
                            SettingUtils.setDeliveryAddressCode(deliveryCode);
                            dbHelper.removeCustomer();
                            dbHelper.insertCustomer(
                                    customer_code,
                                    customer_name,
                                    phone_no,
                                    response.optString("Address1"),
                                    response.optString("Address2"),
                                    response.optString("Address3"),
                                    response.optString("IsActive"),
                                    response.optString("HaveTax"),
                                    response.optString("TaxType"),
                                    response.optString("TaxPerc"),
                                    response.optString("TaxCode"),
                                    response.optString("CreditLimit"),
                                    "Singapore",
                                    response.optString("CurrencyCode"));
                            customerDetails=dbHelper.getCustomer();

                            dbHelper.removeAllItems();
                            JSONArray products=response.getJSONArray("InvoiceDetails");
                            for (int i=0;i<products.length();i++){
                                JSONObject object=products.getJSONObject(i);

                                String retail_price="0.0";
                                String unit_price="0.0";
                                if (!object.optString("RetailPrice").equals("null")){
                                    retail_price=object.optString("RetailPrice");
                                }

                                if (!object.optString("UnitCost").equals("null")){
                                    unit_price=object.optString("UnitCost");
                                }
                                dbHelper.insertCart(
                                        object.optString("ProductCode"),
                                        object.optString("ProductName"),
                                        object.optString("CQty"),
                                        object.optString("LQty"),
                                        object.optString("SubTotal"),
                                        "",
                                        object.optString("NetTotal"),
                                        "weight",
                                        object.optString("CartonPrice"),
                                        object.optString("Price"),
                                        object.optString("PcsPerCarton"),
                                        object.optString("Tax"),
                                        object.optString("SubTotal"),
                                        object.optString("TaxType"),
                                        object.optString("FOCQty"),
                                        "",
                                        object.optString("ExchangeQty"),
                                        "",
                                        object.optString("ItemDiscount"),
                                        object.optString("ReturnQty"),
                                        "","",object.optString("Total"),"",object.optString("UOMCode"));

                                int count=dbHelper.numberOfRows();
                                if (products.length()==count){
                                    Utils.setCustomerSession(NewInvoiceListActivity.this,customer_code);
                                    Intent intent=new Intent(NewInvoiceListActivity.this,AddInvoiceActivity.class);
                                    intent.putExtra("billDiscount",bill_discount);
                                    intent.putExtra("itemDiscount",item_discount);
                                    intent.putExtra("subTotal",sub_total);
                                    intent.putExtra("customerId",customer_code);
                                    intent.putExtra("invoiceNumber",invoice_no);
                                    intent.putExtra("activityFrom","InvoiceEdit");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                            int count=dbHelper.numberOfRows();
                            if (count==0){
                                Toast.makeText(getApplicationContext(),"No Products Details Found,Try again!",Toast.LENGTH_SHORT).show();
                            }
                        }*/


                        pDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            pDialog.dismiss();
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

    private void getInvoiceEditDetails(String invoiceNo) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("InvoiceNo",invoiceNo);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //    EditSODetails
        //    EditSODetailsWithFOC
        String url= Utils.getBaseUrl(this) +"DuplicateInvoiceDetails";
        Log.w("JsonValue:",jsonObject.toString());
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_duplicat:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Getting Invoice Details...");
        pDialog.setCancelable(false);
        pDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try{
                        Log.w("inv_duplicat_edit:",response.toString());
                        if (response.length()>0) {
                            String statusCode = response.optString("statusCode");
                            if (statusCode.equals("1")) {
                                JSONArray salesArray = response.optJSONArray("responseData");
                                if (salesArray.length() > 0) {
                                    JSONObject salesObject = salesArray.optJSONObject(0);
                                    String invoice_no = salesObject.optString("invoiceNumber");
                                    String company_code = salesObject.optString("CompanyCode");
                                    String customer_code = salesObject.optString("customerCode");
                                    String customer_name = salesObject.optString("customerName");
                                    String total = salesObject.optString("total");
                                    String inv_date = salesObject.optString("invoiceDate");
                                    String sub_total = salesObject.optString("subTotal");
                                    String bill_discount = salesObject.optString("billDiscount");
                                    String item_discount = salesObject.optString("ItemDiscount");
                                    String tax = salesObject.optString("taxTotal");
                                    String customerBill_Disc=salesObject.optString("customerDiscountPercentage");
                                   // String customerBill_Disc=salesObject.optString("billDiscountPercentage");
                                    String net_total = salesObject.optString("netTotal");
                                    String currency_rate = salesObject.optString("CurrencyRate");
                                    String currency_name = salesObject.optString("currencyName");
                                    String tax_type = salesObject.optString("taxType");
                                    String tax_perc = salesObject.optString("taxPerc");
                                    String tax_code = salesObject.optString("taxCode");
                                    String phone_no = salesObject.optString("DelPhoneNo");
                                    String so_date = salesObject.optString("soDate");
                                    String signFlag = salesObject.optString("signFlag");
                                    Utils.setInvoiceMode("Invoice");
                                    if (signFlag.equals("Y")) {
                                        String signature = salesObject.optString("signature");
                                        Utils.setSignature(signature);
                                    }

                                    dbHelper.removeCustomer();
                                    dbHelper.insertCustomer(
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
                                            "",
                                            "Singapore",
                                            salesObject.optString("currencyCode"));
                                    customerDetails = dbHelper.getCustomer();
                                    dbHelper.removeAllItems();
                                    dbHelper.removeAllInvoiceItems();
                                    dbHelper.removeCustomerTaxes();
                                    CustomerDetails model = new CustomerDetails();
                                    model.setCustomerCode(customer_code);
                                    model.setCustomerName(customer_name);
                                    model.setCustomerAddress1(salesObject.optString("address1"));
                                    model.setTaxPerc(salesObject.optString("taxPerc"));
                                    model.setTaxType(salesObject.optString("taxType"));
                                    model.setTaxCode(salesObject.optString("taxCode"));
                                    ArrayList<CustomerDetails> taxList = new ArrayList<>();
                                    taxList.add(model);
                                    Log.w("TaxModelPrint::", model.toString());
                                    dbHelper.insertCustomerTaxValues(taxList);

                                    JSONArray products = salesObject.getJSONArray("invoiceDetails");
                                    for (int i = 0; i < products.length(); i++) {
                                        JSONObject object = products.getJSONObject(i);
                                        String lqty = "0.0";
                                        String cqty = "0.0";
                                        if (!object.optString("unitQty").equals("null")) {
                                            lqty = object.optString("unitQty");
                                        }

                                        if (!object.optString("quantity").equals("null")) {
                                            cqty = object.optString("quantity");
                                        }
                                        double priceValue = 0.0;
                                        String return_qty = "0";
                                        double net_qty = Double.parseDouble(cqty) - Double.parseDouble(return_qty);
                                        String price_value = object.optString("price");
                                        //String price_value=object.optString("grossPrice");

                                        double return_amt = (Double.parseDouble(return_qty) * Double.parseDouble(price_value));
                                        double total1 = (net_qty * Double.parseDouble(price_value));
                                        double sub_total1 = total1 - return_amt;

                                        long laterDate = System.currentTimeMillis();
                                        int millisec = 18000;
                                        Timestamp original = new Timestamp(laterDate);
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTimeInMillis(original.getTime());
                                        cal.add(Calendar.MILLISECOND, millisec);
                                        Timestamp timeStamp = new Timestamp(cal.getTime().getTime());

                                        dbHelper.insertCreateInvoiceCartEdit(
                                                object.optString("productCode"),
                                                object.optString("productName"),
                                                object.optString("uomCode"),
                                                cqty.toString(),
                                                return_qty,
                                                String.valueOf(net_qty),
                                                object.optString("foc_Qty"),
                                                price_value,
                                                object.optString("stockInHand"),
                                                object.optString("total"),
                                                object.optString("subTotal"),
                                                object.optString("priceWithGST"),
                                                object.optString("netTotal"),
                                                object.optString("itemDiscount"),
                                                salesObject.optString("billDiscount"),
                                                "",
                                                "",
                                                object.optString("exc_Qty"),
                                                salesObject.optString("minimumSellingPrice"),
                                                object.optString("stockInHand"), String.valueOf(timeStamp),
                                                object.optString("itemAllowFOC")
                                                );

                                        myEdit.putString("billDisc_amt", salesObject.optString("billDiscount"));
                                        myEdit.putString("billDisc_percent", salesObject.optString("discountPercentage"));
                                        myEdit.apply();

                                        Log.w("ProductsLength:", products.length() + "");
                                        Log.w("ActualPrintProducts:", dbHelper.numberOfRowsInInvoice() + "");
                                        if (products.length() == dbHelper.numberOfRowsInInvoice()) {
                                            redirectActivity(customer_code, customer_name, invoiceNo
                                                    ,customerBill_Disc,inv_date);
                                            break;
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "no data found", Toast.LENGTH_LONG).show();
                                    pDialog.dismiss();
                                }
                            }
                            else {
                                pDialog.dismiss();
                            }
                            pDialog.dismiss();
                        }
                        pDialog.dismiss();
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


    public void redirectActivity(String customer_code, String customer_name, String inv_code ,
                                 String customerBill_Disc,String inv_date) {
        //  if (products.length()==dbHelper.numberOfRowsInInvoice()){

          Utils.setCustomerSession(NewInvoiceListActivity.this, customer_code);
       // setCustomerDetails(customer_code);
        Intent intent = new Intent(getApplicationContext(), CreateNewInvoiceActivity.class);
        intent.putExtra("customerCode", customer_code);
        intent.putExtra("duplicateInvNo", inv_code);
        intent.putExtra("duplicateInvDate", inv_date);
        intent.putExtra("customerName", customer_name);
       // intent.putExtra("customerBillDisc", customerBill_Disc);
        intent.putExtra("from", "Duplicate");
        startActivity(intent);
        finish();

    }

    private void setDeleteInvoice(String invoiceNumber, String status, String invoiceDate) throws JSONException {
        // Initialize a new RequestQueue instance

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("InvoiceStatus", status);
        jsonObject.put("InvoiceNo", invoiceNumber);
        jsonObject.put("InvoiceDate", invoiceDate);
        Log.w("GivenInputRequest::", jsonObject.toString());
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "CancellationDocument";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:", url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Cancelling Invoice...");
        pDialog.setCancelable(false);
        pDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        Log.w("DelInvoice_Response_is:", response.toString());
                        //  {"statusCode":1,"statusMessage":"Invoice Cancelled Successfully","responseData":{"docNum":"8010025","error":null}}
                        // {"IsSaved":false,"Result":"pass","IsDeleted":true}
                        if (response.length() > 0) {
                            String statusCode = response.optString("statusCode");
                            String statusMessage = response.optString("statusMessage");
                            if (statusCode.equals("1") && statusMessage.equals("Invoice Cancelled Successfully")) {
                                Toast.makeText(getApplicationContext(), "Invoice Cancelled Successfully...!", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(getIntent());
                                dbHelper.removeAllProducts();
                            } else {
                                JSONObject object = response.getJSONObject("responseData");
                                String error = object.optString("error");
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                            }
                        }
                        pDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            pDialog.dismiss();
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


    public void setNettotal(ArrayList<InvoiceModel> invoiceList) {
        double net_amount = 0.0;
        for (InvoiceModel model : invoiceList) {
            if (model.getBalance() != null && !model.getBalance().equals("null")) {
                net_amount = net_amount + Double.parseDouble(model.getBalance());
            }
        }
        netTotalText.setText("$ " + Utils.twoDecimalPoint(net_amount));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sorting_menu, menu);
        this.menu = menu;
        MenuItem item = menu.findItem(R.id.action_barcode);
        item.setVisible(false);

        MenuItem addInvoice = menu.findItem(R.id.action_add);
        addInvoice.setVisible(false);
        if (createInvoiceSetting.equals("true")) {
            if (company_name.equals("AADHI INTERNATIONAL PTE LTD")) {
                addInvoice.setVisible(false);
            } else {
                addInvoice.setVisible(true);
            }
        } else {
            addInvoice.setVisible(false);
        }
      /*  ArrayList<UserRoll> userRolls=helper.getUserPermissions();
        if (userRolls.size()>0) {
            for (UserRoll roll : userRolls) {
                if (roll.getFormName().equals("Add Invoice")){
                    if (roll.getHavePermission().equals("true")){
                       // addInvoice.setVisible(true);
                        if (createInvoiceSetting=="true"){
                            addInvoice.setVisible(true);
                        }else {
                            addInvoice.setVisible(false);
                        }
                    }else {
                        addInvoice.setVisible(true);
                    }
                }
            }
        }*/
        this.menu = menu;
        MenuItem filter = menu.findItem(R.id.action_filter);
        if (visibleFragment.equals("invoices")) {
            filter.setVisible(true);
            //filter.setVisible(false);
        } else {
            filter.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//finish();
            onBackPressed();

         /*   case R.id.action_remove:
                showRemoveAlert();
                break;*/
        } else if (item.getItemId() == R.id.action_customer_name) {
            Collections.sort(invoiceList, new Comparator<InvoiceModel>() {
                public int compare(InvoiceModel obj1, InvoiceModel obj2) {
                    // ## Ascending order
                    return obj1.getName().compareToIgnoreCase(obj2.getName()); // To compare string values
                    // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                    // ## Descending order
                    // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                    // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                }
            });
            invoiceAdapter.notifyDataSetChanged();
        } else if (item.getItemId() == R.id.action_amount) {
            Collections.sort(invoiceList, new Comparator<InvoiceModel>() {
                public int compare(InvoiceModel obj1, InvoiceModel obj2) {
                    // ## Ascending order
                    //  return obj1.getNetTotal().compareToIgnoreCase(obj2.getNetTotal()); // To compare string values
                    return Double.valueOf(obj1.getNetTotal()).compareTo(Double.valueOf(obj2.getNetTotal())); // To compare integer values

                    // ## Descending order
                    // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                    // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                }
            });
            invoiceAdapter.notifyDataSetChanged();
        } else if (item.getItemId() == R.id.action_date) {

            try {
                Collections.sort(invoiceList, new Comparator<InvoiceModel>() {
                    public int compare(InvoiceModel obj1, InvoiceModel obj2) {
                        SimpleDateFormat sdfo = new SimpleDateFormat("yyyy-MM-dd");
                        // Get the two dates to be compared
                        Date d1 = null;
                        Date d2 = null;
                        try {
                            d1 = sdfo.parse(obj1.getDate());
                            d2 = sdfo.parse(obj2.getDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // ## Ascending order
                        //  return obj1.getNetTotal().compareToIgnoreCase(obj2.getNetTotal()); // To compare string values
                        return d1.compareTo(d2); // To compare integer values

                        // ## Descending order
                        // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                        // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                    }
                });
                invoiceAdapter.notifyDataSetChanged();
            } catch (Exception ex) {
                Log.w("Error:", ex.getMessage());
            }
        } else if (item.getItemId() == R.id.action_add) {

            Intent intent = new Intent(getApplicationContext(), CustomerListActivity.class);
            intent.putExtra("from", "iv");
            startActivityForResult(intent, customerSelectCode);
           /* SharedPreferences sharedPreferences = getSharedPreferences("customerPref",MODE_PRIVATE);
            String selectCustomerId = sharedPreferences.getString("customerId", "");
            if (selectCustomerId!=null && !selectCustomerId.isEmpty()){
                customerDetails=dbHelper.getCustomer(selectCustomerId);
                if (customerDetails.size()>0){
                    showCustomerDialog(this,customerDetails.get(0).getCustomerName(),customerDetails.get(0).getCustomerCode(),customerDetails.get(0).getCustomerAddress1());
                }else {
                    customerLayout.setVisibility(View.VISIBLE);
                    invoiceOptionLayout.setVisibility(View.GONE);
                    customerNameText.setText("");
                    searchFilterView.setVisibility(View.GONE);
                    isSearchCustomerNameClicked=false;
                    //  viewCloseBottomSheet();
                    if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            }else {
                    customerLayout.setVisibility(View.VISIBLE);
                    invoiceOptionLayout.setVisibility(View.GONE);
                    customerNameText.setText("");
                    searchFilterView.setVisibility(View.GONE);
                    isSearchCustomerNameClicked=false;
                    //  viewCloseBottomSheet();
                    if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
            }*/
        } else if (item.getItemId() == R.id.action_filter) {
            if (searchFilterView.getVisibility() == View.VISIBLE) {
                searchFilterView.setVisibility(View.GONE);
                customerNameText.setText("");
                isSearchCustomerNameClicked = false;
                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                //slideUp(searchFilterView);
            } else {
                customerNameText.setText("");
                isSearchCustomerNameClicked = false;
                searchFilterView.setVisibility(View.VISIBLE);
                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                // slideDown(searchFilterView);
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == customerSelectCode) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("customerCode");
                Utils.setCustomerSession(this, result);
                Intent intent = new Intent(NewInvoiceListActivity.this, AddInvoiceActivityOld.class);
                intent.putExtra("customerId", result);
                intent.putExtra("activityFrom", "Invoice");
                startActivity(intent);
                //finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // Write your code if there's no result
            }
        } else if (requestCode == FILTER_CUSTOMER_CODE && resultCode == Activity.RESULT_OK) {
            selectCustomerCode = data.getStringExtra("customerCode");
            selectCustomerName = data.getStringExtra("customerName");
            customerNameText.setText(selectCustomerName);
            Log.w("CustomerCode:", selectCustomerCode);
        }
    } //onActivityResult

    private void showCustomerDialog(Activity activity, String customer_name, String customer_code, String desc) {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.cutom_alert_dialog, viewGroup, false);

        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        TextView customerName = dialogView.findViewById(R.id.customer_name_value);
        TextView description = dialogView.findViewById(R.id.description);

        customerName.setText(customer_name + " - " + customer_code);
        description.setText("Do you want to continue this customer ?");

        Button yesButton = dialogView.findViewById(R.id.buttonYes);
        Button noButton = dialogView.findViewById(R.id.buttonNo);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                dbHelper.removeCustomer();
                dbHelper.removeAllItems();
                setCustomerDetails(customer_code);
                AddInvoiceActivityOld.customerId = customer_code;
                Intent intent = new Intent(NewInvoiceListActivity.this, AddInvoiceActivityOld.class);
                intent.putExtra("customerId", customer_code);
                intent.putExtra("activityFrom", "Invoice");
                startActivity(intent);
                finish();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isSearchCustomerNameClicked = false;
                customerLayout.setVisibility(View.VISIBLE);
                invoiceOptionLayout.setVisibility(View.GONE);
                //   viewCloseBottomSheet();
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }

    public void setCustomerDetails(String customerId) {
        SharedPreferences sharedPreferences = getSharedPreferences("customerPref", MODE_PRIVATE);
        SharedPreferences.Editor customerPredEdit = sharedPreferences.edit();
        customerPredEdit.putString("customerId", customerId);
        customerPredEdit.apply();
    }


    @Override
    public void onBackPressed() {
        //Execute your code here
        //Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        // startActivity(intent);
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            finish();
        }
    }

    private class GetCustomersTask extends AsyncTask<Void, Integer, String> {
        String TAG = getClass().getSimpleName();

        protected void onPreExecute() {
            super.onPreExecute();
            customerList = new ArrayList<>();
            emptyTextView.setText("Customers List loading please wait...");
        }

        protected String doInBackground(Void... arg0) {
            Log.d(TAG + " DoINBackGround", "On doInBackground...");
            // Initialize a new RequestQueue instance
            RequestQueue requestQueue = Volley.newRequestQueue(NewInvoiceListActivity.this);
            String url = Utils.getBaseUrl(NewInvoiceListActivity.this) + "MasterApi/GetCustomer_All?Requestdata={CompanyCode:" + companyCode + "}";
            Log.w("Given_url:", url);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                    url, null,
                    response -> {
                        try {
                            // pDialog.dismiss();
                            // Loop through the array elements
                            Log.w("Customer_Response:", response.toString());
                            for (int i = 0; i < response.length(); i++) {
                                // Get current json object
                                JSONObject customerObject = response.getJSONObject(i);
                                CustomerModel model = new CustomerModel();
                                model.setCustomerCode(customerObject.optString("CustomerCode"));
                                model.setCustomerName(customerObject.optString("CustomerName"));
                                model.setCustomerAddress(customerObject.optString("Address1"));
                                model.setHaveTax(customerObject.optString("HaveTax"));
                                model.setTaxType(customerObject.optString("TaxType"));
                                model.setTaxPerc(customerObject.optString("TaxPerc"));
                                model.setTaxCode(customerObject.optString("TaxCode"));
                                if (customerObject.optString("BalanceAmount").equals("null") || customerObject.optString("BalanceAmount").isEmpty()) {
                                    model.setOutstandingAmount("0.00");
                                } else {
                                    model.setOutstandingAmount(customerObject.optString("BalanceAmount"));
                                }
                                customerList.add(model);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {

                // Do something when error occurred
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
            jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
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
            requestQueue.add(jsonArrayRequest);
            return "You are at PostExecute";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (customerList.size() > 0) {
                emptyTextView.setVisibility(View.GONE);
                customerView.setVisibility(View.VISIBLE);
                dbHelper.removeAllCustomers();
                dbHelper.insertCustomerList(customerList);
                setAdapter(customerList);
            } else {
                customerView.setVisibility(View.GONE);
                emptyTextView.setText("No Customer found..");
                emptyTextView.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }

        }
    }

    public void getCustomers() {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "CustomerList";
        customerList = new ArrayList<>();
        emptyTextView.setText("Customers List loading please wait...");
        Log.w("Given_url:", url);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET,
                url, null,
                response -> {
                    try {
                        // pDialog.dismiss();
                        // Loop through the array elements
                        Log.w("Customer_Response:", response.toString());
                        String statusCode = response.optString("statusCode");
                        if (statusCode.equals("1")) {
                            JSONArray customerDetailArray = response.optJSONArray("responseData");
                            for (int i = 0; i < customerDetailArray.length(); i++) {
                                JSONObject object = customerDetailArray.optJSONObject(i);
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
                                model.setBillDiscPercentage(object.optString("discountPercentage"));

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
                        } else {
                            Toast.makeText(getApplicationContext(), "Error,in getting Customer list", Toast.LENGTH_LONG).show();
                        }

                        if (customerList.size() > 0) {
                            emptyTextView.setVisibility(View.GONE);
                            customerView.setVisibility(View.VISIBLE);
                            //new InsertCustomerTask().execute();
                            setAdapter(customerList);
                        } else {
                            customerView.setVisibility(View.GONE);
                            emptyTextView.setText("No Customer found..");
                            emptyTextView.setVisibility(View.VISIBLE);
                            progressLayout.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {

            // Do something when error occurred
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
        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
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
        requestQueue.add(jsonArrayRequest);
    }

    class InsertCustomerTask extends AsyncTask<Void, Integer, String> {
        String TAG = getClass().getSimpleName();

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(Void... arg0) {
            Log.d(TAG + " DoINBackGround", "On doInBackground...");
            dbHelper.removeAllCustomers();
            dbHelper.insertCustomerList(customerList);
            return "You are at PostExecute";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    private void setAdapter(ArrayList<CustomerModel> customerNames) {
        progressLayout.setVisibility(View.GONE);
        customerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        customerNameAdapter = new SelectCustomerAdapter(this, customerNames, new SelectCustomerAdapter.CallBack() {
            @Override
            public void searchCustomer(String customer, String customername, int pos) {
                customerNameEdittext.setText("");
                setAdapter(customerList);
                // customerNameAdapter.notifyDataSetChanged();
                if (isSearchCustomerNameClicked) {
                    // viewCloseBottomSheet();
                    //  searchFilterView.setVisibility(View.GONE);
                    if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    setCustomerDetails(customer);
                    customerNameText.setText(customername);
                    selectCustomerId = customer;
                    selectCustomerName = customername;
                    //  redirectInvoice=true;
                } else {
                    int count = dbHelper.numberOfRows();
                    if (count > 0) {
                        showProductDeleteAlert(customer);
                    } else {
                        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else {
                            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        dbHelper.removeAllItems();
                        setCustomerDetails(customer);
                        selectCustomerId = customer;
                        redirectInvoice = true;
                       /* Intent intent=new Intent(NewInvoiceListActivity.this,AddInvoiceActivity.class);
                        intent.putExtra("customerId",customer);
                        intent.putExtra("activityFrom","Invoice");
                        startActivity(intent);
                        finish();*/
                    }
                }
            }
        });
        customerView.setAdapter(customerNameAdapter);
        totalCustomers.setText(customerNames.size() + " Customers");
    }

    public void showProductDeleteAlert(String customerId) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Warning !");
        builder1.setMessage("Products in Cart will be removed..");
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else {
                            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        dialog.cancel();
                        setCustomerDetails(customerId);
                        dbHelper.removeAllItems();
                        selectCustomerId = customerId;
                        redirectInvoice = true;
                        //Intent intent=new Intent(NewInvoiceListActivity.this,AddInvoiceActivity.class);
                        // intent.putExtra("customerId",customerId);
                        // intent.putExtra("activityFrom","Invoice");
                        //startActivity(intent);
                        // finish();
                    }
                });
        builder1.setNegativeButton(
                "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public static void viewCloseBottomSheet() {
        //  hideKeyboard();
        pdfViewLayout.setVisibility(View.GONE);
        if (isSearchCustomerNameClicked) {
            customerLayout.setVisibility(View.VISIBLE);
            invoiceOptionLayout.setVisibility(View.GONE);
        } else {
            customerLayout.setVisibility(View.GONE);
            invoiceOptionLayout.setVisibility(View.VISIBLE);
        }
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        // get the Customer name from the local db
    }


    public void getInvoicePdf(String invoiceno, String mode) {

        try {
            // Initialize a new RequestQueue instance
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            // Initialize a new JsonArrayRequest instance
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("InvoiceNo", invoiceno);

            String url = Utils.getBaseUrl(this) + "DownloadPDFInvoice";

            Log.w("Given_url_PdfDownload:", url + "/" + jsonObject.toString());
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Generating Pdf...");
            pDialog.setCancelable(false);
            pDialog.show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonObject,
                    response -> {
                        try {
                            pDialog.dismiss();
                            Log.w("InvoicePdfResponse:", response.toString());
                            //    {"statusCode":1,"statusMessage":"Success",
                            //    "responseData":{"pdfURL":"http:\/\/172.16.5.60:8349\/PDF\/InvoiceNo_15031.pdf"}}
                            if (response.length() > 0) {
                                String statusCode = response.optString("statusCode");
                                if (statusCode.equals("1")) {
                                    JSONObject object = response.optJSONObject("responseData");
                                    String pdfUrl = object.optString("pdfURL");
                                    shareMode = mode;
                                    new InvoicePdfDownload(this).execute(pdfUrl, "invoice", invoiceno);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error in Getting report..", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {
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
        } catch (Exception ex) {
        }
    }

    public void viewPdfLayout(String invoiceNumber, ArrayList<InvoicePrintPreviewModel> invoiceDetails) {
        //pdfGenerateDialog.show();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                storagePath = Environment.getExternalStorageDirectory();
                invoiceNO = invoiceNumber;
                FILE = Environment.getExternalStorageDirectory() + "/" + invoiceNO + ".pdf";
                fatchinvoiceList(invoiceDetails);
                //createInvoicePdfTable(invoiceDetails);
            } else {
                requestPermission(); // Code for permission
            }
        } else {
            storagePath = Environment.getExternalStorageDirectory();
            invoiceNO = invoiceNumber;
            FILE = Environment.getExternalStorageDirectory() + "/" + invoiceNO + ".pdf";
            fatchinvoiceList(invoiceDetails);
            //createInvoicePdfTable(invoiceDetails);
        }
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        checkPermission(activity);
    }

    private static boolean checkPermission(Activity activity) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = Constants.getPdfFolderPath(NewInvoiceListActivity.this);
            File folder = new File(extStorageDirectory);
            if (!folder.exists()) {
                File productsDirectory = new File(Constants.getPdfFolderPath(NewInvoiceListActivity.this));
                productsDirectory.mkdirs();
            }
            try {
                File newFile = new File(folder, fileName);
                if (newFile.exists()) {
                    newFile.delete();
                }
                Log.w("NewFileName:", newFile.toString());
//                newFile.createNewFile();
                FileDownloader.downloadFile(fileUrl, newFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    private class InvoicePdfDownload extends AsyncTask<String, Integer, String> {

        private Context c;
        private int file_progress_count = 0;
        File newFile;

        public InvoicePdfDownload(Context c) {
            this.c = c;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream is = null;
            OutputStream os = null;
            HttpURLConnection con = null;
            int length;
            try {
                URL url = new URL(sUrl[0]);
                con = (HttpURLConnection) url.openConnection();
                con.connect();

                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "HTTP CODE: " + con.getResponseCode() + " " + con.getResponseMessage();
                }

                length = con.getContentLength();
                pd.setMax(length / (1000));
                is = con.getInputStream();

                Log.w("DownloadImageURL:", url.toString());

                //String folderPath = Environment.getExternalStorageDirectory() + "/CatalogErp/Products";
                File folder = new File(Constants.getFolderPath(NewInvoiceListActivity.this));
                if (!folder.exists()) {
                    File productsDirectory = new File(Constants.getFolderPath(NewInvoiceListActivity.this));
                    productsDirectory.mkdirs();
                }

                //create a new file
                String filepath = sUrl[1] + "_" + sUrl[2];
                String newfilePath = filepath.replace("/", "_");
                newFile = new File(Constants.getFolderPath(NewInvoiceListActivity.this), newfilePath + ".pdf");
                if (newFile.exists()) {
                    newFile.delete();
                }
                Log.w("GivenFilePath:", newFile.toString());
                newFile.createNewFile();
                //os = new FileOutputStream(Environment.getExternalStorageDirectory()+File.separator+"CatalogImages" + File.separator + "a-computer-engineer.jpg");
                os = new FileOutputStream(newFile);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = is.read(data)) != -1) {
                    if (isCancelled()) {
                        is.close();
                        return null;
                    }
                    total += count;
                    if (length > 0) {
                        publishProgress((int) total);
                    }
                    this.file_progress_count = (int) ((100 * total) / ((long) length));
                    os.write(data, 0, count);
                }
            } catch (Exception e) {
                Log.w("File_Write_Error:", e.getMessage());
                return e.toString();
            } finally {
                try {
                    if (os != null)
                        os.close();
                    if (is != null)
                        is.close();
                } catch (IOException ioe) {
                }
                if (con != null)
                    con.disconnect();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Downloading Pdf..");
            pd.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            pd.setIndeterminate(false);
            pd.setProgress(progress[0] / 1000);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                Log.w("ProgressCount:", file_progress_count + "");
                if (file_progress_count == 100) {
                    pd.dismiss();
                    if (newFile.exists()) {
                        if (shareMode.equals("Share")) {
                            pdfFile = newFile;
                            displayFromAsset(newFile);
                        } else {
                            shareWhatsapp(newFile);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "NO file Download", Toast.LENGTH_SHORT).show();
                    }
                }
                if (result != null) {

                }
            } catch (Exception exception) {
            }
        }
    }


    public void shareWhatsapp(File file) {
        Intent shareIntent = new Intent();
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setAction(Intent.ACTION_SEND);
        //without the below line intent will show error
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(android.content.Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", file));
        // Target whatsapp:
        shareIntent.setPackage("com.whatsapp");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            shareWhatsappBusiness(file);
            //Toast.makeText(NewInvoiceListActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareWhatsappBusiness(File file) {
        Intent shareIntent = new Intent();
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setAction(Intent.ACTION_SEND);
        //without the below line intent will show error
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(android.content.Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", file));
        // Target whatsapp:
        shareIntent.setPackage("com.whatsapp.w4b");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(NewInvoiceListActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /*public void showPdf(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                storagePath = Environment.getExternalStorageDirectory();
                invoiceNO = "KJHS_q23";
                FILE = Environment.getExternalStorageDirectory() + "/" + invoiceNO+".pdf";
                fatchinvoiceList();
            } else {
                requestPermission(); // Code for permission
            }
        } else {
            storagePath = Environment.getExternalStorageDirectory();
            invoiceNO = "KJHS_q23";
            FILE = Environment.getExternalStorageDirectory() + "/" + invoiceNO+".pdf";
            fatchinvoiceList();

        }
    }
*/
    public void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void filter(String text) {
        try {
            //new array list that will hold the filtered data
            ArrayList<CustomerModel> filterdNames = new ArrayList<>();
            //looping through existing elements
            for (CustomerModel s : customerList) {
                //if the existing elements contains the search input
                if (s.getCustomerName().toLowerCase().contains(text.toLowerCase()) || s.getCustomerCode().toLowerCase().contains(text.toLowerCase())) {
                    //adding the element to filtered list
                    filterdNames.add(s);
                }
            }
            //calling a method of the adapter class and passing the filtered list
            customerNameAdapter.filterList(filterdNames);

        } catch (Exception ex) {
            Log.e("Error_in_filter", Objects.requireNonNull(ex.getMessage()));
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    void fatchinvoiceList(ArrayList<InvoicePrintPreviewModel> invoiceDetails) {
        // Create a document and set it's properties
        Document objDocument = new Document();
        objDocument.setCreator("Powered By : Google.com");
        objDocument.setAuthor("Google");
        objDocument.setTitle("Google Invoice");
        // Create a page to add to the document
        Page objPage = new Page(PageSize.LETTER, PageOrientation.PORTRAIT, 20.0f);

        String strText = "INVOICE";
        Label invoiceTitle = new Label(strText, 0, 0, 504, 100, Font.getHelvetica(), 20, TextAlign.CENTER);
        // Create a Label to add to the page
        Label objLabel = new Label(company_name + "\n" + company_address1 + "\n " + company_address2 + "\n" + company_address3 + "", 0, 100, 504, 100, Font.getHelvetica(), 12, TextAlign.LEFT);
        Label objLabel2 = new Label("Invoice Number: " + invoiceDetails.get(0).getInvoiceNumber() + "\nInvoice Date: " + invoiceDetails.get(0).getInvoiceDate() + "\nCustomer Name: " + invoiceDetails.get(0).getCustomerName(), 0, 100, 504, 100, Font.getHelvetica(), 12, TextAlign.RIGHT);
        // Add label to page
        //objLabel.setTextOutlineColor(com.cete.dynamicpdf.Color);
        objPage.getElements().add(invoiceTitle);
        objPage.getElements().add(objLabel);
        objPage.getElements().add(objLabel2);

        Label line = new Label(PdfUtils.drawLine(), 0, 220, 700, 400);
        objPage.getElements().add(line);


        int index = 1;
        int space = 240;
        for (InvoicePrintPreviewModel model : invoiceDetails) {
            Log.w("BiggestLength:", PdfUtils.getLength(model.getInvoiceList()) + "");
            Label titleLabel = new Label("SNo" + PdfUtils.snoSpace() + "Description    " + PdfUtils.getDescriptionSpace(15, '\t') + "Qty" + PdfUtils.qtySpace() + "Price" + PdfUtils.priceSpace() + "Total", 0, 200, 700, 100, Font.getHelvetica(), 15, TextAlign.LEFT);
            objPage.getElements().add(titleLabel);
            for (InvoicePrintPreviewModel.InvoiceList invoiceList : model.getInvoiceList()) {
                if (invoiceList.getDescription().length() < 15) {
                    Label titleLabel1 = new Label(index + "" + PdfUtils.snoSpace() + invoiceList.getDescription() + PdfUtils.getDescriptionSpace(PdfUtils.getLength(model.getInvoiceList()) + 11, '\t') + (int) Double.parseDouble(invoiceList.getNetQty()) + PdfUtils.qtySpace() + Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getPricevalue())) + PdfUtils.priceSpace() + Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getTotal())), 0, space, 700, 100, Font.getHelvetica(), 15, TextAlign.LEFT);
                    objPage.getElements().add(titleLabel1);
                } else {
                    Label titleLabel1 = new Label(index + "" + PdfUtils.snoSpace() + invoiceList.getDescription().substring(0, 15) + PdfUtils.getDescriptionSpace(15, '\t') + (int) Double.parseDouble(invoiceList.getNetQty()) + PdfUtils.qtySpace() + Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getPricevalue())) + PdfUtils.priceSpace() + Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getTotal())), 0, space, 700, 100, Font.getHelvetica(), 15, TextAlign.LEFT);
                    objPage.getElements().add(titleLabel1);
                }

                index++;
                space = space + 30;
            }
            Label totalLabel = new Label("Sub Total:" + Utils.twoDecimalPoint(Double.parseDouble(model.getSubTotal())) + " \nGST: " + Utils.twoDecimalPoint(Double.parseDouble(model.getNetTax())) + " \nNet Total: " + Utils.twoDecimalPoint(Double.parseDouble(model.getNetTotal())), 0, space + 50, 504, 100, Font.getHelvetica(), 15, TextAlign.RIGHT);
            objPage.getElements().add(totalLabel);
        }

        Label line1 = new Label(PdfUtils.drawLine(), 0, space + 30, 700, 400);
        objPage.getElements().add(line1);

        // Add page to document
        objDocument.getPages().add(objPage);

        try {
            objDocument.draw(FILE);
            try {
                File filepath = new File(FILE);
                pdfFile = filepath;
                sharePdfView(pdfFile);
                // displayFromAsset(pdfFile);
            } catch (ActivityNotFoundException e) {
            }
        } catch (Exception edx) {
            Log.e("PdfOpenError:", edx.getMessage());
        }
    }

    public void sharePdfView(File pdfFile) {
        try {
            if (pdfGenerateDialog != null && pdfGenerateDialog.isShowing()) {
                pdfGenerateDialog.dismiss();
            }
            Log.w("URL-FromthisPdf:", Uri.fromFile(pdfFile).toString());
            //FileProvider.getUriForFile(this,"Share",pdfFile);
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                            BuildConfig.APPLICATION_ID + ".provider", pdfFile));
            startActivity(Intent.createChooser(share, "Share"));
        } catch (Exception e) {
        }
    }

    public void createInvoicePdfTable(ArrayList<InvoicePrintPreviewModel> invoiceDetails) {
        //Create a document object.
        Document document = new Document();
        document.setCreator("Powered By : Google.com");
        document.setAuthor("Google");
        document.setTitle("Google Invoice");

        //Create a page.
        Page page = new Page(PageSize.LETTER, PageOrientation.PORTRAIT, 20.0f);

        //Create Table2 object.
        Table2 table2 = new Table2(0, 0, 700, 1000);

        // Add columns to the table
        table2.getColumns().add(150);
        table2.getColumns().add(90);
        table2.getColumns().add(90);
        table2.getColumns().add(90);

        // Add rows to the table and add cells to the rows
        Row2 row1 = table2.getRows().add(40, Font.getHelveticaBold(), 16, Grayscale.getBlack(), Grayscale.getGray());

        row1.getCellDefault().setAlign(TextAlign.CENTER);
        row1.getCellDefault().setVAlign(VAlign.CENTER);
        row1.getCells().add("Header 1");
        row1.getCells().add("Header 2");
        row1.getCells().add("Header 3");
        row1.getCells().add("Header 4");

        Row2 row2 = table2.getRows().add(30);
        Cell2 cell1 = row2.getCells().add("Rowheader 1", Font.getHelveticaBold(), 16, Grayscale.getBlack(), Grayscale.getGray(), 1);
        cell1.setAlign(TextAlign.CENTER);
        cell1.setVAlign(VAlign.CENTER);
        row2.getCells().add("Item 1");
        row2.getCells().add("Item 2");
        row2.getCells().add("Item 3");
        Row2 row3 = table2.getRows().add(30);

        Cell2 cell2 = row3.getCells().add("Rowheader 2", Font.getHelveticaBold(), 16, Grayscale.getBlack(), Grayscale.getGray(), 1);
        cell2.setAlign(TextAlign.CENTER);
        cell2.setVAlign(VAlign.CENTER);

        row3.getCells().add("Item 4");
        row3.getCells().add("Item 5, this item is much longer than the rest so that " +
                "you can see that each row will automatically expand to fit to the " +
                "height of the largest element in that row.");
        row3.getCells().add("Item 6");

        Row2 row4 = table2.getRows().add(30);

        Cell2 cell3 = row4.getCells().add("Rowheader 3", Font.getHelveticaBold(), 16, Grayscale.getBlack(), Grayscale.getGray(), 1);
        cell3.setAlign(TextAlign.CENTER);
        cell3.setVAlign(VAlign.CENTER);

        row4.getCells().add("Item 7");
        row4.getCells().add("Item 8");
        row4.getCells().add("Item 9");
        // Add the table to the page
        page.getElements().add(table2);
        //add page to the document.
        document.getPages().add(page);

        //Save document to file
        try {
            document.draw(FILE);
            try {
                File filepath = new File(FILE);
                pdfFile = filepath;
                // sharePdfView(pdfFile);
                displayFromAsset(pdfFile);
            } catch (ActivityNotFoundException e) {
            }
        } catch (Exception edx) {
            Log.e("PdfOpenError:", edx.getMessage());
        }
    }


    private void displayFromAsset(File assetFileName) {

        Log.w("DisplayFileName::", assetFileName.toString());

        customerLayout.setVisibility(View.GONE);
        invoiceOptionLayout.setVisibility(View.GONE);
        pdfViewLayout.setVisibility(View.VISIBLE);
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        pdfView.fromFile(assetFileName)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }


    // slide the view from its current position to below itself
    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void slideUp(View view) {
        // view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }


    public void getDate(EditText dateEditext) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(NewInvoiceListActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateEditext.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

   /* private void getInvoicePrintDetails(String invoiceNumber,int copy) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("CompanyCode",companyId);
        jsonObject.put("InvoiceNo",invoiceNumber);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"SalesApi/GetInvoiceByCode?Requestdata="+jsonObject.toString();
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Generating Print Preview...");
        pDialog.setCancelable(false);
        pDialog.show();
        invoiceHeaderDetails =new ArrayList<>();
        invoicePrintList=new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try{
                        Log.w("Invoice_Details:",response.toString());
                        if (response.length() > 0) {
                            InvoicePrintPreviewModel model = new InvoicePrintPreviewModel();
                            model.setInvoiceNumber(response.optString("InvoiceNo"));
                            model.setInvoiceDate(response.optString("InvoiceDateString"));
                            model.setCustomerCode(response.optString("CustomerCode"));
                            model.setCustomerName(response.optString("CustomerName"));
                            model.setAddress(response.optString("Address1"));
                            model.setDeliveryAddress(response.optString("Address1"));
                            model.setSubTotal(response.optString("SubTotal"));
                            model.setNetTax(response.optString("Tax"));
                            model.setNetTotal(response.optString("NetTotal"));
                            model.setTaxType(response.optString("TaxType"));
                            model.setTaxValue(response.optString("TaxPerc"));
                            model.setOutStandingAmount(response.optString("BalanceAmount"));
                            model.setBillDiscount(response.optString("BillDIscount"));
                            model.setItemDiscount(response.optString("ItemDiscount"));

                            JSONArray products = response.getJSONArray("InvoiceDetails");
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject object = products.getJSONObject(i);
                                if (Double.parseDouble(object.optString("LQty")) > 0 || Double.parseDouble(object.optString("LQty")) < 0) {
                                    InvoicePrintPreviewModel.InvoiceList invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                    invoiceListModel.setProductCode(object.optString("ProductCode"));
                                    invoiceListModel.setDescription(object.optString("ProductName"));
                                    invoiceListModel.setLqty(object.optString("LQty"));
                                    invoiceListModel.setCqty(object.optString("CQty"));
                                    invoiceListModel.setNetQty(object.optString("LQty"));
                                    double qty = Double.parseDouble(object.optString("LQty"));
                                    double price = Double.parseDouble(object.optString("Price"));

                                    double nettotal = qty * price;
                                    invoiceListModel.setTotal(String.valueOf(nettotal));
                                    invoiceListModel.setPricevalue(String.valueOf(price));
                                    invoiceListModel.setCartonPrice(object.optString("CartonPrice"));
                                    invoiceListModel.setUnitPrice(object.optString("Price"));
                                    invoiceListModel.setPcsperCarton(object.optString("PcsPerCarton"));
                                    invoiceListModel.setItemtax(object.optString("Tax"));
                                    invoiceListModel.setSubTotal(object.optString("SubTotal"));
                                    invoicePrintList.add(invoiceListModel);


                                    if (!object.optString("ReturnQty").isEmpty() && !object.optString("ReturnQty").equals("null") && Double.parseDouble(object.optString("ReturnQty")) > 0)  {
                                        invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                        invoiceListModel.setProductCode(object.optString("ProductCode"));
                                        invoiceListModel.setDescription(object.optString("ProductName"));
                                        invoiceListModel.setLqty(object.optString("LQty"));
                                        invoiceListModel.setCqty(object.optString("CQty"));
                                        invoiceListModel.setNetQty("-"+object.optString("ReturnQty"));

                                        double qty1 = Double.parseDouble(object.optString("ReturnQty"));
                                        double price1 = Double.parseDouble(object.optString("Price"));
                                        double nettotal1 = qty1 * price1;
                                        invoiceListModel.setTotal("-"+String.valueOf(nettotal1));
                                        invoiceListModel.setPricevalue(String.valueOf(price1));

                                        invoiceListModel.setUomCode(object.optString("UOMCode"));
                                        invoiceListModel.setCartonPrice(object.optString("CartonPrice"));
                                        invoiceListModel.setUnitPrice(object.optString("Price"));
                                        invoiceListModel.setPcsperCarton(object.optString("PcsPerCarton"));
                                        invoiceListModel.setItemtax(object.optString("Tax"));
                                        invoiceListModel.setSubTotal(object.optString("SubTotal"));
                                        invoicePrintList.add(invoiceListModel);
                                    }


                                    if (Double.parseDouble(object.optString("CQty")) > 0 || Double.parseDouble(object.optString("CQty")) < 0) {
                                        invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                        invoiceListModel.setProductCode(object.optString("ProductCode"));
                                        invoiceListModel.setDescription(object.optString("ProductName"));
                                        invoiceListModel.setLqty(object.optString("LQty"));
                                        invoiceListModel.setCqty(object.optString("CQty"));
                                        invoiceListModel.setNetQty(object.optString("CQty"));

                                        double qty1 = Double.parseDouble(object.optString("CQty"));
                                        double price1 = Double.parseDouble(object.optString("CartonPrice"));
                                        double nettotal1 = qty1 * price1;
                                        invoiceListModel.setTotal(String.valueOf(nettotal1));
                                        invoiceListModel.setPricevalue(String.valueOf(price1));

                                        invoiceListModel.setUomCode(object.optString("UOMCode"));
                                        invoiceListModel.setCartonPrice(object.optString("CartonPrice"));
                                        invoiceListModel.setUnitPrice(object.optString("Price"));
                                        invoiceListModel.setPcsperCarton(object.optString("PcsPerCarton"));
                                        invoiceListModel.setItemtax(object.optString("Tax"));
                                        invoiceListModel.setSubTotal(object.optString("SubTotal"));
                                        invoicePrintList.add(invoiceListModel);
                                    }

                                } else {
                                    if (Double.parseDouble(object.optString("CQty")) > 0 || Double.parseDouble(object.optString("CQty")) < 0) {
                                        Log.w("PrintedCqtyValue:", object.optString("CQty"));
                                        InvoicePrintPreviewModel.InvoiceList invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                        invoiceListModel.setProductCode(object.optString("ProductCode"));
                                        invoiceListModel.setDescription(object.optString("ProductName"));
                                        invoiceListModel.setLqty(object.optString("LQty"));
                                        invoiceListModel.setCqty(object.optString("CQty"));
                                        invoiceListModel.setNetQty(object.optString("CQty"));

                                        double qty1 = Double.parseDouble(object.optString("CQty"));
                                        double price1 = Double.parseDouble(object.optString("CartonPrice"));
                                        double nettotal1 = qty1 * price1;
                                        invoiceListModel.setTotal(String.valueOf(nettotal1));
                                        invoiceListModel.setPricevalue(String.valueOf(price1));

                                        invoiceListModel.setUomCode(object.optString("UOMCode"));
                                        invoiceListModel.setCartonPrice(object.optString("CartonPrice"));
                                        invoiceListModel.setUnitPrice(object.optString("Price"));
                                        invoiceListModel.setPcsperCarton(object.optString("PcsPerCarton"));
                                        invoiceListModel.setItemtax(object.optString("Tax"));
                                        invoiceListModel.setSubTotal(object.optString("SubTotal"));
                                        invoicePrintList.add(invoiceListModel);


                                        if (!object.optString("ReturnQty").isEmpty() && !object.optString("ReturnQty").equals("null") && Double.parseDouble(object.optString("ReturnQty")) > 0)  {
                                            invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                            invoiceListModel.setProductCode(object.optString("ProductCode"));
                                            invoiceListModel.setDescription(object.optString("ProductName"));
                                            invoiceListModel.setLqty(object.optString("LQty"));
                                            invoiceListModel.setCqty(object.optString("CQty"));
                                            invoiceListModel.setNetQty("-"+object.optString("ReturnQty"));

                                            double qty12 = Double.parseDouble(object.optString("ReturnQty"));
                                            double price12 = Double.parseDouble(object.optString("Price"));
                                            double nettotal12 = qty12 * price12;
                                            invoiceListModel.setTotal("-"+String.valueOf(nettotal12));
                                            invoiceListModel.setPricevalue(String.valueOf(price12));

                                            invoiceListModel.setUomCode(object.optString("UOMCode"));
                                            invoiceListModel.setCartonPrice(object.optString("CartonPrice"));
                                            invoiceListModel.setUnitPrice(object.optString("Price"));
                                            invoiceListModel.setPcsperCarton(object.optString("PcsPerCarton"));
                                            invoiceListModel.setItemtax(object.optString("Tax"));
                                            invoiceListModel.setSubTotal(object.optString("SubTotal"));
                                            invoicePrintList.add(invoiceListModel);
                                        }
                                    }
                                }
                            }

                            model.setInvoiceList(invoicePrintList);
                            invoiceHeaderDetails.add(model);
                        }

                        pDialog.dismiss();
                        ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(NewInvoiceListActivity.this,printerMacId);
                        if (isInvoicePrint){
                            zebraPrinterActivity.printInvoice(copy,invoiceHeaderDetails,invoicePrintList,"false");
                        }else {
                            zebraPrinterActivity.printDeliveryOrder(copy,invoiceHeaderDetails,invoicePrintList);
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
    }*/

    public void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    public void getCustomerDetails(String customerCode, boolean isloader) {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(NewInvoiceListActivity.this);
        // Initialize a new JsonArrayRequest instance
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("CustomerCode", customerCode);
            jsonObject.put("CompanyCode", companyCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Utils.getBaseUrl(NewInvoiceListActivity.this) + "MasterApi/GetCustomer?Requestdata=" + jsonObject.toString();
        Log.w("Given_url:", url);
        ProgressDialog progressDialog = new ProgressDialog(NewInvoiceListActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Customer Details Loading...");
        if (isloader) {
            progressDialog.show();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.w("details_response:", response.toString());
                        if (response.length() > 0) {
                            if (response.optString("ErrorMessage").equals("null")) {
                                oustandingAmount = response.optString("BalanceAmount");
                                progressDialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error in getting response", Toast.LENGTH_LONG).show();
                            }
                        }
                        // pDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            //  pDialog.dismiss();
            Log.w("Error_throwing:", error.toString());
            progressDialog.dismiss();
            // Toast.makeText(getActivity(),error.toString(),Toast.LENGTH_LONG).show();
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

    private void getInvoicePrintDetails(String invoiceNumber, int copy, String doPrint) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject = new JSONObject();
        // jsonObject.put("CompanyCode", companyId);
        jsonObject.put("InvoiceNo", invoiceNumber);
        jsonObject.put("LocationCode", locationCode);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "InvoiceDetails";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:", url+jsonObject);
        // pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        // pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        // pDialog.setTitleText("Generating Print Preview...");
        // pDialog.setCancelable(false);
        //  pDialog.show();
        invoiceHeaderDetails = new ArrayList<>();
        invoicePrintList = new ArrayList<>();
        salesReturnList = new ArrayList<>();
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
                        String statusCode = response.optString("statusCode");
                        if (statusCode.equals("1")) {
                            JSONArray responseData = response.getJSONArray("responseData");
                            JSONObject object = responseData.optJSONObject(0);

                            InvoicePrintPreviewModel model = new InvoicePrintPreviewModel();
                            model.setInvoiceNumber(object.optString("invoiceNumber"));
                            model.setInvoiceDate(object.optString("invoiceDate"));
                            model.setCustomerCode(object.optString("customerCode"));
                            model.setCustomerName(object.optString("customerName"));
                            model.setOverAllTotal(object.optString("overAllTotal"));
                            // model.setAddress(object.optString("street"));
                            model.setAddress(object.optString("address1") + object.optString("address2") + object.optString("address3"));
                            model.setAddress1(object.optString("address1"));
                            model.setAddress2(object.optString("address2"));
                            model.setAddress3(object.optString("address3"));
                            model.setAddressstate(object.optString("street")+" "
                                    + object.optString("block") + " " + object.optString("city"));
                            model.setAddresssZipcode(object.optString("countryName")+" "+object.optString("state")+" "
                                    +object.optString("zipcode"));

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
                            model.setAllowDeliveryAddress(object.optString("showShippingAddress"));
                            model.setCurrentAddress(object.optString("CurrentAddress"));
                            String signFlag = object.optString("signFlag");
                            if (signFlag.equals("Y")) {
                                String signature = object.optString("signature");
                                Utils.setSignature(signature);
                                createSignature();
                            } else {
                                Utils.setSignature("");
                            }
                            JSONArray detailsArray = object.optJSONArray("invoiceDetails");
                            for (int i=0;i<detailsArray.length();i++) {
                                JSONObject detailObject = detailsArray.optJSONObject(i);
                                if (Double.parseDouble(detailObject.optString("quantity")) > 0) {
                                    InvoicePrintPreviewModel.InvoiceList invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                    invoiceListModel.setProductCode(detailObject.optString("productCode"));
                                    invoiceListModel.setDescription(detailObject.optString("productName"));
                                    invoiceListModel.setLqty(detailObject.optString("unitQty"));
                                    invoiceListModel.setCqty(detailObject.optString("cartonQty"));
                                    invoiceListModel.setNetQty(detailObject.optString("quantity"));
                                    invoiceListModel.setExcQty(detailObject.optString("exc_Qty"));
                                    invoiceListModel.setNetQuantity(detailObject.optString("netQuantity"));
                                    invoiceListModel.setFocQty(detailObject.optString("foc_Qty"));
                                    invoiceListModel.setSaleType("");
                                    if (detailObject.optString("bP_CatalogNo") != null) {
                                        invoiceListModel.setCustomerItemCode(detailObject.optString("bP_CatalogNo"));
                                    }
                                    invoiceListModel.setReturnQty(detailObject.optString("returnQty"));
                                    invoiceListModel.setCartonPrice(detailObject.optString("cartonPrice"));
                                    invoiceListModel.setUnitPrice(detailObject.optString("price"));
                                    double qty = Double.parseDouble(detailObject.optString("quantity"));
                                    double price = Double.parseDouble(detailObject.optString("price"));
                                    invoiceListModel.setUomCode(detailObject.optString("uomCode"));

                                    double nettotal = qty * price;
                                    invoiceListModel.setTotal(String.valueOf(nettotal));
                                    invoiceListModel.setPricevalue(String.valueOf(price));

                                    invoiceListModel.setPcsperCarton(detailObject.optString("pcsPerCarton"));
                                    invoiceListModel.setItemtax(detailObject.optString("totalTax"));
                                    invoiceListModel.setSubTotal(detailObject.optString("subTotal"));
                                    invoicePrintList.add(invoiceListModel);
                                    Log.w("invoicSizeEntr1","");

                                }
                                if (!detailObject.optString("returnQty").isEmpty() && !detailObject.optString("returnQty").equals("null")
                                        && Double.parseDouble(detailObject.optString("returnQty")) > 0) {
                                    InvoicePrintPreviewModel.InvoiceList invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                    invoiceListModel.setProductCode(detailObject.optString("productCode"));
                                    invoiceListModel.setDescription(detailObject.optString("productName"));
                                    invoiceListModel.setLqty(detailObject.optString("unitQty"));
                                    invoiceListModel.setCqty(detailObject.optString("cartonQty"));
                                    invoiceListModel.setNetQty(detailObject.optString("quantity"));
                                    invoiceListModel.setExcQty(detailObject.optString("exc_Qty"));
                                    invoiceListModel.setNetQuantity(detailObject.optString("returnQty"));
                                    invoiceListModel.setFocQty(detailObject.optString("foc_Qty"));
                                    invoiceListModel.setSaleType("Return");
                                    if (detailObject.optString("bP_CatalogNo") != null) {
                                        invoiceListModel.setCustomerItemCode(detailObject.optString("bP_CatalogNo"));
                                    }
                                    invoiceListModel.setReturnQty(detailObject.optString("returnQty"));
                                    invoiceListModel.setCartonPrice(detailObject.optString("cartonPrice"));
                                    invoiceListModel.setUnitPrice(detailObject.optString("price"));
                                    double qty = Double.parseDouble(detailObject.optString("quantity"));
                                    double price = Double.parseDouble(detailObject.optString("price"));

                                    double nettotal = qty * price;
                                    invoiceListModel.setTotal("0.00");
                                    invoiceListModel.setPricevalue("0.00");

                                    invoiceListModel.setPcsperCarton(detailObject.optString("pcsPerCarton"));
                                    invoiceListModel.setItemtax(detailObject.optString("totalTax"));
                                    invoiceListModel.setSubTotal(detailObject.optString("subTotal"));
                                    invoicePrintList.add(invoiceListModel);
                                    Log.w("invoicSizeEntr2","");

                                }
                                if (!detailObject.optString("exc_Qty").isEmpty() && !detailObject.optString("exc_Qty").equals("null")
                                        && Double.parseDouble(detailObject.optString("exc_Qty")) > 0) {
                                    InvoicePrintPreviewModel.InvoiceList invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                    invoiceListModel.setProductCode(detailObject.optString("productCode"));
                                    invoiceListModel.setDescription(detailObject.optString("productName"));
                                    invoiceListModel.setLqty(detailObject.optString("unitQty"));
                                    invoiceListModel.setCqty(detailObject.optString("cartonQty"));
                                    invoiceListModel.setNetQty(detailObject.optString("quantity"));
                                    invoiceListModel.setExcQty(detailObject.optString("exc_Qty"));
                                    invoiceListModel.setNetQuantity(detailObject.optString("exc_Qty"));
                                    invoiceListModel.setFocQty(detailObject.optString("foc_Qty"));
                                    invoiceListModel.setSaleType("Exchange");

                                    if (detailObject.optString("bP_CatalogNo") != null) {
                                        invoiceListModel.setCustomerItemCode(detailObject.optString("bP_CatalogNo"));
                                    }
                                    invoiceListModel.setReturnQty(detailObject.optString("returnQty"));
                                    invoiceListModel.setCartonPrice(detailObject.optString("cartonPrice"));
                                    invoiceListModel.setUnitPrice(detailObject.optString("price"));
                                    double qty = Double.parseDouble(detailObject.optString("quantity"));
                                    double price = Double.parseDouble(detailObject.optString("price"));

                                    double nettotal = qty * price;
                                    invoiceListModel.setTotal("0.00");
                                    invoiceListModel.setPricevalue("0.00");

                                    invoiceListModel.setPcsperCarton(detailObject.optString("pcsPerCarton"));
                                    invoiceListModel.setItemtax(detailObject.optString("totalTax"));
                                    invoiceListModel.setSubTotal(detailObject.optString("subTotal"));
                                    invoicePrintList.add(invoiceListModel);
                                    Log.w("invoicSizeEntr3","");

                                }
                                if (!detailObject.optString("foc_Qty").isEmpty() && !detailObject.optString("foc_Qty").equals("null")
                                        && Double.parseDouble(detailObject.optString("foc_Qty")) > 0) {
                                    InvoicePrintPreviewModel.InvoiceList invoiceListModel = new InvoicePrintPreviewModel.InvoiceList();
                                    invoiceListModel.setProductCode(detailObject.optString("productCode"));
                                    invoiceListModel.setDescription(detailObject.optString("productName"));
                                    invoiceListModel.setLqty(detailObject.optString("unitQty"));
                                    invoiceListModel.setCqty(detailObject.optString("cartonQty"));
                                    invoiceListModel.setNetQty(detailObject.optString("quantity"));
                                    invoiceListModel.setExcQty(detailObject.optString("exc_Qty"));
                                    invoiceListModel.setNetQuantity(detailObject.optString("foc_Qty"));
                                    invoiceListModel.setFocQty(detailObject.optString("foc_Qty"));
                                    invoiceListModel.setSaleType("FOC");

                                    if (detailObject.optString("bP_CatalogNo") != null) {
                                        invoiceListModel.setCustomerItemCode(detailObject.optString("bP_CatalogNo"));
                                    }
                                    invoiceListModel.setReturnQty(detailObject.optString("returnQty"));
                                    invoiceListModel.setCartonPrice(detailObject.optString("cartonPrice"));
                                    invoiceListModel.setUnitPrice(detailObject.optString("price"));
                                    double qty = Double.parseDouble(detailObject.optString("quantity"));
                                    double price = Double.parseDouble(detailObject.optString("price"));

                                    double nettotal = qty * price;
                                    invoiceListModel.setTotal("0.00");
                                    invoiceListModel.setPricevalue("0.00");

                                    invoiceListModel.setPcsperCarton(detailObject.optString("pcsPerCarton"));
                                    invoiceListModel.setItemtax(detailObject.optString("totalTax"));
                                    invoiceListModel.setSubTotal(detailObject.optString("subTotal"));
                                    invoicePrintList.add(invoiceListModel);
                                    Log.w("invoicSizeEntr4","");

                                }


                            }
                            model.setInvoiceList(invoicePrintList);

                            JSONArray SRArray = object.optJSONArray("sR_Details");
                            assert SRArray != null;
                            if (SRArray.length() > 0) {
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
                            printInvoice(copy,doPrint);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error in printing Data...", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            //  pDialog.dismiss();
            Log.w("Error_throwing:", error.toString());
            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedUser = usersList.get(position).getUserName();
        Log.w("UserSelected:", selectedUser);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedUser = "";
    }

    private void createSignature() {
        if (Utils.getSignature() != null && !Utils.getSignature().isEmpty()) {
            try {
                ImageUtil.saveStamp(this, Utils.getSignature(), "Signature");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean validatePrinterConfiguration(){
        boolean printetCheck=false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this,"This device does not support bluetooth",Toast.LENGTH_SHORT).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            Toast.makeText(this,"Enable bluetooth and connect the printer",Toast.LENGTH_SHORT).show();
        } else {
            // Bluetooth is enabled
            if (!printerType.isEmpty() && !printerMacId.isEmpty()){
                printetCheck=true;
            }else {
                Toast.makeText(this,"Please configure Printer",Toast.LENGTH_SHORT).show();
            }
        }
        return printetCheck;
    }

    public void printInvoice(int copy,String doPrint) {
        if (validatePrinterConfiguration()) {

            if (createInvoiceSetting.equals("true")) {
            Log.w("invPrinter", "cg");
                PrinterUtils printerUtils = new PrinterUtils(this, printerMacId);
                printerUtils.printInvoice(copy, invoiceHeaderDetails, invoicePrintList, doPrint);

          /*  TSCPrinter printer = new TSCPrinter(NewInvoiceListActivity.this, printerMacId);
            printer.printInvoice(copy, invoiceHeaderDetails, invoicePrintList, "false");
            printer.setOnCompletionListener(new TSCPrinter.OnCompletionListener() {
                @Override
                public void onCompleted() {
                    Utils.setSignature("");
                    Toast.makeText(getApplicationContext(), "Invoice printed successfully!", Toast.LENGTH_SHORT).show();
                }
            });*/
            } else {
                if (isInvoicePrint) {
                    Log.w("invPrinter11", "");
                    if (printerType.equalsIgnoreCase("Zebra Printer")) {
                        ZebraPrinterActivity zebraPrinterActivity = new ZebraPrinterActivity(NewInvoiceListActivity.this, printerMacId);
                        zebraPrinterActivity.printInvoice(copy, invoiceHeaderDetails, invoicePrintList, "false");
                    } else if (printerType.equalsIgnoreCase("TSC Printer")) {
                        Log.w("invPrinter22", "");
                        TSCPrinter printer = new TSCPrinter(NewInvoiceListActivity.this, printerMacId);
                        printer.printInvoice(copy, invoiceHeaderDetails, invoicePrintList, "false");
                        printer.setOnCompletionListener(new TSCPrinter.OnCompletionListener() {
                            @Override
                            public void onCompleted() {
                                Utils.setSignature("");
                                Toast.makeText(getApplicationContext(), "Invoice printed successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                        // ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(NewInvoiceListActivity.this,printerMacId);
                        // zebraPrinterActivity.printInvoice(copy,invoiceHeaderDetails,invoicePrintList,"false");
                    }
                } else {
                    // zebraPrinterActivity.printDeliveryOrder(copy,invoiceHeaderDetails,invoicePrintList);
                }
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

}