package <%= @pkg %>;

import org.eclipse.jetty.server.Server;

import me.geso.avans.jetty.JettyServerBuilder;
import <%= @pkg %>.controller.RootController;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new JettyServerBuilder()
            .setPort(21110)
            .registerPackage(RootController.class.getPackage())
            .build();
        server.start();
        server.join();
    }
}
