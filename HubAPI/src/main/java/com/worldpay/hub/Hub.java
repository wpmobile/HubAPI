package com.worldpay.hub;

import java.io.IOException;
import java.io.InputStream;

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
    public void setMode(int mode) throws HubResponseException, IOException;
    public void updateFirmware(InputStream firmware, int length) throws HubResponseException, IOException;
    public PrinterFactory getPrinter();
    public void setDiagnosticLight(int light, int colour, int state) throws HubResponseException, IOException;
}
