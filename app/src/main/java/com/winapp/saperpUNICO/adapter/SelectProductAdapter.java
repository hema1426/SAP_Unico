package com.winapp.saperpUNICO.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.winapp.saperpUNICO.R;
import com.winapp.saperpUNICO.activity.AddInvoiceActivityOld;
import com.winapp.saperpUNICO.activity.CreateNewInvoiceActivity;
import com.winapp.saperpUNICO.activity.ProductAnalyzeActivity;
import com.winapp.saperpUNICO.activity.SearchProductActivity;
import com.winapp.saperpUNICO.activity.StockProductsActivity;
import com.winapp.saperpUNICO.activity.TransferProductAddActivity;
import com.winapp.saperpUNICO.db.DBHelper;
import com.winapp.saperpUNICO.model.ProductsModel;
import com.winapp.saperpUNICO.model.SettingsModel;
import com.winapp.saperpUNICO.salesreturn.NewSalesReturnProductAddActivity;
import com.winapp.saperpUNICO.salesreturn.SalesReturnActivity;
import com.winapp.saperpUNICO.utils.SessionManager;
import com.winapp.saperpUNICO.utils.SharedPreferenceUtil;
import com.winapp.saperpUNICO.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectProductAdapter extends RecyclerView.Adapter<SelectProductAdapter.ViewHolder> {
    private ArrayList<ProductsModel> products;
    public CallBack callBack;
    public Context context;
    public DBHelper dbHelper;
    public boolean isAllowLowStock = false;
    private SessionManager session;
    private String negativeStockStr = "No";
    public String saveAction = "0";
    public String isLastPrice = "false";
    public String shortCodeStr = "";
    public String foreignNameStr = "";
    private SharedPreferenceUtil sharedPreferenceUtil;

    public SelectProductAdapter(Context context, ArrayList<ProductsModel> products, CallBack callBack) {
        this.products = Utils.getProductList(products);
        this.callBack = callBack;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.select_product_items, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ProductsModel model = products.get(i);
        dbHelper = new DBHelper(context);
        session = new SessionManager(context);
        sharedPreferenceUtil = new SharedPreferenceUtil(context);

        HashMap<String, String> user = session.getUserDetails();
        negativeStockStr = user.get(SessionManager.KEY_NEGATIVE_STOCK);

        shortCodeStr = sharedPreferenceUtil.getStringPreference(sharedPreferenceUtil.KEY_SHORT_CODE, "");
        isLastPrice = sharedPreferenceUtil.getStringPreference(sharedPreferenceUtil.KEY_LAST_PRICE, "");
        if (model.getForeignName() != null
                && !model.getForeignName().equals("null")
                && !model.getForeignName().isEmpty()) {
            foreignNameStr = " - " + model.getForeignName();
        }
        if (context instanceof StockProductsActivity){

            viewHolder.image_pdt_iteml.setVisibility(View.VISIBLE);
            viewHolder.productName.setText(model.getProductName()+foreignNameStr);
        } else {
            viewHolder.image_pdt_iteml.setVisibility(View.GONE);
            viewHolder.productName.setText(model.getProductName());
        }

//        viewHolder.productName.setText(model.getProductName()+foreignNameStr);
        viewHolder.productCode.setText(model.getProductCode());

        if (model.getStockQty() != null && !model.getStockQty().equals("null")) {
            if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0) {
                viewHolder.stockQty.setText(model.getStockQty() + " ( No Stock )");
            } else {
                viewHolder.stockQty.setText(model.getStockQty());
            }
        } else {
            Log.w("LogStockQty:", model.getStockQty());
            if (model.getStockQty().equals("null")) {
                viewHolder.stockQty.setText("0" + " ( No Stock )");
            } else {
                viewHolder.stockQty.setText(model.getStockQty() + " ( No Stock )");
            }
        }
      /*  if (model.getLastPrice()!=null && !model.getLastPrice().isEmpty() && Double.parseDouble(model.getLastPrice()) > 0.00){
            viewHolder.price.setText(String.valueOf("$ "+ Utils.twoDecimalPoint(Double.parseDouble(model.getLastPrice()))));
        }else {
            viewHolder.price.setText(String.valueOf("$ "+ Utils.twoDecimalPoint(Double.parseDouble(model.getUnitCost()))));
        }*/
        if (shortCodeStr.equalsIgnoreCase("FUXIN")) {
            if (isLastPrice.equalsIgnoreCase("True")) {
                viewHolder.price.setText(String.valueOf("$ " + model.getLastPrice()));
            } else {
                viewHolder.price.setText(String.valueOf("$ " + model.getUnitCost()));
            }
        } else {
            if (model.getLastPrice() != null && !model.getLastPrice().isEmpty() && Double.parseDouble(model.getLastPrice()) > 0.00) {
                viewHolder.price.setText(String.valueOf("$ " + model.getLastPrice()));
            } else {
                viewHolder.price.setText(String.valueOf("$ " + model.getUnitCost()));
            }
        }

        if (model.getStockQty() != null && !model.getStockQty().equals("null")) {
            if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0 || model.getStockQty().equals("null")) {
                viewHolder.stockQty.setTextColor(Color.parseColor("#D24848"));
                // viewHolder.rootLayout.setBackgroundColor(Color.parseColor("#FDEDEC"));
            } else if (Double.parseDouble(model.getStockQty()) > 0) {
                viewHolder.stockQty.setTextColor(Color.parseColor("#2ECC71"));
                // viewHolder.rootLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        }

        if (model.getImageUrl()!=null && !model.getImageUrl().equals("null") ){
            RequestOptions myOptions = new RequestOptions().fitCenter();
            Glide.with(context)
                    .asBitmap()
                    //.apply(myOptions)
                    .load(model.getProductImage())
                    .error(R.drawable.no_image_found)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            viewHolder.image_pdt_iteml.setImageResource(R.drawable.no_image_found);
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).into(viewHolder.image_pdt_iteml);
        }else {
            viewHolder.image_pdt_iteml.setImageResource(R.drawable.no_image_found);
        }
        viewHolder.productName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLowStockSetting();
                if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0) {
                    if (context instanceof SalesReturnActivity || context instanceof NewSalesReturnProductAddActivity || context instanceof TransferProductAddActivity) {
                        callBack.searchProduct(model);
                    } else {
                        if (context instanceof AddInvoiceActivityOld) {
                            if (AddInvoiceActivityOld.activityFrom.equals("ReOrderSales")) {
                                callBack.searchProduct(model);
                            } else {
//                                if (isAllowLowStock){
                                if (negativeStockStr.equalsIgnoreCase("Yes")) {
                                    callBack.searchProduct(model);
                                } else {
                                    showAlert();
                                }
                            }
                        } else if (context instanceof SearchProductActivity || context instanceof CreateNewInvoiceActivity) {
                            ArrayList<SettingsModel> settings = dbHelper.getSettings();
                            if (settings.size() > 0) {
                                for (SettingsModel model : settings) {
                                    if (model.getSettingName().equals("invoice_switch")) {
                                        if (model.getSettingValue().equals("1")) {
                                            saveAction = "Invoice";
                                        } else {
                                            saveAction = "SalesOrder";
                                        }
                                    }
                                }
                            }
                            if (saveAction.equals("Invoice")) {
                                // if (isAllowLowStock){
                                if (negativeStockStr.equalsIgnoreCase("Yes")) {
                                    callBack.searchProduct(model);
                                } else {
                                    showAlert();
                                }
                            } else {
                                callBack.searchProduct(model);
                            }
                        }
                    }
                } else {
                    callBack.searchProduct(model);
                }
            }
        });

        if (context instanceof NewSalesReturnProductAddActivity
                || context instanceof TransferProductAddActivity ||
                context instanceof CreateNewInvoiceActivity || context instanceof StockProductsActivity
                || context instanceof SearchProductActivity || context instanceof SalesReturnActivity
                || AddInvoiceActivityOld.activityFrom.equals("SalesOrder") ||
                AddInvoiceActivityOld.activityFrom.equals("SalesEdit") ||
                AddInvoiceActivityOld.activityFrom.equals("ReOrderSales")) {

            viewHolder.productInfo.setVisibility(View.GONE);
        } else {

            viewHolder.productInfo.setVisibility(View.VISIBLE);
        }

        viewHolder.productInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductAnalyzeActivity.class);
                intent.putExtra("productId", model.getProductCode());
                intent.putExtra("productName", model.getProductName());
                intent.putExtra("customerCode", AddInvoiceActivityOld.customerId);
                intent.putExtra("uom", model.getUomCode());
                context.startActivity(intent);
            }
        });

        viewHolder.selectProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLowStockSetting();
                if (model.getStockQty() != null && !model.getStockQty().isEmpty() && !model.getStockQty().equals("null")) {
                    Log.w("stokpdtqty", "" + model.getStockQty());
                    if (Double.parseDouble(model.getStockQty()) == 0 || Double.parseDouble(model.getStockQty()) < 0) {
                        if (context instanceof SalesReturnActivity || context instanceof NewSalesReturnProductAddActivity || context instanceof TransferProductAddActivity) {
                            Log.w("stokpdtqty", "" + model.getStockQty());

                            callBack.searchProduct(model);
                        } else {
                            if (context instanceof AddInvoiceActivityOld) {
                                if (AddInvoiceActivityOld.activityFrom.equals("ReOrderSales")) {
                                    callBack.searchProduct(model);
                                } else {
//                                    if (isAllowLowStock){
                                    if (negativeStockStr.equalsIgnoreCase("Yes")) {
                                        callBack.searchProduct(model);
                                    } else {
                                        showAlert();
                                    }
                                }
                            } else if (context instanceof SearchProductActivity || context instanceof CreateNewInvoiceActivity) {
                                ArrayList<SettingsModel> settings = dbHelper.getSettings();
                                if (settings.size() > 0) {
                                    for (SettingsModel model : settings) {
                                        if (model.getSettingName().equals("invoice_switch")) {
                                            if (model.getSettingValue().equals("1")) {
                                                saveAction = "Invoice";
                                            } else {
                                                saveAction = "SalesOrder";
                                            }
                                        }
                                    }
                                }
                                if (saveAction.equals("Invoice")) {
                                    //   if (isAllowLowStock){
                                    if (negativeStockStr.equalsIgnoreCase("Yes")) {
                                        callBack.searchProduct(model);
                                    } else {
                                        showAlert();
                                    }
                                } else {
                                    callBack.searchProduct(model);
                                }
                            }
                        }
                    } else {
                        callBack.searchProduct(model);
                    }
                } else {
                    callBack.searchProduct(model);
                }
            }
        });

   /*     if (i % 2==0){
            viewHolder.rootLayout.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }else {
            viewHolder.rootLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        }
*/
       /* viewHolder.selectProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.searchProduct(model);
            }
        });
        viewHolder.productCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.searchProduct(model);
            }
        });

        viewHolder.productName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.searchProduct(model);
            }
        });*/
    }

    /**
     * Get = the low stock invoice setting to allow the Negative stock allow the invoice
     */
    public void getLowStockSetting() {
        ArrayList<SettingsModel> settings = dbHelper.getSettings();
        if (settings.size() > 0) {
            for (SettingsModel model : settings) {
                if (model.getSettingName().equals("allow_negative_switch")) {
                    if (model.getSettingValue().equals("1")) {
                        isAllowLowStock = true;
                    } else {
                        isAllowLowStock = false;
                    }
                }
            }
        } else {
            isAllowLowStock = false;
        }
        Log.w("IsAllowedStock:", isAllowLowStock + "");
    }

    public void showAlert() {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Warning !")
                .setContentText("Product stock is Low!")
                .setConfirmText("Cancel")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                }).show();
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName;
        private TextView productCode;
        private LinearLayout selectProduct;
        private LinearLayout rootLayout;
        private TextView stockQty;
        private TextView price;
        private CardView mainItem;
        private ImageView productInfo;
        private ImageView image_pdt_iteml;

        public ViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.item_name);
            productCode = view.findViewById(R.id.item_code);
            selectProduct = view.findViewById(R.id.product_view);
            stockQty = view.findViewById(R.id.stock);
            price = view.findViewById(R.id.price);
            mainItem = view.findViewById(R.id.cardlist_item);
            productInfo = view.findViewById(R.id.product_info);
            rootLayout = view.findViewById(R.id.rootLayout);
            image_pdt_iteml = view.findViewById(R.id.image_pdt_item);

        }
    }

    public interface CallBack {
        void searchProduct(ProductsModel model);
    }

    public void filterList(ArrayList<ProductsModel> products) {
        this.products = Utils.getProductList(products);
        notifyDataSetChanged();
    }

    public ArrayList<ProductsModel> getProductsList() {
        return this.products;
    }
}