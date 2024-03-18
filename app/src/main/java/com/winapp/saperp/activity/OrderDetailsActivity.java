package com.winapp.saperp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.winapp.saperp.R;
import com.winapp.saperp.adapter.OrderDetailsAdapter;
import com.winapp.saperp.db.DBHelper;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView orderDate;
    private TextView orderTime;
    private TextView customerName;
    private TextView orderId;
    private TextView orderStatus;
    private TextView paidAmount;
    private TextView dueAmount;
    private TextView dueDays;
    private TextView totalOrderText;
    private TextView totalOutstanding;
    private RecyclerView orderDetailsView;
    private Button btnReorder;
    private ArrayList<OrderDetailsAdapter.OrderDetailsModel> orderDetailsList;
    private OrderDetailsAdapter orderDetailsAdapter;
    private SweetAlertDialog pDialog;
    private String orderNumber;
    private String activityFrom;
    private String invoiceNumber;
    private String salesOrderNumber;
    private SessionManager session;
    private HashMap<String,String > user;
    private String companyId;
    private String locationCode;
    private DBHelper dbHelper;
    private String userName;
    private String order_date;
    private String customer_name;
    private String paid_amount;
    private String due_amount;
    private String order_status;
    private double net_orderAmt=0.0;
    private boolean isQtyCheck=false;
    private CheckBox checkAllProducts;
    private String customer_code;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order Details");
        dbHelper=new DBHelper(this);
        session=new SessionManager(this);
        user=session.getUserDetails();
        companyId=user.get(SessionManager.KEY_COMPANY_CODE);
        userName=user.get(SessionManager.KEY_USER_NAME);
        locationCode=user.get((SessionManager.KEY_LOCATION_CODE));
        orderDate=findViewById(R.id.order_date);
        orderTime=findViewById(R.id.order_time);
        customerName=findViewById(R.id.customer_name_value);
        orderId=findViewById(R.id.order_id);
        orderStatus=findViewById(R.id.order_status);
        paidAmount=findViewById(R.id.paid_amount);
        dueAmount=findViewById(R.id.due_amount);
        dueDays=findViewById(R.id.due_days);
        totalOrderText =findViewById(R.id.total_orders);
        totalOutstanding=findViewById(R.id.total_outstanding);
        orderDetailsView =findViewById(R.id.orderListDetailsView);
        btnReorder=findViewById(R.id.btn_reorder);
        checkAllProducts=findViewById(R.id.check_all_products);

        if (getIntent()!=null){
            orderNumber=getIntent().getStringExtra("orderNumber");
            activityFrom=getIntent().getStringExtra("activityFrom");
            order_date=getIntent().getStringExtra("orderDate");
            customer_name=getIntent().getStringExtra("customerName");
            customer_code=getIntent().getStringExtra("customerCode");
            paid_amount=getIntent().getStringExtra("paidAmount");
            due_amount=getIntent().getStringExtra("dueAmount");
            order_status=getIntent().getStringExtra("orderStatus");
            getSupportActionBar().setTitle(customer_name);
            orderId.setText(orderNumber);
            orderDate.setText(order_date);
            customerName.setText(customer_name);
            paidAmount.setText(paid_amount);
            dueAmount.setText(due_amount);
            orderStatus.setText(order_status);
            if (order_status.equals("Open")){
                orderStatus.setTextColor(Color.parseColor("#229954"));
            }else {
                orderStatus.setTextColor(Color.parseColor("#5DADE2"));
            }

            if (activityFrom.equals("SalesOrder")){
                salesOrderNumber=orderNumber;
                try {
                    getSalesOrderDetails(salesOrderNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                invoiceNumber=orderNumber;
                try {
                    getInvoiceDetails(invoiceNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        btnReorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (net_orderAmt!=0.0){
                    showReorderConfirmDialog();
                }else {
                    Toast.makeText(getApplicationContext(),"Please add product to reorder",Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkAllProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAllProducts.isChecked()){
                    checkAllProducts.setText("Uncheck All Products");
                    int index=0;
                    showDialog();
                    for (OrderDetailsAdapter.OrderDetailsModel model : orderDetailsList){
                        model.setProductCheck(true);
                        insertOrderProducts(true,model);
                        if (index==orderDetailsList.size()-1){
                            closeDialog();
                        }
                        index++;
                    }
                    orderDetailsAdapter.notifyDataSetChanged();
                    setNetTotal(orderDetailsList);
                }else {
                    checkAllProducts.setText("Check All Products");
                    for (OrderDetailsAdapter.OrderDetailsModel model : orderDetailsList){
                        model.setProductCheck(false);
                        insertOrderProducts(false,model);
                    }
                    orderDetailsAdapter.notifyDataSetChanged();
                    setNetTotal(orderDetailsList);
                }
            }
        });


       /* checkAllProducts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    checkAllProducts.setText("Uncheck All Products");
                    for (OrderDetailsAdapter.OrderDetailsModel model : orderDetailsList){
                        model.setProductCheck(true);
                    }
                    orderDetailsAdapter.notifyDataSetChanged();
                }else {
                    checkAllProducts.setText("Check All Products");
                    for (OrderDetailsAdapter.OrderDetailsModel model : orderDetailsList){
                        model.setProductCheck(false);
                    }
                    orderDetailsAdapter.notifyDataSetChanged();
                }
            }
        });
*/
    }

    public void showDialog(){
        progressDialog=new ProgressDialog(OrderDetailsActivity.this);
        progressDialog.setMessage("Inserting Products..Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    public void closeDialog(){
        if (progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    public void insertOrderProducts(boolean isCheked, OrderDetailsAdapter.OrderDetailsModel model){
        Log.w("AddModel:",model.toString());
        if (isCheked){
            dbHelper.insertCart(
                    model.getProductId(),
                    model.getProductName(),
                    model.getCtnQty(),
                    model.getPcsQty(),
                    model.getSubTotal(),
                    "",
                    model.getNetAmount(),
                    "weight",
                    model.getCartonPrice(),
                    model.getLoosePrice(),
                    model.getPcsPerCarton(),
                    model.getTax(),
                    model.getSubTotal(),
                    model.getTaxType(),
                    model.getFocQty(),
                    "",
                    model.getExchangeQty(),
                    "",
                    model.getItemDiscount(),
                    model.getReturnQty(),
                    "",
                    "",
                    model.getTotal(),
                    model.getStockQty(),
                    model.getUomcode(),"0.00");
           // Toast.makeText(getApplicationContext(),"Product added to your list",Toast.LENGTH_SHORT).show();
        }else {
            dbHelper.deleteProduct(model.getProductId());
          //  Toast.makeText(getApplicationContext(),"Product removed from your list",Toast.LENGTH_SHORT).show();
        }
    }

    private void showReorderConfirmDialog() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.reorder_confirm_dialog, viewGroup, false);

        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);
        final CheckBox checkQtybox=dialogView.findViewById(R.id.reorder_qty_check);
        Button yesButton=dialogView.findViewById(R.id.buttonYes);
        Button noButton=dialogView.findViewById(R.id.buttonNo);

        checkQtybox.setOnCheckedChangeListener((compoundButton, b) -> isQtyCheck = b);

        checkQtybox.setChecked(true);
        checkQtybox.setVisibility(View.GONE);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if (checkQtybox.isChecked()){
                    if (activityFrom.equals("SalesOrder")){
                        AddInvoiceActivity.customerId=customer_code;
                        Utils.setCustomerSession(OrderDetailsActivity.this,customer_code);
                        Intent intent=new Intent(OrderDetailsActivity.this,AddInvoiceActivity.class);
                        intent.putExtra("customerId",customer_code);
                        intent.putExtra("activityFrom","ReOrderSales");
                        startActivity(intent);
                        finish();
                    }else {
                        AddInvoiceActivity.customerId=customer_code;
                        Utils.setCustomerSession(OrderDetailsActivity.this,customer_code);
                        Intent intent=new Intent(OrderDetailsActivity.this,AddInvoiceActivity.class);
                        intent.putExtra("customerId",customer_code);
                        intent.putExtra("activityFrom","ReOrderInvoice");
                        startActivity(intent);
                        finish();
                    }
                }else {
                     Intent intent=new Intent(OrderDetailsActivity.this,ReOrderCartActivity.class);
                     startActivity(intent);
                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

            }
        });
    }


    private void getSalesOrderDetails(String soNumber) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("CompanyCode",companyId);
        jsonObject.put("SoNo", soNumber);
        jsonObject.put("LocationCode",locationCode);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"SalesApi/GetSOByCode?Requestdata="+jsonObject.toString();
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Getting SalesOrder Details...");
        pDialog.setCancelable(false);
        pDialog.show();
        orderDetailsList=new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try{
                        Log.w("SalesOrder_Details:",response.toString());
                        if (response.length()>0){
                            String customer_code=response.optString("CustomerCode");
                            String customer_name=response.optString("CustomerName");
                            String phone_no=response.optString("DelPhoneNo");
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

                            dbHelper.removeAllItems();
                            JSONArray products=response.getJSONArray("SoDetails");
                            for (int i=0;i<products.length();i++){
                                JSONObject object=products.getJSONObject(i);

                                String lqty="0.0";
                                String cqty="0.0";
                                if (!object.optString("LQty").equals("null")){
                                    lqty=object.optString("LQty");
                                }

                                if (!object.optString("CQty").equals("null")){
                                    cqty=object.optString("CQty");
                                }

                                OrderDetailsAdapter.OrderDetailsModel model=new OrderDetailsAdapter.OrderDetailsModel();
                                model.setProductId(object.optString("ProductCode"));
                                model.setProductName(object.optString("ProductName"));
                                model.setCtnQty(cqty);
                                model.setPcsQty(lqty);
                                model.setPcsPerCarton(object.optString("PcsPerCarton"));
                                model.setCartonPrice(object.optString("CartonPrice"));
                                model.setLoosePrice(object.optString("Price"));
                                model.setSubTotal(object.optString("SubTotal"));
                                model.setTax(object.optString("Tax"));
                                model.setNetQty(object.optString("Qty"));
                                model.setNetAmount(object.optString("NetTotal"));
                                model.setProductCheck(false);

                                orderDetailsList.add(model);
                            }
                            setOrdersAdapter(orderDetailsList);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Execute your code here
        int count=dbHelper.numberOfRows();
        if (count>0){
            showAlert();
        }else {
            finish();
        }
    }

    private void showAlert() {
        AlertDialog.Builder builder=new AlertDialog.Builder(OrderDetailsActivity.this);
        builder.setTitle("Warning..!");
        builder.setMessage("All Products in cart will be removed.!");
        builder.setCancelable(false);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                dbHelper.removeAllItems();
                finish();
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void getInvoiceDetails(String invoiceNumber) throws JSONException {
        // Initialize a new RequestQueue instance
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("CompanyCode",companyId);
        jsonObject.put("InvoiceNo",invoiceNumber);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url= Utils.getBaseUrl(this) +"SalesApi/GetInvoiceByCode?Requestdata="+jsonObject.toString();
        // Initialize a new JsonArrayRequest instance
        Log.w("Given_url:",url);
        orderDetailsList=new ArrayList<>();
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Getting Invoice Details...");
        pDialog.setCancelable(false);
        pDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.w("Invoice_Details:",response.toString());
                        if (response.length()>0){
                            String customer_code=response.optString("CustomerCode");
                            String customer_name=response.optString("CustomerName");
                            String phone_no=response.optString("DelPhoneNo");
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

                            dbHelper.removeAllItems();
                            JSONArray products=response.getJSONArray("InvoiceDetails");
                            for (int i=0;i<products.length();i++){
                                JSONObject object=products.getJSONObject(i);

                                String lqty="0.0";
                                String cqty="0.0";
                                if (!object.optString("LQty").equals("null")){
                                    lqty=object.optString("LQty");
                                }

                                if (!object.optString("CQty").equals("null")){
                                    cqty=object.optString("CQty");
                                }

                                OrderDetailsAdapter.OrderDetailsModel model=new OrderDetailsAdapter.OrderDetailsModel();
                                model.setProductId(object.optString("ProductCode"));
                                model.setProductName(object.optString("ProductName"));
                                model.setCtnQty(cqty);
                                model.setPcsQty(lqty);
                                model.setPcsPerCarton(object.optString("PcsPerCarton"));
                                model.setCartonPrice(object.optString("CartonPrice"));
                                model.setLoosePrice(object.optString("Price"));
                                model.setTax(object.optString("Tax"));
                                model.setSubTotal(object.optString("SubTotal"));
                                model.setNetQty(object.optString("Qty"));
                                model.setNetAmount(object.optString("NetTotal"));
                                model.setProductCheck(false);

                                orderDetailsList.add(model);
                            }
                        }
                        setOrdersAdapter(orderDetailsList);
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


    private void setOrdersAdapter(ArrayList<OrderDetailsAdapter.OrderDetailsModel> ordersList) {
        orderDetailsView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(OrderDetailsActivity.this);
        orderDetailsView.setLayoutManager(layoutManager);
        orderDetailsAdapter = new OrderDetailsAdapter(this,ordersList, orderList -> {
            double net_order_amount=0.0;
            int count=0;
            for (OrderDetailsAdapter.OrderDetailsModel model:orderList){
                if (model.isProductCheck()){
                    net_order_amount+=Double.parseDouble(model.getNetAmount());
                    count++;
                }
            }
            if (count==orderList.size()){
                checkAllProducts.setChecked(true);
                checkAllProducts.setText("Uncheck All Products");
            }else {
                checkAllProducts.setChecked(false);
                checkAllProducts.setText("Check All Products");
            }
            net_orderAmt=net_order_amount;
            totalOrderText.setText("$ "+Utils.twoDecimalPoint(net_order_amount));
        });
        orderDetailsView.setAdapter(orderDetailsAdapter);
    }

    public void setNetTotal(ArrayList<OrderDetailsAdapter.OrderDetailsModel> orderList){
        double net_order_amount=0.0;
        int count=0;
        for (OrderDetailsAdapter.OrderDetailsModel model:orderList){
            if (model.isProductCheck()){
                net_order_amount+=Double.parseDouble(model.getNetAmount());
                count++;
            }
        }
        if (count==orderList.size()){
            checkAllProducts.setChecked(true);
            checkAllProducts.setText("Uncheck All Products");
        }else {
            checkAllProducts.setChecked(false);
            checkAllProducts.setText("Check All Products");
        }
        net_orderAmt=net_order_amount;
        totalOrderText.setText("$ "+Utils.twoDecimalPoint(net_order_amount));
    }
}