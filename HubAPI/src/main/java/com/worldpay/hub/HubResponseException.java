package com.worldpay.hub;

import com.worldpay.hub.MePOS.commands.Command;
import com.worldpay.hub.MePOS.commands.ErrorResponse;

public class HubResponseException extends Exception
{
    protected ErrorResponse mError;
    public HubResponseException() { super(); }
    public HubResponseException(String message) { super(message); }
    public HubResponseException(String message, Throwable cause) { super(message, cause); }
    public HubResponseException(Throwable cause) { super(cause); }
    public HubResponseException(Command e)
    {
        if(e.getCommandCode() == 'E')
            mError = (ErrorResponse)e;
    }

    public ErrorResponse getError()
    {
        return mError;
    }
}
