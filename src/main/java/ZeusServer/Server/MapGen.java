package ZeusServer.Server;

import ZeusServer.Helpers.OpenSimplexNoise;
import org.joml.Vector3i;

import static java.lang.Math.floorMod;

public class MapGen {
    public  static final int CHUNK_SIZE = 16;

    OpenSimplexNoise terrainNoise;
    OpenSimplexNoise treeNoise;
    private long seed;

    public MapGen() {
//        seed = Math.round(Math.random()*1000000);
        seed = 0;
        terrainNoise = new OpenSimplexNoise(seed);
        treeNoise = new OpenSimplexNoise(seed/2);
    }

    public short getBlock(int x, int y, int z) {
        int xx = (int)Math.floor((float)x/8);
        int zz = (int)Math.floor((float)z/8);

        float v = ((float)x/8) - xx;
        float u = ((float)z/8) - zz;

        float point00 = getNoisePoint(xx*8,     zz*8);
        float point10 = getNoisePoint((xx+1)*8, zz*8);
        float point01 = getNoisePoint(xx*8,     (zz+1)*8);
        float point11 = getNoisePoint((xx+1)*8, (zz+1)*8);

        float x1 = lerp(point00, point01, u);
        float x2 = lerp(point10, point11, u);

        float average = lerp(x1, x2, v);

        if (y + 1 - average < -2) return 3;
        if (y + 1 - average < 0) return 2;
        if (y - average < 0) return 1;

        if (y - average < 5)
            if (Math.pow(treeNoise.eval(x, z), 2) > 0.6) return 11;

        if (y - average < 5 && y - average > 3) {
            for (var i = -2; i < 3; i++) {
                for (var j = -2; j < 3; j++) {
                    if (i != 0 || j != 0) {
                        if (Math.pow(treeNoise.eval(x + i, z + j), 2) > 0.6) return 12;
                    }
                }
            }
        }

        if (y - average < 7 && y - average > 5) {
            for (var i = -1; i < 2; i++) {
                for (var j = -1; j < 2; j++) {
                    if (Math.pow(treeNoise.eval(x + i, z + j), 2) > 0.6) return 12;
                }
            }
        }

        if (y - 1 - average < 0) {
            if (Math.random() > 0.2) {
                return (short)(4 + Math.round(Math.random() * 4));
            }
        }
        return 0;
    }

    public short getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    private int multiplyPerlin(int x, int z, int horz, int vert) {
        return (int)(terrainNoise.eval((float)x / horz, (float)z / horz) * vert);
    }

    private static float lerp(float s, float e, float t) {
        return s + (e - s) * t;
    }

    private float getNoisePoint(int x, int y) {
        double value = multiplyPerlin(x, y, 600, 100) * 0.6;
        value += multiplyPerlin(x, y, 100, 50) * 0.6;
        value += multiplyPerlin(x, y, 50, 25) * 0.6;
        return (float)value;
    }
}
