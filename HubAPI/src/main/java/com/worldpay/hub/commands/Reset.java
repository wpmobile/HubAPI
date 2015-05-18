package com.worldpay.hub.commands;

/**
 * Created by Micro2 on 06/05/2015.
 */
public class Reset extends Command
{
    public static final byte MODE_NOT_CONFIGURED = 0x00;
    public static final byte MODE_FAULTY = 0x01;
    public static final byte MODE_NORMAL = 0x02;

    public Reset()
    {
        mCommand = 'G';
        mCommandData = new byte[] { MODE_NORMAL };
    }

    public void setMode(byte mode)
    {
        if(mode < 3)
            mCommandData = new byte[] { mode };
    }
}
