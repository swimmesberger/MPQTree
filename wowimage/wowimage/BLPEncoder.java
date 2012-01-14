// FrontEnd Plus GUI for JAD
// DeCompiled : BLPEncoder.class

package wowimage;

import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

// Referenced classes of package wowimage:
//            ConversionException, ArrayReader, ArrayWriter, DXTCFormat

public class BLPEncoder
{

    int bitmapType;
    int imageWidth;
    int imageHeight;
    boolean bitmapHasAlpha;
    int transparentColor;
    final int TYPE_PALETTE = 1;
    final int TYPE_RGB = 2;
    byte palette[];
    int bitmap[];

    public BLPEncoder(String s, String s1)
        throws ConversionException
    {
        if(s == "bmp")
        {
            File file = new File(s1);
            int i = (int)file.length();
            byte abyte0[] = new byte[i];
            try
            {
                FileInputStream fileinputstream1 = new FileInputStream(s1);
                fileinputstream1.read(abyte0);
                fileinputstream1.close();
            }
            catch(IOException ioexception1)
            {
                throw new ConversionException("Error reading " + s1);
            }
            ArrayReader arrayreader = new ArrayReader(abyte0);
            if(arrayreader.readByte() != 66 || arrayreader.readByte() != 77)
                throw new ConversionException(s1 + " is not a valid .bmp");
            arrayreader.seek(10);
            int k = arrayreader.readInt();
            if(arrayreader.readInt() != 40)
                throw new ConversionException("Corrupt BMP file header (header size)");
            imageWidth = arrayreader.readInt();
            imageHeight = arrayreader.readInt();
            if(arrayreader.readShort() != 1)
                throw new ConversionException("Corrupt BMP file header (bit planes)");
            int l = arrayreader.readShort();
            if(l != 8)
                throw new ConversionException("Only 256 color BMP files are supported");
            int i1 = arrayreader.readInt();
            if(i1 != 0)
                throw new ConversionException("RLE-compressed BMPs are not supported");
            if(arrayreader.readInt() != imageWidth * imageHeight + 2)
                throw new ConversionException("Corrupt BMP file header (bitmap length)");
            arrayreader.seek(46);
            int j1 = arrayreader.readInt();
            if(j1 == 0)
                j1 = 256;
            arrayreader.seek(54);
            byte abyte1[] = arrayreader.readBytes(4 * j1);
            palette = new byte[1024];
            System.arraycopy(abyte1, 0, palette, 0, 4 * j1);
            arrayreader.seek(k);
            byte abyte2[] = arrayreader.readBytes(imageWidth * imageHeight);
            bitmap = new int[abyte2.length];
            for(int k1 = 0; k1 < abyte2.length; k1 += imageWidth)
                System.arraycopy(abyte2, abyte2.length - imageWidth - k1, bitmap, k1, imageWidth);

        } else
        if(s == "png")
            try
            {
                FileInputStream fileinputstream = new FileInputStream(s1);
                BufferedImage bufferedimage = ImageIO.read(fileinputstream);
                fileinputstream.close();
                ColorModel colormodel;
                try
                {
                    colormodel = bufferedimage.getColorModel();
                }
                catch(NullPointerException nullpointerexception)
                {
                    throw new ConversionException(s1 + " is not a real PNG image");
                }
                if(colormodel.getPixelSize() == 8)
                {
                    transparentColor = ((IndexColorModel)colormodel).getTransparentPixel();
                    if(transparentColor != -1)
                        bitmapHasAlpha = true;
                    bitmapType = 1;
                    palette = new byte[1024];
                    for(int j = 0; j < 256; j++)
                    {
                        palette[j * 4] = (byte)colormodel.getBlue(j);
                        palette[j * 4 + 1] = (byte)colormodel.getGreen(j);
                        palette[j * 4 + 2] = (byte)colormodel.getRed(j);
                    }

                    imageWidth = bufferedimage.getWidth();
                    imageHeight = bufferedimage.getHeight();
                    bitmap = new int[imageWidth * imageHeight];
                    Raster raster = bufferedimage.getData();
                    bitmap = raster.getPixels(0, 0, imageWidth, imageHeight, bitmap);
                } else
                if(colormodel.getPixelSize() == 24)
                {
                    bitmapType = 2;
                    bitmapHasAlpha = false;
                    imageWidth = bufferedimage.getWidth();
                    imageHeight = bufferedimage.getHeight();
                    bitmap = new int[imageWidth * imageHeight * 3];
                    Raster raster1 = bufferedimage.getData();
                    bitmap = raster1.getPixels(0, 0, imageWidth, imageHeight, bitmap);
                } else
                if(colormodel.getPixelSize() == 32)
                {
                    bitmapType = 2;
                    bitmapHasAlpha = true;
                    imageWidth = bufferedimage.getWidth();
                    imageHeight = bufferedimage.getHeight();
                    bitmap = new int[imageWidth * imageHeight * 4];
                    Raster raster2 = bufferedimage.getData();
                    bitmap = raster2.getPixels(0, 0, imageWidth, imageHeight, bitmap);
                } else
                {
                    throw new ConversionException("32 bit image: " + colormodel.getPixelSize());
                }
            }
            catch(IOException ioexception)
            {
                throw new ConversionException("bla");
            }
    }

    public void writeBLP(String s)
        throws IOException, ConversionException
    {
        int k3 = 1172;
        int l3 = imageWidth;
        int j4 = imageHeight;
        int i;
        for(i = 0; l3 >= 1; i++)
            l3 /= 2;

        int j3;
        for(j3 = 0; j4 >= 1; j3++)
            j4 /= 2;

        int l4 = i <= j3 ? i : j3;
        System.out.println(l4 + " mipmaps");
        l3 = imageWidth;
        j4 = imageHeight;
        if(bitmapType == 1)
        {
            if(!bitmapHasAlpha)
            {
                for(int j = 0; j < l4; j++)
                {
                    k3 += l3 * j4;
                    l3 /= 2;
                    j4 /= 2;
                }

                ArrayWriter arraywriter = new ArrayWriter(k3);
                arraywriter.writeByte(66);
                arraywriter.writeByte(76);
                arraywriter.writeByte(80);
                arraywriter.writeByte(50);
                arraywriter.writeInt(1);
                arraywriter.writeShort(1);
                arraywriter.writeByte(8);
                arraywriter.writeByte(1);
                arraywriter.writeInt(imageWidth);
                arraywriter.writeInt(imageHeight);
                l3 = imageWidth;
                j4 = imageHeight;
                int i5 = 1172;
                for(int k = 0; k < l4; k++)
                {
                    arraywriter.writeInt(i5);
                    i5 += l3 * j4;
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter.seek(84);
                l3 = imageWidth;
                j4 = imageHeight;
                for(int l = 0; l < l4; l++)
                {
                    arraywriter.writeInt(l3 * j4);
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter.seek(148);
                arraywriter.writeBytes(palette);
                l3 = imageWidth;
                j4 = imageHeight;
                int i6 = 1 << l4;
                for(int i7 = 1; i7 < 1 << l4; i7 *= 2)
                    arraywriter.writeBytes(getMipmap(i7));

                arraywriter.store(s);
            } else
            {
                for(int i1 = 0; i1 < l4; i1++)
                {
                    k3 += l3 * j4 * 2;
                    l3 /= 2;
                    j4 /= 2;
                }

                ArrayWriter arraywriter1 = new ArrayWriter(k3);
                arraywriter1.writeByte(66);
                arraywriter1.writeByte(76);
                arraywriter1.writeByte(80);
                arraywriter1.writeByte(50);
                arraywriter1.writeInt(1);
                arraywriter1.writeByte(1);
                arraywriter1.writeByte(8);
                arraywriter1.writeByte(8);
                arraywriter1.writeByte(1);
                arraywriter1.writeInt(imageWidth);
                arraywriter1.writeInt(imageHeight);
                l3 = imageWidth;
                j4 = imageHeight;
                int j5 = 1172;
                for(int j1 = 0; j1 < l4; j1++)
                {
                    arraywriter1.writeInt(j5);
                    j5 += l3 * j4 * 2;
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter1.seek(84);
                l3 = imageWidth;
                j4 = imageHeight;
                for(int k1 = 0; k1 < l4; k1++)
                {
                    arraywriter1.writeInt(l3 * j4 * 2);
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter1.seek(148);
                arraywriter1.writeBytes(palette);
                l3 = imageWidth;
                j4 = imageHeight;
                int j6 = 1 << l4;
                for(int j7 = 1; j7 < 1 << l4; j7 *= 2)
                {
                    int ai[] = getMipmap(j7);
                    arraywriter1.writeBytes(ai);
                    arraywriter1.writeBytes(getAlphaChannel(ai, transparentColor));
                }

                arraywriter1.store(s);
            }
        } else
        if(bitmapType == 2)
            if(!bitmapHasAlpha)
            {
                for(int l1 = 0; l1 < l4; l1++)
                {
                    k3 += l3 * j4 <= 8 ? 8 : l3 * j4;
                    l3 /= 2;
                    j4 /= 2;
                }

                ArrayWriter arraywriter2 = new ArrayWriter(k3);
                arraywriter2.writeByte(66);
                arraywriter2.writeByte(76);
                arraywriter2.writeByte(80);
                arraywriter2.writeByte(50);
                arraywriter2.writeInt(1);
                arraywriter2.writeShort(2);
                arraywriter2.writeByte(0);
                arraywriter2.writeByte(1);
                arraywriter2.writeInt(imageWidth);
                arraywriter2.writeInt(imageHeight);
                l3 = imageWidth;
                j4 = imageHeight;
                int k5 = 1172;
                for(int i2 = 0; i2 < l4; i2++)
                {
                    arraywriter2.writeInt(k5);
                    k5 += l3 * j4 <= 8 ? 8 : l3 * j4;
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter2.seek(84);
                l3 = imageWidth;
                j4 = imageHeight;
                for(int j2 = 0; j2 < l4; j2++)
                {
                    arraywriter2.writeInt(l3 * j4 <= 8 ? 8 : l3 * j4);
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter2.seek(1172);
                int k6 = 1 << l4;
                for(int k7 = 1; k7 < 1 << l4; k7 *= 2)
                {
                    l3 = imageWidth / k7;
                    j4 = imageHeight / k7;
                    arraywriter2.writeBytes(DXTCFormat.encodeDXT1(l3, j4, getMipmap(k7)));
                }

                arraywriter2.store(s);
            } else
            {
                for(int k2 = 0; k2 < l4; k2++)
                {
                    k3 += l3 * j4 <= 16 ? 16 : l3 * j4;
                    l3 /= 2;
                    j4 /= 2;
                }

                ArrayWriter arraywriter3 = new ArrayWriter(k3);
                arraywriter3.writeByte(66);
                arraywriter3.writeByte(76);
                arraywriter3.writeByte(80);
                arraywriter3.writeByte(50);
                arraywriter3.writeInt(1);
                arraywriter3.writeByte(2);
                arraywriter3.writeByte(8);
                arraywriter3.writeByte(1);
                arraywriter3.writeByte(1);
                arraywriter3.writeInt(imageWidth);
                arraywriter3.writeInt(imageHeight);
                l3 = imageWidth;
                j4 = imageHeight;
                int l5 = 1172;
                for(int l2 = 0; l2 < l4; l2++)
                {
                    arraywriter3.writeInt(l5);
                    l5 += l3 * j4 <= 16 ? 16 : l3 * j4;
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter3.seek(84);
                l3 = imageWidth;
                j4 = imageHeight;
                for(int i3 = 0; i3 < l4; i3++)
                {
                    arraywriter3.writeInt(l3 * j4 <= 16 ? 16 : l3 * j4);
                    l3 /= 2;
                    j4 /= 2;
                }

                arraywriter3.seek(1172);
                int l6 = 1 << l4;
                for(int l7 = 1; l7 < 1 << l4; l7 *= 2)
                {
                    int i4 = imageWidth / l7;
                    int k4 = imageHeight / l7;
                    arraywriter3.writeBytes(DXTCFormat.encodeDXT2(i4, k4, getMipmap(l7)));
                }

                arraywriter3.store(s);
            }
    }

    private int[] getMipmap(int i)
        throws ConversionException
    {
        if(i == 1)
            return bitmap;
        int j = imageWidth / i;
        int k = imageHeight / i;
        if(j == 0)
            j = 1;
        if(k == 0)
            k = 1;
        int ai1[] = new int[3];
        int ai[];
        if(bitmapType == 1)
        {
            ai = new int[j * k];
            for(int l = 0; l < k; l++)
            {
                for(int k1 = 0; k1 < j; k1++)
                    ai[l * j + k1] = bitmap[l * imageWidth * i + k1 * i];

            }

        } else
        if(bitmapType == 2)
        {
            if(!bitmapHasAlpha)
            {
                ai = new int[j * k * 3];
                for(int i1 = 0; i1 < k; i1++)
                {
                    for(int l1 = 0; l1 < j; l1++)
                    {
                        ai[(i1 * j + l1) * 3] = bitmap[(i1 * imageWidth * i + l1 * i) * 3];
                        ai[(i1 * j + l1) * 3 + 1] = bitmap[(i1 * imageWidth * i + l1 * i) * 3 + 1];
                        ai[(i1 * j + l1) * 3 + 2] = bitmap[(i1 * imageWidth * i + l1 * i) * 3 + 2];
                    }

                }

            } else
            {
                ai = new int[j * k * 4];
                for(int j1 = 0; j1 < k; j1++)
                {
                    for(int i2 = 0; i2 < j; i2++)
                    {
                        ai[(j1 * j + i2) * 4] = bitmap[(j1 * imageWidth * i + i2 * i) * 4];
                        ai[(j1 * j + i2) * 4 + 1] = bitmap[(j1 * imageWidth * i + i2 * i) * 4 + 1];
                        ai[(j1 * j + i2) * 4 + 2] = bitmap[(j1 * imageWidth * i + i2 * i) * 4 + 2];
                        ai[(j1 * j + i2) * 4 + 3] = bitmap[(j1 * imageWidth * i + i2 * i) * 4 + 3];
                    }

                }

            }
        } else
        {
            throw new ConversionException("Critical Error: type in encoding");
        }
        return ai;
    }

    private byte[] getAlphaChannel(int ai[], int i)
        throws ConversionException
    {
        byte abyte0[] = new byte[ai.length];
        for(int j = 0; j < ai.length; j++)
            if(ai[j] == i)
                abyte0[j] = 0;
            else
                abyte0[j] = -1;

        return abyte0;
    }

    private int findClosestPaletteColor(int ai[])
    {
        int i = 0;
        int j = 0x20ef486;
        for(int l = 0; l < 256; l++)
        {
            int k = sqr(ai[0] - palette[l * 3]) + sqr(ai[1] - palette[l * 3 + 1]) + sqr(ai[2] - palette[l * 3 + 2]);
            if(k < j)
            {
                j = k;
                i = l;
            }
        }

        return i;
    }

    private int sqr(int i)
    {
        return i * i;
    }
}
