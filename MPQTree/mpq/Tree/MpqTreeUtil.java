/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpq.Tree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import mpq.MpqUtil;
import mwt.wow.mpq.MpqFile;

/**
 *
 * @author Thedeath
 * TODO: Make this tree level independent
 */
public class MpqTreeUtil
{   
    public static File exportNode(DefaultMutableTreeNode node, File dir, MpqTree mpqTree) throws IOException
    {
        return exportNode(node, dir, true, mpqTree);
    }
    
    public static File exportNode(DefaultMutableTreeNode node, File dir, boolean direct, MpqTree mpqTree) throws IOException
    {
        if (node.getChildCount() == 0)
        {
            TreePath treePath = getPath(node);
            MpqFile mpqFileOfPath = mpqTree.getMqpFileOfPath(treePath);
            File f = null;
            if(direct)
            {
                Object[] path = treePath.getPath();
                if(path.length > 3)
                {
                    dir = new File(dir + File.separator + getDirForPathString(treePath));
                    dir.mkdirs();
                }
            }
            if(dir != null)
            {
                System.out.println(dir);
                f = new File(dir + File.separator + MpqUtil.getMpqFileName(mpqFileOfPath));
            }
            else
            {
                f = new File(MpqUtil.getMpqFileName(mpqFileOfPath));
            }
            System.out.println(f.getAbsolutePath());
            mpqFileOfPath.extractTo(f);
            return f;
        }
        else
        {
            return exportDir(node, dir, direct, mpqTree);
        }
    }
    

    public static File exportDir(DefaultMutableTreeNode node, File dir, boolean direct, MpqTree mpqTree) throws IOException
    {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        if(parent.isRoot() && direct == false)
        {
            System.out.println("Drag and Drop COMPLETE MPQ-File not supported !");
            return null;
        }
        TreePath treePath = getPath(node);
        if(dir == null || direct == false)
        {
            dir = getDirForPath(treePath);
            dir.mkdirs();
        }
        for (int i = 0; i < node.getChildCount(); i++)
        {
            exportNode((DefaultMutableTreeNode) node.getChildAt(i), dir, direct, mpqTree);
        }
        return dir;
    }
    
    public static File exportDir(DefaultMutableTreeNode node, File dir, MpqTree mpqTree) throws IOException
    {
        return exportDir(node, dir, true, mpqTree);
    }

    public static File getDirForPath(TreePath treePath)
    {
        File dirF = new File(getDirForPathString(treePath));
        return dirF;
    }
    
    public static String getDirForPathString(TreePath treePath)
    {
        Object[] path = treePath.getPath();
        int startIndex = 1;
        if(path.length >= 3)
        {
            startIndex = 2;
        }
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode)treePath.getLastPathComponent();
        boolean flag = false;
        if(lastPathComponent.isLeaf())
        {
            flag = true;
        }
        String dir = path[startIndex].toString().replaceAll("\\W","");
        for (int i = startIndex+1; i < path.length; i++)
        {
            if(flag == true && (i+1) == path.length)
            {
                break;
            }
            dir = dir + File.separator + path[i].toString().replaceAll("\\W","");
        }
        return dir;
    }

    public static TreePath getPath(DefaultMutableTreeNode treeNode)
    {
        List<Object> nodesTemp = new ArrayList<Object>();
        if (treeNode != null)
        {
            nodesTemp.add(treeNode);
            treeNode = (DefaultMutableTreeNode) treeNode.getParent();
            while (treeNode != null)
            {
                nodesTemp.add(0, treeNode);
                treeNode = (DefaultMutableTreeNode) treeNode.getParent();
            }
        }
        return nodesTemp.isEmpty() ? null : new TreePath(nodesTemp.toArray());
    }
}
