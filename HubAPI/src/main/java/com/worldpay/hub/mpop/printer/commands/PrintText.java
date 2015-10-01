package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

import java.io.UnsupportedEncodingException;

public class PrintText extends PrinterCommand
{
    public PrintText()
    {
        init("");
    }

    public PrintText(String text)
    {
        init(text);
    }

    protected void init(String text)
    {
        try
        {
            mData = text.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }
}
