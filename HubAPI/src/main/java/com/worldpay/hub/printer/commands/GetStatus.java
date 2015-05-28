package com.worldpay.hub.printer.commands;

public class GetStatus extends PrinterCommand
{
    public static final byte STATUS_PRINTER = 0x01;
    public static final byte STATUS_CASH_DRAWER = 0x02;
    public GetStatus(byte status)
    {
        mData = new byte[] { 0x1D, 0x72, status };
    }
}
