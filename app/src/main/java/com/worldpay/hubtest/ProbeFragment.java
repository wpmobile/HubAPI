package com.worldpay.hubtest;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.MePOS.MePOSHub;
import com.worldpay.hub.MePOS.commands.SystemInformation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Micro2 on 29/09/2015.
 */
public class ProbeFragment extends Fragment implements HubProvider.Prober
{
    private final String TAG = ProbeFragment.class.getSimpleName();
    private HubProvider mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_probe, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HubProvider) activity;
            mListener.setProber(this);

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HubProvider");
        }
    }

    @Override
    public void probe(MePOSHub hub)
    {
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
        catch(HubResponseException e)
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

        } catch (HubResponseException e)
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

        } catch (HubResponseException e)
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

        } catch (HubResponseException e)
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
        } catch (HubResponseException e)
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
        } catch (HubResponseException e)
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
}
