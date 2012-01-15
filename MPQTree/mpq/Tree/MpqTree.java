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

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import mpq.ExtMpqArchive;
import mpq.MpqArchiveComparator;
import mpq.MpqUtil;
import mwt.wow.mpq.MpqFile;
import wowimage.ConversionException;

public class MpqTree extends JTree
{

    private DefaultTreeModel model;
    private ExtMpqArchive[] archives;
    private HashMap<String, ExtMpqArchive> archiveMap = new HashMap<String, ExtMpqArchive>();
    private boolean finishedBuildTree = false;

    public MpqTree(ExtMpqArchive ext)
    {
        this(new ExtMpqArchive[]
                {
                    ext
                });
    }

    public MpqTree(ExtMpqArchive[] ext)
    {
        super();
        this.archives = ext;
        intModel();
        intListener();
        intDragAndDrop();
    }

    public MpqTree()
    {
        super();
        intModel();
        intListener();
        intDragAndDrop();
    }

    private void intDragAndDrop()
    {
        this.setTransferHandler(new MpqTreeTransferHandler(this));
        this.setDragEnabled(true);
        this.setDropMode(DropMode.ON_OR_INSERT);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
    }

    private void implMouseClicked(MouseEvent e)
    {
        TreePath path = getClosestPathForLocation(e.getX(), e.getY());
        if (e.getButton() == 1 && e.getClickCount() >= 2)
        {
            doubleClickOnNode(path);
        }
    }

    private void rightClickOnNode(TreePath path, MouseEvent e)
    {
        if (path == null)
        {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        String toLowerCase = node.getUserObject().toString().toLowerCase();
        if (toLowerCase.endsWith(".mpq"))
        {
            if (e.isPopupTrigger())
            {
                MpqTreePopupMenu menu = new MpqTreePopupMenu(this, node, true);
                menu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        } 
        else
        {
            if (e.isPopupTrigger())
            {
                MpqTreePopupMenu menu = new MpqTreePopupMenu(this, node, false);
                menu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    public void removeSelectedArchives()
    {
        TreePath[] selectionPaths = this.getSelectionPaths();
        for (int i = 0; i < selectionPaths.length; i++)
        {
            TreePath path = selectionPaths[i];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            String toLowerCase = node.getUserObject().toString().toLowerCase();
            if (toLowerCase.endsWith(".mpq"))
            {
                ExtMpqArchive mpqArchiveOfPath = getMpqArchiveOfPath(path);
                removeMpqArchive(mpqArchiveOfPath);
            }
        }
    }

    private void doubleClickOnNode(TreePath path)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject().toString().toLowerCase().endsWith(".blp"))
        {
            try
            {
                MpqFile file = getMqpFileOfPath(path);
                showImage(MpqUtil.convertMpqFileToImage(file), MpqUtil.getMpqFileName(file));
            } catch (ConversionException ex)
            {
                JOptionPane.showMessageDialog(this, "This file cant be opened ! (Reason: " + ex.getMessage() + ")");
            } catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "This file cant be opened ! (Reason: Some IO Problem)");
            } catch (RuntimeException ex)
            {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "This file cant be opened ! (Reason: unknown)");
            }
        }
    }

    public MpqFile getMqpFileOfPath(TreePath path) throws IOException
    {
        String[] convertTreePath = convertTreePath(path);
        // get ExtMpqArchive to mpqName
        ExtMpqArchive get = getMpqArchiveOfPath(convertTreePath);
        MpqFile file = get.getFile(convertTreePath[1], 0, 0);
        return file;
    }

    public ExtMpqArchive getMpqArchiveOfPath(TreePath path)
    {
        String[] convertTreePath = convertTreePath(path);
        return getMpqArchiveOfPath(convertTreePath);
    }

    public boolean isOnlyMpqArchive(TreePath path)
    {
        String[] convertTreePath = convertTreePath(path);
        if (convertTreePath[1] == null || convertTreePath[1].equals(""))
        {
            return true;
        }
        return false;
    }

    private ExtMpqArchive getMpqArchiveOfPath(String[] convertTreePath)
    {
        // get ExtMpqArchive to mpqName
        ExtMpqArchive get = this.archiveMap.get(convertTreePath[0]);
        return get;
    }


    private void showImage(BufferedImage img)
    {
        showImage(img, "");
    }

    private void showImage(BufferedImage img, String windowTitle)
    {
        JFrame frame = new JFrame();
        frame.setTitle(windowTitle);
        frame.setLayout(new GridLayout(1, 1));
        frame.add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private String convertTreePathImpl(String s, TreePath path)
    {
        if (path == null)
        {
            return s;
        }
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) path.getLastPathComponent();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        if (lastPathComponent == root)
        {
            return s;
        }
        s = s + lastPathComponent.getUserObject().toString() + "\\";
        s = convertTreePathImpl(s, path.getParentPath());
        if (s.endsWith("\\"))
        {
            int indexOf = s.lastIndexOf("\\");
            if (indexOf != -1)
            {
                s = s.substring(0, indexOf);
            }
        }
        return s;
    }

    /*
     * @return String[0] = mpqName || String[1] = fileName
     */
    private String[] convertTreePath(TreePath path)
    {
        String convertedTreePath = convertTreePathImpl(new String(), path);
        String[] split = convertedTreePath.split("\\\\");
        String reversePath = "";
        String mpqName = split[split.length - 1];
        // -2 because index starts with 0 and last index is the mpqName
        int startIndex = split.length - 2;
        //reverse path
        for (int i = startIndex; i >= 0; i--)
        {
            if (i == startIndex)
            {
                reversePath = reversePath + split[i];
            } else
            {
                reversePath = reversePath + "\\" + split[i];
            }
        }
        return new String[]
                {
                    mpqName, reversePath
                };
    }

    private void intListener()
    {
        this.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                implMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                TreePath path = getClosestPathForLocation(e.getX(), e.getY());
                rightClickOnNode(path, e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                TreePath path = getClosestPathForLocation(e.getX(), e.getY());
                rightClickOnNode(path, e);
            }
        });
        this.addKeyListener(new KeyAdapter()
        {

            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    removeSelectedArchives();
                }
            }
        });
    }

    private void intModel()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("MpqFiles");
        model = new DefaultTreeModel(root);
        this.setModel(model);
        this.setRootVisible(false);
        if (archives != null)
        {
            Arrays.sort(archives, new MpqArchiveComparator(true));
            for (ExtMpqArchive arch : this.archives)
            {
                addMpqArchive(arch);
            }
            this.archives = null;
        }
    }

    public void addMpqArchive(ExtMpqArchive arch, int index)
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        String archiveName = arch.getArchiveFile().getName();
        this.archiveMap.put(archiveName, arch);
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(archiveName);
        model.insertNodeInto(defaultMutableTreeNode, root, index);
        Thread t = new MpqTreeBuilder(defaultMutableTreeNode, arch);
        t.start();
        this.expandPath(new TreePath(root));
    }

    public void addMpqArchive(ExtMpqArchive arch)
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        int childCount = root.getChildCount();
        if (childCount == -1)
        {
            childCount = 0;
        }
        addMpqArchive(arch, childCount);
    }

    public void removeMpqArchive(ExtMpqArchive arch)
    {
        String name = arch.getArchiveFile().getName();
        removeMpqArchive(name);
    }

    public void removeMpqArchive(String archiveName)
    {
        ExtMpqArchive remove = this.archiveMap.remove(archiveName);
        try
        {
            remove.close();
        } catch (IOException ex)
        {
            Logger.getLogger(MpqTree.class.getName()).log(Level.SEVERE, null, ex);
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        for (int i = 0; i < root.getChildCount(); i++)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            if (node.getUserObject().toString().equals(archiveName))
            {
                this.model.removeNodeFromParent(node);
                break;
            }
        }
    }

    // builds the tree for the given archive and append it to the given node
    public void buildTree(DefaultMutableTreeNode node, ExtMpqArchive arch)
    {
        try
        {
            ArrayList<String> files = arch.getFiles();
            for (String s : files)
            {
                addDirectories(s, node);
            }
            finishedBuildTree = true;
        } catch (IOException ex)
        {
            Logger.getLogger(MpqTree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addDirectories(String completeFileName, DefaultMutableTreeNode node)
    {
        // I have to escape the "\" twice.
        String[] split = completeFileName.split("\\\\");
        addDirectoriesImpl(node, split, 0);
    }

    private void addDirectoriesImpl(DefaultMutableTreeNode parent, String[] split, int start)
    {
        if (start >= split.length)
        {
            return;
        }
        // check if there is already a child with that name
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (childAt.getUserObject().equals(split[start]))
            {
                addDirectoriesImpl(childAt, split, start + 1);
                return;
            }
        }
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(split[start]);
        parent.add(defaultMutableTreeNode);
        addDirectoriesImpl(defaultMutableTreeNode, split, start + 1);
        return;
    }

    // sorts the given node
    public void sort(DefaultMutableTreeNode parent)
    {
        sortLevel(parent);
    }

    // slow but effective way to sort, this method should only be called in a thread !
    private void sortLevel(DefaultMutableTreeNode node)
    {
        ArrayList<DefaultMutableTreeNode> tempList = new ArrayList<DefaultMutableTreeNode>();
        HashMap<String, DefaultMutableTreeNode> map = new HashMap<String, DefaultMutableTreeNode>();
        int childC = node.getChildCount();
        for (int i = 0; i < childC; i++)
        {
            DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) node.getChildAt(0);
            String key = childAt.getUserObject().toString();
            tempList.add(childAt);
            map.put(key, childAt);
            // sometimes there is a exception here, if yes try again x)
            while (true)
            {
                try
                {
                    // if the child should not have a parent break
                    if (childAt.getParent() == null)
                    {
                        break;
                    }
                    model.removeNodeFromParent(childAt);
                    break;
                } catch (Exception ex)
                {/* Ignore Exception */

                }
            }
        }
        Collections.sort(tempList, new MpqNodeComparator());
        int index = 0;
        for (DefaultMutableTreeNode s : tempList)
        {
            DefaultMutableTreeNode get = map.get(s.getUserObject().toString());
            while (true)
            {
                try
                {
                    if (get.getParent() == node)
                    {
                        break;
                    }
                    model.insertNodeInto(get, node, index);
                } catch (Exception ex)
                {
                }
            }
            sortLevel(get);
            index++;
        }
        tempList = null;
        map = null;
    }

    class MpqTreeBuilder extends Thread
    {

        private DefaultMutableTreeNode parent;
        private ExtMpqArchive archive;

        public MpqTreeBuilder(DefaultMutableTreeNode parent, ExtMpqArchive archive)
        {
            super("MpqTreeBuilderThread - " + archive.getArchiveFile().getName());
            this.parent = parent;
            this.archive = archive;
        }

        @Override
        public void run()
        {
            buildTree(parent, archive);
            while (finishedBuildTree == false)
            {
            }
            sort(parent);
            // expand the first node in tree (root isnt shown)
            if (parent.getChildCount() > 0)
            {
                //TreePath treePath = new TreePath(parent.getChildAt(0));
                expandRow(0);
            }
        }
    }
}
