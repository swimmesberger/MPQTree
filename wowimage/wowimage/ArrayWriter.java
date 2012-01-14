// FrontEnd Plus GUI for JAD
// DeCompiled : ArrayWriter.class
package wowimage;

import java.io.FileOutputStream;
import java.io.IOException;

public class ArrayWriter
{

    private int pos;
    private byte data[];

    public ArrayWriter(int i)
    {
        data = new byte[i];
        pos = 0;
    }

    public void seek(int i)
    {
        pos = i;
    }

    void writeInt(int i)
    {
        data[pos++] = (byte) (i & 0xff);
        data[pos++] = (byte) (i >> 8 & 0xff);
        data[pos++] = (byte) (i >> 16 & 0xff);
        data[pos++] = (byte) (i >> 24 & 0xff);
    }

    void writeShort(int i)
    {
        data[pos++] = (byte) (i & 0xff);
        data[pos++] = (byte) (i >> 8 & 0xff);
    }

    void writeByte(int i)
    {
        data[pos++] = (byte) (i & 0xff);
    }

    public void writeBytes(byte abyte0[])
    {
        writeBytes(abyte0, 0, abyte0.length);
    }

    public void writeBytes(int ai[])
    {
        writeBytes(ai, 0, ai.length);
    }

    public void writeBytes(byte abyte0[], int i, int j)
    {
        System.arraycopy(abyte0, i, data, pos, j);
        pos += j;
    }

    public void writeBytes(int ai[], int i, int j)
    {
        byte abyte0[] = new byte[j];
        for (int k = 0; k < j; k++)
        {
            abyte0[k] = (byte) ai[k];
        }

        writeBytes(abyte0, i, j);
    }

    public void store(String s)
    throws IOException
    {
        FileOutputStream fileoutputstream = new FileOutputStream(s);
        fileoutputstream.write(data);
        fileoutputstream.close();
    }
}
