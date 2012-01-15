package wowimage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BLPFile
{

    BLPDecoder o;

    public BLPFile(File f) throws ConversionException
    {
        o = new BLPDecoder(f);
    }
    
    public BLPFile(InputStream in, int size) throws ConversionException
    {
        o = new BLPDecoder(in, size);
    }

    public BufferedImage getImg() throws IOException, ConversionException
    {
        return o.writePNG("");
    }
}
