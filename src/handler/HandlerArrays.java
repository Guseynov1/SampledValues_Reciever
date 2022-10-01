package handler;

import com.sun.istack.internal.NotNull;
import org.pcap4j.core.PcapNativeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class HandlerArrays extends DecodingBytes {
    public static void main(String[] args) throws PcapNativeException {
        ReadPacket.getRawData();
    }


    public static int getMissing(ArrayList<Integer> mas)
    {
        Collections.sort(mas);
        int i = 1;
        while (i < mas.size()){
            if (mas.get(i) - mas.get(i - 1) == 1);
            else {
                System.out.println("Missing number is " + (mas.get(i - 1) + 1));
                mas.add((mas.get(i - 1) + 1));
                Collections.sort(mas);
            }
            i++;
        }
        return i;
    }

    public static long getCRC32(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    @NotNull
    public static byte[] getByte(@NotNull int[] arr) {
        byte[] mas = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            mas[i] = (byte) arr[i];

        }
        return mas;
    }

    @NotNull
    public static int[] getInt(@NotNull byte[] arr) {
        int[] mas = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < 0) {
                mas[i] = (int) arr[i] + 256;
            } else mas[i] = arr[i];
        }
        return mas;
    }

    public static long getBytesToLong(byte[] bytes ) {
        if (bytes == null) return 0;
        long value = 0;
        for (byte aByte : bytes) {
            // value += ( (long)bytes[i] & 0xffL ) << ( 8 * i );
            value = (value << 8) + (aByte & 0xffL);
        }
        return value;
    }

}

