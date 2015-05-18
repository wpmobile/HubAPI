package com.worldpay.hub.printer.commands;

public class FeedPaper extends PrinterCommand
{
    public FeedPaper()
    {
        printerFeed( (byte)0x01 );
    }

    public FeedPaper(int lines)
    {
        printerFeed((byte)lines);
    }

    protected void printerFeed(byte lines)
    {
        mData = new byte[] { 0x14, lines };
    }
}
