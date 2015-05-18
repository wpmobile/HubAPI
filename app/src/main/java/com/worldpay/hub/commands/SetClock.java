package com.worldpay.hub.commands;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetClock extends Command
{
    public SetClock()
    {
        mCommand = 'K';
        mCommandData = new byte[6];

        setDateTime(System.currentTimeMillis());
    }

    public SetClock(Date date)
    {
        mCommand = 'K';
        mCommandData = new byte[6];

        setDateTime(date);
    }

    public void setDateTime(long millis)
    {
        setDateTime(new Date(millis));
    }

    public void setDateTime(Date d)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        mCommandData[0] = (byte) cal.get(Calendar.YEAR);
        mCommandData[1] = (byte) cal.get(Calendar.MONTH);
        mCommandData[2] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        mCommandData[3] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        mCommandData[4] = (byte) cal.get(Calendar.MINUTE);
        mCommandData[5] = (byte) cal.get(Calendar.SECOND);
    }

    public Date getDate()
    {
        String date = String.format("%02d-%02d-%02d %02d:%02d:%02d",
            mCommandData[0], mCommandData[1], mCommandData[2],
            mCommandData[3], mCommandData[4], mCommandData[5]);

        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

        Date dt = null;
        try
        {
            dt = df.parse(date);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        return dt;
    }
}
