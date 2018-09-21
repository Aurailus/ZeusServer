package ZeusServer.Server;

import ZeusServer.Server.GameServer;

public class Main {
    public static void main(String... args) throws Exception {
        if (args.length <= 0) throw new Exception("No Port Specified! Port should be the first argument.");

        int port = Integer.parseInt(args[0]);
        GameServer server = new GameServer(port);
        server.start();
    }
}
