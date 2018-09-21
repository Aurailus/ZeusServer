package ZeusServer.Helpers;

import org.joml.Vector3i;

public class VecUtils {
    public static Vector3i stringToVector(String in) {
        String[] coords = in.split(",");
        if (coords.length != 3) return null;
        Vector3i pos = new Vector3i();
        try {
            pos.x = Integer.valueOf(coords[0]);
            pos.y = Integer.valueOf(coords[1]);
            pos.z = Integer.valueOf(coords[2]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return pos;
    }

    public static String vectorToString(Vector3i in) {
        return vectorToString(in.x, in.y, in.z);
    }

    public static String vectorToString(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}