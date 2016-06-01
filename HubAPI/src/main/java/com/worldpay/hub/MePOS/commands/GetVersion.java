package com.worldpay.hub.MePOS.commands;

public class GetVersion extends Command
{
    public GetVersion()
    {
        mCommand = 'v';
        mCommandData = new byte[] { 0x00, 0x00, 0x00, 0x00, /* serial number of the recipient */
                                    0x01, 0x00, 0x00        /* protocol version 1.0.0 */ };
        mRequiresResponse = true;
    }

    public void setSerialNumber(byte[] serial)
    {
        if(serial.length == 4)
        {
            mCommandData = new byte[7];
            System.arraycopy(serial, 0, mCommandData, 0, 4);

            //Append protocol version
            mCommandData[4] = 0x01;
            mCommandData[5] = 0x00;
            mCommandData[6] = 0x00;
        }
    }

    public int getSerialNumber()
    {
        int sn  =  (mCommandData[0] & 0xFF) << 24;
        sn      += (mCommandData[1] & 0xFF) << 16;
        sn      += (mCommandData[2] & 0xFF) << 8;
        sn      +=  mCommandData[3] & 0xFF;

        return sn;
    }

    public String getProtocolVersion()
    {
        if(mCommandData.length == 7)
            return String.format("%d.%d.%d", mCommandData[4] & 0xFF,
                                             mCommandData[5] & 0xFF,
                                             mCommandData[6] & 0xFF);
        else
            return "";
    }
}
