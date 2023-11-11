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
            try {
                if (networkPackage instanceof DummyPackage) {
                    // Only process and send back DummyPackage

                    Thread.sleep(((DummyPackage) networkPackage).getDelay() * 1000);
                    writeToSocket((DummyPackage) networkPackage);
                    ((DummyPackage) networkPackage).setCompleted(true);
                }
            } catch (InterruptedException e) {
                System.out.println("Package (#" + networkPackage.getId() + ") interrupted!");
            }
    }

    private void writeToSocket(DummyPackage dummyPackage) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            // Convert packages to the format that we received it from the server
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
