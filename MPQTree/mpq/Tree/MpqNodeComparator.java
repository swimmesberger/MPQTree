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

import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;

public class MpqNodeComparator implements Comparator<DefaultMutableTreeNode>
{

    @Override
    public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2)
    {
        int erg = 0;
        // then its a file
        if(o1.getChildCount() <= 0)
        {
            if(o2.getChildCount() <= 0)
            {
                erg = o1.getUserObject().toString().compareToIgnoreCase(o2.getUserObject().toString());
            }
            else
            {
                erg = 1;
            }
        }
        // dir
        else
        {
            if(o2.getChildCount() <= 0)
            {
                erg = -1;
            }
            else
            {
                erg = o1.getUserObject().toString().compareToIgnoreCase(o2.getUserObject().toString());
            }
        }
        // debug output
        //System.out.println("String1: " + o1 + "\n" + "String2: " + o2 + "\n" + "Ergebniss: " + erg);
        return erg;
    }
    
}
