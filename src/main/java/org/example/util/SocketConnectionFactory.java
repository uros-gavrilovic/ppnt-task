package org.example.util;

import java.io.IOException;
import java.net.Socket;

/**
 * Utility class used to encapsulate the logic of creating a socket connection using Factory Method pattern.
 */
public class SocketConnectionFactory {
    private final String serverAddress;
    private final int serverPort;


    public SocketConnectionFactory() {
        this.serverAddress = AppConfig.getServerAddress();
        this.serverPort = AppConfig.getServerPort();
    }

    public Socket connect() throws IOException {
        return new Socket(serverAddress, serverPort);
    }
}