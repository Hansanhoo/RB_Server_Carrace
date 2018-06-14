import java.util.concurrent.TimeUnit;

/**
 * Timer for the Server
 * 
 * @author hansanhoo
 *
 */
public class ServerTimer implements Runnable
{
	private int duration;	
	//server this Timer is running on
	private Server server;

	/**
	 * 
	 * @param duration
	 * @param server
	 */
	public ServerTimer(int duration, Server server)
	{
		this.duration = duration;
		this.server = server;
	}

	/**
	 * 
	 * @return timer duration
	 */
	public int getDuration()
	{
		return this.duration;
	}
	/**
	 * starts a timer
	 */
	private void startTimer()
	{
		while (this.duration > 0)
		{
			System.out.println("Remaining: " + duration + " seconds till the race will start!");

			try
			{
				duration--;
				TimeUnit.SECONDS.sleep(1);
			}
			catch (InterruptedException e)
			{
				System.out.println(e.getMessage());
			}
		}
		server.notifyFromTimer();
	}

	@Override
	public void run()
	{
		startTimer();
	}
}