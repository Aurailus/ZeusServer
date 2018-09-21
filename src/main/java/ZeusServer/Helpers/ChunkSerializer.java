package ZeusServer.Helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static ZeusServer.Helpers.Bytes.*;

public class ChunkSerializer {

    public static byte[] encodeChunk(short[] chunk, ArrayList<short[]> sides) {
        ArrayList<Byte> list = new ArrayList<>();
        byte[] rleArray = RLE.encode(chunk);
        byte[] size = intToBytes(rleArray.length);
        list.add(size[0]);
        list.add(size[1]);
        list.add(size[2]);
        list.add(size[3]);

        ByteArrayInputStream rleStream = new ByteArrayInputStream(rleArray);
        while (rleStream.available() > 0) list.add((byte)rleStream.read());

        for (short[] shorts : sides) {
            addAdjacent(list, shorts);
        }

        byte[] array = new byte[list.size()];
        for (var i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        ByteArrayOutputStream bos;
        try {
            bos = new ByteArrayOutputStream(array.length);
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(array);
            gzip.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        byte[] compressed = bos.toByteArray();

        try {
            bos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return compressed;
    }

    private static void addAdjacent(ArrayList<Byte> list, short[] adj) {
        byte[] rleArray = RLE.encode(adj);
        byte[] size = intToBytes(rleArray.length);
        list.add(size[0]);
        list.add(size[1]);
        list.add(size[2]);
        list.add(size[3]);

        ByteArrayInputStream rleStream = new ByteArrayInputStream(rleArray);
        while (rleStream.available() > 0) list.add((byte)rleStream.read());
    }
}
