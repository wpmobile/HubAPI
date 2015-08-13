package com.worldpay.hub.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Micro2 on 12/08/2015.
 */
public class SetIO extends Command
{
    public static final int COLOR_RED      = 1;
    public static final int COLOR_GREEN    = 2;
    public static final int COLOR_BLUE     = 4;

    public static final int DIAGNOSTIC_LIGHT_1  = 1;
    public static final int DIAGNOSTIC_LIGHT_2  = 4;
    public static final int DIAGNOSTIC_LIGHT_3  = 16;
    public static final int COSMETIC_LIGHT      = 256;

    public static final int CASH_DRAWER_RELEASE = 4096;

    protected int mInputSignificance;
    protected int mInputValues;
    protected int mInputChanges;
    protected int mInputEventMaskSignificance;
    protected int mInputEventMask;
    protected int mOutputSignificance;
    protected int mOutputValues;

    public SetIO()
    {
        mInputSignificance = 0;
        mInputValues = 0;
        mInputChanges = 0;
        mInputEventMaskSignificance = 0;
        mInputEventMask = 0;
        mOutputSignificance = 0;
        mOutputValues = 0;

        mCommand = 'I';
        mCommandData = new byte[32];
    }

    public void setValue(int index, boolean value)
    {
        //If there is a change, then we mark it regardless of what the value is
        mOutputSignificance |= index;

        if(value)
        {
            //set to 1.  Easy, we can do a simple bitwise or
            mOutputValues |= index;
        }
        else
        {
            //set to 0.  We can do a bitwise and with a mask
            int valueMask = 0xFFFFFFFF - index;
            mOutputValues &= valueMask;
        }
    }

    @Override
    public byte[] getCommandData()
    {
        ByteBuffer bb = ByteBuffer.allocate(32);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(0); //Always start with 32bits of 0
        bb.putInt(mInputSignificance);
        bb.putInt(mInputValues);
        bb.putInt(mInputChanges);
        bb.putInt(mInputEventMaskSignificance);
        bb.putInt(mInputEventMask);
        bb.putInt(mOutputSignificance);
        bb.putInt(mOutputValues);

        mCommandData = bb.array();
        return mCommandData;
    }

    @Override
    public void setCommandData(byte[] data)
    {
        mCommandData = data;
    }

}
