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
 *  Created on 14.12.2005
 *  --- CVS Information ---
 *  $Id: FilenameConditionHandler.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.xml.sax.parser.rules;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import phex.xml.sax.rules.DFilenameCondition;

import javax.xml.parsers.SAXParser;
import java.io.CharArrayWriter;

public class FilenameConditionHandler extends DefaultHandler
{
    public static final String ELEMENT_NAME = DFilenameCondition.ELEMENT_NAME;

    private final CharArrayWriter text = new CharArrayWriter();

    private final SAXParser parser;

    private final DFilenameCondition condition;

    private final DefaultHandler parent;

    public FilenameConditionHandler( DFilenameCondition condition, Attributes attributes,
        DefaultHandler parent, SAXParser parser )
    {
        this.condition = condition;
        this.parser = parser;
        this.parent = parent;
    }

    /**
     * Receive notification of the start of an element.
     *
     * @param name The element type name.
     * @param attributes The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement( String uri, String localName, String qName,
        Attributes attributes ) throws SAXException
    {
        text.reset();
        return;
    }

    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if ( qName.equals( "term" ) )
        {
            condition.getTerms().add( text.toString() );
        }
        else if ( qName.equals( ELEMENT_NAME ) )
        {
            parser.getXMLReader().setContentHandler( parent );
        }
    }

    public InputSource resolveEntity( String publicId, String systemId )
    {
        return null;
    }

    public void characters( char[] ch, int start, int length )
    {
        text.write( ch, start, length );
    }
}
