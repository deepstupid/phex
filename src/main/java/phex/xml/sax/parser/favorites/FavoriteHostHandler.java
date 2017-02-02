/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  Created on 18.11.2005
 *  --- CVS Information ---
 *  $Id: FavoriteHostHandler.java 3891 2007-08-30 16:43:43Z gregork $
 */
package phex.xml.sax.parser.favorites;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import phex.common.log.NLogger;
import phex.xml.sax.favorites.DFavoriteHost;
import phex.xml.sax.parser.ParserUtils;

import javax.xml.parsers.SAXParser;
import java.io.CharArrayWriter;

/**
 *
 */
public class FavoriteHostHandler extends DefaultHandler {
    public static final String THIS_TAG_NAME = "favorite-host";

    private final CharArrayWriter text = new CharArrayWriter();

    private final SAXParser parser;

    private final DFavoriteHost dHost;

    private final DefaultHandler parent;

    public FavoriteHostHandler(DFavoriteHost dHost, Attributes attributes,
                               DefaultHandler parent, SAXParser parser) {
        this.dHost = dHost;
        this.parser = parser;
        this.parent = parent;
    }

    /**
     * Receive notification of the start of an element.
     *
     * @param name       The element type name.
     * @param attributes The specified or defaulted attributes.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        text.reset();
        return;
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName) {
            case "host-name":
                dHost.setHostName(text.toString());
                break;
            case "ip":
                dHost.setIp(ParserUtils.fromHexBinary(text.toString()));
                break;
            case "port":
                try {
                    dHost.setPort(Integer.parseInt(text.toString()));
                } catch (NumberFormatException exp) {
                    NLogger.error(FavoriteHostHandler.class, exp, exp);
                }
                break;
            case THIS_TAG_NAME:
                parser.getXMLReader().setContentHandler(parent);
                break;
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return null;
    }

    public void characters(char[] ch, int start, int length) {
        text.write(ch, start, length);
    }
}
