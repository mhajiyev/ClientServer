//package com.company;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {

    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int port;
    private Scanner scanner;
    public int request_code;
    private String originalMessage;
    private Socket tcpSocket;

    private client(String serverAddr, int negotiationPort,int reqCode, String message) throws IOException {
        this.serverAddress = InetAddress.getByName(serverAddr);
        this.port = negotiationPort;                    //  Client saves the negotiation port received from the server
        this.udpSocket = new DatagramSocket(0);   //Creates udpSocket binded to an available port
        scanner = new Scanner(System.in);
        this.request_code = reqCode;
        this.originalMessage = message;
    }


    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length!=4 ) {
            System.out.println("WARNING!!!Invalid number of input arguments.Exiting..."); return;}

        try {
        client sender = new client(args[0], Integer.parseInt(args[1]),Integer.parseInt(args[2]),args[3]);   //if neg_port and req_code are not integer then it throws
                                                                                                            //similarly, if args[0] is not a valid IP address it throws
        System.out.println("-- Running UDP Client at " + InetAddress.getLocalHost() + " -->");
        sender.start();}

        catch (Exception e){System.out.println("WARNING!!!Invalid type of an input argument.Exiting..."); return;}
    }

    private int start() throws IOException {
        String reqCode = Integer.toString(this.request_code);

              DatagramPacket packet = new DatagramPacket(
                      reqCode.getBytes(), reqCode.getBytes().length, serverAddress, port);

              udpSocket.send(packet);   //Sending the req_code via UDP to the server

              byte [] bufReceived = new byte[256];
              DatagramPacket packetReceived = new DatagramPacket(bufReceived,bufReceived.length);

              udpSocket.receive(packetReceived);   // Client receives a random port number sent by the server for TCP communication

              String msg = new String(packetReceived.getData()).trim();
              int randomPort = Integer.parseInt(msg);  // Saving the random port number for later use

              System.out.println("Received a TCP port number from the server. It is "+randomPort);

              packet.setData(msg.getBytes());
              udpSocket.send(packet);   // Sending back the random port number to server to verify

              udpSocket.receive(packetReceived);  // Receiving an acknowledgement status
              msg = new String(packetReceived.getData()).trim();

              if (msg.equals("Acknowledged")) {
                  System.out.println("Server has acknowledged the transfer.Sending the original message through TCP...");
                  udpSocket.close();
              }
              else System.out.println("Server has not validated the TCP port number ");


              //Transaction using TCP sockets

              this.tcpSocket = new Socket(serverAddress,randomPort);
              PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
              BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
              out.println(originalMessage);   // Sending the original message to server
              String resp = in.readLine();    // Receiving the reversed message
              System.out.println("CLIENT_RCV_MSG="+resp);

              return 0;

    }


}
