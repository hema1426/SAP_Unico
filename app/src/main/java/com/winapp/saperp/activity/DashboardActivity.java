package com.winapp.saperp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.winapp.saperp.R;
import com.winapp.saperp.adapter.CreditLimitDialogAdapter;
import com.winapp.saperp.adapter.DashboardAdapter;
import com.winapp.saperp.db.DBHelper;
import com.winapp.saperp.fragments.ProductsFragment;
import com.winapp.saperp.model.CreditLimitDialogResponse;
import com.winapp.saperp.model.CustomerModel;
import com.winapp.saperp.model.NewLocationModel;
import com.winapp.saperp.model.ProductsModel;
import com.winapp.saperp.model.SettingsModel;
import com.winapp.saperp.model.UomModel;
import com.winapp.saperp.model.UserRoll;
import com.winapp.saperp.receipts.ReceiptsListActivity;
import com.winapp.saperp.salesreturn.NewSalesReturnListActivity;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.GridSpacingItemDecoration;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DashboardActivity extends NavigationActivity {

    private RecyclerView dashboardView;
    private DashboardAdapter dashboardAdapter;
    private ArrayList<String> titleList;
    private TextView userName;
    private TextView dateText;
    private ArrayList<CustomerModel> customerList;
    private SessionManager session;
    private HashMap<String,String > user;
    private HashMap<String,String > company;
    private String companyCode;
    private DBHelper dbHelper;

    CreditLimitDialogAdapter creditLimitDialogAdapter;
     public RecyclerView rv_crditLimit ;
    private LinearLayout catalogLayout;
    private LinearLayout salesOrderLayout;
    private LinearLayout invoiceLayout;
    private LinearLayout receiptsLayout;
    private LinearLayout deliveryLayout;
    private LinearLayout salesReturnLayout;
    private LinearLayout productsLayout;
    private LinearLayout customerLayout;
    private LinearLayout settingsLayout;
    private CardView catalogCard;

    private ImageView creditLimit_Img;
    public static ArrayList<CreditLimitDialogResponse> creditLimitArrayList = new ArrayList<>();

    public static ArrayList<ProductsModel> productList;
    public String locationCode;
    private TextView timeText;


    public static String newSelectedCompany="0";
    public static String newSelectedCompanyName="";
    AlertDialog dialog;
    SweetAlertDialog pDialog;
    public ProgressDialog pdialog;
    public static TextView textCartItemCount;
    public static int mCartItemCount = 0;
    public ImageView companyLogo;
    public String companyLogoString;
    private ArrayList<NewLocationModel.LocationDetails> locationDetailsl;
    public String fromWarehouseCode = "";
    public String fromWarehouseName = "";
    public boolean isPermissionToChangeLocation = false;
    public boolean isCheckedCreditLimit = false;
    public Dialog creditDialog;
    public TextView itemSize_creditl;
    public String creditAmtApi = "10500";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        //Remember this is the FrameLayout area within your activity_main.xml
        getLayoutInflater().inflate(R.layout.activity_dashboard, contentFrameLayout);
       // setContentView(R.layout.activity_dashboard);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Home");
        userName = findViewById(R.id.user_name);
        dateText = findViewById(R.id.date);
        dbHelper=new DBHelper(this);
        session=new SessionManager(this);
        user=session.getUserDetails();
        company=session.getCompanyDetails();
        companyLogoString=company.get(SessionManager.KEY_COMPANY_LOGO);
        companyCode =user.get(SessionManager.KEY_COMPANY_CODE);
        locationCode=user.get(SessionManager.KEY_LOCATION_CODE);
        catalogLayout=findViewById(R.id.catalog_layout);
        salesOrderLayout=findViewById(R.id.sales_order_layout);
        invoiceLayout=findViewById(R.id.invoice_layout);
        receiptsLayout=findViewById(R.id.receipt_layout);
        deliveryLayout=findViewById(R.id.delivery_order_layout);
        salesReturnLayout=findViewById(R.id.sales_return_layout);
        productsLayout=findViewById(R.id.product_layout);
        customerLayout =findViewById(R.id.customer_layout);
        settingsLayout=findViewById(R.id.settings_layout);
        catalogCard=findViewById(R.id.catalog_card);
        creditLimit_Img = findViewById(R.id.creditLimit_dial);

        timeText=findViewById(R.id.time);
        companyLogo=findViewById(R.id.company_logo);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread= new Thread(runnable);
        myThread.start();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        dateText.setText(formattedDate);
        session = new SessionManager(this);
        user = session.getUserDetails();
        userName.setText(user.get(SessionManager.KEY_USER_NAME));
        dashboardView=findViewById(R.id.root_layout);
        titleList=new ArrayList<>();

        Log.w("Logged_In_Location:",locationCode.toString());
        dbHelper.removeAllReturn();
        dbHelper.removeAllInvoiceItems();
        dbHelper.removeAllItems();
        Utils.clearCustomerSession(this);

     //   showCreditdialog(creditAmtApi);
        ArrayList<SettingsModel> settings=dbHelper.getSettings();
        if (settings!=null) {
            if (settings.size() > 0) {
                for (SettingsModel model : settings) {
                    if (model.getSettingName().equals("showLocationPermission")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            isPermissionToChangeLocation = true;
                        } else {
                            isPermissionToChangeLocation=false;
                        }
                    }
                    else if (model.getSettingName().equals("creditLimitSwitch")) {
                        Log.w("SettingNameI:", model.getSettingName());
                        Log.w("SettingValueI:", model.getSettingValue());
                        if (model.getSettingValue().equals("1")) {
                            isCheckedCreditLimit = true;
                        } else {
                            isCheckedCreditLimit = false;
                        }
                    }

                }
            }


   //     Log.w("CompanyLogoString:",companyLogoString);
      /*  if (companyLogoString!=null && !companyLogoString.isEmpty()){
            byte[] imageByteArray = Base64.decode(companyLogoString, Base64.DEFAULT);
            Glide.with(this)
                    .asBitmap()
                    .load(companyLogoString)
                    .into(companyLogo);
        }else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.winapp_logo)
                    .into(companyLogo);
        }*/

    //    getCreditLimit();

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.winapp_logo)
                .into(companyLogo);

        try {
           // getAllSettings();
            getLocationList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.w("creditsetting",""+isCheckedCreditLimit);

        if (isCheckedCreditLimit){
            creditLimit_Img.setVisibility(View.VISIBLE);
            creditLimit_Img.setEnabled(true);

            if(creditLimitArrayList.size() > 0) {
                showCreditdialog(creditLimitArrayList);
            }
            else{
                getCreditLimit();
            }
        }
        else{
            creditLimit_Img.setVisibility(View.GONE);
            creditLimit_Img.setEnabled(false);
        }
        creditLimit_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(creditLimitArrayList.size() > 0) {
                    showCreditdialog(creditLimitArrayList);
                }
                else{
                    getCreditLimit();
                }
            }
        });

       /* titleList.add("Catalog");
        titleList.add("Sales");
        titleList.add("Invoice");
        titleList.add("Receipt");
        titleList.add("Delivery Order");
        titleList.add("Sales Return");
        titleList.add("Product");
        titleList.add("Goods Receive");
        titleList.add("Settings");*/
        ArrayList<UserRoll> userRolls=helper.getUserPermissions();
        if (userRolls.size()>0){
            for (UserRoll roll:userRolls){
                switch (roll.getFormName()) {
                    case "Catalog":
                        if (roll.getHavePermission().equals("true")) {
                            catalogLayout.setVisibility(View.VISIBLE);
                        }else {
                            catalogLayout.setVisibility(View.GONE);
                        }
                        break;
                    case "Sales Order":
                        if (roll.getHavePermission().equals("true")) {
                            salesOrderLayout.setVisibility(View.VISIBLE);
                        }else {
                            salesOrderLayout.setVisibility(View.GONE);
                        }
                        break;
                    case "Invoice":
                        if (roll.getHavePermission().equals("true")) {
                            invoiceLayout.setVisibility(View.VISIBLE);
                        }else {
                            invoiceLayout.setVisibility(View.GONE);
                        }
                        break;
                    case "Receipts":
                        if (roll.getHavePermission().equals("true")) {
                            receiptsLayout.setVisibility(View.VISIBLE);
                        }else {
                            receiptsLayout.setVisibility(View.GONE);
                        }
                        break;
                    case "Settings":
                        if (roll.getHavePermission().equals("true")) {
                            settingsLayout.setVisibility(View.VISIBLE);
                        }else {
                            settingsLayout.setVisibility(View.GONE);
                         }
                        break;
                    case "Sales Return":
                        if (roll.getHavePermission().equals("true")){
                            salesReturnLayout.setVisibility(View.VISIBLE);
                        }else {
                            salesReturnLayout.setVisibility(View.GONE);
                        }
                        break;
                    case "Customer List":
                        if (roll.getHavePermission().equals("true")) {
                            customerLayout.setVisibility(View.VISIBLE);
                        } else {
                            customerLayout.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        }


        }

        ArrayList<SettingsModel> settings1=dbHelper.getSettings();
         if (settings1==null || settings1.size() == 0) {
                dbHelper.insertSettings("invoice_switch","0");
            }

        try {
            dashboardAdapter = new DashboardAdapter(this, titleList);
            int mNoOfColumns = Utils.calculateNoOfColumns(this,150);
            // GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, mNoOfColumns);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, mNoOfColumns);
            dashboardView.setLayoutManager(mLayoutManager);
            // categoriesView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            dashboardView.addItemDecoration(new GridSpacingItemDecoration(2, GridSpacingItemDecoration.dpToPx(this,2), true));
            dashboardView.setItemAnimator(new DefaultItemAnimator());
            dashboardView.setAdapter(dashboardAdapter);
            dashboardView.setVisibility(View.GONE);
        }catch (Exception ex){ Log.e("TAG","Error in Populating the data:"+ex.getMessage()); }

        // Set the Onlclick listener in the Layout
        catalogLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  Intent intent=new Intent(DashboardActivity.this, CategoriesActivity.class);
               // startActivity(intent);
            }
        });

        catalogCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent intent=new Intent(DashboardActivity.this,SchedulingActivity.class);
                startActivity(intent);*/
                Intent intent=new Intent(DashboardActivity.this, CategoriesActivity.class);
                startActivity(intent);
            }
        });

        salesOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashboardActivity.this,SalesOrderListActivity.class);
                startActivity(intent);
            }
        });

        invoiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashboardActivity.this,NewInvoiceListActivity.class);
                startActivity(intent);
            }
        });

        receiptsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashboardActivity.this, ReceiptsListActivity.class);
                startActivity(intent);
            }
        });

        deliveryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashboardActivity.this,DeliveryOrderListActivity.class);
                startActivity(intent);
            }
        });


        productsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashboardActivity.this,StockProductsActivity.class);
                startActivity(intent);
            }
        });

        customerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // need to implement
                Intent intent=new Intent(DashboardActivity.this, CustomerListActivity.class);
                intent.putExtra("Message","Open");
                intent.putExtra("from","cus");
                startActivity(intent);
            }
        });


        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashboardActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });

        salesReturnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  Intent intent=new Intent(DashboardActivity.this, SalesReturnActivity.class);
                startActivity(intent);*/

                Intent intent=new Intent(DashboardActivity.this, NewSalesReturnListActivity.class);
                startActivity(intent);

                //throw new ArithmeticException("Test fix"); // Force a crash

            }
        });
    }

    private void showCreditdialog(ArrayList<CreditLimitDialogResponse> creditLimitArrayList) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.credit_limit_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        ImageView closeImg = promptsView.findViewById(R.id.dial_close);
        TextView okBtn = (TextView) promptsView.findViewById(R.id.ok_dial);
        itemSize_creditl = (TextView) promptsView.findViewById(R.id.itemSize_credit);
        rv_crditLimit = (RecyclerView) promptsView.findViewById(R.id.rv_creditlimit_list);

        setCreditAdater(creditLimitArrayList);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creditDialog.dismiss();
            }
        });
        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creditDialog.dismiss();
            }
        });

        alertDialogBuilder.setView(promptsView);
        creditDialog = alertDialogBuilder.create();
        creditDialog.setCancelable(true);
        creditDialog.show();

    }

    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

    public void doWork() {
        runOnUiThread(() -> {
            try{
                Date dt = new Date();
                int hours = dt.getHours();
                int minutes = dt.getMinutes();
                int seconds = dt.getSeconds();
                String curTime = hours + " : " + minutes + " : " + seconds;
                timeText.setText(curTime);
            }catch (Exception e) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_cart);
        final MenuItem cartTotal=menu.findItem(R.id.action_total);
        cartTotal.setVisible(false);
        final MenuItem cart=menu.findItem(R.id.action_cart);
        cart.setVisible(false);
        final MenuItem search=menu.findItem(R.id.action_search);
        search.setVisible(false);
        View actionView = menuItem.getActionView();
        textCartItemCount = actionView.findViewById(R.id.cart_badge);
        setupBadge();
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductsFragment.closeSheet();
                onOptionsItemSelected(menuItem);
                Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cart) {
            // setFragment(new SchedulingFragment());
            return true;
        } else if (id == R.id.action_search) {
            ProductsFragment.closeSheet();
            Intent intent=new Intent(DashboardActivity.this,SearchProductActivity.class);
            startActivity(intent);
        }else if (id==R.id.choose_company){
            ArrayList<MainHomeActivity.MultipleCompanyModel> companyList=dbHelper.getAllCompanies();
            if (companyList.size() > 0) {
                showCompanyDialog();
            }else {
                Toast.makeText(getApplicationContext(),"No Company Found",Toast.LENGTH_SHORT).show();
            }
        }else if (id==R.id.choose_location){
            if (isPermissionToChangeLocation){
                if (locationDetailsl.size() > 0){
                    getLocationDialog(locationDetailsl);
                }else {
                    Toast.makeText(getApplicationContext(), "No Location Found..!", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(getApplicationContext(),"You Don't have permission to change location..!",Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            user=session.getUserDetails();
            locationCode=user.get(SessionManager.KEY_LOCATION_CODE);
            menu.getItem(0).setVisible(true);
            menu.getItem(0).setTitle("Location : "+locationCode);
        }catch (Exception ex){
        }
        return true;
    }

    private void getLocationList() throws JSONException {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "WarehouseList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url_location:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading Location...");
        pDialog.setCancelable(false);
        pDialog.show();
        locationDetailsl = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                null,
                response -> {
                    try{
                        Log.w("locationlist:",response.toString());
                        pDialog.dismiss();
                        String statusCode=response.optString("statusCode");
                        String statusMessage=response.optString("statusMessage");
                        if (statusCode.equals("1")){
                            NewLocationModel locationModel = new NewLocationModel();
                            JSONArray locationArray=response.optJSONArray("responseData");
                            for (int i = 0; i < locationArray.length(); i++) {
                                JSONObject jsonObject = locationArray.getJSONObject(i);
                                NewLocationModel.LocationDetails locationDetails = new NewLocationModel.LocationDetails();
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

    private void getLocationDialog(ArrayList<NewLocationModel.LocationDetails> locationDetailsArrayList){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select Location");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.selection_single_dialog);
        for(int i = 0 ; i < locationDetailsArrayList.size() ; i++) {
            arrayAdapter.add(locationDetailsArrayList.get(i).getLocationName()+" - "+locationDetailsArrayList.get(i).getLocationCode());
        }
        int checkedItem = Utils.getLocationCodeIndex(locationDetailsArrayList,locationCode);
        builderSingle.setSingleChoiceItems(arrayAdapter, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fromWarehouseCode = locationDetailsArrayList.get(which).getLocationCode();
                if (fromWarehouseCode!=null && !fromWarehouseCode.isEmpty()){
                    if (!Objects.equals(locationCode, fromWarehouseCode)){
                        Log.w("NewLocationSelected:",fromWarehouseCode.toString());
                    }else {
                        Toast.makeText(getApplicationContext(),"Same location should not choose..!",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Select Location",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builderSingle.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.setSessionForLocation(getApplicationContext(),fromWarehouseCode);
                Intent intent=new Intent(getApplicationContext(),DashboardActivity.class);
                startActivity(intent);
                finish();
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


    private void showCompanyDialog() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ProductsFragment.closeSheet();
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.multiple_company_layout, viewGroup, false);

        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);
        final RecyclerView companyListView=dialogView.findViewById(R.id.companyListView);
        ArrayList<MainHomeActivity.MultipleCompanyModel> companyList=dbHelper.getAllCompanies();
        companyListView.setHasFixedSize(true);

        int index=0;
        for (MainHomeActivity.MultipleCompanyModel model:companyList){
            if (model.getCompanyId().equals(companyCode)){
                MainHomeActivity.MultipleCompanyModel values=companyList.get(index);
                MainHomeActivity.MultipleCompanyModel values1=companyList.get(0);
                companyList.set(0,values);
                companyList.set(index,values1);
                break;
            }
            index++;
        }
        // RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        companyListView.setLayoutManager(new LinearLayoutManager(DashboardActivity.this, LinearLayoutManager.VERTICAL, false));
        MainHomeActivity.CompanyChooseAdapter companyAdapter = new MainHomeActivity.CompanyChooseAdapter(companyList,companyCode, new MainHomeActivity.CompanyChooseAdapter.CallBack() {
            @Override
            public void sortProduct(String letter) {

            }
        });
        companyListView.setAdapter(companyAdapter);
        Button yesButton=dialogView.findViewById(R.id.buttonYes);
        Button noButton=dialogView.findViewById(R.id.buttonNo);
        //finally creating the alert dialog and displaying it
        dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        dialog.setCancelable(false);
        dialog.show();
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainHomeActivity.newSelectedCompany.equals("0")){
                    showConfirmDialog(MainHomeActivity.newSelectedCompanyName);
                }else {
                    Toast.makeText(getApplicationContext(),"Please select the Company",Toast.LENGTH_SHORT).show();
                }
                // dialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                MainHomeActivity.newSelectedCompany="0";
            }
        });
    }

    public void showConfirmDialog(String companyname){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Information");
        builder.setMessage("Are you sure want to Switch over "+companyname+" ?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                dialogInterface.dismiss();
                Intent intent=new Intent(DashboardActivity.this,NewCompanySwitchActivity.class);
                intent.putExtra("from","Dashboard");
                intent.putExtra("companyCode",MainHomeActivity.newSelectedCompany);
                intent.putExtra("locationCode",locationCode);
                intent.putExtra("companyName",MainHomeActivity.newSelectedCompanyName);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    public static void setupBadge() {
        mCartItemCount = helper.numberOfRows();
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void getCreditLimit() {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Utils.getBaseUrl(this) + "ReportCustomerCreditTermsList";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_Credit_URL:", url);

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading Credit...");
        pDialog.setCancelable(false);
        pDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                creditLimitArrayList = new ArrayList<>();

                Log.w("Res_credit:", response.toString());
                // Loop through the array elements
                JSONArray uomArray = response.optJSONArray("responseData");
                if (uomArray != null && uomArray.length() > 0) {
                    for (int j = 0; j < uomArray.length(); j++) {
                        JSONObject obj = uomArray.getJSONObject(j);
                        CreditLimitDialogResponse model = new CreditLimitDialogResponse(
                                obj.optString("balance"),
                                obj.optString("creditLine"),
                             obj.optString("customerCode"),
                                obj.optString("customerName"));
                        creditLimitArrayList.add(model);
                    }
                }

                Log.w("UOM_TEXT:", uomArray.toString());
                pDialog.dismiss();
                if (creditLimitArrayList.size() > 0) {
                    runOnUiThread(() -> {
                        showCreditdialog(creditLimitArrayList);
                    });
                }
                else{
                    Toast.makeText(this, "No credit data", Toast.LENGTH_SHORT).show();
                }
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
    public void setCreditAdater(ArrayList<CreditLimitDialogResponse> arrayList ) {
        // total.setText(splitModelList.get(0).getTotal());
        itemSize_creditl.setText("Customer "+arrayList.size());
        rv_crditLimit.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        creditLimitDialogAdapter = new CreditLimitDialogAdapter(this, arrayList);
        rv_crditLimit.setAdapter(creditLimitDialogAdapter);
    }


    @Override
    protected void onResume() {
        dbHelper.removeAllReturn();
        dbHelper.removeAllInvoiceItems();
        dbHelper.removeAllItems();
        Utils.clearCustomerSession(this);
        super.onResume();
    }
}

