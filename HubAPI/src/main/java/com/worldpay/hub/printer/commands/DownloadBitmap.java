package com.worldpay.hub.printer.commands;

public class DownloadBitmap extends PrinterCommand
{
    public DownloadBitmap(byte[] bitmap)
    {
        mData = new byte[bitmap.length + 1];
        mData[0] = 0x1B;
        System.arraycopy(bitmap, 0, mData, 1, bitmap.length);

        //When we transmit this command, we need to add a 100ms delay
        //to allow for the image to be copied into flash memory
        mDelay = 100;
    }
}
