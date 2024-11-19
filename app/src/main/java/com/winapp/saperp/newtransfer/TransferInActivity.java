package com.winapp.saperp.newtransfer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.winapp.saperp.R;
import com.winapp.saperp.activity.CartActivity;
import com.winapp.saperp.activity.StockRequestListActivity;
import com.winapp.saperp.activity.TransferListProductActivity;
import com.winapp.saperp.activity.TransferProductAddActivity;
import com.winapp.saperp.db.DBHelper;
import com.winapp.saperp.model.CreateInvoiceModel;
import com.winapp.saperp.model.ItemGroupList;
import com.winapp.saperp.utils.CaptureSignatureView;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.ImageUtil;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.SharedPreferenceUtil;
import com.winapp.saperp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TransferInActivity extends AppCompatActivity {
    SweetAlertDialog pDialog;
    private ArrayList<TransferInModel> transferInModels;
    private ArrayList<TransferInModel.TransferInDetails> transferInDetailsl;
    private ArrayList<LocationModel.LocationDetails> locationDetailsl;
    public TransferInAdapter transferInAdapter;
    public RecyclerView transferInView;
    public TextView pdtsizel;
    public TextView fromlocationl;
    public TextView tolocationl;
    public TextView toolbartxt;
    public LinearLayout toolbarImglay;
    public ImageView saveImg;
    int count=0;

    private ArrayList<ItemGroupList> itemGroup;
    private AppCompatSpinner groupspinner;
    public LinearLayout tolocationlay;
    public LinearLayout fromlocationlay;
    public String fromWarehouseCode = "";
    public String toWarehouseCode = "";
    public String fromWarehouseName = "";
    public EditText search_ed;
    public TextView emptytxt;
    public static String currentDate;
    String companyCode;
    String username;
    HashMap<String ,String> user;
    SessionManager session;
    static ProgressDialog progressDialog;
    public String companyName;
    public String locationCode;
    public static String customerCode;
    public static boolean isPrintEnable=false;
    private ImageView cancelSheet;
    public static TextView selectedBank;
    private Button cancelButton;
    private Button okButton;
    public static EditText amountText;
    private AlertDialog alert;
    public CheckBox invoicePrintCheck;
    public TextView saveTitle;
    public ImageView signatureCapture;
    public LinearLayout attachement_layoutInvl ;
    public static String signatureString="";
    public static String imageString;
    private AlertDialog signatureAlert;
    private TextView saveMessage;
    private TextView default_uom_transfl;
    private String transferType;
    private String settingUOMval ="PCS";
    private String islocationPermission;

    private SharedPreferenceUtil sharedPreferenceUtil;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_in);

        Log.w("activity_cg",getClass().getSimpleName().toString());

        session=new SessionManager(this);
        user=session.getUserDetails();
        progressDialog =new ProgressDialog(this);
        sharedPreferenceUtil = new SharedPreferenceUtil(this);
      //  settingUOMval = sharedPreferenceUtil.getStringPreference(sharedPreferenceUtil.KEY_SETTING_TRANS_UOM, "");
        Log.w("transferUOM..", "" + settingUOMval);

        companyCode=user.get(SessionManager.KEY_COMPANY_CODE);
        companyName=user.get(SessionManager.KEY_COMPANY_NAME);
        username=user.get(SessionManager.KEY_USER_NAME);
        locationCode=user.get(SessionManager.KEY_LOCATION_CODE);
        islocationPermission=user.get(SessionManager.IS_LOCATION_PERMISSION);

        default_uom_transfl  = findViewById(R.id.default_uom_transf);
        transferInView = findViewById(R.id.rv_transferInList);
        tolocationl = findViewById(R.id.tolocation_transfer);
        fromlocationl = findViewById(R.id.fromlocation_transfer);
        tolocationlay = findViewById(R.id.toloc_lay);
        fromlocationlay = findViewById(R.id.fromloc_lay);
        search_ed = findViewById(R.id.searchBar_transfer);
        emptytxt = findViewById(R.id.empty_txt);
        pdtsizel = findViewById(R.id.pdtsize);
        groupspinner = findViewById(R.id.spinner_status);
        saveImg= findViewById(R.id.save_image);
        toolbarImglay= findViewById(R.id.iv_customtoolbar_img);
        toolbartxt= findViewById(R.id.tv_customtoolbar_title);

        default_uom_transfl.setText(settingUOMval);
        transferInDetailsl=new ArrayList<>();

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        currentDate = df1.format(c);

        if (getIntent()!=null){
            transferType=getIntent().getStringExtra("transferType");
            toolbartxt.setText(transferType);
            if (transferType.equals("Stock Request")){
                fromlocationlay.setEnabled(true);
                tolocationlay.setEnabled(false);
                tolocationl.setText(locationCode);
                toWarehouseCode = locationCode;
            }else {
                if (transferType.equals("Transfer In")) {
                    if (islocationPermission.equalsIgnoreCase("Y")){
                        fromlocationlay.setEnabled(true);
                        tolocationlay.setEnabled(true);
                    }else {
                        fromlocationlay.setEnabled(true);
                        tolocationlay.setEnabled(false);
                    }
                    tolocationl.setText(locationCode);
                    toWarehouseCode = locationCode;
                } else {
                    if (islocationPermission.equalsIgnoreCase("Y")){
                        fromlocationlay.setEnabled(true);
                        tolocationlay.setEnabled(true);
                    }else {
                        fromlocationlay.setEnabled(false);
                        tolocationlay.setEnabled(true);
                    }
                    fromlocationl.setText(locationCode);
                    fromWarehouseCode = locationCode;
                }
            }
        }

        toolbarImglay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count=0;
                for(int i = 0; i<transferInDetailsl.size(); i++){
                    if(!transferInDetailsl.get(i).getQty().isEmpty()){
                        count+=Integer.parseInt(transferInDetailsl.get(i).getQty());
                    }
                }
                if (count>0){
                    showDeleteAlert();
                }else {
                    finish();
                }
            }
        });

        saveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i<transferInDetailsl.size(); i++){
                    if(!transferInDetailsl.get(i).getQty().isEmpty()){
                        count+=Integer.parseInt(transferInDetailsl.get(i).getQty());
                    }
                }
                Log.e("qqty",""+count);
                if (fromWarehouseCode!=null && toWarehouseCode!=null && !toWarehouseCode.isEmpty() &&
                        !fromWarehouseCode.isEmpty()){
                    if (count > 0){
                        try {
                            showSaveAlert(transferType);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"Add product first...!",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Select Locations...!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        fromlocationlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getfromlocationDialog(locationDetailsl);
            }
        });

        tolocationlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fromWarehouseCode.isEmpty() && fromWarehouseCode != null) {
                    gettolocationDialog(locationDetailsl);
                } else{
                    Toast.makeText(TransferInActivity.this, "Select from location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(transferInDetailsl == null ){
            emptytxt.setVisibility(View.VISIBLE);
            pdtsizel.setVisibility(View.GONE);
            search_ed.setEnabled(false);
            transferInView.setVisibility(View.GONE);
        }

        try {
           // getGrouplist();
            getLocationlist();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        search_ed.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    String searchtxt = s.toString();
                   // if (!searchtxt.isEmpty()) {
                        filter(searchtxt.toString());
                    //}
//                    else{
//                        setTransferInAdapter(transferInDetailsl);
//                    }
                  //  Log.w("transFiltSize",""+transferInDetailsl.size());

                }else{
                    Log.w("transFiltSizeaa",""+transferInDetailsl.size());
                    setTransferInAdapter(transferInDetailsl);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence query, int start, int before, int count) {
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            int count=0;
            for(int i = 0; i<transferInDetailsl.size(); i++){
                if(!transferInDetailsl.get(i).getQty().isEmpty()){
                    count+=Integer.parseInt(transferInDetailsl.get(i).getQty());
                }
            }
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

    public void showDeleteAlert(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(TransferInActivity.this);
        builder1.setMessage("Data Will be Cleared are you sure want to back?");
        builder1.setCancelable(false);
        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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

    @SuppressLint("NotifyDataSetChanged")
    public void setTransferInAdapter(ArrayList<TransferInModel.TransferInDetails> transferInList){
        try {
            transferInView.setVisibility(View.VISIBLE);
            pdtsizel.setVisibility(View.VISIBLE);
            search_ed.setEnabled(true);
            emptytxt.setVisibility(View.GONE);
            pdtsizel.setText(transferInList.size()+" Products");
            transferInAdapter = new TransferInAdapter(getApplicationContext(), transferInList,transferType);
            transferInView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
            transferInView.setItemAnimator(new DefaultItemAnimator());
            transferInView.setAdapter(transferInAdapter);
            transferInAdapter.notifyDataSetChanged();
            //categoriesView.setVisibility(View.VISIBLE);
            //emptyLayout.setVisibility(View.GONE);
        }catch (Exception ex){
            Log.e("TAG","Error in Populating the data:"+ex.getMessage());
        }
    }

    public void showSaveAlert(String mode){
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
            signatureCapture = customLayout.findViewById(R.id.signature_capture);
            attachement_layoutInvl = customLayout.findViewById(R.id.attachement_layoutInv);
            attachement_layoutInvl.setVisibility(View.GONE);

            TextView noOfCopy = customLayout.findViewById(R.id.no_of_copy);
            Button copyPlus = customLayout.findViewById(R.id.increase);
            Button copyMinus = customLayout.findViewById(R.id.decrease);
            Button signatureButton = customLayout.findViewById(R.id.btn_signature);
            LinearLayout copyLayout = customLayout.findViewById(R.id.print_layout);

            //invoicePrintCheck.setVisibility(View.GONE);
            if (mode.equals("Transfer In") || mode.equals("Transfer Out") || mode.equals("Covert Transfer")){
                saveTitle.setText("Save Transfer");
                saveMessage.setText("Are you sure want to save Transfer?");
                invoicePrintCheck.setText("Transfer Print");
            }else {
                saveTitle.setText("Save Stock Request");
                saveMessage.setText("Are you sure want to save Stock Request?");
                invoicePrintCheck.setText("Stock Request Print");
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
                    alert.dismiss();
                    if (mode.equals("Transfer In") || mode.equals("Transfer Out") ||
                            mode.equals("Covert Transfer") || mode.equals("Stock Request")) {
                       createJsonObject();
                    }
                }catch (Exception exception){}
            });
            copyPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String copyvalue = noOfCopy.getText().toString();
                    int copy = Integer.parseInt(copyvalue);
                    copy++;
                    noOfCopy.setText(copy + "");
                }
            });

            copyMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!noOfCopy.getText().toString().equals("1")) {
                        String copyvalue = noOfCopy.getText().toString();
                        int copy = Integer.parseInt(copyvalue);
                        copy--;
                        noOfCopy.setText(copy + "");
                    }
                }
            });
            signatureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSignatureAlert();
                }
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

    public void showSignatureAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.signature_layout, null);
        alertDialog.setView(customLayout);
        final Button acceptButton = customLayout.findViewById(R.id.buttonYes);
        final Button cancelButton = customLayout.findViewById(R.id.buttonNo);
        final Button clearButton = customLayout.findViewById(R.id.buttonClear);
        LinearLayout mContent = customLayout.findViewById(R.id.signature_layout);
        acceptButton.setEnabled(false);
        acceptButton.setAlpha(0.4f);
        CaptureSignatureView mSig = new CaptureSignatureView(TransferInActivity.this, null, new CaptureSignatureView.OnSignatureDraw() {
            @Override
            public void onSignatureCreated() {
                acceptButton.setEnabled(true);
                acceptButton.setAlpha(1f);
            }
        });
        mContent.addView(
                mSig,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // byte[] signature = captureSignatureView.getBytes();
                Bitmap signature = mSig.getBitmap();
                signatureCapture.setImageBitmap(signature);
                signatureString = ImageUtil.convertBimaptoBase64(signature);
                Utils.setSignature(signatureString);
                signatureAlert.dismiss();
                Log.w("SignatureString:", signatureString);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signatureAlert.dismiss();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signatureString = "";
                Utils.setSignature("");
                mSig.ClearCanvas();
            }
        });
        signatureAlert = alertDialog.create();
        signatureAlert.setCanceledOnTouchOutside(false);
        signatureAlert.show();
    }

    private void createJsonObject() throws JSONException {
       // transferInDetailsl = transferInAdapter.getTransferInlist();

        JSONObject rootJson=new JSONObject();
        JSONObject itemsObject =new JSONObject();
        JSONArray itemsArray=new JSONArray();

        if (transferType.equals("Covert Transfer")){
            rootJson.put("invTransReqNo","");
        }else {
            rootJson.put("invTransReqNo","");
        }

        rootJson.put("invTransNo","");
        rootJson.put("invTransStatus","");
        rootJson.put("customerCode","");
        rootJson.put("customerName","");
        rootJson.put("fromWhsCode",fromWarehouseCode);
        rootJson.put("toWhsCode",toWarehouseCode);
        rootJson.put("docDate",currentDate);
        rootJson.put("docDueDate",currentDate);
        rootJson.put("transferType","");
        rootJson.put("user",username);

        // Sales Details Add to the Objects
        int index=1;
        for (TransferInModel.TransferInDetails model:transferInDetailsl){
            if(model.getQty()!=null && !model.getQty().isEmpty() && Integer.parseInt(model.getQty())> 0){
                Log.w("transFerQtyaa",""+model.getQty());

                itemsObject =new JSONObject();
                itemsObject.put("itemCode",model.getProductCode());
                itemsObject.put("itemName",model.getProductName());
                itemsObject.put("qty",String.valueOf(model.getQty()));
                itemsObject.put("fromWhsCode",fromWarehouseCode);
                itemsObject.put("toWhsCode",toWarehouseCode);
                if (transferType.equals("Transfer In") || transferType.equals("Transfer Out")) {
                    if(!model.getInventoryUOM().equals("null") &&
                            model.getInventoryUOM() != null &&  !model.getInventoryUOM().equals("")) {
                        itemsObject.put("UomCode", model.getInventoryUOM());
                    }else{
                        itemsObject.put("UomCode",settingUOMval);
                    }
                }else {
                    itemsObject.put("UomCode",settingUOMval);
                }

                itemsObject.put("docEntry","");
                itemsObject.put("objectType","");
                itemsObject.put("lineNum","");
                itemsObject.put("batchNum","");
                itemsObject.put("serialNum","");
                itemsArray.put(itemsObject);
                index++;
            }
        }
        rootJson.put("itItem", itemsArray);

        Log.w("GivenStockRequest:",rootJson.toString());

       saveTransferOrRequest(rootJson,1,transferType);

    }

    public  void saveTransferOrRequest(JSONObject jsonBody, int copy,String transferType){
        try {
            pDialog = new SweetAlertDialog(TransferInActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setCancelable(false);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            Log.w("GivenInvoiceRequest:",jsonBody.toString());
            String URL="";
            if (transferType.equals("Transfer In") || transferType.equals("Transfer Out") || transferType.equals("Covert Transfer")){
                URL=Utils.getBaseUrl(this)+"PostingInventoryTransfer";
                Log.w("Given_TransferApi:",URL);
                pDialog.setTitleText("Saving Transfer...");
            }else {
                URL=Utils.getBaseUrl(this)+"PostingInventoryTransferRequest";
                Log.w("Given_StockRequestApi:",URL);
                pDialog.setTitleText("Saving Stock Request...");

            }
            pDialog.show();
            JsonObjectRequest salesOrderRequest = new JsonObjectRequest(Request.Method.POST, URL,jsonBody, response -> {
                Log.w("Transfer_ResponseSap:",response.toString());
                pDialog.dismiss();
                String statusCode=response.optString("statusCode");
                String message=response.optString("statusMessage");
                JSONObject responseData = null;
                responseData=response.optJSONObject("responseData");
                if (statusCode.equals("1")){
                    if (transferType.equals("Transfer In") || transferType.equals("Transfer Out") || transferType.equals("Covert Transfer")){
                        assert responseData != null;
                        String docNum=responseData.optString("docNum");
                        Toast.makeText(getApplicationContext(),"Transfer Saved Success...!",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getApplicationContext(), TransferListProductActivity.class);
                        if (isPrintEnable) {
                            intent.putExtra("docNum",docNum);
                            intent.putExtra("transferType",transferType);
                        }
                        startActivity(intent);
                        finish();
                    }else {
                        assert responseData != null;
                        String docNum=responseData.optString("docNum");
                        Toast.makeText(getApplicationContext(),"Stock Request Saved Success...!",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getApplicationContext(), StockRequestListActivity.class);
                        if (isPrintEnable) {
                            intent.putExtra("docNum",docNum);
                            intent.putExtra("transferType",transferType);
                        }
                        startActivity(intent);
                        finish();
                    }
                }else {
                   /* Intent intent=new Intent(getApplicationContext(),TransferListProductActivity.class);
                    if (isPrintEnable) {
                        intent.putExtra("docNum","22010004");
                        intent.putExtra("transferType",transferType);
                    }
                    startActivity(intent);
                    finish();*/
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

    private void filter(String text) {
        try {
            //new array list that will hold the filtered data
            ArrayList<TransferInModel.TransferInDetails> filterProducts = new ArrayList<>();
            //looping through existing elements
            //   for (ProductsModel s : selectProductAdapter.getProductsList()) {
            emptytxt.setVisibility(View.GONE);

            for (TransferInModel.TransferInDetails s : transferInDetailsl) {
                //if the existing elements contains the search input
                if (s.getProductName().toLowerCase().contains(text.toLowerCase()) ||
                        s.getProductCode().toLowerCase().contains(text.toLowerCase())) {
                    //adding the element to filtered list
                    filterProducts.add(s);
                    emptytxt.setVisibility(View.GONE);
                }

            }
            //calling a method of the adapter class and passing the filtered list
            transferInAdapter.updateList(filterProducts);
            Log.e("filter",""+filterProducts);
            if(filterProducts.isEmpty()){
                emptytxt.setVisibility(View.VISIBLE);
            }

            //setAdapter(filterProducts);
            pdtsizel.setText(filterProducts.size()+" Products");

        }catch (Exception ex){
            Log.e("Error_in_filter", Objects.requireNonNull(ex.getMessage()));
        }
    }

//    void filter(String text){
//        ArrayList<TransferInModel.TransferInDetails> temp = new ArrayList<>();
//        for(TransferInModel.TransferInDetails d: transferInDetailsl){
//
//            String item = d.toString().toLowerCase();
//            if(item.contains(text)){
//                temp.add(d);
//                Log.e("temp",""+temp);
//            }
//        }
//        //update recyclerview
//        transferInAdapter.updateList(temp);
//    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getTransferIn(String warehouseCode,String itemGroupCode) {
        String url;
        try {

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("WarehouseCode", warehouseCode);
            jsonObj.put("ItemGroupCode", itemGroupCode);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            url= Utils.getBaseUrl(this) +"ProductList";
            Log.w("pdtlist_urlTransAdd:", url+jsonObj);
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();

            transferInModels = new ArrayList<>();
            transferInDetailsl = new ArrayList<>();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonObj,
                    response -> {
                        try {
                            pDialog.dismiss();

                            Log.w("pdtlistTransAdd:", response.toString());

                            //pDialog.dismiss();
                            String statusCode = response.optString("statusCode");
                            String statusMessage = response.optString("statusMessage");
                            if (statusCode.equals("1")) {
                                TransferInModel transferInModel = new TransferInModel();

                                JSONArray pdtArray = response.optJSONArray("responseData");

                                for (int i = 0; i < pdtArray.length(); i++) {
                                    JSONObject jsonObject = pdtArray.getJSONObject(i);

                                    if (transferType.equals("Transfer In")) {
                                        if (jsonObject.optInt("stockInHand") > 0){
                                            TransferInModel.TransferInDetails transferInDetails = new TransferInModel.TransferInDetails();
                                            transferInDetails.setProductName(jsonObject.optString("productName"));
                                            transferInDetails.setProductCode(jsonObject.optString("productCode"));
                                            transferInDetails.setStockInHand((jsonObject.optInt("stockInHand")));
                                            transferInDetails.setQty("");
                                            transferInDetails.setInventoryUOM(jsonObject.optString("defaultInventoryUOM"));
                                            transferInDetailsl.add(transferInDetails);
                                        }
                                    } else {
                                        TransferInModel.TransferInDetails transferInDetails = new TransferInModel.TransferInDetails();
                                        transferInDetails.setProductName(jsonObject.optString("productName"));
                                        transferInDetails.setProductCode(jsonObject.optString("productCode"));
                                        transferInDetails.setStockInHand((jsonObject.optInt("stockInHand")));
                                        transferInDetails.setQty("");
                                        transferInDetails.setInventoryUOM(jsonObject.optString("defaultInventoryUOM"));
                                        transferInDetailsl.add(transferInDetails);
                                    }
                                }
                                Log.w("entrTransddd",""+transferInDetailsl.size());

                                if (transferInDetailsl.size() > 0) {
                                    transferInModel.setTransferInDetails(transferInDetailsl);
                                    setTransferInAdapter(transferInDetailsl);
                                    Log.w("entrTrans",""+transferInDetailsl.size());
                                }
                            } else{
                                transferInAdapter.notifyDataSetChanged();
                                transferInDetailsl.clear();
                                transferInView.setAdapter(null);
                                pdtsizel.setText("0 Products");
                                Toast.makeText(getApplicationContext(),statusMessage,Toast.LENGTH_SHORT).show();
                                Log.w("entrTransff","");

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                // Do something when error occurred
                // pDialog.dismiss();
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

        } catch (Exception e) {
        }
    }

    private void getLocationlist() throws JSONException {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"WarehouseList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_location:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading Warehouses...");
        pDialog.setCancelable(false);
        pDialog.show();

        locationDetailsl = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try{
                        Log.w("locationlist:",response.toString());
                        pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            LocationModel locationModel = new LocationModel();
                            JSONArray  locationArray=response.optJSONArray("responseData");
                            for (int i = 0; i < locationArray.length(); i++) {
                                JSONObject jsonObject = locationArray.getJSONObject(i);
                                LocationModel.LocationDetails locationDetails = new LocationModel.LocationDetails();
                                locationDetails.setLocationName(jsonObject.optString("whsName"));
                                locationDetails.setLocationCode(jsonObject.optString("whsCode"));
                                locationDetailsl.add(locationDetails);
                            }
                            if (locationDetailsl.size()>0){
                                locationModel.setLocationDetailsArrayList(locationDetailsl);
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
    }


     private void getfromlocationDialog(ArrayList<LocationModel.LocationDetails> locationDetailsArrayList){

         AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
         builderSingle.setTitle("Select From Warehouse");

         final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.selection_single_dialog);
         for(int i = 0 ; i < locationDetailsArrayList.size() ; i++) {
             arrayAdapter.add(locationDetailsArrayList.get(i).getLocationName());
         }

         int checkedItem = -1;

         builderSingle.setSingleChoiceItems(arrayAdapter, checkedItem, new DialogInterface.OnClickListener() {
             @RequiresApi(api = Build.VERSION_CODES.M)
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 // user checked an item
                 String strName = arrayAdapter.getItem(which);
                 fromlocationl.setText(strName);
                 for(int i = 0 ; i < locationDetailsArrayList.size() ; i++) {

                     if(Objects.equals(strName,  locationDetailsArrayList.get(i).getLocationName())){
                         Log.e("fromlocatcode",""+locationDetailsArrayList.get(i).getLocationCode());
                         fromWarehouseCode = locationDetailsArrayList.get(i).getLocationCode();
                         fromWarehouseName = locationDetailsArrayList.get(i).getLocationName();
                     }
                 }
                 if (!Objects.equals(fromWarehouseCode, toWarehouseCode)){
                     getTransferIn(fromWarehouseCode,"All");
                     dialog.dismiss();
                 }else {
                     Toast.makeText(getApplicationContext(),"From warehouse and To warehouse should not be same...!",Toast.LENGTH_SHORT).show();
                 }

             }
         });
         builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });

         builderSingle.setCancelable(false);
         builderSingle.show();
     }

    private void gettolocationDialog(ArrayList<LocationModel.LocationDetails> locationDetailsArrayList){

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select To Warehouse");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.selection_single_dialog);
        for(int i = 0 ; i < locationDetailsArrayList.size() ; i++) {
            arrayAdapter.add(locationDetailsArrayList.get(i).getLocationName());
        }

        int checkedItem = -1;

        builderSingle.setSingleChoiceItems(arrayAdapter, checkedItem, new DialogInterface.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                String strName = arrayAdapter.getItem(which);
                Log.e("frmlocapi", "" + strName + "..." + fromWarehouseName);
                if (!strName.isEmpty() && !fromWarehouseCode.isEmpty() && fromWarehouseCode != null) {

                    if (!fromWarehouseName.equalsIgnoreCase(strName)) {
                        tolocationl.setText(strName);
                        for (int i = 0; i < locationDetailsArrayList.size(); i++) {
                            String locatName = locationDetailsArrayList.get(i).getLocationName();
                            String locatCode = locationDetailsArrayList.get(i).getLocationCode();
                            if (Objects.equals(strName, locationDetailsArrayList.get(i).getLocationName())) {
                                Log.e("locatcode", "" + locationDetailsArrayList.get(i).getLocationCode());
                                toWarehouseCode = locationDetailsArrayList.get(i).getLocationCode();
                            }
                        }
                        if (!Objects.equals(fromWarehouseCode, toWarehouseCode)){
                            getTransferIn(toWarehouseCode,"All");
                            dialog.dismiss();
                        }else {
                            Toast.makeText(getApplicationContext(),"From warehouse and To warehouse should not be same...!",Toast.LENGTH_SHORT).show();
                        }
                      /*  try {
                            getGrouplist(fromWarehouseCode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/

                       // getTransferIn(fromWarehouseCode,"All");

                        dialog.dismiss();
                    } else {
                        Toast.makeText(TransferInActivity.this, "From location & to location should not be same", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setCancelable(false);
        builderSingle.show();
    }


    private ArrayList<ItemGroupList> getGrouplist(String fromLocation) throws JSONException {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"ItemGroupList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_group:",url);
//        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//        pDialog.setTitleText("Loading Groups...");
//        pDialog.setCancelable(false);
//        pDialog.show();
        itemGroup =new ArrayList<>();
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
                                setupGroup(itemGroup,fromLocation);
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

    private void setupGroup(ArrayList<ItemGroupList> itemGroupLists,String fromLocation) {
        ArrayAdapter<ItemGroupList> myAdapter = new ArrayAdapter<ItemGroupList>(this, android.R.layout.simple_list_item_1, itemGroupLists);
        groupspinner.setAdapter(myAdapter);
        groupspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String itemCode =itemGroup.get(i).getGroupCode();
                String itemName =itemGroup.get(i).getGroupName();
                Log.e("selectspinn",""+ itemName);

                getTransferIn(fromLocation,itemCode);
//                try {
//                    getLocationlist();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}