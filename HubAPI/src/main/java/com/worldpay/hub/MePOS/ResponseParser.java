package com.worldpay.hub.MePOS;
import android.util.Log;

import com.worldpay.hub.Checksum;
import com.worldpay.hub.MePOS.commands.Command;
import com.worldpay.hub.link.Envelope;
import com.worldpay.hub.usbserial.util.HexDump;

import java.nio.ByteBuffer;

/**
 * Deserialise a response into a queryable class.
 */
public class ResponseParser
{
    public static final int STX = 0x02;
    public static final int DLE = 0x10;

    public int mFrameLength;
    public int mCurrentLength;

    protected static final int STATE_START          = 0;
    protected static final int STATE_LENGTH         = 1;
    protected static final int STATE_ADDRESS        = 2;
    protected static final int STATE_SOURCE         = 3;
    protected static final int STATE_TAG            = 4;
    protected static final int STATE_COMMAND_CODE   = 5;
    protected static final int STATE_COMMAND_DATA   = 6;
    protected static final int STATE_CHECKSUM       = 7;

    protected int mExpectedState = STATE_START;

    protected Envelope mCurrentEnvelope;
    protected Command mCurrentCommand;
    protected ByteBuffer mCommandData;

    private static final boolean SUPPORT_MULTI_FRAME = false;

    /**
     * Extracts Envelopes from Frames
     * @param data
     */
    public Envelope process(byte[] data)
    {
        //This is a finite state machine, there are not very many states.
        //We're going to process the data byte-by-byte
        //STX - Flush any current data as a new frame and start a fresh one

        //Its important that this method is not initialised when called, state must be
        //maintained between calls.

     //   Log.d("MePOS", "Raw data follows");
     //   Log.d("MePOS", HexDump.dumpHexString(data, 0, data.length));
        byte[] unescapedData = unescape(data);

        Log.i("MePOS", "Unescaped response data follows");
        Log.i("MePOS", HexDump.dumpHexString(unescapedData, 0, unescapedData.length));

        //Validate the checksum
        if(!validate(unescapedData))
            return null;

        startNewFrame();
        for(byte b : unescapedData)
        {
            if(mExpectedState == STATE_START && b == STX)
            {
                //Header byte, we've already unescaped, so we might hit other STX bytes in the data
                //now.  That's completely valid
                mExpectedState = STATE_LENGTH;
            }
            else if(mExpectedState == STATE_LENGTH) {
                addLength(b);
            }
            else {
                switch (mExpectedState) {

                    case STATE_ADDRESS:
                        if (mCurrentEnvelope != null) {
                            mCurrentEnvelope.setAddress(b);
                            mExpectedState = STATE_SOURCE;
                        }
                        break;
                    case STATE_SOURCE:
                        if (mCurrentEnvelope != null) {
                            mCurrentEnvelope.setSource(b);
                            mExpectedState = STATE_COMMAND_CODE;
                        }
                        break;
                    case STATE_COMMAND_CODE:
                        mCurrentCommand = Command.getCommand((char)b);
                        mExpectedState = STATE_TAG;
                        break;
                    case STATE_TAG:
                        if (mCurrentEnvelope != null) {
                            mCurrentEnvelope.setTag((byte) (b & 0x7F));
                            mExpectedState = STATE_COMMAND_DATA;
                        }
                        break;
                    case STATE_COMMAND_DATA:
                        addCommandData(b);
                        break;
                    case STATE_CHECKSUM:
                        //Do nothing, already validated
                        break;
                }

                mCurrentLength++;

                if(mCurrentLength == mFrameLength)
                {
                    //We've read the expected data
                    mExpectedState = STATE_CHECKSUM;
                }
            }
        }

        mCurrentCommand.setCommandData(mCommandData.array());
        mCurrentEnvelope.setCommand(mCurrentCommand);
        return mCurrentEnvelope;
    }

    protected void addCommandData(byte b)
    {
        mCommandData.put(b);
    }

    protected boolean validate(byte[] data)
    {
        int len = data.length;

        //The last two bytes make the checksum we need to compare
        byte msb = (byte)(data[len - 2] & 0xFF);
        byte lsb = (byte)(data[len - 1] & 0xFF);
        int testCrc = msb & 0xFF;
        testCrc = testCrc << 8;
        testCrc += lsb & 0xFF;
        testCrc = testCrc & 0xFFFF;

        //Create our own calculation for what the CRC should be
        byte[] validateData = new byte[len - 2];
        System.arraycopy(data, 0, validateData, 0, len - 2);
        int crc = Checksum.generate(validateData);

        if(crc == testCrc)
            return true;
        else
        {
            Log.i("MePOS", "CRC failed");
            Log.i("MePOS", String.format("Expected: %04X  Actual: %04X", crc, testCrc));
            return false;
        }
    }

    protected void startNewFrame()
    {
        mCurrentEnvelope = new Envelope();
        mCurrentCommand = new Command();
        mFrameLength = 0;
        mCurrentLength = 0;
        mExpectedState = STATE_START;
    }

    protected void addLength(byte b)
    {
        if(mFrameLength == 0 && ((b & 0x80) == 0x80))
        {
            mFrameLength = b & (byte)0x7F; //Mask out bit 7
            //Don't change the state marker
        }
        else
        {
            //TODO:  This doesn't calculate the length for responses > 127 bytes correctly
            //Only expecting one byte, or it is the second byte
            mFrameLength += b;
            mExpectedState = STATE_ADDRESS;
            mCommandData = ByteBuffer.allocate(mFrameLength - 4);
        }
    }

    protected byte[] unescape(byte[] data)
    {
        //First pass - determine length
        int len = 1;

        for(int i = 1; i < data.length; i++)
        {
            if (data[i] != DLE)
            {
                len++;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(data[0]);

        //Second pass, copy data
        //init of i = 1 is deliberate.  don't escape the first byte (STX)
        boolean flipByte = false;
        for(int i = 1; i < data.length; i++)
        {
            if(data[i] == DLE)
            {
                flipByte = true;
            }
            else
            {
                byte b = data[i];
                if(flipByte)
                    b = (byte)(b & 0xBF);
                buffer.put(b);
                flipByte = false;
            }
        }

        return buffer.array();
    }
}
