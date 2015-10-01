package com.worldpay.hub.MePOS.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Change the printer port characteristics
 */
public class SetPort extends Command
{
    public static final int BAUD_9600           = 9600;
    public static final int BAUD_57600          = 57600;
    public static final int BAUD_115200         = 115200;

    public static final int STOPBITS_1          = 0x00;
    public static final int STOPBITS_15         = 0x40;
    public static final int STOPBITS_2          = 0x80;
    public static final int STOPBITS_NO_CHANGE  = 0xC0;

    public static final int PARITY_NONE         = 0x00;
    public static final int PARITY_ODD          = 0x08;
    public static final int PARITY_EVEN         = 0x10;
    public static final int PARITY_FORCE_EVEN   = 0x18;
    public static final int PARITY_FORCE_ODD    = 0x20;
    public static final int PARITY_NO_CHANGE    = 0x38;

    public static final int DATA_BITS_5         = 0x00;
    public static final int DATA_BITS_6         = 0x01;
    public static final int DATA_BITS_7         = 0x02;
    public static final int DATA_BITS_8         = 0x03;
    public static final int DATA_BITS_NO_CHANGE = 0x07;

    public static final int FLOW_CONTROL_NONE       = 0x00;
    public static final int FLOW_CONTROL_XON_XOFF   = 0x10;
    public static final int FLOW_CONTROL_RTS_CTS    = 0x20;
    public static final int FLOW_CONTROL_NO_CHANGE  = 0xF0;

    private static final int DO_NOT_CHANGE      = 0xFFFF;
    private static final int NOT_APPLICABLE     = 0x00;

    private int mBaudRate = BAUD_115200;
    private int mCharControl = STOPBITS_NO_CHANGE + PARITY_NO_CHANGE + DATA_BITS_NO_CHANGE;
    private int mFlowControl = FLOW_CONTROL_NO_CHANGE;

    public SetPort()
    {
        mCommand = 'A';
        mCommandData = new byte[14];
    }

    @Override
    public byte[] getCommandData()
    {
        ByteBuffer bb = ByteBuffer.allocate(14);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(mBaudRate); //Little endian 32bit int
        bb.put((byte) mCharControl);
        bb.put((byte) mFlowControl);
        bb.putShort((short) DO_NOT_CHANGE); //Modem values
        bb.putShort((short) DO_NOT_CHANGE); //Modem mask
        bb.putShort((short) NOT_APPLICABLE); //Radio values
        bb.putShort((short) NOT_APPLICABLE); //Radio mask
        mCommandData = bb.array();
        return mCommandData;
    }

    @Override
    public void setCommandData(byte[] data)
    {
        mCommandData = data;
    }

    public void setBaudRate(int baud)
    {
        mBaudRate = baud;
    }

    public void setCharacterControl(int characterControl)
    {
        mCharControl = characterControl;
    }

    public void setFlowControl(int flow)
    {
        mFlowControl = flow;
    }

}
