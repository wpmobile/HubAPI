package com.worldpay.hub.MePOS.printer.commands;

import android.graphics.Bitmap;
import android.util.Log;

import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;

/**
 * Immediately print one bitmap row.
 */
public class Raster extends PrinterCommand
{
    public Raster(byte[] rowData)
    {
        init(rowData);
    }

    private void init(byte[] data)
    {
/*        mData = new byte[data.length + 4];
        mData[0] = 0x1B; // Raster data follows
        mData[1] = 0x4B; //
        mData[2] = (byte)(data.length & 0xFF);
        mData[3] = (byte)((data.length >> 8) & 0xFF);

        System.arraycopy(data, 0, mData, 4, data.length);*/

        mData = new byte[data.length + 1];
        mData[0] = 0x11; // Raster data follows

        System.arraycopy(data, 0, mData, 1, data.length);
    }
}
