package com.worldpay.hub;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.worldpay.hub.commands.Command;
import com.worldpay.hub.commands.GetClock;
import com.worldpay.hub.commands.GetSerialNumber;
import com.worldpay.hub.commands.GetSystemInformation;
import com.worldpay.hub.commands.GetVersion;
import com.worldpay.hub.commands.Ping;
import com.worldpay.hub.commands.RawData;
import com.worldpay.hub.commands.Reset;
import com.worldpay.hub.commands.SetClock;
import com.worldpay.hub.commands.SetSystemInformation;
import com.worldpay.hub.commands.SystemInformation;
import com.worldpay.hub.link.Envelope;
import com.worldpay.hub.link.Frame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.worldpay.hub.printer.PrinterQueue;
import com.worldpay.hub.printer.commands.PrinterCommand;
import com.worldpay.hub.usbserial.driver.UsbSerialPort;
import com.worldpay.hub.usbserial.util.HexDump;

public class MePOS
{
    protected final static String TAG = "MePOS";
    protected UsbSerialPort mPort;
    protected UsbManager mManager;

    protected final static byte HUB_ADDRESS = 0x18;
    protected final static byte PRINTER_ADDRESS = 0x12;
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
        ArrayList<Command> response = executeCommand(new GetSerialNumber());

        int serialNumber = 0;
        for(Command snum : response)
        {
            if (snum != null && snum.getCommandCode() == 'y')
            {
                GetSerialNumber sn = (GetSerialNumber) snum;
                serialNumber = sn.getSerialNumber();
            } else if (snum != null && snum.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(snum);
            }
        }

        return serialNumber;
    }

    public Date getDateTime() throws MePOSResponseException
    {
        ArrayList<Command> response = executeCommand(new GetClock());
        Date date = null;

        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'k')
            {
                GetClock gc = (GetClock) command;
                date = gc.getDate();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return date;
    }

    public Date setDateTime(Date date) throws MePOSResponseException
    {
        ArrayList<Command> response = executeCommand(new SetClock(date));
        Date responseDate = null;
        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'K')
            {
                SetClock sc = (SetClock) command;
                responseDate = sc.getDate();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return responseDate;
    }

    public SystemInformation getSystemInformation() throws MePOSResponseException
    {
        ArrayList<Command> response = executeCommand(new GetSystemInformation());
        SystemInformation si = null;
        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'z')
            {
                GetSystemInformation getSI = (GetSystemInformation) command;
                si = getSI.getSystemInformation();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return si;
    }

    public SystemInformation setSystemInformation(SystemInformation systemInformation) throws MePOSResponseException
    {
        ArrayList<Command> response = executeCommand(new SetSystemInformation(systemInformation));
        SystemInformation si = null;

        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'z')
            {
                GetSystemInformation getSI = (GetSystemInformation) command;
                si = getSI.getSystemInformation();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return si;
    }

    public String ping(String message) throws MePOSResponseException
    {
        ArrayList<Command> response = executeCommand(new Ping(message));

        String responseText = "";
        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'p')
            {
                Ping pong = (Ping) command;
                responseText = pong.getResponseMessage();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return responseText;
    }

    public boolean sendRawData(byte[] data) throws MePOSResponseException
    {
        ArrayList<Command> response = executeCommand(new RawData(data));
        boolean success = false;

        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'N')
            {
                success = true;
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return success;
    }

    public String getVersion() throws MePOSResponseException
    {
        ArrayList<Command> response = executeCommand(new GetVersion());
        String protocolVersion = "";

        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'v')
            {
                GetVersion version = (GetVersion) command;
                protocolVersion = version.getProtocolVersion();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return protocolVersion;
    }

    public void reset() throws MePOSResponseException
    {
        executeCommand(new Reset());
    }

    public void print(PrinterQueue queue) throws MePOSResponseException
    {
        PrinterCommand nextCommand = queue.getNextCommand();
        while(nextCommand != null)
        {
            ArrayList<Command> response  = executeCommand(new RawData(nextCommand.getData()),
                                                            PRINTER_ADDRESS,
                                                            0 /* no timeout */);

            //Delay processing if necessary
            if(nextCommand.getDelay() > 0)
                try
                {
                    Thread.sleep(nextCommand.getDelay());
                } catch (InterruptedException e)
                {
                    //e.printStackTrace();
                }

            //Move on to the next one
            nextCommand = queue.getNextCommand();
        }
    }

    protected ArrayList<Command> executeCommand(Command c)
    {
        //Default target is the hub management interface
        return executeCommand(c, HUB_ADDRESS, DEFAULT_TIMEOUT);
    }

    protected ArrayList<Command> executeCommand(Command c, byte target, int timeout)
    {
        ArrayList<Command> responses = new ArrayList<Command>();

        UsbDeviceConnection connection = mManager.openDevice(mPort.getDriver().getDevice());

        Log.d(TAG, "Link established");

        if (connection == null) {
            Log.d(TAG, "Opening device failed");
        }

        try
        {
            mPort.open(connection);
            Frame[] frames = Frame.getFrames(new Envelope(c, TABLET_ADDRESS, target));

            //TODO:  are there commands which will require more than 1 frame?
            byte[] dataToSend = frames[0].getFrameData();

            Log.d(TAG, HexDump.dumpHexString(dataToSend, 0, Math.min(32, dataToSend.length)));
            int writeLen = mPort.write(dataToSend, timeout);
            Log.d(TAG, String.format("Wrote %d bytes", writeLen));

            responses  = readResponses(timeout);
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

        return responses;
    }

    protected ArrayList<Command> readResponses(int timeout)
    {
        ArrayList<Command> responses = new ArrayList<Command>();
        byte[] readBuffer = new byte[MAX_FRAMESIZE];
        int bytesRead = 1;
        boolean waitingForResponse = true;
        long startTime = System.currentTimeMillis();

        try
        {
            while(waitingForResponse  || bytesRead > 0)
            {
                bytesRead  = mPort.read(readBuffer, timeout);
                if(waitingForResponse && bytesRead > 0)
                {
                    waitingForResponse = false;
                    Log.d(TAG, "Read some data, not waiting for further responses");
                }

                if(waitingForResponse && ((startTime + timeout) < System.currentTimeMillis()))
                {
                    waitingForResponse = false;
                    Log.d(TAG, "Timeout, not waiting for further responses");
                }

                Log.d(TAG, String.format("Read %d bytes", bytesRead));
                Log.d(TAG, HexDump.dumpHexString(readBuffer, 0, Math.min(32, readBuffer.length)));
                if(bytesRead > 0)
                {
                    Log.d(TAG, "********************************************");
                    Log.d(TAG, "*         HAPPY DAYS ARE HERE AGAIN        *");
                    Log.d(TAG, "********************************************");

                    //Just take the response bytes
                    byte[] responseBytes = new byte[bytesRead];
                    System.arraycopy(readBuffer, 0, responseBytes, 0, bytesRead);
                    //Deserialise response into a command
                    Command c = deserialise(responseBytes);
                    responses.add(c);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return responses;
    }

    protected Command deserialise(byte[] response)
    {
        Command c = null;
        try
        {
            //Deserialise the response
            ResponseParser parser = new ResponseParser();
            Envelope env = parser.process(response);
            if(env != null)
                c = env.getCommand();
        }
        catch(Exception e)
        {
            Log.d("MePOS", "Fatal error");
            Log.d("MePOS", "Could not deserialise command");
            Log.d("MePOS", HexDump.dumpHexString(response, 0, Math.min(32, response.length)));
        }
        return c;
    }
}
