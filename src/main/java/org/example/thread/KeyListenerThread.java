package org.example.thread;

import lombok.Getter;

import java.io.IOException;

/**
 * A thread that listens for any key press and sets the exit signal to true.
 */
@Getter
public class KeyListenerThread extends Thread {
    private volatile boolean exitSignal = false;


    public KeyListenerThread() {
        setDaemon(true); // Set the thread as a daemon to terminate when the main thread exits
        start(); // Start the key listener thread
    }

    @Override
    public void run() {
        System.out.println("Type any key to exit.");
        try {
            System.in.read(); // Wait for any key press
            exitSignal = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}