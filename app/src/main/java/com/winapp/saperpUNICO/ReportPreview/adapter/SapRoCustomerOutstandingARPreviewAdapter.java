package com.winapp.saperpUNICO.ReportPreview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.winapp.saperpUNICO.R;
import com.winapp.saperpUNICO.model.CustomerStateModel;

import java.util.ArrayList;

public class SapRoCustomerOutstandingARPreviewAdapter extends RecyclerView.Adapter<SapRoCustomerOutstandingARPreviewAdapter.ViewHolder> {

    private ArrayList<CustomerStateModel.CustInvoiceDetailsAR> customerDetailsLists;
    private Context context;
    View view;
    private String printView;
    public SapRoCustomerOutstandingARPreviewAdapter(Context context, ArrayList<CustomerStateModel.CustInvoiceDetailsAR> customerDetails, String printView) {
        this.context=context;
        this.customerDetailsLists = customerDetails;
        this.printView=printView;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.sap_ro_customer_outstand_preview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        CustomerStateModel.CustInvoiceDetailsAR customerList =customerDetailsLists.get(position);

        viewHolder.slNo.setText(String.valueOf(position+1));
        viewHolder.invoiceno.setText(customerList.getInvoiceNumber());
        viewHolder.invoicedate.setText(customerList.getInvoiceDate());
        viewHolder.nettotal.setText(customerList.getNetTotal());
        viewHolder.balance.setText(customerList.getBalanceAmount());
    }

    @Override
    public int getItemCount() {
        return customerDetailsLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView slNo;
        private TextView invoiceno;
        private TextView invoicedate,nettotal,balance;

        public ViewHolder(View view) {
            super(view);
            slNo=view.findViewById(R.id.itemsno);
            invoiceno=view.findViewById(R.id.item_invoiceno);
            invoicedate=view.findViewById(R.id.item_invoicedate);
            nettotal=view.findViewById(R.id.itemnettoal);
            balance=view.findViewById(R.id.item_balance);
        }
    }
}