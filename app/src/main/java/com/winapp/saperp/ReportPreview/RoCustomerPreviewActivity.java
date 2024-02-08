package com.winapp.saperp.ReportPreview;

import static com.winapp.saperp.utils.Utils.twoDecimalPoint;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.winapp.saperp.R;
import com.winapp.saperp.ReportPreview.adapter.RoCustomerPreviewPrintAdapter;
import com.winapp.saperp.model.CustomerStateModel;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.Utils;
import com.winapp.saperp.zebraprinter.TSCPrinter;

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

public class RoCustomerPreviewActivity extends AppCompatActivity {
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
    private TextView fromdat,todat,nettotal,balance,cust_name;
    private RecyclerView customerListView;
    private RoCustomerPreviewPrintAdapter adapter;
    String mNettotal,mBalance;

    private String printerMacId;
    private String printerType;
    private SharedPreferences sharedPreferences;
    private String from_date;
    private String to_date;
    private String customer_name;
    private String customer_code;
    private String username;
    private String allUserFilter;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ro_customer_preview);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Customer Outstanding");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        companyNametext =findViewById (R.id.company_name);
        companyAddress1Text =findViewById (R.id.company_addr1);
        companyAddress2Text =findViewById (R.id.company_addr2);
        companyAddress3Text =findViewById (R.id.company_addr3);
        companyGstText =findViewById (R.id.company_gst);
        companyPhoneText =findViewById (R.id.company_phone);
        fromdat =findViewById (R.id.fromdate_txt);
        todat =findViewById (R.id.todate_txt);
        nettotal =findViewById (R.id.nettotal_txt);
        balance =findViewById (R.id.balance_txt);
        cust_name =findViewById (R.id.customer_txt);
        customerListView = findViewById (R.id.rv_customerlist);

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

        try {
            getCustomerStatement(customer_code,fromDateString,toDateString,"O",1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCustomerStatement(String customer_id,String from_date,String to_date, String status,int copy) throws JSONException {

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

                        double nettotal1 = 0.00;
                        double balance1 = 0.00;
                        JSONArray jsonArray1 = detailObject.optJSONArray("reportCustomerStatementDetails");

                        for (int i = 0; i < Objects.requireNonNull(jsonArray1).length(); i++) {
                            JSONObject object = jsonArray1.optJSONObject(i);
                            CustomerStateModel.CustInvoiceDetails custInvoiceDetailModel = new CustomerStateModel.CustInvoiceDetails();
                            custInvoiceDetailModel.setInvoiceNumber(object.optString("invoiceNo"));
                            custInvoiceDetailModel.setInvoiceDate(object.optString("invoiceDate"));
                            custInvoiceDetailModel.setNetTotal(Utils.twoDecimalPoint(Double.parseDouble(object.optString("netTotal"))));
                            custInvoiceDetailModel.setBalanceAmount(Utils.twoDecimalPoint(Double.parseDouble(object.optString("balance"))));
                            nettotal1 += Double.parseDouble(object.optString("netTotal"));
                            balance1 += Double.parseDouble(object.optString("balance"));
                            custInvoiceDetailsList.add(custInvoiceDetailModel);
                        }
                        nettotal.setText(Utils.twoDecimalPoint(nettotal1));
                        balance.setText(Utils.twoDecimalPoint(balance1));
                        model.setCustInvoiceDetailList(custInvoiceDetailsList);
                        customerStateList.add(model);
                    } else {
                        finish();
                        Toast.makeText(getApplicationContext(), "No Customer Statement Found..!", Toast.LENGTH_SHORT).show();
                    }
                }
                pDialog.dismiss();
                if (customerStateList.size()>0){
                    setCustomerAdapter(customerStateList);
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

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    public void setCustomerAdapter(ArrayList<CustomerStateModel> customerStateLists ) {
        cust_name.setText(customerStateLists.get(0).getCustomerName());
        customerListView.setHasFixedSize(true);
        customerListView.setLayoutManager(new LinearLayoutManager(RoCustomerPreviewActivity.this, LinearLayoutManager.VERTICAL, false));
        adapter = new RoCustomerPreviewPrintAdapter(RoCustomerPreviewActivity.this, custInvoiceDetailsList, "Transfer Detail");
        customerListView.setAdapter(adapter);
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
                        setCustomerStatementPrint(1);
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

    public void setCustomerStatementPrint(int copy) throws IOException {
        if (validatePrinterConfiguration()){
            if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"CustomerStatement");
                printer.printCustomerStatement(copy,customerStateList,from_date,to_date);
            } else {
                Toast.makeText(getApplicationContext(),"This Printer not Support...!",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }

}