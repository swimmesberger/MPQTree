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

import java.util.Comparator;

public class MpqArchiveComparator implements Comparator<ExtMpqArchive>
{
    private boolean descending;
    
    // strings how the names should be ordered: common 1st, 2nd expansion, 3rd lichking .... 
    private String[] searchStrings = {"common", "expansion", "lichking", "base", "locale", "speech", "patch"};
    private String[] secondSearchStrings = {"locale", "speech", "enus"};
    
    public MpqArchiveComparator(boolean descending)
    {
        this.descending = descending;
    }
    
    public MpqArchiveComparator()
    {
        this(false);
    }
    
    
    @Override
    public int compare(ExtMpqArchive o1, ExtMpqArchive o2)
    {
        int mult = 1;
        if(descending)
        {
            mult = -1;
        }
        String name1 = deleteExtension(o1.getArchiveFile().getName().toLowerCase());
        String name2 = deleteExtension(o2.getArchiveFile().getName().toLowerCase());
        return compare(name1, name2, searchStrings, mult);
    }
    
    private int compare(String name1, String name2, String[] searchString, int mult)
    {
        for(String s : searchString)
        {
            if(name1.contains(s) && !name2.contains(s))
            {
                return 1*mult;
            }
            if(name1.contains(s) && name2.contains(s))
            {
                String stringToNumber1 = getStringToNumber(name1);
                String stringToNumber2 = getStringToNumber(name2);
                if(stringToNumber1.equals(stringToNumber2))
                {
                    return getNumberWorth(name1,name2, mult);
                }
                else
                {
                    for(String second : secondSearchStrings)
                    {
                        if(name2.contains(second) && !name1.contains(second))
                        {
                            return 1*mult;
                        }
                        else if(name2.contains(second) && name1.contains(second))
                        {
                            return getNumberWorth(name1,name2, mult);
                        }
                        else if(!name2.contains(second) && name1.contains(second))
                        {
                            return -1*mult;
                        }
                    }
                }
            }
            if(name2.contains(s) && !name1.contains(s))
            {
                return -1*mult;
            }
        }
        return -1*mult;
    }
    
    private int getNumberWorth(String name1, String name2, int mult)
    {
        int numb1 = getEndNumber(name1);
        int numb2 = getEndNumber(name2);
        if(numb2 > numb1)
        {
            return 1*mult;
        }
        else if(numb1 == numb2)
        {
            return 0*mult;
        }
        else
        {
            return -1*mult;
        }
    }
    
    private String deleteExtension(String s)
    {
        int lastIndexOf = s.lastIndexOf(".");
        if(lastIndexOf != -1)
        {
            return s.substring(0, lastIndexOf);
        }
        return s;
    }
    
    private int getEndNumber(String s)
    {
        int lastIndexOf = s.lastIndexOf("-");
        int number = 1;
        if(lastIndexOf != -1)
        {
            String numb = s.substring(lastIndexOf+1, s.length());
            try
            {
                number = Integer.parseInt(numb);
            }catch(NumberFormatException ex){}
        }
        return number;
    }
    
    private String getStringToNumber(String s)
    {
        int lastIndexOf = s.lastIndexOf("-");
        if(lastIndexOf != -1)
        {
            String numb = s.substring(lastIndexOf+1, s.length());
            try
            {
                // this parse is only for that to check if String numb is really an Integer
                int number = Integer.parseInt(numb);
                s = s.substring(0, lastIndexOf);
            }catch(NumberFormatException ex){}
        }
        return s;
    }


    
}
