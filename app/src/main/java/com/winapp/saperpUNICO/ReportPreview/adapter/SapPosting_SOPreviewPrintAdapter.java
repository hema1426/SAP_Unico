package com.winapp.saperpUNICO.ReportPreview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.winapp.saperpUNICO.R;
import com.winapp.saperpUNICO.model.ReportPostingInvSOModel;
import com.winapp.saperpUNICO.utils.Utils;

import java.util.ArrayList;

public class SapPosting_SOPreviewPrintAdapter extends RecyclerView.Adapter<SapPosting_SOPreviewPrintAdapter.ViewHolder> {

    private ArrayList<ReportPostingInvSOModel.ReportSODetails> reportInvoiceDetailsArrayList;
    private Context context;
    View view;
    private String printView;

    public SapPosting_SOPreviewPrintAdapter(Context context, ArrayList<ReportPostingInvSOModel.ReportSODetails> reportInvoiceDetailsArrayList) {
        this.context=context;
        this.reportInvoiceDetailsArrayList = reportInvoiceDetailsArrayList;
        this.printView=printView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.sap_ro_postinginv_preview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ReportPostingInvSOModel.ReportSODetails reportSalesSummarymodel =reportInvoiceDetailsArrayList.get(position);
        viewHolder.sNo.setText(String.valueOf(position+1));
        viewHolder.balance.setVisibility(View.GONE);

        viewHolder.InvDate.setText(reportSalesSummarymodel.getInvoiceDate());
        viewHolder.InvNo.setText(reportSalesSummarymodel.getInvoiceNo());
        viewHolder.custName.setText(reportSalesSummarymodel.getCustomerName());
        viewHolder.total.setText(Utils.twoDecimalPoint(Double.parseDouble(reportSalesSummarymodel.getTotal())));
      // viewHolder.balance.setText(Utils.twoDecimalPoint(Double.parseDouble(reportSalesSummarymodel.getBalance())));
    }

    @Override
    public int getItemCount() {
        return reportInvoiceDetailsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView InvNo,sNo;
        private TextView InvDate;
        private TextView custName;
        private TextView total;
        private TextView balance;

        public ViewHolder(View view) {
            super(view);
            sNo =view.findViewById(R.id.item_sno_post);
            InvNo =view.findViewById(R.id.item_invno_post);
            InvDate =view.findViewById(R.id.item_invdate_post);
            custName =view.findViewById(R.id.item_cust_post);
            total =view.findViewById(R.id.item_total_post);
            balance =view.findViewById(R.id.item_bal_post);
        }
    }
}