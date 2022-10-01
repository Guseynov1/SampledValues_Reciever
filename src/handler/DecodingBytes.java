package handler;

import com.sun.istack.internal.NotNull;
import lombok.Data;

import java.util.Arrays;

@Data
public class DecodingBytes extends ReadPacket {

    // TODO: level 2
    public static int[] smpCnt, confRev, smpSynch;
    public static int end_ASDU;
    // TODO: level 3
    public static int[] PhsMeas;

    public static int findElementASDU(int k, @NotNull int [] output){
        if (k >= output.length) {
            System.out.println("Not frame check sequence");
            end_ASDU = output.length;
            return end_ASDU;
        }
        if(output[k] == 0x82 && output[k-2] == 0x30) {
            smpCnt =  Arrays.copyOfRange(output, k, k+output[k+1]+2);
            k = k+output[k+1]+2;
            findElementASDU(k, output);
        }
        else if(output[k] == 0x83 && output[k-4] == 0x82){
            confRev =  Arrays.copyOfRange(output, k, k+output[k+1]+2);
            k = k+output[k+1]+2;
            findElementASDU(k, output);
        }
        else if(output[k] == 0x85) {
            smpSynch =  Arrays.copyOfRange(output, k, k+output[k+1]+2);
            k = k+output[k+1]+2;
            findElementASDU(k, output);
        }
        else if(output[k] == 0x87 && output[k-3] == 0x85) {
            PhsMeas =  Arrays.copyOfRange(output, k, k+output[k+1]+2);
            k = k+output[k+1]+2;
            findElementASDU(k, output);
        }
        else end_ASDU = k;
        return end_ASDU;
    }

    public static int[] findAPDU(@NotNull int [] output){
        int[] svid = null;
        for (int i = 0; i < output.length; i++) {
            if(output[i] == 0xa2 && output[i+2] == 0x30 && output[i+1] == (output[i+3]+2) && output[i+4] == 0x80) {
                svid = Arrays.copyOfRange(output, i+4, i+4+2+output[i+5]+1);
                svid[svid.length-1] = i+4+2+output[i+5]+1;
                break;
            }
            else if (output[i] == 0xa2 && output[i+3] == 0x30 && output[i+2] == (output[i+4]+2) && output[i+5] == 0x80 && output[i+1] == 0x81) {
                svid = Arrays.copyOfRange(output, i+5, i+5+2+output[i+6]+1);
                svid[svid.length-1] = i+5+2+output[i+6]+1;
                break;
            }
        }
        return svid;
    }


    @NotNull
    public static String svID_decode(@NotNull int [] svid){
        StringBuilder SVID = new StringBuilder();
        for (int svID_i: svid) {
            SVID.append((char) svID_i);
        }
        return SVID.toString();
    }

    @NotNull
    public static long [] IU_decode(int[] PhsMeas){
        int[] ph_value = Arrays.copyOfRange(PhsMeas, 2, 2+PhsMeas[1]);
        long[] iu = new long[ph_value.length/4];
        for (int i = 0; i < ph_value.length; i=i+4) {
            if (ph_value[i] != 0xff) {
                long k = ((((long) ph_value[i] << 8 | ph_value[i + 1]) << 8 | ph_value[i + 2]) << 8 | ph_value[i + 3]); iu[i/4] = k;
            }
            else {
                long k = ((((long) (ph_value[i]^0xff) << 8 | (ph_value[i + 1]^0xff)) << 8 | (ph_value[i + 2]^0xff)) << 8 | (ph_value[i + 3]^0xff)); iu[i/4] = -k-1;
            }
        }
        return iu;
    }
    @NotNull
    public static long [] validity_decode(int[] PhsMeas){
        int[] ph_value = Arrays.copyOfRange(PhsMeas, 6, 2+PhsMeas[1]);
        long[] quality = new long[ph_value.length/4];
        for (int i = 0; i < ph_value.length; i=i+4) {
            if (ph_value[i] != 0xff) {
                long k = ((((long) ph_value[i] << 8 | ph_value[i + 1]) << 8 | ph_value[i + 2]) << 8 | ph_value[i + 3]); quality[i/4] = k;
            }
            else {
                long k = ((((long) (ph_value[i]^0xff) << 8 | (ph_value[i + 1]^0xff)) << 8 | (ph_value[i + 2]^0xff)) << 8 | (ph_value[i + 3]^0xff)); quality[i/4] = -k-1;
            }
        }
        return quality;

    }

    public static int getDecode(int[] bytes, int length) {
        int[] byt = Arrays.copyOfRange(bytes, 2, 2 + bytes[1]);
        int val = 0;
        if (length > 132) throw new RuntimeException("Too big to fit");
        for (int i = 0; i < length; i++) {
            val = (val << 8) + (byt[i] & 0xff);
        }
        return val;
    }

    @NotNull
    public static String hexDecode(@NotNull int[] bytes) {
        char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int sym = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[sym / 16];
            hexChars[i * 2 + 1] = hexArray[sym % 16];
        }
        return new String(hexChars);
    }


}
