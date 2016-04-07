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
import com.worldpay.hub.Logger;
import com.worldpay.hub.MePOS.firmware.Command;
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
    protected final int TIMEOUT = 5000;
    protected final byte NUL = 0;    /* Null */
    protected final byte SOH = 1;    /* Start Of Header */
    protected final byte EOT = 4;    /* End Of Transmission */
    protected final byte ACK = 6;    /* ACKnowlege */
    protected final byte NAK = 0x15; /* Negative AcKnowlege */
    protected final byte CAN = 0x18; /* Cancel */
    protected final byte SUB = 0x1A; /* Substitute */
    protected final byte CHAR_C = 0x43; /* The C symbol */
    protected final byte PROMPT = 0x3E; /* The > symbol */
    protected final byte HASH = 0x23; /* The # symbol */

    protected ReadThread readThread;

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

        //Start the ReadThread
        readThread = new ReadThread();
        readThread.start();
    }

    public int bytesSent()
    {
        return mBytesSent;
    }

    public void close()
    {
        readThread.cancel();
    }

    /***
     * Send firmware but specialised to send the  magic number for the loader.
     */
    public boolean sendLoader(InputStream is, int length) throws HubResponseException, IOException
    {
        boolean success = false;

        //We have to send some data to the hub to prepare it for the firmware
        Command beforeFirmware = new Command(Command.TYPE_SEND_COMMAND, 0x20000800, length);
        Command writeToFlash = new Command(Command.TYPE_WRITE_WORD, 0x20000840, 0x00);
        Command setComType = new Command(Command.TYPE_WRITE_WORD, 0x20000848, 0x01);
        Command setTraceLevel = new Command(Command.TYPE_WRITE_WORD, 0x2000084C, 0x01);
        Command selectRAMBank = new Command(Command.TYPE_WRITE_WORD, 0x20000850, 0x00);
        Command executeCodeFromAddress = new Command(Command.TYPE_GO_COMMAND, 0x20000800);

        Logger.d(TAG, "Sending start signal");
        send(beforeFirmware, CHAR_C);

        mBytesSent = 0;
        //Ok, we can proceed
        byte ack = processFirmware(is, length);

        if(ack == CAN || ack == PROMPT)
        {
            //Abort
            throw new HubResponseException("XModem data transfer failed");
        }

        //And then send the command for the hub to restart
        send(writeToFlash, PROMPT);
        send(setComType, PROMPT);
        send(setTraceLevel, PROMPT);
        send(selectRAMBank, PROMPT);
        send(executeCodeFromAddress, NUL);

        //If we've not thrown an exception then...
        success = true;

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

        //We have to send some data to the hub to prepare it for the firmware
        Command beforeFirmware = new Command(Command.TYPE_SEND_COMMAND, 0x20002174, length);
        Command writeToFlash = new Command(Command.TYPE_WRITE_WORD, 0x20000840, 0x02);
        Command initialBufferAddress = new Command(Command.TYPE_WRITE_WORD, 0x20000848, 0x20002174);
        Command firmwareLength = new Command(Command.TYPE_WRITE_WORD, 0x2000084C, length);
        Command offsetCommand = new Command(Command.TYPE_WRITE_WORD, 0x20000850, offset);
        Command executeCodeFromAddress = new Command(Command.TYPE_GO_COMMAND, 0x20000800);

        Logger.d(TAG, "Sending start signal");
        send(beforeFirmware, CHAR_C);

        mBytesSent = 0;
        //Ok, we can proceed
        byte ack = processFirmware(is, length);

        if(ack == CAN || ack == PROMPT)
        {
            //Abort
            throw new HubResponseException("XModem data transfer failed");
        }

        //And then send the command for the hub to restart
        send(writeToFlash, PROMPT);
        send(initialBufferAddress, PROMPT);
        send(firmwareLength, PROMPT);
        send(offsetCommand, PROMPT);
        send(executeCodeFromAddress, PROMPT);

        //If we've not thrown an exception then...
        success = true;

        return success;
    }

    /***
     * Commits firmware that has already been loaded.
     */
    public void commitFirmware()
    {
        //There are apparently two ways to do this...
        //send("W20000840,00000006#W20000844,00000000#W20000848,00000001#W2000084C,00000001#");
        //Lets stick with these magic numbers for today.
       send("#\r\nW400E0A04,5A00010B#");
    }

    public void send(Command command, byte expected) throws HubResponseException
    {
        byte data[] = new byte[0];
        try
        {
            data = command.toString().getBytes("ISO-8859-1");
            Logger.d(TAG, String.format("Writing %d bytes", data.length));
            String writeData = HexDump.dumpHexString(data, 0, data.length);
            Logger.d(TAG, writeData);

            mLastReceipt = NUL;
            mHub.write(data, data.length);

            //Wait for a prompt response before continuing

            if(expected != NUL && awaitAcknowlegement(expected) != expected)
            {
                throw new HubResponseException("Could not send hub firmware command");
            }
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
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
            Logger.d(TAG, String.format("Writing %d bytes", data.length));
            String writeData = HexDump.dumpHexString(data, 0, data.length);
            Logger.d(TAG, "\n" + writeData);
            mHub.write(data, data.length);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    protected byte processFirmware(InputStream is, int length) throws HubResponseException, IOException
    {
        int sequenceNumber = 1;
        byte acknowledgement = 0x00;
        mBytesSent = 0;

        //Read all the data in advance
        byte[] dataToSend = new byte[length];
        is.read(dataToSend, 0, length);

        while(mBytesSent < length)
        {
            byte[] buffer = new byte[DATA_SIZE_128];
            //Read in the next 128 bytes
            int readLen = DATA_SIZE_128;

            //Check to see if there is at least DATA_SIZE_128 bytes left between length (the total length)
            //and mBytesSent (the number of bytes read)
            Logger.d(TAG, String.format("%d byte remain", length - mBytesSent));
            if(length - mBytesSent < DATA_SIZE_128)
            {
                //we don't have a full 128 bytes to read
                readLen = length - mBytesSent;
            }

            //copy the packet to send
            System.arraycopy(dataToSend, mBytesSent, buffer, 0, readLen);
            //Track how many bytes we've read
            mBytesSent += readLen;

            //check that this is a full packet
            if(readLen < DATA_SIZE_128)
                for(int i = readLen; i < DATA_SIZE_128; i++)
                    buffer[i] = SUB; //Pad out the remaining space in the envelope

            int retries = 0;
            boolean acknowledged = false;
            //There is an assumption that the ReadThread is running simultaneously
            while(retries < MAX_RETRIES && !acknowledged)
            {
                mLastReceipt = NUL;
                sendPacket(buffer, sequenceNumber++);
                retries++;

                acknowledgement = awaitAcknowlegement(ACK);
                acknowledged = awaitAcknowlegement(ACK) == ACK;

                if (acknowledgement == CAN || acknowledgement == PROMPT)
                {
                    //If we get a CAN signal from the hub we should abort and restart.
                    //The prompt signal *might* mean this if we are in the middle of
                    //and XModem send.

                    return acknowledgement;
                }
            }

            if(retries == MAX_RETRIES)
            {
                //Failed to send
                throw new HubResponseException("Send firmware failed");
            }

            Logger.d(TAG, String.format("Sent 0x%X bytes of 0x%X", mBytesSent, length));
        }

        //Send end of transmission marker
        sendData(new byte[] { EOT }, 1);

        //Wait for a prompt before sending the upload commands.
        awaitAcknowlegement(PROMPT);

        return acknowledgement;
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


        //Add the CRC
       /* int crc = calcCrc(sendBuffer, 3, DATA_SIZE_128);
        sendBuffer[131] = (byte) (crc >> 8);    //MSB
        sendBuffer[132] = (byte) (crc & 0xFF);  //LSB*/
        byte[] crc = calCrc(sendBuffer, 3, DATA_SIZE_128);
        sendBuffer[131] = crc[0];    //MSB
        sendBuffer[132] = crc[1];    //LSB
       // Logger.d(TAG, String.format("Checksum %02X%02X", sendBuffer[131], sendBuffer[132]));

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
        //Logger.d(TAG, String.format("Writing %d bytes", length));
        String writeData = HexDump.dumpHexString(data, 0, data.length);
        Logger.d(TAG, "\n" + writeData);
        mHub.write(data, length);
    }

    class ReadThread extends Thread
    {
        final int USB_DATA_BUFFER = 8192;
        boolean running = false;

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
            running = true;

            while (!Thread.currentThread().isInterrupted() && running)
            {
                try
                {
                    Thread.sleep(100);
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
                //Logger.d(TAG,"iavailable:" + readcount);
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
                        //Logger.d(TAG, "RT usbdata[" + i + "]:(" + usbdata[i] + ")");

                        if(usbdata[i] == NAK
                        || usbdata[i] == ACK
                        || usbdata[i] == HASH
                        || usbdata[i] == CAN
                        || usbdata[i] == PROMPT
                        || usbdata[i] == CHAR_C)
                        {
                            mLastReceipt = usbdata[i];
                            Logger.d(TAG, String.format("Got response: 0x%02X", usbdata[i]));
                            if(usbdata[i] == CAN)
                                Logger.d(TAG, "Cancel signal received");
                        }
                    }
                    //Ignore other response bytes
                }
                else
                {
                   // Logger.d(TAG, "No response from board");
                }
            }
            Logger.d(TAG, "Read thread interrupted");
        }


        public final void cancel()
        {
            running = false;
            interrupt();
        }
    }
}
