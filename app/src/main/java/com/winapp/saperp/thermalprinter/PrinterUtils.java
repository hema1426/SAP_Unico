package com.winapp.saperp.thermalprinter;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.winapp.saperp.thermalprinter.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.winapp.saperp.thermalprinter.DeviceConnFactoryManager.CONN_STATE_FAILED;
import static com.winapp.saperp.utils.Utils.twoDecimalPoint;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cete.dynamicpdf.merger.T;
import com.example.tscdll.TSCActivity;
import com.printer.command.EscCommand;
import com.printer.command.LabelCommand;
import com.winapp.saperp.db.DBHelper;
import com.winapp.saperp.model.InvoicePrintPreviewModel;
import com.winapp.saperp.model.SettingsModel;
import com.winapp.saperp.model.TransferDetailModel;
import com.winapp.saperp.utils.Constants;
import com.winapp.saperp.utils.SessionManager;
import com.winapp.saperp.utils.Utils;
import com.winapp.saperp.zebraprinter.TSCPrinter;
import com.zebra.sdk.printer.ZebraPrinter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import okhttp3.internal.Util;

public class PrinterUtils extends AppCompatActivity {

    private Context context;
    private String macAddress;
    private TSCActivity TscDll;
    private String username;

    private ZebraPrinter printer;
    private BluetoothAdapter bluetoothAdapter;
    public static String configLabel ="";
    // private SettingsManager settings;
    private static final String FILE_NAME = "TEMP.LBL";
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String LINEBREAK = "\r\b";
    private static final int LABEL_WIDTH = 574;
    private static final int FONT = 5;
    private static final int FONT_SIZE = 0;
    private static final int LEFT_MARGIN = 10;
    private static final int RIGHT_MARGIN = 564;
    private static final int LINE_THICKNESS = 2;
    private static final int LINE_SPACING = 40;
    private static final int TITLE_LINE_SPACING = 30;
    private static final String CMD_TEXT = "T";
    private static final String CMD_LINE = "L";
    private static final String CMD_PRINT = "PRINT" + LINE_SEPARATOR;
    private static final String SPACE = " ";
    private static final int POSTFEED = 50;
    public static int PAPER_WIDTH = 78;
    Bitmap logo,image;
    String logoStr = "logoprint",mUser="";
    private static final String HEADER = "PW " + LABEL_WIDTH + LINE_SEPARATOR
            + "TONE 0" + LINE_SEPARATOR + "SPEED 3" + LINE_SEPARATOR + "ZPL"
            + POSTFEED + LINE_SEPARATOR + "NO-PACE" + "BAR-SENSE"
            + LINE_SEPARATOR;
    byte FONT_TYPE;
    String custlastname;
    private TSCPrinter.OnCompletionListener listener;
    private String company_name;
    private String company_code;
    private String company_address1;
    private String company_address2;
    private String company_address3;
    private String company_phone;
    private String company_gst;
    private SessionManager session;
    private HashMap<String ,String> user;
    public ArrayList<SettingsModel> settingsList;
    private DBHelper dbHelper;
    private String showUserName="";
    private String showSignature="";
    private String showLogo="";
    private String showReturn="";
    private String showUom="";
    private String showQrCode="";
    private String showStamp="";
    private String payNow="";
    private String bankCode="";
    private String cheque="";

    /**
     * RequestCode
     */
    private static final int REQUEST_CODE = 0x001;

    /**
     * Permissions
     */
    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH
    };

    /**
     * Permission Array list
     */
    private ArrayList<String> per = new ArrayList<>();

    /**
     * 蓝牙请求码
     */
    public static final int BLUETOOTH_REQUEST_CODE = 0x002;

    private ThreadPool threadPool;//线程

    /**
     * Printer ID
     */
    private int id = 0;

    /**
     * Connect Printer ID
     */
    private static final int CONN_PRINTER = 0x003;
    /**
     * Command Error Connection
     */
    private static final int PRINTER_COMMAND_ERROR = 0x004;

    /**
     * Disconnection Status
     */
    private static final int CONN_STATE_DISCONN = 0x005;

    public PrinterUtils(Context context,String macAddress) {
        this.context = context;
        this.macAddress = macAddress;
        TscDll = new TSCActivity();
        session = new SessionManager(context);
        dbHelper = new DBHelper(context);
        user = session.getUserDetails();
        username = user.get(SessionManager.KEY_USER_NAME);
        company_name = user.get(SessionManager.KEY_COMPANY_NAME);
        company_code = user.get(SessionManager.KEY_COMPANY_CODE);
        company_address1 = user.get(SessionManager.KEY_ADDRESS1);
        company_address2 = user.get(SessionManager.KEY_ADDRESS2);
        company_address3 = user.get(SessionManager.KEY_ADDRESS3);
        company_phone = user.get(SessionManager.KEY_PHONE_NO);
        company_gst = user.get(SessionManager.KEY_COMPANY_REG_NO);
        payNow=user.get(SessionManager.KEY_PAY_NOW);
        cheque=user.get(SessionManager.KEY_CHEQUE);
        bankCode=user.get(SessionManager.KEY_BANK);
        settingsList = dbHelper.getSettings();
        if (settingsList != null) {
            if (settingsList.size() > 0) {
                for (SettingsModel model : settingsList) {
                    if (model.getSettingName().equals("showUserName")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            showUserName = "true";
                        } else {
                            showUserName = "false";
                        }
                    }else if (model.getSettingName().equals("showLogo")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            showLogo="true";
                        }else {
                            showLogo="false";
                        }
                    }else if (model.getSettingName().equals("showReturnDetails")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            showReturn="true";
                        }else {
                            showReturn="false";
                        }
                    }else if (model.getSettingName().equals("showUom")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            showUom="true";
                        }else {
                            showUom="false";
                        }
                    }else if (model.getSettingName().equals("showSignature")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            showSignature="true";
                        }else {
                            showSignature="false";
                        }
                    }else if (model.getSettingName().equals("showQRCode")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            showQrCode="true";
                        }else {
                            showQrCode="false";
                        }
                    }else if (model.getSettingName().equals("showPaidOrUnpaidImage")) {
                        Log.w("SettingName:", model.getSettingName());
                        Log.w("SettingValue:", model.getSettingValue());
                        if (model.getSettingValue().equals("True")) {
                            showStamp="true";
                        }else {
                            showStamp="false";
                        }
                    }
                }
            }
        }
    }


    public void printLabel() {
        Log.i("TAG", "PrintLabel");
        threadPool = ThreadPool.getInstantiation();
        threadPool.addTask(new Runnable() {
            @Override
            public void run() {
                //先判断打印机是否连接
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                    return;
                }
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    Log.i("TAG", "start printing");
                    sendLabel();
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
         * 注册接收连接状态的广播
         */
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_QUERY_PRINTER_STATE);
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

/*
    public void printInvoice(int copy, ArrayList<InvoicePrintPreviewModel> invoiceHeaderDetails, ArrayList<InvoicePrintPreviewModel.InvoiceList> invoiceList, String isDoPrint){
        TscDll.openport(this.macAddress);

        //String status = TscDll.printerstatus(300);
        int y=0;
        int height=50;

        TscDll.sendcommand("GAP 0 mm, 0 mm\r\n");//Gap media
      //  TscDll.sendcommand("BLINE 1 mm, 0 mm\r\n");//blackmark media
        TscDll.clearbuffer();
        TscDll.sendcommand("SPEED 4\r\n");
        TscDll.sendcommand("DENSITY 12\r\n");
        TscDll.sendcommand("CODEPAGE UTF-8\r\n");
        TscDll.sendcommand("SET TEAR ON\r\n");
        TscDll.sendcommand("SET COUNTER @1 1\r\n");
        TscDll.sendcommand("@1 = \"0001\"\r\n");
        TscDll.sendcommand("TEXT 50,100,\"FONT001.TTF\",0,2,2,@1\r\n");
        TscDll.sendcommand("TEXT 50,100,\"FONT001.TTF\",0,2,2,\"TEST FONT\"\n");

        // Define the Line
       // TscDll.sendcommand("BAR 0,70,800,4\n");
      //  TscDll.sendcommand("TEXT 0,260,\"2\",0,1,1,1,\"TSC Printer\"\n");
        TscDll.sendcommand("TEXT 10,10,\"Poppins.TTF\",0,10,12,\"TEST FONT View\"\n");
        TscDll.sendcommand("TEXT 10,100,\"Bold.TTF\",0,10,12,\"TEST FONT View\"\n");


       // tsc.addText(0, 150, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
        ///TscDll.barcode(0, 100, "128", 100, 1, 0, 3, 3, "123456789");
     */
/*   StringBuilder temp = new StringBuilder();
        if (company_name.length() > 38) {
            temp.append((company_name.length() > 38) ? company_name.substring(0, 37) : company_name);
            TscDll.printerfont(0, y += LINE_SPACING, "3", 0, 1, 1,
                    temp.toString());
            String companyname = company_name.substring(37);
            temp.append((companyname.length() > 37) ? companyname.substring(0, 36) : companyname);
            TscDll.printerfont(0, y += LINE_SPACING, "3", 0, 1, 1,
                    temp.toString());
        }else {
            TscDll.printerfont(0, y += LINE_SPACING, "3", 0, 1, 1,
                    company_name);
        }

        if (company_address1!=null && !company_address1.isEmpty()){
            TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                    company_address1);
        }

        if(company_address2!=null && !company_address2.isEmpty()){
            TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                    company_address2);
        }

        if (company_address3!=null && !company_address3.isEmpty()){
            TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                    company_address3);
        }

        if (company_phone!=null && !company_phone.isEmpty()){
            TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                    "TEL :"+company_phone);
        }

        if (company_gst!=null && !company_gst.isEmpty()){
            TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                    "CO REG NO : "+company_gst);
        }

        // Define the Box
      //  TscDll.sendcommand("BOX 10,0,780,490,8\n");

        TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                "__________________________________________________");

        TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                "SN");
        TscDll.printerfont(70, y , "2", 0, 1, 1,
                "PRODUCT");
        TscDll.printerfont(400, y , "2", 0, 1, 1,
                "PRICE");
        TscDll.printerfont(500, y , "2", 0, 1, 1,
                "TOTAL");

        TscDll.printerfont(70, y += LINE_SPACING, "2", 0, 1, 1,
                "ISS");
        TscDll.printerfont(200, y , "2", 0, 1, 1,
                "RTN");
        TscDll.printerfont(300, y , "2", 0, 1, 1,
                "NET");

        TscDll.printerfont(400, y , "2", 0, 1, 1,
                "($)");

        TscDll.printerfont(500, y , "2", 0, 1, 1,
                "($)");
        TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                "__________________________________________________");
        int index=1;
        for (InvoicePrintPreviewModel.InvoiceList invoice : invoiceList) {
            TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 0, 0,
                    ""+index+"   "+invoice.getDescription()+"           ");
            TscDll.printerfont(80, y += LINE_SPACING, "2", 0, 1, 1,
                    ""+(int)Double.parseDouble(invoice.getNetQty()));
            TscDll.printerfont(210, y , "2", 0, 1, 1,
                    ""+(int)Double.parseDouble(invoice.getReturnQty()));
            TscDll.printerfont(310, y , "2", 0, 1, 1,
                    ""+(int)Double.parseDouble(invoice.getNetQuantity()));

            TscDll.printerfont(410, y , "2", 0, 1, 1,
                    ""+(int)Double.parseDouble(invoice.getPricevalue()));

            TscDll.printerfont(510, y , "2", 0, 1, 1,
                    ""+(int)Double.parseDouble(invoice.getTotal()));
            index++;
            height=height+10;
        }
        TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                "__________________________________________________");

        TscDll.printerfont(200, y += LINE_SPACING, "2", 0, 1, 1,
                "SUB TOTAL:$  ");
        TscDll.printerfont(480, y , "2", 0, 1, 1,
                twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSubTotal())));


        TscDll.printerfont(200, y += LINE_SPACING, "2", 0, 1, 1,
                "GST("+invoiceHeaderDetails.get(0).getTaxType()+":"+ (int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+"):$");
        TscDll.printerfont(480, y , "2", 0, 1, 1,
                twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTax())));

        TscDll.printerfont(200, y += LINE_SPACING, "2", 0, 1, 1,
                "GRAND TOTAL:$  ");
        TscDll.printerfont(450, y , "2", 0, 2, 1,
                twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTotal())));

        TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                "__________________________________________________");


        TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                "Total Outstanding : "+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getOutStandingAmount())));


        TscDll.printerfont(0, y += LINE_SPACING, "2", 0, 1, 1,
                "__________________________________________________");

        height=height+5;*//*

        TscDll.sendcommand("SIZE 75 mm, "+height+" mm\r\n");
        TscDll.printlabel(1, 1);
        TscDll.closeport(5000);
    }
*/

    public void printInvoice(int copy, ArrayList<InvoicePrintPreviewModel> invoiceHeaderDetails,
                             ArrayList<InvoicePrintPreviewModel.InvoiceList> invoiceList, String isDoPrint){

        TscDll.openport(this.macAddress);
        //String status = TscDll.printerstatus(300);
        int y=0;
        int height=120;
        if (showSignature.equals("true")){
            String filePath2 = Constants.getSignatureFolderPath(context);
            String fileName2 = "Signature.jpg";
            File mFile2 = new File(filePath2, fileName2);
            if (mFile2.exists()){
                height+=20;
            }
        }

        if (showLogo.equals("true")){
            try {
                String filePath = Constants.getSignatureFolderPath(context);
                String fileName = "Logo.jpg";
                File mFile = new File(filePath, fileName);
                if (mFile.exists()){
                    height+=20;
                }
            }catch (Exception e){}

        }

        if (showStamp.equals("true")){
            try {
                String fileName = "Paid.jpg";
                String filePath = Constants.getSignatureFolderPath(context);
                File mFile = new File(filePath, fileName);
                if (mFile.exists()){
                    height+=20;
                }
            }catch (Exception e){}
        }

        int finalHeight=height+(invoiceList.size() * 10)+50;
        TscDll.sendcommand("SIZE 80 mm, "+finalHeight+" mm\n");
        TscDll.sendcommand("GAP 0 mm, 0 mm\r\n");//Gap media
        TscDll.sendcommand("BLINE 0 mm, 0 mm\r\n");//blackmark media
        TscDll.clearbuffer();
        TscDll.sendcommand("SPEED 4\r\n");
        TscDll.sendcommand("DENSITY 12\r\n");
        TscDll.sendcommand("CODEPAGE UTF-8\r\n");
        TscDll.sendcommand("SET TEAR ON\r\n");
        TscDll.sendcommand("SET COUNTER @0 +1\r\n");
        TscDll.sendcommand("@0 = \"000001\"\r\n");

        if (showLogo.equals("true")){
            try {
                String filePath = Constants.getSignatureFolderPath(context);
                String fileName = "Logo.jpg";
                File mFile = new File(filePath, fileName);
                if (mFile.exists()){
                    y += LINE_SPACING;
                    TscDll.sendpicture_resize(180, y, mFile.getAbsolutePath(),200,200);
                    y += 170;
                }
            }catch (Exception e){}

        }

        // tsc.addText(0, 150, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
        ///TscDll.barcode(0, 100, "128", 100, 1, 0, 3, 3, "123456789");
        y += LINE_SPACING;
        TscDll.sendcommand("TEXT 260,"+y+",\"Bold.TTF\",0,10,10,2,\""+company_name+"\"\n\n");


        if (company_address1!=null && !company_address1.isEmpty()){
            y += LINE_SPACING;
            Log.w("Address1_Value:",company_address1);
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address1+"\"\n");
        }

        if(company_address2!=null && !company_address2.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address2+"\"\n\n");
        }

        if (company_address3!=null && !company_address3.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address3+"\"\n");
        }

        if (company_phone!=null && !company_phone.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\" TEL : "+company_phone+"\"\n");
        }

        if (company_gst!=null && !company_gst.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\"UEN-No/CO REG NO : "+company_gst+"\"\n");
        }

        y += LINE_SPACING+10;
        if (Double.parseDouble(invoiceHeaderDetails.get(0).getNetTax()) > 0) {
            TscDll.sendcommand("TEXT 260," + y + ",\"Bold.TTF\",0,10,10,2,\"" + "Tax Invoice" + "\"\n");
        }
        else {
            TscDll.sendcommand("TEXT 260," + y + ",\"Bold.TTF\",0,10,10,2,\"" + "Invoice" + "\"\n");
        }

        y += LINE_SPACING+10;
        // Define the Box
        //TscDll.sendcommand("BOX 0,"+y+",570,0,2\n");
        TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,9,9,\""+"No : "+invoiceHeaderDetails.get(0).getInvoiceNumber()+"\"\n");
        TscDll.sendcommand("TEXT 350,"+y+",\"Bold.TTF\",0,8,8,\""+invoiceHeaderDetails.get(0).getInvoiceDate()+"-"+ Utils.getCurrentTime()+"\"\n");
        y += LINE_SPACING;
        if(invoiceHeaderDetails.get(0).getCustomerName().length()<=45){
            TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,8,8,\""+"CUST: "+invoiceHeaderDetails.get(0).getCustomerName()+"\"\n\n");
        } else{
            String firstname = invoiceHeaderDetails.get(0).getCustomerName().substring(0,42);
            String secondname = invoiceHeaderDetails.get(0).getCustomerName().substring(42);

            TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,8,8,\""+"CUST: "+firstname+"\"\n\n");
            y += 30;
            TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,8,8,\""+secondname+"\"\n\n");
        }
        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"CODE: "+invoiceHeaderDetails.get(0).getCustomerCode()+"\"\n");

      /*  if (!invoiceHeaderDetails.get(0).getAddress().isEmpty()){
            y += 30;
            String a = invoiceHeaderDetails.get(0).getAddress();
            String[] b = a.split(",");
            TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"ADD: "+b[0]+","+b[1]+"\"\n");
            if (b.length>=3){
                if (b[2]!=null){
                    y += 20;
                    TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,7,7,\"" + b[2] + "\"\n");
                }
            }
        }*/

        if (!invoiceHeaderDetails.get(0).getAddress().isEmpty()) {
            Log.w("GivenPrintAddress:",invoiceHeaderDetails.get(0).getAddress().toString());
            y += LINE_SPACING;
            if (invoiceHeaderDetails.get(0).getAddress().length() <= 45) {
                TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,8,8,\"" + "ADDR: " + invoiceHeaderDetails.get(0).getAddress() + "\"\n\n");
            } else {
                String address1 = invoiceHeaderDetails.get(0).getAddress().substring(0, 42);
                String address2 = invoiceHeaderDetails.get(0).getAddress().substring(42);

                TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,8,8,\"" + "ADDR: " + address1 + "\"\n\n");
                y += 30;
                TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,8,8,\"" + address2 + "\"\n\n");
            }
        }

        if (invoiceHeaderDetails.get(0).getPaymentTerm()!=null && !invoiceHeaderDetails.get(0).getPaymentTerm().isEmpty() && !invoiceHeaderDetails.get(0).getPaymentTerm().equals("null")){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 0," + y + ",\"Bold.TTF\",0,9,9,\" Payment Terms : " + invoiceHeaderDetails.get(0).getPaymentTerm().toUpperCase() + "\"\n\n");
        }
        if (showUserName.equals("true")){
            TscDll.sendcommand("TEXT 400,"+y+",\"Poppins.TTF\",0,8,8,\""+"User: "+username+"\"\n");
        }

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"SN"+"\"\n");
        TscDll.sendcommand("TEXT 50,"+y+",\"Poppins.TTF\",0,8,8,\""+"PRODUCT"+"\"\n");
        if (showReturn.equals("false")) {
            if(!company_code.equals("Trans Orient Singapore Pte Ltd")){
                TscDll.sendcommand("TEXT 300," + y + ",\"Poppins.TTF\",0,8,8,\"" + "NET" + "\"\n");
            }
        }
        TscDll.sendcommand("TEXT 400,"+y+",\"Poppins.TTF\",0,8,8,\""+"PRICE"+"\"\n");
        TscDll.sendcommand("TEXT 500,"+y+",\"Poppins.TTF\",0,8,8,\""+"TOTAL"+"\"\n");

      //  TscDll.sendcommand("TEXT 70,"+y+",\"Poppins.TTF\",0,8,8,\""+"ISS"+"\"\n");
        if (showReturn.equals("true")) {
            if(!company_code.equals("Trans Orient Singapore Pte Ltd")) {
                y += TITLE_LINE_SPACING;
                TscDll.sendcommand("TEXT 50," + y + ",\"Poppins.TTF\",0,8,8,\"" + "ISS" + "\"\n");
                TscDll.sendcommand("TEXT 200," + y + ",\"Poppins.TTF\",0,8,8,\"" + "RTN" + "\"\n");
                TscDll.sendcommand("TEXT 300," + y + ",\"Poppins.TTF\",0,8,8,\"" + "NET" + "\"\n");
                TscDll.sendcommand("TEXT 400," + y + ",\"Poppins.TTF\",0,8,8,\"" + "($)" + "\"\n");
                TscDll.sendcommand("TEXT 500," + y + ",\"Poppins.TTF\",0,8,8,\"" + "($)" + "\"\n");
            }
            else {
                y += TITLE_LINE_SPACING;
                TscDll.sendcommand("TEXT 50," + y + ",\"Poppins.TTF\",0,8,8,\"" + "ISS" + "\"\n");
                TscDll.sendcommand("TEXT 400," + y + ",\"Poppins.TTF\",0,8,8,\"" + "($)" + "\"\n");
                TscDll.sendcommand("TEXT 500," + y + ",\"Poppins.TTF\",0,8,8,\"" + "($)" + "\"\n");
            }
        }
      //  TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,8,8,\""+"RTN"+"\"\n");


        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        int index=1;
        for (InvoicePrintPreviewModel.InvoiceList invoice : invoiceList) {
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 5,"+y+",\"Bold.TTF\",0,8,8,\""+index+"\"\n");
            String productName="";
            if (Double.parseDouble(twoDecimalPoint(Double.parseDouble(invoice.getTotal())))==0.00){
                if (invoice.getReturnQty()!=null && !invoice.getReturnQty().isEmpty() && Double.parseDouble(invoice.getReturnQty()) > 0){
                    productName=invoice.getDescription()+"-(as Return)";
                }else {
                    productName=invoice.getDescription()+"-(as FOC)";
                }
            }else if (Double.parseDouble(twoDecimalPoint(Double.parseDouble(invoice.getTotal())))>0.00){
                productName=invoice.getDescription();
            }else {
                productName=invoice.getDescription()+"-(as RTN)";
            }
            String uomcode="";
            if(invoice.getUomCode()!=null && !invoice.getUomCode().isEmpty() && !invoice.getUomCode().equals("null")){
                uomcode="("+invoice.getUomCode()+")";
            }

            if (showUom.equals("true")) {
                TscDll.sendcommand("TEXT 50," + y + ",\"Bold.TTF\",0,8,8,\"" + productName + uomcode + "\"\n");
            } else {
                TscDll.sendcommand("TEXT 50," + y + ",\"Bold.TTF\",0,8,8,\"" + productName+ "\"\n");
            }

            if (showReturn.equals("true")) {
                if(!company_code.equals("Trans Orient Singapore Pte Ltd")) {
                    y += 30;
                    TscDll.sendcommand("TEXT 80," + y + ",\"Poppins.TTF\",0,8,8,\"" + (int) Double.parseDouble(invoice.getNetQty()) + "\"\n");
                    TscDll.sendcommand("TEXT 210," + y + ",\"Poppins.TTF\",0,8,8,\"" + (int) Double.parseDouble(invoice.getReturnQty()) + "\"\n");
                    TscDll.sendcommand("TEXT 310," + y + ",\"Poppins.TTF\",0,8,8,\"" + (int) Double.parseDouble(invoice.getNetQuantity()) + "\"\n");
/*
                TscDll.sendcommand("TEXT 410,"+y+",\"Poppins.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoice.getPricevalue()))+"\"\n");
*/
                    TscDll.sendcommand("TEXT 410," + y + ",\"Poppins.TTF\",0,8,8,\"" + invoice.getPricevalue() + "\"\n");
                    TscDll.sendcommand("TEXT 510," + y + ",\"Poppins.TTF\",0,8,8,\"" + twoDecimalPoint(Double.parseDouble(invoice.getTotal())) + "\"\n");
                }
                else{
                    y += 30;
                    TscDll.sendcommand("TEXT 80," + y + ",\"Poppins.TTF\",0,8,8,\"" + (int) Double.parseDouble(invoice.getNetQty()) + "\"\n");
//                    TscDll.sendcommand("TEXT 210," + y + ",\"Poppins.TTF\",0,8,8,\"" + (int) Double.parseDouble(invoice.getReturnQty()) + "\"\n");
//                    TscDll.sendcommand("TEXT 310," + y + ",\"Poppins.TTF\",0,8,8,\"" + (int) Double.parseDouble(invoice.getNetQuantity()) + "\"\n");
/*
                TscDll.sendcommand("TEXT 410,"+y+",\"Poppins.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoice.getPricevalue()))+"\"\n");
*/
                    TscDll.sendcommand("TEXT 410," + y + ",\"Poppins.TTF\",0,8,8,\"" + invoice.getPricevalue() + "\"\n");
                    TscDll.sendcommand("TEXT 510," + y + ",\"Poppins.TTF\",0,8,8,\"" + twoDecimalPoint(Double.parseDouble(invoice.getTotal())) + "\"\n");
                }
                }else {
                y += 30;
                TscDll.sendcommand("TEXT 310,"+y+",\"Poppins.TTF\",0,8,8,\""+(int)Double.parseDouble(invoice.getNetQuantity())+"\"\n");
/*
                TscDll.sendcommand("TEXT 410,"+y+",\"Poppins.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoice.getPricevalue()))+"\"\n");
*/
                TscDll.sendcommand("TEXT 410,"+y+",\"Poppins.TTF\",0,8,8,\""+invoice.getPricevalue()+"\"\n");
                TscDll.sendcommand("TEXT 510,"+y+",\"Poppins.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoice.getTotal()))+"\"\n");
            }

            index++;
        }
        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        if (company_code.equals("SUPERSTAR TRADERS PTE LTD")){
            y += 20;
            TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,8,8,\""+"SUB TOTAL: $ "+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSubTotal()))+"\"\n");

            y += LINE_SPACING;

           //TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"GST $ "+"\"\n");
             TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,8,8,\""+"GST("+invoiceHeaderDetails.get(0).getTaxType()+":"+ (int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % ):$ "+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTax()))+"\"\n");

            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,8,8,\""+"GRAND TOTAL: $ "+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTotal()))+"\"\n");
        }else {
            if (!invoiceHeaderDetails.get(0).getTaxType().equals("I")){
                y += 20;
                TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"SUB TOTAL:$ "+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSubTotal()))+"\"\n");

                y += LINE_SPACING;
                TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"GST("+invoiceHeaderDetails.get(0).getTaxType()+":"+ (int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % ):$ "+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTax()))+"\"\n");

                y += LINE_SPACING;
                TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"GRAND TOTAL:$ "+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTotal()))+"\"\n");

            }else {
                y += 30;
                TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"GRAND TOTAL:$ "+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTotal()))+"\"\n");
                y += 30;
                TscDll.sendcommand("TEXT 220,"+y+",\"Poppins.TTF\",0,8,8,\""+"(GST Included "+(int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % )\"\n");
            }
        }

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");


     /*   y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\"TOTAL OUTSTANDING: "+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getOutStandingAmount()))+"\"\n");

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");*/
// todo
        if(invoiceHeaderDetails.get(0).getSalesReturnList().size()>0){
            if (invoiceHeaderDetails.get(0).getTaxType().equalsIgnoreCase("I")){
                y += 20;
                TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"CN No : "+invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getSalesReturnNumber()+"\"\n");
                y += 30;
                TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"CN NET TOTAL:$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRNetTotal()))+"\"\n");

                y += 30;
                TscDll.sendcommand("TEXT 220,"+y+",\"Poppins.TTF\",0,8,8,\""+"(GST Included "+(int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % )\"\n");
            } else {
                y += 20;
                TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,9,9,\""+"CN No : "+invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getSalesReturnNumber()+"\"\n");
                y += 30;
                TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"CN SUB TOTAL:$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRSubTotal()))+"\"\n");

                y += LINE_SPACING;
                TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"GST("+invoiceHeaderDetails.get(0).getTaxType()+":"+ (int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % ):$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRTaxTotal()))+"\"\n");

                y += LINE_SPACING;
                TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"CN NET TOTAL:$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRNetTotal()))+"\"\n");
            }

            y += LINE_SPACING;
            TscDll.sendcommand("BAR 0,"+y+",800,2\n");

            y += 20;
            TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"BALANCE AMT:"+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getOverAllTotal()))+"\"\n");

            y += LINE_SPACING;
            TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        }


        y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"Customer Signature & Company Stamp"+"\"\n");

        if (showSignature.equals("true")){
            try {
                if (Utils.getSignature()!=null && !Utils.getSignature().isEmpty()){
                    y += LINE_SPACING;
                    String filePath = Constants.getSignatureFolderPath(context);
                    String fileName = "Signature.jpg";
                    File mFile = new File(filePath, fileName);
                    if (mFile.exists()){
                        TscDll.sendpicture_resize(0, y, mFile.getAbsolutePath(),400,80);
                    }
                }
            }catch (Exception e){}
        }

        y += 150;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"-------------------------------------------------"+"\"\n");

     /*   y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"THIS IS COMPUTER GENERATED INVOICE"+"\"\n");
        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"NO SIGNATURE REQUIRED"+"\"\n");*/

        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"RECEIVED ABOVE GOODS IN GOOD ORDER AND CONDITION"+"\"\n");

        if (showStamp.equals("true")){
            try {
                double balanceAmount= Double.parseDouble(invoiceHeaderDetails.get(0).getBalanceAmount());
                String fileName = "Paid.jpg";
                if (balanceAmount > 0.00){
                    fileName="UnPaid.jpg";
                }
                String filePath = Constants.getSignatureFolderPath(context);
                File mFile = new File(filePath, fileName);
                Log.w("FilePathValues:",mFile.toString());
                if (mFile.exists()){
                    y += 30;
                    TscDll.sendpicture_resize(150, y, mFile.getAbsolutePath(),200,200);
                    y+= 220;
                    TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"-------------------------------------------------"+"\"\n");
                }
            }catch (Exception e){}
        }


        if (showQrCode.equals("true")){
            try {
                String filePath = Constants.getSignatureFolderPath(context);
                String fileName = "QrCode.jpg";
                File mFile = new File(filePath, fileName);
                if (mFile.exists()){
                    y += 20;
                    TscDll.sendpicture_resize(120, y, mFile.getAbsolutePath(),300,300);
                /*if (paynow_uen!=null && !paynow_uen.isEmpty()){
                    y+=290;
                    TscDll.sendcommand("TEXT 160,"+y+",\"Bold.TTF\",0,8,8,\"UEN No: "+paynow_uen+"\"\n");
                }
                if (paynow_mobile!=null && !paynow_mobile.isEmpty()){
                    y+=30;
                    TscDll.sendcommand("TEXT 160,"+y+",\"Bold.TTF\",0,8,8,\"PayNow No: "+paynow_mobile+"\"\n");
                }*/
                }
            }catch (Exception e){}
        }

       // y += LINE_SPACING;
       // TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        if (payNow!=null && !payNow.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,8,8,2,\" PAYNOW : "+payNow+"\"\n");
        }
        if (bankCode!=null && !bankCode.isEmpty()){
            y += 30;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,8,8,2,\" BANK : "+bankCode+"\"\n");
        }
        if (cheque!=null && !cheque.isEmpty()){
            y += 30;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,8,8,2,\" CHEQUE : "+cheque+"\"\n");
        }
        y += 150;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"-------------------------------------------------"+"\"\n");

        if(!invoiceHeaderDetails.get(0).getSubTotal().isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,8,8,2,\" Sales Person : "+payNow+"\"\n");
        }
        if(!invoiceHeaderDetails.get(0).getSubTotal().isEmpty()){
            y += 30;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,8,8,2,\" Contact : "+bankCode+"\"\n");
        }

      //  y += 30;
     //   TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"-------------------------------------------------"+"\"\n");

        TscDll.printlabel(1, copy);
        TscDll.closeport(5000);
    }


 /*   public void printInvoice(int copy, ArrayList<InvoicePrintPreviewModel> invoiceHeaderDetails,
                             ArrayList<InvoicePrintPreviewModel.InvoiceList> invoiceList, String isDoPrint){
        TscDll.openport(this.macAddress);

        //String status = TscDll.printerstatus(300);
        int y=0;
        int height=150;

        TscDll.sendcommand("GAP 0 mm, 0 mm\r\n");//Gap media
        TscDll.sendcommand("BLINE 0 mm, 0 mm\r\n");//blackmark media
        TscDll.clearbuffer();
        TscDll.sendcommand("SPEED 4\r\n");
        TscDll.sendcommand("DENSITY 12\r\n");
        TscDll.sendcommand("CODEPAGE UTF-8\r\n");
        TscDll.sendcommand("SET TEAR ON\r\n");
        TscDll.sendcommand("SET COUNTER @0 +1\r\n");
        TscDll.sendcommand("@0 = \"000001\"\r\n");


        // Define the Line
        // TscDll.sendcommand("BAR 0,70,800,4\n");
        //  TscDll.sendcommand("TEXT 0,260,\"2\",0,1,1,1,\"TSC Printer\"\n");
        // TscDll.sendcommand("TEXT 10,10,\"Poppins.TTF\",0,10,12,\"TEST FONT View\"\n");
        // TscDll.sendcommand("TEXT 10,100,\"Bold.TTF\",0,10,12,\"TEST FONT View\"\n");


        // tsc.addText(0, 150, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
        ///TscDll.barcode(0, 100, "128", 100, 1, 0, 3, 3, "123456789");
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Bold.TTF\",0,10,10,2,\""+company_name+"\"\n\n");


        if (company_address1!=null && !company_address1.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address1+"\"\n");
        }

        if(company_address2!=null && !company_address2.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address2+"\"\n\n");
        }

        if (company_address3!=null && !company_address3.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address3+"\"\n");
        }

        if (company_phone!=null && !company_phone.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\" TEL : "+company_phone+"\"\n");
        }

        if (company_gst!=null && !company_gst.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\"UEN-No/CO REG NO : "+company_gst+"\"\n");
        }

        y += LINE_SPACING+10;
        if (Double.parseDouble(invoiceHeaderDetails.get(0).getNetTax()) > 0) {
            TscDll.sendcommand("TEXT 260," + y + ",\"Bold.TTF\",0,10,10,2,\"" + "Tax Invoice" + "\"\n");
        }
        else {
            TscDll.sendcommand("TEXT 260," + y + ",\"Bold.TTF\",0,10,10,2,\"" + "Invoice" + "\"\n");
        }


        y += LINE_SPACING+10;
        // Define the Box
        //TscDll.sendcommand("BOX 0,"+y+",570,0,2\n");
        TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,9,9,\""+"No : "+invoiceHeaderDetails.get(0).getInvoiceNumber()+"\"\n");
        TscDll.sendcommand("TEXT 380,"+y+",\"Bold.TTF\",0,7,7,\""+invoiceHeaderDetails.get(0).getInvoiceDate()+"-"+ Utils.getCurrentTime()+"\"\n");
        y += LINE_SPACING;
        if(invoiceHeaderDetails.get(0).getCustomerName().length()<=45){
            TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"CUST: "+invoiceHeaderDetails.get(0).getCustomerName()+"\"\n\n");
        } else{
            String firstname = invoiceHeaderDetails.get(0).getCustomerName().substring(0,42);
            String secondname = invoiceHeaderDetails.get(0).getCustomerName().substring(42);

            TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"CUST: "+firstname+"\"\n\n");
            y += 30;
            TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+secondname+"\"\n\n");
        }
        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"CODE: "+invoiceHeaderDetails.get(0).getCustomerCode()+"\"\n");

      *//*  if (!invoiceHeaderDetails.get(0).getAddress().isEmpty()){
            y += 30;
            String a = invoiceHeaderDetails.get(0).getAddress();
            String[] b = a.split(",");
            TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"ADD: "+b[0]+","+b[1]+"\"\n");
            if (b.length>=3){
                if (b[2]!=null){
                    y += 20;
                    TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,7,7,\"" + b[2] + "\"\n");
                }
            }
        }*//*

        if (!invoiceHeaderDetails.get(0).getAddress().isEmpty()) {
            y += LINE_SPACING;
            if (invoiceHeaderDetails.get(0).getAddress().length() <= 45) {
                TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,8,8,\"" + "ADDR: " + invoiceHeaderDetails.get(0).getAddress() + "\"\n\n");
            } else {
                String address1 = invoiceHeaderDetails.get(0).getAddress().substring(0, 42);
                String address2 = invoiceHeaderDetails.get(0).getAddress().substring(42);

                TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,8,8,\"" + "ADDR: " + address1 + "\"\n\n");
                y += 30;
                TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,8,8,\"" + address2 + "\"\n\n");
            }
        }

        if (invoiceHeaderDetails.get(0).getPaymentTerm()!=null && !invoiceHeaderDetails.get(0).getPaymentTerm().isEmpty() && !invoiceHeaderDetails.get(0).getPaymentTerm().equals("null")){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 0," + y + ",\"Bold.TTF\",0,8,8,\" Payment Terms : " + invoiceHeaderDetails.get(0).getPaymentTerm() + "\"\n\n");
        }
        TscDll.sendcommand("TEXT 400,"+y+",\"Poppins.TTF\",0,8,8,\""+"User: "+username+"\"\n");


       *//* int in=0;
        for (String s : b) {
            System.out.println(s);
            if (in==0){
                TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"ADD: "+b[0]+","+b[1]+"\"\n");
            }else {
                y += 20;
                if (b[2]!=null){
                    TscDll.sendcommand("TEXT 0," + y + ",\"Poppins.TTF\",0,7,7,\"" + b[2] + "\"\n");
                }
            }
            in++;
        }*//*
        // if (invoiceHeaderDetails.get(0).getAddress1().isEmpty() && !invoiceHeaderDetails.get(0).getAddress1().equals("null")){
        // TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"ADD: "+invoiceHeaderDetails.get(0).getAddress()+"\"\n");
        //   }

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"SN"+"\"\n");
        TscDll.sendcommand("TEXT 50,"+y+",\"Poppins.TTF\",0,8,8,\""+"PRODUCT"+"\"\n");
        TscDll.sendcommand("TEXT 400,"+y+",\"Poppins.TTF\",0,8,8,\""+"PRICE"+"\"\n");
        TscDll.sendcommand("TEXT 500,"+y+",\"Poppins.TTF\",0,8,8,\""+"TOTAL"+"\"\n");

        y += TITLE_LINE_SPACING;
        TscDll.sendcommand("TEXT 70,"+y+",\"Poppins.TTF\",0,8,8,\""+"ISS"+"\"\n");
        TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,8,8,\""+"RTN"+"\"\n");
        TscDll.sendcommand("TEXT 300,"+y+",\"Poppins.TTF\",0,8,8,\""+"NET"+"\"\n");
        TscDll.sendcommand("TEXT 400,"+y+",\"Poppins.TTF\",0,8,8,\""+"($)"+"\"\n");
        TscDll.sendcommand("TEXT 500,"+y+",\"Poppins.TTF\",0,8,8,\""+"($)"+"\"\n");

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        int index=1;
        for (InvoicePrintPreviewModel.InvoiceList invoice : invoiceList) {
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 5,"+y+",\"Bold.TTF\",0,8,8,\""+index+"\"\n");
            TscDll.sendcommand("TEXT 50,"+y+",\"Poppins.TTF\",0,8,8,\""+invoice.getDescription()+"\"\n");
            y += 30;
            TscDll.sendcommand("TEXT 80,"+y+",\"Bold.TTF\",0,8,8,\""+(int)Double.parseDouble(invoice.getNetQty())+"\"\n");
            TscDll.sendcommand("TEXT 210,"+y+",\"Bold.TTF\",0,8,8,\""+(int)Double.parseDouble(invoice.getReturnQty())+"\"\n");
            TscDll.sendcommand("TEXT 310,"+y+",\"Bold.TTF\",0,8,8,\""+(int)Double.parseDouble(invoice.getNetQuantity())+"\"\n");
            TscDll.sendcommand("TEXT 410,"+y+",\"Bold.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoice.getPricevalue()))+"\"\n");
            TscDll.sendcommand("TEXT 510,"+y+",\"Bold.TTF\",0,8,8,\""+twoDecimalPoint(Double.parseDouble(invoice.getTotal()))+"\"\n");
            index++;
            height=height+10;
        }
        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        if (!invoiceHeaderDetails.get(0).getTaxType().equals("I")){
            y += 20;
            TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"SUB TOTAL:$"+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSubTotal()))+"\"\n");

            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"GST("+invoiceHeaderDetails.get(0).getTaxType()+":"+ (int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % ):$"+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTax()))+"\"\n");

            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"GRAND TOTAL:$"+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTotal()))+"\"\n");

        }else {
            y += 30;
            TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"GRAND TOTAL:$"+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getNetTotal()))+"\"\n");
            y += 30;
            TscDll.sendcommand("TEXT 220,"+y+",\"Poppins.TTF\",0,8,8,\""+"(GST Included "+(int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % )\"\n");
        }
        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");



     *//*   y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\"TOTAL OUTSTANDING: "+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getOutStandingAmount()))+"\"\n");

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");*//*

        if(invoiceHeaderDetails.get(0).getSalesReturnList().size()>0){
            if (invoiceHeaderDetails.get(0).getTaxType().equalsIgnoreCase("I")){
                y += 20;
                TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"CN No : "+invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getSalesReturnNumber()+"\"\n");
                y += 30;
                TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"CN NET TOTAL:$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRNetTotal()))+"\"\n");

                y += 30;
                TscDll.sendcommand("TEXT 220,"+y+",\"Poppins.TTF\",0,8,8,\""+"(GST Included "+(int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % )\"\n");
            } else {
                y += 20;
                TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,9,9,\""+"CN No : "+invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getSalesReturnNumber()+"\"\n");
                y += 30;
                TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"CN SUB TOTAL:$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRSubTotal()))+"\"\n");

                y += LINE_SPACING;
                TscDll.sendcommand("TEXT 200,"+y+",\"Poppins.TTF\",0,9,9,\""+"GST("+invoiceHeaderDetails.get(0).getTaxType()+":"+ (int)Double.parseDouble(invoiceHeaderDetails.get(0).getTaxValue())+" % ):$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Poppins.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRTaxTotal()))+"\"\n");

                y += LINE_SPACING;
                TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"CN NET TOTAL:$"+"\"\n");
                TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getSalesReturnList().get(0).getsRNetTotal()))+"\"\n");
            }

            height=height+10;
            y += LINE_SPACING;
            TscDll.sendcommand("BAR 0,"+y+",800,2\n");

            y += 20;
            TscDll.sendcommand("TEXT 200,"+y+",\"Bold.TTF\",0,9,9,\""+"BALANCE AMT:"+"\"\n");
            TscDll.sendcommand("TEXT 480,"+y+",\"Bold.TTF\",0,9,9,\""+twoDecimalPoint(Double.parseDouble(invoiceHeaderDetails.get(0).getOverAllTotal()))+"\"\n");

            y += LINE_SPACING;
            TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        }



        y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"Customer Signature & Company Stamp"+"\"\n");


        y += 150;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"-------------------------------------------------"+"\"\n");

     *//*   y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"THIS IS COMPUTER GENERATED INVOICE"+"\"\n");
        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"NO SIGNATURE REQUIRED"+"\"\n");*//*

        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,7,7,\""+"RECEIVED ABOVE GOODS IN GOOD ORDER AND CONDITION"+"\"\n");

        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"-------------------------------------------------"+"\"\n");

        height=height+20;
        TscDll.sendcommand("SIZE 80 mm, "+height+" mm\n");
        TscDll.printlabel(1, copy);
        TscDll.closeport(5000);
    }*/

    private String  printCompanyDetails(int y, StringBuilder cpclConfigLabel) {
        try {
            if (company_name.length() > 38) {
                cpclConfigLabel.append((company_name.length() > 38) ? company_name.substring(0, 37) : company_name+"\n");
                String companyname = company_name.substring(37);
                cpclConfigLabel.append((companyname.length() > 37) ? companyname.substring(0, 36) : companyname+"\n");
            }else {
                cpclConfigLabel.append(company_name+"\n");
            }

            if (company_address1!=null && !company_address1.isEmpty()){
                cpclConfigLabel.append(company_address1+"\n");
            }

            if(company_address2!=null && !company_address2.isEmpty()){
                cpclConfigLabel.append(company_address2+"\n");
            }

            if (company_address3!=null && !company_address3.isEmpty()){
                cpclConfigLabel.append( company_address3+"\n");
            }

            if (company_phone!=null && !company_phone.isEmpty()){
                cpclConfigLabel.append( "TEL : "+company_phone+"\n");
            }

            if (company_gst!=null && !company_gst.isEmpty()){
                cpclConfigLabel.append("CO REG NO : "+company_gst+"\n");
            }

        }catch (Exception exception){}
        return cpclConfigLabel.toString();
    }


    public void printTransferDetail(int copy , ArrayList<TransferDetailModel> transferDetailModels,
                                    ArrayList<TransferDetailModel.TransferDetails> transferDetailsList) throws IOException {

        TscDll.openport(this.macAddress);

        int y=0;
        int height=100;

        TscDll.sendcommand("GAP 0 mm, 0 mm\r\n");//Gap media

        TscDll.clearbuffer();
        TscDll.sendcommand("SPEED 4\r\n");
        TscDll.sendcommand("DENSITY 12\r\n");
        TscDll.sendcommand("CODEPAGE UTF-8\r\n");
        TscDll.sendcommand("SET TEAR ON\r\n");
        TscDll.sendcommand("SET COUNTER @1 1\r\n");
        TscDll.sendcommand("@1 = \"0001\"\r\n");

        StringBuilder temp = new StringBuilder();
        if (company_name.length() > 38) {
            temp.append((company_name.length() > 38) ? company_name.substring(0, 37) : company_name);
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Bold.TTF\",0,10,10,2,\""+company_name+"\"\n\n");
            String companyname = company_name.substring(37);
            temp.append((companyname.length() > 37) ? companyname.substring(0, 36) : companyname);
            TscDll.sendcommand("TEXT 260,"+y+",\"Bold.TTF\",0,10,10,2,\""+temp.toString()+"\"\n\n");
        }else {
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Bold.TTF\",0,10,10,2,\""+company_name+"\"\n\n");
        }

        if (company_address1!=null && !company_address1.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address1+"\"\n");
        }

        if(company_address2!=null && !company_address2.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address2+"\"\n\n");
        }

        if (company_address3!=null && !company_address3.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\""+company_address3+"\"\n");
        }

        if (company_phone!=null && !company_phone.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\" TEL : "+company_phone+"\"\n");
        }

        if (company_gst!=null && !company_gst.isEmpty()){
            y += LINE_SPACING;
            TscDll.sendcommand("TEXT 260,"+y+",\"Poppins.TTF\",0,9,9,2,\" CO REG NO : "+company_gst+"\"\n");
        }

        y += LINE_SPACING+10;
        TscDll.sendcommand("TEXT 200," + y + ",\"Bold.TTF\",0,10,10,\"" + "LOADING SHEET" + "\"\n");
        y += 10;
        TscDll.sendcommand("TEXT 210," + y + ",\"Bold.TTF\",0,9,9,\"" + "(Load Stock)" + "\"\n");

        y += LINE_SPACING+10;
        // Define the Box
        TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,9,9,\""+"No: "+transferDetailModels.get(0).getNumber()+"\"\n");
        TscDll.sendcommand("TEXT 400,"+y+",\"Bold.TTF\",0,7,7,\""+transferDetailModels.get(0).getDate()+"\"\n");
        y += 30;
        TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,8,8,\""+"From Location: "+transferDetailModels.get(0).getFromLocation()+"\"\n");
        TscDll.sendcommand("TEXT 330,"+y+",\"Poppins.TTF\",0,8,8,\""+"To Location: "+transferDetailModels.get(0).getToLocation()+"\"\n");

        y += 30;
        TscDll.sendcommand("TEXT 350,"+y+",\"Poppins.TTF\",0,8,8,\""+"Status: "+transferDetailModels.get(0).getStatus()+"\"\n");

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        y += 20;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"NO"+"\"\n");
        TscDll.sendcommand("TEXT 60,"+y+",\"Poppins.TTF\",0,8,8,\""+"DESCRIPTION"+"\"\n");
        TscDll.sendcommand("TEXT 350,"+y+",\"Poppins.TTF\",0,8,8,\""+"QTY"+"\"\n");
        TscDll.sendcommand("TEXT 500,"+y+",\"Poppins.TTF\",0,8,8,\""+"UOM"+"\"\n");

        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        int index=1;
        for (TransferDetailModel.TransferDetails transferDetails : transferDetailsList) {

            y += 30;
            TscDll.sendcommand("TEXT 5,"+y+",\"Poppins.TTF\",0,8,8,\""+index+"\"\n");

            TscDll.sendcommand("TEXT 60,"+y+",\"Poppins.TTF\",0,8,8,\""+transferDetails.getDescription()+"\"\n");
            y += 20;
            TscDll.sendcommand("TEXT 360,"+y+",\"Bold.TTF\",0,8,8,\""+(transferDetails.getQty())+"\"\n");
            TscDll.sendcommand("TEXT 500,"+y+",\"Poppins.TTF\",0,8,8,\""+(transferDetails.getUomCode())+"\"\n");

            index++;
            height=height+10;
        }
        y += LINE_SPACING;
        TscDll.sendcommand("BAR 0,"+y+",800,2\n");

        y += LINE_SPACING+50;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"------------"+"\"\n");
        TscDll.sendcommand("TEXT 280,"+y+",\"Poppins.TTF\",0,8,8,\""+"------------"+"\"\n");
        TscDll.sendcommand("TEXT 430,"+y+",\"Poppins.TTF\",0,8,8,\""+"------------"+"\"\n");

        y += LINE_SPACING;
        TscDll.sendcommand("TEXT 0,"+y+",\"Poppins.TTF\",0,8,8,\""+"Approved By"+"\"\n");
        TscDll.sendcommand("TEXT 280,"+y+",\"Poppins.TTF\",0,8,8,\""+"Issue By"+"\"\n");
        TscDll.sendcommand("TEXT 430,"+y+",\"Poppins.TTF\",0,8,8,\""+"Requested By"+"\"\n");

//        y += +10;
//        TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,8,8,\""++"\"\n");
//        TscDll.sendcommand("TEXT 280,"+y+",\"Bold.TTF\",0,8,8,\""++"\"\n");
//        TscDll.sendcommand("TEXT 430,"+y+",\"Bold.TTF\",0,8,8,\""++"\"\n");
//
//        y += +10;
//        TscDll.sendcommand("TEXT 0,"+y+",\"Bold.TTF\",0,8,8,\""+dat+"\"\n");
//        TscDll.sendcommand("TEXT 350,"+y+",\"Bold.TTF\",0,8,8,\""+dat+"\"\n");

        height=height+20;
        TscDll.sendcommand("SIZE 80 mm, "+height+" mm\n");
        TscDll.printlabel(1, copy);
        TscDll.closeport(5000);

    }


    public void printInvoiceC(int copy, ArrayList<InvoicePrintPreviewModel> invoiceHeaderDetails, ArrayList<InvoicePrintPreviewModel.InvoiceList> invoiceList, String isDoPrint){
        LabelCommand tsc = new LabelCommand();
        tsc.addCls();

        tsc.addText(200, 0, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_2,
                "Tax Cash Sales");
        tsc.addLimitFeed(100);
        tsc.addBar(0,40,1000,2);
        tsc.addText(0, 70, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "NO PRODUCT                 PRICE   TOTAL");
        tsc.addText(0, 100, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "    ISS    RTN    NET      ($)   ($)");
        /*  tsc.addText(0, 110, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "__________________________________________________");*/
        tsc.addBar(0,130,1000,2);
        tsc.addLimitFeed(50);
        int index=1;
        for (InvoicePrintPreviewModel.InvoiceList invoice : invoiceList) {
            tsc.addText(0, 150, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                    ""+index+""+invoice.getDescription()+"           ");
            tsc.addText(0, 180, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                    ""+invoice.getNetQty()+""+invoice.getReturnQty()+""+invoice.getNetQuantity()+""+invoice.getPrice()+""+invoice.getTotal()+"");

            index++;
        }


   /*     tsc.addText(0, 150, LabelCommand.FONTTYPE.FONT_1, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2,
                "price：" + "99.00");
        tsc.addText(0, 140, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "quantity：" + "99");
        tsc.addText(0, 190, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "date：" + "2020-02-02");*/

        tsc.addPrint(1, 1); // 打印标签
        tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
        /* 发送数据 */
        Vector<Byte> data = tsc.getCommand();
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            Log.i("TAG", "sendLabel: \n" +
                    "printer is empty");
            return;
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(data);
    }



    private String horizontalLine(int y, int thickness) {
        return new StringBuilder(CMD_LINE).append(SPACE).append(LEFT_MARGIN)
                .append(SPACE).append(y).append(SPACE).append(RIGHT_MARGIN)
                .append(SPACE).append(y).append(SPACE).append(thickness)
                .append(LINE_SEPARATOR).toString();
    }



    private void sendLabel() {
        LabelCommand tsc = new LabelCommand();
       // tsc.addSize(100, 100); // 设置标签尺寸，按照实际尺寸设置
       /* tsc.addGap(0); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL);// 设置打印方向
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON);//开启带Response的打印，用于连续打印
        tsc.addReference(0, 0);// 设置原点坐标
        tsc.addTear(EscCommand.ENABLE.ON); // 撕纸模式开启
        tsc.addCls();// 清除打印缓冲区*/
        // 绘制简体中文

        tsc.addCls();
        //    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.aadhi_logo);
        //    tsc.addBitmap(100, 0, LabelCommand.BITMAP_MODE.XOR, 400, b);
        //tsc.addLimitFeed(50);
        int y=0;
        tsc.addBar(0,40,1000,2);
        tsc.addText(0, 70, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "NO PRODUCT                 PRICE   TOTAL");
        tsc.addText(0, 100, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "    ISS    RTN    NET      ($)   ($)");
        /*  tsc.addText(0, 110, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "__________________________________________________");*/
        tsc.addBar(0,130,1000,2);
        tsc.addLimitFeed(50);
        tsc.addText(0, 150, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "1   French Loaf - Pkt(6)                          ");
        tsc.addText(0, 180, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "     10     2     8        2.00    16.00");

        tsc.addText(0, 210, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "2   French Loaf - Pkt(6)                          ");
        tsc.addText(0, 240, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "     10     2     8        2.00    16.00");

        tsc.addText(0, 270, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "3   French Loaf - Pkt(6)                          ");
        tsc.addText(0, 300, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "     10     2     8        2.00    16.00");




   /*     tsc.addText(0, 150, LabelCommand.FONTTYPE.FONT_1, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2,
                "price：" + "99.00");
        tsc.addText(0, 140, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "quantity：" + "99");
        tsc.addText(0, 190, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "date：" + "2020-02-02");*/

        tsc.addPrint(1, 1); // 打印标签
        tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
        /* 发送数据 */
        Vector<Byte> data = tsc.getCommand();
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null) {
            Log.i("TAG", "sendLabel: \n" +
                    "printer is empty");
            return;
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(data);
    }


    public void connectPrinter(){
        closePort();
       // String macAddress = BluetoothListActivity.EXTRA_DEVICE_ADDRESS;
        Log.w("MacAddressPrint:",macAddress);
        new DeviceConnFactoryManager.Build()
                .setId(id)
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                .setMacAddress(macAddress)
                .build();
        Log.i("TAG", "onActivityResult: \n" + "connect bluetooth" + id);
        threadPool = ThreadPool.getInstantiation();
        threadPool.addTask(new Runnable() {
            @Override
            public void run() {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
            }
        });
    }

    private void closePort() {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null && DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort != null) {
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader.cancel();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort.closePort();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort = null;
        }
    }



    private void unRegisterReceiver(){
        context.unregisterReceiver(receiver);
    }

    /**
     * Broadcast receiver for the Bluetooth Status
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DeviceConnFactoryManager.ACTION_CONN_STATE.equals(action)) {
                int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                switch (state) {
                    case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                        if (id == deviceId)
                            Log.w("Not ConnectedDevices","");
                        break;
                    case DeviceConnFactoryManager.CONN_STATE_CONNECTING:

                        break;
                    case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                        Log.w("Connected","Success");
                        Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
                        break;
                    case CONN_STATE_FAILED:
                        Toast.makeText(context, "\n" +
                                "Connection failed! Try again or restart the printer", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else if (ACTION_USB_DEVICE_DETACHED.equals(action)) {
                mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN:
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                        Toast.makeText(context, "successfully disconnected", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PRINTER_COMMAND_ERROR:
                    Toast.makeText(context, "Please select the correct printer order", Toast.LENGTH_SHORT).show();
                    break;
                case CONN_PRINTER:
                    Toast.makeText(context, "\n" + "Please connect the printer first", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private String text(int x, int y, String text) {
        return new StringBuilder(CMD_TEXT).append(SPACE).append(FONT)
                .append(SPACE).append(FONT_SIZE).append(SPACE).append(x)
                .append(SPACE).append(y).append(SPACE).append(text)
                .append(LINE_SEPARATOR).toString();
    }
}
