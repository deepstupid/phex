// Revision: 161350
// Thu Apr 14 20:39:46 2005 UTC 

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package phex.util;

/**
 * Thrown when there is a failure condition during the encoding process.  This
 * exception is thrown when an Encoder encounters a encoding specific exception
 * such as invalid data, inability to calculate a checksum, characters outside of the
 * expected range.
 *
 * @author Apache Software Foundation
 * @version $Id: EncoderException.java 3380 2006-04-14 11:49:41Z gregork $
 */
public class EncoderException extends Exception {

    /**
     * Declares the Serial Version Uid.
     *
     * @see <a href="http://c2.com/cgi/wiki?AlwaysDeclareSerialVersionUid">Always Declare Serial Version Uid</a>
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of this exception with an useful message.
     *
     * @param pMessage a useful message relating to the encoder specific error.
     */
    public EncoderException(String pMessage) {
        super(pMessage);
    }
}  