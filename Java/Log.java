
public final class Log {

	/**
	 * @param args
	 */
	public static void debug(Object msg) {
		System.out.println(msg);
	}
	
	public static void charWrite(char msg) {
		System.out.print(msg);
	}

	public static void error(Object msg) {
		
		if(msg instanceof Exception){
			((Exception)msg).printStackTrace();
		}
		else{
			System.out.println(msg);	
		}
		
	}
}
