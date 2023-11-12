package org.example.util;

import org.example.model.CancelPackage;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class UtilFunctions {
    /**
     * The size of each data segment in bytes specified in the package specification.
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
        byte[] packageBytes = new byte[DATA_SIZE];

        int packageId = convertToLittleEndian(readPackageBytes(dataInputStream));

        NetworkPackage networkPackage;
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

        networkPackage.setLength(convertToLittleEndian(readPackageBytes(dataInputStream)));
        networkPackage.setId(convertToLittleEndian(readPackageBytes(dataInputStream)));

        if (networkPackage instanceof DummyPackage) {
            dataInputStream.readFully(packageBytes);
            ((DummyPackage) networkPackage).setDelay(convertToLittleEndian(packageBytes));
            LocalTime expiresAt = LocalTime.now().plusSeconds(((DummyPackage) networkPackage).getDelay());
            ((DummyPackage) networkPackage).setValidUntil(expiresAt);
        }

        return networkPackage;
    }

    /**
     * Function that reads X amount of bytes from the stream and returns them as a byte array.
     * Based on package specification, currently it's 4 bytes per data segment.
     *
     * @param dataInputStream The stream that we read the data from.
     * @return A byte array containing the data.
     * @throws IOException If an error occurs while reading the data.
     */
    public static byte[] readPackageBytes(DataInputStream dataInputStream) throws IOException {
        byte[] packageBytes = new byte[DATA_SIZE];
        dataInputStream.readFully(packageBytes);
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
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Function that writes the package to the socket.
     * @param dummyPackage The package that we want to write to the socket.
     * @param serverAddress The address of the server.
     * @param serverPort The port of the server.
     */
    public static void writeToSocket(DummyPackage dummyPackage, String serverAddress, int serverPort) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            // Convert packages to the format that we received it from the server
            outputStream.writeInt(convertToBigEndian(dummyPackage.getId()));
            outputStream.writeInt(convertToBigEndian(dummyPackage.getLength()));
            outputStream.writeInt(convertToBigEndian(dummyPackage.getPackageId()));
            outputStream.writeInt(convertToBigEndian(dummyPackage.getDelay()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that reads all packages from file and returns them as a list.
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
            e.printStackTrace();
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
     * @param byteArray The byte array that we want to convert.
     * @return An integer in little endian format.
     */
    public static int convertToLittleEndian(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * Function that converts an integer to big endian format.
     * @param value The integer that we want to convert.
     * @return An integer in big endian format.
     */
    public static int convertToBigEndian(int value) {
        return Integer.reverseBytes(value);
    }
}
