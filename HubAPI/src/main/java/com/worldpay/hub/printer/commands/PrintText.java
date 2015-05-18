package com.worldpay.hub.printer.commands;

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
        mData = text.getBytes();
    }
}
