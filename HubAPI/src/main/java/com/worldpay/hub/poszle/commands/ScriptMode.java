package com.worldpay.hub.poszle.commands;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterCommandNotImplementedException;

public class ScriptMode extends PrinterCommand
{
    public static final int SUPERSCRIPT = 2;
    public static final int SUBSCRIPT   = 1;
    public static final int NORMAL      = 0;

    public ScriptMode(int type) throws PrinterCommandNotImplementedException
    {
        throw new PrinterCommandNotImplementedException();
    }
}
