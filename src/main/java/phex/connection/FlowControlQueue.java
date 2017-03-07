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
 */
package phex.connection;

import phex.msg.Message;
import phex.util.CircularQueue;


/**
 * This queue handles the flow control algorithem described in the
 * 'SACHRIFC: Simple Flow Control for Gnutella' proposal by Limewire.
 */
public class FlowControlQueue extends CircularQueue {
    /**
     * The number of messages send by each message burst.
     */
    private final int burstSize;
    /**
     * The lifetime of a message.
     */
    private final int msgTimeout;
    /**
     * Indicates whether the queue is a lifo of fifo queue. This is necessary to
     * handle the different message types.
     */
    private final boolean isLIFO;
    /**
     * The number of messages dropped in this queue.
     */
    private int dropCount;
    /**
     * The number of messages send in the curretn message burst.
     */
    private int currentBurstCount;


    public FlowControlQueue(int burstSize, int msgTimeout, int maxSize,
                            boolean isLIFO) {
        super(maxSize);
        dropCount = 0;
        //msgQueue = new CircularQueue( maxSize );
        this.burstSize = burstSize;
        this.msgTimeout = msgTimeout;
        this.isLIFO = isLIFO;
    }

    public void addMessage(Message message) {
        Object dropObj;
        synchronized (this) {
            dropObj = addToTail(message);
        }
        if (dropObj != null) {// count dropped msg for stats
            //Logger.logMessage( Logger.FINEST, Logger.NETWORK,
            //    "Dropping overflowing message: " + message );
            dropCount++;
        }
    }

    public Message removeMessage() {
        if (currentBurstCount == burstSize) {// all elements of this burst returned.
            return null;
        }

        long expiredCreationTime = System.currentTimeMillis() - msgTimeout;
        while (true) {
            Message message = removeNextMessage();
            if (message == null) {
                // no more messages in queue
                return null;
            }

            if (message.getCreationTime() < expiredCreationTime) {
                // drop message
                //Logger.logMessage( Logger.FINEST, Logger.NETWORK,
                //    "Dropping expired message: " + message );
                dropCount++;
                continue;
            }

            // found a message, return it and count it to the current burst.
            currentBurstCount++;
            return message;
        }
    }

    public void initNewMessageBurst() {
        currentBurstCount = 0;
    }

    public int getAndResetDropCount() {
        int tmpDropCount = dropCount;
        dropCount = 0;
        return tmpDropCount;
    }

    private Message removeNextMessage() {
        if (isEmpty()) {
            return null;
        }

        synchronized (this) {
            if (isLIFO) {
                return (Message) removeFromTail();
            } else {
                return (Message) removeFromHead();
            }
        }
    }
}