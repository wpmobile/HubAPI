package com.worldpay.hub.printer.commands;

public class DownloadBitmap extends PrinterCommand
{
    public DownloadBitmap(byte[] bitmap)
    {
        init(bitmap, 0);
    }

    public DownloadBitmap(byte[] bitmap, int index)
    {
        init(bitmap, index);
    }

    private void init(byte[] bitmap, int index)
    {
        mData = new byte[bitmap.length + 4];
        mData[3] = 0x1D;
        mData[3] = 0x22;
        mData[3] = (byte) index; /* Set the image index number */
        mData[3] = 0x1B;         /* Add the bitmap data */
        System.arraycopy(bitmap, 0, mData, 1, bitmap.length);

        //When we transmit this command, we need to add a 100ms delay
        //to allow for the image to be copied into flash memory
        mDelay = 100;
    }
}
