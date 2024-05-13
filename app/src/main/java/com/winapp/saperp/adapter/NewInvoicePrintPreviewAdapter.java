package com.winapp.saperp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.winapp.saperp.R;
import com.winapp.saperp.model.AppUtils;
import com.winapp.saperp.model.InvoicePrintPreviewModel;
import com.winapp.saperp.utils.Utils;

import java.util.ArrayList;

public class NewInvoicePrintPreviewAdapter extends RecyclerView.Adapter<NewInvoicePrintPreviewAdapter.ViewHolder> {

    private ArrayList<InvoicePrintPreviewModel.InvoiceList> invoiceLists;
    private String companyName;
    private Context context;
    View view;
    private String printView;
    public NewInvoicePrintPreviewAdapter(Context context, ArrayList<InvoicePrintPreviewModel.InvoiceList> invoices,
                                         String printView , String companyName) {
        this.context=context;
        this.invoiceLists = invoices;
        this.printView=printView;
        this.companyName=companyName;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invoice_print_preview_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        InvoicePrintPreviewModel.InvoiceList invoiceList=invoiceLists.get(position);
        viewHolder.slNo.setText(String.valueOf(position+1));
        if (invoiceList.getUomCode()!=null && !invoiceList.getUomCode().equals("null") && !invoiceList.getUomCode().isEmpty()){
            viewHolder.product.setText(invoiceList.getDescription()+" ("+invoiceList.getUomCode()+")");
        }else {
            viewHolder.product.setText(invoiceList.getDescription());
        }
        Log.w("cpmadpta",""+companyName);

        if(companyName.equalsIgnoreCase("Trans Orient Singapore Pte Ltd")){
            viewHolder.rtn.setVisibility(View.INVISIBLE);
            viewHolder.iss.setVisibility(View.INVISIBLE);
        }
        else {
            viewHolder.rtn.setVisibility(View.VISIBLE);
            viewHolder.iss.setVisibility(View.VISIBLE);
            viewHolder.iss.setText(invoiceList.getNetQty());
            viewHolder.rtn.setText(invoiceList.getReturnQty());

        }
        viewHolder.net.setText(invoiceList.getNetQuantity());

       // viewHolder.price.setText(Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getPricevalue())));
        viewHolder.price.setText(invoiceList.getPricevalue());
        viewHolder.total.setText(Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getTotal())));
    }

    @Override
    public int getItemCount() {
        return invoiceLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView slNo;
        private TextView product;
        private TextView rtn;
        private TextView net,iss;
        private TextView price;
        private TextView total;
        public ViewHolder(View view) {
            super(view);
            slNo=view.findViewById(R.id.itemsno);
            product=view.findViewById(R.id.itemproduct);
            rtn=view.findViewById(R.id.itemrtn);
            iss=view.findViewById(R.id.itemiss);
            net=view.findViewById(R.id.itemnet);
            price=view.findViewById(R.id.itemprice);
            total=view.findViewById(R.id.itemtotal);
        }
    }

}