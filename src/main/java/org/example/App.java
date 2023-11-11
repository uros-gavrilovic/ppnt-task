package org.example;

import org.example.model.CancelPackage;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;
import org.example.util.AppConfig;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class App {
    static final int DATA_SIZE = 4; // 4 Bytes for each data segment in the package

    public static void main(String[] args) {
        String serverAddress = AppConfig.getServerAddress();
        int serverPort = AppConfig.getServerPort();

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to the server.");
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            while (true) {
                NetworkPackage networkPackage;

                byte[] packageBytes = new byte[DATA_SIZE];
                dataInputStream.readFully(packageBytes);
                int packageId = convertToLittleEndian(packageBytes);

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

                dataInputStream.readFully(packageBytes);
                networkPackage.setLength(convertToLittleEndian(packageBytes));

                dataInputStream.readFully(packageBytes);
                networkPackage.setId(convertToLittleEndian(packageBytes));

                if(networkPackage instanceof DummyPackage) {
                    dataInputStream.readFully(packageBytes);
                    ((DummyPackage) networkPackage).setDelay(convertToLittleEndian(packageBytes));
                }

                System.out.println(networkPackage);
                System.out.println("--------------------------------------------------------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int convertToLittleEndian(byte byteArray[]) {
        return ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}