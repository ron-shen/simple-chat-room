package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                Socket chatRoomSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(chatRoomSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(chatRoomSocket.getInputStream()));
        ) {
            InputHandler input = new InputHandler(out);
            input.start();

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println(msg); //read input stream
            }
        }
        catch (Exception e){
            e.getMessage();
            System.exit(1);
        }
    }
}

class InputHandler extends Thread {
    private PrintWriter out;

    public InputHandler(PrintWriter output){
        out = output;
    }

    @Override
    public void run() {
        try{
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String fromUser;
            while((fromUser = stdIn.readLine()) != null){
                System.out.println("Me:" + fromUser);
                out.println(fromUser); //write to output stream
            }
        }
        catch (Exception e){
            e.getMessage();
            System.exit(1);
        }
    }
}

