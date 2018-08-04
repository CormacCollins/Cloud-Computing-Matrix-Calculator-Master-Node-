import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class MatrixClient {
	private Socket socket = null;
	//writing simple intiger for matrix size
	private OutputStreamWriter out = null;
	//recieving more complex serializable object
	private ObjectInputStream in = null;
			
	//setup client with requested port
	//TODO: respond to each exception
	public MatrixClient(String hostname, int port) {
		
		try {
			// create a socket
			socket = new Socket(hostname, port);
			//
			out = new OutputStreamWriter(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void setMatrixSize(int n) throws IOException {
		System.out.println("Requesting size " + n);
		BufferedWriter writer = new BufferedWriter(out);
		writer.write(Integer.toString(n));
		writer.newLine();
		writer.flush();
	}
	

	//at this stage don't need to use 'command'
	
	public MatrixResult calculate(String command, int matrixSize) {		
		try {
			setMatrixSize(matrixSize);
		} catch (IOException e) {
			System.out.println("Error writing matrixSize to server");
			e.printStackTrace();
		}
		
		MatrixResult res =  null;
		try {
			res = (MatrixResult) in.readObject();
			out.close();
			in.close();
			socket.close();
		} catch(Exception ex) {
			System.out.println("Error reading server result object");
			System.out.println(ex.getStackTrace());
		}
		
		return res;
	}
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 1024;
		
		if (args.length != 2) {
			System.out.println("Use the default setting...");
		} 
		else {
			//use cmd line args
			hostname = args[0];
			port = Integer.parseInt(args[1]);
		}
		
		MatrixClient client = new MatrixClient(hostname, port);
		int matrixSize = 2; 
		MatrixResult result = client.calculate("matrix-mul", matrixSize);
		System.out.println("Result recieved: error code = " + result.errorcode);		
		
	}

}
