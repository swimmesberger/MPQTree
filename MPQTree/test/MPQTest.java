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
package test;

import java.util.List;
import mpq.Tree.MpqTree;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import mpq.ExtMpqArchive;
import mpq.MpqUtil;

/**
 *
 * @author Thedeath<www.fseek.org>
 */
public class MPQTest
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int showOpenDialog = chooser.showOpenDialog(null);
        ArrayList<ExtMpqArchive> mPQFiles = new ArrayList<ExtMpqArchive>();
        if(showOpenDialog == JFileChooser.APPROVE_OPTION)
        {
            File[] selectedFile = chooser.getSelectedFiles();
            for(int i = 0; i<selectedFile.length; i++)
            {
                File f = selectedFile[i];
                if(f.isDirectory())
                {
                    ExtMpqArchive[] mPQFiles1 = MpqUtil.getMPQFiles(f, true);
                    mPQFiles.addAll(Arrays.asList(mPQFiles1));
                }
                else
                {
                    try
                    {
                        mPQFiles.add(new ExtMpqArchive(f));
                    }catch(Exception ex){}
                }
            }
        }
        ExtMpqArchive[] archives = new ExtMpqArchive[mPQFiles.size()];
        for(int i = 0; i<archives.length; i++)
        {
            archives[i] = mPQFiles.get(i);
        }
        JFrame frame = new JFrame();
        frame.setTitle("MpqTree");
        if(archives.length > 0)
        {
            frame.add(new JScrollPane(new MpqTree(archives)));
        }
        else
        {
            frame.add(new JScrollPane(new MpqTree()));
        }
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true); 
    }
}
