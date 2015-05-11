package com.worldpay.hub.commands;

import java.nio.ByteBuffer;

/**
 * Represents the set of data usable by GetSystemInformation and SetSystemInformation.
 */
public class SystemInformation
{
    protected byte[] mType;
    protected byte[] mSerial;
    protected byte[] mVariant;
    protected byte[] mSupplier;
    protected byte[] mCustomerCode;
    protected byte[] mSupplierKey;
    protected byte[] mCustomerKey;

    protected boolean mIsDirty;
    protected byte[] mCommandData;

    public SystemInformation()
    {
        mIsDirty = true;

        mType = new byte[7];
        mSerial = new byte[4];
        mVariant = new byte[4];
        mSupplier = new byte[4];
        mCustomerCode = new byte[4];
        mSupplierKey = new byte[4];
        mCustomerKey = new byte[4];
    }

    public void setType(String type)
    {
        if (type.length() < 8)
        {
            mType = type.getBytes();
            mIsDirty = true;
        }
    }

    public String getType()
    {
        return new String(mType);
    }

    public void setSerial(int serial)
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(serial);
        mSerial = bb.array();

        mIsDirty = true;
    }

    public int getSerialNumber()
    {
        int sn = mSerial[0] << 24;
        sn += mSerial[1] << 16;
        sn += mSerial[2] << 8;
        sn += mSerial[3];

        return sn;
    }

    public void setVariant(byte[] variant)
    {
        if(variant.length == 4)
        {
            mVariant = variant;
            mIsDirty = true;
        }
    }

    public byte[] getVariant()
    {
        return mVariant;
    }

    public void setSupplier(byte[] supplier)
    {
        if(supplier.length == 4)
        {
            mSupplier = supplier;
            mIsDirty = true;
        }
    }

    public byte[] getSupplier()
    {
        return mSupplier;
    }

    public void setCustomerCode(byte[] customer)
    {
        if(customer.length == 4)
        {
            mCustomerCode = customer;
            mIsDirty = true;
        }
    }

    public byte[] getCustomerCode()
    {
        return mCustomerCode;
    }

    public void setSupplierKey(byte[] supplierKey)
    {
        if(supplierKey.length == 4)
        {
            mSupplierKey = supplierKey;
            mIsDirty = true;
        }
    }

    public byte[] getSupplierKey()
    {
        return mSupplierKey;
    }

    public void setCustomerKey(byte[] customerKey)
    {
        if(customerKey.length == 4)
        {
            mCustomerKey = customerKey;
            mIsDirty = true;
        }
    }

    public byte[] getCustomerKey()
    {
        return mCustomerKey;
    }

    public byte[] getCommandData()
    {
        if(mIsDirty)
        {
            mCommandData = new byte[31];
            System.arraycopy(mType, 0, mCommandData, 0, 7);
            System.arraycopy(mSerial, 0, mCommandData, 7, 4);
            System.arraycopy(mVariant, 0, mCommandData, 11, 4);
            System.arraycopy(mSupplier, 0, mCommandData, 15, 4);
            System.arraycopy(mCustomerCode, 0, mCommandData, 19, 4);
            System.arraycopy(mSupplierKey, 0, mCommandData, 23, 4);
            System.arraycopy(mCustomerKey, 0, mCommandData, 27, 4);

            mIsDirty = false;
        }

        return mCommandData;
    }

    /**
     * Populates this instance based on response data
     * @param data the response from the MePOS unit
     */
    public void deserialise(byte[] data)
    {
        if(data.length == 31)
        {
            System.arraycopy(data, 0, mType, 0, 7);
            System.arraycopy(data, 0, mSerial, 7, 4);
            System.arraycopy(data, 0, mVariant, 11, 4);
            System.arraycopy(data, 0, mSupplier, 15, 4);
            System.arraycopy(data, 0, mCustomerCode, 19, 4);
            System.arraycopy(data, 0, mSupplierKey, 23, 4);
            System.arraycopy(data, 0, mCustomerKey, 27, 4);
        }
    }

    public boolean equals(SystemInformation si)
    {
        return mCommandData.equals(si.getCommandData());
    }
}
