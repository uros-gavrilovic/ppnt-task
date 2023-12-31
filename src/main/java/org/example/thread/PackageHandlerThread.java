package org.example.thread;

import lombok.Data;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;
import org.example.util.AppConfig;

import java.io.IOException;

import static org.example.util.UtilFunctions.writeToSocket;

/**
 * Thread that handles the package that we received from the server.
 */
@Data
public class PackageHandlerThread implements Runnable {
    private final NetworkPackage networkPackage;
    private final String serverAddress;
    private final int serverPort;


    public PackageHandlerThread(NetworkPackage networkPackage) {
        this.networkPackage = networkPackage;
        this.serverAddress = AppConfig.getServerAddress();
        this.serverPort = AppConfig.getServerPort();
    }

    /**
     * Function that handles the package that we received from the server.
     */
    @Override
    public void run() {
            try {
                if (networkPackage instanceof DummyPackage) {
                    // Only process and send back DummyPackage to the server.

                    Thread.sleep(((DummyPackage) networkPackage).getDelay() * 1000);
                    writeToSocket((DummyPackage) networkPackage);
                    ((DummyPackage) networkPackage).setCompleted(true);

                    System.out.println("\tOUT: " + networkPackage);
                }
            } catch (IOException e) {
                System.err.println("Package (#" + networkPackage.getId() + ") failed to be written in socket!");
            } catch (InterruptedException e) {
                System.out.println("Package (#" + networkPackage.getId() + ") interrupted!");
            }
    }
}
