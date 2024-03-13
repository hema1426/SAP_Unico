package com.winapp.saperp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.winapp.saperp.R;
import com.winapp.saperp.db.DBHelper;
import com.winapp.saperp.model.CustomerModel;
import com.winapp.saperp.model.HomePageModel;
import com.winapp.saperp.model.UserRoll;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.ImageUtil;
import com.winapp.saperp.utils.InternetConnector_Receiver;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
    // Define the variables
    private EditText userIdText;
    private EditText passwordText;
    private Button loginButton;
    private boolean isEmailValid;
    private boolean isPasswordValid;
    private SessionManager session;
    private TextView registerText; 
    private SweetAlertDialog pDialog;
    private String activityFrom;
    private ImageView passwordToggle;
    private DBHelper dbHelper;
    private LinearLayout usernameLayout;
    private LinearLayout passwordLayout;
    private LinearLayout mainLayout;
    private CheckBox rememberMe;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    private Button registerButton;
    private TextView companyName;
    private TextView address1Text;
    private TextView address2Text;
    private long lastBackPressTime = 0;
    private String rememberStr;
    private String newSelectedCompany;
    private String newSelectedCompanyName;
    private String locationCode;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Utils.isTablet(this)){
            setContentView(R.layout.sigin_layout_tablet);
        }else {
            setContentView(R.layout.activity_login);
        }
        Log.w("New Code updated:","Success");
        activityFrom=getIntent().getStringExtra("from");

        passwordToggle=findViewById(R.id.password_toggle);
        usernameLayout=findViewById(R.id.user_name_layout);
        passwordLayout=findViewById(R.id.password_layout);
        mainLayout=findViewById(R.id.main_layout);
        rememberMe=findViewById(R.id.remember_me);
        userIdText =findViewById(R.id.email_id);
        passwordText=findViewById(R.id.password);
        loginButton=findViewById(R.id.btn_login);
        registerButton=findViewById(R.id.btn_register);
        loginButton.setOnClickListener(this);
        dbHelper=new DBHelper(this);
        session=new SessionManager(this);

        // Set the Preference value in edittext for Remembering the values
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);

        if (saveLogin) {
            userIdText.setText(loginPreferences.getString("username", ""));
            passwordText.setText(loginPreferences.getString("password", ""));
            rememberMe.setChecked(true);
        }

        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordToggle.getTag().equals("show")){
                    passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordToggle.setTag("hide");
                    passwordToggle.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_hide));
                }else {
                    passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordToggle.setTag("show");
                    passwordToggle.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_eye));
                }
                passwordText.setSelection(passwordText.length());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        // init the variables
        init();
    }

    private void setWidthHeight(View view){
        ViewGroup.LayoutParams params = view.getLayoutParams();
// Changes the height and width to the specified *pixels*
        params.height = 50;
        params.width = 500;
        view.setLayoutParams(params);
    }

    private void init() {
        passwordText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= passwordText.getRight() - passwordText.getTotalPaddingRight()){
                        // your action for drawable click event
                        if (passwordToggle.getTag().equals("show")){
                            passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordToggle.setTag("hide");
                            passwordText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hide, 0);
                        }else {
                            passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            passwordToggle.setTag("show");
                            passwordText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
                        }
                        passwordText.setSelection(passwordText.length());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.btn_login){
            if (InternetConnector_Receiver.isConnectingToInternet(this)){
                try {
                    validateSession();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void goToRegister() {

    }

    private void validateSession() throws JSONException {
        if (userIdText.getText().toString().isEmpty()) {
            userIdText.setError("User Id is Empty");
            isEmailValid = false;
        } /*else if (!Patterns.EMAIL_ADDRESS.matcher(emailIdText.getText().toString()).matches()) {
            emailIdText.setError(getResources().getString(R.string.invalid_email));
            isEmailValid = false;
        }*/ else {
            isEmailValid = true;
        }
        // Check for a valid password.
        if (passwordText.getText().toString().isEmpty()) {
            passwordText.setError(getResources().getString(R.string.error_password));
            isPasswordValid = false;
        } else if (passwordText.getText().length() < 3) {
            passwordText.setError(getResources().getString(R.string.invalid_password));
            isPasswordValid = false;
        } else {
            isPasswordValid = true;
        }
        if (isEmailValid && isPasswordValid) {
            setSession(userIdText.getText().toString(),passwordText.getText().toString());
        }
    }

    private void setSession(String userId, String password) throws JSONException {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("Username",userId);
        jsonObject.put("Password",password);
       // http://172.16.5.60:8345/api/Login
        String url= Utils.getBaseUrl(this) +"Login";
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_SAP_URL:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Authenticating...");
        pDialog.setCancelable(false);
        pDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
                    try{
                        Log.w("Response_SAP_login:",response.toString());
                        if (response.length()>0){
                          //  {"statusCode":1,"statusMessage":"Success",
                            //  "responseData":[{"userName":"User1","roleName":"DepartmentHead","userID":"1","companyCode":"WINAPP_DEMO",
                            //  "companyName":"WINAPP_DEMO","address1":"1 XYZ Chennai  IN 600001","address2":"     "}]}

                            String statusCode=response.optString("statusCode");
                            if (statusCode.equals("1")){
                                JSONArray userArray=response.optJSONArray("responseData");
                                assert userArray != null;
                                JSONObject object=userArray.optJSONObject(0);
                                String username=object.optString("userName");
                                String rollname=object.optString("roleName");
                               // String locationCode=object.optString("LocationCode");
                               // String isuserpermission=object.optString("IsUserPermission");
                               // String ismainlocation=object.optString("IsMainLocation");

                              //  : {"statusCode":1,"statusMessage":"Success","responseData":[{"userName":"AADHIVAN 1","roleName":"","userID":"7",
                                //  "companyCode":"AADHI INTERNATIONAL PTE LTD","companyName":"AADHI INTERNATIONAL PTE LTD","address1":"101 Cecil street  ,
                                //  #20-11,\r\rSG-069533","address2":"","streetPO":"101 Cecil street  , #20-11","streetNO":"Tong Eng Building","zipcode":"069533",
                                //  "country":"SG","phone1":"+65 61006061"}]}

                                String companycode=object.optString("companyCode");
                                String companyname=object.optString("companyName");
                                String address1=object.optString("streetPO");
                                String address2=object.optString("streetNO");
                                String address3=object.optString("countryName")+"-"+object.optString("zipcode");
                                String postalcode=object.optString("zipcode");
                                String country=object.optString("countryName");
                                String phone=object.optString("phone1");
                                String gstNo=object.optString("gstNo");
                                String locationCode=object.optString("warehouse");
                                String ispermission=object.optString("locationAuthorization");
                                String logo=object.optString("logo");
                                String qrcode=object.optString("qrCode");
                                String paid=object.optString("paid");
                                String unpaid=object.optString("unPaid");
                                String paynow=object.optString("payNow");
                                String bank=object.optString("bank");
                                String cheque=object.optString("cheque");
                                session.createLoginSession(
                                        username,password,rollname,locationCode,"1",ispermission,
                                        companycode,companyname,address1,address2,address3,country,postalcode,phone,gstNo,logo,qrcode,paid,unpaid,paynow,bank,cheque
                                );
                                // Adding the Preference values to the Session to remember the values
                                if (rememberMe.isChecked()) {
                                    loginPrefsEditor.putBoolean("saveLogin", true);
                                    loginPrefsEditor.putString("username", username);
                                    loginPrefsEditor.putString("password", password);
                                    loginPrefsEditor.commit();
                                } else {
                                    loginPrefsEditor.clear();
                                    loginPrefsEditor.commit();
                                }
                                // adding details for Loading Content of the Details....

                                if (!logo.isEmpty()){
                                    Utils.setLogo(logo);
                                    try {
                                        ImageUtil.saveStamp(this,logo,"Logo");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    String filePath = Constants.getSignatureFolderPath(this);
                                    String fileName = "Logo.jpg";
                                    File mFile = new File(filePath, fileName);
                                    if (mFile.exists()){
                                        mFile.delete();
                                    }
                                    Utils.setLogo("");
                                }

                                if (!qrcode.isEmpty()){
                                    Utils.setQrcode(qrcode);
                                    try {
                                        ImageUtil.saveStamp(this,qrcode,"QrCode");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    String filePath = Constants.getSignatureFolderPath(this);
                                    String fileName = "QrCode.jpg";
                                    File mFile = new File(filePath, fileName);
                                    if (mFile.exists()){
                                        mFile.delete();
                                    }
                                    Utils.setQrcode("");
                                }

                                if (!paid.isEmpty()){
                                    Utils.setPaid(paid);
                                    try {
                                        ImageUtil.saveStamp(this,paid,"Paid");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    String filePath = Constants.getSignatureFolderPath(this);
                                    String fileName = "Paid.jpg";
                                    File mFile = new File(filePath, fileName);
                                    if (mFile.exists()){
                                        mFile.delete();
                                    }
                                    Utils.setPaid("");
                                }

                                if (!unpaid.isEmpty()){
                                    Utils.setUnpaid(unpaid);
                                    try {
                                        ImageUtil.saveStamp(this,unpaid,"UnPaid");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    String filePath = Constants.getSignatureFolderPath(this);
                                    String fileName = "UnPaid.jpg";
                                    File mFile = new File(filePath, fileName);
                                    if (mFile.exists()){
                                        mFile.delete();
                                    }
                                    Utils.setUnpaid("");
                                }
                                this.newSelectedCompany=companycode;
                                this.newSelectedCompanyName=companyname;
                                this.locationCode=locationCode;

                                Log.w("savlog11",""+loginPreferences.getBoolean("saveLogin", false));

                                pDialog.dismiss();
                                getPrinterSetting(username);
                                Intent intent=new Intent(getApplicationContext(),DashboardActivity.class);
                                startActivity(intent);
                                finish();
                                //  getCompaniesList();
                                //  getUserRollPermission(companycode,rollname);
                            }else {
                                pDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"Invalid Username or Password",Toast.LENGTH_LONG).show();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
                    // Do something when error occurred
                    pDialog.dismiss();
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

    public void getPrinterSetting(String userId) throws JSONException {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url=Utils.getBaseUrl(this) +"UserSettingFlag";
        Log.w("PrinterSettingURL:",url);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("User",userId);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
                    try {
                        Log.w("PrinterResponse:", response.toString());
                        // Loop through the array elements
                        String statusCode=response.optString("statusCode");
                        if (statusCode.equals("1")){
                            JSONArray customerDetailArray=response.optJSONArray("responseData");
                            for (int i=0;i<customerDetailArray.length();i++){
                                JSONObject object=customerDetailArray.optJSONObject(i);
                                dbHelper.insertSettings("showLogo",object.optString("showLogo"));
                                dbHelper.insertSettings("showSignature",object.optString("showSignature"));
                                dbHelper.insertSettings("showPaidOrUnpaidImage",object.optString("showPaidOrUnpaidImage"));
                                dbHelper.insertSettings("showQRCode",object.optString("showQRCode"));
                                dbHelper.insertSettings("showUserName",object.optString("showUserName"));
                                dbHelper.insertSettings("showUom",object.optString("showUom"));
                                dbHelper.insertSettings("showReturnDetails",object.optString("showReturnDetails"));
                                dbHelper.insertSettings("editSO",object.optString("editSO"));
                                dbHelper.insertSettings("showOutstandingAmount",object.optString("showOutstandingAmount"));
                                dbHelper.insertSettings("showLocationPermission",object.optString("showLocationPermission"));
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),"Error,in getting Printer Settings",Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
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


    public void getUserRollPermission(String companyCode,String userRoll) throws JSONException {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        // Initialize a new JsonArrayRequest instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("CompanyCode",companyCode);
        jsonObject.put("RoleName",userRoll);
        ArrayList<UserRoll> userRolls=new ArrayList<>();
        String url=Utils.getBaseUrl(this) +"MerchandiseApi/GetMobileUserRolePermission?Requestdata="+jsonObject.toString();
        Log.w("Given_url:",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        Log.w("Response_is_UserRoll:",response.toString());
                        if (response.length()>0) {
                            dbHelper.removeAllUserPermission();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object=response.getJSONObject(i);
                                UserRoll roll=new UserRoll();
                                roll.setFormCode(object.optString("FormCode"));
                                roll.setFormName(object.optString("FormName"));
                                boolean permission=object.optBoolean("HavePermission");
                                Log.w("UserPermission_Print:", String.valueOf(permission));
                                if (permission){
                                    roll.setHavePermission("true");
                                }else {
                                    roll.setHavePermission("false");
                                }
                                // roll.setIsActive(object.optString("IsActive"));
                                userRolls.add(roll);
                            }
                            dbHelper.insertUserRollPermission(userRolls);
                            if (userRolls.size()>0){
                                getCompanyDetails(companyCode);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
                    // Do something when error occurred
            Log.w("Error_throwing:",error.toString());
            Toast.makeText(getApplicationContext(),"Server not responding,please try again...",Toast.LENGTH_LONG).show();
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



    public void getCompaniesList() throws JSONException {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        // Initialize a new JsonArrayRequest instance
        String url=Utils.getBaseUrl(this) +"MasterApi/GetAll_Company";
        Log.w("Given_url:",url);
        ArrayList<MainHomeActivity.MultipleCompanyModel> companies=new ArrayList<>();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        Log.w("AllCompanyResponse:",response.toString());
                        if (response.length()>0) {
                            for (int i=0;i<response.length();i++){
                                JSONObject jsonObject=response.getJSONObject(i);
                                MainHomeActivity.MultipleCompanyModel model=new MainHomeActivity.MultipleCompanyModel();
                                model.setCompanyId(jsonObject.optString("CompanyCode"));
                                model.setCompanyName(jsonObject.optString("CompanyName"));
                                model.setActive(true);
                                companies.add(model);
                            }
                            dbHelper.removeAllCompanies();
                            dbHelper.insertCompany(companies);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            Log.w("Error_throwing:",error.toString());
            Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
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


    @Override
    public void onBackPressed() {
        /* if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            Snackbar snackbar = Snackbar
                    .make(userIdText, "Click BACK again to exit", Snackbar.LENGTH_LONG);
            snackbar.show();
            this.lastBackPressTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            finishAffinity();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
    public void getCompanyDetails(String companyCode) throws JSONException {
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        // Initialize a new JsonArrayRequest instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("CompanyCode",companyCode);
        ArrayList<UserRoll> userRolls=new ArrayList<>();
        pDialog.setTitleText("Getting Company Details...");
        String url=Utils.getBaseUrl(this) +"MasterApi/Get_Company?Requestdata="+jsonObject.toString();
        Log.w("Given_url:",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try{
                        Log.w("_CompanyDetails:",response.toString());
                        if (response.length()>0) {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object=response.getJSONObject(i);
                                Log.w("GivenCompanyLogo:",object.optString("LogoString"));
                                session.setCompanyDetails(object.optString("ShortCode"),object.optString("LogoString"));
                                // No need Load the Content Straight to the Dashboard

                                // This is for Without loading Data

                              //  Intent intent=new Intent(LoginActivity.this,DashboardActivity.class);
                              //  startActivity(intent);
                              //  finish();

                               // Loading all Data like customer and Products list
                                Intent intent=new Intent(LoginActivity.this,NewCompanySwitchActivity.class);
                                intent.putExtra("from","Login");
                                intent.putExtra("companyCode",this.newSelectedCompany);
                                intent.putExtra("companyName",this.newSelectedCompanyName);
                                intent.putExtra("locationCode",this.locationCode);
                                startActivity(intent);
                                finish();
                            }
                            pDialog.dismiss();
                        }else {
                            Toast.makeText(getApplicationContext(),"Error in Getting Company details, Try again..",Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }, error -> {
            // Do something when error occurred
            Log.w("Error_throwing:",error.toString());
            Toast.makeText(getApplicationContext(),"Server not responding,please try again...",Toast.LENGTH_LONG).show();
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
}