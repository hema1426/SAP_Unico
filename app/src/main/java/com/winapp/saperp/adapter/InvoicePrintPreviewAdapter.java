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
import com.winapp.saperp.activity.NewInvoiceListActivity;
import com.winapp.saperp.model.InvoicePrintPreviewModel;
import com.winapp.saperp.utils.Utils;

import java.util.ArrayList;

public class InvoicePrintPreviewAdapter extends RecyclerView.Adapter<InvoicePrintPreviewAdapter.ViewHolder> {

    private ArrayList<InvoicePrintPreviewModel.InvoiceList> invoiceLists;
    private Context context;
    View view;
    private String printView;
    public InvoicePrintPreviewAdapter(Context context,ArrayList<InvoicePrintPreviewModel.InvoiceList> invoices,String printView) {
        this.context=context;
        this.invoiceLists = invoices;
        this.printView=printView;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
       /*  if (context instanceof NewInvoiceListActivity){
             view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invoice_details_view_items, viewGroup, false);
         }else {
             view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invoice_print_preview_item, viewGroup, false);
         }*/

        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invoice_details_view_items, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        InvoicePrintPreviewModel.InvoiceList invoiceList=invoiceLists.get(position);
        viewHolder.slNo.setText(String.valueOf(position+1));

        if (invoiceList.getUomCode()!=null && !invoiceList.getUomCode().equals("null") && !invoiceList.getUomCode().isEmpty()){
            viewHolder.description.setText(invoiceList.getDescription()+" ("+invoiceList.getUomCode()+")");
        }else {
            viewHolder.description.setText(invoiceList.getDescription());
        }
        if (invoiceList.getSaleType().equals("Return")){
            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQuantity())+" (as Return)");
        }else if (invoiceList.getSaleType().equals("FOC")){
            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQuantity())+" ( as FOC)");
        } else if (invoiceList.getSaleType().equals("Exchange")){
            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQuantity())+" ( as Exch)");
        }else {
            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQuantity())+"");
        }

        viewHolder.price.setText(invoiceList.getPricevalue());
     //   viewHolder.qtyValue.setText(invoiceList.getNetQuantity());

        viewHolder.total.setText(Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getTotal())));

//        if (invoiceList.getUomCode()!=null && !invoiceList.getUomCode().equals("null")
//                && !invoiceList.getUomCode().isEmpty()) {
//
//            if (invoiceList.getFocQty() != null && !invoiceList.getFocQty().isEmpty()
//                    && Double.parseDouble(invoiceList.getFocQty()) > 0) {
//                if (invoiceList.getExcQty() != null && !invoiceList.getExcQty().isEmpty()
//                        && Double.parseDouble(invoiceList.getExcQty()) > 0) {
//                    Log.w("excInv", "" + invoiceList.getExcQty());
//
//                    viewHolder.description.setText((invoiceList.getDescription()) +
//                            " (" + invoiceList.getUomCode() + ")" + " ( as FOC)" + " ( as Exc)");
//                } else {
//                    Log.w("excInv11", "" + invoiceList.getExcQty());
//
//                    viewHolder.description.setText((invoiceList.getDescription()) +
//                            " (" + invoiceList.getUomCode() + ")" + " ( as FOC)");
//                }
//            } else {
//                if (invoiceList.getExcQty() != null && !invoiceList.getExcQty().isEmpty()
//                        && Double.parseDouble(invoiceList.getExcQty()) > 0) {
//                    viewHolder.description.setText((invoiceList.getDescription()) +
//                            " (" + invoiceList.getUomCode() + ")" + " ( as Exc)");
//
//                }
//                else{
//                    viewHolder.description.setText(invoiceList.getDescription()+" ("+invoiceList.getUomCode()+")");
//                }
//            }
//        }
//            else {
//                viewHolder.description.setText(invoiceList.getDescription());
//
//            }



//        if (invoiceList.getUomCode()!=null && !invoiceList.getUomCode().equals("null") && !invoiceList.getUomCode().isEmpty()){
//            viewHolder.description.setText(invoiceList.getDescription()+" ("+invoiceList.getUomCode()+")");
//        }else {
//            viewHolder.description.setText(invoiceList.getDescription());
//        }
//
//        if (Double.parseDouble(invoiceList.getTotal()) < 0.00){
//            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQty())+" (as Return)");
//        }else if (Double.parseDouble(invoiceList.getTotal())==0.00){
//            if (invoiceList.getReturnQty()!=null && !invoiceList.getReturnQty().isEmpty()
//                    && Double.parseDouble(invoiceList.getReturnQty()) > 0){
//                viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQty())+" ( as Return)");
//            }else {
//                if (invoiceList.getExcQty()!=null && !invoiceList.getExcQty().isEmpty() && Double.parseDouble(invoiceList.getExcQty()) > 0) {
//                    viewHolder.qtyValue.setText((int) Double.parseDouble(invoiceList.getNetQty()) + " ( as FOC)");
//                }
//                else{
//                    viewHolder.qtyValue.setText((int) Double.parseDouble(invoiceList.getNetQty()) + " ( as FOC)"+ " ( as Exc)");
//                }
//            }
//        }else {
//            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQty())+"");
//        }

       /* if (invoiceList.getNetQty().contains("-")){
            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQty())+" (as Return)");
        }else {
            viewHolder.qtyValue.setText((int)Double.parseDouble(invoiceList.getNetQty())+"");
        }*/
       /* if (printView.equals("Invoice")){
            viewHolder.price.setVisibility(View.VISIBLE);
            viewHolder.total.setVisibility(View.VISIBLE);
        }else {
            viewHolder.price.setVisibility(View.GONE);
            viewHolder.total.setVisibility(View.GONE);
        }*/
        //viewHolder.price.setText(Utils.twoDecimalPoint(Double.parseDouble(invoiceList.getPricevalue())));

    }

    @Override
    public int getItemCount() {
        return invoiceLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView slNo;
        private TextView description;
        private TextView qtyValue;
        private TextView price;
        private TextView total;
        public ViewHolder(View view) {
            super(view);
            slNo=view.findViewById(R.id.sl_no);
            description=view.findViewById(R.id.description);
            qtyValue=view.findViewById(R.id.qty);
            price=view.findViewById(R.id.price);
            total=view.findViewById(R.id.total);
        }
    }

}