package com.winapp.saperp.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cete.dynamicpdf.io.G;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.winapp.saperp.R;
import com.winapp.saperp.ReportPreview.ReceiptDetailPreviewActivity;
import com.winapp.saperp.ReportPreview.ReceiptSummaryPreviewActivity;
import com.winapp.saperp.ReportPreview.RoCustomerPreviewActivity;
import com.winapp.saperp.ReportPreview.RoInvoicebyProductPreviewActivity;
import com.winapp.saperp.ReportPreview.RoInvoicebySummaryPreviewActivity;
import com.winapp.saperp.ReportPreview.RoReceiptSettlePreviewActivity;
import com.winapp.saperp.ReportPreview.RoSettlementPreviewActivity;
import com.winapp.saperp.ReportPreview.SapSalesSummaryPreviewActivity;
import com.winapp.saperp.ReportPreview.SapStockReturnPreviewActivity;
import com.winapp.saperp.ReportPreview.SapStockSummaryPreviewActivity;
import com.winapp.saperp.model.AppUtils;
import com.winapp.saperp.model.CustomerModel;
import com.winapp.saperp.model.CustomerStateModel;
import com.winapp.saperp.model.InvoiceByProductModel;
import com.winapp.saperp.model.InvoiceSummaryModel;
import com.winapp.saperp.model.ReceiptDetailsModel;
import com.winapp.saperp.model.ReceiptSummaryModel;
import com.winapp.saperp.model.ReportSalesSummaryModel;
import com.winapp.saperp.model.ReportStockSummaryModel;
import com.winapp.saperp.model.SettlementReceiptModel;
import com.winapp.saperp.model.StockBadRequestReturnModel;
import com.winapp.saperp.model.UserListModel;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.Utils;
import com.winapp.saperp.zebraprinter.TSCPrinter;
import com.winapp.saperp.zebraprinter.ZebraPrinterActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import java.util.concurrent.atomic.AtomicReference;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReportsActivity extends NavigationActivity implements View.OnClickListener {
    // Variables Declarations
    private CheckBox invoiceByProduct;
    private CheckBox invoiceSummary;
    private CheckBox customerStatement;
    private CheckBox receiptSummary;
    private CheckBox receiptDetails;
    private CheckBox settlementReport;
    private CheckBox settle_receiptReport;
    private ArrayList<CustomerModel> customerList;
    private ArrayList<String> searchableCustomerList;
    private SearchableSpinner customerListSpinner;
    private SearchableSpinner userListSpinner;
    private SearchableSpinner statusListSpinner;
    private SessionManager session;
    private TextView fromDate;
    private TextView userName;
    private TextView toDate;
    private Button cancelButton;
    private Button okButton;
    private AlertDialog dialog;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String username;
    private HashMap<String,String> user;
    private String from_date="";
    private String to_date="";
    private String customer_name="";
    private String customer_id="";
    private String status_value="";
    private SweetAlertDialog pDialog;
    private JsonObjectRequest jsonObjectRequest;
    private ArrayList<InvoiceByProductModel> invoiceByProductList;
    private ArrayList<InvoiceByProductModel.ProductDetails> productDetailsList;
    private ArrayList<InvoiceSummaryModel> invoiceSummaryList;
    private ArrayList<InvoiceSummaryModel.SummaryDetails> invoiceSummaryDetailsList;
    static String printerMacId;
    static String printerType;
    public SharedPreferences sharedPreferences;
    public Button decreaseButton;
    public Button increaseButton;
    public TextView noOfCopyText;
    public String companyCode;
    public String locationCode;
    int minteger = 1;
    private ArrayList<ReportSalesSummaryModel> reportSalesSummaryList;
    private ArrayList<ReportSalesSummaryModel.ReportSalesSummaryDetails> reportSalesSummaryDetailsList;
    private ArrayList<ReportSalesSummaryModel.ReportSalesSummaryInvDetails> reportSalesSummaryInvDetailsList;
    private ArrayList<ReportStockSummaryModel> reportStockSummaryList;
    private ArrayList<ReportStockSummaryModel.ReportStockSummaryDetails> reportStockSummaryDetailsList;
    private ArrayList<CustomerStateModel> customerStateList ;
    private ArrayList<CustomerStateModel.CustInvoiceDetails> custInvoiceDetailsList ;
    private TextView stockSummaryReport;
    private String currentDate;
    private String currentDateString;
    private TextView badStockReturnSummary;
    private ArrayList<StockBadRequestReturnModel> stockBadRequestReturnList;
    private ArrayList<StockBadRequestReturnModel.StockBadRequestReturnDetails> stockBadRequestReturnDetailsList;
    private JSONObject jsonObject;
    private JSONObject jsonBody;
    private RequestQueue requestQueue;
    private String url;
    public  String apidateFormat;
    private DatePickerDialog datePickerDialog;
    private ArrayAdapter<String> arrayAdapter;
    private ImageView printPreview;
    private ImageView printView;
    boolean isPrintEnable=true;
    private ProgressDialog progressDialog;
    private String isLocationPermissionAuthentication;
    private ArrayList<UserListModel> usersList;
    private ArrayList<String> salesManList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_reports, contentFrameLayout);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reports");

        session=new SessionManager(this);
        user=session.getUserDetails();
        username=user.get(SessionManager.KEY_USER_NAME);
        companyCode=user.get(SessionManager.KEY_COMPANY_CODE);
        locationCode=user.get(SessionManager.KEY_LOCATION_CODE);
        isLocationPermissionAuthentication=user.get(SessionManager.IS_LOCATION_PERMISSION);

        invoiceByProduct=findViewById(R.id.invoice_by_product);
        invoiceSummary=findViewById(R.id.invoice_by_summary);
        customerStatement=findViewById(R.id.customer_statement);
        receiptDetails=findViewById(R.id.receipt_details);
        receiptSummary=findViewById(R.id.receipt_summary);
        settlementReport=findViewById(R.id.settlement_report);
        settle_receiptReport=findViewById(R.id.settle_receipt_report);
        stockSummaryReport=findViewById(R.id.stock_summary);
        badStockReturnSummary=findViewById(R.id.bad_stock_summary);
        printPreview=findViewById(R.id.preview);
        printView=findViewById(R.id.print);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Connecting to Printer...!");


        invoiceSummary.setOnClickListener(this);
        invoiceByProduct.setOnClickListener(this);
        customerStatement.setOnClickListener(this);
        receiptSummary.setOnClickListener(this);
        receiptDetails.setOnClickListener(this);
        settlementReport.setOnClickListener(this);
        settle_receiptReport.setOnClickListener(this);
        stockSummaryReport.setOnClickListener(this);
        badStockReturnSummary.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType=sharedPreferences.getString("printer_type","");
        printerMacId=sharedPreferences.getString("mac_address","");

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        currentDate = df1.format(c);
        apidateFormat =df1.format(c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        currentDateString=formattedDate;

        Log.w("Printer_Mac_Id:",printerMacId);
        Log.w("Printer_Type:",printerType);


        printPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPrintEnable=false;
                printPreview.setBackgroundResource(R.drawable.editext_border);
                printView.setBackground(null);
            }
        });

        printView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPrintEnable=true;
                printView.setBackgroundResource(R.drawable.editext_border);
                printPreview.setBackground(null);
            }
        });

        if (isLocationPermissionAuthentication.equals("Y")){
            try {
                getAllUsers();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        getCustomers();
    }

    @Override
    public void onClick(View view) {
        customer_id = "" ;
        customer_name = "";

        if (view.getId()==R.id.invoice_by_product){
            if (invoiceByProduct.isChecked()){
                invoiceSummary.setChecked(false);
                customerStatement.setChecked(false);
                receiptDetails.setChecked(false);
                receiptSummary.setChecked(false);
                settlementReport.setChecked(false);
                settle_receiptReport.setChecked(false);
                showFilterAlertDialog(view,"Invoice By Products");
            }
        }else if (view.getId()==R.id.invoice_by_summary){
            if (invoiceSummary.isChecked()){
                customerStatement.setChecked(false);
                invoiceByProduct.setChecked(false);
                receiptDetails.setChecked(false);
                receiptSummary.setChecked(false);
                settlementReport.setChecked(false);
                settle_receiptReport.setChecked(false);
                showFilterAlertDialog(view,"Invoice By Summary");
            }

        }else if (view.getId()==R.id.customer_statement){
            if (customerStatement.isChecked()){
                invoiceSummary.setChecked(false);
                invoiceByProduct.setChecked(false);
                receiptDetails.setChecked(false);
                receiptSummary.setChecked(false);
                settlementReport.setChecked(false);
                settle_receiptReport.setChecked(false);
                showFilterAlertDialog(view,"Customer Statement");
            }
        }else if (view.getId()==R.id.receipt_details){
            if (receiptDetails.isChecked()){
                invoiceSummary.setChecked(false);
                invoiceByProduct.setChecked(false);
                receiptSummary.setChecked(false);
                customerStatement.setChecked(false);
                settlementReport.setChecked(false);
                settle_receiptReport.setChecked(false);
                showFilterAlertDialog(view,"Receipt Details");
            }
        }else if (view.getId()==R.id.receipt_summary){
            if (receiptSummary.isChecked()){
                invoiceSummary.setChecked(false);
                invoiceByProduct.setChecked(false);
                receiptDetails.setChecked(false);
                customerStatement.setChecked(false);
                settlementReport.setChecked(false);
                settle_receiptReport.setChecked(false);
                showFilterAlertDialog(view,"Receipt Summary");
            }
        }else if (view.getId()==R.id.settlement_report){
            if (settlementReport.isChecked()){
                invoiceSummary.setChecked(false);
                receiptSummary.setChecked(false);
                invoiceByProduct.setChecked(false);
                receiptDetails.setChecked(false);
                customerStatement.setChecked(false);
                settle_receiptReport.setChecked(false);
                showFilterAlertDialog(view,"Settlement Report");
            }
        }else if (view.getId()==R.id.settle_receipt_report){
            if (settle_receiptReport.isChecked()){
                invoiceSummary.setChecked(false);
                receiptSummary.setChecked(false);
                invoiceByProduct.setChecked(false);
                receiptDetails.setChecked(false);
                customerStatement.setChecked(false);
                settlementReport.setChecked(false);
                showFilterAlertDialog(view,"Settlement With Receipt");
            }
        }
        else if (view.getId()==R.id.stock_summary){
            try {
                showFilterAlertDialog(view,"Stock Summary");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (view.getId()==R.id.bad_stock_summary){
            try {
                showFilterAlertDialog(view,"Bad Stock Report");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearAllSelection(){
        try {
            if (progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            invoiceSummary.setChecked(false);
            invoiceByProduct.setChecked(false);
            customerStatement.setChecked(false);
            receiptDetails.setChecked(false);
            receiptSummary.setChecked(false);
        }catch (Exception exception){}
    }

    public boolean validatePrinterConfiguration(){
        boolean printerCheck =false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(ReportsActivity.this,"This device does not support bluetooth",Toast.LENGTH_SHORT).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            Toast.makeText(ReportsActivity.this,"Enable bluetooth and connect the printer",Toast.LENGTH_SHORT).show();
        } else {
            // Bluetooth is enabled
            if (!printerType.isEmpty() && !printerMacId.isEmpty()){
                printerCheck =true;
            }else {
                Toast.makeText(ReportsActivity.this,"Please configure Printer",Toast.LENGTH_SHORT).show();
            }
        }
        return printerCheck;
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

    @SuppressLint("MissingInflatedId")
    public void showFilterAlertDialog(View view, String title) {
        try {
            // create an alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);
            // set the custom layout
            builder.setCancelable(false);
            final View customLayout = getLayoutInflater().inflate(R.layout.filter_alert_dialog, null);
            builder.setView(customLayout);
            // add a button
            customerListSpinner=customLayout.findViewById(R.id.customer_list_spinner);
            userListSpinner=customLayout.findViewById(R.id.user_list_spinner);
            fromDate=customLayout.findViewById(R.id.from_date);
            toDate=customLayout.findViewById(R.id.to_date);
            okButton=customLayout.findViewById(R.id.btn_ok);
            cancelButton=customLayout.findViewById(R.id.btn_cancel);
            statusListSpinner=customLayout.findViewById(R.id.status_list_spinner);
            userName=customLayout.findViewById(R.id.user_name);
            decreaseButton=customLayout.findViewById(R.id.decrease);
            increaseButton=customLayout.findViewById(R.id.increase);
            noOfCopyText=customLayout.findViewById(R.id.no_of_copy);
            LinearLayout stattusLayout=customLayout.findViewById(R.id.status_layout);
            LinearLayout customerListLayout =customLayout.findViewById(R.id.customer_list_layout);
            LinearLayout userListLayout=customLayout.findViewById(R.id.user_list_layout);
            customerListSpinner.setTitle("Select Customer");
           // customerListSpinner.setVisibility(View.GONE);
            if (isLocationPermissionAuthentication.equals("Y")){
                if (salesManList!=null && salesManList.size() > 1){
                    userListLayout.setVisibility(View.VISIBLE);
                    userName.setVisibility(View.GONE);
                }else {
                    userListLayout.setVisibility(View.GONE);
                    userName.setVisibility(View.VISIBLE);
                }
            }else {
                userListLayout.setVisibility(View.GONE);
                userName.setVisibility(View.VISIBLE);
            }
            userName.setText(username);
            if (customerList.size()>0){
                setDataToAdapter(searchableCustomerList);
            }

            if (salesManList!=null && salesManList.size() > 1){
                setUserAdapter(salesManList);
            }
            AtomicReference<ArrayList<String>> status = new AtomicReference<>(new ArrayList<>());
            status.get().add("ALL");
            status.get().add("PAID");
            status.get().add("NOT PAID");
            setStatusList(status.get());

            if (title.equals("Receipt Details") || title.equals("Receipt Summary") || title.equals("Stock Summary")
                    || title.equals("Bad Stock Report") || title.equals("Customer Statement")){
                stattusLayout.setVisibility(View.GONE);
            }else {
                stattusLayout.setVisibility(View.VISIBLE);
            }

            if (title.equals("Stock Summary") || title.equals("Bad Stock Report")){
                customerListSpinner.setVisibility(View.GONE);
            }

            if (title.equals("Settlement With Receipt")){
                customerListSpinner.setVisibility(View.GONE);
                stattusLayout.setVisibility(View.GONE);
                toDate.setVisibility(View.GONE);
                userName.setVisibility(View.VISIBLE);
                //  userListLayout.setVisibility(View.VISIBLE);
            }

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = df.format(c);
            fromDate.setText(formattedDate);
            toDate.setText(formattedDate);

            // Action for From Date;
            fromDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDate(fromDate);
                }
            });

            // Action from Date;
            toDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDate(toDate);
                }
            });

            if (isPrintEnable){
                okButton.setText(" Print ");
            }else {
                okButton.setText("  Print Preview  ");
            }

            okButton.setOnClickListener(view1 -> {
                try {
                    String[] customername = new String[0];
                    if (!fromDate.getText().toString().isEmpty()){
                        from_date=fromDate.getText().toString();

                    }
                    if (!toDate.getText().toString().isEmpty()){
                        to_date=toDate.getText().toString();
                    }
                    if (!customerListSpinner.getSelectedItem().toString().equals("Select Customer")){
                        customername=customerListSpinner.getSelectedItem().toString().split("~");
                        customer_id=customername[1];
                    }

                    if (!from_date.isEmpty() && !to_date.isEmpty()){
                        Date fromDate = null;
                        try {
                            fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(from_date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Date toDate = null;
                        try {
                            toDate = new SimpleDateFormat("dd/MM/yyyy").parse(to_date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // Use SimpleDateFormat#format() to format a Date into a String in a certain pattern.
                        //status_value=statusListSpinner.getSelectedItem().toString();
                        if (statusListSpinner.getSelectedItem().toString().equals("PAID")){
                            status_value="C";
                        }else if (statusListSpinner.getSelectedItem().toString().equals("NOT PAID")){
                            status_value="O";
                        }else {
                            status_value="";
                        }
                        String fromDateString = new SimpleDateFormat("yyyyMMdd").format(fromDate);
                        String toDateString = new SimpleDateFormat("yyyyMMdd").format(toDate);
                        System.out.println(fromDateString+"-"+toDateString); // 2011-01-18
                        switch (title) {
                            case "Invoice By Products":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        getInvoiceByProduct(customer_id, fromDateString, toDateString, status_value, Integer.parseInt(noOfCopyText.getText().toString()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, RoInvoicebyProductPreviewActivity.class);
                                    intent.putExtra("fromDate",from_date);
                                    intent.putExtra("toDate",to_date);
                                    intent.putExtra("locationCode",locationCode);
                                    intent.putExtra("companyId",companyCode);
                                    intent.putExtra("customerCode",customer_id);
                                    intent.putExtra("customerName",customer_name);
                                    intent.putExtra("userName",username);
                                    intent.putExtra("status",status_value);
                                    startActivity(intent);
                                }
                                break;
                            case "Invoice By Summary":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        getInvoicesBySummary(customer_id, fromDateString, toDateString, status_value, Integer.parseInt(noOfCopyText.getText().toString()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, SapSalesSummaryPreviewActivity.class);
                                    intent.putExtra("fromDate",from_date);
                                    intent.putExtra("toDate",to_date);
                                    intent.putExtra("locationCode",locationCode);
                                    intent.putExtra("companyId",companyCode);
                                    intent.putExtra("customerCode",customer_id);
                                    intent.putExtra("customerName",customer_name);
                                    intent.putExtra("userName",username);
                                    intent.putExtra("status",status_value);
                                    startActivity(intent);
                                }
                                break;
                            case "Customer Statement":
                                if (!customerListSpinner.getSelectedItem().toString().equals("Select Customer")) {
                                    dialog.dismiss();
                                    progressDialog.setMessage("Printing in Progress...!");
                                    progressDialog.show();
                                    if (isPrintEnable){
                                        try {
                                            getCustomerStatement(customer_id, customername[0].toString(),
                                                    fromDateString, toDateString, "O",
                                                    Integer.parseInt(noOfCopyText.getText().toString()));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }else {
                                        Intent intent=new Intent(ReportsActivity.this, RoCustomerPreviewActivity.class);
                                        intent.putExtra("fromDate",from_date);
                                        intent.putExtra("toDate",to_date);
                                        intent.putExtra("locationCode",locationCode);
                                        intent.putExtra("companyId",companyCode);
                                        intent.putExtra("customerCode",customer_id);
                                        intent.putExtra("customerName",customer_name);
                                        intent.putExtra("userName",username);
                                       // intent.putExtra("allUserFilter",isAllUserCheck[0]);
                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Select Customer for Statement", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "Receipt Details":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        getReceiptDetails(customer_id, fromDateString, toDateString, Integer.parseInt(noOfCopyText.getText().toString()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, ReceiptDetailPreviewActivity.class);
                                    intent.putExtra("fromDate",from_date);
                                    intent.putExtra("toDate",to_date);
                                    intent.putExtra("locationCode",locationCode);
                                    intent.putExtra("companyId",companyCode);
                                    intent.putExtra("customerCode",customer_id);
                                    intent.putExtra("customerName",customer_name);
                                    intent.putExtra("userName",username);
                                    startActivity(intent);
                                }
                                break;
                            case "Receipt Summary":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        getReceiptSummary(customer_id, fromDateString, toDateString,
                                                Integer.parseInt(noOfCopyText.getText().toString()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, ReceiptSummaryPreviewActivity.class);
                                    intent.putExtra("fromDate",from_date);
                                    intent.putExtra("toDate",to_date);
                                    intent.putExtra("locationCode",locationCode);
                                    intent.putExtra("companyId",companyCode);
                                    intent.putExtra("customerCode",customer_id);
                                    intent.putExtra("customerName",customer_name);
                                    intent.putExtra("userName",username);
                                    startActivity(intent);
                                }
                                break;

                            case "Settlement Report":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        getSettlementReport(customer_id,from_date,to_date,
                                                Integer.parseInt(noOfCopyText.getText().toString()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, RoSettlementPreviewActivity.class);
                                    intent.putExtra("fromDate",from_date);
                                    intent.putExtra("toDate",to_date);
                                    intent.putExtra("locationCode",locationCode);
                                    intent.putExtra("companyId",companyCode);
                                    intent.putExtra("customerCode",customer_id);
                                    intent.putExtra("customerName",customer_name);
                                    intent.putExtra("userName",username);
                                    startActivity(intent);
                                }
                                break;

                            case "Settlement With Receipt":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        getSettleReceiptReport(fromDateString,Integer.parseInt(noOfCopyText.getText().toString()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, RoReceiptSettlePreviewActivity.class);
                                    intent.putExtra("fromDate",apidateFormat);
                                    intent.putExtra("userName",username);
                                    startActivity(intent);
                                }
                                break;
                            case "Bad Stock Report":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        StockBadRequestReturn(Integer.parseInt(noOfCopyText.getText().toString()),"DM","Damaged/Expired"
                                                ,fromDateString,toDateString);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, SapStockReturnPreviewActivity.class);
                                    intent.putExtra("fromDate",from_date);
                                    intent.putExtra("toDate",to_date);
                                    intent.putExtra("locationCode",locationCode);
                                    intent.putExtra("companyId",companyCode);
                                    intent.putExtra("customerCode",customer_id);
                                    intent.putExtra("customerName",customer_name);
                                    intent.putExtra("userName",username);
                                    startActivity(intent);
                                }
                                break;

                            case "Stock Summary":
                                dialog.dismiss();
                                progressDialog.setMessage("Printing in Progress...!");
                                progressDialog.show();
                                if (isPrintEnable){
                                    try {
                                        getReportStockSummary(Integer.parseInt(noOfCopyText.getText().toString()),currentDate,locationCode,fromDateString,toDateString);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Intent intent=new Intent(ReportsActivity.this, SapStockSummaryPreviewActivity.class);
                                    intent.putExtra("fromDate",from_date);
                                    intent.putExtra("toDate",to_date);
                                    intent.putExtra("locationCode",locationCode);
                                    intent.putExtra("companyId",companyCode);
                                    intent.putExtra("customerCode",customer_id);
                                    intent.putExtra("customerName",customer_name);
                                    intent.putExtra("userName",username);
                                    startActivity(intent);
                                }
                                break;
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"Select Dates", Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception exception){}
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearAllSelection();
                    dialog.dismiss();
                }
            });
            // create and show the alert dialog
            dialog = builder.create();
            dialog.show();
        }catch (Exception exception){}
    }

    @Override
    protected void onResume() {
        clearAllSelection();
        super.onResume();
    }

    public void getDate(TextView dateEditext){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(ReportsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Date selectedMonthYearDate ;

                        try {
                            selectedMonthYearDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    .parse(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        //                                        userSelectedDate = selectedMonthYearDate;
                        dateEditext.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        if (selectedMonthYearDate != null) {
                            apidateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US).format(selectedMonthYearDate);
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
    private void getSettlementReport(String customer_id,String from_date,String to_date,int copy) throws JSONException {
        try {
            jsonObject=new JSONObject();
            jsonObject.put("CustomerCode",customer_id);
            jsonObject.put("FromDate",from_date);
            jsonObject.put("EndDate",to_date);
            jsonObject.put("ModifyUser",username);
            requestQueue = Volley.newRequestQueue(this);
            url= Utils.getBaseUrl(this) +"SalesApi/GetSettlementDetailReport?Requestdata="+jsonObject.toString();
            // Initialize a new JsonArrayRequest instance
            Log.w("Settlement_Summary:",url);
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Processing Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
            ArrayList<SettlementReceiptModel> settlementReceiptDetails =new ArrayList<>();
            ArrayList<SettlementReceiptModel.CurrencyDenomination> settlementDenomination=new ArrayList<>();
            ArrayList<SettlementReceiptModel.Expense> settlementExpenses=new ArrayList<>();
            jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try{
                    Log.w("SettlementResponse:",response.toString());
                    JSONArray receiptDetailsArray=response.optJSONArray("DetailList");
                    for (int i=0;i<receiptDetailsArray.length();i++){
                        JSONObject receiptObject=receiptDetailsArray.optJSONObject(i);
                        SettlementReceiptModel model =new SettlementReceiptModel();
                        model.setReceiptNo(receiptObject.optString("ReceiptNo"));
                        model.setReceiptDate(receiptObject.optString("ReceiptDate"));
                        model.setCustomerName(receiptObject.optString("CustomerName"));
                        model.setCustomerCode(receiptObject.optString("CustomerCode"));
                        model.setPaidAmount(receiptObject.optString("PaidAmount"));
                        model.setCreditAmount(receiptObject.optString("CreditAmount"));
                        double finalAmount = Double.parseDouble(receiptObject.optString("PaidAmount")) - Double.parseDouble(receiptObject.optString("CreditAmount"));
                        model.setFinalPaidAmount(Utils.twoDecimalPoint(finalAmount));
                        model.setPaymode(receiptObject.optString("Paymode"));
                        model.setChequeDate(receiptObject.optString("ChequeDate"));
                        model.setBankCode(receiptObject.optString("BankCode"));
                        model.setChequeNo(receiptObject.optString("ChequeNo"));
                        // Add the Values to main Arraylist
                        settlementReceiptDetails.add(model);
                    }
                    JSONArray denominationArray=response.optJSONArray("DetailSettlementList");
                    assert denominationArray != null;
                    if (denominationArray.length()>0){
                        for (int i=0;i<denominationArray.length();i++){
                            JSONObject denominationObject=denominationArray.optJSONObject(i);
                            SettlementReceiptModel.CurrencyDenomination denomination=new SettlementReceiptModel.CurrencyDenomination();
                            denomination.setDenomination(denominationObject.optString("DetailSettlementList"));
                            denomination.setCount(denominationObject.optString("DenominationCount"));
                            denomination.setTotal(denominationObject.optString("TotalAmount"));
                            // Adding the Denomination Details to the Arraylist
                            settlementDenomination.add(denomination);
                        }
                    }
                    JSONArray expenseArray=response.optJSONArray("ExpenseSettlementList");
                    assert expenseArray!=null;
                    if (denominationArray.length()>0){
                        for (int i=0;i<expenseArray.length();i++){
                            JSONObject expenseObject=expenseArray.optJSONObject(i);
                            SettlementReceiptModel.Expense expense=new SettlementReceiptModel.Expense();
                            expense.setExpeneName(expenseObject.optString("GroupName"));
                            expense.setExpenseTotal(expenseObject.optString("NetTotal"));
                            // Adding the expense Details to Arraylist
                            settlementExpenses.add(expense);
                        }
                    }
                    pDialog.dismiss();
                    clearAllSelection();
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
        }catch (Exception e){}
    }

    private void getSettleReceiptReport(String from_date,int copy) throws JSONException {
        try {
            jsonObject=new JSONObject();
            jsonObject.put("Date",from_date);
            jsonObject.put("User",username);

            requestQueue = Volley.newRequestQueue(this);
            url= Utils.getBaseUrl(this) +"ReportSettlementWithReceipt";
            // Initialize a new JsonArrayRequest instance
            Log.w("Settlement_Receip:",url + jsonObject);
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Processing Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
            ArrayList<SettlementReceiptModel> settlementReceiptDetails =new ArrayList<>();
            ArrayList<SettlementReceiptModel.CurrencyDenomination> settlementDenomination=new ArrayList<>();
            ArrayList<SettlementReceiptModel.Expense> settlementExpenses=new ArrayList<>();
            jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
                try{
                    Log.w("SettlementResponse:",response.toString());

                    String statusCode=response.optString("statusCode");
                    String statusMessage=response.optString("statusMessage");
                    if (statusCode.equals("1")){
                        JSONArray jsonArray=response.optJSONArray("responseData");
                        JSONObject detailObject=jsonArray.optJSONObject(0);

                        JSONArray receiptDetailsArray=detailObject.optJSONArray("reportSettlementWithReceiptDetails");

                    for (int i=0;i<receiptDetailsArray.length();i++){
                        JSONObject receiptObject=receiptDetailsArray.optJSONObject(i);
                        SettlementReceiptModel model =new SettlementReceiptModel();

                        model.setReceiptNo(receiptObject.optString("receiptNo"));
                        model.setReceiptDate(receiptObject.optString("receiptDate"));
                        model.setCustomerName(receiptObject.optString("customerName"));
                        model.setCustomerCode(receiptObject.optString("customerCode"));
                        model.setPaymode(receiptObject.optString("Paymode"));
//                        model.setChequeDate(receiptObject.optString("ChequeDate"));
//                        model.setBankCode(receiptObject.optString("BankCode"));
//                        model.setChequeNo(receiptObject.optString("ChequeNo"));


//                        model.setTotalInvoiceAmount(receiptObject.getString("totalInvoiceAmount"));
//                        model.setTotalPaidAmount(receiptObject.getString("totalPaidAmount"));
//                        model.setTotalLessAmount(receiptObject.getString("totalLessAmount"));
//                        model.setTotalCashAmount(receiptObject.getString("totalTransferAmount"));
//                        model.setTotalChequeAmount(receiptObject.getString("totalCheckAmount"));


                        // Add the Values to main Arraylist
                        settlementReceiptDetails.add(model);
                    }

                    JSONArray denominationArray=detailObject.optJSONArray("reportSettlementWithReceiptDenomination");
                    assert denominationArray != null;
                    if (denominationArray.length()>0){
                        for (int i=0;i<denominationArray.length();i++){
                            JSONObject denominationObject=denominationArray.optJSONObject(i);
                            SettlementReceiptModel.CurrencyDenomination denomination=new SettlementReceiptModel.CurrencyDenomination();
                            denomination.setDenomination(denominationObject.optString("denomination"));
                            denomination.setCount(denominationObject.optString("count"));
                            denomination.setTotal(denominationObject.optString("total"));
                            // Adding the Denomination Details to the Arraylist
                            settlementDenomination.add(denomination);
                        }
                    }

                    JSONArray expenseArray=detailObject.optJSONArray("reportSettlementWithReceiptExpenses");
                    assert expenseArray!=null;
                    if (denominationArray.length()>0){
                        for (int i=0;i<expenseArray.length();i++){
                            JSONObject expenseObject=expenseArray.optJSONObject(i);
                            SettlementReceiptModel.Expense expense=new SettlementReceiptModel.Expense();
                            expense.setExpeneName(expenseObject.optString("expenses"));
                            expense.setExpenseTotal(expenseObject.optString("total"));
                            // Adding the expense Details to Arraylist
                            settlementExpenses.add(expense);
                        }
                    }
                    pDialog.dismiss();
                    clearAllSelection();
                    }else {
                        Toast.makeText(getApplicationContext(),statusMessage,Toast.LENGTH_SHORT).show();
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
        }catch (Exception e){}
    }

   /* public void getCustomers(String groupCode){
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url=Utils.getBaseUrl(this) +"CustomerList";
        customerList=new ArrayList<>();
        allCustomersList=new ArrayList<>();
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("GroupCode",groupCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.w("Given_url:",url);
        dialog=new ProgressDialog(CustomerListActivity.this);
        dialog.setMessage("Loading Customers List...");
        dialog.setCancelable(false);
        dialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    try {
                        Log.w("Response_SAP_CUSTOMERS:", response.toString());

                        // {"statusCode":1,"statusMessage":"Success",
                        // "responseData":[{"customerCode":"CUS\/686","customerName":"VH FACTORY","groupCode":"100",
                        // "contactPerson":"","creditLimit":"150.000000","currencyCode":"SGD","currencyName":"Singapore Dollar",
                        // "taxType":"","taxCode":"SR","taxName":"Sales Standard Rated Supplier SR","taxPercentage":"7.000000",
                        // "balance":"21.600000","outstandingAmount":"128.400000","address":"","street":"","city":"","state":"",
                        // "zipCode":"","country":"","createDate":"13\/07\/2021","updateDate":"30\/07\/2021","active":"N","remark":""},
                        // {"customerCode":"WinApp","customerName":"WinApp","groupCode":"100","contactPerson":"","creditLimit":"0.000000"
                        // ,"currencyCode":"SGD","currencyName":"Singapore Dollar","taxType":"I","taxCode":"","taxName":"","taxPercentage":"",
                        // "balance":"0.000000","outstandingAmount":"0.000000","address":"","street":"","city":"","state":"","zipCode":""
                        // ,"country":"","createDate":"29\/07\/2021","updateDate":"30\/07\/2021","active":"N","remark":""}]}

                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
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

                        dialog.dismiss();
                        if (customerList.size()>0){
                            // new InsertCustomerTask().execute();
                            setAdapter(customerList);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                },
                error -> {
                    dialog.dismiss();
                    // Do something when error occurred
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


    public void getCustomers(){
        // Initialize a new RequestQueue instance
        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"CustomerList";
        jsonObject=new JSONObject();
        try {
            jsonObject.put("GroupCode","All");
            jsonObject.put("LocationCode",locationCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        customerList=new ArrayList<>();
        searchableCustomerList=new ArrayList<>();
        searchableCustomerList.add("Select Customer");
        Log.w("Given_url_customer:",url);
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Customer List Loading....");
        progressDialog.show();
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        Log.w("Response_Customer:", response.toString());
                        // pDialog.dismiss();
                        // Loop through the array elements

                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
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
                                model.setBillDiscPercentage(object.optString("discountPercentage"));
                                //  model.setCustomerBarcode(object.optString("BarCode"));
                                // model.setCustomerBarcode(String.valueOf(i));
                                if (object.optString("outstandingAmount").equals("null") || object.optString("outstandingAmount").isEmpty()) {
                                    model.setOutstandingAmount("0.00");
                                } else {
                                    model.setOutstandingAmount(object.optString("outstandingAmount"));
                                }
                                customerList.add(model);
                                searchableCustomerList.add(object.optString("customerName")+"~"+object.optString("customerCode"));
                                progressDialog.dismiss();
                            }
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Error,in getting Customer list",Toast.LENGTH_LONG).show();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Do something when error occurred
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

    private void getAllUsers() throws JSONException {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"UserList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_UserList:",url);
        usersList =new ArrayList<>();
        salesManList=new ArrayList<>();
        salesManList.add("Select Salesman");
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("User",username);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try{
                        Log.w("UserListResponse:",response.toString());
                        String statusCode=response.optString("statusCode");
                        String message=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            JSONArray responseData=response.getJSONArray("responseData");
                            for (int i=0;i<responseData.length();i++){
                                JSONObject object=responseData.optJSONObject(i);
                                UserListModel model=new UserListModel();
                                model.setUserName(object.optString("userName"));
                                model.setGender(object.optString("sex"));
                                model.setJobTitle(object.optString("jobTitle"));
                                usersList.add(model);
                                salesManList.add(object.optString("userName"));
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
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

    private void getInvoiceByProduct(String customer_id,String from_date,String to_date,String status,int copy) throws JSONException {
        // Initialize a new RequestQueue instance
        jsonObject=new JSONObject();
        jsonObject.put("User",username);
        jsonObject.put("CustomerCode",customer_id);
        jsonObject.put("Warehouse",locationCode);
        jsonObject.put("FromDate",from_date);
        jsonObject.put("ToDate",to_date);
        jsonObject.put("Status",status);

        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportInvoiceByProduct";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        invoiceByProductList=new ArrayList<>();
        productDetailsList=new ArrayList<>();
        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
                    try{
                        Log.w("InvoiceByProductRes:",response.toString());
                        String status_value="ALL";
                        if (status.equals("O")){
                            status_value="NOT PAID";
                        }else if (status.equals("C")){
                            status_value="PAID";
                        }else {
                            status_value="ALL";
                        }

                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray responseData=response.optJSONArray("responseData");
                            assert responseData != null;
                            if (responseData.length()>0){
                                JSONObject object=responseData.optJSONObject(0);
                                InvoiceByProductModel invoiceByProductModel=new InvoiceByProductModel();
                                invoiceByProductModel.setFromDate(object.optString("fromDate"));
                                invoiceByProductModel.setToDate(object.optString("toDate"));
                                invoiceByProductModel.setUserName(username);
                                invoiceByProductModel.setLocationCode(object.optString("warehouseCode"));
                                invoiceByProductModel.setLocationName(object.optString("warehouseName"));
                                invoiceByProductModel.setPrintType(status_value);
                                JSONArray detailsArray=object.optJSONArray("reportInvByProductDetails");
                                for (int i = 0; i< Objects.requireNonNull(detailsArray).length(); i++){
                                    JSONObject detailsObject=detailsArray.getJSONObject(i);
                                    InvoiceByProductModel.ProductDetails productDetails=new InvoiceByProductModel.ProductDetails();
                                    productDetails.setProductCode(detailsObject.optString("productCode"));
                                    productDetails.setProductName(detailsObject.optString("productName"));
                                    productDetails.setProductQty(detailsObject.optString("quantity"));
                                    productDetailsList.add(productDetails);
                                }
                                invoiceByProductModel.setProductDetails(productDetailsList);
                                invoiceByProductList.add(invoiceByProductModel);
                            }else {
                                pDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"No Record Found...",Toast.LENGTH_SHORT).show();
                            }
                        }
                        pDialog.dismiss();
                        clearAllSelection();
                        if (invoiceByProductList.size()>0){
                            setInvoiceByProductPrint(copy);
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

    private void getInvoicesBySummary(String customer_id,String from_date,String to_date,String status,int copy) throws JSONException {
        // Initialize a new RequestQueue instance
        jsonObject=new JSONObject();
        jsonObject.put("User",username);
        jsonObject.put("FromDate",from_date);
        jsonObject.put("ToDate",to_date);
        jsonObject.put("LocationCode",locationCode);
        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportSalesSummary";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_Summary:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();

        invoiceSummaryList=new ArrayList<>();
        reportSalesSummaryList =new ArrayList<>();
        reportSalesSummaryDetailsList =new ArrayList<>();
        reportSalesSummaryInvDetailsList=new ArrayList<>();
        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
            try{
                Log.w("ReportSalesSummary:",response.toString());

                pDialog.dismiss();
                String statusCode=response.optString("statusCode");
                String statusMessage=response.optString("statusMessage");
                if (statusCode.equals("1")){
                    JSONArray  summaryDetailsArray=response.optJSONArray("responseData");
                    assert summaryDetailsArray != null;
                    JSONObject detailObject=summaryDetailsArray.optJSONObject(0);
                    ReportSalesSummaryModel model=new ReportSalesSummaryModel();
                    model.setCompanyId(detailObject.optString("user"));
                    model.setCashInHand(detailObject.optString("cashInHand"));
                    model.setTotalSales(detailObject.optString("totalSales"));
                    model.setCashReceived(detailObject.optString("cashReceived"));
                    model.setRefunded(detailObject.optString("refunded"));

                    JSONArray cashSalesArray=detailObject.optJSONArray("cashSales");
                    for (int i = 0; i< Objects.requireNonNull(cashSalesArray).length(); i++){
                        JSONObject objectItem= cashSalesArray.optJSONObject(i);
                        ReportSalesSummaryModel.ReportSalesSummaryDetails reportSalesSummaryModel =
                                new ReportSalesSummaryModel.ReportSalesSummaryDetails();

                        reportSalesSummaryModel.setTransNo(objectItem.optString("transactionNo"));
                        reportSalesSummaryModel.setCustomer(objectItem.optString("customerName"));
                        reportSalesSummaryModel.setAmount(objectItem.optString("amount"));
                        reportSalesSummaryModel.setType(objectItem.optString("type"));
                        reportSalesSummaryModel.setPaymentDate(objectItem.optString("paymentDate"));

                        reportSalesSummaryDetailsList.add(reportSalesSummaryModel);
                    }
                    JSONArray invoiceArray=detailObject.optJSONArray("otherSales");
                    for (int i = 0; i< Objects.requireNonNull(invoiceArray).length(); i++){
                        JSONObject objectInv= invoiceArray.optJSONObject(i);
                        ReportSalesSummaryModel.ReportSalesSummaryInvDetails reportSalesSummaryInvModel =
                                new ReportSalesSummaryModel.ReportSalesSummaryInvDetails();

                        reportSalesSummaryInvModel.setTransNo(objectInv.optString("transactionNo"));
                        reportSalesSummaryInvModel.setCustomer(objectInv.optString("customerName"));
                        reportSalesSummaryInvModel.setAmount(objectInv.optString("amount"));
                        reportSalesSummaryInvModel.setType(objectInv.optString("type"));
                        reportSalesSummaryInvModel.setBalanceAmount(objectInv.optString("balanceAmount"));
                        reportSalesSummaryInvModel.setPaymentDate(objectInv.optString("paymentDate"));

                        reportSalesSummaryInvDetailsList.add(reportSalesSummaryInvModel);
                    }

                    model.setReportSalesSummaryDetailsList(reportSalesSummaryDetailsList);
                    if(reportSalesSummaryInvDetailsList.size()>0) {
                        model.setReportSalesSummaryInvDetailsList(reportSalesSummaryInvDetailsList);
                    }
                    reportSalesSummaryList.add(model);
                    clearAllSelection();
                    if (reportSalesSummaryInvDetailsList.size() > 0 || reportSalesSummaryDetailsList.size() > 0){
                        setInvoiceSummaryPrint(copy);
                    }else {
                        Toast.makeText(getApplicationContext(),"No Record Found...",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),statusMessage,Toast.LENGTH_SHORT).show();
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

    private void getCustomerStatement(String customer_id,String customer_name,String from_date,String to_date, String status,int copy) throws JSONException {

        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("CustomerCode",customer_id);
        jsonObject.put("Status",status);
        jsonObject.put("FromDate",from_date);
        jsonObject.put("ToDate",to_date);
        jsonObject.put("LocationCode",locationCode);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportCustomerStatement";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
       pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
       pDialog.setTitleText("Processing Please wait...");
       pDialog.setCancelable(false);
       pDialog.show();

        customerStateList =new ArrayList<>();
        custInvoiceDetailsList =new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
            try{
                Log.w("CustStat:",response.toString());

                //pDialog.dismiss();
                String statusCode=response.optString("statusCode");
                String statusMessage=response.optString("statusMessage");
                if (statusCode.equals("1")) {
                    JSONArray jsonArray = response.optJSONArray("responseData");
                    assert jsonArray != null;
                    if (jsonArray.length() > 0) {
                        JSONObject detailObject = jsonArray.optJSONObject(0);

                        CustomerStateModel model = new CustomerStateModel();
                        model.setCustomerName(detailObject.optString("customerName"));

                        double nettotal1 = 0.0;
                        double balance1 = 0.0;
                        JSONArray jsonArray1 = detailObject.optJSONArray("reportCustomerStatementDetails");

                        for (int i = 0; i < Objects.requireNonNull(jsonArray1).length(); i++) {
                            JSONObject object = jsonArray1.optJSONObject(i);
                            CustomerStateModel.CustInvoiceDetails custInvoiceDetailModel = new CustomerStateModel.CustInvoiceDetails();
                            custInvoiceDetailModel.setInvoiceNumber(object.optString("invoiceNo"));
                            custInvoiceDetailModel.setInvoiceDate(object.optString("invoiceDate"));
                            custInvoiceDetailModel.setNetTotal(object.optString("netTotal"));
                            custInvoiceDetailModel.setBalanceAmount(object.optString("balance"));
                            nettotal1 += Double.parseDouble(object.optString("netTotal"));
                            balance1 += Double.parseDouble(object.optString("balance"));
                            custInvoiceDetailsList.add(custInvoiceDetailModel);
                        }
                        model.setCustInvoiceDetailList(custInvoiceDetailsList);
                        customerStateList.add(model);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Customer Statement Found..!", Toast.LENGTH_SHORT).show();
                    }
                }
                clearAllSelection();
                pDialog.dismiss();
                if (customerStateList.size()>0){
                    setCustomerStatementPrint(copy);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }, error -> {
            // Do something when error occurred
            // pDialog.dismiss();
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

/*
    private void getCustomerStatement(String customer_id,String customer_name,String from_date,String to_date,String status,int copy) throws JSONException {
        // Initialize a new RequestQueue instance
        jsonObject=new JSONObject();
        jsonObject.put("CustomerCode",customer_id);
        jsonObject.put("FromDate",from_date);
        jsonObject.put("ToDate",to_date);
        jsonObject.put("Status",status);

        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportCustomerStatement";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_Summary:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        invoiceSummaryList=new ArrayList<>();
        invoiceSummaryDetailsList=new ArrayList<>();

        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try{
                        Log.w("CustomerStateMent:",response.toString());

                        String status_value="ALL";
                        if (status.equals("O")){
                            status_value="NOT PAID";
                        }else if (status.equals("C")){
                            status_value="PAID";
                        }else {
                            status_value="ALL";
                        }

                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray responseData=response.optJSONArray("responseData");
                            assert responseData != null;
                            if (responseData.length()>0){
                                JSONObject object=responseData.optJSONObject(0);
                                InvoiceSummaryModel invoiceSummaryModel =new InvoiceSummaryModel();
                                invoiceSummaryModel.setFromDate(object.optString("fromDate"));
                                invoiceSummaryModel.setToDate(object.optString("toDate"));
                                invoiceSummaryModel.setUserName(username);
                                invoiceSummaryModel.setCustomerName(customer_name);
                                invoiceSummaryModel.setPrintType(status_value);
                                JSONArray detailsArray=object.optJSONArray("reportCustomerStatementDetails");
                                for (int i = 0; i< Objects.requireNonNull(detailsArray).length(); i++){
                                    JSONObject detailsObject=detailsArray.getJSONObject(i);
                                    InvoiceSummaryModel.SummaryDetails details =new InvoiceSummaryModel.SummaryDetails();
                                    details.setInvoiceNumber(detailsObject.optString("invoiceNo"));
                                    details.setDate(detailsObject.optString("invoiceDate"));
                                    details.setPaidAmount(detailsObject.optString("paidAmount"));
                                    details.setBalanceAmount(detailsObject.optString("balance"));
                                    details.setNetTotal(detailsObject.optString("netTotal"));;
                                    invoiceSummaryDetailsList.add(details);
                                }
                                invoiceSummaryModel.setSummaryList(invoiceSummaryDetailsList);
                                invoiceSummaryList.add(invoiceSummaryModel);
                            }else {
                                pDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"No Record Found...",Toast.LENGTH_SHORT).show();
                            }
                        }
                        pDialog.dismiss();
                        clearAllSelection();
                        if (invoiceSummaryList.size()>0){
                            setCustomerStatementPrint(copy);
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
*/

    // Define the function for the Getting the Receipt details
    private void getReceiptDetails(String customer_id,String from_date,String to_date,int copy) throws JSONException {
        jsonObject=new JSONObject();
        jsonObject.put("User",username);
        jsonObject.put("CustomerCode",customer_id);
        jsonObject.put("FromDate",from_date);
        jsonObject.put("ToDate",to_date);
        jsonObject.put("LocationCode",locationCode);

        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportReceiptDetails";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_Summary:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();

        ArrayList<ReceiptDetailsModel> receiptDetails =new ArrayList<>();
        ArrayList<ReceiptDetailsModel.ReceiptDetails> receiptDetailsList=new ArrayList<>();
        ArrayList<ReceiptDetailsModel.ReceiptDetails.InvoiceDetails> invoiceDetailsList=new ArrayList<>();

        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try{
                        Log.w("ReceiptDetailsRes:",response.toString());
                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray responseData=response.optJSONArray("responseData");
                            assert responseData != null;
                            if (responseData.length()>0){
                                JSONObject object=responseData.optJSONObject(0);
                                ReceiptDetailsModel receiptModel =new ReceiptDetailsModel();
                                receiptModel.setFromDate(object.optString("fromDate"));
                                receiptModel.setToDate(object.optString("toDate"));
                                receiptModel.setCustomerName(object.optString("customerName"));
                                receiptModel.setCustomerCode(object.optString("customerCode"));
                                JSONArray detailsArray=object.optJSONArray("reportReceiptDetails");
                                for (int i = 0; i< Objects.requireNonNull(detailsArray).length(); i++){

                                    // Get the Receipt details for the Particular products
                                    JSONObject detailsObject=detailsArray.getJSONObject(i);
                                    ReceiptDetailsModel.ReceiptDetails details =new ReceiptDetailsModel.ReceiptDetails();
                                    details.setReceiptNumber(detailsObject.optString("receiptNo"));
                                    details.setReceiptDate(detailsObject.optString("receiptDate"));
                                    details.setPaymode(detailsObject.optString("paymode"));
                                    details.setPaymodeTotal(detailsObject.optString("paymodeTotal"));
                                    details.setReceiptTotal(detailsObject.optString("receiptTotal"));
                                    details.setCustomerCode(detailsObject.optString("customerCode"));
                                    details.setCustomerName(detailsObject.optString("customerName"));
                                    receiptDetailsList.add(details);

                                    // Get the Invoice Details for the Particular products

                                    JSONArray invoiceArray=detailsObject.optJSONArray("reportReceiptInvoiceDetails");
                                    for (int j=0;j<invoiceArray.length();j++){
                                        JSONObject invoiceObject=invoiceArray.optJSONObject(j);
                                        ReceiptDetailsModel.ReceiptDetails.InvoiceDetails invoiceDetails=new ReceiptDetailsModel.ReceiptDetails.InvoiceDetails();
                                        invoiceDetails.setInvoiceNumber(invoiceObject.optString("invoiceNo"));
                                        invoiceDetails.setInvoiceDate(invoiceObject.optString("invoiceDate"));
                                        invoiceDetails.setNetTotal(invoiceObject.optString("netTotal"));
                                        invoiceDetailsList.add(invoiceDetails);
                                    }
                                    details.setInvoiceDetailsList(invoiceDetailsList);
                                }
                                receiptModel.setReceiptDetailsList(receiptDetailsList);
                                receiptDetails.add(receiptModel);
                                pDialog.dismiss();
                            }else {
                                pDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"No Record Found...",Toast.LENGTH_SHORT).show();
                            }
                        }
                        pDialog.dismiss();
                        clearAllSelection();
                        if (invoiceDetailsList.size()>0 || receiptDetailsList.size()>0){
                            setReceiptDetailsPrint(receiptDetails,copy);
                        }else {
                            Toast.makeText(getApplicationContext(),"No Record Found...",Toast.LENGTH_SHORT).show();
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

    private void getReceiptSummary(String customer_id,String from_date,String to_date,int copy) throws JSONException {
        // Initialize a new RequestQueue instance
        jsonObject=new JSONObject();
        jsonObject.put("User",username);
        jsonObject.put(    "Status" , "O");
        jsonObject.put("CustomerCode",customer_id);
        jsonObject.put("FromDate",from_date);
        jsonObject.put("ToDate",to_date);
        jsonObject.put("LocationCode",locationCode);
        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportReceiptSummary";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_Summary:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        ArrayList<ReceiptSummaryModel> receiptDetails =new ArrayList<>();
        ArrayList<ReceiptSummaryModel.ReceiptDetails> receiptDetailsList=new ArrayList<>();
        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
                    try{
                        Log.w("ReceiptDetailsRes:",response.toString());
                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray responseData=response.optJSONArray("responseData");
                            assert responseData != null;
                            if (responseData.length()>0){
                                JSONObject object=responseData.optJSONObject(0);
                                ReceiptSummaryModel receiptModel =new ReceiptSummaryModel();
                                receiptModel.setFromDate(object.optString("fromDate"));
                                receiptModel.setToDate(object.optString("toDate"));
                                receiptModel.setCustomerName(object.optString("customerName"));
                                receiptModel.setCustomerCode(object.optString("customerCode"));
                                receiptModel.setUser(username);
                                JSONArray detailsArray=object.optJSONArray("reportReceiptSummaryDetails");
                                for (int i = 0; i< Objects.requireNonNull(detailsArray).length(); i++){
                                    // Get the Receipt details for the Particular products
                                    JSONObject detailsObject=detailsArray.getJSONObject(i);
                                    ReceiptSummaryModel.ReceiptDetails details =new ReceiptSummaryModel.ReceiptDetails();
                                    details.setInvoiceNo(detailsObject.optString("invoiceNo"));
                                    details.setReceiptNo(detailsObject.optString("receiptNo"));
                                    details.setReceiptDate(detailsObject.optString("receiptDate"));
                                    details.setCustomerCode(detailsObject.optString("customerCode"));
                                    details.setPaidAmount(detailsObject.optString("paidAmount"));
                                    details.setCustomerName(detailsObject.optString("customerName"));
                                    details.setPaymode(detailsObject.optString("payMode"));
                                    receiptDetailsList.add(details);
                                }
                                receiptModel.setReceiptDetailsList(receiptDetailsList);
                                receiptDetails.add(receiptModel);
                                pDialog.dismiss();
                            }else {
                                pDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"No Record Found...",Toast.LENGTH_SHORT).show();
                            }
                        }
                        pDialog.dismiss();
                        clearAllSelection();
                        if (receiptDetailsList.size()>0){
                            setReceiptSummaryPrint(receiptDetails,copy);
                        }else {
                            Toast.makeText(getApplicationContext(),"No Record Found...",Toast.LENGTH_SHORT).show();
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


    private void StockBadRequestReturn(int copy,String warehouse,String reason,String from_d,String to_d) throws JSONException {
        // Initialize a new RequestQueue instance
        jsonBody = new JSONObject();
        jsonBody.put("Warehouse",warehouse);
        jsonBody.put("Reason",reason);
        jsonBody.put("FromDate",from_d);
        jsonBody.put("ToDate",to_d);
        jsonBody.put("User",username);

        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportStockReturn";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url+"-"+jsonBody.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();

        stockBadRequestReturnList =new ArrayList<>();
        stockBadRequestReturnDetailsList =new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try{
                        Log.w("StockBadRequestReturn:",response.toString());

                        pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");

                        if (statusCode.equals("1")){
                            JSONArray  stockReturnArray = response.optJSONArray("responseData");
                            if (stockReturnArray.length()>0){
                                assert stockReturnArray != null;
                                JSONObject detailObject=stockReturnArray.optJSONObject(0);
                                StockBadRequestReturnModel model=new StockBadRequestReturnModel();
                                model.setWarehouseCode(detailObject.optString("warehouseCode"));
                                model.setReason(detailObject.optString("reason"));
                                model.setDate(detailObject.optString("date"));

                                JSONArray itemsArray=detailObject.optJSONArray("reportSalesReturnDetails");
                                for (int i = 0; i< Objects.requireNonNull(itemsArray).length(); i++){
                                    JSONObject objectItem= itemsArray.optJSONObject(i);
                                    StockBadRequestReturnModel.StockBadRequestReturnDetails stockBadRequestReturnModel =
                                            new StockBadRequestReturnModel.StockBadRequestReturnDetails();
                                    stockBadRequestReturnModel.setDescription(objectItem.optString("description"));
                                    stockBadRequestReturnModel.setQty(objectItem.optString("quantity"));
                                    stockBadRequestReturnModel.setUomCode(objectItem.optString("uomCode"));

                                    stockBadRequestReturnDetailsList.add(stockBadRequestReturnModel);
                                }
                                model.setStockBadRequestReturnDetailsList(stockBadRequestReturnDetailsList);
                                stockBadRequestReturnList.add(model);

                                if (stockBadRequestReturnDetailsList.size() > 0) {
                                    clearAllSelection();
                                    TSCPrinter printer = new TSCPrinter(this, printerMacId,"BadStock");
                                    printer.printStockBadRequestReturn(copy, from_date,to_date,stockBadRequestReturnList);
                                }else {
                                    Toast.makeText(getApplicationContext(),"No BadStock Found...",Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                clearAllSelection();
                                Toast.makeText(getApplicationContext(),"No Records Found...!",Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),statusMessage,Toast.LENGTH_SHORT).show();
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




    private void getReportStockSummary(int copy,String date,String warehouseCode,String from_d,String to_d) throws JSONException {
        // Initialize a new RequestQueue instance
        jsonBody = new JSONObject();
        jsonBody.put("Date",date);
        jsonBody.put("WarehouseCode",warehouseCode);
        jsonBody.put("FromDate",from_d);
        jsonBody.put("ToDate",to_d);
        jsonBody.put("User",username);

        requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportStockSummary";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_summary:",url+"-"+jsonBody.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        reportStockSummaryList =new ArrayList<>();
        reportStockSummaryDetailsList =new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try{
                        Log.w("ReportStockSummary:",response.toString());

                        pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            JSONArray  stockDetailsArray=response.optJSONArray("responseData");
                            if (stockDetailsArray.length() > 0){
                                assert stockDetailsArray != null;
                                JSONObject object1=stockDetailsArray.optJSONObject(0);
                                ReportStockSummaryModel model=new ReportStockSummaryModel();
                                //   model.setDeviceId(object1.optString(""));
                                //  model.setCompantId(object1.optString(""));

                                for (int i=0;i<stockDetailsArray.length();i++) {
                                    JSONObject object=stockDetailsArray.optJSONObject(i);
                                    ReportStockSummaryModel.ReportStockSummaryDetails reportStockSummaryModel = new ReportStockSummaryModel.ReportStockSummaryDetails();
                                    reportStockSummaryModel.setProductCode(object.optString("productCode"));
                                    reportStockSummaryModel.setProductName(object.optString("productName"));
                                    reportStockSummaryModel.setUomCode(object.optString("uomCode"));
                                    reportStockSummaryModel.setOpenQty(object.optString("openQty"));
                                    reportStockSummaryModel.setIn(object.optString("in"));
                                    reportStockSummaryModel.setSalesQty(object.optString("salesQty"));
                                    reportStockSummaryModel.setFocQty(object.optString("focQty"));
                                    reportStockSummaryModel.setRtnQty(object.optString("rtnQty"));
                                    reportStockSummaryModel.setOut(object.optString("out"));
                                    reportStockSummaryModel.setBalance(object.optString("balance"));
                                    reportStockSummaryModel.setOtherInorOut(object.optString("otherInorOut"));

                                    Log.e("pdtcodee",""+object.optString("productCode"));
                                    reportStockSummaryDetailsList.add(reportStockSummaryModel);
                                }
                                model.setReportStockSummaryDetailsList(reportStockSummaryDetailsList);
                                reportStockSummaryList.add(model);

                                if (reportStockSummaryDetailsList.size()>0){
                                    TSCPrinter printer=new TSCPrinter(this,printerMacId,"StockSummary");
                                    printer.printReportStockSummary(copy,from_date,to_date,locationCode,companyCode,reportStockSummaryList);
                                    clearAllSelection();
                                }else {
                                    Toast.makeText(getApplicationContext(),"No StockSummary Found...",Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(getApplicationContext(),"No StockSummary Found...",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(),statusMessage,Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            // pDialog.dismiss();
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

    public void setReceiptSummaryPrint(ArrayList<ReceiptSummaryModel> receiptDetails,int copy) throws IOException {
        if (validatePrinterConfiguration()){
            if (printerType.equals("Zebra Printer")){
                ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(this,printerMacId);
                zebraPrinterActivity.printReceiptSummary(copy,receiptDetails);
            }else if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"ReceiptSummary");
                printer.printReceiptSummary(copy,from_date,to_date,receiptDetails);
                Utils.setSignature("");
            }
            else {
                Toast.makeText(getApplicationContext(),"This Printer not Support...!",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

    public void setReceiptDetailsPrint(ArrayList<ReceiptDetailsModel> receiptDetails,int copy) throws IOException {
        if (validatePrinterConfiguration()){
            if (printerType.equals("Zebra Printer")){
                ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(this,printerMacId);
                zebraPrinterActivity.printReceiptDetails(copy,receiptDetails);
            }else if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"ReceiptDetails");
                printer.printReceiptDetail(copy,from_date,to_date,receiptDetails);
                Utils.setSignature("");
            }
            else {
                Toast.makeText(getApplicationContext(),"This Printer not Support...!",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

    public void setInvoiceByProductPrint(int copy){
        if (validatePrinterConfiguration()){
            if (printerType.equals("Zebra Printer")){
                ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(this,printerMacId);
                zebraPrinterActivity.printInvoiceByProduct(copy,invoiceByProductList);
            }else if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"InvoiceByProduct");
                try {
                    printer.printInvoiceByProduct(copy,productDetailsList,from_date,to_date,locationCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Utils.setSignature("");
            }
            else {
                Toast.makeText(getApplicationContext(),"This Printer not Support...!",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

    public void setInvoiceSummaryPrint(int copy){
        if (validatePrinterConfiguration()){
            if (printerType.equals("Zebra Printer")){
                ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(this,printerMacId);
                zebraPrinterActivity.printInvoiceSummary(copy,invoiceSummaryList);
            }else if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"SalesReport");
                try {
                    printer.printReportSalesSummary(1,from_date,to_date,reportSalesSummaryList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(),"This Printer not Support...!",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

    public void setCustomerStatementPrint(int copy) throws IOException {
        if (validatePrinterConfiguration()){
            if (printerType.equals("Zebra Printer")){
                ZebraPrinterActivity zebraPrinterActivity=new ZebraPrinterActivity(this,printerMacId);
                zebraPrinterActivity.printCustomerStatement(copy,invoiceSummaryList);
            }else if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"Customer Statement");
                try {
                    printer.printCustomerStatement(copy,customerStateList,from_date,to_date);
                    Utils.setSignature("");
                }catch (Exception e){}

            }
            else {
                Toast.makeText(getApplicationContext(),"This Printer not Support...!",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

    public void increaseInteger(View view) {
        minteger = minteger + 1;
        display(minteger);

    }public void decreaseInteger(View view) {
        if (minteger > 1){
            minteger = minteger - 1;
            display(minteger);
        }
    }

    private void display(int number) {
        noOfCopyText.setText("" +number);
    }


    public void setDataToAdapter(ArrayList<String> arrayList) {
        try {
            // Creating ArrayAdapter using the string array and default spinner layout
            customerListSpinner.setTitle("Select Customer");
            arrayAdapter = new ArrayAdapter<String>(ReportsActivity.this, android.R.layout.simple_spinner_item, arrayList);
            // Specify layout to be used when list of choices appears
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Applying the adapter to our spinner
            customerListSpinner.setAdapter(arrayAdapter);
            // customerListSpinner.setOnItemSelectedListener(this);
        }catch (Exception e){}

    }


    public void setUserAdapter(ArrayList<String> arrayList) {
        try {
            // Creating ArrayAdapter using the string array and default spinner layout
            userListSpinner.setTitle("Select Salesman");
            arrayAdapter = new ArrayAdapter<>(ReportsActivity.this, android.R.layout.simple_spinner_item, arrayList);
            // Specify layout to be used when list of choices appears
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Applying the adapter to our spinner
            userListSpinner.setAdapter(arrayAdapter);
            userListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!arrayList.get(position).equals("Select Salesman")){
                        username=arrayList.get(position).toString();
                    }else {
                        username=user.get(SessionManager.KEY_USER_NAME);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    username=user.get(SessionManager.KEY_USER_NAME);
                }
            });
            // customerListSpinner.setOnItemSelectedListener(this);
        }catch (Exception e){}

    }

    public void setStatusList(ArrayList<String> arrayList) {
        try {
            // Creating ArrayAdapter using the string array and default spinner layout
            statusListSpinner.setTitle("Select Status");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ReportsActivity.this, android.R.layout.simple_spinner_item, arrayList);
            // Specify layout to be used when list of choices appears
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Applying the adapter to our spinner
            statusListSpinner.setAdapter(arrayAdapter);
            // customerListSpinner.setOnItemSelectedListener(this);
        }catch (Exception ed){}
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}