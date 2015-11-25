package com.worldpay.hubtest;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Printer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.MePOS.MePOSHub;
import com.worldpay.hub.MePOS.printer.commands.DownloadBitmap;
import com.worldpay.hub.MePOS.printer.commands.Justify;
import com.worldpay.hub.MePOS.printer.commands.PrintBitmap;
import com.worldpay.hub.MePOS.printer.commands.Raster;
import com.worldpay.hub.MePOS.printer.commands.Underline;
import com.worldpay.hub.PrinterCommandNotImplementedException;
import com.worldpay.hub.PrinterFactory;
import com.worldpay.hub.PrinterQueue;
import com.worldpay.hub.mpop.mpopHub;
import com.worldpay.hub.usbserial.driver.UsbSerialDriver;
import com.worldpay.hub.usbserial.driver.UsbSerialPort;
import com.worldpay.hub.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by Micro2 on 29/09/2015.
 */
public class DeviceFragment extends Fragment
{
    private final String TAG = DeviceFragment.class.getSimpleName();
    PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private UsbSerialPort mPort;
    private Spinner mUSBDevices;
    private Spinner mBTDevices;

    private Button mPrinterTest;
    private Button mHammer;
    private Button mDrawer;
    private Button mProbe;
    private Button mPrinterFeed;
    private Button mRasterPrint;

    private RadioButton mSelectUSB;
    private RadioButton mSelectBT;

    private TextView mDeviceType;
    private TextView mDeviceName;

    private UsbDevice mSelectedUSB;

    private static final String ACTION_USB_PERMISSION = "com.example.graphicsprinter.USB_PERMISSION";

    private static final int MESSAGE_REFRESH = 101;
    private static final int REQUEST_ENABLE_BT = 1;


    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_REFRESH:
                    refreshDeviceList();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private List<UsbSerialPort> mUSBEntries = new ArrayList<UsbSerialPort>();
    private List<String> mUSBNames = new ArrayList<String>();
    private ArrayAdapter<String> mUSBAdapter;
    private ArrayAdapter<String> mBTAdapter;

    private HubProvider mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_deviceselection, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HubProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HubProvider");
        }
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mSelectUSB = (RadioButton) getActivity().findViewById(R.id.usbDeviceSelector);
        mSelectBT = (RadioButton) getActivity().findViewById(R.id.btDeviceSelector);

        mDeviceName = (TextView) getActivity().findViewById(R.id.deviceAddress);
        mDeviceType = (TextView) getActivity().findViewById(R.id.deviceType);

        mSelectBT.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View view)
                 {
                     //Unset USB
                     mSelectUSB.setChecked(false);
                     mUSBDevices.setEnabled(false);
                     mBTDevices.setEnabled(true);
                     mDeviceType.setText("BT Devices");
                     mDeviceName.setText("");
                 }
             }
        );

        mSelectUSB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Unset BT
                mSelectBT.setChecked(false);
                mUSBDevices.setEnabled(true);
                mBTDevices.setEnabled(false);
                mDeviceType.setText("MePOS");
                mDeviceName.setText("");

                refreshDeviceList();
            }
        });

        mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

        mUSBAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mUSBNames);
        mBTAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);

        mBTDevices = (Spinner) getActivity().findViewById(R.id.btDevices);
        mBTDevices.setAdapter(mBTAdapter);
        mBTDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3)
            {
                try
                {
                    mListener.setHub(new mpopHub("BT:" + mBTAdapter.getItem(arg2), getActivity()));
                    mDeviceType.setText("BT Devices");
                    mDeviceName.setText(mBTAdapter.getItem(arg2));
                } catch (HubResponseException e)
                {
                    mDeviceType.setText("Error");
                    mDeviceName.setText("");
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub
            }
        });

        mUSBDevices = (Spinner) getActivity().findViewById(R.id.usbDevices);
        mUSBDevices.setAdapter(mUSBAdapter);
        mUSBDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3)
            {
                // TODO Auto-generated method stub
                mPort = mUSBEntries.get(arg2);
                if (mPort != null)
                {
                    mPermissionIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, new Intent(
                            ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    getActivity().registerReceiver(mUsbReceiver, filter);

                    mUsbManager.requestPermission(mPort.getDriver().getDevice(), mPermissionIntent);
                }

                //Change the hub
                mListener.setHub(new MePOSHub(mPort, mUsbManager));
                mDeviceType.setText("MePOS");
                mDeviceName.setText(mPort.getDriver().getDevice().getDeviceName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub
            }
        });


        mDrawer = (Button) getActivity().findViewById(R.id.openCashDrawer);
        mDrawer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                try
                {
                    mListener.getHub().openCashDrawer();
                } catch (HubResponseException e)
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
                try
                {
                    mListener.getHub().printerFeed(5);
                } catch (HubResponseException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        mRasterPrint = (Button) getActivity().findViewById(R.id.raster);
        mRasterPrint.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mListener.getHub() == null)
                    return;

                PrinterFactory factory = mListener.getHub().getPrinter();

                byte[] picture = new byte[]{};
                try
                {
                    InputStream is = getResources().openRawResource(R.raw.bmphack);
                    picture = new byte[is.available()];
                    int read = is.read(picture);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                //MePOS
                PrinterQueue imageQueue = new PrinterQueue();

                //Decode the bitmap
                imageQueue.rasterPrint(picture);

                try
                {
                    mListener.getHub().print(imageQueue);
                } catch (HubResponseException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        mPrinterTest = (Button) getActivity().findViewById(R.id.printTest);
        mPrinterTest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if(mListener.getHub() == null)
                    return;

                Log.d("Sammy", String.format("Starting print test"));
                PrinterFactory factory = mListener.getHub().getPrinter();


                byte[] picture = new byte[]{};
                try
                {
                    InputStream is = getResources().openRawResource(R.raw.bmphack);
                    picture = new byte[is.available()];
                    int read = is.read(picture);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                //MePOS
                PrinterQueue imageQueue = new PrinterQueue();
                imageQueue.add(new DownloadBitmap(picture))
                        .add(new PrintBitmap());

                /* mPOP
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.headshot);
                try
                {
                    imageQueue.add(factory.PrintBitmap(bitmap, 384, PrinterFactory.ROTATION_0))
                              .add(factory.CutPaper(PrinterFactory.CUT_FULL));

                    Bitmap bmp;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable = true;
                    bmp = BitmapFactory.decodeByteArray(picture, 0, picture.length, options);

                    imageQueue.add(factory.PrintBitmap(bmp, 384, PrinterFactory.ROTATION_0))
                               .add(factory.CutPaper(PrinterFactory.CUT_FULL));
                }
                catch (PrinterCommandNotImplementedException e)
                {
                    e.printStackTrace();
                }*/

                PrinterQueue queue = new PrinterQueue();
                try
                {
                    queue.add(factory.ClearPrinter())
                            .add(factory.SetCodePage(32))
                            .add(factory.PrintText("Hello Sam\n"))
                            .add(factory.Underline(Underline.UNDERLINE_SINGLE))
                            .add(factory.PrintText("This is underlined\n"))
                            .add(factory.Underline(Underline.UNDERLINE_NONE))
                            .add(factory.Beep())
                            .add(factory.DoubleWidthCharacters())
                            .add(factory.PrintText("This is wide\n"))
                            .add(factory.SingleWidthCharacters())
                            .add(factory.ClearPrinter())
                            .add(factory.ReversePrintMode(PrinterFactory.REVERSE_ON))
                            .add(factory.PrintText(" This is reversed \n"))
                            .add(factory.ReversePrintMode(PrinterFactory.REVERSE_OFF))
                            .add(factory.Bold(PrinterFactory.BOLD_ON))
                            .add(factory.PrintText("This is bold\n"))
                            .add(factory.Bold(PrinterFactory.BOLD_OFF))
                            .add(factory.Justify(Justify.JUSTIFY_RIGHT))
                            .add(factory.PrintText("Justify right\n"))
                            .add(factory.Justify(Justify.JUSTIFY_CENTRE))
                            .add(factory.PrintText("Justify centre\n"))
                            .add(factory.Justify(Justify.JUSTIFY_LEFT))
                            .add(factory.PrintText("Justify left\n"))
                            .add(factory.PrintText("................................\n"))
                            .add(factory.PrintText("1 x Bionic Arm             \u00A31.99\n"))
                            .add(factory.FeedPaper(10))
                            .add(factory.CutPaper(PrinterFactory.CUT_FULL))
                            .add(factory.FeedPaper(2));
                } catch (PrinterCommandNotImplementedException e)
                {
                    Log.d("Sammy", "Command not implemented!");
                    e.printStackTrace();
                }

                try
                {
                    Log.d("Sammy", String.format("Printing now"));
                    mListener.getHub().print(imageQueue);
                   // mListener.getHub().print(queue);
                } catch (HubResponseException e)
                {
                    Log.d("Sammy", e.getMessage());
                    e.printStackTrace();
                } catch (IOException e)
                {
                    Log.d("Sammy", e.getMessage());
                    e.printStackTrace();
                }
                Log.d("Sammy", String.format("Print finished"));
            }

        });

        mHammer = (Button) getActivity().findViewById(R.id.printHammer);
        mHammer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (mListener.getHub() == null)
                    return;

                PrinterFactory factory = mListener.getHub().getPrinter();

                byte[] header = new byte[]{};
                byte[] footer = new byte[]{};
                try
                {
                    InputStream is = getResources().openRawResource(R.raw.tr1);
                    header = new byte[is.available()];
                    int read = is.read(header);

                    is = getResources().openRawResource(R.raw.tr2);
                    footer = new byte[is.available()];
                    read = is.read(footer);

                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                //MePOS
                PrinterQueue queue = new PrinterQueue();
                try
                {
                    queue.add(new DownloadBitmap(header))
                            .add(new PrintBitmap())
                            .add(factory.SetCodePage(32))
                            .add(factory.PrintText("Hello Sam\n"))
                            .add(factory.Underline(Underline.UNDERLINE_SINGLE))
                            .add(factory.PrintText("This is underlined\n"))
                            .add(factory.Underline(Underline.UNDERLINE_NONE))
                        /*.add(factory.Beep())*/
                            .add(factory.DoubleWidthCharacters())
                            .add(factory.PrintText("This is wide\n"))
                            .add(factory.SingleWidthCharacters())
                            .add(factory.ClearPrinter())
                            .add(factory.ReversePrintMode(PrinterFactory.REVERSE_ON))
                            .add(factory.PrintText(" This is reversed \n"))
                            .add(factory.ReversePrintMode(PrinterFactory.REVERSE_OFF))
                            .add(factory.Bold(PrinterFactory.BOLD_ON))
                            .add(factory.PrintText("This is bold\n"))
                            .add(factory.Bold(PrinterFactory.BOLD_OFF))
                            .add(factory.Justify(Justify.JUSTIFY_RIGHT))
                            .add(factory.PrintText("Justify right\n"))
                            .add(factory.Justify(Justify.JUSTIFY_CENTRE))
                            .add(factory.PrintText("Justify centre\n"))
                            .add(factory.Justify(Justify.JUSTIFY_LEFT))
                            .add(factory.PrintText("Justify left\n"))
                            .add(factory.PrintText("................................\n"))
                            .add(factory.PrintText("1 x Bionic Arm             \u00A31.99\n"))
                            .add(new DownloadBitmap(footer))
                            .add(new PrintBitmap())
                       /* .add(factory.OpenDrawer())*/
                            .add(factory.FeedPaper(10))
                            .add(factory.CutPaper(PrinterFactory.CUT_FULL))
                            .add(factory.FeedPaper(2));
                } catch (PrinterCommandNotImplementedException e)
                {
                    e.printStackTrace();
                }

                try
                {
                    mListener.getHub().print(queue);
                } catch (HubResponseException e)
                {
                    Log.d("Sammy", e.getMessage());
                    e.printStackTrace();
                } catch (IOException e)
                {
                    Log.d("Sammy", e.getMessage());
                    e.printStackTrace();
                }
                Log.d("Sammy", String.format("Print finished"));
            }

        });

        mProbe = (Button) getActivity().findViewById(R.id.probe);
        mProbe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                    if(mDeviceType.getText().toString().equals("MePOS"))
                        mListener.probe((MePOSHub) mListener.getHub());

            }
        });

        mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, 2000);
    }

    private void refreshDeviceList()
    {
        new AsyncTask<Void, Void, List<UsbSerialPort>>()
        {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params)
            {
                Log.d(TAG, "Refreshing device list ...");
                mUSBNames.clear();

                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

                final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
                for (final UsbSerialDriver driver : drivers)
                {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    Log.d(TAG, String.format("+ %s: %s port%s",
                            driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
                    result.addAll(ports);

                    Log.d("Sammy", String.format("Adding device %04X:%04X", driver.getDevice().getVendorId(), driver.getDevice().getProductId()));
                    mUSBNames.add(driver.getDevice().getDeviceName());
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result)
            {
                mUSBEntries.clear();
                mUSBEntries.addAll(result);
                mUSBAdapter.notifyDataSetChanged();
                Log.d(TAG, "Done refreshing USB, " + mUSBEntries.size() + " entries found.");
            }

        }.execute((Void) null);

        //Get Bluetooth devices
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBluetoothAdapter != null)
    {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mBTAdapter.add(device.getName()/* + "\n" + device.getAddress()*/);
            }
        }
        Log.d(TAG, "Done refreshing USB, " + mUSBEntries.size() + " entries found.");
    }
}


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if (device != null)
                        {
                            // call method to set up device communication
                        }
                    } else
                    {
                        Log.d("ERROR", "permission denied for device " + device);
                    }
                }
            }
        };
    };
}