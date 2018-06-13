/**
 * class to Start Server
 * 
 * @author hansanhoo
 *
 */
public class startUpServer
{

	public static void main(String[] args)
	{
		Server s1 = new Server(5555);
		Thread thServer = new Thread(s1);
		thServer.start();

	}

}
