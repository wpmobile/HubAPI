package com.worldpay.hub.poszle;

import com.worldpay.hub.Hub;
import com.worldpay.hub.HubResponseException;
import com.worldpay.hub.PrinterCommand;
import com.worldpay.hub.PrinterFactory;
import com.worldpay.hub.PrinterQueue;
import com.miurasystems.miuralibrary.MPIHandler;
import com.miurasystems.miuralibrary.tlv.ResponseMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Micro2 on 13/05/2016.
 */
public class POSzleHub implements Hub, MPIHandler
{
    @Override
    public void print(PrinterQueue queue) throws HubResponseException, IOException
    {
    }

    @Override
    public void printerFeed(int lines) throws HubResponseException, IOException
    {

    }

    @Override
    public void openCashDrawer() throws HubResponseException, IOException
    {

    }

    @Override
    public boolean isCashDrawerOpen() throws HubResponseException, IOException
    {
        return false;
    }

    @Override
    public boolean hasPaper() throws HubResponseException, IOException
    {
        return false;
    }

    @Override
    public void setMode(int mode) throws HubResponseException, IOException
    {
        return;
    }

    @Override
    public void updateFirmware(InputStream firmware, int length) throws HubResponseException, IOException
    {
        return;
    }

    @Override
    public PrinterFactory getPrinter()
    {
        return new POSzleFactory();
    }

    @Override
    public void setDiagnosticLight(int light, int colour, int state) throws HubResponseException, IOException
    {
        return;
    }

    //MPI Handler methods specific to the POSzle implmentation

    @Override
    public void connected()
    {

    }

    @Override
    public void receivedSolicitedMessage(ResponseMessage rm)
    {

    }

    @Override
    public void receivedUnsolicitedMessage(ResponseMessage rm)
    {

    }

    @Override
    public void disconnected()
    {

    }

    @Override
    public void connectionState(boolean flg)
    {

    }
}
