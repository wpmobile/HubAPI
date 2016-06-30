package com.worldpay.hub.MePOS;
import android.util.Log;

import com.worldpay.hub.Checksum;
import com.worldpay.hub.Logger;
import com.worldpay.hub.MePOS.commands.Command;
import com.worldpay.hub.link.Envelope;
import com.worldpay.hub.usbserial.util.HexDump;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Deserialise a response into a queryable class.
 */
public class ResponseParser
{
    public static final int STX = 0x02;
    public static final int DLE = 0x10;

    public int mFrameLength;
    public int mCurrentLength;

    protected static final int STATE_SEARCHING      = 0;
    protected static final int STATE_LENGTH         = 1;
    protected static final int STATE_ADDRESS        = 2;
    protected static final int STATE_SOURCE         = 3;
    protected static final int STATE_TAG            = 4;
    protected static final int STATE_COMMAND_CODE   = 5;
    protected static final int STATE_COMMAND_DATA   = 6;
    protected static final int STATE_CHECKSUM_MSB   = 7;
    protected static final int STATE_CHECKSUM_LSB   = 8;

    protected boolean mEscaping;

    protected int mExpectedState = STATE_ADDRESS;

    protected Envelope mCurrentEnvelope;
    protected Command mCurrentCommand;
    protected ByteBuffer mCommandData;
    protected byte[] mReadData;

    private static final boolean SUPPORT_MULTI_FRAME = true;

    /**
     * Extracts Envelopes from Frames
     * @param data
     */
    public ArrayList<Envelope> process(byte[] data)
    {
        ArrayList<Envelope> envelopes = new ArrayList<Envelope>();

        if(mReadData == null)
        {
            mReadData = data;
        }
        else
        {
            //Append this as new data to process
            byte[] tempData = new byte[mReadData.length + data.length];
            System.arraycopy(mReadData, 0, tempData, 0, mReadData.length);
            System.arraycopy(data, 0, tempData, 0, data.length);

            mReadData = tempData;
        }

        //Logger.d("MePOS", "Raw data follows");
        //Logger.d("MePOS", HexDump.dumpHexString(mReadData, 0, mReadData.length));

        //Scan the data for a start byte
        int pos = 0;
        int startMarker = 0;
        boolean isEscaping = false;
        byte checksumMSB = 0x00;

        //We need to work out how long the command is to allocate data
        int validationLength = 1; //1 for STX
        ByteBuffer validationBuffer = ByteBuffer.allocate(1024);

        mExpectedState = STATE_SEARCHING;
        //check that there is at least space for STX, len, command + checksum
        if(mReadData.length < 8)
        {
            //Not enough data, wait for some more
            saveData(pos);
            return envelopes;
        }


        //Looks like enough data, lets parse it
        for(byte b : mReadData)
        {
            if(pos == mReadData.length && mExpectedState != STATE_CHECKSUM_LSB)
            {
                //We've reached the end of the data, but we are not looking for a checksum byte,
                //then we don't have a complete set of data.  Save now and abort
                saveData(startMarker);
                continue;
            }

            //Three special cases
            if(b == STX)
            {
                //We are at the beginning
                startMarker = pos;
                startNewFrame();

                validationLength = 1; //1 for stx
                mExpectedState = STATE_LENGTH;
                pos++;
                continue;
            }
            else if(b == DLE) //its essential that this test comes before the escaping test
            {
                isEscaping = true;
                pos++;
                continue;
            }
            else if(isEscaping)
            {
                //flip this byte and carry on this iteration
                b = (byte)(b & 0xBF);
                isEscaping = false;
            }

            switch (mExpectedState)
            {
                case STATE_SEARCHING:
                    //just loop around - we need to discard this data until we find an STX byte
                    break;
                case STATE_LENGTH:
                    if ((b & 0x40) == 0x40)
                    {
                        //Two byte length
                        mFrameLength = (byte) (b & 0xBF);
                        mFrameLength = mFrameLength << 8;
                        validationLength++;
                    } else //single or second byte
                    {
                        mFrameLength += b;

                        //Check that there is enough data left in the buffer to fulfill this command
                        if (mReadData.length < pos + mFrameLength + 2) //+2 for checksum
                        {
                            saveData(startMarker);
                            //Sniff, sniff.  Can you smell a goto?
                            return envelopes;
                        }

                        //We should have enough data to complete this parse.  Allocate some storage
                        mCommandData = ByteBuffer.allocate(mFrameLength - 4); // - 4 for address, source, tag and command

                        validationLength++;
                        validationLength += mFrameLength;

                        validationBuffer = ByteBuffer.allocate(validationLength);
                        validationBuffer.put((byte) (STX));
                        //Add the escaped length back in
                        if (pos > 1 && (mReadData[pos - 1] & 0x40) == 0x40)
                        {
                            //Two byte length
                            validationBuffer.put(mReadData[pos - 1]);
                        }

                        //Add this length byte
                        validationBuffer.put(b);

                        //The next byte should be the command bytes
                        mExpectedState = STATE_ADDRESS;
                    }
                    break;
                case STATE_ADDRESS:
                    mCurrentEnvelope.setAddress(b);
                    validationBuffer.put(b);
                    mExpectedState = STATE_SOURCE;
                    break;
                case STATE_SOURCE:
                    mCurrentEnvelope.setSource(b);
                    validationBuffer.put(b);
                    mExpectedState = STATE_COMMAND_CODE;
                    break;
                case STATE_COMMAND_CODE:
                    mCurrentCommand = Command.getCommand((char) b);
                    validationBuffer.put(b);
                    mExpectedState = STATE_TAG;
                    break;
                case STATE_TAG:
                    mCurrentEnvelope.setTag((byte) (b & 0x7F));
                    validationBuffer.put(b);
                    //Its possible to have no command data
                    if(mCommandData.capacity() > 0)
                    {
                        mExpectedState = STATE_COMMAND_DATA;
                    }
                    else
                    {
                        //No command data - go to checksum
                        mExpectedState = STATE_CHECKSUM_MSB;
                    }
                    break;
                case STATE_COMMAND_DATA:
                    mCommandData.put(b);
                    validationBuffer.put(b);

                    //Check to see if we're expecting any more command data bytes
                    int bytesRemaining = mCommandData.remaining();
                    if (bytesRemaining == 0)
                    {
                        //Finished
                        mExpectedState = STATE_CHECKSUM_MSB;
                    }
                    break;
                case STATE_CHECKSUM_MSB:
                    checksumMSB = b;
                    mExpectedState = STATE_CHECKSUM_LSB;
                    break;
                case STATE_CHECKSUM_LSB:
                    //Validate what we've got
                    if(validate(validationBuffer.array(), checksumMSB, b))
                    {
                        mCurrentCommand.setCommandData(mCommandData.array());
                        mCurrentEnvelope.setCommand(mCurrentCommand);

                        envelopes.add(mCurrentEnvelope);
                    }

            }

            //Increment the positional counter
            pos++;
        }

        return envelopes;
    }

    protected boolean validate(byte[] data, byte msb, byte lsb)
    {
        //The last two bytes make the checksum we need to compare
        int testCrc = msb & 0xFF;
        testCrc = testCrc << 8;
        testCrc += lsb & 0xFF;
        testCrc = testCrc & 0xFFFF;

        //Create our own calculation for what the CRC should be
        int crc = Checksum.generate(data);

        if(crc == testCrc)
        {
            return true;
        }
        else
        {
            Logger.d("MePOS", "CRC failed");
            Logger.d("MePOS", String.format("Expected: %04X  Actual: %04X", crc, testCrc));
            return false;
        }
    }

    protected void startNewFrame()
    {
        mCurrentEnvelope = new Envelope();
        mCurrentCommand = new Command();
        mFrameLength = 0;
    }

    protected void saveData(int fromIndex)
    {
        int len = mReadData.length - fromIndex;
        if(len > 0)
        {
            Logger.d("MePOS", "Saving data");
            Logger.d("MePOS", String.format("Saving %d bytes from %d to %d", mReadData.length - fromIndex, fromIndex, mReadData.length));
            byte[] tempdata = new byte[mReadData.length - fromIndex];
            System.arraycopy(mReadData, fromIndex, tempdata, 0, mReadData.length - fromIndex);
            mReadData = tempdata;
        }
    }

    public void clear()
    {
        mReadData = null;
    }
}
