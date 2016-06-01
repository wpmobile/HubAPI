package com.worldpay.hub.MePOS.commands;

/**
 * Created by Micro2 on 29/04/2015.
 */
public class Command {
    private final String TAG = "API Command";
    protected char mCommand;
    protected byte[] mCommandData;
    protected boolean mRequiresResponse;

    public Command()
    {
        mCommand = '\0';
        mCommandData = null;
        mRequiresResponse = false;
    }

    public byte[] getCommandData()
    {
        return mCommandData;
    }

    public int getCommandLength()
    {
        if(mCommandData == null)
            return 0;
        else
            return mCommandData.length;
    }

    public void setCommandData(byte[] data)
    {
        mCommandData = data;
    }

    public char getCommandCode()
    {
        return mCommand;
    }

    public void setCommandCode(char code)
    {
        mCommand = code;
    }

    public void setCommandCode(byte code)
    {
        mCommand = (char)code;
    }

    public boolean requiresResponse()
    {
        return mRequiresResponse;
    }

    //Creates an instance of the right command based on the response data
    public static Command getCommand(char code)
    {
        Command c = null;
        //read the command code and create an instance of the correct class
        switch(code)
        {
            case 'k':
                c = new GetClock();
                break;
            case 'y':
                c = new GetSerialNumber();
                break;
            case 'z':
                c = new GetSystemInformation();
                break;
            case 'v':
                c = new GetVersion();
                break;
            case 'Y':
                c = new SetSystemInformation();
                break;
            case 'K':
                c = new SetClock();
                break;
            case 'N':
                c = new RawData();
                break;
            case 'G':
                c = new Reset();
                break;
            case 'p':
                c = new Ping();
                break;
            case 'E':
                c = new ErrorResponse();
                break;
            case 'I':
                c = new SetIO();
                break;
            default:
                c = new Command();
                break;
        }
        return c;
    }

    protected void deserialise(byte[] response)
    {
        /* deliberately blank */
    }
}
