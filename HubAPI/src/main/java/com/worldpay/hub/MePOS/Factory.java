package com.worldpay.hub.MePOS;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Printer;

import com.worldpay.hub.MePOS.printer.commands.RasterBitmap;
import com.worldpay.hub.MePOS.printer.commands.SetCharacterSet;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;
import com.worldpay.hub.PrinterFactory;
import com.worldpay.hub.MePOS.printer.commands.Beep;
import com.worldpay.hub.MePOS.printer.commands.Bold;
import com.worldpay.hub.MePOS.printer.commands.ClearPrinter;
import com.worldpay.hub.MePOS.printer.commands.CutPaper;
import com.worldpay.hub.MePOS.printer.commands.DoubleWidthCharacters;
import com.worldpay.hub.MePOS.printer.commands.DownloadBitmap;
import com.worldpay.hub.MePOS.printer.commands.EraseMemory;
import com.worldpay.hub.MePOS.printer.commands.FeedPaper;
import com.worldpay.hub.MePOS.printer.commands.Flush;
import com.worldpay.hub.MePOS.printer.commands.GetStatus;
import com.worldpay.hub.MePOS.printer.commands.HorizontalTab;
import com.worldpay.hub.MePOS.printer.commands.InitialisePrinter;
import com.worldpay.hub.MePOS.printer.commands.Italic;
import com.worldpay.hub.MePOS.printer.commands.Justify;
import com.worldpay.hub.MePOS.printer.commands.LeftMargin;
import com.worldpay.hub.MePOS.printer.commands.OpenDrawer;
import com.worldpay.hub.MePOS.printer.commands.PrintBitmap;
import com.worldpay.hub.MePOS.printer.commands.PrintTestPage;
import com.worldpay.hub.MePOS.printer.commands.PrintText;
import com.worldpay.hub.MePOS.printer.commands.ReversePrintMode;
import com.worldpay.hub.MePOS.printer.commands.SelectMemory;
import com.worldpay.hub.MePOS.printer.commands.SetCodePage;
import com.worldpay.hub.MePOS.printer.commands.SetTabs;
import com.worldpay.hub.MePOS.printer.commands.SetWidth;
import com.worldpay.hub.MePOS.printer.commands.SingleWidthCharacters;
import com.worldpay.hub.MePOS.printer.commands.Underline;
import com.worldpay.hub.PrinterQueue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * MePOS implemetnation of the printer factory
 */
public class Factory implements PrinterFactory
{
    protected static final String TAG = "MePOS";
    @Override
    public PrinterCommand Beep() throws PrinterCommandNotImplementedException
    {
        return new Beep();
    }

    @Override
    public PrinterCommand Bold() throws PrinterCommandNotImplementedException
    {
        return new Bold();
    }

    @Override
    public PrinterCommand Bold(int mode) throws PrinterCommandNotImplementedException
    {
        return new Bold(mode);
    }


    @Override
    public PrinterCommand ClearPrinter() throws PrinterCommandNotImplementedException
    {
        return new ClearPrinter();
    }

    @Override
    public PrinterCommand CutPaper() throws PrinterCommandNotImplementedException
    {
        return new CutPaper();
    }

    @Override
    public PrinterCommand CutPaper(int type) throws PrinterCommandNotImplementedException
    {
        return new CutPaper(type);
    }

    @Override
    public PrinterCommand DoubleWidthCharacters() throws PrinterCommandNotImplementedException
    {
        return new DoubleWidthCharacters();
    }

    @Override
    public PrinterCommand DownloadBitmap(byte[] data) throws PrinterCommandNotImplementedException
    {
        return new DownloadBitmap(data);
    }

    @Override
    public PrinterCommand EraseMemory() throws PrinterCommandNotImplementedException
    {
        return new EraseMemory();
    }

    @Override
    public PrinterCommand FeedPaper() throws PrinterCommandNotImplementedException
    {
        return new FeedPaper();
    }

    @Override
    public PrinterCommand FeedPaper(int lines) throws PrinterCommandNotImplementedException
    {
        return new FeedPaper(lines);
    }

    @Override
    public PrinterCommand Flush() throws PrinterCommandNotImplementedException
    {
        return new Flush();
    }

    @Override
    public PrinterCommand GetStatus(byte status) throws PrinterCommandNotImplementedException
    {
        return new GetStatus(status);
    }

    @Override
    public PrinterCommand HorizontalTab() throws PrinterCommandNotImplementedException
    {
        return new HorizontalTab();
    }

    @Override
    public PrinterCommand InitialisePrinter() throws PrinterCommandNotImplementedException
    {
        return new InitialisePrinter();
    }

    @Override
    public PrinterCommand Italic() throws PrinterCommandNotImplementedException
    {
        return new Italic();
    }

    @Override
    public PrinterCommand Italic(int onOff) throws PrinterCommandNotImplementedException
    {
        return new Italic(onOff);
    }

    @Override
    public PrinterCommand Justify() throws PrinterCommandNotImplementedException
    {
        return new Justify();
    }

    @Override
    public PrinterCommand Justify(int position) throws PrinterCommandNotImplementedException
    {
        return new Justify(position);
    }

    @Override
    public PrinterCommand LeftMargin() throws PrinterCommandNotImplementedException
    {
        return new LeftMargin();
    }

    @Override
    public PrinterCommand LeftMargin(int margin) throws PrinterCommandNotImplementedException
    {
        return new LeftMargin(margin);
    }

    @Override
    public PrinterCommand OpenDrawer() throws PrinterCommandNotImplementedException
    {
        return new OpenDrawer();
    }

    @Override
    public PrinterCommand PrintBitmap() throws PrinterCommandNotImplementedException
    {
        return new PrintBitmap();
    }

    @Override
    public PrinterCommand PrintBitmap(int index) throws PrinterCommandNotImplementedException
    {
        return new PrintBitmap(index);
    }

    @Override
    public PrinterCommand PrintBitmap(Bitmap bitmap, int width, int rotation) throws PrinterCommandNotImplementedException
    {
        return new PrintBitmap(bitmap, width, rotation);
    }

    @Override
    public PrinterCommand PrintTestPage() throws PrinterCommandNotImplementedException
    {
        return new PrintTestPage();
    }

    @Override
    public PrinterCommand PrintText(String text) throws PrinterCommandNotImplementedException
    {
        return new PrintText(text);
    }

    @Override
    public PrinterCommand ReversePrintMode() throws PrinterCommandNotImplementedException
    {
        return new ReversePrintMode(PrinterFactory.REVERSE_ON);
    }

    @Override
    public PrinterCommand ReversePrintMode(int onOff) throws PrinterCommandNotImplementedException
    {
        return new ReversePrintMode(onOff);
    }

    @Override
    public PrinterCommand SelectMemory() throws PrinterCommandNotImplementedException
    {
        return new SelectMemory();
    }

    @Override
    public PrinterCommand SelectMemory(byte memoryLocation) throws PrinterCommandNotImplementedException
    {
        return new SelectMemory(memoryLocation);
    }

    @Override
    public PrinterCommand SetCodePage(int codePage) throws PrinterCommandNotImplementedException
    {
        return new SetCodePage(codePage);
    }

    @Override
    public PrinterCommand SetTabs() throws PrinterCommandNotImplementedException
    {
        return new SetTabs();
    }

    @Override
    public PrinterCommand SetTabs(int... positions) throws PrinterCommandNotImplementedException
    {
        return new SetTabs(positions);
    }

    @Override
    public PrinterCommand SetWidth(int width) throws PrinterCommandNotImplementedException
    {
        return new SetWidth(width);
    }

    @Override
    public PrinterCommand SingleWidthCharacters() throws PrinterCommandNotImplementedException
    {
        return new SingleWidthCharacters();
    }

    @Override
    public PrinterCommand Underline(int mode) throws PrinterCommandNotImplementedException
    {
        return new Underline(mode);
    }

    @Override
    public PrinterCommand SetCharacterSet(int mode) throws PrinterCommandNotImplementedException
    {
        return new SetCharacterSet(mode);
    }

    @Override
    public PrinterCommand RasterPrint(byte[] picture) throws PrinterCommandNotImplementedException
    {
        //Decode the bitmap
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
/*
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
*/
        int dataLength  = rowLength * pixelHeight;
        byte[] printData = new byte[dataLength];
        Log.d(TAG, String.format("Getting image data from offset %04X, reading %d bytes", offset, dataLength));
        Log.d(TAG, String.format("Reading %d rows of %d bytes", pixelHeight, rowLength));
        System.arraycopy(picture, offset, printData, 0, dataLength);
        return new RasterBitmap(printData);
    }

    @Override
    public void PrintRasterImage(PrinterQueue queue, byte[] picture)
    {
        //Decode the bitmap
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

        if (!valid)
            throw new IllegalArgumentException("Not a valid bitmap");

        //Calculate the number of bytes in each row
        int rowLength = ((pixelWidth + 31) / 32) * 4; //https://en.wikipedia.org/wiki/BMP_file_format#Pixel_storage
        if (rowLength > 36)
        {
            Log.d(TAG, "Long row length, printing in HD mode");
            printHDRasterImage(rowLength, pixelHeight, offset, picture, queue);
        }
        else
        {
            Log.d(TAG, "Short row length, printing in SD mode");
            printSDRasterImage(rowLength, pixelHeight, offset, picture, queue);
        }
    }

    /**
     * Prints a high density (576 pixel) image
     */
    private void printHDRasterImage(int rowLength, int pixelHeight, int offset, byte[] picture, PrinterQueue queue)
    {
        //Read the bitmap row by row
        //The rows are in reverse order, so start at the bottom and work backwards
        int dataLength  = rowLength * pixelHeight;
        int sliceLength = rowLength * 24;
        int sliceCount = pixelHeight / 24;
        if(pixelHeight % 24 > 0)
            sliceCount++; //Add an extra slice for incomplete slices at the end.
        int nextByte = offset;
        int sliceStart = offset;
        int actual = 0;
        byte[] masks = new byte[] {(byte)0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
        // This is much more complex than low density, as three rows are sent together.  You send the first bytes of each row (rotated)
        // then the second byte of each row, rather than the top row first... hmm.

        //Log.d(TAG, String.format("Row Length: %d Slice Length: %d Slice Count: %d", rowLength, sliceLength, sliceCount));
        for(int i=1; i <= sliceCount; i++)
        {
            //Create a slice
            sliceStart = picture.length - (sliceLength * i) - 1;
            int thisSliceLength = sliceLength;

            //Have we got enough data between the start point and the offset?
            if(sliceStart <= offset)
            {
                //We've not got enough data for this slice - it should be the final slice
                sliceStart = offset;
                thisSliceLength = sliceStart + sliceLength - offset - 1;
            }
            //Log.d(TAG, String.format("Slice start 0x%04X", sliceStart));
            byte[] slice = new byte[rowLength * 24];
            //For each byte in the slice
            int col = 1;
            byte sourceByte;
            //start with the first byte to the viewer
            nextByte = sliceStart + thisSliceLength - rowLength;
       /*     if(nextByte >= picture.length)
            {
                //We don't have image data for all of this slice.  Start with the final row
                Log.d(TAG, "Slice is shorter than the remaining image.");
                nextByte = picture.length - rowLength - 1;
            }*/

            //Log.d(TAG, String.format("Starting first column at %04X col %d", nextByte, col));
            int bit = 7;
            for (int b = 0; b < thisSliceLength; b++)
            {
                //Get each of the 8 bytes needed to provide bits into this byte
                for (int box = 0;  box < 8; box++)
                {
                    //Log.d(TAG, String.format("Reading byte %04X", nextByte));
                    sourceByte = picture[nextByte];
                    if ((sourceByte & masks[7 - bit]) == 0)
                    {
                        //Set the bit mask, msb first
                        slice[b] += masks[box];
                    }
                    nextByte -= rowLength;
                }
                //Every third byte, move on to the next bit mask
                if(b % 3 == 2) //E.g 5 % 3 = 2
                {
                    bit--;
                    if(bit < 0)
                    {
                        bit = 7;
                        //We only want to move to the next byte column when we've done all 8 bits in the column
                        col++;
                    }

                    //Move onto the next column.
                    nextByte = sliceStart + thisSliceLength - rowLength + col;
                    //Log.d(TAG, String.format("Starting new column at %04X col %d", nextByte, col));

                    if(nextByte >= picture.length || nextByte <= offset)
                    {
                        //We've got more slice than picture, because the image doesn't
                        //divide neatly into 24 rows.  We can abort here, and send this slice now
                        break;
                    }
                }
            }
            //Log.d(TAG, String.format("Expected: %d Actual: %d", 8 * rowLength, actual));
            queue.add(new RasterBitmap(slice, RasterBitmap.MODE_HIGH));
        }
    }
    /**
     * Prints a standard density (288 pixel) image
     */
    private void printSDRasterImage(int rowLength, int pixelHeight, int offset, byte[] picture, PrinterQueue queue)
    {

        //Read the bitmap row by row
        //The rows are in reverse order, so start at the bottom and work backwards
        int dataLength  = rowLength * pixelHeight;
        int sliceLength = rowLength * 8;
        int sliceCount = pixelHeight / 8;
        int nextByte = offset;
        int sliceStart = offset;
        int actual = 0;
        byte[] masks = new byte[] {(byte)0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
        //  Log.d(TAG, String.format("Row Length: %d Slice Length: %d Slice Count: %d", rowLength, sliceLength, sliceCount));
        for(int i=sliceCount - 1; i > 0; i--)
        {
            //Create a slice
            sliceStart = (sliceLength * i) + offset;
            //Log.d(TAG, String.format("Slice start 0x%04X", sliceStart));
            byte[] slice = new byte[sliceLength];
            actual = 0;
            for(int col = 0; col < rowLength; col++)
            {
                //Calculate the start byte for this box
                nextByte = sliceStart + sliceLength - rowLength + col;
                for (int box = 0; box < 8; box++)
                {
                    /*if(col == 0)
                    {
                        Log.d(TAG, String.format("Next Byte at: %04X Value: %02X", nextByte, picture[nextByte]));
                    }*/
                    //Log.d(TAG, String.format("Adding byte for col: %d box: %d index: %d", col, box, (col * 8) + box));
                    //Log.d(TAG, String.format("Reading byte 0x%04X", nextByte));

                    //Add a 90 degree rotation
                    // slice[(col * 8) + box] = (byte)~(picture[nextByte]);
                    //Image needs to be inverted

                    byte inverted = (byte)~(picture[nextByte]);

                    //for each bit in the byte...
                    for(int bit = 0; bit < 8; bit++)
                    {
                        //If this bit is set...
                        if((inverted & masks[bit]) == masks[bit])
                        {
                            //then set this bit on the byte in the box that matches the rotation
                            // read this as:
                            // slice[start of the 8x8 box, + offset for this bit]
                            // += the mask for the byte this represents.  So that bit set on the bottom
                            // byte uses the final mask to set the 0x01 bit.  Another example is that
                            // a bit set on the second byte up (6) would use the mask number 6 (0x02)
                            slice[(col * 8) + bit] += masks[box];
                        }
                    }

                    /* Worked example */
                    /* Source data
                       xxxx.... = 0xF0
                       .xxxx... = 0x78
                       ..xxxx.. = 0x3C
                       ...xxxx. = 0x1E
                       ....xxxx = 0x0F
                       ...xxxx. = 0x1E
                       ..xxxx.. = 0x3C
                       .xxxx... = 0x78

                       for each byte...
                         check each bit
                            if((0xF0 & 0x80) == 0x80) it does
                                output byte 7 += 0x80
                            if((0xF0 & 0x40) == 0x40) it does
                                output byte 6 += 0x40
                            if((0xF0 & 0x20) == 0x20) it does
                                output byte 5 += 0x20
                            if((0xF0 & 0x10) == 0x10) it does
                                output byte 4 += 0x10
                                the rest are left as 0
                     */
                    nextByte -= rowLength;

                    actual++;
                }
            }

            Log.d(TAG, String.format("Expected: %d Actual: %d", 8 * rowLength, actual));
            queue.add(new RasterBitmap(slice));
        }
    }
}

