package ZeusServer.Networking;

import ZeusServer.Helpers.*;
import ZeusServer.Server.MapGen;
import org.joml.Vector3i;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ZeusServer.Server.MapGen.CHUNK_SIZE;

public class ClientThread extends Thread implements Runnable {
    private Socket socket;
    private Pacman pacman;
    private boolean alive;
    private MapGen mapGen;

    private ThreadPoolExecutor mapGenPool;
    private ArrayList<Future> mapGenFutures;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    private void init() {
        this.pacman = new Pacman(socket);
        mapGen = new MapGen();
        pacman.start();
        alive = true;

        mapGenPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(32);
        mapGenPool.setMaximumPoolSize(64);
        mapGenPool.setKeepAliveTime(32, TimeUnit.SECONDS);

        mapGenFutures = new ArrayList<>();
    }

    private void update() {
        pacman.getPackets((PacketData in) -> {
            switch (in.type) {
                case DEBUG:
                    System.out.println(new String(in.data, StandardCharsets.ISO_8859_1));
                    break;
                case REQUEST_CHUNK:
                    deferredRenderChunk(in);
                    break;
                default:
                    System.out.println("Recieved packet of type " + in.type + "and we can't to deal with it!");
            }
        });
//        pacman.sendPacket(PacketType.DEBUG, "Server to client: Hi! Time is " + System.currentTimeMillis());
    }

    private void deferredRenderChunk(PacketData in) {
        mapGenFutures.add(mapGenPool.submit(() -> {
            Vector3i position = VecUtils.stringToVector(new String(in.data, StandardCharsets.ISO_8859_1));
            if (position == null) return;
            StringBuilder s = new StringBuilder();
            s.append(VecUtils.vectorToString(position));
            s.append("|");

            System.out.println("Generating chunk at position " + position);

            var bytes = ChunkSerializer.encodeChunk(generateChunk(position), generateSides(position));
            if (bytes == null) return;

            s.append(new String(bytes, StandardCharsets.ISO_8859_1));

            pacman.sendPacket(PacketType.BLOCK_CHUNK, s.toString());
        }));
    }

    private ArrayList<short[]> generateSides(Vector3i pos) {
        ArrayList<short[]> sides = new ArrayList<>();

        var array = new short[256];
        for (var i = 0; i < 256; i++) {
            array[i] = mapGen.getBlock(pos.x*CHUNK_SIZE + 16, pos.y*CHUNK_SIZE + i/16, pos.z*CHUNK_SIZE + i%16);
        }
        sides.add(array);

        array = new short[256];
        for (var i = 0; i < 256; i++) {
            array[i] = mapGen.getBlock(pos.x*CHUNK_SIZE - 1, pos.y*CHUNK_SIZE + i/16, pos.z*CHUNK_SIZE + i%16);
        }
        sides.add(array);

        array = new short[256];
        for (var i = 0; i < 256; i++) {
            array[i] = mapGen.getBlock(pos.x*CHUNK_SIZE + i/16, pos.y*CHUNK_SIZE + 16, pos.z*CHUNK_SIZE + i%16);
        }
        sides.add(array);

        array = new short[256];
        for (var i = 0; i < 256; i++) {
            array[i] = mapGen.getBlock(pos.x*CHUNK_SIZE + i/16, pos.y*CHUNK_SIZE - 1, pos.z*CHUNK_SIZE + i%16);
        }
        sides.add(array);

        array = new short[256];
        for (var i = 0; i < 256; i++) {
            array[i] = mapGen.getBlock(pos.x*CHUNK_SIZE + i%16, pos.y*CHUNK_SIZE + i/16, pos.z*CHUNK_SIZE + 16);
        }
        sides.add(array);

        array = new short[256];
        for (var i = 0; i < 256; i++) {
            array[i] = mapGen.getBlock(pos.x*CHUNK_SIZE + i%16, pos.y*CHUNK_SIZE + i/16, pos.z*CHUNK_SIZE - 1);
        }
        sides.add(array);

        return sides;
    }

    private short[] generateChunk(Vector3i pos) {
        var chunk = new short[4096];

        for (var i = 0; i < CHUNK_SIZE; i++) {
            for (var j = 0; j < CHUNK_SIZE; j++) {
                for (var k = 0; k < CHUNK_SIZE; k++) {
                    short fill = mapGen.getBlock(i + pos.x*CHUNK_SIZE, j + pos.y*CHUNK_SIZE, k + pos.z*CHUNK_SIZE);
                    chunk[i + CHUNK_SIZE * (j + CHUNK_SIZE * k)] = fill;
                }
            }
        }
        return chunk;
    }

    @Override
    public void run() {
        init();

        while (alive) {
            update();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
