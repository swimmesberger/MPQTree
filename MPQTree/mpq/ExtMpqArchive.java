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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import mwt.wow.mpq.MpqArchive;
import mwt.wow.mpq.MpqFile;

public class ExtMpqArchive extends MpqArchive
{

    private boolean fileNamesLoaded = false;
    private ArrayList<String> fileNames;

    public ExtMpqArchive(File file) throws IOException
    {
        super(file);
    }

    public ExtMpqArchive(File file, boolean rw) throws IOException
    {
        super(file, rw);
    }

    public ArrayList<String> getFiles() throws IOException
    {
        if (fileNames == null)
        {
            fileNames = new ArrayList<String>();
        }
        if (fileNamesLoaded == false)
        {
            loadFileNames();
            fileNames.addAll(this.listFileNames());
        }
        return fileNames;
    }

    private void loadFileNames() throws IOException
    {
        MpqFile listFile = this.getFile("(listfile)", 0, 0);
        if (listFile != null)
        {
            InputStream inputStream = listFile.getInputStream();
            try
            {
                Scanner scan = new Scanner(inputStream, "UTF-8");
                scan.useDelimiter(Pattern.compile("[;\\r\\n]+"));
                while (scan.hasNext())
                {
                    String name = scan.next();
                    this.getFile(name, null, null);
                }
                scan.close();
            } finally
            {
                inputStream.close();
            }
        }
        this.fileNamesLoaded = true;
    }
}
