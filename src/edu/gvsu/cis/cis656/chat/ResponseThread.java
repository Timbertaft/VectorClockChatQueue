package edu.gvsu.cis.cis656.chat;

import edu.gvsu.cis.cis656.clock.VectorClock;
import edu.gvsu.cis.cis656.message.Message;
import edu.gvsu.cis.cis656.message.MessageTypes;
import edu.gvsu.cis.cis656.queue.PriorityQueue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ResponseThread implements Runnable {

    private DatagramSocket socket;
    private PriorityQueue<Message> queue;
    private VectorClock CallingClock;

    ResponseThread(DatagramSocket socket, PriorityQueue<Message> queue, VectorClock CallingClock) {
        this.socket = socket;
        this.queue = queue;
        this.CallingClock = CallingClock;
    }

    private boolean complete = false;

    public void run() {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (!complete) {

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (complete)
                socket.close();

            if (!Thread.currentThread().isInterrupted()) {
                String incomingmessage = new String(packet.getData(), 0, packet.getLength());
                Message message = Message.parseMessage(incomingmessage);

                if (message.type == MessageTypes.ERROR) {
                    System.out.println(message.message);
                } else {
                    queue.add(message);
                    Message queuetop = queue.peek();
                    while (queuetop != null) {

                        if (CallingClock.getTime(message.pid) + 1 == message.ts.getTime(message.pid) &&
                                CallingClock.happenedBefore(message.ts)) {

                            System.out.println("\n" + queuetop.sender + queuetop.message);
                            System.out.print("Type a message or type exit to terminate the application:  ");

                            CallingClock.update(queuetop.ts);

                            queue.remove(queuetop);
                            queuetop = queue.peek();
                        } else {
                            queuetop = null;
                        }
                    }
                }
            }
        }

        socket.close();
    }

    void stop() {
        complete = true;
    }
}