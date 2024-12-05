package com.winapp.saperpUNICO.ReportPreview;

import static com.winapp.saperpUNICO.utils.Utils.twoDecimalPoint;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.winapp.saperpUNICO.R;
import com.winapp.saperpUNICO.ReportPreview.adapter.RoCustomerPreviewPrintAdapter;
import com.winapp.saperpUNICO.ReportPreview.adapter.RoSupplierPreviewPrintAdapter;
import com.winapp.saperpUNICO.ReportPreview.adapter.SapRoCustomerOutstandingARPreviewAdapter;
import com.winapp.saperpUNICO.model.CustomerStateModel;
import com.winapp.saperpUNICO.utils.Constants;
import com.winapp.saperpUNICO.utils.Utils;
import com.winapp.saperpUNICO.zebraprinter.TSCPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class RoSupplierStatementPreviewActivity extends AppCompatActivity {
    private TextView companyNametext;
    private TextView companyAddress1Text;
    private TextView companyAddress2Text;
    private TextView companyAddress3Text;
    private TextView companyPhoneText;
    private TextView companyGstText;
    private String company_name;
    private String company_address1;
    private String company_address2;
    private String company_address3;
    String companyId = "1";
    String locationCode;
    SweetAlertDialog pDialog;
    private ArrayList<CustomerStateModel> customerStateList ;
    private ArrayList<CustomerStateModel.CustInvoiceDetails> custInvoiceDetailsList ;
    private ArrayList<CustomerStateModel.CustInvoiceDetailsAR> custInvoiceDetailsARList ;

    private TextView titlel,fromdat,todat,nettotal,nettotal_txt_final,nettotalAr1;
    private TextView balance,balanceAr1,balance_final,cust_name;
    private RecyclerView supplierListView;
    private RoSupplierPreviewPrintAdapter adapter;
    private SapRoCustomerOutstandingARPreviewAdapter adapter1;

    private RecyclerView customerListView1;
    private LinearLayout ArCustlistLayl,fromdatlay;
    double mNettotal =0.0;
    double mBalance =0.0;
    double mNettotal1 =0.0;
    double mBalance1 =0.0;
    double mNettotalFinal =0.0;
    double mBalanceFinal =0.0;
    private String printerMacId;
    private String printerType;
    private SharedPreferences sharedPreferences;
    private String from_date;
    private String to_date;
    private String customer_name;
    private String customer_code;
    private String supplier_code;
    private String actionReport;
    private String username;
    private String allUserFilter;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ro_supplier_statement_preview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.w("activity_cg",getClass().getSimpleName().toString());

        companyNametext =findViewById (R.id.company_name);
        companyAddress1Text =findViewById (R.id.company_addr1);
        companyAddress2Text =findViewById (R.id.company_addr2);
        companyAddress3Text =findViewById (R.id.company_addr3);
        companyGstText =findViewById (R.id.company_gst);
        companyPhoneText =findViewById (R.id.company_phone);
        fromdat =findViewById (R.id.fromdate_txt);
        titlel =findViewById (R.id.title);
        fromdatlay =findViewById (R.id.fromdate_sup_lay);
        todat =findViewById (R.id.todate_txt);
        nettotal =findViewById (R.id.nettotal_txt);
        balance =findViewById (R.id.balance_txt);
        cust_name =findViewById (R.id.customer_txt);
        supplierListView = findViewById (R.id.rv_supState_list);
        customerListView1 = findViewById (R.id.rv_customerlist1);
        ArCustlistLayl = findViewById (R.id.ArCustlistLay);
        nettotalAr1 =findViewById (R.id.nettotal_txt1);
        nettotal_txt_final =findViewById (R.id.nettotal_txt_final);
        balanceAr1 =findViewById (R.id.balance_txt1);
        balance_final =findViewById (R.id.balance_txt_final);

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType=sharedPreferences.getString("printer_type","");
        printerMacId=sharedPreferences.getString("mac_address","");

        if (getIntent()!=null){
            from_date=getIntent().getStringExtra("fromDate");
            to_date=getIntent().getStringExtra("toDate");
            companyId=getIntent().getStringExtra("companyId");
            locationCode=getIntent().getStringExtra("locationCode");
            customer_name=getIntent().getStringExtra("customerName");
            customer_code=getIntent().getStringExtra("customerCode");
            supplier_code=getIntent().getStringExtra("roSupplierCode");
            actionReport=getIntent().getStringExtra("actionRP");
            username=getIntent().getStringExtra("userName");
            allUserFilter=getIntent().getStringExtra("allUserFilter");


            fromdat.setText(from_date);
            todat.setText(to_date);
        }


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

        String fromDateString = new SimpleDateFormat("yyyyMMdd").format(fromDate);
        String toDateString = new SimpleDateFormat("yyyyMMdd").format(toDate);

        if(actionReport.equals("supplierRp")){
            Objects.requireNonNull(getSupportActionBar()).setTitle("Supplier Statement");
            fromdatlay.setVisibility(View.VISIBLE);
            titlel.setText("Supplier Statement");
            try {
                getSupplierStatement(supplier_code,toDateString,fromDateString,"",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Objects.requireNonNull(getSupportActionBar()).setTitle("Supplier Statement(as on Date)");
            fromdatlay.setVisibility(View.INVISIBLE);
            titlel.setText("Supplier Statement(as on Date)");
            try {
                getSupplierStatementDate(supplier_code,toDateString,"",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void getSupplierStatementDate(String supplier_id,String to_date, String status,int copy) throws JSONException {

        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("VendorCode",supplier_id);
        jsonObject.put("Status","");
        jsonObject.put("ToDate",to_date);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportSupplierStatementToDate";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_supStatda:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();

        customerStateList =new ArrayList<>();
        custInvoiceDetailsList =new ArrayList<>();
        custInvoiceDetailsARList =new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
            try{
                Log.w("supplStatdat_res:",response.toString());

                //pDialog.dismiss();
                String statusCode=response.optString("statusCode");
                String statusMessage=response.optString("statusMessage");
                if (statusCode.equals("1")) {
                    JSONArray jsonArray = response.optJSONArray("responseData");
                    assert jsonArray != null;
                    if (jsonArray.length() > 0) {
                        JSONObject detailObject = jsonArray.optJSONObject(0);

                        CustomerStateModel model = new CustomerStateModel();
                        model.setCustomerName(detailObject.optString("vendorName"));

                        double nettotal1 = 0.0;
                        double nettotal2 = 0.0;
                        double balance1 = 0.0;
                        double balance2 = 0.0;
                        JSONArray jsonArray1 = detailObject.optJSONArray("reportSupplierStatementDetails");

                        for (int i = 0; i < Objects.requireNonNull(jsonArray1).length(); i++) {
                            JSONObject object = jsonArray1.optJSONObject(i);
                            CustomerStateModel.CustInvoiceDetails custInvoiceDetailModel = new CustomerStateModel.CustInvoiceDetails();
                            custInvoiceDetailModel.setInvoiceNumber(object.optString("invoiceNo"));
                            custInvoiceDetailModel.setInvoiceDate(object.optString("invoiceDate"));
                            custInvoiceDetailModel.setNetTotal(Utils.twoDecimalPoint(Double.parseDouble(object.optString("netTotal"))));
                            custInvoiceDetailModel.setBalanceAmount(Utils.twoDecimalPoint(Double.parseDouble(object.optString("balance"))));
                            nettotal1 += Double.parseDouble(object.optString("netTotal"));
                            balance1 += Double.parseDouble(object.optString("balance"));
                            mNettotal = Double.parseDouble(twoDecimalPoint(nettotal1));
                            mBalance = Double.parseDouble(twoDecimalPoint(balance1));

                            custInvoiceDetailsList.add(custInvoiceDetailModel);
                        }
                        JSONArray jsonArray2 = detailObject.optJSONArray("reportCustomerARCreditMemoStatementDetails");
                        if (jsonArray2.length() > 0) {
                            for (int i = 0; i < Objects.requireNonNull(jsonArray2).length(); i++) {
                                JSONObject object = jsonArray2.optJSONObject(i);
                                CustomerStateModel.CustInvoiceDetailsAR custInvoiceDetailModel1 = new CustomerStateModel.CustInvoiceDetailsAR();

                                custInvoiceDetailModel1.setInvoiceNumber(object.optString("arInvoiceNo"));
                                custInvoiceDetailModel1.setInvoiceDate(object.optString("arInvoiceDate"));
                                custInvoiceDetailModel1.setNetTotal(object.optString("arNetTotal"));
                                custInvoiceDetailModel1.setBalanceAmount(object.optString("arBalance"));

                                nettotal2 += Double.parseDouble(object.optString("arNetTotal"));
                                balance2 += Double.parseDouble(object.optString("arBalance"));
                                mNettotal1 = Double.parseDouble(twoDecimalPoint(nettotal2));
                                mBalance1 = Double.parseDouble(twoDecimalPoint(balance2));

                                custInvoiceDetailsARList.add(custInvoiceDetailModel1);
                            }
                        }
                        model.setCustInvoiceDetailList(custInvoiceDetailsList);
                        model.setCustInvoiceDetailsARList(custInvoiceDetailsARList);
                        customerStateList.add(model);

                        if (custInvoiceDetailsList.size() > 0) {
                            setCustomerAdapter(customerStateList);
                            nettotal.setText(String.valueOf(mNettotal));
                            balance.setText(String.valueOf(mBalance));
                        }

                    } else {
                        finish();
                        Toast.makeText(getApplicationContext(), "No Supplier Statement Found..!", Toast.LENGTH_SHORT).show();
                    }
                }
                pDialog.dismiss();
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

    private void getSupplierStatement(String supplier_id,String to_date,String from_date, String status,int copy) throws JSONException {

        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("VendorCode",supplier_id);
        jsonObject.put("Status","");
        jsonObject.put("FromDate",from_date);
        jsonObject.put("ToDate",to_date);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        url= Utils.getBaseUrl(this) +"ReportSupplierStatement";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_supStat:",url+"/"+jsonObject.toString());
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();

        customerStateList =new ArrayList<>();
        custInvoiceDetailsList =new ArrayList<>();
        custInvoiceDetailsARList =new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
            try{
                Log.w("supplStat_res:",response.toString());

                //pDialog.dismiss();
                String statusCode=response.optString("statusCode");
                String statusMessage=response.optString("statusMessage");
                if (statusCode.equals("1")) {
                    JSONArray jsonArray = response.optJSONArray("responseData");
                    assert jsonArray != null;
                    if (jsonArray.length() > 0) {
                        JSONObject detailObject = jsonArray.optJSONObject(0);

                        CustomerStateModel model = new CustomerStateModel();
                        model.setCustomerName(detailObject.optString("vendorName"));

                        double nettotal1 = 0.0;
                        double nettotal2 = 0.0;
                        double balance1 = 0.0;
                        double balance2 = 0.0;
                        JSONArray jsonArray1 = detailObject.optJSONArray("reportSupplierStatementDetails");

                        for (int i = 0; i < Objects.requireNonNull(jsonArray1).length(); i++) {
                            JSONObject object = jsonArray1.optJSONObject(i);
                            CustomerStateModel.CustInvoiceDetails custInvoiceDetailModel = new CustomerStateModel.CustInvoiceDetails();
                            custInvoiceDetailModel.setInvoiceNumber(object.optString("invoiceNo"));
                            custInvoiceDetailModel.setInvoiceDate(object.optString("invoiceDate"));
                            custInvoiceDetailModel.setNetTotal(Utils.twoDecimalPoint(Double.parseDouble(object.optString("netTotal"))));
                            custInvoiceDetailModel.setBalanceAmount(Utils.twoDecimalPoint(Double.parseDouble(object.optString("balance"))));
                            nettotal1 += Double.parseDouble(object.optString("netTotal"));
                            balance1 += Double.parseDouble(object.optString("balance"));
                            mNettotal = Double.parseDouble(twoDecimalPoint(nettotal1));
                            mBalance = Double.parseDouble(twoDecimalPoint(balance1));

                            custInvoiceDetailsList.add(custInvoiceDetailModel);
                        }
                        JSONArray jsonArray2 = detailObject.optJSONArray("reportCustomerARCreditMemoStatementDetails");
                        if (jsonArray2.length() > 0) {
                            for (int i = 0; i < Objects.requireNonNull(jsonArray2).length(); i++) {
                                JSONObject object = jsonArray2.optJSONObject(i);
                                CustomerStateModel.CustInvoiceDetailsAR custInvoiceDetailModel1 = new CustomerStateModel.CustInvoiceDetailsAR();

                                custInvoiceDetailModel1.setInvoiceNumber(object.optString("arInvoiceNo"));
                                custInvoiceDetailModel1.setInvoiceDate(object.optString("arInvoiceDate"));
                                custInvoiceDetailModel1.setNetTotal(object.optString("arNetTotal"));
                                custInvoiceDetailModel1.setBalanceAmount(object.optString("arBalance"));

                                nettotal2 += Double.parseDouble(object.optString("arNetTotal"));
                                balance2 += Double.parseDouble(object.optString("arBalance"));
                                mNettotal1 = Double.parseDouble(twoDecimalPoint(nettotal2));
                                mBalance1 = Double.parseDouble(twoDecimalPoint(balance2));

                                custInvoiceDetailsARList.add(custInvoiceDetailModel1);
                            }
                        }
                            model.setCustInvoiceDetailList(custInvoiceDetailsList);
                            model.setCustInvoiceDetailsARList(custInvoiceDetailsARList);
                            customerStateList.add(model);

                            if (custInvoiceDetailsList.size() > 0) {
                                setCustomerAdapter(customerStateList);
                                nettotal.setText(String.valueOf(mNettotal));
                                balance.setText(String.valueOf(mBalance));
                            }
//                        if (custInvoiceDetailsARList.size() > 0) {
//                            ArCustlistLayl.setVisibility(View.VISIBLE);
//
//                            setCustomerAdapterAR(customerStateList);
//
//                            nettotalAr1.setText(String.valueOf(mNettotal1));
//                            balanceAr1.setText(String.valueOf(mBalance1));
//                            mNettotalFinal = mNettotal - mNettotal1;
//                            mBalanceFinal = mBalance - mBalance1;
//                            nettotal_txt_final.setText(twoDecimalPoint(mNettotalFinal));
//                            balance_final.setText(twoDecimalPoint(mBalanceFinal));
//                            Log.w("custnettot", "" + mNettotal + ".." + mBalance);
//                            Log.w("custnettot11", "" + mNettotal1 + ".." + mBalance1);
//                            Log.w("custnettotfinal", "" + mNettotalFinal + ".." + mBalanceFinal);
//                        } else {
//                            ArCustlistLayl.setVisibility(View.GONE);
//                        }

                    } else {
                        finish();
                        Toast.makeText(getApplicationContext(), "No Supplier Statement Found..!", Toast.LENGTH_SHORT).show();
                    }
                }
                pDialog.dismiss();
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

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    public void setCustomerAdapter(ArrayList<CustomerStateModel> customerStateLists ) {
        cust_name.setText(customerStateLists.get(0).getCustomerName());
        supplierListView.setHasFixedSize(true);
        supplierListView.setLayoutManager(new LinearLayoutManager(RoSupplierStatementPreviewActivity.this, LinearLayoutManager.VERTICAL, false));
        adapter = new RoSupplierPreviewPrintAdapter(RoSupplierStatementPreviewActivity.this, custInvoiceDetailsList, "Transfer Detail");
        supplierListView.setAdapter(adapter);
    }
    public void setCustomerAdapterAR(ArrayList<CustomerStateModel> customerStateLists ) {
        //  cust_name.setText(customerStateLists.get(0).getCustomerName());
        customerListView1.setHasFixedSize(true);
        customerListView1.setLayoutManager(new LinearLayoutManager(RoSupplierStatementPreviewActivity.this, LinearLayoutManager.VERTICAL, false));
        adapter1 = new SapRoCustomerOutstandingARPreviewAdapter(RoSupplierStatementPreviewActivity.this, custInvoiceDetailsARList, "Transfer Detail");
        customerListView1.setAdapter(adapter1);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.print_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_print) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                Toast.makeText(getApplicationContext(),"This device does not support bluetooth",Toast.LENGTH_SHORT).show();
            } else if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled :)
                Toast.makeText(getApplicationContext(),"Enable bluetooth and connect the printer",Toast.LENGTH_SHORT).show();
            } else {
                // Bluetooth is enabled
                if (!printerType.isEmpty()){
                    try {
                        setSupplierStatementPrint(1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Please configure Printer",Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }else if (id==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    public void setSupplierStatementPrint(int copy) throws IOException {
        if (validatePrinterConfiguration()){
            if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"SupplierStatement");
                printer.printSupplierStatement(copy,customerStateList,from_date,to_date);
            } else {
                Toast.makeText(getApplicationContext(),"This Printer not Support...!",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

}