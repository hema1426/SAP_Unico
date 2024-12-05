package com.winapp.saperpUNICO.ReportPreview;


import static com.winapp.saperpUNICO.utils.Utils.twoDecimalPoint;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import com.winapp.saperpUNICO.ReportPreview.adapter.RoInvoicebySummaryPreviewAdapter;
import com.winapp.saperpUNICO.ReportPreview.adapter.SapPosting_INVPreviewPrintAdapter;
import com.winapp.saperpUNICO.ReportPreview.adapter.SapPosting_SOPreviewPrintAdapter;
import com.winapp.saperpUNICO.model.InvoiceSummaryModel;
import com.winapp.saperpUNICO.model.ReportPostingInvSOModel;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SapPostingInv_SOPreviewActivity extends AppCompatActivity {
    private TextView companyNametext;
    private TextView companyAddress1Text;
    private TextView companyAddress2Text;
    private TextView companyAddress3Text;
    private TextView companyPhoneText;
    private LinearLayout Invoicelist_lay;
    public LinearLayout SOlist_lay;
    private TextView companyGstText;
    private String company_name;
    private String company_address1;
    private String company_address2;
    private String company_address3;

    private TextView customerCode;
    private TextView customerName;
    private TextView userName;
    private TextView subTotall;
    private TextView netTotall;
    private TextView gst_txt;
    private SweetAlertDialog pDialog;
    private String doNo;
    private RecyclerView rv_postInv_list;
    private RecyclerView rv_postSo_list;
    double sum_totalInv = 0.0;
    double sum_BalInv = 0.0;
    double sum_totalSO = 0.0;
    double sum_BalSO = 0.0;
    private ArrayList<ReportPostingInvSOModel> reportPostingInvSOModelArrayList;
    private ArrayList<ReportPostingInvSOModel.ReportInvoiceDetails> reportInvoiceDetailsArrayList;
    private ArrayList<ReportPostingInvSOModel.ReportSODetails> reportSODetailsArrayList;
    public SapPosting_SOPreviewPrintAdapter sapPostingSoPreviewPrintAdapter;
    public SapPosting_INVPreviewPrintAdapter sapPostingInvPreviewPrintAdapter;
    private TextView fromdatel;
    private TextView totalreceived;
    private TextView totalreceivedInv;
    private TextView Invbalance;
    private TextView totalSales;
    private TextView totalcash_received;
    private TextView totalrefund;
    private TextView totalcashinhand;
    private TextView todatel;
    String username = "winapp";
    String status = "";
    String companyId = "1";
    private ArrayList<InvoiceSummaryModel> invoiceSummaryModel ;
    private ArrayList<InvoiceSummaryModel.SummaryDetails> invoiceSummaryDetailsList ;
    private TextView locationl;
    public String fromdatetxt = "17-05-2022";
    public String todatetxt = "12_09-2022";
    public String loccodetxt = "HQ";
    private RecyclerView rv_invsum_perv_listl;
    public RoInvoicebySummaryPreviewAdapter roInvoicebySummaryPreviewAdapter;
    public double sum_balamt = 0.0;
    public double sum_totalamt = 0.0;
    public double sum_paidamt = 0.0;
    public TextView balancel;
    public TextView nettotal;
    public TextView paidl;
    private String printerMacId;
    private String printerType;
    private SharedPreferences sharedPreferences;
    private String from_date;
    private String to_date;
    private String locationCode;
    private String customer_name;
    private String customer_code = "";
    private JSONObject jsonObject;
    private String url;
    TextView balSo_listl,totalSo_listl,balInv_listl,totalInv_listl ;

    private RequestQueue requestQueue;
    private JsonObjectRequest jsonObjectRequest;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sap_ro_postinginv_so_preview_print);
        getSupportActionBar().setTitle("Posting Invoice & UnPosting SalesOrder");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.w("activity_cg",getClass().getSimpleName().toString());

        companyNametext =findViewById (R.id.company_name);
        companyAddress1Text =findViewById (R.id.company_addr1);
        companyAddress2Text =findViewById (R.id.company_addr2);
        companyAddress3Text =findViewById (R.id.company_addr3);
        companyGstText =findViewById (R.id.company_gst);
        companyPhoneText =findViewById (R.id.company_phone);

        SOlist_lay = findViewById (R.id.postingSo_lay);
        Invoicelist_lay =findViewById (R.id.postingInv_lay);
        rv_postInv_list =findViewById (R.id.rv_postinv_perview_list);
        rv_postSo_list =findViewById (R.id.rv_post_so_list);
        balSo_listl =findViewById (R.id.balSo_list);
       totalSo_listl =findViewById (R.id.totalSo_list);
        balInv_listl =findViewById (R.id.balInv_list);
        totalInv_listl =findViewById (R.id.totalInv_list);
        todatel =findViewById (R.id.todate);
        fromdatel =findViewById (R.id.fromdate);
        userName =findViewById (R.id.user_pre);

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType=sharedPreferences.getString("printer_type","");
        printerMacId=sharedPreferences.getString("mac_address","");

        if (getIntent()!=null){
            fromdatetxt=getIntent().getStringExtra("fromDate");
            todatetxt=getIntent().getStringExtra("toDate");
            companyId=getIntent().getStringExtra("companyId");
            loccodetxt=getIntent().getStringExtra("locationCode");
            customer_name=getIntent().getStringExtra("customerName");
            customer_code=getIntent().getStringExtra("customerCode");
            username=getIntent().getStringExtra("userName");
            status=getIntent().getStringExtra("status");
        }

        from_date=fromdatetxt;
        to_date=todatetxt;
        locationCode=loccodetxt;
        fromdatel.setText(fromdatetxt);
        todatel.setText(todatetxt);

        Date fromDate = null;
        try {
            fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(fromdatetxt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date toDate = null;
        try {
            toDate = new SimpleDateFormat("dd/MM/yyyy").parse(todatetxt);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String fromDateString = new SimpleDateFormat("yyyyMMdd").format(fromDate);
        String toDateString = new SimpleDateFormat("yyyyMMdd").format(toDate);



        try {
            getPostingInvSoSummary(1,fromDateString,toDateString,username,status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getPostingInvSoSummary(int copy,String fromDate,String toDate,String user,String status) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("FromDate",fromDate);
//        jsonBody.put("LocationCode",loccodetxt);
        jsonBody.put("CustomerCode",customer_code);
        jsonBody.put("DocStatus","");
        jsonBody.put("ToDate",toDate);
        jsonBody.put("User",user);
//        "Warehouse":"VAN 5"
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"ReportPostingInvoiceUnPostingSalesOrder";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_PostInv:",url+jsonBody);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
        reportPostingInvSOModelArrayList =new ArrayList<>();
        reportInvoiceDetailsArrayList =new ArrayList<>();
        reportSODetailsArrayList =new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try{
                        Log.w("Report_PostInvSO:",response.toString());

                        pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            JSONArray  summaryDetailsArray=response.optJSONArray("responseData");
                            assert summaryDetailsArray != null;
                            JSONObject detailObject=summaryDetailsArray.optJSONObject(0);
                            ReportPostingInvSOModel model=new ReportPostingInvSOModel();

                            model.setCompanyId(detailObject.optString("user"));
                            userName.setText(detailObject.optString("user"));

                            JSONArray detailArray=detailObject.optJSONArray("invoiceList");
                            for (int i = 0; i< Objects.requireNonNull(detailArray).length(); i++){
                                JSONObject objectItem= detailArray.optJSONObject(i);
                                ReportPostingInvSOModel.ReportInvoiceDetails reportInvoiceDetailsModel =
                                        new ReportPostingInvSOModel.ReportInvoiceDetails();
//
                                reportInvoiceDetailsModel.setInvoiceNo(objectItem.optString("invoiceNo"));
                                reportInvoiceDetailsModel.setCustomerCode(objectItem.optString("customerCode"));
                                reportInvoiceDetailsModel.setCustomerName(objectItem.optString("customerName"));
                                reportInvoiceDetailsModel.setBalance(objectItem.optString("balance"));
                                reportInvoiceDetailsModel.setTotal(objectItem.optString("total"));
                                reportInvoiceDetailsModel.setInvoiceDate(objectItem.optString("invoiceDate"));

                                reportInvoiceDetailsArrayList.add(reportInvoiceDetailsModel);

                                sum_totalInv += Double.parseDouble(reportInvoiceDetailsModel.getTotal());
                                sum_BalInv += Double.parseDouble(reportInvoiceDetailsModel.getBalance());

                            }
                            JSONArray detailArray1=detailObject.optJSONArray("salesOrderList");
                            for (int i = 0; i< Objects.requireNonNull(detailArray1).length(); i++){
                                JSONObject objectItem1= detailArray1.optJSONObject(i);
                                ReportPostingInvSOModel.ReportSODetails reportSODetailsModel =
                                        new ReportPostingInvSOModel.ReportSODetails();

                                reportSODetailsModel.setInvoiceNo(objectItem1.optString("soNo"));
                                reportSODetailsModel.setCustomerCode(objectItem1.optString("customerCode"));
                                reportSODetailsModel.setCustomerName(objectItem1.optString("customerName"));
                                reportSODetailsModel.setBalance(objectItem1.optString("balance"));
                                reportSODetailsModel.setTotal(objectItem1.optString("total"));
                                reportSODetailsModel.setInvoiceDate(objectItem1.optString("soDate"));

                                reportSODetailsArrayList.add(reportSODetailsModel);

                                sum_totalSO += Double.parseDouble(reportSODetailsModel.getTotal());
                                sum_BalSO += Double.parseDouble(reportSODetailsModel.getBalance());
                            }
                            if(reportInvoiceDetailsArrayList.size()>0) {
                                Invoicelist_lay.setVisibility(View.VISIBLE);
                                model.setReportInvoiceDetailsArrayList(reportInvoiceDetailsArrayList);
                                setAdapterInvoice(reportInvoiceDetailsArrayList);
                            }
                            if(reportSODetailsArrayList.size()>0) {
                                model.setReportSODetailsArrayList(reportSODetailsArrayList);
                                SOlist_lay.setVisibility(View.VISIBLE);
                                setAdapterSO(reportSODetailsArrayList);
                            }
                            reportPostingInvSOModelArrayList.add(model);
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

    public void setAdapterInvoice(ArrayList<ReportPostingInvSOModel.ReportInvoiceDetails> invoiceDetailsArrayList ) {
       balInv_listl.setText(twoDecimalPoint(sum_BalInv));
       totalInv_listl.setText(twoDecimalPoint(sum_totalInv));

        rv_postInv_list.setHasFixedSize(true);
        rv_postInv_list.setLayoutManager(new LinearLayoutManager(SapPostingInv_SOPreviewActivity.this,
                LinearLayoutManager.VERTICAL, false));
        sapPostingInvPreviewPrintAdapter = new SapPosting_INVPreviewPrintAdapter(SapPostingInv_SOPreviewActivity.this,
                invoiceDetailsArrayList);
        rv_postInv_list.setAdapter(sapPostingInvPreviewPrintAdapter);
    }
    public void setAdapterSO(ArrayList<ReportPostingInvSOModel.ReportSODetails> soDetailsArrayList ) {
        balSo_listl.setText(twoDecimalPoint(sum_BalSO));
        totalSo_listl.setText(twoDecimalPoint(sum_totalSO));

        rv_postSo_list.setHasFixedSize(true);
        rv_postSo_list.setLayoutManager(new LinearLayoutManager(SapPostingInv_SOPreviewActivity.this,
                LinearLayoutManager.VERTICAL, false));
        sapPostingSoPreviewPrintAdapter = new SapPosting_SOPreviewPrintAdapter(SapPostingInv_SOPreviewActivity.this,
                soDetailsArrayList);
        rv_postSo_list.setAdapter(sapPostingSoPreviewPrintAdapter);
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
                        setPostingInvSoPrint(1);
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

    public void setPostingInvSoPrint(int copy) throws IOException {
        if (validatePrinterConfiguration()){
            if (printerType.equals("TSC Printer")){
                TSCPrinter printer=new TSCPrinter(this,printerMacId,"POstingInvSo");
                printer.printPostingInvSOReport(copy,from_date, to_date,reportPostingInvSOModelArrayList);
            }
        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }
}
