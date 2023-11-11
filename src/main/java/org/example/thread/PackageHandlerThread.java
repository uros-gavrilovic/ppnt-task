package org.example.thread;

import lombok.Data;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;

@Data
public class PackageHandlerThread implements Runnable {
    private final NetworkPackage networkPackage;

    @Override
    public void run() {
        if (networkPackage instanceof DummyPackage) {
            try {
                Thread.sleep(((DummyPackage) networkPackage).getDelay() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("OUT: " + networkPackage);
        }
    }
}
