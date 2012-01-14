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
package mpq.Tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import mpq.ExtMpqArchive;
import mpq.MpqUtil;

public class MpqTreeTransferHandler extends TransferHandler
{
    private MpqTree mpqTree;

    public MpqTreeTransferHandler(MpqTree mpqTree)
    {
        this.mpqTree = mpqTree;
    }
    
    
    @Override
    public boolean canImport(TransferSupport support)
    {
        DataFlavor[] dataFlavors = support.getDataFlavors();
        DataFlavor fl = dataFlavors[0];
        String mimeType = fl.getMimeType();
        if(fl == DataFlavor.javaFileListFlavor || mimeType.contains("application/x-java-file-list"))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        Transferable t = support.getTransferable();
        JTree.DropLocation loc = (JTree.DropLocation)support.getDropLocation();
        int childIndex = loc.getChildIndex();
        ArrayList<ExtMpqArchive> archives = new ArrayList<ExtMpqArchive>();
        try
        {
            ArrayList<File> files = getFiles(t);
            
            for(File f : files)
            {
                if(f.isDirectory())
                {
                    // if error try next
                    try
                    {
                        ExtMpqArchive[] mPQFiles = MpqUtil.getMPQFiles(f, true);
                        archives.addAll(Arrays.asList(mPQFiles));
                    }catch(Exception ex){};
                }
                // if error try next
                try
                {
                    archives.add(new ExtMpqArchive(f));
                }catch(Exception ex)
                {
                    ex.printStackTrace();
                };
            }
        } catch (UnsupportedFlavorException ex)
        {
            Logger.getLogger(MpqTreeTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex)
        {
            Logger.getLogger(MpqTreeTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        for(ExtMpqArchive arch : archives)
        {
            if(childIndex == -1)childIndex = 0;
            this.mpqTree.addMpqArchive(arch, childIndex);
            childIndex++;
        }
        return true;
    }
    
     public static ArrayList<File> getFiles(Transferable t) throws UnsupportedFlavorException, IOException, java.lang.ClassCastException
    {
        Object transferData = t.getTransferData(DataFlavor.javaFileListFlavor);
        if(transferData instanceof ArrayList)
        {
            ArrayList<File> list = (ArrayList<File>)transferData;
            return list;
        }
        // need to do that for windows explorer support, they send me a Arrays$ArrayLidt didnt find an other solution
        else
        {
            ArrayList<File> list = new ArrayList<File>();
            list.addAll((Collection) transferData);
            return list;
        }
    }
}
