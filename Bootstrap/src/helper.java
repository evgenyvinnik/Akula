/*
 * This class defines helper methods.
 */
public class helper {

	public void ASSERT(boolean arg)
	{
		if(!arg)
		{
			System.exit(0);
		}
	}
	
	public void ASSERT(String log_msg, boolean arg)
	{
		if(!arg)
		{
			System.out.println("ASSERT: " + log_msg);
			System.exit(0);
		}
	}
}
