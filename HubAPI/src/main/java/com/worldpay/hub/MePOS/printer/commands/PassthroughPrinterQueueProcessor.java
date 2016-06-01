package com.worldpay.hub.MePOS.printer.commands;

import android.util.Log;

import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.Logger;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterQueue;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This implementation of PrinterQueueProcessor will send a printer command for each element in the
 * queue, resulting in more, smaller messages
 */
public class PassthroughPrinterQueueProcessor implements PrinterQueueProcessor
{
    protected final static String TAG = "MePOS";
    private PrinterFlusher mFlusher;

    @Override
    public void print(PrinterQueue queue) throws IOException, HubResponseException
    {
        if(mFlusher == null) return;
        mFlusher.setFlowControl(PrinterFlusher.FLOW_CONTROL_OFF);

        PrinterCommand nextCommand = queue.getNextCommand();
        while(nextCommand != null)
        {
            mFlusher.flush(nextCommand.getData());
            if(nextCommand.getDelay() > 0)
            {
                try
                {
                    //Logger.d(TAG, "Need to add a delay");
                    Thread.sleep(nextCommand.getDelay());
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            nextCommand = queue.getNextCommand();
        }
    }

    @Override
    public void setPrinterFlusher(PrinterFlusher flusher) throws IllegalArgumentException
    {
        if(flusher == null)
            throw new IllegalArgumentException();
        mFlusher = flusher;
    }
}
