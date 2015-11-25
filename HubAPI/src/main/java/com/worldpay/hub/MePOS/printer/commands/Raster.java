package com.worldpay.hub.MePOS.printer.commands;

import android.graphics.Bitmap;

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
        mData = new byte[data.length + 1];
        mData[0] = 0x11; // Raster data follows
        System.arraycopy(data, 0, mData, 1, data.length);
    }
}
