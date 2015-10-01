package com.worldpay.hub;

import java.io.IOException;

/**
 * A generic and abstract hub interface to allow the easy change of hub hardware
 */
public interface Hub
{
    public void print(PrinterQueue queue) throws HubResponseException, IOException;
    public void printerFeed(int lines) throws HubResponseException, IOException;
    public void openCashDrawer() throws HubResponseException, IOException;
    public boolean isCashDrawerOpen() throws HubResponseException, IOException;
    public boolean hasPaper() throws HubResponseException, IOException;
    public PrinterFactory getPrinter();
}
