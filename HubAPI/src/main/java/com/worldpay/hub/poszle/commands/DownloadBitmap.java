package com.worldpay.hub.poszle.commands;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;

public class DownloadBitmap extends PrinterCommand
{
    public DownloadBitmap(byte[] bitmap) throws PrinterCommandNotImplementedException
    {
        init(bitmap, 0);
    }

    public DownloadBitmap(byte[] bitmap, int index) throws PrinterCommandNotImplementedException
    {
        init(bitmap, index);
    }

    private void init(byte[] bitmap, int index) throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }
}
