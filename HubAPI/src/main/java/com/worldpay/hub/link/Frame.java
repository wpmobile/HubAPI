package com.worldpay.hub.link;

import com.worldpay.hub.Checksum;
import com.worldpay.hub.MePOS.commands.Command;

/**
 * Represents a single frame to be transmitted
 */
public class Frame
{
    private static final int MAX_DATASIZE = 16383;
    public static final int STX = 0x02;
    public static final int DLE = 0x10;
    protected byte[] mData;
    protected boolean mIsValid;

    /*
    Produces an array of frames suitable for transmitting the command
     */
    public static Frame[] getFrames(Envelope envelope)
    {
        int dataLength = envelope.getDataLength();
        int framesQuantity = (int) Math.ceil((float)dataLength / MAX_DATASIZE);

        Frame[] frames = new Frame[framesQuantity];
        //For each 16k block of command data, create a frame and add it to the
        //array
        int offset = 0;
        int c = 0;
        while (offset < envelope.getData().length)
        {
            int len = Math.min(envelope.getData().length - offset, MAX_DATASIZE);

            byte[] buffer = new byte[len];
            System.arraycopy(envelope.getData(), offset, buffer, 0, len);
            frames[c++] = new Frame(buffer);

            offset += len;
        }

        return frames;
    }

    /**
     * Creates a new instance of a Frame to wrap the supplied data
     * @param data
     */
    public Frame(byte[] data)
    {
        //Check that the data is < 16k in length
        mIsValid = data.length <= MAX_DATASIZE;

        int len = data.length + 2;  // +1 for STX, +1 for length
        if(data.length > 127)       /* needs an extra length byte */
            len++;

        //Allocate a new byte array for this frame
        byte[] buffer = new byte[len];

        //Prepend the STX byte
        int c = 0;
        buffer[c++] = STX;

        //Prepend the length bytes
        if(data.length < 128)
        {
            buffer[c++] = (byte) data.length;
        }
        else
        {
            int tempLen = data.length;
            buffer[c++] = (byte) (0x80 | (tempLen & 0x7F));
            buffer[c++] = (byte) (0x7F & (tempLen >> 7));
        }

      /*  int testLen = 1962;
        byte byte1 = (byte) (0x80 | (testLen & 0x7F));
        byte byte2 = (byte) (0x7F & (testLen >> 7));

        Logger.d("MePOS", String.format("Byte 1 (LSB): %02X Byte 2 (MSB): %02X", byte1, byte2));

        if(byte1 == 0xAA && byte2 == 0x0F)
            Logger.d("MePOS", "Length calculation is correct");*/

        //Copy the data
        System.arraycopy(data, 0, buffer, c, data.length);

        //Append the CRC
        buffer = appendCRC(buffer, Checksum.generate(buffer));

        //Logger.d("MePOS", "Raw data follows");
        //HexDump.logHexString(buffer, 0, buffer.length);

        //Escape the data
        mData = escapeFrame(buffer);

       // Logger.d("MePOS", "Unescaped data follows");
      //  HexDump.logHexString(mData, 0, mData.length);
    }

    public Frame()
    {
        mData = new byte[1];
        mData[0] = STX;

        mIsValid = true;
    }

    protected byte[] appendCRC(byte[] data, int crc)
    {
        int c = data.length;
        int len = c + 2; //for crc
        byte[] buffer = new byte[len];
        System.arraycopy(data, 0, buffer, 0, c);

        //Add the CRC to the data packet
        buffer[c++] = (byte) (crc >> 8);    //MSB
        buffer[c++] = (byte) (crc & 0xFF);  //LSB

        return buffer;
    }

    protected byte[] escapeFrame(byte[] data)
    {
        int reqDataLen = requiredDataLength(data);

        //If we don't need to escape the data, then return the input data
        if(reqDataLen == data.length)
            return data;

      //  Logger.d("MePOS", String.format("Orig data len: %d  Escaped data len: %d", data.length, reqDataLen));

        byte[] buffer = new byte[reqDataLen];

        int c = 0;
        for(byte b : data)
        {
            if(c > 0 && (b == STX || b == DLE)) //Ignore first byte
            {
                buffer[c++] = DLE;
                buffer[c++] = (byte) (b | 0x40); //Set bit 6 on escaped characters
            }
            else
            {
                buffer[c++] = b;
            }
        }

        return buffer;
    }

    /*
    * The required number of bytes required to escape the provided data
    */
    public int requiredDataLength(byte[] data)
    {
        //Check each byte for a 0x20 byte or 0x10 byte
       // Logger.d("MePOS", "Calculating required number of bytes to hold escaped data");
       // Logger.d("MePOS", String.format("Original length: %d", data.length));
        int len = 0;
        for(byte b : data)
        {
            if(len > 0 && (b == STX || b == DLE)) //Ignore first byte
            {
                len++;
            }

            len++;
        }

      //  Logger.d("MePOS", String.format("Escaped length: %d", len));
        return len;
    }

    //Returns the byte array of the data for this frame
    public byte[] getFrameData()
    {
        return mData;
    }

    /**
     * If the frame was created with an invalid amount of data,
     * then the frame is invalid.
     * Frames can be empty, but should not have any length bytes or CRC.
     * @return
     */
    public boolean isValid()
    {
        return mIsValid;
    }
}
