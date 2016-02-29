package com.worldpay.hub;

import android.util.Log;

public class Checksum
{
    protected static final String TAG = "CRC";

    public static int generate(byte[] data)
    {
        return generate(data, 0x1021);
    }

    public static int generate(byte[] data, int polynomial)
    {
        return generate(data, polynomial, 0x0000);
    }

    public static int generate(byte[] data, int polynomial, int initialValue)
    {
        int crc = initialValue;

        //GBE Checksum is broken.  We have to skip the first byte
        for (int c = 1; c < data.length; c++) {
            byte b = data[c];
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        //Logger.d(TAG, Integer.toHexString(crc));
        return crc;
    }
}
