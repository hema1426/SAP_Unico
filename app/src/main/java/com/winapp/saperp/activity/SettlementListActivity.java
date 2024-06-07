package com.winapp.saperp.activity;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.winapp.saperp.R;
import com.winapp.saperp.adapter.CurrencyDenominationAdapter;
import com.winapp.saperp.adapter.ExpenseAdapter;
import com.winapp.saperp.adapter.SettlementListAdapter;
import com.winapp.saperp.model.CurrencyModel;
import com.winapp.saperp.model.ExpenseModel;
import com.winapp.saperp.model.SettlementListModel;
import com.winapp.saperp.printpreview.SettlementPrintPreview;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.Utils;
import com.winapp.saperp.zebraprinter.TSCPrinter;
import com.winapp.saperp.zebraprinter.ZebraPrinterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettlementListActivity extends NavigationActivity implements View.OnClickListener {

    private ArrayList<SettlementListModel> settlementList;
    private SettlementListAdapter settlementListAdapter;
    private RecyclerView settlementListView;
    private ProgressDialog dialog;
    private LinearLayout editLayout;
    private LinearLayout printPreviewLayout;
    private LinearLayout printLayout;
    private FloatingActionButton editButton;
    private FloatingActionButton printPreviewButton;
    private FloatingActionButton printButton;
    private BottomSheetBehavior behavior;
    private LinearLayout transLayout;
    private TextView cancelSheet;
    private String editSettlementNumber;
    private TextView optionSettlementNumber;
    private String editSettlementDate;
    private String editSettlementUser;
    private String editLocationCode;
    private String editSettlementAmount;
    private int REQUEST_CODE=12;
    private String redirection;
    private boolean isMove=false;
    private LinearLayout emptyLayout;
    private String username;
    private SessionManager session;
    private HashMap<String ,String> user;
    private ArrayList<CurrencyModel> currencyList;
    private CurrencyDenominationAdapter currencyDenominationAdapter;
    private ExpenseAdapter expenseAdapter;
    private String printerMacId;
    private String printerType;
    private ArrayList<ExpenseModel> expenseList;
    private SharedPreferences sharedPreferences;
    private String locationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_settlement_list, contentFrameLayout);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settlements");

        sharedPreferences = getSharedPreferences("PrinterPref", MODE_PRIVATE);
        printerType = sharedPreferences.getString("printer_type", "");
        printerMacId = sharedPreferences.getString("mac_address", "");

        dialog=new ProgressDialog(this);
        settlementListView=findViewById(R.id.settlement_view);
        editLayout=findViewById(R.id.edit_layout);
        printPreviewLayout=findViewById(R.id.print_preview_layout);
        printLayout=findViewById(R.id.print_layout);
        editButton=findViewById(R.id.edit_settlement);
        printPreviewButton=findViewById(R.id.print_preview);
        printButton=findViewById(R.id.print);
        transLayout=findViewById(R.id.trans_layout);
        cancelSheet=findViewById(R.id.option_cancel);
        optionSettlementNumber=findViewById(R.id.settlement_no);
        emptyLayout=findViewById(R.id.empty_layout);
        session=new SessionManager(this);
        user=session.getUserDetails();
        username=user.get(SessionManager.KEY_USER_NAME);
        locationCode=user.get(SessionManager.KEY_LOCATION_CODE);

        editLayout.setOnClickListener(this);
        printPreviewButton.setOnClickListener(this);
        printPreviewLayout.setOnClickListener(this);
        printLayout.setOnClickListener(this);
        editButton.setOnClickListener(this);
        printButton.setOnClickListener(this);
        cancelSheet.setOnClickListener(this);

        try {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("User",username);
            jsonObject.put("LocationCode",locationCode);
            getAllSettlement(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        View bottomSheet = findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_SETTLING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED");
                        getSupportActionBar().setTitle("Select Option");
                        transLayout.setVisibility(View.VISIBLE);
                        transLayout.setClickable(false);
                        transLayout.setEnabled(false);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                        getSupportActionBar().setTitle("Settlements");
                        if (isMove){
                            if (redirection.equals("Settlement")){
                                Intent intent=new Intent(getApplicationContext(),SettlementActivity.class);
                                intent.putExtra("action","Edit");
                                intent.putExtra("editDate",editSettlementDate);
                                intent.putExtra("editSettlementNumber",editSettlementNumber);
                                intent.putExtra("editLocationCode",editLocationCode);
                                intent.putExtra("editUser",editSettlementUser);
                                startActivityForResult(intent,REQUEST_CODE);
                            }else {
                                Intent intent=new Intent(getApplicationContext(), SettlementPrintPreview.class);
                                intent.putExtra("settlementNumber",editSettlementNumber);
                                intent.putExtra("outstandingAmount",editLocationCode);
                                startActivityForResult(intent,REQUEST_CODE);
                            }
                        }
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

    }

    public void showSettlementOption(){
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void hideOption(){
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    public void getAllSettlement(JSONObject jsonObject){
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"SettlementList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_Settlement_URL:",url+".."+jsonObject);
        settlementList=new ArrayList<>();
        dialog=new ProgressDialog(this);
        dialog.setMessage("Getting Settlements...");
        dialog.setCancelable(false);
        dialog.show();
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url,
                jsonObject,
                response -> {
                    try{
                        Log.w("SettlementResponse:",response.toString());
                        // Loop through the array elements

                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray detailsArray=response.optJSONArray("responseData");
                            if (detailsArray.length()>0){
                                for (int i=0;i<detailsArray.length();i++){
                                    JSONObject settlementObject = detailsArray.getJSONObject(i);
                                    SettlementListModel settlement =new SettlementListModel();
                                    settlement.setSettlementNumber(settlementObject.optString("settlementNo"));
                                    settlement.setSettlementDate(settlementObject.optString("settlementDate"));
                                    settlement.setPaidAmount(settlementObject.optString("totalAmount"));
                                    settlement.setTotalExpense(settlementObject.optString("totalExpense"));
                                    settlement.setSettlementBy(settlementObject.optString("user"));
                                    settlement.setLocationCode(settlementObject.optString("locationCode"));
                                    settlement.setSettlementCode(settlementObject.optString("code"));
                                    settlementList.add(settlement);
                                }
                                if (settlementList.size()>0){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            settlementListView.setVisibility(View.VISIBLE);
                                            emptyLayout.setVisibility(View.GONE);
                                            setAdapter(settlementList);
                                            dialog.dismiss();
                                        }
                                    });
                                }else {
                                    dialog.dismiss();
                                    settlementListView.setVisibility(View.GONE);
                                    emptyLayout.setVisibility(View.VISIBLE);
                                }
                            }else {
                                dialog.dismiss();
                                settlementListView.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                            }
                        }else {

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

    private void setAdapter(ArrayList<SettlementListModel> settlementList) {
        settlementListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        settlementListAdapter = new SettlementListAdapter(settlementList, new SettlementListAdapter.CallBack() {
            @Override
            public void showOption(String settlementNumber, String date, String user, String location) {
                editSettlementNumber=settlementNumber;
                editSettlementDate=date;
                editLocationCode=location;
                editSettlementUser=user;
                optionSettlementNumber.setText("SettlementNo : "+editSettlementNumber);
                showSettlementOption();
            }

        });
        settlementListView.setAdapter(settlementListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_menu, menu);
        MenuItem menuItem1=menu.findItem(R.id.action_total);
        MenuItem menuItem2=menu.findItem(R.id.action_cart);
        menuItem1.setVisible(false);
        menuItem2.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//finish();
            // onBackPressed();
            finish();
        } else if (item.getItemId() == R.id.action_add) {
            Intent intent=new Intent(getApplicationContext(),SettlementActivity.class);
            intent.putExtra("action","Add");
            startActivityForResult(intent,REQUEST_CODE);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.print){
            try {
                hideOption();
                getSettlementDetails(editSettlementNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (view.getId()==R.id.print_preview){
            isMove=true;
            redirection="PrintPreView";
            hideOption();
        }else if (view.getId()==R.id.print_preview_layout){
            isMove=true;
            redirection="PrintPreView";
            hideOption();
        }else if (view.getId()==R.id.edit_settlement){
            isMove=true;
            redirection="Settlement";
            hideOption();
         /*   Intent intent=new Intent(getApplicationContext(),SettlementActivity.class);
            intent.putExtra("action","Edit");
            intent.putExtra("editDate",editSettlementDate);
            intent.putExtra("editSettlementNumber",editSettlementNumber);
            intent.putExtra("editLocationCode",editLocationCode);
            intent.putExtra("editUser",editSettlementUser);
            startActivity(intent);*/
        }else if (view.getId()==R.id.edit_layout){
            isMove=true;
            redirection="Settlement";
            hideOption();
          /*  Intent intent=new Intent(getApplicationContext(),SettlementActivity.class);
            intent.putExtra("action","Edit");
            intent.putExtra("editDate",editSettlementDate);
            intent.putExtra("editSettlementNumber",editSettlementNumber);
            intent.putExtra("editLocationCode",editLocationCode);
            intent.putExtra("editUser",editSettlementUser);
            startActivity(intent);*/
        }else if (view.getId()==R.id.print_layout){
            try {
                hideOption();
                getSettlementDetails(editSettlementNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (view.getId()==R.id.option_cancel){
            isMove=false;
            hideOption();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE  && resultCode  == RESULT_OK) {
                String requiredValue = data.getStringExtra("key");
                assert requiredValue != null;
                if (requiredValue.equals("Refresh")){
                    try {
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("User",username);
                        jsonObject.put("LocationCode",locationCode);
                        getAllSettlement(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            //Toast.makeText(Activity.this, ex.toString(), oast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        finish();
    }

    private void getSettlementDetails(String editSettlementNumber) throws JSONException {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("SettlementNo",editSettlementNumber);
        jsonObject.put("LocationCode",locationCode);
        String url= Utils.getBaseUrl(this) +"SettlementDetails";
        // Initialize a new JsonArrayRequest instance
        ProgressDialog dialog=new ProgressDialog(this);
        dialog.setMessage("Getting Settlement Details...");
        dialog.setCancelable(false);
        dialog.show();
        Log.w("Given_settlemtEdit_url:",url+jsonObject);
        expenseList=new ArrayList<>();
        expenseList.clear();
        currencyList=new ArrayList<>();
        currencyList.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, jsonObject,
                response -> {
                    try{
                        Log.w("PrintSettlmentRes:",response.toString());
                        dialog.dismiss();
                        if (response.length()>0){
                            String statusCode=response.optString("statusCode");
                            if (statusCode.equals("1")){
                                JSONArray responseDataArray=response.getJSONArray("responseData");
                                JSONObject responseObject=responseDataArray.getJSONObject(0);
                                String settlementAmount=responseObject.optString("totalAmount");
                                JSONArray denominationArray=responseObject.optJSONArray("settlementDetails");
                                for (int i = 0; i< Objects.requireNonNull(denominationArray).length(); i++){
                                    JSONObject currencyObject = denominationArray.optJSONObject(i);
                                    CurrencyModel product =new CurrencyModel();
                                    product.setCurrencyName(currencyObject.optString("currency"));
                                    product.setCount(currencyObject.optString("count"));
                                    product.setId(String.valueOf(i));
                                    product.setTotal(currencyObject.optString("total"));
                                    currencyList.add(product);
                                }

                                JSONArray expenseArray=responseObject.optJSONArray("settlementExpensesDetails");
                                for (int i = 0; i< Objects.requireNonNull(expenseArray).length(); i++){
                                    JSONObject expenseObject=expenseArray.optJSONObject(i);
                                    ExpenseModel model=new ExpenseModel();
                                    model.setExpenseName(expenseObject.optString("groupName"));
                                    model.setExpenseId(String.valueOf(i+1));
                                    model.setGroupNo(expenseObject.optString("groupNo"));
                                    model.setExpenseTotal(expenseObject.optString("amount"));
                                    expenseList.add(model);
                                }
                            }else {
                                Toast.makeText(getApplicationContext(),"Error in Getting Data...",Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (currencyList.size()>0){
                            hideOption();
                            printSettlement();
                        }else {
                            Toast.makeText(getApplicationContext(),"No Data Found...",Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            Log.w("Error_throwing:",error.toString());
            Toast.makeText(getApplicationContext(),"Server Error,Please check",Toast.LENGTH_LONG).show();
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

    public void printSettlement(){
        if (Utils.validatePrinterConfiguration(this,printerType,printerMacId)) {

        if (printerType.equals("Zebra Printer")) {
            ZebraPrinterActivity zebraPrinterActivity = new ZebraPrinterActivity(SettlementListActivity.this, printerMacId);
            zebraPrinterActivity.printSettlement(1,
                    editSettlementNumber.toString(),
                    editSettlementDate,
                    editLocationCode,
                    editSettlementUser,
                    currencyList,expenseList
            );
        }else if (printerType.equals("TSC Printer")){
            TSCPrinter printer=new TSCPrinter(this,printerMacId,"Settlement");
            try {
                printer.setSettlementPrintSave(1,
                        editSettlementNumber.toString(),
                        editSettlementDate,
                        editLocationCode,
                        editSettlementUser,
                        currencyList,
                        expenseList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        }else {
            Toast.makeText(getApplicationContext(),"Please configure the Printer",Toast.LENGTH_SHORT).show();
        }
    }


    /* @Override
    protected void onResume() {
        super.onResume();
        try {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("CompanyCode",companyCode);
            getAllSettlement(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}