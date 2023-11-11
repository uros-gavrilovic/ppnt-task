package org.example.thread;

import lombok.Data;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;

import java.io.*;
import java.net.Socket;

import static org.example.util.UtilFunctions.convertToBigEndian;

@Data
public class PackageHandlerThread implements Runnable {
    private final NetworkPackage networkPackage;
    private final String serverAddress;
    private final int serverPort;

    @Override
    public void run() {
        if (networkPackage instanceof DummyPackage) {
            // Only process and send back DummyPackage
            try {
                Thread.sleep(((DummyPackage) networkPackage).getDelay() * 1000);
                ((DummyPackage) networkPackage).setCompleted(true);
                writeToSocket((DummyPackage) networkPackage);
            } catch (InterruptedException e) {
                System.out.println("Package (" + networkPackage.getId() + ") interrupted! Should be saved in file");
            }
        }
        writeToFile(networkPackage);
    }

    private void writeToFile(NetworkPackage networkPackage) {
        try (OutputStream fileOutputStream = new FileOutputStream("packages.dat", true);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            // Write the object to the file
            objectOutputStream.writeObject(networkPackage);
            objectOutputStream.close();
            System.out.println("NetworkPackage appended to file successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeToSocket(DummyPackage dummyPackage) {
        try (Socket socket = new Socket(serverAddress,
                serverPort); DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            outputStream.writeInt(convertToBigEndian(dummyPackage.getId()));
            outputStream.writeInt(convertToBigEndian(dummyPackage.getLength()));
            outputStream.writeInt(convertToBigEndian(dummyPackage.getPackageId()));
            outputStream.writeInt(convertToBigEndian(dummyPackage.getDelay()));

            System.out.println("\tOUT: " + dummyPackage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
