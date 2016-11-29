/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  --- SVN Information ---
 *  $Id:$
 */
package phex.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util
{
    public static boolean copyFile(String sourceFilePath, String destinationFilePath)
    {
        if (sourceFilePath == null || destinationFilePath == null)
        {
            return false;
        }

        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        try
        {
            File inputFile = new File(sourceFilePath);
            if (!inputFile.isFile())
            {
                return false;
            }

            File outputFile = new File(destinationFilePath);
            if (outputFile.exists())
            {
                return false;
            }

            inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            int c = inputStream.read();
            while (c != -1)
            {
                outputStream.write(c);

                c = inputStream.read();
            }

            return true;
        }
        catch (SecurityException ex)
        {
            return false;
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
        catch (IOException ex)
        {
            return false;
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException ex)
                {
                }
            }

            if (outputStream != null)
            {
                try
                {
                    outputStream.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
    }

    public static boolean moveFile(String sourceFilePath, String destinationFilePath)
    {
        try
        {
            if (sourceFilePath == null)
            {
                return false;
            }

            final boolean copyOK = copyFile(sourceFilePath, destinationFilePath);
            if (!copyOK)
            {
                return false;
            }

            File file = new File(sourceFilePath);
            return file.delete();
        }
        catch (SecurityException ex)
        {
            return false;
        }
    }
}