package com.worldpay.hub.printer;
import com.worldpay.hub.printer.commands.PrinterCommand;

import java.util.ArrayDeque;
import java.util.LinkedList;

/***
 * Manages a queue of printer commands
 */
public class PrinterQueue
{
    protected ArrayDeque<PrinterCommand> mQueue;

    public PrinterQueue()
    {
        mQueue = new ArrayDeque<PrinterCommand>();
    }

    public PrinterQueue add(PrinterCommand command)
    {
        mQueue.addLast(command);
        return this;
    }

    public PrinterCommand getNextCommand()
    {
        return mQueue.pollFirst();
    }
}
