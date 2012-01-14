// FrontEnd Plus GUI for JAD
// DeCompiled : ArrayReader.class
package wowimage;

public class ArrayReader
{

    private int pos;
    private byte data[];

    public ArrayReader(byte abyte0[])
    {
        data = abyte0;
        pos = 0;
    }

    public void seek(int i)
    {
        pos = i;
    }

    public int readInt()
    {
        int i = 0;
        int j = data[pos++];
        if (j < 0)
        {
            j += 256;
        }
        i += j;
        j = data[pos++];
        if (j < 0)
        {
            j += 256;
        }
        i += j << 8;
        j = data[pos++];
        if (j < 0)
        {
            j += 256;
        }
        i += j << 16;
        j = data[pos++];
        if (j < 0)
        {
            j += 256;
        }
        i += j << 24;
        return i;
    }

    public int readShort()
    {
        int i = 0;
        int j = data[pos++];
        if (j < 0)
        {
            j += 256;
        }
        i += j;
        j = data[pos++];
        if (j < 0)
        {
            j += 256;
        }
        i += j << 8;
        return i;
    }

    public int readByte()
    {
        int i = 0;
        int j = data[pos++];
        if (j < 0)
        {
            j += 256;
        }
        i += j;
        return i;
    }

    public byte[] readBytes(int i)
    {
        byte abyte0[] = new byte[i];
        System.arraycopy(data, pos, abyte0, 0, i);
        pos += i;
        return abyte0;
    }
}
