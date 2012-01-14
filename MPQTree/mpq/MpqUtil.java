/*
 * Copyright (C) 2011 Thedeath<www.fseek.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpq;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mwt.wow.mpq.MpqFile;
import wowimage.BLPFile;
import wowimage.ConversionException;

public class MpqUtil
{

    // if rekursivly = true all directorys in the directory also scanned
    public static ExtMpqArchive[] getMPQFiles(File directory, boolean rekursivly)
    {
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("The given file isnt a directory !");
        }
        return getMpqFilesFromDir(directory, rekursivly);
    }

    private static ExtMpqArchive[] getMpqFilesFromDir(File directory, boolean rekursivly)
    {
        
        ExtMpqArchive[] archives = null;
        if(rekursivly)
        {
            File[] listFiles = directory.listFiles(new MpqFileFilter(true));
            archives = getMpqFilesFromDirRek(listFiles);
        }
        else
        {
            File[] listFiles = directory.listFiles(new MpqFileFilter());
            archives = getMpqFilesFromDirImpl(listFiles);
        }
        return archives;
    }
    
    private static ExtMpqArchive[] getMpqFilesFromDirImpl(File[] listFiles)
    {
        ExtMpqArchive[] archives = new ExtMpqArchive[listFiles.length];
        for (int i = 0; i < listFiles.length; i++)
        {
            try
            {
                archives[i] = new ExtMpqArchive(listFiles[i]);
            } catch (IOException ex)
            {
                // file isnt a "real" mpq file (?)
                Logger.getLogger(MpqUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return archives;
    }
    
    
    private static ExtMpqArchive[] getMpqFilesFromDirRek(File[] listFiles)
    {
        ArrayList<ExtMpqArchive> archives = new ArrayList<ExtMpqArchive>();
        archives = getMpqFilesFromDirRekImpl(listFiles, archives);
        Object[] toArray = archives.toArray();
        ExtMpqArchive[] archs = new ExtMpqArchive[toArray.length];
        for(int i = 0; i<toArray.length; i++)
        {
            archs[i] = (ExtMpqArchive)toArray[i];
        }
        return archs;
    }
    
    private static ArrayList<ExtMpqArchive> getMpqFilesFromDirRekImpl(File[] listFiles, ArrayList<ExtMpqArchive> archives)
    {
        for (int i = 0; i < listFiles.length; i++)
        {
            try
            {
                File f = listFiles[i];
                if(f.isDirectory())
                {
                    getMpqFilesFromDirRekImpl(f.listFiles(new MpqFileFilter(true)), archives);
                }
                else
                {
                    archives.add(new ExtMpqArchive(f));
                }
            } catch (IOException ex)
            {
                // file isnt a "real" mpq file (?)
                Logger.getLogger(MpqUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return archives;
    }
  
    public static BufferedImage convertMpqFileToImage(MpqFile file) throws ConversionException, IOException
    {
        File createTempFile = File.createTempFile(getMpqFileName(file), null);
        file.extractTo(createTempFile);
        BufferedImage convertFileToImage = convertFileToImage(createTempFile);
        boolean delete = createTempFile.delete();
        if(delete == false)
        {
            createTempFile.deleteOnExit();
        }
        return convertFileToImage;
    }
    
    public static BufferedImage convertFileToImage(File file) throws ConversionException, IOException
    {
        BLPFile blpFile = new BLPFile(file);
        BufferedImage img = blpFile.getImg();
        return img;
    }
    
    public static String getMpqFileName(MpqFile file)
    {
        String[] split = file.getFilePath().split("\\\\");
        String mpqFileName = split[split.length-1];
        return mpqFileName;
    }
}