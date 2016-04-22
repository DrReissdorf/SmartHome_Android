package home.sven.smarthome_android.socket;

import java.net.*;
import java.util.Enumeration;

public class UdpDiscover {
    public static String findIP(String discoveryMessage, int udpPort) {
        // Find the server using UDP broadcast
        try {
            //Open a random TCP_CONTROL_PORT to send the package
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);
            c.setSoTimeout(3000);

            byte[] sendData = discoveryMessage.getBytes();

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, udpPort);
                        c.send(sendPacket);
                    } catch (Exception e) {
                    }

                    System.out.println(" >>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }

            System.out.println(" >>> Done looping over all network interfaces. Now waiting for a reply!");

            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket);

            //We have a response
            System.out.println(" >>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
            String message = new String(receivePacket.getData()).trim();
            c.close();
            return message;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
