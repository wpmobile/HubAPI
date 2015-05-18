package com.worldpay.hub.commands;

/**
 * Created by Micro2 on 05/05/2015.
 */
public class GetSystemInformation extends Command
{
    protected SystemInformation mSystemInformation;

    public GetSystemInformation()
    {
        mCommand = 'z';
        mSystemInformation = new SystemInformation();
    }

    @Override
    public void setCommandData(byte[] data)
    {
        mSystemInformation.deserialise(data);
    }

    public SystemInformation getSystemInformation()
    {
        return mSystemInformation;
    }

}
