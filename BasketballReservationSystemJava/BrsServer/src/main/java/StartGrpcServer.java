import basketball.network.protobuffprotocol.BasketballServiceGrpc;
import basketball.server.BasketballServerGrpcImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Properties;

public class StartGrpcServer {
    private static final int defaultPort = 55556;

    public static void main(String[] args) {

        Properties serverProps;
        try {
            serverProps = getProps();
        } catch (IOException e) {
            System.err.println("Cannot find basketballserver.properties " + e);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(serverProps.getProperty("basketball.server.port"));
        } catch (NumberFormatException ex) {
            System.out.println("Wrong port number! Using default port...");
            port = defaultPort;
        }

        Server server = ServerBuilder
                .forPort(port)
                .addService(getService())
                .build();

        try {
            server.start();
            System.out.println("Basketball server started on port " + port + "...");

            /*
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Theatre Reservation System server is shutting down!");
                server.shutdown();
            }));
             */

            server.awaitTermination();
        } catch (IOException e) {
            System.err.println("Error starting the server" + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Error awaitTermination" + e.getMessage());
        }
    }

    public static BasketballServiceGrpc.BasketballServiceImplBase getService(){
        ApplicationContext context = new ClassPathXmlApplicationContext("BasketballBeans.xml");
        return context.getBean(BasketballServerGrpcImpl.class);
    }

    public static Properties getProps() throws IOException {
        Properties serverProps = new Properties();
        serverProps.load(StartGrpcServer.class.getResourceAsStream("/basketballserver.properties"));
        System.out.println("Server properties set. ");
        serverProps.list(System.out);
        return serverProps;
    }
}
