package com.worldpay.hub.MePOS.commands;

/**
 * Created by Micro2 on 06/05/2015.
 */
public class Reset extends Command
{
    public static final byte MODE_NOT_CONFIGURED = 0x00;
    public static final byte MODE_FAULTY         = 0x01;
    public static final byte MODE_NORMAL         = 0x02;
    public static final byte MODE_UPGRADE        = 0x04;

    public Reset()
    {
        mCommand = 'G';
        mCommandData = new byte[] { MODE_NORMAL };
    }

    public void setMode(byte mode)
    {
        if (mode == MODE_NOT_CONFIGURED
        ||  mode == MODE_FAULTY
        ||  mode == MODE_NORMAL
        ||  mode == MODE_UPGRADE)
            mCommandData = new byte[] { mode, 0x00 };
    }
}
