// FrontEnd Plus GUI for JAD
// DeCompiled : BLPDecoder.class
package wowimage;

import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

// Referenced classes of package wowimage:
//            ConversionException, ArrayReader, ArrayWriter, DXTCFormat
public class BLPDecoder
{

    int bitmapType;
    int imageWidth;
    int imageHeight;
    boolean bitmapHasAlpha;
    final int TYPE_PALETTE = 1;
    final int TYPE_RGB = 2;
    byte palette[];
    int bitmap[];
    byte alphaChannel[];
    int mipmapOffsets[];
    int mipmapLengths[];

    public BLPDecoder(String s)
    throws ConversionException
    {
        File file = new File(s);
        vBLPDecoder(file);
    }

    public BLPDecoder(File file)
    throws ConversionException
    {
        vBLPDecoder(file);
    }

    private void vBLPDecoder(File file)
    throws ConversionException
    {

        int i1 = (int) file.length();
        byte abyte0[] = new byte[i1];
        try
        {
            FileInputStream fileinputstream = new FileInputStream(file.getAbsoluteFile());
            fileinputstream.read(abyte0);
            fileinputstream.close();
        } catch (IOException ioexception)
        {
            throw new ConversionException("Error reading " + file.getAbsoluteFile());
        }
        ArrayReader arrayreader = new ArrayReader(abyte0);
        if (arrayreader.readByte() != 66 || arrayreader.readByte() != 76 || arrayreader.readByte() != 80 || arrayreader.readByte() != 50)
        {
            throw new ConversionException(file.getAbsoluteFile() + " is not a BLP2 file");
        }
        /*
        struct BLP2Header
        {
        FourCC      ID; // Always 'BLP2'
        UInt32      Type;
        UInt8       Encoding;
        UInt8       AlphaDepth;
        UInt8       AlphaEncoding;
        UInt8       HasMips;
        UInt32      Width;
        UInt32      Height;
        UInt32      Offsets[16];
        UInt32      Lengths[16];
        RGBAColor8  Palette[256];
        }; /**/

        // type = 1
        // encoding = 2
        // alphaDepth = 1
        // alphaEncoding = 0
        // HashMips = 1
        int blpType = arrayreader.readInt();
        int blpEncoding = arrayreader.readByte();
        int alphaDepth = arrayreader.readByte();
        int alphaEncoding = arrayreader.readByte();
        int hashMips = arrayreader.readByte();
        imageWidth = arrayreader.readInt();
        imageHeight = arrayreader.readInt();
        mipmapOffsets = new int[16];
        for (int i = 0; i < 16; i++)
        {
            mipmapOffsets[i] = arrayreader.readInt();
        }

        mipmapLengths = new int[16];
        for (int j = 0; j < 16; j++)
        {
            mipmapLengths[j] = arrayreader.readInt();
        }

        if (blpEncoding == 1)
        {
            if (alphaEncoding != 8)
            {
                throw new ConversionException("Indexed bitmaps with " + alphaEncoding + " bpp are not supported");
            }
        }
        else if (blpEncoding == 2)
        {
            if (alphaDepth != 0 && alphaDepth != 8 && alphaDepth != 1)
            {
                throw new ConversionException("DXT2 compressed images aren't supported yet");
            }
        }
        else
        {
            throw new ConversionException("This type of bitmap compression (" + blpEncoding + ") isn't supported yet");
        }
        if (hashMips != 1 && hashMips != 2 && hashMips != 0)
        {
            throw new ConversionException("Critical Error: unknown4 = " + hashMips + " dataformat = " + blpEncoding);
        }
        if (blpEncoding == 1)
        {
            if (hashMips == 0)
            {
                throw new ConversionException("Critical Error: dataFormat == 1 && unknown4 = " + hashMips);
            }
            arrayreader.seek(148);
            palette = arrayreader.readBytes(1024);
            if (alphaDepth == 0)
            {
                bitmapType = 1;
                bitmapHasAlpha = false;
                arrayreader.seek(mipmapOffsets[0]);
                if (imageWidth * imageHeight != mipmapLengths[0])
                {
                    throw new ConversionException("Critical Error: imageSize = " + imageWidth * imageHeight + ", mipmapSize = " + mipmapLengths[0]);
                }
                byte abyte1[] = arrayreader.readBytes(mipmapLengths[0]);
                bitmap = new int[imageWidth * imageHeight];
                for (int k = 0; k < imageWidth * imageHeight; k++)
                {
                    bitmap[k] = abyte1[k];
                }

            }
            else if (alphaDepth == 8)
            {
                bitmapType = 1;
                bitmapHasAlpha = true;
                if (imageWidth * imageHeight * 2 != mipmapLengths[0])
                {
                    throw new ConversionException("Critical Error: alpha imageSize = " + imageWidth * imageHeight + ", mipmapSize = " + mipmapLengths[0]);
                }
                arrayreader.seek(mipmapOffsets[0]);
                byte abyte2[] = arrayreader.readBytes(mipmapLengths[0] / 2);
                bitmap = new int[imageWidth * imageHeight];
                for (int l = 0; l < imageWidth * imageHeight; l++)
                {
                    bitmap[l] = abyte2[l];
                }

                arrayreader.seek(mipmapOffsets[0] + mipmapLengths[0] / 2);
                alphaChannel = arrayreader.readBytes(mipmapLengths[0] / 2);
            }
        }
        else if (blpEncoding == 2)
        {
            if (alphaDepth == 0)
            {
                /*if(j2 == 0) {
                throw new ConversionException("Critical Error: dataFormat == 2 && unknown4 = " + j2);
                }/**/
                bitmapType = 2;
                bitmapHasAlpha = false;
                arrayreader.seek(mipmapOffsets[0]);
                if ((imageWidth * imageHeight) / 2 != mipmapLengths[0])
                {
                    throw new ConversionException("Critical Error: imageSize = " + imageWidth * imageHeight + ", mipmapSize = " + mipmapLengths[0] + ", maybe not DXT1");
                }
                byte abyte3[] = arrayreader.readBytes(mipmapLengths[0]);
                bitmap = DXTCFormat.decodeDXT1(imageWidth, imageHeight, abyte3);
            }
            else if (alphaDepth == 8)
            {
                bitmapType = 2;
                bitmapHasAlpha = true;
                arrayreader.seek(mipmapOffsets[0]);
                if (imageWidth * imageHeight != mipmapLengths[0])
                {
                    throw new ConversionException("Critical Error: imageSize = " + imageWidth * imageHeight + ", mipmapSize = " + mipmapLengths[0] + ", maybe not DXT1");
                }
                byte abyte4[] = arrayreader.readBytes(mipmapLengths[0]);
                bitmap = DXTCFormat.decodeDXT2(imageWidth, imageHeight, abyte4);
            }
            else if (alphaDepth == 1)
            {
                /*if(j2 == 0) {
                throw new ConversionException("Critical Error: dataFormat == 2 && unknown4 = " + j2);
                }/**/
                bitmapType = 2;
                bitmapHasAlpha = true;
                arrayreader.seek(mipmapOffsets[0]);
                if ((imageWidth * imageHeight) / 2 != mipmapLengths[0])
                {
                    throw new ConversionException("Critical Error: imageSize = " + imageWidth * imageHeight + ", mipmapSize = " + mipmapLengths[0] + ", maybe not DXT1");
                }
                byte abyte4[] = arrayreader.readBytes(mipmapLengths[0]);
                bitmap = DXTCFormat.decodeDXT1(imageWidth, imageHeight, abyte4);
            }
        }
    }

    public BufferedImage writePNG(String s)
    throws IOException, ConversionException
    {
        BufferedImage bufferedimage = null;

        if (bitmapType == 1)
        {
            if (bitmapHasAlpha)
            {
                int i = -1;
                int j = -1;
                byte abyte2[] = new byte[256];
                byte abyte4[] = new byte[256];
                byte abyte5[] = new byte[256];
                for (int l = 0; l < 256; l++)
                {
                    abyte5[l] = palette[l * 4];
                    abyte4[l] = palette[l * 4 + 1];
                    abyte2[l] = palette[l * 4 + 2];
                }

                int ai[][] = new int[256][256];
                int i1 = 0;
                do
                {
                    if (i1 >= 256)
                    {
                        break;
                    }
                    int k1 = i1 + 1;
                    do
                    {
                        if (k1 >= 256)
                        {
                            break;
                        }
                        if (abyte2[i1] - abyte2[k1] == 0 && abyte4[i1] - abyte4[k1] == 0 && abyte5[i1] - abyte5[k1] == 0)
                        {
                            j = i1;
                            i = k1;
                            break;
                        }
                        i1++;
                    } while (true);
                    if (i != -1)
                    {
                        break;
                    }
                    i1++;
                } while (true);
                if (i == -1)
                {
                    throw new ConversionException("Couldn't find transparency color");
                }
                IndexColorModel indexcolormodel1 = new IndexColorModel(8, 256, abyte2, abyte4, abyte5, i);
                for (int j1 = 0; j1 < imageWidth * imageHeight; j1++)
                {
                    if (bitmap[j1] == i)
                    {
                        bitmap[j1] = j;
                    }
                    if (alphaChannel[j1] >= 0 && alphaChannel[j1] < 112)
                    {
                        bitmap[j1] = i;
                    }
                }

                bufferedimage = new BufferedImage(imageWidth, imageHeight, 13, indexcolormodel1);
                WritableRaster writableraster3 = bufferedimage.getRaster();
                try
                {
                    writableraster3.setPixels(0, 0, imageWidth, imageHeight, bitmap);
                }catch(ArrayIndexOutOfBoundsException ex){};
                if (s.length() > 3)
                {
                    File file3 = new File(s);
                    ImageIO.write(bufferedimage, "png", file3);
                }
            }
            else
            {
                byte abyte0[] = new byte[256];
                byte abyte1[] = new byte[256];
                byte abyte3[] = new byte[256];
                for (int k = 0; k < 256; k++)
                {
                    abyte3[k] = palette[k * 4];
                    abyte1[k] = palette[k * 4 + 1];
                    abyte0[k] = palette[k * 4 + 2];
                }

                IndexColorModel indexcolormodel = new IndexColorModel(8, 256, abyte0, abyte1, abyte3);
                bufferedimage = new BufferedImage(imageWidth, imageHeight, 13, indexcolormodel);
                WritableRaster writableraster2 = bufferedimage.getRaster();
                writableraster2.setPixels(0, 0, imageWidth, imageHeight, bitmap);
                if (s.length() > 3)
                {
                    File file2 = new File(s);
                    ImageIO.write(bufferedimage, "png", file2);
                }

            }
        }
        else if (bitmapType == 2)
        {
            if (bitmapHasAlpha)
            {
                bufferedimage = new BufferedImage(imageWidth, imageHeight, 2);
                WritableRaster writableraster = bufferedimage.getRaster();
                try
                {
                    writableraster.setPixels(0, 0, imageWidth, imageHeight, bitmap);
                }catch(ArrayIndexOutOfBoundsException ex)
                {
                    ex.printStackTrace();
                }
                if (s.length() > 3)
                {
                    File file = new File(s);
                    ImageIO.write(bufferedimage, "png", file);
                }
            }
            else
            {
                bufferedimage = new BufferedImage(imageWidth, imageHeight, 1);
                WritableRaster writableraster1 = bufferedimage.getRaster();
                writableraster1.setPixels(0, 0, imageWidth, imageHeight, bitmap);
                if (s.length() > 3)
                {
                    File file1 = new File(s);
                    ImageIO.write(bufferedimage, "png", file1);
                }
            }
        }

        return bufferedimage;
    }

    public void writeBMP(String s)
    throws IOException
    {
        int i = imageWidth * imageHeight + 40 + 14 + 1024 + 2;
        ArrayWriter arraywriter = new ArrayWriter(i);
        arraywriter.writeByte(66);
        arraywriter.writeByte(77);
        arraywriter.writeInt(i);
        arraywriter.writeInt(0);
        arraywriter.writeInt(1078);
        arraywriter.writeInt(40);
        arraywriter.writeInt(imageWidth);
        arraywriter.writeInt(imageHeight);
        arraywriter.writeShort(1);
        arraywriter.writeShort(8);
        arraywriter.writeInt(0);
        arraywriter.writeInt(imageWidth * imageHeight + 2);
        arraywriter.writeInt(2834);
        arraywriter.writeInt(2834);
        arraywriter.writeInt(0);
        arraywriter.writeInt(0);
        arraywriter.writeBytes(palette);
        for (int j = 0; j < bitmap.length; j += imageWidth)
        {
            arraywriter.writeBytes(bitmap, bitmap.length - imageWidth - j, imageWidth);
        }

        arraywriter.writeShort(0);
        arraywriter.store(s);
    }

    private int[] getMipmap(int i)
    {
        if (i == 1)
        {
            return bitmap;
        }
        int j = imageWidth / i;
        int k = imageHeight / i;
        if (j == 0)
        {
            j = 1;
        }
        if (k == 0)
        {
            k = 1;
        }
        int ai[] = new int[j * k];
        for (int l = 0; l < k; l++)
        {
            for (int i1 = 0; i1 < j; i1++)
            {
                ai[l * j + i1] = bitmap[l * j * i + i1 * i];
            }

        }

        return ai;
    }

    private static int unsigned(byte byte0)
    {
        return byte0 < 0 ? 256 + byte0 : byte0;
    }
}
