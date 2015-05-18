package com.worldpay.hub.commands;

import java.nio.ByteBuffer;

/**
 * Created by Samuel Pickard on 29/04/2015.
 */
public class GetSerialNumber extends Command
{
    public GetSerialNumber()
    {
        mCommand = 'y';
        mCommandData = new byte[]{ (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
    }

    public void setSerialNumber(int sn)
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(sn);
        mCommandData = bb.array();
    }

    public int getSerialNumber()
    {
        int sn = mCommandData[0] << 24;
        sn += mCommandData[1] << 16;
        sn += mCommandData[2] << 8;
        sn += mCommandData[3];

        return sn;
    }
}
