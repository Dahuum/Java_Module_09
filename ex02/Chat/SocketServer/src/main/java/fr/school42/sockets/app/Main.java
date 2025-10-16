package fr.school42.sockets.app;

import fr.school42.sockets.config.SocketsApplicationConfig;
import fr.school42.sockets.server.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	public static void main (String args[]) 
	{
		int port = 8081;

		for (String arg: args) {
			if (arg.startsWith("--port=")) port = Integer.parseInt(arg.substring(7));
		}
		
		try {
		    System.out.println("Wa l9lawi, rah kayna try catch fl main ? ");
    		ApplicationContext context = new AnnotationConfigApplicationContext(SocketsApplicationConfig.class);
    
    		Server server = context.getBean(Server.class);
    		server.start(port);
    		((ConfigurableApplicationContext) context).close();
		} catch (Exception e) { System.err.println(e.getMessage()); }
	}
}