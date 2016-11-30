/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
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
 *  Created on 02.02.2006
 *  --- CVS Information ---
 *  $Id: ImageFilterUtils.java 4351 2009-01-14 16:44:31Z gregork $
 */
package phex.gui.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.util.Arrays;

public class ImageFilterUtils
{
    private static Logger logger = LoggerFactory.getLogger( ImageFilterUtils.class );
    
    public static Icon createGrayIcon(Icon icon)
    {
        try
        {
            return new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) icon)
                .getImage()));
        }
        catch ( ClassCastException exp )
        {// apple.laf.AquaSystemIcon throws CCE, of what type is it?
            Class[] interfaces = icon.getClass().getInterfaces();
            Class<?> superclass = icon.getClass().getSuperclass();
            logger.error( exp.toString(), exp );
            logger.error( "CCE from {}, Interfaces: {}, Super: {}", 
                new Object[] {icon.getClass(), Arrays.toString(interfaces),
                superclass} );
            return icon;
        }
    }

    public static Icon createGrayIcon(Icon icon, boolean brighter, int percent)
    {
        GrayFilter filter = new GrayFilter( brighter, percent );
        ImageProducer prod = new FilteredImageSource(((ImageIcon) icon)
            .getImage().getSource(), filter);
        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
        return new ImageIcon(grayImage);
    }

}
