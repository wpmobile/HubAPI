package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.PrinterQueue;

import java.io.IOException;

/**
 * PrinterQueueProcessors convert a PrinterQueue into
 */
public interface PrinterQueueProcessor
{
    public void print(PrinterQueue queue) throws IOException, HubResponseException;
    public void setPrinterFlusher(PrinterFlusher flusher) throws IllegalArgumentException;
}
