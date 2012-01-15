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
import java.awt.dnd.DropTarget;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import mpq.ExtMpqArchive;
import mpq.MpqUtil;
import mwt.wow.mpq.MpqFile;

public class MpqTreeTransferHandler extends TransferHandler
{

    private MpqTree mpqTree;
    private DataFlavor nodesFlavor;
    private DataFlavor[] flavors = new DataFlavor[1];
    private DefaultMutableTreeNode[] nodesToRemove;

    public MpqTreeTransferHandler(MpqTree mpqTree)
    {
        this.mpqTree = mpqTree;
        try
        {
            String mimeType = DataFlavor.javaFileListFlavor.getMimeType();
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch (ClassNotFoundException e)
        {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }

    @Override
    public boolean canImport(TransferSupport support)
    {
        DataFlavor[] dataFlavors = support.getDataFlavors();
        DataFlavor fl = dataFlavors[0];
        String mimeType = fl.getMimeType();
        if (fl == DataFlavor.javaFileListFlavor || mimeType.contains("application/x-java-file-list"))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        System.out.println("importData");
        Transferable t = support.getTransferable();
        JTree.DropLocation loc = (JTree.DropLocation) support.getDropLocation();
        int childIndex = loc.getChildIndex();
        ArrayList<ExtMpqArchive> archives = new ArrayList<ExtMpqArchive>();
        try
        {
            ArrayList<File> files = getFiles(t);

            for (File f : files)
            {
                if (f.isDirectory())
                {
                    // if error try next
                    try
                    {
                        ExtMpqArchive[] mPQFiles = MpqUtil.getMPQFiles(f, true);
                        archives.addAll(Arrays.asList(mPQFiles));
                    } catch (Exception ex)
                    {
                    };
                }
                // if error try next
                try
                {
                    archives.add(new ExtMpqArchive(f));
                } catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(mpqTree, ex.getMessage());
                } catch (Exception ex)
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
        for (ExtMpqArchive arch : archives)
        {
            if (childIndex == -1)
            {
                childIndex = 0;
            }
            this.mpqTree.addMpqArchive(arch, childIndex);
            childIndex++;
        }
        return true;
    }

    private DefaultMutableTreeNode copy(TreeNode node)
    {
        return (DefaultMutableTreeNode) node;
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null)
        {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<DefaultMutableTreeNode> myNodes = new ArrayList<DefaultMutableTreeNode>();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
            myNodes.add(node);
            for (int i = 1; i < paths.length; i++)
            {
                DefaultMutableTreeNode next = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if (next.getLevel() < node.getLevel())
                {
                    break;
                } else if (next.getLevel() > node.getLevel())
                {  // child node
                    node.add(copy(next));
                    // node already contains child
                } else
                {                                        // sibling
                    myNodes.add(copy(next));
                }
            }
            DefaultMutableTreeNode[] nodes = myNodes.toArray(new DefaultMutableTreeNode[myNodes.size()]);
            return new NodesTransferable(nodes);
        }
        return null;
    }
    
    /*
     * TODO: GUI for transfer
     */
    @Override
    public int getSourceActions(JComponent c)
    {
        return COPY_OR_MOVE;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action)
    {
        if (exportedFiles.isEmpty() == false)
        {
            // Remove nodes saved in nodesToRemove in createTransferable.
            for (int i = 0; i < exportedFiles.size(); i++)
            {
                System.out.println(deleteFile(exportedFiles.get(i)));
            }
        }
    }

    public static boolean deleteFile(File path)
    {
        if (path.exists())
        {
            if (path.isDirectory())
            {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++)
                {
                    if (files[i].isDirectory())
                    {
                        deleteFile(files[i]);
                    } else
                    {
                        files[i].delete();
                    }
                }
            }
        }
        return (path.delete());
    }

    public static ArrayList<File> getFiles(Transferable t) throws UnsupportedFlavorException, IOException, java.lang.ClassCastException
    {
        Object transferData = t.getTransferData(DataFlavor.javaFileListFlavor);
        if (transferData instanceof ArrayList)
        {
            ArrayList<File> list = (ArrayList<File>) transferData;
            return list;
        } // need to do that for windows explorer support, they send me a Arrays$ArrayLidt didnt find an other solution
        else
        {
            ArrayList<File> list = new ArrayList<File>();
            list.addAll((Collection) transferData);
            return list;
        }
    }
    private ArrayList<File> exportedFiles;

    public class NodesTransferable implements Transferable
    {

        DefaultMutableTreeNode[] nodes;

        public NodesTransferable(DefaultMutableTreeNode[] nodes)
        {
            this.nodes = nodes;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
            if (!isDataFlavorSupported(flavor))
            {
                throw new UnsupportedFlavorException(flavor);
            }
            exportedFiles = new ArrayList<File>();
                DefaultMutableTreeNode node = nodes[0];
                try
                {
                    exportNode(node, false);
                } catch (IOException ex)
                {
                    Logger.getLogger(MpqTreeTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
//            for (int i = 0; i < nodes.length; i++)
//            {
//
//            }
            return exportedFiles;
        }

        private void exportNode(DefaultMutableTreeNode node, boolean isDir) throws IOException
        {
            if (node.getChildCount() == 0)
            {
                TreePath treePath = getPath(node);
                Object[] path = treePath.getPath();
                String dir = path[1].toString().replaceAll("\\W","");
                for (int i = 2; i < path.length; i++)
                {
                    if ((i + 1) == path.length)
                    {
                        break;
                    }
                    dir = dir + File.separator + path[i].toString().replaceAll("\\W","");
                }
                File dirF = new File(dir);
                dirF.mkdirs();
                MpqFile mpqFileOfPath = mpqTree.getMqpFileOfPath(treePath);
                File f = new File(dirF.getAbsolutePath() + File.separator + MpqUtil.getMpqFileName(mpqFileOfPath));
                mpqFileOfPath.extractTo(f);
                if(!isDir)
                {
                    System.out.println(f.getAbsolutePath());
                    exportedFiles.add(f);
                }
            }
            else
            {
                exportDir(node, isDir);
            }
        }
        
        private void exportDir(DefaultMutableTreeNode node, boolean isDir) throws IOException
        {
            TreePath treePath = getPath(node);
            Object[] path = treePath.getPath();
            String dir = path[1].toString().replaceAll("\\W","");
            for (int i = 2; i < path.length; i++)
            {
                if ((i + 1) == path.length)
                {
                    break;
                }
                dir = dir + File.separator + path[i].toString().replaceAll("\\W","");
            }
            File dirF = new File(dir);
            dirF.mkdirs();
            for (int i = 0; i < node.getChildCount(); i++)
            {
                exportNode((DefaultMutableTreeNode) node.getChildAt(i), true);
            }
            if(!isDir)
            {
                System.out.println(dirF.getAbsolutePath());
                exportedFiles.add(dirF);
            }
        }

        public TreePath getPath(DefaultMutableTreeNode treeNode)
        {
            List<Object> nodes = new ArrayList<Object>();
            if (treeNode != null)
            {
                nodes.add(treeNode);
                treeNode = (DefaultMutableTreeNode) treeNode.getParent();
                while (treeNode != null)
                {
                    nodes.add(0, treeNode);
                    treeNode = (DefaultMutableTreeNode) treeNode.getParent();
                }
            }

            return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
        }

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return nodesFlavor.equals(flavor);
        }
    }
}
