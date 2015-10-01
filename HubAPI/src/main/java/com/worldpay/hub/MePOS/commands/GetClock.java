package com.worldpay.hub.MePOS.commands;

import com.worldpay.hub.util.ShortDate;

import java.util.Date;

public class GetClock extends Command
{
    protected Date mDate;
    public GetClock()
    {
        mCommand = 'k';
        mDate = null;
    }

    @Override
    public void setCommandData(byte[] data)
    {
        mCommandData = data;
        mDate = new ShortDate(data).toDate();
    }

    public Date getDate()
    {
        return mDate;
    }
}
