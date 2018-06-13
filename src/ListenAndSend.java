import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Class to Listen for Input and Send Output (Server)
 * 
 * @author hansanhoo
 *
 */
public class ListenAndSend implements Runnable
{

	private String line = null;
	private BufferedReader is = null;
	private BufferedWriter os = null;
	private Socket s = null;

	private Server server;

	/**
	 * 
	 * @param s
	 * @param server
	 */
	public ListenAndSend(Socket s, Server server)
	{
		this.s = s;
		this.server = server;
	}

	@Override
	public void run()
	{
		getCarName();
	}

	/**
	 * gets Car name From Client and register Car if possible
	 */
	private void getCarName()
	{
		try
		{
			is = new BufferedReader(new InputStreamReader(s.getInputStream()));
		}
		catch (IOException e)
		{
			System.out.println("IO error in server thread");
		}
		try
		{

			line = is.readLine();

			while (line.compareTo("QUIT") != 0)
			{
				String name = line;
				
				// -->RegisterCar get Duration of Timer send it TO Client
				int time = this.server.registerCar(name, this);

				line = String.valueOf(time);
				sendToClient(line);

				line = is.readLine();
			}
		}
		catch (IOException e)
		{
			line = this.toString(); // reused String line for getting thread
									// name
			System.out.println(
					"IO Error/ Client " + line + " terminated abruptly");
		}
		catch (NullPointerException e)
		{
			line = this.toString(); // reused String line for getting thread
									// name
			System.out.println("Client " + line + " Closed");
		}
		finally
		{
			try
			{
				System.out.println("Connection Closing..");
				if (is != null)
				{
					is.close();
					System.out.println(" Socket Input Stream Closed");
				}

			}
			catch (IOException ie)
			{
				System.out.println("Socket Close Error");
			}
		} // end finally
	}

	public void sendToClient(String msg)
	{
		try
		{
			os = new BufferedWriter(
					new OutputStreamWriter(s.getOutputStream()));
			os.write(msg);
			os.newLine();
			os.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
