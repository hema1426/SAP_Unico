package com.winapp.saperpUNICO.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.winapp.saperpUNICO.R
import com.winapp.saperpUNICO.activity.CreateNewInvoiceActivity
import com.winapp.saperpUNICO.model.AppUtils
import com.winapp.saperpUNICO.model.CreditLimitDialogResponse
import com.winapp.saperpUNICO.model.ItemGroupList
import com.winapp.saperpUNICO.model.ProductsModel
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.Objects
import android.widget.ArrayAdapter

@SuppressLint("StaticFieldLeak")
object CommonMethodKotl {
    var mDialog: Dialog? = null

    fun cancelProgressDialog() {
        if (mDialog != null
            && mDialog!!.isShowing
        ) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }
    fun showError_dialog(activity: Activity,msg: String) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Error")
        alertDialogBuilder.setMessage(msg)
        alertDialogBuilder.setPositiveButton(
            "Ok"
        ) { dialog, which ->
            dialog.dismiss()
        }
        alertDialogBuilder.show()
    }

    fun showProgressDialog(activity: Activity) {
        if (activity != null
            && mDialog == null
        ) {
            mDialog = Dialog(activity)
            mDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mDialog!!.setCancelable(false)
            mDialog!!.setCanceledOnTouchOutside(false)
            mDialog!!.setContentView(R.layout.dialog)
            mDialog!!.window!!
                .setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        android.R.color.transparent
                    )
                )
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(mDialog!!.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            mDialog!!.window!!.attributes = lp
            mDialog!!.show()
        }
    }



}
