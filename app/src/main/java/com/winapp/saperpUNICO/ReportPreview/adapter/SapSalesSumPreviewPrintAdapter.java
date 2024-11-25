package com.winapp.saperpUNICO.ReportPreview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.winapp.saperpUNICO.R;
import com.winapp.saperpUNICO.model.ReportSalesSummaryModel;
import com.winapp.saperpUNICO.utils.Utils;
import java.util.ArrayList;

public class SapSalesSumPreviewPrintAdapter extends RecyclerView.Adapter<SapSalesSumPreviewPrintAdapter.ViewHolder> {

    private ArrayList<ReportSalesSummaryModel.ReportSalesSummaryDetails> reportSalesSummaryDetailsArrayList;
    private Context context;
    View view;
    private String printView;

    public SapSalesSumPreviewPrintAdapter(Context context, ArrayList<ReportSalesSummaryModel.ReportSalesSummaryDetails> reportSalesSummaryDetailsArrayList, String printView) {
        this.context=context;
        this.reportSalesSummaryDetailsArrayList = reportSalesSummaryDetailsArrayList;
        this.printView=printView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.sap_ro_salessum_preview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ReportSalesSummaryModel.ReportSalesSummaryDetails reportSalesSummarymodel =reportSalesSummaryDetailsArrayList.get(position);

        viewHolder.slNo.setText(String.valueOf(position+1));
        viewHolder.transNo.setText(reportSalesSummarymodel.getTransNo());
        viewHolder.cust.setText(reportSalesSummarymodel.getCustomer());
        viewHolder.price.setText("$   "+ Utils.twoDecimalPoint(Double.parseDouble(reportSalesSummarymodel.getAmount())));
        if(reportSalesSummarymodel.getType().equalsIgnoreCase("CS")) {
            viewHolder.type.setText("Cash Sales");
        }
        else{
            viewHolder.type.setText(String.valueOf(reportSalesSummarymodel.getType()));
        }
    }

    @Override
    public int getItemCount() {
        return reportSalesSummaryDetailsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView slNo;
        private TextView transNo;
        private TextView cust;
        private TextView price;
        private TextView type;

        public ViewHolder(View view) {
            super(view);
            slNo =view.findViewById(R.id.item_sNo);
            transNo=view.findViewById(R.id.item_transfNo);
            cust=view.findViewById(R.id.item_cust);
            price=view.findViewById(R.id.item_amt);
            type=view.findViewById(R.id.item_type);
        }
    }
}