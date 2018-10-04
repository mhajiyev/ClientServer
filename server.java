//package com.company;
import java.net.*;
import java.io.*;


public class server {

    private DatagramSocket udpSocket;
    private int req_code;
    private int NegotiationPort;
    private ServerSocket tcpSocket;

    public server(int req_code) throws SocketException, IOException {
        this.req_code = req_code;
        this.udpSocket = new DatagramSocket(0);
        this.NegotiationPort = udpSocket.getLocalPort();
        System.out.println("SERVER_PORT="+Integer.toString(this.NegotiationPort));
    }

    private void listen() throws Exception {
        System.out.println("-- Running Server at " + InetAddress.getLocalHost() + "--");
        String msg;



        while (true) {
            byte[] bufReceive = new byte[256];

            DatagramPacket packet = new DatagramPacket(bufReceive, bufReceive.length); //UDP packet to be received

            udpSocket.receive(packet); //Receiving the req_code from client

            InetAddress clientIP = packet.getAddress();  // Retrieving client's IP address
            int portClient = packet.getPort();  // Retrieving client's port number


            msg = new String(packet.getData()).trim();
            if (Integer.parseInt(msg) != this.req_code) {
                System.out.println("REQUEST CODE IS INVALID!!!SHUTTING DOWN THE SERVER.");
                return;
            } else {    // Request Code is VALID
                this.tcpSocket = new ServerSocket(0);
                int randomPort = tcpSocket.getLocalPort();
                String randomPortString = Integer.toString(randomPort);
                System.out.println("SERVER_TCP_PORT=" + randomPortString);

                DatagramPacket packetOut = new DatagramPacket(randomPortString.getBytes(), randomPortString.getBytes().length, clientIP, portClient); //packet is addressed to clientIP and portClient
                udpSocket.send(packetOut);  // Sending the random port number


                udpSocket.receive(packet);  //Hearing back from the client
                msg = new String(packet.getData()).trim();

                if (Integer.parseInt(msg) == randomPort)  // Validating that client has received the correct random port number
                {
                    System.out.println("Client got the TCP port correctly.Sending an acknowledgement...");

                    String acknowledgement = "Acknowledged";
                    packetOut.setData(acknowledgement.getBytes());
                    udpSocket.send(packetOut);
                } else {
                    System.out.println("Client did not get TCP port.Obfuscated packet!!!");
                }


                listenonTCP();   // Calling the function
            }
        }
    }


    //This function is responsible for the TCP communication with the client
    private void listenonTCP()throws Exception{

        Socket client = tcpSocket.accept();
        String clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("\r\nNew TCP connection from client on "+ clientAddress);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
        String message = in.readLine();             //Receiving the message from the client
        System.out.println("\r\nSERVER_RCV_MSG=" + message);
        String reversedMsg  = new StringBuilder(message).reverse().toString();   // Reversing the original message
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        out.println(reversedMsg);           // Sending the reversed message

      // After completing the interaction with the client, the server continues to listen to other client requests
        //byte[] bufReceive = new byte[256];

        //DatagramPacket packet = new DatagramPacket(bufReceive, bufReceive.length);
        //udpSocket.receive(packet);


    }

    public static void main(String[] args) throws Exception {
        try {
            server client = new server(Integer.parseInt(args[0]));   // this throws if args[0] is not an integer
            client.listen();
        }
        catch (Exception e){System.out.println("WARNING!!!Invalid type of an input argument.Exiting..."); return;}
        }

    }





