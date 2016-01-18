package com.worldpay.hub;

public class PrinterCommand
{
    protected static final String TAG = "MePOS";
    protected byte[] mData;
    protected int mDelay;

    public PrinterCommand()
    {
        mDelay = 0;
    }

    public byte[] getData()
    {
        return mData;
    }

    /***
     * Some printer commands require a delay to allow for processing on the printer
     * @return the number of milliseconds to wait
     */
    public int getDelay()
    {
        return mDelay;
    }
}
