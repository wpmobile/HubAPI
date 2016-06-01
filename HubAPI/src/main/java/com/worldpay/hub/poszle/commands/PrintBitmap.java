package com.worldpay.hub.poszle.commands;
import android.graphics.Bitmap;

import com.starmicronics.starioextension.commandbuilder.Bitmap.SCBBitmapConverter;
import com.starmicronics.starioextension.commandbuilder.ISCBBuilder;
import com.starmicronics.starioextension.commandbuilder.SCBFactory;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;
import com.worldpay.hub.PrinterFactory;

import java.util.List;

/**
 * Unlike the MePOS implementation, this simply prints the image without having to download it first
 * This also expects an Android ARGB_8888 bitmap
 */
public class PrintBitmap extends PrinterCommand
{

    public PrintBitmap()
            throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }

    public PrintBitmap(int index)
            throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }

    public PrintBitmap(Bitmap bitmap, int width, int rotation)
    {
        SCBBitmapConverter.Rotation rot;
        switch(rotation)
        {
            case PrinterFactory.ROTATION_90:
                rot = SCBBitmapConverter.Rotation.Right90;
                break;
            case PrinterFactory.ROTATION_180:
                rot = SCBBitmapConverter.Rotation.Rotate180;
                break;
            case PrinterFactory.ROTATION_270:
                rot = SCBBitmapConverter.Rotation.Left90;
                break;
            default:
                rot = SCBBitmapConverter.Rotation.Normal;
                break;
        }

        ISCBBuilder builder = SCBFactory.createBuilder(SCBFactory.Emulation.Star);
        builder.appendBitmap(bitmap, false, width, rot);

        List<byte[]> listBuf = builder.getBuffer();

        int totalSize = 0;

        totalSize = listBuf.get(0).length;
        mData = new byte[totalSize];
        System.arraycopy(listBuf.get(0), 0, mData, 0, listBuf.get(0).length);

        /*
        for(byte[] buf:listBuf) {
            totalSize += buf.length;
        }

        mData = new byte[totalSize];
        int offset = 0;
        //Copy the data from the buider to the mData object.
        for(byte[] buf:listBuf) {
            System.arraycopy(buf, 0, mData, offset, buf.length);
            offset += buf.length;
        }*/
    }

}
