package com.worldpay.hub.MePOS.printer.commands;

import android.graphics.Bitmap;
import android.util.Log;

import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;

/**
 * Prints the last downloaded bitmap
 */
public class RasterBitmap extends PrinterCommand
{
    public static final int MODE_LOW = 0;
    public static final int MODE_HIGH = 33;

    public RasterBitmap() throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }

    public RasterBitmap(byte[] bitmap)
    {
        init(bitmap, MODE_LOW);
    }

    public RasterBitmap(byte[] bitmap, int mode)
    {
        init(bitmap, mode);
    }

    public RasterBitmap(Bitmap bitmap, int width, int rotation, int mode) throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }

    private void init(byte[] bitmap, int mode)
    {
        int len = bitmap.length;
        if(mode == MODE_HIGH)
        {
            //We need to understand that this is x3 the width
            len = len / 3;
        }

        mData = new byte[bitmap.length + 6];
        mData[0] = 0x1B;
        mData[1] = 0x2A;
        mData[2] = (byte) mode;
        mData[3] = (byte)(len & 0xFF);
        mData[4] = (byte)((len >> 8) & 0xFF);

        //Logger.d("MePOS", String.format("len %d nl %d nh %d mode %d", len, mData[3], mData[4], mode));

        System.arraycopy(bitmap, 0, mData, 5, bitmap.length);
        mData[mData.length - 1] = 0x0A;

        mDelay = 100;
    }
}
