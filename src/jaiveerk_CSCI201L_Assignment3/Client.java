package jaiveerk_CSCI201L_Assignment3;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client extends Thread {
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private float current_balance;
	private float profit = 0;
	
	public Client(String hostname, int port) {
		try {
			Socket s = new Socket(hostname, port);
			oos = new ObjectOutputStream(s.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(s.getInputStream());
			this.start();
		} catch (IOException ioe) {
			System.out.printf("Error trying to start Client with hostname %s, port %d:     %s", hostname, port, ioe.getLocalizedMessage());		}
		catch (Exception e) {
			System.out.printf("Error trying to start Client with hostname %s, port %d:     %s", hostname, port, e.getLocalizedMessage());
		}
	}
	
	public void run() {
		//System.out.println("Running");
		while(true) {
		try {
				Message received = (Message) ois.readObject();
				//System.out.println(received);
				//code to give Client a trader balance for initialization
				if(received.description.equals("initialization")) {
					this.current_balance = received.startingBalance;
					//System.out.println("Initialized with starting balance of $" + this.current_balance);
				}
				else if(received.description.equals("print")){
					System.out.println(received.print_message);
				}
				else if (received.description.equals("trades")) {
					Vector<Trade> currentTrades = received.trades;
					
					for(int i=0; i< currentTrades.size(); i++) {
						Trade temp = currentTrades.get(i);
						String direction = "purchase" ;
						if(currentTrades.get(i).quantity < 0) {
							direction = "sale";
						}
						System.out.printf(getTime() + "Assigned %s of %d stocks of %s. Total cost/gain estimate = $%.2f * %d = $%.2f \n", direction, 
								Math.abs(temp.quantity), temp.ticker, temp.price, Math.abs(temp.quantity), 
								Math.abs(temp.trade_value));
					}
					
					for(int i=0; i< currentTrades.size(); i++) {
						Trade temp = currentTrades.get(i);
						String direction = "purchase" ;
						if(currentTrades.get(i).quantity < 0) {
							direction = "sale";
						}
						System.out.printf(getTime() + "Starting %s of %d stocks of %s. Total cost/gain estimate = $%.2f * %d = $%.2f \n", direction, 
								Math.abs(temp.quantity), temp.ticker, temp.price, Math.abs(temp.quantity), 
								Math.abs(temp.trade_value));
						
						Thread.sleep(1000);
						
						//If we purchased
						if(temp.quantity > 0) {
							System.out.printf(getTime() + "Finished %s of %d stocks of %s. Remaining balance = $%.2f - $%.2f = $%.2f \n", direction, 
									Math.abs(temp.quantity), temp.ticker, current_balance, Math.abs(temp.trade_value), 
									(current_balance-Math.abs(temp.trade_value)));
							current_balance -= temp.trade_value;
						}
						//If we sold
						else {
							System.out.printf(getTime() + "Finished %s of %d stocks of %s. Profit earned till now = $%.2f + %.2f = $%.2f \n", direction, 
									Math.abs(temp.quantity), temp.ticker, profit, Math.abs(temp.trade_value), 
									profit + Math.abs(temp.trade_value));
							profit += Math.abs(temp.trade_value);
						}
					}
					Message response = new Message(getTime() + "Done trading");
					oos.writeObject(response);
					oos.flush();
				}
				else if(received.description.equals("final")){
					System.out.print(getTime() + "Incomplete trades: ");
					for(int i=0; i<received.trades.size(); i++) {
						Trade temp = received.trades.get(i);
						System.out.printf("(%d, %s, %d, %s),  ", temp.time, temp.ticker, temp.quantity, temp.date);
					}
					System.out.printf("Total Profit Earned: $%.2f \n", profit);
					System.out.println(getTime() + "Processing complete!!");
					Message response = new Message("shutdown");
					oos.writeObject(response);
					oos.flush();
					break;
				}
		} 
		catch (EOFException eofe) {
			System.out.println("eofe in Client.run(): " + eofe.getLocalizedMessage());
			break;
		}
		catch (IOException ioe) {
			System.out.println("ioe in Client.run(): " + ioe.getLocalizedMessage());
			ioe.printStackTrace();
			break;
		}
		catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}}
	
	public static void main(String [] args) {
		System.out.println("Welcome to SalStocks v2.0!");
		Scanner in = new Scanner(System.in);
		String hostname = null;
		int port = 0;
		while(true) {
			try {
			System.out.println("Enter the server hostname");
			hostname = in.next();
			}
			catch(InputMismatchException ime) {
				System.out.println("Invalid input" + ime.getMessage());
				}
			catch(Exception e) {
				System.out.println("Invalid input, please try again");
				}
			break;
		}
		while(true) {
			try {
				System.out.println("Enter the server port");
				port = in.nextInt();
				}
				catch(InputMismatchException ime) {
					System.out.println("Invalid input, error code: " + ime.getMessage() + ". Please try again");
					}
				catch(Exception e) {
					System.out.println("Invalid input, please try again");
					}
			break;
			}
			//Client c = new Client("localhost", 3456);
			Client c = new Client(hostname, port);
//		catch(IOException ioe) {
//			System.out.printf("Error trying to start Client with hostname %s, port %d:     %s", hostname, port, ioe.getLocalizedMessage());
//
//		}

}
	
public static String getTime() {
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss.SS");
	LocalDateTime now = LocalDateTime.now();
	return ("[" + dtf.format(now) + "] ");
	
}

}
