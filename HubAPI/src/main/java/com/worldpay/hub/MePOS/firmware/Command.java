package com.worldpay.hub.MePOS.firmware;

/**
 * Encapsulates a single command to the hub.
 *
 * The command has four parts
 * 1.  The type indicator - Send command (S), read word (W), write word (w), Go (G)
 * 2.  The address
 * 3.  Optionally, the value
 * 4.  A terminator, #
 */
public class Command
{
    public final static String TYPE_SEND_COMMAND = "S";
    public final static String TYPE_READ_WORD    = "W";
    public final static String TYPE_WRITE_WORD   = "w";
    public final static String TYPE_GO_COMMAND   = "G";

    protected String mType;
    protected int mAddress;
    protected int mValue;

    protected String mAsString;

    public Command(String type, int address, int value)
    {
        mAsString = String.format("%S%08X,%08X#", type, address, value);
    }


    public Command(String type, int address)
    {
        mAsString = String.format("%S%08X#", type, address);
    }

    public String toString()
    {
        return mAsString;
    }
}
