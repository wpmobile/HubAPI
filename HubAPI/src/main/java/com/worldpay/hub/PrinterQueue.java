package com.worldpay.hub;

import com.worldpay.hub.MePOS.printer.commands.PrintText;
import com.worldpay.hub.MePOS.printer.commands.Raster;
import com.worldpay.hub.MePOS.printer.commands.RasterBitmap;
import com.worldpay.hub.mpop.printer.commands.CutPaper;
import com.worldpay.hub.mpop.printer.commands.FeedPaper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;

/***
 * Manages a queue of printer commands
 */
public class PrinterQueue
{
    protected ArrayDeque<PrinterCommand> mQueue;
    protected int mSize;

    public PrinterQueue()
    {
        mQueue = new ArrayDeque<PrinterCommand>();
        mSize = 0;
    }

    public PrinterQueue add(PrinterCommand command)
    {
        mQueue.addLast(command);
        mSize += command.getData().length;

        return this;
    }

    //Returns the number of bytes required serialise the current queue
    public int size()
    {
        return mSize;
    }

    public PrinterCommand getNextCommand()
    {
        return mQueue.pollFirst();
    }
}
