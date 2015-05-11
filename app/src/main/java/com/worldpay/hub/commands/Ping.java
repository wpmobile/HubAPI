package com.worldpay.hub.commands;

public class Ping extends Command
{
    public Ping()
    {
        mCommand = 'p';
        mCommandData = "Sam is ace".getBytes();
    }

    public Ping(String message)
    {
        mCommand = 'p';
        mCommandData = message.getBytes();
    }

    public String getResponseMessage()
    {
        return new String(mCommandData);
    }
}
