package com.winapp.saperp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.winapp.saperp.R;
import com.winapp.saperp.activity.SalesOrderListActivity;
import com.winapp.saperp.model.SalesOrderPrintPreviewModel;
import com.winapp.saperp.utils.Utils;

import java.util.ArrayList;

public class SalesOrderPrintPreviewAdapter extends RecyclerView.Adapter<SalesOrderPrintPreviewAdapter.ViewHolder> {

    private ArrayList<SalesOrderPrintPreviewModel.SalesList> salesList;
    private Context context;
    View view;
    public SalesOrderPrintPreviewAdapter(Context context,ArrayList<SalesOrderPrintPreviewModel.SalesList> sales) {
        this.context=context;
        this.salesList = sales;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (context instanceof SalesOrderListActivity){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sales_details_view_items, viewGroup, false);
        }else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sales_order_print_preview, viewGroup, false);
        }
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        SalesOrderPrintPreviewModel.SalesList salesList = this.salesList.get(position);
        viewHolder.slNo.setText(String.valueOf(position+1));
        viewHolder.code.setText(salesList.getProductCode());
        if (salesList.getUomCode()!=null && !salesList.getUomCode().equals("null") && !salesList.getUomCode().isEmpty()){
            viewHolder.description.setText(salesList.getDescription()+"("+salesList.getUomCode()+")");
        }else {
            viewHolder.description.setText(salesList.getDescription());
        }
        if (Double.parseDouble(salesList.getTotal()) < 0.00){
            viewHolder.qtyValue.setText((int)Double.parseDouble(salesList.getNetQty())+" (as Return)");
        }else if (Double.parseDouble(salesList.getTotal())==0.00){
            viewHolder.qtyValue.setText((int)Double.parseDouble(salesList.getNetQty())+" ( as FOC)");
        }else {
            viewHolder.qtyValue.setText((int)Double.parseDouble(salesList.getNetQty())+"");
        }
      //  viewHolder.price.setText(Utils.twoDecimalPoint(Double.parseDouble(salesList.getPricevalue())));
        viewHolder.price.setText(salesList.getPricevalue());

        viewHolder.total.setText(Utils.twoDecimalPoint(Double.parseDouble(salesList.getTotal())));
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView slNo;
        private TextView description;
        private TextView code;
        private TextView qtyValue;
        private TextView price;
        private TextView total;
        public ViewHolder(View view) {
            super(view);
            slNo=view.findViewById(R.id.sl_no);
            code=view.findViewById(R.id.code);
            description=view.findViewById(R.id.description);
            qtyValue=view.findViewById(R.id.qty);
            price=view.findViewById(R.id.price);
            total=view.findViewById(R.id.total);
        }
    }

}