package com.worldpay.hub;

import com.worldpay.hub.commands.Command;
import com.worldpay.hub.commands.ErrorResponse;

public class MePOSResponseException extends Exception
{
    protected ErrorResponse mError;
    public MePOSResponseException() { super(); }
    public MePOSResponseException(String message) { super(message); }
    public MePOSResponseException(String message, Throwable cause) { super(message, cause); }
    public MePOSResponseException(Throwable cause) { super(cause); }
    public MePOSResponseException(Command e)
    {
        if(e.getCommandCode() == 'E')
            mError = (ErrorResponse)e;
    }

    public ErrorResponse getError()
    {
        return mError;
    }
}
