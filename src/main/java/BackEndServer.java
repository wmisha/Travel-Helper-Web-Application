import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;



public class BackEndServer {
    public static final int PORT = 8080;

    public static void main(String[] args){
        Server server = new Server(PORT);

        // Context 1 =
        ServletContextHandler context1 = new ServletContextHandler();
        context1.setContextPath("/user");
        context1.addServlet(UserRegisterServlet.class, "/");

        ServletContextHandler context11 = new ServletContextHandler();
        context11.setContextPath("/user/login");
        context11.addServlet(UserLoginServlet.class, "/");

        ServletContextHandler context2 = new ServletContextHandler();
        context2.setContextPath("/hotel");
        context2.addServlet(SearchHotelServlet.class, "/");

        ServletContextHandler context22 = new ServletContextHandler();
        context22.setContextPath("/hotel/review");
        context22.addServlet(SearchReviewServlet.class, "/");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { context1, context2, context11, context22 });

        server.setHandler(handlers);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
