package ZeusServer.Server;

import ZeusServer.Networking.ClientThread;

import java.net.ServerSocket;
import java.net.Socket;

class GameServer {
    private int port;

    GameServer(int port) {
        this.port = port;
    }

    void start() throws Exception {
        ServerSocket socket = new ServerSocket(this.port);

        while (true) {
            Socket clientSocket = socket.accept();
            ClientThread t = new ClientThread(clientSocket);
            t.start();
        }
    }
}
