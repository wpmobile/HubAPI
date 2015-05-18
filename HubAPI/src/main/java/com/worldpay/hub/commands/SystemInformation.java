package com.worldpay.hub.commands;

import com.worldpay.hub.util.ShortDate;

import java.nio.ByteBuffer;
import java.util.Date;

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
    protected String mFWDocNumber;
    protected String mFWSArticleCode;
    protected String mFWRevision;
    protected ShortDate mBuildDate;
    protected byte mNode;
    protected byte mOperationalMode;
    protected byte mActiveMode;
    protected ShortDate mLastDate;
    protected byte[] mChannelSetup;

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
        mFWDocNumber = "";
        mFWRevision = "";
        mFWSArticleCode = "";
        mBuildDate = new ShortDate();
        mNode = 0x00;
        mOperationalMode = 0x00;
        mActiveMode = 0x00;
        mLastDate = new ShortDate();
        mChannelSetup = new byte[16];
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

    /* The following properties are read only */
    public String getFWDocNumber()
    {
        return mFWDocNumber;
    }

    public String getFWSArticleCode()
    {
        return mFWSArticleCode;
    }

    public String getFWRevision()
    {
        return mFWRevision;
    }

    public Date getBuildDate()
    {
        return mBuildDate.toDate();
    }

    public byte getNode()
    {
        return mNode;
    }

    public byte getOperationalMode()
    {
        return mOperationalMode;
    }

    public byte getActiveMode()
    {
        return mActiveMode;
    }

    public Date getLastDate()
    {
        return mLastDate.toDate();
    }

    public byte[] getChannelSetup()
    {
        return mChannelSetup;
    }

    public byte[] getCommandData()
    {
        //We only need the writable version of System Information
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
            System.arraycopy(data, 7, mSerial, 0, 4);
            System.arraycopy(data, 11, mVariant, 0, 4);
            System.arraycopy(data, 15, mSupplier, 0, 4);
            System.arraycopy(data, 19, mCustomerCode, 0, 4);
            System.arraycopy(data, 23, mSupplierKey, 0, 4);
            System.arraycopy(data, 27, mCustomerKey, 0, 4);
        }
        else if(data.length > 70)
        {
            /* Full system info has a variable length (but more that the short writable version */
            System.arraycopy(data, 0, mType, 0, 7);
            System.arraycopy(data, 7, mSerial, 0, 4);
            System.arraycopy(data, 11, mVariant, 0, 4);
            byte[] docNo = new byte[4];
            System.arraycopy(data, 15, docNo, 0, 4);
            mFWDocNumber = new String(docNo);

            byte[] artCode = new byte[7];
            System.arraycopy(data, 19, artCode, 0, 7);
            mFWSArticleCode = new String(artCode);

            byte[] rev = new byte[7];
            System.arraycopy(data, 26, rev, 0, 7);
            mFWRevision = new String(rev);

            byte[] bd = new byte[6];
            System.arraycopy(data, 33, bd, 0, 6);
            mBuildDate = new ShortDate(bd);

            System.arraycopy(data, 39, mSupplier, 0, 4);
            System.arraycopy(data, 43, mCustomerCode, 0, 4);

            mNode = data[44];
            mOperationalMode = data[45];
            mActiveMode = data[46];

            byte[] ld = new byte[6];
            System.arraycopy(data, 48, ld, 0, 6);
            mLastDate = new ShortDate(ld);

            System.arraycopy(data, 54, mChannelSetup, 0, 16);
        }
    }

    public boolean equals(SystemInformation si)
    {
        return mCommandData.equals(si.getCommandData());
    }
}
