package com.worldpay.hub.MePOS;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.worldpay.hub.Hub;
import com.worldpay.hub.Logger;
import com.worldpay.hub.MePOS.commands.Command;
import com.worldpay.hub.MePOS.commands.GetClock;
import com.worldpay.hub.MePOS.commands.GetSerialNumber;
import com.worldpay.hub.MePOS.commands.GetSystemInformation;
import com.worldpay.hub.MePOS.commands.GetVersion;
import com.worldpay.hub.MePOS.commands.Ping;
import com.worldpay.hub.MePOS.commands.RawData;
import com.worldpay.hub.MePOS.commands.Reset;
import com.worldpay.hub.MePOS.commands.SetClock;
import com.worldpay.hub.MePOS.commands.SetIO;
import com.worldpay.hub.MePOS.commands.SetPort;
import com.worldpay.hub.MePOS.commands.SetSystemInformation;
import com.worldpay.hub.MePOS.commands.SystemInformation;
import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.MePOS.printer.commands.BatchedPrinterQueueProcessor;
import com.worldpay.hub.MePOS.printer.commands.PassthroughPrinterQueueProcessor;
import com.worldpay.hub.MePOS.printer.commands.PrinterFlusher;
import com.worldpay.hub.MePOS.printer.commands.PrinterQueueProcessor;
import com.worldpay.hub.PrinterFactory;
import com.worldpay.hub.link.Envelope;
import com.worldpay.hub.link.Frame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import com.worldpay.hub.PrinterQueue;
import com.worldpay.hub.MePOS.printer.commands.FeedPaper;
import com.worldpay.hub.MePOS.printer.commands.GetStatus;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.usbserial.driver.UsbSerialPort;
import com.worldpay.hub.usbserial.util.HexDump;

//TODO:  This is far too large and monolithic - break it down into classes.

public class MePOSHub implements Hub, PrinterFlusher
{
    protected final static String TAG = "MePOS";
    protected UsbSerialPort mPort;
    protected UsbManager mManager;
    protected boolean mFlowControl = false;

    protected final static byte HUB_ADDRESS = 0x18;
    protected final static byte PRINTER_ADDRESS = 0x12;
    protected final static byte TABLET_ADDRESS = 0x30;
    protected final static byte CASHDRAWER_ADDRESS = 0x1C;
    protected final static byte IO_ADDRESS = 0x1B;

    protected final static int DEFAULT_TIMEOUT = 4000; //4 seconds
    protected final static int MAX_FRAMESIZE = 16389; //
    protected final static int MAX_DATASIZE  = 65536; //

    protected final static int CHUNKSIZE = 10 * 1024; //Max number of bytes to send
    //protected final static int CHUNKSIZE = 16000; //Max number of bytes to send

    protected final static int MAX_FAILURES = 1;

    //Diagnostic lights constants
    public static final int COLOR_RED      = 2;
    public static final int COLOR_GREEN    = 1;
    public static final int COLOR_BLUE     = 4;

    //The power values are the values of the green light in the MePOS documentation,
    //e.g 16  Diagnostic#5, Green 1 = on, 0 = off. This is the Printer LED

    public static final int DIAGNOSTIC_LIGHT_POWER      = 1 << 0;
    public static final int DIAGNOSTIC_LIGHT_NETWORK    = 1 << 2;
    public static final int DIAGNOSTIC_LIGHT_TABLET     = 1 << 4;
    public static final int DIAGNOSTIC_LIGHT_PED        = 1 << 6;
    public static final int COSMETIC_LIGHT              = 1 << 8;
    public static final int DIAGNOSTIC_LIGHT_PRINTER    = 1 << 16;
    public static final int DIAGNOSTIC_LIGHT_USB1       = 1 << 18;
    public static final int DIAGNOSTIC_LIGHT_USB2       = 1 << 20;

    public static final int STATE_ON        = 1;
    public static final int STATE_OFF       = 0;

    public static final int OTA_MODE = 1;
    public static boolean DEBUG_LOG_ENABLED = false;

    public MePOSHub(UsbSerialPort port, UsbManager manager)
    {
        mPort = port;
        mManager = manager;
        mFlowControl = true;
    }

    public MePOSHub(UsbSerialPort port, UsbManager manager, boolean flowcontrol)
    {
        mPort = port;
        mManager = manager;
        mFlowControl = flowcontrol;
    }

    @Override
    public void setFlowControl(boolean flowControl)
    {
        mFlowControl = flowControl;
    }

    /***
     * Returns the serial number of the MePOS device as an int.
     * @return int representation of the serial number
     * @throws HubResponseException
     */
    public int getSerialNumber() throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new GetSerialNumber());

        int serialNumber = 0;
        for(Envelope e : response)
        {
            Command snum = e.getCommand();
            if (snum != null && snum.getCommandCode() == 'y')
            {
                GetSerialNumber sn = (GetSerialNumber) snum;
                serialNumber = sn.getSerialNumber();
            } else if (snum != null && snum.getCommandCode() == 'E')
            {
                throw new HubResponseException(snum);
            }
        }

        return serialNumber;
    }

    /***
     * Gets the currently configured datetime from the hub
     * @return Date with the hub date
     * @throws HubResponseException
     */
    public Date getDateTime() throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new GetClock());
        Date date = null;

        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'k')
            {
                GetClock gc = (GetClock) command;
                date = gc.getDate();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return date;
    }

    /***
     * Sets the datetime on the hub
     * @param date The datetime to set
     * @return The date the hub has been set to
     * @throws HubResponseException
     */
    public Date setDateTime(Date date) throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new SetClock(date));
        Date responseDate = null;
        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'K')
            {
                SetClock sc = (SetClock) command;
                responseDate = sc.getDate();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return responseDate;
    }

    /***
     * Queries the MePOS Hub for a complex system info object containing several properties
     * (including serial number)
     * @return A populated System Information object.  Some values may be empty or null.
     * @throws HubResponseException
     */
    public SystemInformation getSystemInformation() throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new GetSystemInformation());
        SystemInformation si = null;
        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'z')
            {
                GetSystemInformation getSI = (GetSystemInformation) command;
                si = getSI.getSystemInformation();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return si;
    }

    /**
     * Sets a sub set of system information values (e.g. serial number(
     * @param systemInformation
     * @return The values which have been set, as a SystemInformation object
     * @throws HubResponseException
     */
    public SystemInformation setSystemInformation(SystemInformation systemInformation) throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new SetSystemInformation(systemInformation));
        SystemInformation si = null;

        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'z')
            {
                GetSystemInformation getSI = (GetSystemInformation) command;
                si = getSI.getSystemInformation();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return si;
    }

    /***
     * Sends a message to the hub to test connectivity.
     * @param message
     * @return A string, moving each character up by one value in the ASCII table.  A message of
     * 'hal' will return 'ibm'
     * @throws HubResponseException
     */
    public String ping(String message) throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new Ping(message));

        String responseText = "";
        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'p')
            {
                Ping pong = (Ping) command;
                responseText = pong.getResponseMessage();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return responseText;
    }

    /**
     * Sends raw data to the hub, usually so that it can be routed elsewhere.  Check the address you
     * send raw data to.  Often used to send data to the printer
     * @param data
     * @return true if the data has been received successfully by the MePOS device
     * @throws HubResponseException
     */
    public boolean sendRawData(byte[] data) throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new RawData(data));
        boolean success = false;

        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'N')
            {
                success = true;
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return success;
    }

    /**
     * Returns the version number of the firmware on the hub.
     * @return String with the version in the form major.minor.revision e.g. 1.0.0
     * @throws HubResponseException
     */
    public String getVersion() throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new GetVersion());
        String protocolVersion = "";

        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'v')
            {
                GetVersion version = (GetVersion) command;
                protocolVersion = version.getProtocolVersion();
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return protocolVersion;
    }

    /**
     * Issues a reset command to the MePOS.  This can be used to change the operating mode of the
     * MePOS device, and should be used with care
     * @throws HubResponseException
     */
    public void reset() throws HubResponseException, IOException
    {
        executeCommand(new Reset());
    }

    /**
     * Process the printer queue and sends the commands to the printer
     * @param queue
     * @throws HubResponseException
     */
    @Override
    public void print(PrinterQueue queue) throws HubResponseException, IOException
    {
        //Create the appropriate PrinterQueueProcessor based on the strategy
        PrinterQueueProcessor queueProcessor = null;

        //BatchedPrinterQueueProcessor implements a strategy of fewer, larger messages with flow control
        //queueProcessor = new BatchedPrinterQueueProcessor(MAX_DATASIZE);

        //PassthroughPrinterQueueProcessor implements a strategy of more, smaller messages with no flow control
        queueProcessor = new PassthroughPrinterQueueProcessor();

        //The queue processor needs to delegate the actual transmission of data. In this case, back to MePOSHub
        queueProcessor.setPrinterFlusher(this);

        //And now, process that queue.
        queueProcessor.print(queue);
    }

    /***
     * Open the cash drawer via the MePOS board.  This no longer operates through the printer
     * @throws HubResponseException
     * @throws IOException
     */
    public void openCashDrawer() throws HubResponseException, IOException
    {
        RawData openDrawer = new RawData(new byte[] {0x1B, 0x70, 0x48, 0x7F, 0x7F});
        executeCommand(openDrawer, CASHDRAWER_ADDRESS, DEFAULT_TIMEOUT);
    }

    /***
     * This is designed as a short cut to quickly turn the light off in 1 line.
     * @param light
     * @param state
     * @throws HubResponseException
     * @throws IOException
     */
    public void setDiagnosticLight(int light, int state) throws HubResponseException, IOException
    {
        if(light == COSMETIC_LIGHT)
        {
            setDiagnosticLight(COSMETIC_LIGHT, COLOR_RED, state);
            setDiagnosticLight(COSMETIC_LIGHT, COLOR_GREEN, state);
            setDiagnosticLight(COSMETIC_LIGHT, COLOR_BLUE, state);
        }
        else
        {
            setDiagnosticLight(light, COLOR_RED, state);
            setDiagnosticLight(light, COLOR_GREEN, state);
        }
    }

    @Override
    public void setDiagnosticLight(int light, int colour, int state) throws HubResponseException, IOException
    {
        SetIO io = new SetIO();

        //Cool huh?  Mulitplying the constants gives you the correct value for MePOS
        io.setValue(light * colour, state == STATE_ON);

        executeCommand(io, IO_ADDRESS, DEFAULT_TIMEOUT);
    }

    //** Implementation of PrinterFlusher
    @Override
    public void flush(byte[] buffer) throws HubResponseException, IOException
    {
        executeCommand(new RawData(buffer), PRINTER_ADDRESS, DEFAULT_TIMEOUT, (byte) 0x00);
    }

    @Override
    public int flush(byte[] buffer, int sequenceNumber) throws HubResponseException, IOException
    {
        //Split the data into chunks suitable for the hub
        int offset = 0;
        while(offset < buffer.length)
        {
            int len = CHUNKSIZE;
            if((buffer.length - offset) < CHUNKSIZE)
                len = buffer.length - offset;

            Logger.d(TAG, String.format("Created a send buffer of %d bytes", len));
            byte[] sendBuffer = new byte[len];

            //Copy this chunk to the send buffer
            System.arraycopy(buffer, offset, sendBuffer, 0, len);

            //Send this buffer
            ArrayList<Envelope> responses = null;

            //Keep resending this section until we get acknowledgement
            int failureCount = 0;

            if(mFlowControl)
            {
                Logger.d(TAG, "Flow control is ON");
                while (responses == null || responses.size() == 0)
                {
                    Logger.d(TAG, String.format("Flushing sequence %d to printer", sequenceNumber));
                    Logger.d(TAG, String.format("Attempt number %d", failureCount + 1));
                    responses = executeCommand(new RawData(sendBuffer), PRINTER_ADDRESS, DEFAULT_TIMEOUT, (byte) sequenceNumber);

                    Logger.d(TAG, String.format("Got %d responses", responses.size()));

                    //Search responses for response to this command tag
                    boolean found = false;
                    for(Envelope e : responses)
                    {
                        Logger.d(TAG, "Checking responses");
                        Logger.d(TAG, String.format("Sent: %d  Recv: %d", sequenceNumber, e.getTag()));
                        if (!found && e.getTag() == (byte) sequenceNumber)
                        {
                            Logger.d(TAG, "Found ack for this section - proceed with next part");
                            found = true;
                            continue;
                        }
                    }

                    if (responses == null || responses.size() == 0 || !found)
                    {
                        failureCount++;
                        Logger.d(TAG, "Attempt failed, will retry");
                    }

                    if (failureCount > MAX_FAILURES)
                    {
                        //Fatal error, abort
                        throw new HubResponseException("Exceeded hub command retry limit");
                    }
                }
                //Rotate sequence number if it goes over its maximum value of 15
                sequenceNumber++;
                if (sequenceNumber > 15) sequenceNumber = 1;
            }
            else
            {
                //No flow control, just send
                //Logger.d(TAG, "Sending full queue to printer");
                //Logger.d(TAG, String.format("Sending %d bytes to printer", sendBuffer.length));
                executeCommand(new RawData(sendBuffer), PRINTER_ADDRESS, DEFAULT_TIMEOUT, (byte) sequenceNumber);
            }

            Logger.d(TAG, "Sent printer part to buffer");

            offset += len;
        }

        return sequenceNumber;
    }

    @Override
    public void setMode(int mode) throws HubResponseException, IOException
    {
        if(mode == OTA_MODE)
        {
            Reset reset = new Reset();
            reset.setMode(Reset.MODE_UPGRADE);
            executeCommand(reset);
        }
    }

    @Override
    public void updateFirmware(InputStream firmware, int length) throws HubResponseException, IOException
    {
    }

    /**
     * Instructs the printer to feed n lines.  This also flushes the printer buffer.
     * @param lines
     * @throws HubResponseException
     */
    @Override
    public void printerFeed(int lines) throws HubResponseException, IOException
    {
        executeCommand(new RawData(new FeedPaper(lines).getData()),
                PRINTER_ADDRESS,
                DEFAULT_TIMEOUT);
    }

    /**
     * Check the printer to see if there is any paper left
     * @return true if the printer has paper
     */
    @Override
    public boolean hasPaper() throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new RawData(new GetStatus(GetStatus.STATUS_PRINTER).getData()),
                                                    PRINTER_ADDRESS,
                                                    DEFAULT_TIMEOUT);
        RawData raw = null;

        boolean hasPaper = true;

        for(Envelope e : response)
        {
            Command command = e.getCommand();
            if (command != null && command.getCommandCode() == 'N')
            {
                if(command.getCommandData().length > 0)
                {
                    //Get the first byte and test the flag
                    byte responseStatus = command.getCommandData()[0];
                    //0x00 means that it has paper, anything else means that it is empty
                    hasPaper = (responseStatus == 0x00);
                }
                else
                {
                    throw new HubResponseException(command);
                }
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return hasPaper;
    }

    @Override
    public PrinterFactory getPrinter()
    {
        return new Factory();
    }

    /**
     * Asks the printer to check if the cash drawer is open
     * @return true if the drawer is open
     * @throws HubResponseException
     */
    @Override
    public boolean isCashDrawerOpen() throws HubResponseException, IOException
    {
        ArrayList<Envelope> response = executeCommand(new RawData(new GetStatus(GetStatus.STATUS_CASH_DRAWER).getData()),
                                                    PRINTER_ADDRESS,
                                                    DEFAULT_TIMEOUT);
        RawData raw = null;

        boolean drawerOpen = true;

        for(Envelope e : response)
        {
            Command command = e.getCommand();
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
                    throw new HubResponseException(command);
                }
            } else if (command != null && command.getCommandCode() == 'E')
            {
                throw new HubResponseException(command);
            }
        }

        return drawerOpen;
    }

    public void setPrinterBaudRate(int baudRate)throws HubResponseException, IOException
    {
        SetPort command = new SetPort();
        command.setBaudRate(SetPort.BAUD_115200);
        executeCommand(command);
    }

    /**
     * Executes the command to the default address, and with the default timeout
     * @param c is the command to be executed
     * @return An ArrayList of Commands as the response from the MePOS device
     * @throws HubResponseException
     */
    protected ArrayList<Envelope> executeCommand(Command c) throws HubResponseException, IOException
    {
        //Default target is the hub management interface
        return executeCommand(c, HUB_ADDRESS, DEFAULT_TIMEOUT);
    }

    /**
     * Executes the command to the provided address, and with the provided timeout
     * @param c is the command to be executed
     * @return An ArrayList of Commands as the response from the MePOS device
     * @throws HubResponseException
     */
    protected ArrayList<Envelope> executeCommand(Command c, byte target, int timeout) throws HubResponseException, IOException
    {
        //Add a default sequence number
        return executeCommand(c, target, timeout, (byte)0);
    }

    /**
     * Executes the command to the provided address, and with the provided timeout
     * @param c is the command to be executed
     * @return An ArrayList of Commands as the response from the MePOS device
     * @throws HubResponseException
     */
    protected ArrayList<Envelope> executeCommand(Command c, byte target, int timeout, byte sequenceNumber) throws HubResponseException, IOException
    {
        ArrayList<Envelope> responses = new ArrayList<Envelope>();

        UsbDeviceConnection connection = mManager.openDevice(mPort.getDriver().getDevice());

        //Logger.d(TAG, "Link established");

        if (connection == null) {
            IOException e = new IOException();
            Log.e(TAG, "Opening device failed", e);
            throw e;
        }

        Frame[] frames = Frame.getFrames(new Envelope(c, TABLET_ADDRESS, target, sequenceNumber));

        byte[] dataToSend = frames[0].getFrameData();
            Logger.d(TAG, String.format("Writing %d bytes", dataToSend.length));
            Logger.d(TAG, HexDump.dumpHexString(dataToSend, 0, dataToSend.length));

        Logger.d(TAG, "Opening port");
        try
        {
            mPort.open(connection);
        }
        catch(IOException io)
        {
            Logger.d(TAG, "Port open failed");
            throw io;
        }
        //For robustness, do some automatic retries
        int failureCount = 0;
        boolean writeSuccess = false;
        int writeLen = 0;
        while(!writeSuccess)
        {
            try
            {
                writeLen = mPort.write(dataToSend, timeout);

                //If we've not thrown an exception, then retry.
                writeSuccess = true;
            } catch (IOException e)
            {
                failureCount++;
                if (failureCount > 5)
                {
                    mPort.close();
                    throw new IOException(e);
                }
                //Wait to allow time for the line to clear
                try
                {
                    Logger.d(TAG, "Port failure - sleep and retry");
                    Thread.sleep(250);
                } catch (InterruptedException ie)
                {
                    ie.printStackTrace();
                }
            }
        }

        Logger.d(TAG, String.format("Written %d bytes", writeLen));

        if(mFlowControl)
        {
            Logger.d(TAG, "Calling response read");
            responses = readResponses(timeout);
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

    /**
     * Reads responses from the USB bus.  It will wait for at least the timeout period for the first
     * response, but does not wait for subsequent responses.
     * @param timeout in milliseconds
     * @return An ArrayList of Commands as the response from the MePOS device
     * @throws IOException
     * @throws HubResponseException
     */
    protected ArrayList<Envelope> readResponses(int timeout) throws IOException, HubResponseException
    {
        ArrayList<Envelope> responses = new ArrayList<Envelope>();
        byte[] readBuffer = new byte[MAX_FRAMESIZE];
        int bytesRead = 1;
        boolean waitingForResponse = true;
        long startTime = System.currentTimeMillis();

        Logger.d(TAG, "Waiting for response..");
        while(waitingForResponse  || bytesRead > 0)
        {
            bytesRead  = mPort.read(readBuffer, timeout);
            if(waitingForResponse && bytesRead > 0)
            {
                waitingForResponse = false;
                Logger.d(TAG, "Read some data, not waiting for further responses");
            }

            if(waitingForResponse && ((startTime + timeout) < System.currentTimeMillis()))
            {
                waitingForResponse = false;
                Logger.d(TAG, "Timeout, not waiting for further responses");
            }

            if(bytesRead > 0)
            {
                Logger.d(TAG, HexDump.dumpHexString(readBuffer, 0, Math.min(32, readBuffer.length)));
                Logger.d(TAG, "********************************************");
                Logger.d(TAG, "*         HAPPY DAYS ARE HERE AGAIN        *");
                Logger.d(TAG, "********************************************");
                Logger.d(TAG, String.format("Read %d bytes", bytesRead));

                //Just take the response bytes
                byte[] responseBytes = new byte[bytesRead];
                System.arraycopy(readBuffer, 0, responseBytes, 0, bytesRead);
                //Deserialise response into a command
                Envelope e = deserialise(responseBytes);
                if(e != null)
                {
                    responses.add(e);
                }
            }
        }

        return responses;
    }

    /**
     * Converts a byte[] from the MePOS hub into a usable Command object
     * @param response the raw data
     * @return The completed Command object
     * @throws HubResponseException
     */
    protected Envelope deserialise(byte[] response) throws HubResponseException
    {
        Envelope env = null;
        try
        {
            //Deserialise the response
            ResponseParser parser = new ResponseParser();
            env = parser.process(response);
            if(env!=null) {
                Logger.d("MePOS", String.format("Tag id : %x", env.getTag()));
            }
            Logger.d("MePOS", String.format("Tag id : %x", "envelop is null"));
        }
        catch(Exception e)
        {
            Log.i("MePOS", "Fatal error");
            Log.i("MePOS", "Could not deserialise response");
            Log.i("MePOS", HexDump.dumpHexString(response, 0, response.length));

            e.printStackTrace();
            throw new HubResponseException("Cannot deserialise command", e);
        }
        return env;
    }
}
