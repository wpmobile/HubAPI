package com.worldpay.hubtest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.worldpay.hub.MePOS;
import com.worldpay.hub.MePOSResponseException;
import com.worldpay.hub.commands.SetPort;
import com.worldpay.hub.commands.SystemInformation;
import com.worldpay.hub.printer.PrinterQueue;
import com.worldpay.hub.printer.commands.Bold;
import com.worldpay.hub.printer.commands.ClearPrinter;
import com.worldpay.hub.printer.commands.CutPaper;
import com.worldpay.hub.printer.commands.DoubleWidthCharacters;
import com.worldpay.hub.printer.commands.DownloadBitmap;
import com.worldpay.hub.printer.commands.FeedPaper;
import com.worldpay.hub.printer.commands.Italic;
import com.worldpay.hub.printer.commands.Justify;
import com.worldpay.hub.printer.commands.PrintBitmap;
import com.worldpay.hub.printer.commands.PrintText;
import com.worldpay.hub.printer.commands.ReversePrintMode;
import com.worldpay.hub.printer.commands.SelectMemory;
import com.worldpay.hub.printer.commands.SetCodePage;
import com.worldpay.hub.printer.commands.Underline;
import com.worldpay.hub.usbserial.driver.UsbSerialDriver;
import com.worldpay.hub.usbserial.driver.UsbSerialPort;
import com.worldpay.hub.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
    private Button mDrawer;
    private Button mPrinterFeed;
    private Button mLights;
    private ToggleButton mHighSpeed;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int CAPTURE_IMAGE_THUMBNAIL_ACTIVITY_REQUEST_CODE = 1888;


    private static final int MESSAGE_REFRESH = 101;

    private UsbDevice mSelectedDevice;

    private static final String ACTION_USB_PERMISSION = "com.example.graphicsprinter.USB_PERMISSION";

    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
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

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mHighSpeed = (ToggleButton)getActivity().findViewById(R.id.highSpeed);

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

        mDrawer = (Button) getActivity().findViewById(R.id.openDrawer);
        mDrawer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                MePOS hub = new MePOS(mPort, mUsbManager);
                try
                {
                    hub.openCashDrawer();
                } catch (MePOSResponseException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        mPrinterFeed = (Button) getActivity().findViewById(R.id.printerFeed);
        mPrinterFeed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                MePOS hub = new MePOS(mPort, mUsbManager);
                try
                {
                    hub.printerFeed(5);
                } catch (MePOSResponseException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
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
                byte[] picture = new byte[]{};
                try
                {
                    InputStream is = getResources().openRawResource(R.raw.tr1);
                    picture = new byte[is.available()];
                    int read = is.read(picture);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                PrinterQueue imageQueue = new PrinterQueue();
                imageQueue.add(new SelectMemory(SelectMemory.MEMORY_RAM))
                          .add(new DownloadBitmap(picture, 1));

                PrinterQueue queue = new PrinterQueue();
                queue.add(new ClearPrinter())
                        .add(new SetCodePage(SetCodePage.CODE_PAGE_1252))
                        .add(new PrintText("Hello Sam\n"))
                        .add(new Underline(Underline.UNDERLINE_SINGLE))
                        .add(new PrintText("This is underlined\n"))
                        .add(new Underline(Underline.UNDERLINE_NONE))
                        /*.add(new Beep())*/
                        .add(new DoubleWidthCharacters())
                        .add(new PrintText("This is wide\n"))
                        .add(new ClearPrinter())
                        .add(new Italic(Italic.ITALIC_ON))
                        .add(new PrintText("This is italic\n"))
                        .add(new Italic(Italic.ITALIC_OFF))
                        .add(new ReversePrintMode(ReversePrintMode.REVERSE_ON))
                        .add(new PrintText("This is reversed\n"))
                        .add(new ReversePrintMode(ReversePrintMode.REVERSE_OFF))
                        .add(new Bold(Bold.BOLD_ON))
                        .add(new PrintText("This is bold\n"))
                        .add(new Bold(Bold.BOLD_OFF))
                        .add(new Justify(Justify.JUSTIFY_RIGHT))
                        .add(new PrintText("Justify right\n"))
                        .add(new Justify(Justify.JUSTIFY_CENTRE))
                        .add(new PrintText("Justify centre\n"))
                        .add(new Justify(Justify.JUSTIFY_LEFT))
                        .add(new PrintText("Justify left\n"))
                        .add(new PrintText("............................................\n"))
                        .add(new PrintText("1 x Bionic Arm                         £1.99\n"))
                       /* .add(new OpenDrawer())*/
                        .add(new PrintBitmap(1))
                        .add(new FeedPaper(10))
                        .add(new CutPaper(CutPaper.CUT_FULL))
                        .add(new FeedPaper(2));

                MePOS hub = new MePOS(mPort, mUsbManager);

                try
                {
                    if(mHighSpeed.isChecked())
                        hub.setPrinterBaudRate(SetPort.BAUD_115200);
                    else
                        hub.setPrinterBaudRate(SetPort.BAUD_57600);
                } catch (MePOSResponseException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                try
                {
                    hub.print(imageQueue);
                    hub.print(queue);
                } catch (MePOSResponseException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        });

        mLights = (Button) getActivity().findViewById(R.id.lightShow);
        mLights.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                MePOS hub = new MePOS(mPort, mUsbManager);
                try
                {
                    //Light show!
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT,     MePOS.STATE_OFF);

                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.COLOR_RED, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.COLOR_RED, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.COLOR_RED, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.COLOR_RED, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.COLOR_RED, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.COLOR_RED, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.COLOR_GREEN, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.COLOR_GREEN, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.COLOR_GREEN, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.COLOR_GREEN, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.COLOR_GREEN, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.COLOR_GREEN, MePOS.STATE_OFF);

                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.COLOR_RED, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.COLOR_RED, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.COLOR_RED, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.COLOR_GREEN, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.COLOR_GREEN, MePOS.STATE_ON);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.COLOR_GREEN, MePOS.STATE_ON);

                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.STATE_OFF);

                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT, MePOS.COLOR_RED);
                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT, MePOS.COLOR_GREEN);
                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT, MePOS.COLOR_BLUE);
                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT, MePOS.STATE_OFF);

                    int colours[] = new int[] { MePOS.COLOR_RED, MePOS.COLOR_GREEN, MePOS.STATE_OFF };
                    int lights[] = new int[] { MePOS.DIAGNOSTIC_LIGHT_1, MePOS.DIAGNOSTIC_LIGHT_2, MePOS.DIAGNOSTIC_LIGHT_3, MePOS.COSMETIC_LIGHT};
                    for(int i = 0; i < 100; i++)
                    {
                        hub.setDiagnosticLight(lights[i % 4], colours[i % 3]);
                    }

                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_1, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_2, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.DIAGNOSTIC_LIGHT_3, MePOS.STATE_OFF);
                    hub.setDiagnosticLight(MePOS.COSMETIC_LIGHT,     MePOS.STATE_OFF);
                }
                catch (MePOSResponseException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
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

                    Log.d("Sammy", String.format("Adding device %04X:%04X", driver.getDevice().getVendorId(), driver.getDevice().getProductId()));
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
        if (mPort == null && mEntries.isEmpty())
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
        } catch (IOException e)
        {
            e.printStackTrace();
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
        } catch (IOException e)
        {
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
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        //OK, this one is a little different.  This call returns a SystemInformation object
        //with many properites

        SystemInformation systemInformation = null;

        try
        {
            systemInformation = hub.getSystemInformation();

        } catch (MePOSResponseException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }


        if (systemInformation != null)
        {
            TextView tv = (TextView) getActivity().findViewById(R.id.systemType);
            tv.setText(systemInformation.getType());

            tv = (TextView) getActivity().findViewById(R.id.sysInfoSerialNo);
            tv.setText(String.format("%08X", systemInformation.getSerialNumber()));

            tv = (TextView) getActivity().findViewById(R.id.deviceVariant);
            tv.setText(formatBytes(systemInformation.getVariant()));

/*            tv = (TextView) getActivity().findViewById(R.id.supplierCode);
            tv.setText(formatBytes(systemInformation.getSupplier()));

            tv = (TextView) getActivity().findViewById(R.id.customerCode);
            tv.setText(formatBytes(systemInformation.getCustomerCode()));*/

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
        }

        try
        {
            TextView tv = (TextView) getActivity().findViewById(R.id.paperStatus);
            if(hub.hasPaper())
            {
                tv.setText("Printer has paper");
            }
            else
            {
                tv.setText("Printer has run out of paper");
            }
        } catch (MePOSResponseException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            TextView tv = (TextView) getActivity().findViewById(R.id.cashDrawer);
            if(hub.isCashDrawerOpen())
            {
                tv.setText("Cash drawer is open");
            }
            else
            {
                tv.setText("Cash drawer is closed");
            }
        } catch (MePOSResponseException e)
        {
            e.printStackTrace();
        } catch (IOException e)
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
