package org.example.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UtilFunctions {
    public static byte[] readPackageBytes(DataInputStream dataInputStream, int dataSize) throws IOException {
        byte[] packageBytes = new byte[dataSize];
        dataInputStream.readFully(packageBytes);
        return packageBytes;
    }

    public static int convertToLittleEndian(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
