package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer {
    private static HashMap<Long, ChatService> connectedServices = new HashMap<>();;

    public static void main(String[] args){
        if (args.length != 1) {
            System.err.println("Usage: java KnockKnockServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try(ServerSocket serverSocket = new ServerSocket(portNumber);){
            //create threads for each client
            while(true){
                Socket clientSocket = serverSocket.accept();
                executor.execute(new ChatService(clientSocket));
            }
        }
        catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    public static void register(ChatService client){
        connectedServices.put(client.getId(), client);
    }

    public static void remove(ChatService client){
        connectedServices.remove(client.getId());
    }

    public static void broadcastMsg(ChatService issuer, String msg){
        for (Long key : connectedServices.keySet()) {
            if(key != issuer.getId()){
                ChatService client = connectedServices.get(key);
                client.getOut().println(msg);
            }
        }
    }

}

class ChatService extends Thread{
    private Socket clientSocket;
    private String clientName;
    private PrintWriter out = null;
    private BufferedReader in = null;

    public ChatService(Socket socket){
        clientSocket = socket;
        try{
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            try{
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            }
            catch (IOException e1){
                e1.getMessage();
            }
        }
    }

    @Override
    public void run() {
        System.out.println("thread " + this.getId() + " started...");
        try {
            String inputLine;
            String msg;
            out.println("Please enter your name");
            inputLine = in.readLine();
            setClientName(inputLine);
            TcpServer.register(this);
            TcpServer.broadcastMsg(this, clientName + " joins the chat room!");
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("quit")){
                    msg = clientName + " leaves the chat room...";
                    TcpServer.broadcastMsg(this, msg);
                    TcpServer.remove(this);
                    clientSocket.close();
                    System.out.println("Thread " + this.getId() + " closed...");
                    break;
                }
                else{
                    msg = clientName + ":" + inputLine;
                    TcpServer.broadcastMsg(this, msg);
                }
            }
        }
        catch(IOException e){
                e.getMessage();
        }
    }

    public void setClientName(String name){
        clientName = name;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn(){
        return in;
    }

    public String getClientName(){
        return clientName;
    }
}


