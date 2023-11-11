package org.example;

import org.example.model.NetworkPackage;
import org.example.thread.KeyListenerThread;
import org.example.thread.PackageHandlerThread;
import org.example.util.AppConfig;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.util.UtilFunctions.*;

public class App {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        String serverAddress = AppConfig.getServerAddress();
        int serverPort = AppConfig.getServerPort();

        KeyListenerThread keyListenerThread = new KeyListenerThread(); // Start listening for key presses.

        ArrayList<NetworkPackage> packages = readFromFile();
        System.out.println("Loaded " + packages.size() + " past packages.");

        try (Socket socket = new Socket(serverAddress, serverPort);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            System.out.println("Connected to the server!");

            while (!keyListenerThread.isExitSignal()) {
                NetworkPackage networkPackage = createNetworkPackage(dataInputStream);
                packages.add(networkPackage);
                System.out.println("IN: " + networkPackage);

                PackageHandlerThread packageHandlerThread =
                        new PackageHandlerThread(networkPackage, serverAddress, serverPort);
                executorService.execute(packageHandlerThread);
            }
            executorService.shutdownNow();
            writeToFile(packages);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}