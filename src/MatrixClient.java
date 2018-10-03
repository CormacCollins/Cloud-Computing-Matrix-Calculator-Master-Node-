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
import java.util.Scanner;

public class MatrixClient {
	private static Socket socket = null;
	//writing simple intiger for matrix size
	private static ObjectOutputStream out = null;
	//private static DataOutputStream dos = null;
	private static DataInputStream dis = null;
	//recieving more complex serializable object
	private static ObjectInputStream in = null;
	//private ObjectOutputStream out = null;
	//private String IPaddress = "xxxxxxx";
	//private String username = "xxxxxx";
	//private String password = "xxxxxxxxx";
	private static double [][] a =null;
	private static double [][] b =null;
	static MatrixResult res;

	private int id;
			
	public enum BinaryOperation{
		ADD,
		MUTIPLY,
		SUBSTRACT
	}
	BinaryOperation operation;
	//setup client with requested port
			
	
	//setup client with requested port
	//TODO: respond to each exception
	public MatrixClient(String hostname, int port) {

		
		
		try {
			// create a socket
			socket = new Socket(hostname, port);
			//
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}


	//at this stage don't need to use 'command'
	
	
	
	public static  void CreateMatrix(int size) {
		a = new double[size][size];
		b = new double[size][size];
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
	// main function for send and recive 
	public static void sendObject() throws IOException, ClassNotFoundException {
		String id = null;
		int matrixSize = 100;
		String output;
		Scanner sc = new Scanner (System.in);
		System.out.println("input the opreation you want ");
		System.out.println("1 for add \n 2for multiply \n 3 for minus ");
		System.out.println("4 for check status \n 5 for get result");
		int op = sc.nextInt();
		
		if(op == 1 && op ==2 && op == 3) {
			System.out.println("do you want to input the matrix by hand?");
			boolean temp = sc.nextBoolean();
			if(temp) {
				
			}else {
				System.out.println("inter the size of matrix you want");
				matrixSize = sc.nextInt(); 
				CreateMatrix(matrixSize);
			}
		}else if (op ==4 && op == 5) {
			System.out.println("enter the id");
			id = sc.nextLine();
		}
		SendWork send = new SendWork(op,a,b,id);		
		out = new ObjectOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
		in = new ObjectInputStream(socket.getInputStream());
		out.writeObject(send);
		if(op == 5) {
			res = (MatrixResult) in.readObject();
		}else {
			output = dis.readUTF();
			System.out.println(output);
		}
		out.close();
		in.close();
		dis.close();
		socket.close();
		
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
		try {
		sendObject();
		}catch( IOException e ) {
			e.printStackTrace();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		

		String rowFull = "row_full";
		String dataSp = "data_split";
		
		
	

		
//		try {
//			Thread.sleep(1000000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		switch (res.stat) {
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
