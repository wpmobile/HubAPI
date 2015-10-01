package com.worldpay.hub.mpop.printer.commands;
import com.worldpay.hub.PrinterCommand;

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
        mData = new byte[] { 0x1B, 0x61, lines };
    }
}
