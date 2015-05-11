package com.worldpay.hub.commands;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        String date = String.format("%02d-%02d-%02d %02d:%02d:%02d",
                                    data[0], data[1], data[2],
                                    data[3], data[4], data[5]);

        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

        try
        {
            mDate = df.parse(date);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public Date getDate()
    {
        return mDate;
    }
}
