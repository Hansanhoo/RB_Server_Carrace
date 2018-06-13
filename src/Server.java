
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Server Class
 * 
 * @author hansanhoo
 *
 */
public class Server implements Runnable
{
	// Port des Servers
	private int port;
	// Addresse des Servers
	private InetAddress serverAddress;
	private ServerSocket ss2 = null;

	private boolean registerActive = false;
	// amount of cars
	int registerdCars;

	// List of Clients
	private List<ListenAndSend> socketList;

	// ClientsListenerAndSender zu CarList des Clients
	private Map<ListenAndSend, List<Car>> carsSocketMap;

	private ServerTimer timer;

	// constanten
	private static final int min = 2;
	private static final int max = 8;

	/**
	 * 
	 * @param port
	 *            this server is running on
	 */
	public Server(int port)
	{
		this.port = port;
		this.socketList = new ArrayList<>();
		this.carsSocketMap = new HashMap<>();
		this.registerdCars = 0;
	}

	/**
	 * 
	 * @return Map with Sockets to this Server
	 */
	public List<ListenAndSend> getListListenAndSend()
	{
		return this.socketList;
	}

	/**
	 * 
	 * @return serverAddress
	 */
	public InetAddress getServerAddress()
	{
		return this.serverAddress;
	}

	/**
	 * sets boolean if register is allowed or not
	 * 
	 * @param status
	 */
	public void setRegisterActive(Boolean status)
	{
		this.registerActive = status;
	}

	@Override
	public void run()
	{
		// startTimer(); Thread
		// checkForClients();Thread -> checkForCars();->ServerThread
		// if timer 0 && cars > 1 startRace();

		System.out.println("Server Listening......");
		try
		{
			this.ss2 = new ServerSocket(port);

			startTimer();
			listenForClients();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Server error");
		}
	}

	/**
	 * starts ServerTime as a Thread and sets registerActive to true
	 */
	private void startTimer()
	{
		this.registerActive = true;
		this.timer = new ServerTimer(15, this);
		Thread timerThread = new Thread(this.timer);
		timerThread.start();
	}

	/**
	 * starts Listening for Clients
	 */
	private void listenForClients()
	{
		while (true)
		{
			try
			{
				// get Socket that is accepted
				Socket s = this.ss2.accept();

				// falls Localhost als 4Tupel addresse des Clienten sich
				// übergeben lassen(hier localhost)
				this.serverAddress = this.ss2.getInetAddress();

				System.out.println("connection Established");
				// Get Car Name from CLient!And send msg
				ListenAndSend listenSend = new ListenAndSend(s, this);
				this.socketList.add(listenSend);
				this.carsSocketMap.put(listenSend, null);
				Thread th = new Thread(listenSend);
				th.start();

			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Connection Error");
			}
		}
	}

	/**
	 * Sends message to all Clients
	 * 
	 * @param msg
	 */
	public void broadCast(String msg)
	{
		for (int i = 0; i < this.getListListenAndSend().size(); i++)
		{
			ListenAndSend ls = this.getListListenAndSend().get(i);
			ls.sendToClient(msg);
		}
	}

	/**
	 * register Cars if Client enters Car Name
	 */
	public int registerCar(String name, ListenAndSend las)
	{
		if (this.registerActive)
		{

			// Liste von Autos zu Client aus HashMap holen
			List<Car> carsListClient = this.carsSocketMap.get(las);
			if (carsListClient == null)
			{ // falls beim Client noch keine Autos registriert sind
				carsListClient = new ArrayList<>();
			}
			if (!carsListClient.contains(name))
			{
				Car car = new Car(name, las);
				carsListClient.add(car);

				this.carsSocketMap.put(las, carsListClient);
				this.registerdCars++;
				las.sendToClient(
						"Register Car succesful! Your Car Name is : " + name);
				System.out.println("Response to Client  :  "
						+ "Register Car succesful ur car name : " + name);
			}

		}
		else
		{
			las.sendToClient("Register Time is not Active!");
		}
		return this.timer.getDuration();

	}

	/**
	 * gets called if ServerTimer's duration = 0
	 */
	public void notifyFromTimer()
	{
		this.registerActive = false;
		if (this.registerdCars == 0)
		{

			System.out.print("0 PartyMembers! restarted Timer");
		}
		else if (this.registerdCars == 1)
		{

			for (ListenAndSend las : this.carsSocketMap.keySet())
			{

				las.sendToClient(
						"U are the only one who registerd for the Car race!");
			}
		}
		else
		{
			startRace();
		}
		restart();

	}

	public void restart()
	{
		this.registerdCars = 0;
		clearCars();
		this.registerActive = true;
		ServerTimer timer = new ServerTimer(30, this);
		Thread serverTimer = new Thread(timer);
		serverTimer.start();
		broadCast(String.valueOf(30));
	}

	/**
	 * deletes all registered cars
	 */
	public void clearCars()
	{
		for (ListenAndSend las : carsSocketMap.keySet())
		{
			List<Car> carsListClient = this.carsSocketMap.get(las);
			carsListClient = new ArrayList<>();
			carsSocketMap.put(las, carsListClient);
		}
	}

	/**
	 * starts a car Race if there are more than 2 Cars registered
	 */
	public void startRace()
	{
		Race race = new Race();
		List<Car> cars = new ArrayList<>();

		for (ListenAndSend las : carsSocketMap.keySet())
		{

			List<Car> carsClient = carsSocketMap.get(las);
			if (cars != null)
			{ // No cars registered to this client.

				for (Car car : carsClient)
				{
					int speed = ThreadLocalRandom.current().nextInt(min,
							max + 1);
					car.setSpeed(speed);
					car.setTempRace(race);
					cars.add(car);
				}
			}
		}

		Collections.sort(cars);

		ExecutorService executorService = Executors
				.newFixedThreadPool(cars.size());

		List<Future<String>> futures;
		for (Car car : cars)
		{
			System.out.println(car.getName() + " ...");
		}
		try
		{
			futures = executorService.invokeAll(cars);
			// No more tasks are accepted
			executorService.shutdown();
			executorService.awaitTermination(Integer.MAX_VALUE,
					TimeUnit.NANOSECONDS);

			String result = getResultOfRace(futures);
			broadCast(result);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * gets a String with Results of Car Race
	 * 
	 * @param futures
	 * @return String of Placements
	 */
	private String getResultOfRace(List<Future<String>> futures)
	{
		String result = "";
		for (Future<String> future : futures)
		{
			try
			{
				String add = future.get();
				result += add + "\n";
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}
}
