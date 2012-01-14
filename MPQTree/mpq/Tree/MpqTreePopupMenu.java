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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;
import mpq.ExtMpqArchive;
import mpq.MpqUtil;
import mwt.wow.mpq.MpqFile;
import wowimage.ConversionException;

public class MpqTreePopupMenu extends JPopupMenu
{
    public MpqTreePopupMenu(MpqFile file)
    {
        showMpqFileMenu(file);
    }
    
    public MpqTreePopupMenu(MpqTree tree)
    {
        showMpqArchiveFileMenu(tree);
    }

    private void showMpqFileMenu(MpqFile file)
    {
        this.add(createSaveAsItem(file));
    }
    
    private void showMpqArchiveFileMenu(MpqTree tree)
    {
        this.add(createDeleteItem(tree));
    }
    
    private JMenuItem createDeleteItem(final MpqTree tree)
    {
        JMenuItem item = new JMenuItem("Remove");
        item.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                tree.removeSelectedArchives();
            }
        });
        return item;
    }

    private JMenuItem createSaveAsItem(final MpqFile file)
    {
        JMenuItem item = new JMenuItem("Save as...");
        item.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveAs(file);
            }
        });
        return item;
    }

    private void saveAs(MpqFile file)
    {
        while(true)
        {
            JFileChooser chooser = createSaveChooser();
            int showSaveDialog = chooser.showSaveDialog(null);
            if (showSaveDialog == JFileChooser.APPROVE_OPTION)
            {
                File selectedFile = chooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                String extension = findExtension(chooser);
                if (!path.endsWith(extension))
                {
                    selectedFile = new File(path + extension);
                }
                // JOptionPane.CANCEL_OPTION = -1,JOptionPane.NO_OPTION = 0; JOptionPane.YES_OPTION = 1
                int handleFileExists = 1;
                if(selectedFile.exists())
                {
                    handleFileExists = handleFileExists();
                }
                if(handleFileExists == -1)return;
                if(handleFileExists == 1)
                {
                    saveFile(selectedFile, file);
                    break;
                }
            }
            else
            {
                break;
            }
        }
    }
    
    private void saveFile(File selectedFile, MpqFile file)
    {
        String absolutePath = selectedFile.getAbsolutePath();
        int lastIndexOf = absolutePath.lastIndexOf(".");
        if (lastIndexOf != -1)
        {
            try
            {
                String suffix = absolutePath.substring(lastIndexOf + 1, absolutePath.length());
                if(suffix.toLowerCase().equals("blp"))
                {
                    file.extractTo(selectedFile);
                }
                else
                {
                    ImageIO.write(MpqUtil.convertMpqFileToImage(file), suffix, selectedFile);
                }
            } catch (ConversionException ex)
            {
                Logger.getLogger(MpqTreePopupMenu.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex)
            {
                Logger.getLogger(MpqTreePopupMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private String findExtension(JFileChooser chooser)
    {
        FileFilter filter = chooser.getFileFilter();
        FileFilter[] choosableFileFilters = chooser.getChoosableFileFilters();
        String extension = "";
        if(choosableFileFilters[0] == filter)
        {
            extension = ".blp";
        }
        else
        {
            extension = ".png";
        }
        return extension;
    }
    
    private JFileChooser createSaveChooser()
    {
        final JFileChooser chooser = new JFileChooser();
        FileFilter blpFileFilter = new FileFilter()
        {

            @Override
            public boolean accept(File f)
            {
                if (f.getAbsolutePath().toLowerCase().endsWith(".blp") || f.isDirectory())
                {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription()
            {
                return "Warcraft Skin File (.blp)";
            }
        };
        FileFilter pngFileFilter = new FileFilter()
        {

            @Override
            public boolean accept(File f)
            {
                if (f.getAbsolutePath().toLowerCase().endsWith(".png") || f.isDirectory())
                {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription()
            {
                return "Portable Network Graphic (.png)";
            }
        };
        FileFilter[] newFilter = new FileFilter[]
        {
            blpFileFilter, pngFileFilter
        };
        for (FileFilter f : newFilter)
        {
            chooser.addChoosableFileFilter(f);
        }
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        return chooser;
    }
    
    private int handleFileExists()
    {
        int showOptionDialog = JOptionPane.showOptionDialog(this, "This file exists already !\nDo you want to overwrite it ?", "File Exists !",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null);
        if(showOptionDialog == JOptionPane.CANCEL_OPTION)
        {
            return -1;
        }
        else if(showOptionDialog == JOptionPane.NO_OPTION)
        {
            return 0;
        }
        return 1;
    }
}
