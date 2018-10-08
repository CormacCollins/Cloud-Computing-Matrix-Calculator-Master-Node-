import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class WorkDetails {
	public SendWork work;
	public Socket socket;
	
	public WorkDetails(SendWork w, Socket s) {
		work = w;
		socket = s;
	}
}
