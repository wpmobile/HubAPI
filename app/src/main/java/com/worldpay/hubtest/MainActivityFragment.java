package com.worldpay.hubtest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.worldpay.hub.MePOSResponseException;
import com.worldpay.hub.commands.SystemInformation;
import com.worldpay.hub.printer.PrinterQueue;
import com.worldpay.hub.printer.commands.Beep;
import com.worldpay.hub.printer.commands.Bold;
import com.worldpay.hub.printer.commands.ClearPrinter;
import com.worldpay.hub.printer.commands.CutPaper;
import com.worldpay.hub.printer.commands.DoubleWidthCharacters;
import com.worldpay.hub.printer.commands.FeedPaper;
import com.worldpay.hub.printer.commands.Flush;
import com.worldpay.hub.printer.commands.Italic;
import com.worldpay.hub.printer.commands.Justify;
import com.worldpay.hub.printer.commands.OpenDrawer;
import com.worldpay.hub.printer.commands.PrintText;
import com.worldpay.hub.printer.commands.ReversePrintMode;
import com.worldpay.hub.printer.commands.Underline;
import com.worldpay.hub.usbserial.driver.UsbSerialDriver;
import com.worldpay.hub.usbserial.driver.UsbSerialPort;
import com.worldpay.hub.usbserial.driver.UsbSerialProber;
import com.worldpay.hub.MePOS;
import com.worldpay.hub.util.HexDump;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
{

    private final String TAG = MainActivity.class.getSimpleName();
    PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private UsbSerialPort mPort;
    private Spinner mSpinner;
    private Button mProbe;
    private Button mPrintTest;

    private static final int MESSAGE_REFRESH = 101;

    private UsbDevice mSelectedDevice;

    private static final String ACTION_USB_PERMISSION = "com.example.graphicsprinter.USB_PERMISSION";

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    refreshDeviceList();
                    //mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private List<UsbSerialPort> mEntries = new ArrayList<UsbSerialPort>();
    private List<String> mNames = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;

    public MainActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mNames);

        mSpinner = (Spinner) getActivity().findViewById(R.id.spinner);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3)
            {
                // TODO Auto-generated method stub
                mPort = mEntries.get(arg2);
                if (mPort != null)
                {
                    mPermissionIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, new Intent(
                            ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    getActivity().registerReceiver(mUsbReceiver, filter);

                    mUsbManager.requestPermission(mPort.getDriver().getDevice(), mPermissionIntent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub
            }
        });

        mProbe = (Button) getActivity().findViewById(R.id.probe);
        mProbe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                probe();
            }
        });

        mPrintTest = (Button) getActivity().findViewById(R.id.printTest);
        mPrintTest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                PrinterQueue queue = new PrinterQueue();
                queue.add(new ClearPrinter())
                        .add(new PrintText("Hello Sam"))
                        .add(new Flush())
                        .add(new Underline(Underline.UNDERLINE_SINGLE))
                        .add(new PrintText("This is underlined"))
                        .add(new Flush())
                        .add(new Underline(Underline.UNDERLINE_NONE))
                        /*.add(new Beep())*/
                        .add(new DoubleWidthCharacters())
                        .add(new PrintText("This is wide"))
                        .add(new Flush())
                        .add(new ClearPrinter())
                        .add(new Italic(Italic.ITALIC_ON))
                        .add(new PrintText("This is italic"))
                        .add(new Flush())
                        .add(new Italic(Italic.ITALIC_OFF))
                        .add(new ReversePrintMode(ReversePrintMode.REVERSE_ON))
                        .add(new PrintText("This is reversed"))
                        .add(new Flush())
                        .add(new ReversePrintMode(ReversePrintMode.REVERSE_OFF))
                        .add(new Bold(Bold.BOLD_ON))
                        .add(new PrintText("This is bold"))
                        .add(new Flush())
                        .add(new Bold(Bold.BOLD_ON))
                        .add(new Justify(Justify.JUSTIFY_RIGHT))
                        .add(new PrintText("Justify right\n"))
                        .add(new Justify(Justify.JUSTIFY_RIGHT))
                        .add(new Justify(Justify.JUSTIFY_CENTRE))
                        .add(new PrintText("Justify centre\n"))
                        .add(new Justify(Justify.JUSTIFY_CENTRE))
                        .add(new Justify(Justify.JUSTIFY_LEFT))
                        .add(new PrintText("Justify left\n"))
                        .add(new Justify(Justify.JUSTIFY_LEFT))
                       /* .add(new OpenDrawer())*/
                        .add(new Flush())
                        .add(new FeedPaper(10))
                        .add(new CutPaper(CutPaper.CUT_FULL))
                        .add(new FeedPaper(10));

                MePOS hub = new MePOS(mPort, mUsbManager);

                try
                {
                    hub.print(queue);
                } catch (MePOSResponseException e)
                {
                    e.printStackTrace();
                }
            }

        });


        mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, 2000);
    }

    private void refreshDeviceList() {

        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params)
            {
                Log.d(TAG, "Refreshing device list ...");

                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

                final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    Log.d(TAG, String.format("+ %s: %s port%s",
                            driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
                    result.addAll(ports);

                    mNames.add(driver.getDevice().getDeviceName());
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {
                mEntries.clear();
                mEntries.addAll(result);
                mAdapter.notifyDataSetChanged();
                Log.d(TAG, "Done refreshing, " + mEntries.size() + " entries found.");
            }

        }.execute((Void) null);
    }

    protected void probe()
    {
        if(mPort == null && mEntries.isEmpty())
            return;

       /* if(mPort == null)
        {
            mPort = mEntries.get(0);
            mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
            mPermissionIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                    0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            getActivity().registerReceiver(mUsbReceiver, filter);

            mUsbManager.requestPermission(mPort.getDriver().getDevice(), mPermissionIntent);
        }*/
        // Instance a new MePOS object with the USBPort and the USBManager
        MePOS hub = new MePOS(mPort, mUsbManager);

        //Here we go!

        //Let's get the date/time
        try
        {
            Log.d(TAG, "Getting date time");
            Date hubDate = hub.getDateTime();
            if(hubDate != null)
            {
                TextView tv = (TextView) getActivity().findViewById(R.id.currentTime);
                DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
                tv.setText(df.format(hubDate));
            }
        }
        catch(MePOSResponseException e)
        {
            // Prolly OK to just ignore it
        }

        //Cool beans, now lets try the serial number!
        try
        {
            Log.d(TAG, "Getting serial number");
            int serialNumber = hub.getSerialNumber();
            TextView tv = (TextView) getActivity().findViewById(R.id.serialNumber);
            tv.setText(String.format("%8X", serialNumber));

        } catch (MePOSResponseException e)
        {
            // Prolly OK to just ignore it
            e.printStackTrace();
        }

        //Get version for the hat-trick
        try
        {
            TextView tv = (TextView) getActivity().findViewById(R.id.version);
            Log.d(TAG, "Getting version");
            tv.setText(hub.getVersion());

        } catch (MePOSResponseException e)
        {
            // Prolly OK to just ignore it
            e.printStackTrace();
        }

        //OK, this one is a little different.  This call returns a SystemInformation object
        //with many properites


        try
        {
            SystemInformation systemInformation = hub.getSystemInformation();

            TextView tv = (TextView) getActivity().findViewById(R.id.systemType);
            tv.setText(systemInformation.getType());

            tv = (TextView) getActivity().findViewById(R.id.sysInfoSerialNo);
            tv.setText(String.format("%08X", systemInformation.getSerialNumber()));

            tv = (TextView) getActivity().findViewById(R.id.deviceVariant);
            tv.setText(formatBytes(systemInformation.getVariant()));

            tv = (TextView) getActivity().findViewById(R.id.supplierCode);
            tv.setText(formatBytes(systemInformation.getSupplier()));

            tv = (TextView) getActivity().findViewById(R.id.customerCode);
            tv.setText(formatBytes(systemInformation.getCustomerCode()));

            tv = (TextView) getActivity().findViewById(R.id.fwRevNo);
            tv.setText(systemInformation.getFWRevision());

            tv = (TextView) getActivity().findViewById(R.id.docNo);
            tv.setText(systemInformation.getFWDocNumber());

            tv = (TextView) getActivity().findViewById(R.id.articleCode);
            tv.setText(systemInformation.getFWSArticleCode());

            DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

            tv = (TextView) getActivity().findViewById(R.id.buildDate);
            tv.setText(df.format(systemInformation.getBuildDate()));

            tv = (TextView) getActivity().findViewById(R.id.lastDate);
            tv.setText(df.format(systemInformation.getLastDate()));

        } catch (MePOSResponseException e)
        {
            e.printStackTrace();
        }

    }

    protected String formatBytes(byte[] data)
    {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < data.length; i++)
        {
            if(i > 0)
                sb.append(':');
            sb.append(String.format("%02X", data[i]));
        }
        return sb.toString();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                        }
                    } else {
                        Log.d("ERROR", "permission denied for device " + device);
                    }
                }
            }
        }
    };
}
