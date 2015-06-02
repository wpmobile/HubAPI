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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import com.worldpay.hub.printer.PrinterQueue;
import com.worldpay.hub.printer.commands.FeedPaper;
import com.worldpay.hub.printer.commands.Flush;
import com.worldpay.hub.printer.commands.GetStatus;
import com.worldpay.hub.printer.commands.OpenDrawer;
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
    protected final static int MAX_DATASIZE  = 32000; //

    protected final static int CHUNKSIZE = 2048; //Max number of bytes to send

    public MePOS(UsbSerialPort port, UsbManager manager)
    {
        mPort = port;
        mManager = manager;
    }

    /***
     * Returns the serial number of the MePOS device as an int.
     * @return int representation of the serial number
     * @throws MePOSResponseException
     */
    public int getSerialNumber() throws MePOSResponseException, IOException
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

    /***
     * Gets the currently configured datetime from the hub
     * @return Date with the hub date
     * @throws MePOSResponseException
     */
    public Date getDateTime() throws MePOSResponseException, IOException
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

    /***
     * Sets the datetime on the hub
     * @param date The datetime to set
     * @return The date the hub has been set to
     * @throws MePOSResponseException
     */
    public Date setDateTime(Date date) throws MePOSResponseException, IOException
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

    /***
     * Queries the MePOS Hub for a complex system info object containing several properties
     * (including serial number)
     * @return A populated System Information object.  Some values may be empty or null.
     * @throws MePOSResponseException
     */
    public SystemInformation getSystemInformation() throws MePOSResponseException, IOException
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

    /**
     * Sets a sub set of system information values (e.g. serial number(
     * @param systemInformation
     * @return The values which have been set, as a SystemInformation object
     * @throws MePOSResponseException
     */
    public SystemInformation setSystemInformation(SystemInformation systemInformation) throws MePOSResponseException, IOException
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

    /***
     * Sends a message to the hub to test connectivity.
     * @param message
     * @return A string, moving each character up by one value in the ASCII table.  A message of
     * 'hal' will return 'ibm'
     * @throws MePOSResponseException
     */
    public String ping(String message) throws MePOSResponseException, IOException
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

    /**
     * Sends raw data to the hub, usually so that it can be routed elsewhere.  Check the address you
     * send raw data to.  Often used to send data to the printer
     * @param data
     * @return true if the data has been received successfully by the MePOS device
     * @throws MePOSResponseException
     */
    public boolean sendRawData(byte[] data) throws MePOSResponseException, IOException
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

    /**
     * Returns the version number of the firmware on the hub.
     * @return String with the version in the form major.minor.revision e.g. 1.0.0
     * @throws MePOSResponseException
     */
    public String getVersion() throws MePOSResponseException, IOException
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

    /**
     * Issues a reset command to the MePOS.  This can be used to change the operating mode of the
     * MePOS device, and should be used with care
     * @throws MePOSResponseException
     */
    public void reset() throws MePOSResponseException, IOException
    {
        executeCommand(new Reset());
    }

    /**
     * Process the printer queue and sends the commands to the printer
     * @param queue
     * @throws MePOSResponseException
     */
    public void print(PrinterQueue queue) throws MePOSResponseException, IOException
    {
        PrinterCommand nextCommand = queue.getNextCommand();
        ByteBuffer bb = ByteBuffer.allocate(MAX_DATASIZE);  //Maximum data size.  This is different
                                                            //to the max frame size, and slicing is
                                                            //done later
        while(nextCommand != null)
        {
            //Queue the command
            if((bb.position() + nextCommand.getData().length) > MAX_DATASIZE)
            {
                //We've not got space for this yet
                //we have to execute
                flushToPrinter(bb);
            }

            bb.put(nextCommand.getData());
            if(nextCommand.getDelay() > 0)
            {
                //we have to execute
                flushToPrinter(bb);
                try
                {
                    Thread.sleep(nextCommand.getDelay());
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            //Move on to the next one
            nextCommand = queue.getNextCommand();
        }

        //Check to see if we've got anything buffered, but not printed
        if(bb.position() > 0)
        {
            flushToPrinter(bb);
        }
    }

    private void flushToPrinter(ByteBuffer bb) throws MePOSResponseException, IOException
    {
        byte[] buffer = new byte[bb.position()];
        System.arraycopy(bb.array(), 0, buffer, 0, bb.position());

        //Split the data into chunks suitable for the hub
        //Theorectically, this is 16Kb, but we've found a 2Kb limit in practice
        int offset = 0;
        while(offset < buffer.length)
        {
            int len = CHUNKSIZE;
            if((buffer.length - offset) < CHUNKSIZE)
                len = buffer.length - offset;
            byte[] sendBuffer = new byte[len];

            //Copy this chunk to the send buffer
            System.arraycopy(buffer, offset, sendBuffer, 0, len);

            //Send this buffer
            executeCommand(new RawData(sendBuffer), PRINTER_ADDRESS, 0 /* no timeout */);

            offset += len;
        }
        bb.clear();
    }
    /**
     * Issues a command to the printer to open the cash drawer
     * @throws MePOSResponseException
     */
    public void openCashDrawer() throws MePOSResponseException, IOException
    {
        executeCommand(new RawData(new OpenDrawer().getData()),
                        PRINTER_ADDRESS,
                        0 /* no timeout */);
    }

    /**
     * Instructs the printer to feed n lines.  This also flushes the printer buffer.
     * @param lines
     * @throws MePOSResponseException
     */
    public void printerFeed(int lines) throws MePOSResponseException, IOException
    {
        executeCommand(new RawData(new FeedPaper(lines).getData()),
                        PRINTER_ADDRESS,
                        0 /* no timeout */);

        executeCommand(new RawData(new Flush().getData()),
                        PRINTER_ADDRESS,
                        0 /* no timeout */);
    }

    /**
     * Check the printer to see if there is any paper left
     * @return true if the printer has paper
     */
    public boolean hasPaper() throws MePOSResponseException, IOException
    {
        ArrayList<Command> response = executeCommand(new RawData(new GetStatus(GetStatus.STATUS_PRINTER).getData()),
                                                    PRINTER_ADDRESS,
                                                    0 /* no timeout */);
        RawData raw = null;

        boolean hasPaper = true;

        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'N')
            {
                if(command.getCommandData().length > 0)
                {
                    //Get the first byte and test the flag
                    byte responseStatus = command.getCommandData()[0];
                    //0x00 means that it has paper, 0x01 means that it is empty
                    hasPaper = !((responseStatus & 0x01) == 0x01);
                }
                else
                {
                    throw new MePOSResponseException(command);
                }
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return hasPaper;
    }

    /**
     * Asks the printer to check if the cash drawer is open
     * @return true if the drawer is open
     * @throws MePOSResponseException
     */
    public boolean isCashDrawerOpen() throws MePOSResponseException, IOException
    {
        ArrayList<Command> response = executeCommand(new RawData(new GetStatus(GetStatus.STATUS_CASH_DRAWER).getData()),
                                                    PRINTER_ADDRESS,
                                                    0 /* no timeout */);
        RawData raw = null;

        boolean drawerOpen = true;

        for(Command command : response)
        {
            if (command != null && command.getCommandCode() == 'N')
            {
                if(command.getCommandData().length > 0)
                {
                    //Get the first byte and test the flag
                    byte responseStatus = command.getCommandData()[0];
                    //0x00 means that the drawer is open, 0x01 means that it is closed
                    drawerOpen = !((responseStatus & 0x01) == 0x01);
                }
                else
                {
                    throw new MePOSResponseException(command);
                }
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new MePOSResponseException(command);
            }
        }

        return drawerOpen;
    }

    /**
     * Executes the command to the default address, and with the default timeout
     * @param c is the command to be executed
     * @return An ArrayList of Commands as the response from the MePOS device
     * @throws MePOSResponseException
     */
    protected ArrayList<Command> executeCommand(Command c) throws MePOSResponseException, IOException
    {
        //Default target is the hub management interface
        return executeCommand(c, HUB_ADDRESS, DEFAULT_TIMEOUT);
    }

    /**
     * Executes the command to the provided address, and with the provided timeout
     * @param c is the command to be executed
     * @return An ArrayList of Commands as the response from the MePOS device
     * @throws MePOSResponseException
     */
    protected ArrayList<Command> executeCommand(Command c, byte target, int timeout) throws MePOSResponseException, IOException
    {
        ArrayList<Command> responses = new ArrayList<Command>();

        UsbDeviceConnection connection = mManager.openDevice(mPort.getDriver().getDevice());

        //Log.d(TAG, "Link established");

        if (connection == null) {
            IOException e = new IOException();
            Log.e(TAG, "Opening device failed", e);
            throw e;
        }

        mPort.open(connection);
        Frame[] frames = Frame.getFrames(new Envelope(c, TABLET_ADDRESS, target));

        byte[] dataToSend = frames[0].getFrameData();

        //Log.d(TAG, HexDump.dumpHexString(dataToSend, 0, Math.min(32, dataToSend.length)));
        int writeLen = mPort.write(dataToSend, timeout);
        //Log.d(TAG, String.format("Wrote %d bytes", writeLen));

        responses  = readResponses(timeout);

        try
        {
            mPort.close();
        } catch (IOException e2)
        {
            // Ignore.
        }

        return responses;
    }

    /**
     * Reads responses from the USB bus.  It will wait for at least the timeout period for the first
     * response, but does not wait for subsequent responses.
     * @param timeout in milliseconds
     * @return An ArrayList of Commands as the response from the MePOS device
     * @throws IOException
     * @throws MePOSResponseException
     */
    protected ArrayList<Command> readResponses(int timeout) throws IOException, MePOSResponseException
    {
        ArrayList<Command> responses = new ArrayList<Command>();
        byte[] readBuffer = new byte[MAX_FRAMESIZE];
        int bytesRead = 1;
        boolean waitingForResponse = true;
        long startTime = System.currentTimeMillis();

        while(waitingForResponse  || bytesRead > 0)
        {
            bytesRead  = mPort.read(readBuffer, timeout);
            if(waitingForResponse && bytesRead > 0)
            {
                waitingForResponse = false;
                //Log.d(TAG, "Read some data, not waiting for further responses");
            }

            if(waitingForResponse && ((startTime + timeout) < System.currentTimeMillis()))
            {
                waitingForResponse = false;
                //Log.d(TAG, "Timeout, not waiting for further responses");
            }

            //Log.d(TAG, String.format("Read %d bytes", bytesRead));
            //Log.d(TAG, HexDump.dumpHexString(readBuffer, 0, Math.min(32, readBuffer.length)));
            if(bytesRead > 0)
            {
                //Log.d(TAG, "********************************************");
                //Log.d(TAG, "*         HAPPY DAYS ARE HERE AGAIN        *");
                //Log.d(TAG, "********************************************");

                //Just take the response bytes
                byte[] responseBytes = new byte[bytesRead];
                System.arraycopy(readBuffer, 0, responseBytes, 0, bytesRead);
                //Deserialise response into a command
                Command c = deserialise(responseBytes);
                responses.add(c);
            }
        }

        return responses;
    }

    /**
     * Converts a byte[] from the MePOS hub into a usable Command object
     * @param response the raw data
     * @return The completed Command object
     * @throws MePOSResponseException
     */
    protected Command deserialise(byte[] response) throws MePOSResponseException
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

            throw new MePOSResponseException("Cannot deserialise command", e);
        }
        return c;
    }
}
