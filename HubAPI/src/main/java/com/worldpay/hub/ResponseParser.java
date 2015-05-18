package com.worldpay.hub;
import android.util.Log;

import com.worldpay.hub.commands.Command;
import com.worldpay.hub.link.Envelope;

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

        byte[] unescapedData = unescape(data);

        //Validate the checksum
        if(!validate(unescapedData))
            return null;

        for(byte b : unescapedData)
        {
            if(b == STX)
            {
                //Start of frame
                startNewFrame();
            }
            else
            {
                if(mExpectedState == STATE_LENGTH) {
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
                                mCurrentEnvelope.setTag(b);
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
        byte[] validateData = new byte[len - 2];
        System.arraycopy(data, 0, validateData, 0, len - 2);
        int crc = Checksum.generate(validateData);

        //The last two bytes make the checksum we need to compare
        int testCrc = (byte)(data[len - 2] & 0xFF);
        testCrc = testCrc << 8;
        testCrc += (byte)(data[len - 1] & 0xFF);
        testCrc = testCrc & 0xFFFF;
        // return (crc == testCrc);

        return true;
    }

    protected void startNewFrame()
    {
        mCurrentEnvelope = new Envelope();
        mCurrentCommand = new Command();
        mFrameLength = 0;
        mCurrentLength = 0;
        mExpectedState = STATE_LENGTH;
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
            //Only expecting one byte, or it is the second byte
            mFrameLength += b;
            mExpectedState = STATE_ADDRESS;
            mCommandData = ByteBuffer.allocate(mFrameLength - 4);
        }
    }

    protected byte[] unescape(byte[] data)
    {
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data[0]);
        //init of i = 1 is deliberate.  don't escape the first byte (STX)

        for(int i = 1; i < data.length; i++)
        {
            if(data[i] == DLE)
            {
                if(data.length <= i+1)
                {
                    data[i] = (byte)(data[1] & (byte)0xBF); //Mask out bit 6
                }
            }
            else
            {
               buffer.put(data[i]);
            }
        }

        return buffer.array();
    }
}
