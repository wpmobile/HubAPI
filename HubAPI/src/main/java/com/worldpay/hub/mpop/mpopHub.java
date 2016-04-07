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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Implementation of the Hub interface against the Star Micronics mpop device.
 */
public class mpopHub implements Hub
{
    protected StarIOPort mPort;

    protected final static int MAX_DATASIZE  = 65536; //
    protected final static int CHUNKSIZE = 10240; //Max number of bytes to send

    public mpopHub(String btname, Context context) throws HubResponseException
    {
        try
        {
            mPort = StarIOPort.getPort(btname, "", 1000, context);
        } catch (StarIOPortException e)
        {
            throw new HubResponseException("Cannot connect to mPOP", e);
        }
    }

    @Override
    public void print(PrinterQueue queue) throws HubResponseException, IOException
    {
        PrinterCommand nextCommand = queue.getNextCommand();
        ByteBuffer bb = ByteBuffer.allocate(queue.size());
        while(nextCommand != null)
        {
            bb.put(nextCommand.getData());
            nextCommand = queue.getNextCommand();
        }

        flushToPrinter(bb);
    }

    //Write some of the printer data to the printer
    private void flushToPrinter(ByteBuffer bb) throws HubResponseException, IOException
    {
        //Send this buffer
        sendCommands(bb.array(), mPort);
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
        sendCommands(new byte[]{0x07}, mPort);
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
    public void setMode(int mode) throws HubResponseException, IOException
    {
        //TODO;
    }

    @Override
    public void setDiagnosticLight(int light, int colour, int state) throws HubResponseException, IOException
    {
        //TODO;
    }

    @Override
    public void updateFirmware(InputStream firmware, int length) throws HubResponseException, IOException
    {
        //TODO:
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
