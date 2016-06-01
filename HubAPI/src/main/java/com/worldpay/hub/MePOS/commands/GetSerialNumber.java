package com.worldpay.hub.MePOS.commands;

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
        mRequiresResponse = true;
    }

    public void setSerialNumber(int sn)
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(sn);
        mCommandData = bb.array();
    }

    public int getSerialNumber()
    {
        int sn  =  (mCommandData[0] & 0xFF) << 24;
        sn      += (mCommandData[1] & 0xFF) << 16;
        sn      += (mCommandData[2] & 0xFF) << 8;
        sn      +=  mCommandData[3] & 0xFF;

        return sn;
    }
}
