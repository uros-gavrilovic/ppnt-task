package org.example.util;

import org.example.model.CancelPackage;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that contains functions that are used in multiple places.
 */
public class UtilFunctions {
    /**
     * The size of each data segment in bytes in the package specification.
     */
    private static final int DATA_SIZE = 4;


    /**
     * Function that creates a NetworkPackage from the binary data that we received from the server.
     *
     * @param dataInputStream The stream that we read the binary data from.
     * @return A NetworkPackage object.
     * @throws IOException If an error occurs while reading the data.
     */
    public static NetworkPackage createNetworkPackage(DataInputStream dataInputStream) throws IOException {
        NetworkPackage networkPackage;

        // Reads 4 bytes (DATA_SIZE) and converts those bytes into an integer following little endian format
        int packageId = bytesToInt(readPackageBytes(dataInputStream));

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

        networkPackage.setLength(bytesToInt(readPackageBytes(dataInputStream)));
        networkPackage.setId(bytesToInt(readPackageBytes(dataInputStream)));

        if (networkPackage instanceof DummyPackage) {
            ((DummyPackage) networkPackage).setDelay(bytesToInt(readPackageBytes(dataInputStream)));
            LocalTime expiresAt = LocalTime.now().plusSeconds(((DummyPackage) networkPackage).getDelay());
            ((DummyPackage) networkPackage).setValidUntil(expiresAt);
        }

        return networkPackage;
    }

    /**
     * Function that reads DATA_SIZE amount of bytes from the stream and returns them as a byte array.
     * Based on package specification, currently it's 4 bytes per data segment.
     *
     * @param dataInputStream The stream that we read the data from.
     * @return A byte array containing the data.
     * @throws IOException If an error occurs while reading the data.
     */
    private static byte[] readPackageBytes(DataInputStream dataInputStream) throws IOException {
        byte[] packageBytes = new byte[DATA_SIZE];
        dataInputStream.readFully(packageBytes);

//        for(int i = 0; i < packageBytes.length; i++) {
//            System.out.println("Byte (#" + i + "): " + packageBytes[i]);
//        }
//        System.out.println("------- NEW DATA SEGMENT -------");

        return packageBytes;
    }

    /**
     * Function that writes all packages to a file in binary format.
     * @param networkPackages The list of packages that we want to write to file.
     */
    public static void writeToFile(ArrayList<NetworkPackage> networkPackages) {
        System.out.println("Saving " + networkPackages.size() + " packages to file...");
        try (OutputStream fileOutputStream = new FileOutputStream("packages.dat");
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            networkPackages.forEach(networkPackage -> {
                try {
                    objectOutputStream.writeObject(networkPackage);
                    System.out.println("Package (#" + networkPackage.getId() + ") saved to file!");
                } catch (IOException e) {
                    System.err.println("Package (#" + networkPackage.getId() + ") failed to be saved to file!");
                }
            });

        } catch (IOException e) {
            System.err.println("Unable to save packages to file!");
        }
    }

    /**
     * Function that writes the package to the socket.
     * The package contents should be converted to a suitable format before writing to the socket.
     *
     * @param dummyPackage The package that we want to write to the socket.
     */
    public static void writeToSocket(DummyPackage dummyPackage) throws IOException{
        Socket socket = new SocketConnectionFactory().connect();
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        // Convert packages to the format of little endian like it was received from the server
        outputStream.write(intToBytes(dummyPackage.getPackageId()));
        outputStream.write(intToBytes(dummyPackage.getLength()));
        outputStream.write(intToBytes(dummyPackage.getId()));
        outputStream.write(intToBytes(dummyPackage.getDelay()));

        socket.close();
        outputStream.close();
    }

    /**
     * Function that reads all packages from file and returns them as a list.
     *
     * @return A list of packages that were read from the binary file.
     */
    public static ArrayList<NetworkPackage> readFromFile() {
        ArrayList<NetworkPackage> networkPackages = new ArrayList<>();

        try (InputStream fileInputStream = new FileInputStream("packages.dat");
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            Object object;
            while ((object = objectInputStream.readObject()) != null) {
                if (object instanceof NetworkPackage) {
                    networkPackages.add((NetworkPackage) object);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Package history not found.");
        } catch (EOFException e) {
            // Do nothing.
        } catch (IOException | ClassNotFoundException e) {
            // If an error occurs, return an empty list.
            System.err.println("Unable to deserialize packages from file!");
        }

        return networkPackages;
    }

    /**
     * Function that grabs all incomplete dummy packages from the list of packages.
     * A dummy package is considered INCOMPLETE if it wasn't sent back to the server, and it hasn't expired yet.
     * INCOMPLETE packages will be sent back to the server.
     *
     * @param packages The list of packages that we want to check.
     * @return A list of incomplete dummy packages.
     * @see DummyPackage
     */
    public static ArrayList<DummyPackage> grabIncompleteDummyPackages(List<NetworkPackage> packages) {
        ArrayList<DummyPackage> incompleteDummyPackages = new ArrayList<>();

        packages.forEach(networkPackage -> {
            if (networkPackage instanceof DummyPackage) {
                DummyPackage dummyPackage = (DummyPackage) networkPackage;

                if (!(dummyPackage).isCompleted() && dummyPackage.getValidUntil().isAfter(LocalTime.now())) {
                    incompleteDummyPackages.add((DummyPackage) networkPackage);
                }
            }
        });

        return incompleteDummyPackages;
    }

    /**
     * Function that grabs all expired dummy packages from the list of packages.
     * A dummy package is considered EXPIRED if it wasn't sent back to the server on time before it expired.
     * EXPIRED packages will be shown as a notification in the console.
     *
     * @param packages The list of packages that we want to check.
     * @return A list of expired dummy packages.
     * @see DummyPackage
     */
    public static ArrayList<DummyPackage> grabExpiredDummyPackages(List<NetworkPackage> packages) {
        ArrayList<DummyPackage> expiredDummyPackages = new ArrayList<>();

        packages.forEach(networkPackage -> {
            if (networkPackage instanceof DummyPackage) {
                DummyPackage dummyPackage = (DummyPackage) networkPackage;

                if (dummyPackage.getValidUntil().isBefore(LocalTime.now()) && !dummyPackage.isCompleted()) {
                    expiredDummyPackages.add((DummyPackage) networkPackage);
                }
            }
        });

        return expiredDummyPackages;
    }

    /**
     * Function that converts a byte array to an integer in little endian format.
     * eg. 0x01|00|00|00 -> 1 (in little endian format)
     * eg. 0x00|00|00|01 -> 1 (in big endian format)
     *
     * @param bytes The byte array that we want to convert.
     * @return An integer in little endian format.
     */
    private static int bytesToInt(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) | ((bytes[2] & 0xFF) << 16) | ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
    }

    /**
     * Function that returns a byte array representing an integer in little-endian format.
     *
     * @param value The integer value to convert.
     * @return The byte array in little-endian format.
     */
    private static byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) ((value >> 16) & 0xFF);
        bytes[3] = (byte) ((value >> 24) & 0xFF);
        return bytes;
    }
}
