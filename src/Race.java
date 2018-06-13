/**
 * Race Class
 * 
 * @author hansanhoo
 *
 */
public class Race
{

	int place;

	public Race()
	{
		place = 0;
	}

	/**
	 * gets Called if a Car enters the finish
	 * 
	 * @return
	 */
	public int finish()
	{
		return ++place;
	}

}
