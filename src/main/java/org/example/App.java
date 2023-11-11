package org.example;

import org.example.model.CancelPackage;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;
import org.example.util.AppConfig;

import java.io.*;
import java.net.Socket;

public class App {
    static final int DATA_SIZE = 4; // 4 Bytes for each data segment in the package

    public static void main(String[] args) {
        String serverAddress = AppConfig.getServerAddress();
        int serverPort = AppConfig.getServerPort();

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to the server.");

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            while(true) {
                NetworkPackage networkPackage;

                byte[] packageIdBytes = new byte[DATA_SIZE];
                dataInputStream.readFully(packageIdBytes); // Read DATA_SIZE (4) number of bytes from the input stream
                int packageId = byteArrayToInt(packageIdBytes); // Convert bytes from the input stream to an integer

                switch (packageId) {
                    case 1:
                        networkPackage = new DummyPackage();
                        break;
                    case 2:
                        networkPackage = new CancelPackage();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown package ID: " + packageId);
                }

                byte[] lenBytes = new byte[DATA_SIZE];
                dataInputStream.readFully(lenBytes);
                networkPackage.setLength(byteArrayToInt(packageIdBytes));

                byte[] idBytes = new byte[DATA_SIZE];
                dataInputStream.readFully(idBytes);
                networkPackage.setLength(byteArrayToInt(idBytes));

                if(networkPackage instanceof DummyPackage) {
                    byte[] delayBytes = new byte[DATA_SIZE];
                    dataInputStream.readFully(delayBytes);
                    ((DummyPackage) networkPackage).setDelay(byteArrayToInt(delayBytes));
                }

                System.out.println(networkPackage);
                System.out.println("--------------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to convert a byte array to an integer
    private static int byteArrayToInt(byte[] byteArray) {
        return (byteArray[3] & 0xFF) << 24 |
                (byteArray[2] & 0xFF) << 16 |
                (byteArray[1] & 0xFF) << 8 |
                (byteArray[0] & 0xFF);
    }
}