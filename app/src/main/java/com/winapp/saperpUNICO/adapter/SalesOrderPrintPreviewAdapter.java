package com.winapp.saperpUNICO.adapter;

import static com.winapp.saperpUNICO.activity.SalesOrderListActivity.shortCodeStr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.winapp.saperpUNICO.R;
import com.winapp.saperpUNICO.activity.SalesOrderListActivity;
import com.winapp.saperpUNICO.model.SalesOrderPrintPreviewModel;
import com.winapp.saperpUNICO.utils.Utils;

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
        Log.w("SOuom",""+salesList.getUomCode());
        if (salesList.getUomCode()!=null && !salesList.getUomCode().equals("null") && !salesList.getUomCode().isEmpty()){
            viewHolder.description.setText(salesList.getDescription()+"("+salesList.getUomCode()+")");
        }else {
            viewHolder.description.setText(salesList.getDescription());
        }
        viewHolder.statusLaySol.setVisibility(View.VISIBLE);
        if (Double.parseDouble(salesList.getTotal()) < 0.00){
            viewHolder.qtyValue.setText((int)Double.parseDouble(salesList.getNetQty())+" (as Return)");
        }else if (Double.parseDouble(salesList.getTotal())==0.00){
            viewHolder.qtyValue.setText((int)Double.parseDouble(salesList.getNetQty())+" ( as FOC)");
        }else {
            viewHolder.qtyValue.setText((int)Double.parseDouble(salesList.getNetQty())+"");
        }
        if(salesList.getRowStatus().equalsIgnoreCase("O")) {
            viewHolder.soStatusl.setText("OPEN");
        }
        if(salesList.getRowStatus().equalsIgnoreCase("C")) {
            viewHolder.soStatusl.setText("CLOSE");
        }
        Log.w("so_total",""+salesList.getTotal());
      //  viewHolder.price.setText(Utils.twoDecimalPoint(Double.parseDouble(salesList.getPricevalue())));
        if(shortCodeStr.equalsIgnoreCase("FUXIN")) {
            viewHolder.price.setText(Utils.fourDecimalPoint(Double.parseDouble(salesList.getPricevalue())));
            viewHolder.total.setText(Utils.fourDecimalPoint(Double.parseDouble(salesList.getTotal())));
        }else{
            viewHolder.price.setText(Utils.twoDecimalPoint(Double.parseDouble(salesList.getPricevalue())));
            viewHolder.total.setText(Utils.twoDecimalPoint(Double.parseDouble(salesList.getTotal())));
        }
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
        private LinearLayout statusLaySol;
        private TextView total,soStatusl;
        public ViewHolder(View view) {
            super(view);
            slNo=view.findViewById(R.id.sl_no);
            code=view.findViewById(R.id.code);
            description=view.findViewById(R.id.description);
            qtyValue=view.findViewById(R.id.qty);
            price=view.findViewById(R.id.price);
            total=view.findViewById(R.id.total);
            soStatusl=view.findViewById(R.id.soStatusItem);
            statusLaySol=view.findViewById(R.id.statusLaySo);

        }
    }

}