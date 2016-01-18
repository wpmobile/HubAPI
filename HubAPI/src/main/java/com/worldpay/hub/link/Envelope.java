package com.worldpay.hub.link;
import com.worldpay.hub.MePOS.commands.Command;
/**
 * An Envelope contains routing and tagging data for a command.
 */
public class Envelope
{
    protected byte[] mData;
    protected Command mCommand;
    protected byte mAddress;
    protected byte mSource;
    protected byte mSequence;
    protected boolean mIsDirty;

    public Envelope()
    {
        init(null, (byte)0x00, (byte)0x00, (byte)0x00);
    }

    public Envelope(Command command, byte source, byte address)
    {
        init(command, source, address, (byte)0x00);
    }

    public Envelope(Command command, byte source, byte address, byte tag)
    {
        init(command, source, address, tag);
    }

    public void setSource(byte source)
    {
        mSource = source;
        mIsDirty = true;
    }

    public byte getSource()
    {
        return mSource;
    }

    public void setAddress(byte source)
    {
        mAddress = source;
        mIsDirty = true;
    }

    public byte getAddress()
    {
        return mAddress;
    }

    public void setTag(byte tag)
    {
        mSequence = tag;
        mIsDirty = true;
    }

    public void setTag(int tag)
    {
        setTag(tag & 0xFF);
    }

    public byte getTag()
    {
        return mSequence;
    }

    public void setCommand(Command command)
    {
        mCommand = command;
        mIsDirty = true;
    }

    protected void init(Command command, byte source, byte address, byte tag)
    {
        mIsDirty = true;
        mCommand = command;
        mSource = source;
        mAddress = address;
        mSequence = tag;
    }

    public byte[] getData()
    {
        if(mIsDirty)
            formatData();

        return mData;
    }

    public int getDataLength()
    {
        if(mIsDirty)
            formatData();

        if(mData == null)
            return 0;
        else
            return mData.length;
    }

    public char getCommandCode()
    {
        if(mCommand != null)
            return mCommand.getCommandCode();
        else
            return '\0';
    }

    public Command getCommand()
    {
        return mCommand;
    }

    protected void formatData()
    {
        if(mIsDirty)
        {
            mData = new byte[mCommand.getCommandLength() + 4]; //+1 Address
            //+1 source
                                                                    //+1 command code
                                                                    //+1 tag
            mData[0] = mAddress;
            mData[1] = mSource;
            mData[2] = (byte) mCommand.getCommandCode();
            mData[3] = mSequence;

            if(mCommand.getCommandLength() > 0)
                System.arraycopy(mCommand.getCommandData(), 0, mData, 4, mCommand.getCommandLength());

            mIsDirty = false;
        }
    }
}
