package com.worldpay.hub;

import com.worldpay.hub.MePOS.printer.commands.Raster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;

/***
 * Manages a queue of printer commands
 */
public class PrinterQueue
{
    protected ArrayDeque<PrinterCommand> mQueue;
    protected int mSize;

    public PrinterQueue()
    {
        mQueue = new ArrayDeque<PrinterCommand>();
        mSize = 0;
    }

    public PrinterQueue add(PrinterCommand command)
    {
        mQueue.addLast(command);
        mSize += command.getData().length;

        return this;
    }

    ///
    //Adds all the necessary raster print commands to printer queue
    //MePOS units only
    public PrinterQueue rasterPrint(byte[] picture)
    {
        ByteBuffer imageBuffer = ByteBuffer.wrap(picture);
        imageBuffer.order(ByteOrder.LITTLE_ENDIAN); //BMP are little endian
        //Check the header
        boolean valid = true;
        valid = valid && imageBuffer.get() == 0x42;
        valid = valid && imageBuffer.get() == 0x4D; //BM leader

        int length = imageBuffer.getInt();
        imageBuffer.getInt(); //Discard
        int offset = imageBuffer.getInt();
        int headerSize = imageBuffer.getInt();
        int pixelWidth = imageBuffer.getInt();
        int pixelHeight = imageBuffer.getInt();
        imageBuffer.getShort(); //Discard
        valid = valid && imageBuffer.getShort() == 0x01; //Colour depth in bits per pixel
        valid = valid && imageBuffer.getInt() == 0x00; //Check that there is no compression

        if(!valid)
            throw new IllegalArgumentException("Not a valid bitmap");

        //Calculate the number of bytes in each row
        int rowLength = ((pixelWidth + 31) / 32) * 4; //https://en.wikipedia.org/wiki/BMP_file_format#Pixel_storage

        //Read the bitmap row by row
        //The rows are in reverse order, so start at the bottom and work backwards

        //Calculate the offset of the last row in the bitmap
        int rowOffset = offset + (rowLength * (pixelHeight - 1));
        for(int i = 0; i < pixelHeight; i++)
        {
            byte[] row = new byte[rowLength];
            //We have to apply a bitwise NOT to each byte
            for(int j = 0; j < rowLength; j++)
            {
                row[j] = (byte)~(picture[rowOffset + j]);
            }
            this.add(new Raster(row));
            rowOffset -= rowLength;
        }

        return this;
    }

    //Returns the number of bytes required serialise the current queue
    public int size()
    {
        return mSize;
    }

    public PrinterCommand getNextCommand()
    {
        return mQueue.pollFirst();
    }
}
