package org.example;

import org.example.model.NetworkPackage;
import org.example.thread.KeyListenerThread;
import org.example.thread.PackageHandlerThread;
import org.example.util.AppConfig;
import org.example.util.UtilFunctions;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.util.UtilFunctions.*;

public class App {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        String serverAddress = AppConfig.getServerAddress();
        int serverPort = AppConfig.getServerPort();

        KeyListenerThread keyListenerThread = new KeyListenerThread();

        int leftoverPackages = readPackagesFromFile().size();
        if(leftoverPackages != 0)
            System.out.println("There are " + leftoverPackages + " packages left from the previous session.");

        try (Socket socket = new Socket(serverAddress, serverPort);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            System.out.println("Connected to the server!");

            while (!keyListenerThread.isExitSignal()) {
                NetworkPackage networkPackage = createNetworkPackage(dataInputStream);
                System.out.println("IN: " + networkPackage);

                PackageHandlerThread packageHandlerThread = new PackageHandlerThread(networkPackage, serverAddress, serverPort);
                executorService.execute(packageHandlerThread);
            }
            executorService.shutdownNow();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}