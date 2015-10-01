package com.worldpay.hub.mpop;

import android.content.Context;

import com.worldpay.hub.Hub;
import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.PrinterFactory;
import com.worldpay.hub.PrinterQueue;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.worldpay.hub.mpop.printer.commands.FeedPaper;
import com.worldpay.hub.PrinterCommand;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Implementation of the Hub interface against the Star Micronics mpop device.
 */
public class mpopHub implements Hub
{
    protected StarIOPort mPort;

    protected final static int MAX_DATASIZE  = 65536; //
    protected final static int CHUNKSIZE = 10240; //Max number of bytes to send

    public mpopHub(String btname, Context context)
    {
        try
        {
            mPort = StarIOPort.getPort(btname, "", 1000, context);
        } catch (StarIOPortException e)
        {
            //$TODO - report error when we can't create a StarIOPort
            e.printStackTrace();
        }
    }

    @Override
    public void print(PrinterQueue queue) throws HubResponseException, IOException
    {
        PrinterCommand nextCommand = queue.getNextCommand();
        ByteBuffer bb = ByteBuffer.allocate(MAX_DATASIZE);  //Maximum data size.  This is different
        //to the max frame size, and slicing is
        //done later
        while(nextCommand != null)
        {
            //Queue the command
            if((bb.position() + nextCommand.getData().length) > MAX_DATASIZE)
            {
                //We've not got space for this yet
                //we have to execute
                flushToPrinter(bb);
            }

            bb.put(nextCommand.getData());
            if(nextCommand.getDelay() > 0)
            {
                //we have to execute
                flushToPrinter(bb);
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

        //Check to see if we've got anything buffered, but not printed
        if(bb.position() > 0)
        {
            flushToPrinter(bb);
        }
    }

    //Write some of the printer data to the printer
    private void flushToPrinter(ByteBuffer bb) throws HubResponseException, IOException
    {
        byte[] buffer = new byte[bb.position()];
        System.arraycopy(bb.array(), 0, buffer, 0, bb.position());

        //Split the data into chunks suitable for the hub
        //Theorectically, this is 16Kb, but we've found a 2Kb limit in practice
        int offset = 0;
        while(offset < buffer.length)
        {
            int len = CHUNKSIZE;
            if((buffer.length - offset) < CHUNKSIZE)
                len = buffer.length - offset;
            byte[] sendBuffer = new byte[len];

            //Copy this chunk to the send buffer
            System.arraycopy(buffer, offset, sendBuffer, 0, len);

            //Send this buffer
            sendCommands(buffer, mPort);
            offset += len;
        }
        bb.clear();
    }

    @Override
    public void printerFeed(int lines) throws HubResponseException, IOException
    {
        PrinterQueue q = new PrinterQueue();
        q.add(new FeedPaper(lines));
        print(q);
    }

    @Override
    public void openCashDrawer() throws HubResponseException, IOException
    {
        //PrinterQueue q = new PrinterQueue();
        //q.add(new OpenDrawer());
        sendCommands(new byte[] { 0x07 }, mPort);
    }

    @Override
    public boolean isCashDrawerOpen() throws HubResponseException, IOException
    {
        return false;
    }

    @Override
    public boolean hasPaper() throws HubResponseException, IOException
    {
        return false;
    }

    @Override
    public PrinterFactory getPrinter()
    {
        return new mPOPFactory();
    }

    /* The following code is cut and paste from the Star example code */
    public enum Result {
        Success,
        ErrorUnknown,
        ErrorOpenPort,
        ErrorBeginCheckedBlock,
        ErrorEndCheckedBlock,
        ErrorWritePort,
        ErrorReadPort,
    }

    public static Result sendCommands(byte[] commands, StarIOPort port)
    {
        Result result = Result.ErrorUnknown;

        try {
            if (port == null) {
                result = Result.ErrorOpenPort;
                return result;
            }

//          // When using an USB interface, you may need to send the following data.
//          byte[] dummy = {0x00};
//          port.writePort(dummy, 0, dummy.length);

            StarPrinterStatus status;

            result = Result.ErrorBeginCheckedBlock;

            status = port.beginCheckedBlock();

            if (status.offline) {
                throw new StarIOPortException("A printer is offline");
            }

            result = Result.ErrorWritePort;

            port.writePort(commands, 0, commands.length);

            result = Result.ErrorEndCheckedBlock;

            port.setEndCheckedBlockTimeoutMillis(30000);     // 30000mS!!!

            status = port.endCheckedBlock();

            if (status.coverOpen) {
                throw new StarIOPortException("Printer cover is open");
            }
            else if (status.receiptPaperEmpty) {
                throw new StarIOPortException("Receipt paper is empty");
            }
            else if (status.offline) {
                throw new StarIOPortException("Printer is offline");
            }

            result = Result.Success;
        }
        catch (StarIOPortException e) {
        }

        return result;
    }
}
