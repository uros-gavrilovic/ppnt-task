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

    public static void writeToFile(ArrayList<NetworkPackage> networkPackages) {
        System.out.println("Saving " + networkPackages.size() + " packages to file...");
        try (OutputStream fileOutputStream = new FileOutputStream("packages.dat");
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            networkPackages.stream().forEach(networkPackage -> {
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
        } catch (IOException | ClassNotFoundException e) {
            // If an error occurs, return an empty list.
            e.printStackTrace();
        }

        return networkPackages;
    }

    public static List<DummyPackage> grabIncompleteDummyPackages(List<NetworkPackage> packages) {
        List<DummyPackage> incompleteDummyPackages = new ArrayList<>();

        packages.stream().forEach(networkPackage -> {
            if (networkPackage instanceof DummyPackage) {
                if (!((DummyPackage) networkPackage).isCompleted()) {
                    incompleteDummyPackages.add((DummyPackage) networkPackage);
                }
            }
        });

        return incompleteDummyPackages;
    }

    public static List<DummyPackage> grabExpiredDummyPackages(List<NetworkPackage> packages) {
        List<DummyPackage> expiredDummyPackages = new ArrayList<>();

        packages.stream().forEach(networkPackage -> {
            if (networkPackage instanceof DummyPackage) {
                if (((DummyPackage) networkPackage).getValidUntil().isBefore(LocalTime.now())) {
                    expiredDummyPackages.add((DummyPackage) networkPackage);
                }
            }
        });

        return expiredDummyPackages;
    }

    public static int convertToLittleEndian(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static int convertToBigEndian(int value) {
        return Integer.reverseBytes(value);
    }
}
