package com.worldpay.hub.MePOS;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.worldpay.hub.Checksum;
import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.usbserial.util.HexDump;

/**
 * Created by SJP on 28/01/2016.
 */

public class FirmwareUpdate
{
    protected final static String TAG = "MePOS";
    protected volatile FT_Device mHub;
    protected volatile byte mLastReceipt;
    protected byte[] mFirmware;
    protected int mBytesSent;
    protected final int MAX_RETRIES = 5;
    protected final int PACKET_SIZE_XMODEM_CRC = 133;
    protected final int DATA_SIZE_128 = 128;
    protected final int TIMEOUT = 1000;
    protected final byte NUL = 0;    /* Null */
    protected final byte SOH = 1;    /* Start Of Header */
    protected final byte EOT = 4;    /* End Of Transmission */
    protected final byte ACK = 6;    /* ACKnowlege */
    protected final byte NAK = 0x15; /* Negative AcKnowlege */
    protected final byte CAN = 0x18; /* Cancel */
    protected final byte SUB = 0x1A; /* Substitute */
    protected final byte HASH = 0x43; /* The # symbol */
    protected final byte PROMPT = 0x3E; /* The > symbol */

    public static final int TYPE_LOADER = 0;
    public static final int TYPE_FIRMWARE = 2;

    /**
     * @param hub The hub device as detected by the D2xx manager
     */
    public FirmwareUpdate(FT_Device hub)
    {
        if(hub == null)
            throw new IllegalArgumentException("Device cannot be null");

        mHub = hub;
        mBytesSent = 0;
    }

    public int bytesSent()
    {
        return mBytesSent;
    }

    /***
     * Send firmware but specialised to send the  magic number for the loader.
     */
    public boolean sendLoader(InputStream is) throws HubResponseException, IOException
    {
        boolean success = false;
        //Start the ReadThread
        ReadThread readThread = new ReadThread();
        readThread.start();

        //We have to send some data to the hub to prepare it for the firmware
        String beforeFirmware = "S20000800,00001740#";

        String afterFirmware = String.format("W20000840,00000000#W20000848,00000001#W2000084C,00000001#W20000850,00000000#W20000854,00000000#W20000858,00000000#G20000800#");

        Log.d(TAG, "Sending start signal");
        send(beforeFirmware);

        //We need to wait for a response
        mLastReceipt = NUL;
        byte ack = awaitAcknowlegement((byte)'C');
        if(ack == 'C')
        {
            mBytesSent = 0;
            //Ok, we can proceed
            processFirmware(is);

            //And then send the command for the hub to restart
            send(afterFirmware);

            if(awaitAcknowlegement(PROMPT) == PROMPT)
            {
                //If we've not thrown an exception then...
                success = true;
            }
        }
        else
        {
            //Stop the read thread
            readThread.cancel();

            throw new HubResponseException("Hub did not acknowledge firmware update mode");
        }

        //Stop the read thread
        readThread.cancel();

        return success;
    }

    /***
     * Sends firmware to the hub. This command needs to be followed with commitFirmware when complete.
     * @param is The input stream containing the data
     * @param length The number of bytes to send
     * @return
     * @throws HubResponseException
     * @throws IOException
     */
    public boolean sendFirmware(InputStream is, int length, int offset) throws HubResponseException, IOException
    {
        boolean success = false;
        //Start the ReadThread
        ReadThread readThread = new ReadThread();
        readThread.start();

        //We have to send some data to the hub to prepare it for the firmware
        String beforeFirmware = String.format("S20002174,%08X#", length);

        String writeToFlash = "W20000840,00000002#";
        String initialBufferAddress = "W20000848,20002174#";
        String firmwareLength = String.format("W2000084C,%08X#", length);
        String offsetCommand = String.format("W20000850,%08d#", offset);
        String executeCodeFromAddress = "G20000800";

        String afterFirmware = writeToFlash + initialBufferAddress + firmwareLength + offsetCommand + executeCodeFromAddress;

        Log.d(TAG, "Sending start signal");
        send(beforeFirmware);

        //We need to wait for a response
        mLastReceipt = NUL;
        byte ack = awaitAcknowlegement((byte)'C');
        if(ack == 'C')
        {
            mBytesSent = 0;
            //Ok, we can proceed
            processFirmware(is);

            //And then send the command for the hub to restart
            send(afterFirmware);

            if(awaitAcknowlegement(PROMPT) == PROMPT)
            {
                //If we've not thrown an exception then...
                success = true;
            }
        }
        else
        {
            //Stop the read thread
            readThread.cancel();

            throw new HubResponseException("Hub did not acknowledge firmware update mode");
        }

        //Stop the read thread
        readThread.cancel();

        return success;
    }

    /***
     * Commits firmware that has already been loaded.
     */
    public void commitFirmware()
    {
        send("#\r\nW400E0A04,5A00010B#");
    }

    /**
     * Sends arbitary code commands to the box
     * @param code
     */
    public void send(String code)
    {
        byte data[] = new byte[0];
        try
        {
            data = code.getBytes("ISO-8859-1");
            Log.d(TAG, String.format("Writing %d bytes", data.length));
            String writeData = HexDump.dumpHexString(data, 0, data.length);
            Log.d(TAG, writeData);
            mHub.write(data, data.length);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    protected void processFirmware(InputStream is) throws HubResponseException, IOException
    {
        int sequenceNumber = 1;
        int len = 1;
        while(len > 0)
        {
            byte[] buffer = new byte[DATA_SIZE_128];
            len = is.read(buffer, 0, DATA_SIZE_128);

            int retries = 0;
            boolean acknowledged = false;
            //There is an assumption that the ReadThread is running simultaneously
            while(retries < MAX_RETRIES && !acknowledged)
            {
                mLastReceipt = NUL;
                sendPacket(buffer, sequenceNumber++);
                retries++;

                acknowledged = awaitAcknowlegement(ACK) == ACK;
            }

            if(retries == MAX_RETRIES)
            {
                //Failed to send
                throw new HubResponseException("Send firmware failed");
            }
        }

        //Send end of transmission marker
        sendData(new byte[] { EOT }, 1);
    }

    protected byte awaitAcknowlegement(byte match)
    {
        byte[] readBuffer;// = new byte[1024];
        int readLength = 0;
        long timeout = System.currentTimeMillis() + TIMEOUT;
        byte response = 0;

        while(mLastReceipt != match && timeout > System.currentTimeMillis())
        {
            /* mLastReceipt is being updated by ReadThread */
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {e.printStackTrace();}
        }

        return mLastReceipt;
    }

    protected void sendPacket(byte[] packet, int seqNo)
    {
        byte[] sendBuffer = new byte[PACKET_SIZE_XMODEM_CRC];

        //Add standard headers
        sendBuffer[0] = SOH;
        sendBuffer[1] = (byte) seqNo;
        sendBuffer[2] = (byte)~seqNo;

        //Copy the data we've got
        System.arraycopy(packet, 0, sendBuffer, 3, packet.length);

        //check that this is a full packet
        if(packet.length < DATA_SIZE_128)
            for(int i = packet.length; i < DATA_SIZE_128; i++)
                sendBuffer[i] = SUB; //Pad out the remaining space in the envelope

        //Add the CRC
       /* int crc = calcCrc(sendBuffer, 3, DATA_SIZE_128);
        sendBuffer[131] = (byte) (crc >> 8);    //MSB
        sendBuffer[132] = (byte) (crc & 0xFF);  //LSB*/
        byte[] crc = calCrc(sendBuffer, 3, DATA_SIZE_128);
        sendBuffer[131] = crc[0];    //MSB
        sendBuffer[132] = crc[1];    //LSB
        Log.d(TAG, String.format("Checksum %02X%02X", sendBuffer[131], sendBuffer[132]));
        Log.d(TAG, HexDump.dumpHexString(sendBuffer));

        //Send the data to the hub
        sendData(sendBuffer, PACKET_SIZE_XMODEM_CRC);
    }

    /***
     * This is the version from the FTDI source code, which seems to generate a different result for reasons I can't quite see yet...
     * When you're using it, check whether you are calling calCrc (theirs) or calcCrc (ours).
     * @param buffer
     * @param startPos
     * @param count
     * @return
     */
    // calculate CRC
    byte[] calCrc(byte[] buffer, int startPos, int count)
    {
        int crc = 0, i;
        byte[] crcHL = new byte[2];

        while(--count >= 0)
        {
            crc = crc ^ (int)buffer[startPos++] << 8;
            for(i = 0; i < 8; ++i)
            {
                if((crc & 0x8000)!= 0) crc = crc << 1 ^ 0x1021;
                else crc = crc << 1;
            }
        }
        crc &=  0xFFFF;

        crcHL[0] = (byte) ((crc >> 8) & 0xFF);
        crcHL[1] = (byte) (crc & 0xFF);

        return crcHL;
    }

    protected int calcCrc(byte[] data, int offset, int length)
    {
        byte[] temp = new byte[length];
        System.arraycopy(data, offset, temp, 0, length);
        return Checksum.generate(temp);
    }

    protected void sendData(byte[] data, int length)
    {
        Log.d(TAG, String.format("Writing %d bytes", length));
        String writeData = HexDump.dumpHexString(data, 0, data.length);
        Log.d(TAG, writeData);
        mHub.write(data, length);
        mBytesSent += DATA_SIZE_128;
    }

    class ReadThread extends Thread
    {
        final int USB_DATA_BUFFER = 8192;

        ReadThread()
        {
            this.setPriority(MAX_PRIORITY);
        }

        public void run()
        {
            byte[] usbdata = new byte[USB_DATA_BUFFER];
            int readcount = 0;
            int iTotalBytes = 0;
            int MAX_NUM_BYTES = 65536;

            while (!this.isInterrupted())
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {e.printStackTrace();}

                //Log.e(TAG,"iTotalBytes:"+iTotalBytes);
                while(iTotalBytes > (MAX_NUM_BYTES - (USB_DATA_BUFFER+1)))
                {
                    try
                    {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e) {e.printStackTrace();}
                }

                readcount = mHub.getQueueStatus();
                //Log.d(TAG,"iavailable:" + readcount);
                if (readcount > 0)
                {
                    if(readcount > USB_DATA_BUFFER)
                    {
                        readcount = USB_DATA_BUFFER;
                    }
                    mHub.read(usbdata, readcount);
                    byte[] modemDataBuffer = new byte[2048];
                    for (int i = 0; i < readcount; i++)
                    {
                        modemDataBuffer[i] = usbdata[i];
                        Log.d(TAG, "RT usbdata[" + i + "]:(" + usbdata[i] + ")");

                        if(usbdata[i] == NAK
                        || usbdata[i] == ACK
                        || usbdata[i] == HASH
                        || usbdata[i] == CAN
                        || usbdata[i] == PROMPT)
                        {
                            mLastReceipt = usbdata[i];
                            Log.d(TAG, String.format("Got response: 0x%02X", usbdata[i]));
                            if(usbdata[i] == CAN)
                                Log.d(TAG, "Cancel signal received");
                        }
                    }
                    //Ignore other response bytes
                }
            }
        }


        public final void cancel()
        {
            interrupt();
        }
    }
}
