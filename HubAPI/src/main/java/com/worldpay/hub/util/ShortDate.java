package com.worldpay.hub.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a MePOS format binary date, a sequence of 6 bytes representing yy, mm, dd, hh, mm, ss
 */
public class ShortDate
{
    protected byte[] mShortDate;
    public ShortDate(byte[] date)
    {
        mShortDate = date;
    }

    public ShortDate()
    {
        mShortDate = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    }

    public Date toDate()
    {
        Date retDate = null;
        String date = String.format("%02d-%02d-%04d %02d:%02d:%02d",
                mShortDate[2], mShortDate[1], mShortDate[0] + 2000,
                mShortDate[3], mShortDate[4], mShortDate[5]);
        Log.d("MePOS", date);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        try
        {
            retDate = df.parse(date);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        return retDate;
    }
}
