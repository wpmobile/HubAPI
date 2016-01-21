package com.worldpay.hub.MePOS.printer.commands;

import com.worldpay.hub.HubResponseException;

import java.io.IOException;

/**
 * This class can flush data to the printer
 */
public interface PrinterFlusher
{
    public static final boolean FLOW_CONTROL_ON = true;
    public static final boolean FLOW_CONTROL_OFF = false;
    public void flush(byte[] data) throws HubResponseException, IOException;
    public int flush(byte[] data, int seqNo) throws HubResponseException, IOException;
    public void setFlowControl(boolean flowControl);
}
