package com.winapp.saperpUNICO.activity

import android.app.Dialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.winapp.saperpUNICO.R
import com.winapp.saperpUNICO.activity.NewInvoiceListActivity.customerGroupClickListener
import com.winapp.saperpUNICO.activity.ReportsActivity.customerClickListenerRo
import com.winapp.saperpUNICO.activity.ReportsActivity.customerGroupClickListenerRo
import com.winapp.saperpUNICO.activity.ReportsActivity.supplierClickListenerRo
import com.winapp.saperpUNICO.activity.SalesOrderListActivity.customerGroupClickListenerSO
import com.winapp.saperpUNICO.model.CustSeperateGroupModel
import com.winapp.saperpUNICO.model.CustomerModel
import com.winapp.saperpUNICO.model.SupplierModel
import java.util.Locale
import java.util.Objects


open class SearchableSpinnerCustomDialog : NavigationActivity() {
    var ed_search_spinner: EditText? = null
    var rv_searchableSpinner: ListView? = null
    var dialog: Dialog? = null
    var searchableCustList: java.util.ArrayList<String>? = null
    var searchableSupplierList: java.util.ArrayList<String>? = null

    fun searchable_CustgroupDialog(action:String, arrayList: ArrayList<CustSeperateGroupModel>, arrayList1: ArrayList<CustSeperateGroupModel>) {
        val li = LayoutInflater.from(this)
        val promptsView = li.inflate(R.layout.searchable_spinner_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        val closeImg = promptsView.findViewById<ImageView>(R.id.dial_close_search)
       ed_search_spinner = promptsView.findViewById<View>(R.id.editsearch_spinner) as EditText
       rv_searchableSpinner = promptsView.findViewById<View>(R.id.rv_searchableSpinner) as ListView

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList)
        rv_searchableSpinner!!.setAdapter(arrayAdapter)
        ed_search_spinner!!.setHint("Search Customer Group")

       ed_search_spinner!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {

                if (editable.toString().length > 0) {
                    filter(editable.toString(),arrayList)
                } else {
                    rv_searchableSpinner!!.setAdapter(arrayAdapter)
                }
            }
        })

        rv_searchableSpinner!!.setOnItemClickListener { parent, view, position, id ->
            var selectitem = ""
            val element = parent.getItemAtPosition(position) // The item that was clicked
            Log.w("arralistting",""+arrayAdapter.getItem(position)+"..."+element);

            for (i in 0 until arrayList1.size) {
                if(arrayList1.get(i).customerGroupName.equals(element.toString(),true)){
                    Log.w("arralistting11",""+arrayList1.get(i).customerGroupCode)
                    selectitem = arrayList1.get(i).customerGroupCode
                }
            }
            if(action.equals("Invoice")){
                customerGroupClickListener.groupSelected(selectitem)
            }
            if(action.equals("SalesOrder")){
                customerGroupClickListenerSO.groupSelected(selectitem)
            }
            if(action.equals("Report")){
                customerGroupClickListenerRo.groupSelected(selectitem)
            }
            dialog!!.dismiss()
        }

        closeImg.setOnClickListener {dialog!!.dismiss() }
        alertDialogBuilder.setView(promptsView)
       dialog = alertDialogBuilder.create()
       dialog!!.setCancelable(true)
       dialog!!.show()
    }
    fun filter(text: String,custGroupLists : ArrayList<CustSeperateGroupModel>) {
        try {
            //new array list that will hold the filtered data
            val filterdNames = java.util.ArrayList<CustSeperateGroupModel>()
            //looping through existing elements
            for (s in custGroupLists) {

                //if the existing elements contains the search input
                if (s.customerGroupName.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault()))
                    || s.customerGroupCode.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault()))
                ) {
                    //adding the element to filtered list
                    filterdNames.add(s)
                }
                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filterdNames)
                rv_searchableSpinner!!.setAdapter(arrayAdapter)
            }
            //calling a method of the adapter class and passing the filtered list
//            if (filterdNames.size > 0) {
//                adapter.filterList(filterdNames)
//                productRecyclerView.setVisibility(View.VISIBLE)
//                emptyLayout.setVisibility(View.GONE)
//            } else {
//                emptyLayout.setVisibility(View.VISIBLE)
//                productRecyclerView.setVisibility(View.GONE)
//            }
//            noOfProducts.setText(filterdNames.size.toString() + " Products")
        } catch (ex: Exception) {
            Log.e("Error_in_filter", Objects.requireNonNull(ex.message)!!)
        }
    }


    fun searchableCustDialog(action:String , searchableCustomerList:ArrayList<String>,custGroupLists : ArrayList<CustomerModel>) {
        val li = LayoutInflater.from(this)
        val promptsView = li.inflate(R.layout.searchable_spinner_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        val closeImg = promptsView.findViewById<ImageView>(R.id.dial_close_search)
        ed_search_spinner = promptsView.findViewById<View>(R.id.editsearch_spinner) as EditText
        rv_searchableSpinner = promptsView.findViewById<View>(R.id.rv_searchableSpinner) as ListView

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, searchableCustomerList)
        rv_searchableSpinner!!.setAdapter(arrayAdapter)
        ed_search_spinner!!.setHint("Search Customer")

        rv_searchableSpinner!!.setOnItemClickListener { parent, view, position, id ->
            var selectCode = ""
            var selectName = ""
            val element = parent.getItemAtPosition(position) // The item that was clicked
            selectName = element.toString()
            Log.w("arralistting",""+arrayAdapter.getItem(position)+"..."+element);

//            for (i in 0 until custGroupLists.size) {
//                if(custGroupLists.get(i).customerName.equals(element.toString(),true)){
//                    Log.w("arralistting11",""+custGroupLists.get(i).customerCode)
//                    selectCode = custGroupLists.get(i).customerCode
//                    selectName = custGroupLists.get(i).customerName
//                }
//            }

            if(action.equals("Report")){
                customerClickListenerRo.custSelected(selectName)
            }
            dialog!!.dismiss()
        }

        ed_search_spinner!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {

                if (editable.toString().length > 0) {
                    filterCustomer(editable.toString(),custGroupLists)
                } else {
                    rv_searchableSpinner!!.setAdapter(arrayAdapter)
                }
            }
        })

        closeImg.setOnClickListener {dialog!!.dismiss() }
        alertDialogBuilder.setView(promptsView)
        dialog = alertDialogBuilder.create()
        dialog!!.setCancelable(true)
        dialog!!.show()
    }
    fun filterCustomer(text: String,custGroupLists : ArrayList<CustomerModel>) {
        //try {
            //new array list that will hold the filtered data
            val filterdNames = java.util.ArrayList<CustomerModel>()
        searchableCustList = java.util.ArrayList<String>()

        //looping through existing elements
            for (s in custGroupLists) {

                //if the existing elements contains the search input
                if (s.customerName.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault()))
                    || s.customerCode.lowercase(Locale.getDefault())
                        .contains(text.lowercase(Locale.getDefault()))
                ) {
                    //adding the element to filtered list
                    filterdNames.add(s)
                }

            }
     //   if (filterdNames.size > 0) {
            for (s1 in filterdNames) {
                searchableCustList!!.add(s1.customerName + "~" + s1.customerCode)
            }
            val arrayAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                searchableCustList!!
            )
            rv_searchableSpinner!!.setAdapter(arrayAdapter)
       // }

    }
    fun searchable_supplierDialog(action:String , searchableSupplierList:ArrayList<String>,
                                  supplierLists : ArrayList<SupplierModel>) {
        val li = LayoutInflater.from(this)
        val promptsView = li.inflate(R.layout.searchable_spinner_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        val closeImg = promptsView.findViewById<ImageView>(R.id.dial_close_search)
        ed_search_spinner = promptsView.findViewById<View>(R.id.editsearch_spinner) as EditText
        rv_searchableSpinner = promptsView.findViewById<View>(R.id.rv_searchableSpinner) as ListView

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, searchableSupplierList)
        rv_searchableSpinner!!.setAdapter(arrayAdapter)
        ed_search_spinner!!.setHint("Search Supplier")

        rv_searchableSpinner!!.setOnItemClickListener { parent, view, position, id ->
            var selectCode = ""
            var selectName = ""
            val element = parent.getItemAtPosition(position) // The item that was clicked
            selectName = element.toString()

            Log.w("arralistting",""+arrayAdapter.getItem(position)+"..."+element);

//            for (i in 0 until supplierLists.size) {
//                if(supplierLists.get(i).customerName.equals(element.toString(),true)){
//                    Log.w("arralistting11",""+supplierLists.get(i).customerCode)
//                    selectCode = supplierLists.get(i).customerCode
//                    selectName = supplierLists.get(i).customerName
//                }
//            }
            if(action.equals("Report")){
                supplierClickListenerRo.supplierSelected(selectName)
            }
            if(action.equals("Purchase")){
                PurchaseInvoiceListActivity.supplierClickListenerPurchase!!.supplierSelected(selectName)
            }
            dialog!!.dismiss()
        }
        ed_search_spinner!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {

                if (editable.toString().length > 0) {
                    filterSupplier(editable.toString(),supplierLists)
                } else {
                    rv_searchableSpinner!!.setAdapter(arrayAdapter)
                }
            }
        })

        closeImg.setOnClickListener {dialog!!.dismiss() }
        alertDialogBuilder.setView(promptsView)
        dialog = alertDialogBuilder.create()
        dialog!!.setCancelable(true)
        dialog!!.show()
    }
    fun filterSupplier(text: String,supplierLists : ArrayList<SupplierModel>) {
        //try {
        //new array list that will hold the filtered data
        var filterdNames = java.util.ArrayList<SupplierModel>()
        searchableSupplierList = java.util.ArrayList<String>()

        //looping through existing elements
        for (s in supplierLists) {

            //if the existing elements contains the search input
            if (s.customerName.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
                || s.customerCode.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                //adding the element to filtered list
                filterdNames.add(s)
            }

        }

        searchableSupplierList!!.clear()
       // if (filterdNames.size > 0) {
            for (s1 in filterdNames) {
                searchableSupplierList!!.add(s1.customerName + "~" + s1.customerCode)
            }
            val arrayAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                searchableSupplierList!!
            )
            rv_searchableSpinner!!.setAdapter(arrayAdapter)
        //}
    }


    interface CustomerGroupClickListener {
        fun groupSelected(id: String?)
    }

    interface SupplierClickListener {
        fun supplierSelected(supplierName: String)
    }

    interface CustomerClickListener {
        fun custSelected(customerName: String)
    }
}
