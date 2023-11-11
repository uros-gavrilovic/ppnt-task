package org.example.util;

import org.example.model.CancelPackage;
import org.example.model.DummyPackage;
import org.example.model.NetworkPackage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class UtilFunctions {
    private static final int DATA_SIZE = 4;

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

    public static byte[] readPackageBytes(DataInputStream dataInputStream) throws IOException {
        byte[] packageBytes = new byte[DATA_SIZE];
        dataInputStream.readFully(packageBytes);
        return packageBytes;
    }

    public static List<NetworkPackage> readPackagesFromFile() {
        List<NetworkPackage> networkPackages = new ArrayList<>();

        try (InputStream fileInputStream = new FileInputStream("packages.dat");
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            while (fileInputStream.available() > 0) {
                NetworkPackage networkPackage = (NetworkPackage) objectInputStream.readObject();

                if (networkPackage instanceof DummyPackage) {
                    DummyPackage dummyPackage = (DummyPackage) networkPackage;
                    System.out.println("Read DummyPackage: " + dummyPackage);
                } else if (networkPackage instanceof CancelPackage) {
                    CancelPackage cancelPackage = (CancelPackage) networkPackage;
                    System.out.println("Read CancelPackage: " + cancelPackage);
                }

                networkPackages.add(networkPackage);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
            return networkPackages;
        }

        return networkPackages;
    }

    public static int convertToLittleEndian(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static int convertToBigEndian(int value) {
        return Integer.reverseBytes(value);
    }
}
