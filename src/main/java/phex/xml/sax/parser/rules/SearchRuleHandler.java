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
 *  Created on 18.11.2005
 *  --- CVS Information ---
 *  $Id: SearchRuleHandler.java 4382 2009-03-12 17:19:53Z m_gar $
 */
package phex.xml.sax.parser.rules;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import phex.xml.sax.rules.DAndConcatCondition;
import phex.xml.sax.rules.DConsequencesList;
import phex.xml.sax.rules.DSearchRule;

import javax.xml.parsers.SAXParser;
import java.io.CharArrayWriter;

/**
 *
 */
public class SearchRuleHandler extends DefaultHandler {
    public static final String ELEMENT_NAME = DSearchRule.ELEMENT_NAME;

    private final CharArrayWriter text = new CharArrayWriter();

    private final SAXParser parser;

    private final DSearchRule searchRule;

    private final DefaultHandler parent;

    public SearchRuleHandler(DSearchRule searchRule, Attributes attributes,
                             DefaultHandler parent, SAXParser parser) {
        this.searchRule = searchRule;
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
        if (qName.equals(DAndConcatCondition.ELEMENT_NAME)) {
            DAndConcatCondition condition = new DAndConcatCondition();
            searchRule.setAndConcatCondition(condition);

            AndConcatConditionHandler handler = new AndConcatConditionHandler(
                    condition, attributes, this, parser);
            parser.getXMLReader().setContentHandler(handler);
        } else if (qName.equals(DConsequencesList.ELEMENT_NAME)) {
            DConsequencesList consequencesList = new DConsequencesList();
            searchRule.setConsequencesList(consequencesList);
            ConsequencesListHandler handler = new ConsequencesListHandler(
                    consequencesList, this, parser);
            parser.getXMLReader().setContentHandler(handler);
        }
        return;
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName) {
            case "name":
                searchRule.setName(text.toString());
                break;
            case "notes":
                searchRule.setNotes(text.toString());
                break;
            case "description":
                searchRule.setDescription(text.toString());
                break;
            case "id":
                searchRule.setId(text.toString());
                break;
            case "permanently-enabled":
                searchRule.setPermanentlyEnabled(Boolean.valueOf(text.toString())
                        .booleanValue());
                break;
            case ELEMENT_NAME:
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
