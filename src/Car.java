import java.util.concurrent.Callable;

import java.util.concurrent.TimeUnit;

/**
 * represents a car
 * @author hansanhoo
 *
 */
public class Car implements Callable<String>, Comparable<Car>
{

	private String name;
	private int place;
	private int speed;
	private ListenAndSend las;
	private Race tempRace;

	/**
	 * 
	 * @param name
	 *            of Car
	 * @param las
	 *            ListenAndSend
	 */
	public Car(String name, ListenAndSend las)
	{
		this.las = las;
		this.place = 0;
		this.name = name;
		this.speed = 0;
	}

	@Override
	public String call()
	{
		String sendToC ="";
		try
		{
			TimeUnit.SECONDS.sleep(speed);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		this.place = tempRace.finish();

		String sendToA = "\n Platz:" + this.place + "| AutoName:"
				+ this.getName() + " mit Speed: " + this.speed;
		if(this.place == 1) {
			sendToC += "Winnnnnnnnner Winner Chicken Dinner !!!! \n";
		}
		sendToC += "\n Sie erreichten Platz : " + this.place
				+ " mit ihrem Auto:" + this.getName() + " Sie fuhren "
				+ this.speed + " sec schnell! Das Rennen ist beendet!";
		las.sendToClient(sendToC);

		return sendToA;
	}

	/**
	 * getName of Car
	 * 
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * sets Race this car is temporary driving
	 * 
	 * @param race
	 */
	public void setTempRace(Race race)
	{
		tempRace = race;
	}

	/**
	 * sets Race this car is temporary driving
	 * 
	 * @param race
	 */
	public void setSpeed(int speed)
	{
		this.speed = speed;
	}

	@Override
	public int compareTo(Car arg0)
	{
		return this.speed - arg0.speed;
	}
}
