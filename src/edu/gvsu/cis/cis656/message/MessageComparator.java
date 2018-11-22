package edu.gvsu.cis.cis656.message;

import java.util.Comparator;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message firstmessage, Message secondmessage) {
        if(firstmessage.ts.happenedBefore(secondmessage.ts)) {
            return -1;
        }
        else if(secondmessage.ts.happenedBefore(firstmessage.ts)) {
            return 1;
        }
        return 0;
    }

}
