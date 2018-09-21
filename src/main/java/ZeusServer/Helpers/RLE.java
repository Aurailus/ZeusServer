package ZeusServer.Helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RLE {
    public static byte[] encode(boolean[] booleans) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            boolean type = booleans[0];
            short count = 0;
            for (boolean bool : booleans) {
                if (bool != type) {
                    bos.write(new byte[]{(byte) (count & 0xff), (byte) ((count >> 8) & 0xff)});
                    bos.write(new byte[]{(byte) (type ? 1 : 0)});
                    type = bool;
                    count = 0;
                }
                count++;
            }
            bos.write(new byte[]{(byte) (count & 0xff), (byte) ((count >> 8) & 0xff)});
            bos.write(new byte[]{(byte) (type ? 1 : 0)});
            return bos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encode(short[] shorts) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            short type = shorts[0];
            short count = 0;
            for (short val: shorts) {
                if (val != type) {
                    bos.write(new byte[]{(byte) (count & 0xff), (byte) ((count >> 8) & 0xff)});
                    bos.write(new byte[]{(byte) (type & 0xff), (byte) ((type >> 8) & 0xff)});
                    type = val;
                    count = 0;
                }
                count++;
            }
            bos.write(new byte[]{(byte) (count & 0xff), (byte) ((count >> 8) & 0xff)});
            bos.write(new byte[]{(byte) (type & 0xff), (byte) ((type >> 8) & 0xff)});
            return bos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static short[] decodeShorts(byte[] bytes) {
        ArrayList<Short> shorts = new ArrayList<>();

        for (var i = 0; i < bytes.length; i+=4) {
            short len = (short)(((bytes[i+1] & 0xFF) << 8) | (bytes[i] & 0xFF));
            short val = (short)(((bytes[i+3] & 0xFF) << 8) | (bytes[i+2] & 0xFF));
            for (var j = 0; j < len; j++) {
                shorts.add(val);
            }
        }

        short[] arr = new short[shorts.size()];

        for (var i = 0; i < shorts.size(); i++) {
            arr[i] = shorts.get(i);
        }

        return arr;
    }

    public static boolean[] decodeBools(byte[] bytes) {
        ArrayList<Boolean> booleans = new ArrayList<>();

        for (var i = 0; i < bytes.length; i+=3) {
            short len = (short)(((bytes[i+1] & 0xFF) << 8) | (bytes[i] & 0xFF));
            boolean val = (bytes[i+2] & 0xFF)==1;
            for (var j = 0; j < len; j++) {
                booleans.add(val);
            }
        }

        boolean[] arr = new boolean[booleans.size()];

        for (var i = 0; i < booleans.size(); i++) {
            arr[i] = booleans.get(i);
        }

        return arr;
    }
}
