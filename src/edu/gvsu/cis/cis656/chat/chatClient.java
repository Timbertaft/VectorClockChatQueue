package edu.gvsu.cis.cis656.chat;

import edu.gvsu.cis.cis656.clock.VectorClock;
import edu.gvsu.cis.cis656.message.Message;
import edu.gvsu.cis.cis656.message.MessageComparator;
import edu.gvsu.cis.cis656.message.MessageTypes;
import edu.gvsu.cis.cis656.queue.PriorityQueue;

import java.net.*;
import java.util.Scanner;

class ChatClient {

    private static DatagramSocket socket = null;
    private static int pid;
    private static InetAddress localHost;
    private static Scanner scanner = new Scanner(System.in);
    private static VectorClock clock = new VectorClock();

    public static void main(String[] args) throws InterruptedException {

        PriorityQueue<Message> messageStack = new PriorityQueue<>(new MessageComparator());
        String username = UserEntry();

        ResponseThread receiverThread = new ResponseThread(socket, messageStack, clock);
        Thread thread  = new Thread(receiverThread);
        thread.start();

        int linecount = 1;
        boolean running = true;
        while (running) {
            System.out.println("Type a message or type exit to terminate the application:  ");
            String line = scanner.nextLine();



            if(line.equals("exit")) {
                System.out.println("Terminating...");
                receiverThread.stop();
                Thread.sleep(500);
                running = false;
            } else {
                clock.tick(pid);
                Message message = new Message(MessageTypes.CHAT_MSG, username, pid, clock, " (" + linecount +  "): " + line);
                Message.sendMessage( message, socket, localHost, 8000);
                linecount++;
            }
        }

        System.exit(0);
    }

    private static String UserEntry() {
        System.out.print("What is your username: ");
        String username = scanner.nextLine();

        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        boolean registered = Registration(username);
        if(registered) {
            clock.addProcess(pid, 0);
        }

        if (!registered) {
            System.out.println("Please restart the app and choose a new username.");
            System.exit(0);
        }
        return username;
    }

    private static boolean Registration(String username)
    {
        try {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            Message message = new Message(MessageTypes.REGISTER, username, 0, null, "reg");
            Message.sendMessage(message, socket, localHost, 8000);

            socket.receive(packet);
            String callback = new String(packet.getData(), 0, packet.getLength());
            Message returned = Message.parseMessage(callback);

            if(returned.type == MessageTypes.ACK) {
                System.out.println(returned.message);
                pid = returned.pid;
            } else if(returned.type == MessageTypes.ERROR) {
                System.out.println(returned.message);
            }

            return returned.type == MessageTypes.ACK;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}