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
 *  $Id: DownloadFileHandler.java 4231 2008-07-15 16:01:10Z gregork $
 */
package phex.xml.sax.parser.downloads;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import phex.common.log.NLogger;
import phex.xml.sax.downloads.DDownloadCandidate;
import phex.xml.sax.downloads.DDownloadFile;
import phex.xml.sax.downloads.DDownloadScope;

import javax.xml.parsers.SAXParser;
import java.io.CharArrayWriter;

/**
 *
 */
public class DownloadFileHandler extends DefaultHandler {
    private final CharArrayWriter text = new CharArrayWriter();

    private final SAXParser parser;

    private final DDownloadFile downloadFile;

    private final DefaultHandler parent;

    public DownloadFileHandler(DDownloadFile downloadFile, DefaultHandler parent, SAXParser parser) {
        this.downloadFile = downloadFile;
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
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        text.reset();
        switch (qName) {
            case DDownloadCandidate.ELEMENT_NAME:
                DDownloadCandidate candidate = new DDownloadCandidate();
                downloadFile.getCandidateList().getSubElementList().add(candidate);
                DownloadCandidateHandler handler = new DownloadCandidateHandler(
                        candidate, this, parser);
                parser.getXMLReader().setContentHandler(handler);
                break;
            case DDownloadScope.UNVERIFIED_SCOPE_ELEMENT_NAME: {
                DDownloadScope scope = new DDownloadScope(DDownloadScope.UNVERIFIED_SCOPE_ELEMENT_NAME);
                String start = attributes.getValue("start");
                if (start != null) {
                    try {
                        scope.setStart(Long.parseLong(start));
                    } catch (NumberFormatException exp) {
                        NLogger.error(DownloadFileHandler.class, exp, exp);
                    }
                }
                String end = attributes.getValue("end");
                if (end != null) {
                    try {
                        scope.setEnd(Long.parseLong(end));
                    } catch (NumberFormatException exp) {
                        NLogger.error(DownloadFileHandler.class, exp, exp);
                    }
                }
                downloadFile.getUnverifiedScopesList().getSubElementList().add(scope);
                break;
            }
            case DDownloadScope.FINISHED_SCOPE_ELEMENT_NAME: {
                DDownloadScope scope = new DDownloadScope(DDownloadScope.FINISHED_SCOPE_ELEMENT_NAME);
                String start = attributes.getValue("start");
                if (start != null) {
                    try {
                        scope.setStart(Long.parseLong(start));
                    } catch (NumberFormatException exp) {
                        NLogger.error(DownloadFileHandler.class, exp, exp);
                    }
                }
                String end = attributes.getValue("end");
                if (end != null) {
                    try {
                        scope.setEnd(Long.parseLong(end));
                    } catch (NumberFormatException exp) {
                        NLogger.error(DownloadFileHandler.class, exp, exp);
                    }
                }
                downloadFile.getFinishedScopesList().getSubElementList().add(scope);
                break;
            }
        }
        return;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName) {
            case "created-time":
                try {
                    downloadFile.setCreationTime(Long.parseLong(text.toString()));
                } catch (NumberFormatException exp) {
                    NLogger.error(DownloadFileHandler.class, exp, exp);
                }
                break;
            case "filesize":
                try {
                    downloadFile.setFileSize(Long.parseLong(text.toString()));
                } catch (NumberFormatException exp) {
                    NLogger.error(DownloadFileHandler.class, exp, exp);
                }
                break;
            case "file-urn":
                downloadFile.setFileURN(text.toString());
                break;
            case "incomplete-file-name":
                downloadFile.setIncompleteFileName(text.toString());
                break;
            case "localfilename":
                downloadFile.setLocalFileName(text.toString());
                break;
            case "modified-time":
                try {
                    downloadFile.setModificationTime(Long.parseLong(text.toString()));
                } catch (NumberFormatException exp) {
                    NLogger.error(DownloadFileHandler.class, exp, exp);
                }
                break;
            case "scope-strategy":
                downloadFile.setScopeSelectionStrategy(text.toString());
                break;
            case "searchterm":
                downloadFile.setSearchTerm(text.toString());
                break;
            case "status":
                try {
                    downloadFile.setStatus(Integer.parseInt(text.toString()));
                } catch (NumberFormatException exp) {
                    NLogger.error(DownloadFileHandler.class, exp, exp);
                }
                break;
            case DDownloadFile.ELEMENT_NAME:
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
