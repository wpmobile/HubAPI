package com.worldpay.hub;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.worldpay.hub.commands.Command;
import com.worldpay.hub.commands.GetClock;
import com.worldpay.hub.commands.GetSerialNumber;
import com.worldpay.hub.commands.GetSystemInformation;
import com.worldpay.hub.commands.Ping;
import com.worldpay.hub.commands.RawData;
import com.worldpay.hub.commands.Reset;
import com.worldpay.hub.commands.SetClock;
import com.worldpay.hub.commands.SetSystemInformation;
import com.worldpay.hub.commands.SystemInformation;
import com.worldpay.hub.link.Envelope;
import com.worldpay.hub.link.Frame;

import java.io.IOException;
import java.util.Date;

import usbserial.driver.UsbSerialPort;
import usbserial.util.HexDump;

public class MePOS
{
    protected final static String TAG = "MePOS";
    protected UsbSerialPort mPort;
    protected UsbManager mManager;

    protected final static byte HUB_ADDRESS = 0x18;
    protected final static byte TABLET_ADDRESS = 0x30;

    protected final static int DEFAULT_TIMEOUT = 1000; //1 second
    protected final static int MAX_FRAMESIZE = 16389; //

    public MePOS(UsbSerialPort port, UsbManager manager)
    {
        mPort = port;
        mManager = manager;
    }

    public int getSerialNumber() throws MePOSResponseException
    {
        Command snum = executeCommand(new GetSerialNumber());
        int serialNumber = 0;
        if(snum != null && snum.getCommandCode() == 'y')
        {
            GetSerialNumber sn = (GetSerialNumber)snum;
            serialNumber = sn.getSerialNumber();
        }
        else if(snum != null && snum.getCommandCode() == 'E')
        {
            throw new MePOSResponseException(snum);
        }

        return serialNumber;
    }

    public Date getDateTime() throws MePOSResponseException
    {
        Command command = executeCommand(new GetClock());
        Date date = null;
        if(command != null && command.getCommandCode() == 'k')
        {
            GetClock gc = (GetClock)command;
            date = gc.getDate();
        }
        else if(command != null && command.getCommandCode() == 'E')
        {
            throw new MePOSResponseException(command);
        }

        return date;
    }

    public Date setDateTime(Date date) throws MePOSResponseException
    {
        Command command = executeCommand(new SetClock(date));
        Date responseDate = null;
        if(command != null && command.getCommandCode() == 'K')
        {
            SetClock sc = (SetClock)command;
            responseDate = sc.getDate();
        }
        else if(command != null && command.getCommandCode() == 'E')
        {
            throw new MePOSResponseException(command);
        }

        return responseDate;
    }

    public SystemInformation getSystemInformation() throws MePOSResponseException
    {
        Command command = executeCommand(new GetSerialNumber());
        SystemInformation si = null;
        if(command != null && command.getCommandCode() == 'z')
        {
            GetSystemInformation getSI = (GetSystemInformation)command;
            si = getSI.getSystemInformation();
        }
        else if(command != null && command.getCommandCode() == 'E')
        {
            throw new MePOSResponseException(command);
        }

        return si;
    }

    public SystemInformation setSystemInformation(SystemInformation systemInformation) throws MePOSResponseException
    {
        Command command = executeCommand(new SetSystemInformation(systemInformation));
        SystemInformation si = null;

        if(command != null && command.getCommandCode() == 'z')
        {
            GetSystemInformation getSI = (GetSystemInformation)command;
            si = getSI.getSystemInformation();
        }
        else if(command != null && command.getCommandCode() == 'E')
        {
            throw new MePOSResponseException(command);
        }

        return si;
    }

    public String ping(String message) throws MePOSResponseException
    {
        Command command = executeCommand(new Ping(message));

        String response = "";
        if(command != null && command.getCommandCode() == 'p')
        {
            Ping pong = (Ping)command;
            response = pong.getResponseMessage();
        }
        else if(command != null && command.getCommandCode() == 'E')
        {
            throw new MePOSResponseException(command);
        }

        return response;
    }

    public boolean sendRawData(byte[] data) throws MePOSResponseException
    {
        Command command = executeCommand(new RawData(data));
        boolean success = false;

        if(command != null && command.getCommandCode() == 'N')
        {
            success = true;
        }
        else if(command != null && command.getCommandCode() == 'E')
        {
            throw new MePOSResponseException(command);
        }

        return success;
    }

    public void reset() throws MePOSResponseException
    {
        executeCommand(new Reset());
    }

    protected Command executeCommand(Command c)
    {
        UsbDeviceConnection connection = mManager.openDevice(mPort.getDriver().getDevice());

        Log.d(TAG, "Link established");

        if (connection == null) {
            Log.d(TAG, "Opening device failed");
        }

        try
        {
            mPort.open(connection);
            Frame[] frames = Frame.getFrames(new Envelope(c, HUB_ADDRESS, TABLET_ADDRESS));

            //TODO:  are there commands which will require more than 1 frame?
            byte[] dataToSend = frames[0].getFrameData();

            Log.d(TAG, HexDump.dumpHexString(dataToSend, 0, Math.min(32, dataToSend.length)));
            int writeLen = mPort.write(dataToSend, DEFAULT_TIMEOUT);
            Log.d(TAG, String.format("Wrote %d bytes", writeLen));
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
            mPort = null;
        }

        try
        {
            mPort.close();
        } catch (IOException e2)
        {
            // Ignore.
        }

        return readResponse();
    }

    protected Command readResponse()
    {
        byte[] readBuffer = new byte[MAX_FRAMESIZE];
        int bytesRead = 0;

        try
        {
            bytesRead = mPort.read(readBuffer, DEFAULT_TIMEOUT);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.d(TAG, String.format("Read %d bytes", bytesRead));
        Log.d(TAG, HexDump.dumpHexString(readBuffer, 0, Math.min(32, readBuffer.length)));

        Command c = null;

        if (bytesRead > 0)
        {
            Log.d(TAG, "********************************************");
            Log.d(TAG, "*         HAPPY DAYS ARE HERE AGAIN        *");
            Log.d(TAG, "********************************************");

            byte[] responseBuffer = new byte[bytesRead];
            System.arraycopy(readBuffer, 0, responseBuffer, 0, bytesRead);

            c = deserialise(responseBuffer);
        }

        return c;
    }

    protected Command deserialise(byte[] response)
    {
        //Deserialise the response
        ResponseParser parser = new ResponseParser();
        Envelope env = parser.process(response);

        Command c = Command.getCommand(env.getCommandCode());
        c.setCommandData(env.getData());

        return c;
    }
}
