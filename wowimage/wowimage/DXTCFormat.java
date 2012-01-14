// FrontEnd Plus GUI for JAD
// DeCompiled : DXTCFormat.class
package wowimage;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class DXTCFormat
{

    public DXTCFormat()
    {
    }

    public static int[] decodeDXT1(int i, int j, byte abyte0[])
    {
        int ai[] = new int[i * j * 3];
        int j2 = 0;
        int ai1[][] = new int[4][];
        for (int l = 0; l < j / 4; l++)
        {
            for (int k = 0; k < i / 4; k++)
            {
                int k2 = unsigned(abyte0[j2]) + unsigned(abyte0[j2 + 1]) * 256;
                int l2 = unsigned(abyte0[j2 + 2]) + unsigned(abyte0[j2 + 3]) * 256;
                ai1[0] = decodeColor(k2);
                ai1[1] = decodeColor(l2);
                ai1[2] = new int[3];
                ai1[3] = new int[3];
                if (k2 > l2)
                {
                    for (int i1 = 0; i1 < 3; i1++)
                    {
                        ai1[2][i1] = (2 * ai1[0][i1] + ai1[1][i1] + 1) / 3;
                    }

                    for (int j1 = 0; j1 < 3; j1++)
                    {
                        ai1[3][j1] = (ai1[0][j1] + 2 * ai1[1][j1] + 1) / 3;
                    }

                }
                else
                {
                    for (int k1 = 0; k1 < 3; k1++)
                    {
                        ai1[2][k1] = (ai1[0][k1] + ai1[1][k1]) / 2;
                    }

                    ai1[3] = decodeColor(0);
                }
                for (int i2 = 0; i2 < 4; i2++)
                {
                    for (int l1 = 0; l1 < 4; l1++)
                    {
                        int i3 = dxtcPixel(abyte0, j2 + 4, l1, i2);
                        ai[((l * 4 + i2) * i + k * 4 + l1) * 3 + 0] = ai1[i3][0];
                        ai[((l * 4 + i2) * i + k * 4 + l1) * 3 + 1] = ai1[i3][1];
                        ai[((l * 4 + i2) * i + k * 4 + l1) * 3 + 2] = ai1[i3][2];
                    }

                }

                j2 += 8;
            }

        }

        return ai;
    }

    public static byte[] encodeDXT1(int i, int j, int ai[])
    {
        byte abyte0[] = new byte[(i * j) / 2];
        int i3 = 0;
        int ai1[] = new int[3];
        int ai2[] = new int[3];
        int ai3[] = new int[3];
        int ai4[][] = new int[4][3];
        int ai5[][] = new int[16][3];
        for (int l = 0; l < j / 4; l++)
        {
            for (int k = 0; k < i / 4; k++)
            {
                int j2 = 0;
                int l2 = 0;
                for (; j2 < 4; j2++)
                {
                    for (int i1 = 0; i1 < 4;)
                    {
                        ai5[l2][0] = ai[((l * 4 + j2) * i + k * 4 + i1) * 3 + 0];
                        ai5[l2][1] = ai[((l * 4 + j2) * i + k * 4 + i1) * 3 + 1];
                        ai5[l2][2] = ai[((l * 4 + j2) * i + k * 4 + i1) * 3 + 2];
                        i1++;
                        l2++;
                    }

                }

                int k4 = 0;
                int l4 = 0;
                int i5 = 0;
                for (int j1 = 0; j1 < 15; j1++)
                {
                    int j4 = furthestColor(ai5, j1);
                    int j5 = colorDistance(ai5[j1], ai5[j4]);
                    if (j5 > i5)
                    {
                        i5 = j5;
                        k4 = j1;
                        l4 = j4;
                    }
                }

                System.arraycopy(ai5[k4], 0, ai4[0], 0, 3);
                System.arraycopy(ai5[l4], 0, ai4[1], 0, 3);
                int l3 = encodeColor(ai4[0]);
                int i4 = encodeColor(ai4[1]);
                if (l3 == i4)
                {
                    l3++;
                }
                else if (l3 < i4)
                {
                    int k5 = l3;
                    l3 = i4;
                    i4 = k5;
                    System.arraycopy(ai4[0], 0, ai4[2], 0, 3);
                    System.arraycopy(ai4[1], 0, ai4[0], 0, 3);
                    System.arraycopy(ai4[2], 0, ai4[1], 0, 3);
                }
                for (int k1 = 0; k1 < 3; k1++)
                {
                    ai4[2][k1] = (2 * ai4[0][k1] + ai4[1][k1] + 1) / 3;
                }

                for (int l1 = 0; l1 < 3; l1++)
                {
                    ai4[3][l1] = (ai4[0][l1] + 2 * ai4[1][l1] + 1) / 3;
                }

                abyte0[i3] = (byte) (l3 % 256);
                abyte0[i3 + 1] = (byte) (l3 / 256);
                abyte0[i3 + 2] = (byte) (i4 % 256);
                abyte0[i3 + 3] = (byte) (i4 / 256);
                for (int k2 = 0; k2 < 4; k2++)
                {
                    int j3 = 0;
                    for (int i2 = 0; i2 < 4; i2++)
                    {
                        int k3 = closestColor(ai4, ai5[k2 * 4 + i2]);
                        j3 += k3 << i2 * 2;
                    }

                    abyte0[i3 + 4 + k2] = (byte) j3;
                }

                i3 += 8;
            }

        }

        return abyte0;
    }

    public static byte[] encodeDXT2(int i, int j, int ai[])
    {
        PrintStream printstream = null;
        try
        {
            printstream = new PrintStream(new FileOutputStream("debug.txt"));
        } catch (Exception exception)
        {
        }
        System.out.println("w = " + i + " h = " + j);
        System.out.println(ai.length);
        if (i < 4 || j < 4)
        {
            System.out.println("padding");
            int ai1[] = new int[64];
            for (int k = 0; k < 4; k++)
            {
                for (int i1 = 0; i1 < 4; i1++)
                {
                    ai1[k * 4 + i1] = ai[((k * j) / 4) * 4 + (i1 * i) / 4];
                }

            }

            ai = ai1;
            i = 4;
            j = 4;
        }
        byte abyte0[] = new byte[i * j];
        System.out.println("w = " + i + " h = " + j);
        int l3 = 0;
        int ai2[] = new int[3];
        int ai3[] = new int[3];
        int ai4[] = new int[3];
        int ai5[][] = new int[4][3];
        int ai6[][] = new int[16][3];
        int ai7[] = new int[16];
        for (int j1 = 0; j1 < j / 4; j1++)
        {
            for (int l = 0; l < i / 4; l++)
            {
                int l2 = 0;
                int k3 = 0;
                for (; l2 < 4; l2++)
                {
                    for (int k1 = 0; k1 < 4;)
                    {
                        ai6[k3][0] = ai[((j1 * 4 + l2) * i + l * 4 + k1) * 4 + 0];
                        ai6[k3][1] = ai[((j1 * 4 + l2) * i + l * 4 + k1) * 4 + 1];
                        ai6[k3][2] = ai[((j1 * 4 + l2) * i + l * 4 + k1) * 4 + 2];
                        ai7[k3] = ai[((j1 * 4 + l2) * i + l * 4 + k1) * 4 + 3];
                        k1++;
                        k3++;
                    }

                }

                int j5 = 0;
                int k5 = 0;
                int l5 = 0;
                for (int l1 = 0; l1 < 15; l1++)
                {
                    int i5 = furthestColor(ai6, l1);
                    int i6 = colorDistance(ai6[l1], ai6[i5]);
                    if (i6 > l5)
                    {
                        l5 = i6;
                        j5 = l1;
                        k5 = i5;
                    }
                }

                System.arraycopy(ai6[j5], 0, ai5[0], 0, 3);
                System.arraycopy(ai6[k5], 0, ai5[1], 0, 3);
                int k4 = encodeColor(ai5[0]);
                int l4 = encodeColor(ai5[1]);
                if (k4 == l4)
                {
                    if (k4 > 32768)
                    {
                        l4 = 0;
                    }
                    else
                    {
                        l4 = 65535;
                    }
                    ai5[1] = decodeColor(l4);
                }
                else if (k4 < l4)
                {
                    int j6 = k4;
                    k4 = l4;
                    l4 = j6;
                    System.arraycopy(ai5[0], 0, ai5[2], 0, 3);
                    System.arraycopy(ai5[1], 0, ai5[0], 0, 3);
                    System.arraycopy(ai5[2], 0, ai5[1], 0, 3);
                }
                for (int i2 = 0; i2 < 3; i2++)
                {
                    ai5[2][i2] = (2 * ai5[0][i2] + ai5[1][i2] + 1) / 3;
                }

                for (int j2 = 0; j2 < 3; j2++)
                {
                    ai5[3][j2] = (ai5[0][j2] + 2 * ai5[1][j2] + 1) / 3;
                }

                abyte0[l3 + 8] = (byte) (k4 % 256);
                abyte0[l3 + 9] = (byte) (k4 / 256);
                abyte0[l3 + 10] = (byte) (l4 % 256);
                abyte0[l3 + 11] = (byte) (l4 / 256);
                for (int i3 = 0; i3 < 4; i3++)
                {
                    int i4 = 0;
                    for (int k2 = 0; k2 < 4; k2++)
                    {
                        int j4 = closestColor(ai5, ai6[i3 * 4 + k2]);
                        i4 += j4 << k2 * 2;
                    }

                    abyte0[l3 + 12 + i3] = (byte) i4;
                }

                for (int j3 = 0; j3 < 8; j3++)
                {
                    abyte0[l3 + j3] = (byte) ((ai7[j3 * 2 + 1] / 16 << 4) + ai7[j3 * 2] / 16);
                }

                l3 += 16;
            }

        }

        return abyte0;
    }

    public static int[] decodeDXT2(int i, int j, byte abyte0[])
    {
        int ai[] = new int[i * j * 4];
        int j2 = 0;
        int ai1[][] = new int[4][];
        int ai2[] = new int[4];
        for (int l = 0; l < j / 4; l++)
        {
            for (int k = 0; k < i / 4; k++)
            {
                for (int i1 = 0; i1 < 4; i1++)
                {
                    ai2[i1] = unsigned(abyte0[j2 + 2 * i1]) + unsigned(abyte0[j2 + 2 * i1 + 1]) * 256;
                }

                int l2 = unsigned(abyte0[j2 + 8]) + unsigned(abyte0[j2 + 9]) * 256;
                int i3 = unsigned(abyte0[j2 + 10]) + unsigned(abyte0[j2 + 11]) * 256;
                ai1[0] = decodeColor(l2);
                ai1[1] = decodeColor(i3);
                ai1[2] = new int[3];
                ai1[3] = new int[3];
                for (int j1 = 0; j1 < 3; j1++)
                {
                    ai1[2][j1] = (2 * ai1[0][j1] + ai1[1][j1] + 1) / 3;
                }

                for (int k1 = 0; k1 < 3; k1++)
                {
                    ai1[3][k1] = (ai1[0][k1] + 2 * ai1[1][k1] + 1) / 3;
                }

                for (int i2 = 0; i2 < 4; i2++)
                {
                    for (int l1 = 0; l1 < 4; l1++)
                    {
                        int j3 = dxtcPixel(abyte0, j2 + 12, l1, i2);
                        ai[((l * 4 + i2) * i + k * 4 + l1) * 4 + 0] = ai1[j3][0];
                        ai[((l * 4 + i2) * i + k * 4 + l1) * 4 + 1] = ai1[j3][1];
                        ai[((l * 4 + i2) * i + k * 4 + l1) * 4 + 2] = ai1[j3][2];
                        int k2 = dxtcAlpha(ai2, l1, i2);
                        ai[((l * 4 + i2) * i + k * 4 + l1) * 4 + 3] = k2 != 15 ? k2 * 16 : 255;
                    }

                }

                j2 += 16;
            }

        }

        return ai;
    }

    private static int furthestColor(int ai[][], int i)
    {
        int j = 0;
        int l = 0;
        for (int i1 = 0; i1 < ai.length; i1++)
        {
            if (i1 == i)
            {
                continue;
            }
            int k = colorDistance(ai[i1], ai[i]);
            if (k > j)
            {
                j = k;
                l = i1;
            }
        }

        return l;
    }

    private static int closestColor(int ai[][], int ai1[])
    {
        int i = 0;
        for (int j = 1; j < ai.length; j++)
        {
            if (colorDistance(ai1, ai[j]) < colorDistance(ai1, ai[i]))
            {
                i = j;
            }
        }

        return i;
    }

    private static int colorDistance(int ai[], int ai1[])
    {
        return (ai[0] - ai1[0]) * (ai[0] - ai1[0]) + (ai[1] - ai1[1]) * (ai[1] - ai1[1]) + (ai[2] - ai1[2]) * (ai[2] - ai1[2]);
    }

    private static int dxtcPixel(byte abyte0[], int i, int j, int k)
    {
        j *= 2;
        int l = unsigned(abyte0[i + k]);
        int i1 = (l & 3 << j) >> j;
        return i1;
    }

    private static int dxtcAlpha(int ai[], int i, int j)
    {
        i *= 4;
        int k = ai[j];
        int l = (k & 15 << i) >> i;
        return l;
    }

    private static int[] decodeColor(int i)
    {
        int j = i & 0x1f;
        int k = (i & 0x7e0) / 32;
        int l = (i & 0xf800) / 2048;
        int ai[] = new int[3];
        ai[0] = l * 8;
        ai[1] = k * 4;
        ai[2] = j * 8;
        return ai;
    }

    private static int encodeColor(int ai[])
    {
        return ai[2] / 8 + (ai[1] / 4) * 32 + (ai[0] / 8) * 2048;
    }

    private static int unsigned(byte byte0)
    {
        return byte0 < 0 ? 256 + byte0 : byte0;
    }
}
