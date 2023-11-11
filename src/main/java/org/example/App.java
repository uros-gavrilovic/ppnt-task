package org.example;

import org.example.model.CancelPackage;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;
import org.example.thread.PackageHandlerThread;
import org.example.util.AppConfig;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.example.util.UtilFunctions.convertToLittleEndian;
import static org.example.util.UtilFunctions.readPackageBytes;

public class App {
    static final int DATA_SIZE = 4; // 4 Bytes for each data segment in the package
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        String serverAddress = AppConfig.getServerAddress();
        int serverPort = AppConfig.getServerPort();

        try (Socket socket = new Socket(serverAddress, serverPort);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            System.out.println("Connected to the server.");

            while (true) {
                NetworkPackage networkPackage;

                byte[] packageBytes = new byte[DATA_SIZE];
                int packageId = convertToLittleEndian(readPackageBytes(dataInputStream, DATA_SIZE));

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

                networkPackage.setLength(convertToLittleEndian(readPackageBytes(dataInputStream, DATA_SIZE)));
                networkPackage.setId(convertToLittleEndian(readPackageBytes(dataInputStream, DATA_SIZE)));

                if(networkPackage instanceof DummyPackage) {
                    dataInputStream.readFully(packageBytes);
                    ((DummyPackage) networkPackage).setDelay(convertToLittleEndian(packageBytes));
                }

                System.out.println("IN: " + networkPackage);
                executorService.execute(new PackageHandlerThread(networkPackage));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}