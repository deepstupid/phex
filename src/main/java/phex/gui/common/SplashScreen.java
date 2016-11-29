/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2009 Phex Development Group
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
 *  $Id: SplashScreen.java 4376 2009-02-08 18:37:37Z gregork $
 */
package phex.gui.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.lang.SystemUtils;

public class SplashScreen extends Frame
{
    public static final String SPLASH_IMAGE_NAME = "/phex/resources/splash.png";

    public SplashScreen()
    {
        super( );
    }

    public void showSplash()
    {
        Object j16Splash = null;
        if ( SystemUtils.isJavaVersionAtLeast( 1.6f ) )
        {
            try
            {
                Class<?> splashClass = Class.forName( "java.awt.SplashScreen" );
                Method splashMethod = splashClass.getMethod( "getSplashScreen", null );
                j16Splash = splashMethod.invoke( null, null );
            }
            catch ( ClassNotFoundException exp )
            {
                exp.printStackTrace();
            }
            catch ( SecurityException exp )
            {
                exp.printStackTrace();
            }
            catch ( NoSuchMethodException exp )
            {
                exp.printStackTrace();
            }
            catch ( IllegalArgumentException exp )
            {
                exp.printStackTrace();
            }
            catch ( IllegalAccessException exp )
            {
                exp.printStackTrace();
            }
            catch ( InvocationTargetException exp )
            {
                exp.printStackTrace();
            }
        }

        if ( j16Splash != null )
        {
            return;
        }
        
        MediaTracker mediaTracker = new MediaTracker(this);
        URL imageURL = SplashScreen.class.getResource( SPLASH_IMAGE_NAME );
        Image image = Toolkit.getDefaultToolkit().getImage( imageURL );
        
        mediaTracker.addImage( image, 0);
        try 
        {
            mediaTracker.waitForID( 0 );
        }
        catch(InterruptedException ex) {
        }
        new SplashWindow(this, image);
    }
    
    public void closeSplash()
    {
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                dispose();
            }} );
    }
    
    private class SplashWindow extends Window implements MouseListener
    {
        private Image image;
        
        SplashWindow(Frame parent, Image image)
        {
            super( parent );
            this.image = image;
            setSize( image.getWidth(null)+4, image.getHeight(null)+4);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle window = getBounds();
            setLocation((screen.width - window.width) / 2,(screen.height - window.height)/2);
            addMouseListener( this );
            setVisible(true);
            repaint();
            toFront();
         }
        
        public void paint( Graphics g )
        {
            g.drawImage( image, 2, 2, null );
            g.setColor( Color.white );
            g.drawLine( 0, 0, 0, image.getHeight( null ) + 3 );
            g.drawLine( 0, 0, image.getWidth( null ) + 3, 0 );
            g.setColor( Color.lightGray );
            g.drawLine( 1, 1, 1, image.getHeight( null ) + 2 );
            g.drawLine( 1, 1, image.getWidth( null ) + 2, 1 );
    
    
            g.setColor( Color.black );
            g.drawLine( 0, image.getHeight( null ) + 3,
                image.getWidth( null ) + 3, image.getHeight( null ) + 3 );
            g.drawLine( image.getWidth( null ) + 3, 0,
                image.getWidth( null ) + 3, image.getHeight( null ) + 3 );
    
            g.setColor( Color.darkGray );
            g.drawLine( 1, image.getHeight( null ) + 2,
                image.getWidth( null ) + 2, image.getHeight( null ) + 2 );
            g.drawLine( image.getWidth( null ) + 2, 1,
                image.getWidth( null ) + 2, image.getHeight( null ) + 2 );
    
    /*
            g.setColor( Color.lightGray );
            g.drawRect( 0, 0, image.getWidth( null ) + 3, image.getHeight( null ) + 3 );
            g.setColor( Color.darkGray );
            g.drawRect( 1, 1, image.getWidth( null ) + 1, image.getHeight( null ) + 1 );
                */
        }
        
        public void mouseClicked( MouseEvent e )
        {
            setVisible( false );
        }
        public void mouseEntered( MouseEvent e )
        {}
        public void mouseExited( MouseEvent e )
        {}
        public void mousePressed( MouseEvent e )
        {}
        public void mouseReleased( MouseEvent e )
        {}
    }
}
