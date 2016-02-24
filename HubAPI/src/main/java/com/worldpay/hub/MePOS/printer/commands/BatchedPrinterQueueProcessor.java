package com.worldpay.hub.MePOS.printer.commands;

import android.util.Log;

import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.Logger;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterQueue;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This implementation of PrinterQueueProcessor will batch together commands to send fewer, larger
 * messages
 */
public class BatchedPrinterQueueProcessor implements PrinterQueueProcessor
{
    protected final static String TAG = "MePOS";
    protected final static int MAX_DATASIZE  = 65536;
    private PrinterFlusher mFlusher;
    private int mMaxDataSize;

    public BatchedPrinterQueueProcessor()
    {
        init(MAX_DATASIZE);
    }

    public BatchedPrinterQueueProcessor(int maxDataSize)
    {
        init(maxDataSize);
    }

    private void init(int maxDataSize)
    {
        mMaxDataSize = maxDataSize;
    }

    @Override
    public void print(PrinterQueue queue) throws IOException
    {
        if(mFlusher == null) return;
        mFlusher.setFlowControl(PrinterFlusher.FLOW_CONTROL_ON);

        int seqNo = 1;
        long lastReset = System.currentTimeMillis();
        PrinterCommand nextCommand = queue.getNextCommand();
        ByteBuffer bb = ByteBuffer.allocate(mMaxDataSize);  //Maximum data size.  This is different
        //to the max frame size, and slicing is
        //done later
        while(nextCommand != null)
        {
            //Queue the command
            if((bb.position() + nextCommand.getData().length) > mMaxDataSize)
            {
                //We've not got space for this yet
                //we have to execute
                try
                {
                    Logger.d(TAG, "Buffer is full, flushing");
                    seqNo = mFlusher.flush(bb.array(), seqNo);
                    bb.clear();
                }
                catch(HubResponseException e)
                {
                    //Flush to printer has failed, abort
                    return;
                }
            }

            //Logger.d(TAG, String.format("Serialising command %s", nextCommand.getClass().getCanonicalName()));
            //Logger.d(TAG, String.format("Position offset : %02X", bb.position()));
            bb.put(nextCommand.getData());
            if(nextCommand.getDelay() > 0)
            {
                try
                {
                    Logger.d(TAG, "Need to add a delay, flushing now");
                    seqNo = mFlusher.flush(bb.array(), seqNo);
                }
                catch(HubResponseException e)
                {
                    //Flush to printer has failed, abort
                    return;
                }

                try
                {
                    Thread.sleep(nextCommand.getDelay());
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            //Move on to the next one
            nextCommand = queue.getNextCommand();
        }

        //Reset the sequence number if we've probably exceeded the time box
        if(System.currentTimeMillis() > lastReset + 800)
        {
            //reset the sequence numbers
            seqNo = 1;
            lastReset = System.currentTimeMillis();
            Logger.d(TAG, "Resetting flow control sequence number");

            //Add a 200 ms delay to ensure that flow control has reset on the hub.  I'd like
            //to say that this is probably the hackiest thing I've ever done, but we both know
            //that it wouldn't be true...
            try
            {
                Thread.sleep(200);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        //Check to see if we've got anything buffered, but not printed
        if(bb.position() > 0)
        {
            try
            {
                Logger.d(TAG, "Flushing final data to printer");
                seqNo = mFlusher.flush(bb.array(), seqNo);
            }
            catch(HubResponseException e)
            {
                //Flush to printer has failed, abort
                return;
            }
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
