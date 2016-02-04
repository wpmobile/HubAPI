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
    protected final int TIMEOUT = 1000000;
    protected final byte NUL = 0;    /* Null */
    protected final byte SOH = 1;    /* Start Of Header */
    protected final byte EOT = 4;    /* End Of Transmission */
    protected final byte ACK = 6;    /* ACKnowlege */
    protected final byte NAK = 0x15; /* Negative AcKnowlege */
    protected final byte CAN = 0x18; /* Cancel */
    protected final byte SUB = 0x1A; /* Substitute */

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

    public boolean sendFirmware(InputStream is, int length) throws HubResponseException, IOException
    {
        boolean success = false;
        //Start the ReadThread
        ReadThread readThread = new ReadThread();
        readThread.start();

        //We have to send some data to the hub to prepare it for the firmware
        String beforeFirmware = String.format("S20002174,%08X#", length);
        String afterFirmware = "W20000840,00000002#W20000848,20002174#W2000084C,00018550" +
                                "#W20000850,00000000#G20000800#";

        send(beforeFirmware);

        //We need to wait for a response
        mLastReceipt = NUL;
        if(awaitAcknowlegement((byte)'C') == 'C')
        {
            mBytesSent = 0;
            //Ok, we can proceed
            processFirmware(is);

            //And then send the command for the hub to restart
            send(afterFirmware);

            //If we've not thrown an exception then...
            success = true;
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
        byte[] buffer = new byte[DATA_SIZE_128];
        while(len > 0)
        {
            len = is.read(buffer, mBytesSent, DATA_SIZE_128);

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
        int crc = calcCrc(sendBuffer, 3, DATA_SIZE_128);
        sendBuffer[131] = (byte) (crc >> 8);    //MSB
        sendBuffer[132] = (byte) (crc & 0xFF);  //LSB

        //Send the data to the hub
        sendData(sendBuffer, PACKET_SIZE_XMODEM_CRC);
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
        Log.d(TAG, HexDump.dumpHexString(data, 0, data.length));
        mBytesSent += mHub.write(data, length);
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
                        || usbdata[i] == 0x43)
                        {
                            mLastReceipt = usbdata[i];
                            Log.d(TAG, String.format("Got response: 0x%02X", usbdata[i]));
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
