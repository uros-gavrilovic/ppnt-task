package org.example;

import org.example.model.DummyPackage;
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
    /**
     * ExecutorService is used to manage threads.
     */
    private static final ExecutorService executorService = Executors.newCachedThreadPool();


    public static void main(String[] args) {
        String serverAddress = AppConfig.getServerAddress();
        int serverPort = AppConfig.getServerPort();

        KeyListenerThread keyListenerThread = new KeyListenerThread(); // Start listening for key presses.

        ArrayList<NetworkPackage> packages = readFromFile(); // Read all past packages from file.
        ArrayList<DummyPackage> incompletePackages = grabIncompleteDummyPackages(packages);
        ArrayList<DummyPackage> expiredPackages = grabExpiredDummyPackages(packages);
        printStats(packages, incompletePackages, expiredPackages);

        try (Socket socket = new Socket(serverAddress, serverPort);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            System.out.println("Connected to the server!");

            while (!keyListenerThread.isExitSignal()) {
                if (!incompletePackages.isEmpty()){
                    // If there are any incomplete packages from previous sessions, handle them first.
                    System.out.println("Handling incomplete packages from previous sessions...");

                    incompletePackages.forEach(incompletePackage -> handlePackage(incompletePackage, serverAddress, serverPort));
                    incompletePackages.clear();
                }

                // Read binary data and create package from server.
                NetworkPackage networkPackage = createNetworkPackage(dataInputStream);
                packages.add(networkPackage);
                System.out.println("IN: " + networkPackage);
                
                handlePackage(networkPackage, serverAddress, serverPort);
            }
            executorService.shutdownNow(); // Stop all threads after a key has been pressed.
            writeToFile(packages); // Save all previous and current packages to file.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printStats(ArrayList<NetworkPackage> packages, ArrayList<DummyPackage> incompletePackages,
                                   ArrayList<DummyPackage> expiredPackages) {
        if(!packages.isEmpty()) {
            System.out.println("Loaded " + packages.size() + " past packages.");
            System.out.println("Incomplete packages: " + incompletePackages.size()
                    + (!incompletePackages.isEmpty() ? " " + incompletePackages : ""));
            System.out.println("Expired incomplete packages: " + expiredPackages.size()
                    + (!expiredPackages.isEmpty() ? " " + expiredPackages : ""));
        }
    }

    private static void handlePackage(NetworkPackage networkPackage, String serverAddress, int serverPort) {
        PackageHandlerThread packageHandlerThread = new PackageHandlerThread(networkPackage, serverAddress, serverPort);
        executorService.execute(packageHandlerThread); // Start a new thread to handle the package.
    }
}