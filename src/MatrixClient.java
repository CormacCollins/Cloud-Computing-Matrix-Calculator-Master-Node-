import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class MatrixClient {
	private Socket socket = null;
	//writing simple intiger for matrix size
	private OutputStreamWriter out = null;
	private DataOutputStream dos = null;
	//private DataInputStream dis = null;
	//recieving more complex serializable object
	private ObjectInputStream in = null;
	//private ObjectOutputStream out = null;
	//private String IPaddress = "xxxxxxx";
	//private String username = "xxxxxx";
	//private String password = "xxxxxxxxx";
	private Integer[][] a;
	private Integer[][] b;
			
	public enum BinaryOperation{
		ADD,
		MUTIPLY,
		SUBSTRACT
	}
	BinaryOperation operation;
	//setup client with requested port
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
	/*
	private void setMatrixSize(int n, String command) throws IOException {
		System.out.println("Requesting size " + n);
		BufferedWriter writer = new BufferedWriter(out);
		writer.write(command + ":" + Integer.toString(n));
		writer.newLine();
		writer.flush();
	}
	*/

	//at this stage don't need to use 'command'
	
	public MatrixResult calculate(String command, int matrixSize) {		
		try {
			//setMatrixSize(matrixSize, command);
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(matrixSize);
			dos.writeUTF(operation.toString());
		} catch (IOException e) {
			System.out.println("Error writing matrixSize to server");
			e.printStackTrace();
		}
		
		MatrixResult res =  null;
		try {
			in = new ObjectInputStream(socket.getInputStream());
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
	
	public  void CreateMatrix(int size) {
		a = new Integer[size][size];
		b = new Integer[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				a[i][j] = (int) (Math.random() * 10);
				b[i][j] = (int) (Math.random() * 10);
				// number in matrix is around 1-10, to get better result
			}
		}
		// print matrix to check the result
		//print_2D(a);
		System.out.println("\n");
		//print_2D(b);

	}
	
	public static void print_2D(Integer[][] c2) {
		for (Integer[] row : c2)

			// converting each row as string
			// and then printing in a separate line
			System.out.println(Arrays.toString(row));

	}
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 1024;
		
		int matrixSize = 100	;
		
		if (args.length != 3) {
			System.out.println("Use the default setting...");
		} 
		else {
			//use cmd line args
			hostname = args[0];
			port = Integer.parseInt(args[1]);
			matrixSize = Integer.parseInt(args[2]);

//			String s = args[3];
//			System.out.println("Process " + s);
			
		}
		

		String rowCol = "row_column";
		
		
		
		MatrixClient client = new MatrixClient(hostname, port);
		
		MatrixResult result = client.calculate(rowCol, matrixSize);
		



		
//		try {
//			Thread.sleep(1000000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		switch (result.stat) {
			case successful_calculation:
				System.out.println("Result successful");
//				for(int i = 0; i < matrixSize; i++) {
//					for(int j = 0; j < matrixSize; j++) {
//						System.out.print(result.answer[i][j] + ", ");
//					}
//					System.out.println();
//				}
				break;
			case network_error:
				System.out.println("Connecection error - please check connection");
				break;
			case invalid_paramaters:
				System.out.println("Invalid params needs format '	");
				break;
			case calc_error:
				System.out.println("Sorry there was an erorr with the calculation");
				break;
			case client_request_read_error:
				System.out.println("Server could not read socket input from client");
				break;
			default:
			break;
		}
		
//		System.out.println("Result recieved: error code = " + result.errorcode);		
		
	}

}
