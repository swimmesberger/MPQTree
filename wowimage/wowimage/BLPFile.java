package wowimage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BLPFile
{

    BLPDecoder o;

    public BLPFile(File f) throws ConversionException
    {
        o = new BLPDecoder(f);
    }

    public BufferedImage getImg() throws IOException, ConversionException
    {
        return o.writePNG("");
    }
}
