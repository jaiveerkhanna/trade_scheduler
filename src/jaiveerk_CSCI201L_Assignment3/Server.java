package jaiveerk_CSCI201L_Assignment3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.CommunicationException;
import javax.sound.midi.SysexMessage;


import java.lang.ClassCastException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import com.google.gson.Gson;
import com.google.gson.stream.MalformedJsonException;


public class Server {
	//public static StockList stocks;
	public static String api_token = "1ae4bd22d0a67b888c1a93e069d29413a9f6da56";
	public static ArrayList<Trade> trades = new ArrayList<Trade>();
	public static ArrayList<Trader> traders = new ArrayList<Trader>();
	public static Vector<ServerThread> serverThreads = new Vector<ServerThread>();
	public static Vector<Trade> activeTrades = new Vector<Trade>();
	public static Boolean moreTrades = true;
	static Lock lock = new ReentrantLock();
//	static Condition serverWorking = lock.newCondition(); 
	static Boolean shutdown = false;
	
	public static void main(String [] args) {
		FileReader fr;
		//BufferedReader br;
		String filename = null;
		while(true) {
		try {
			
			// STEP 1a: Prompt User for Name of SCHEDULE FILE
			Scanner in = new Scanner(System.in);
			while(true) {
			Scanner sc = null;
			Scanner dataScanner = null;
			try {
			System.out.print("What is the name of the file containing the schedule information? ");
			filename = in.nextLine();
			//With reference to https://www.javatpoint.com/how-to-read-csv-file-in-java
			sc = new Scanner(new File(filename));
			}
			catch (FileNotFoundException fnfe) {
				System.out.printf("The file %s could not be found. \n\n",filename);
				continue;
			}
			catch (NoSuchElementException nse) {
				System.out.println("Invalid data format");
				continue;
			}
			
			//STEP 1b: PARSE SCHEDULE FILE
			//https://www.journaldev.com/2335/read-csv-file-java-scanner
			while(sc.hasNextLine()) {
				dataScanner = new Scanner(sc.nextLine());
				dataScanner.useDelimiter(",");
				if(!dataScanner.hasNext()) {
					continue;
				}
				try {
					int time = dataScanner.nextInt();
					String ticker = dataScanner.next();
					int quantity = dataScanner.nextInt();
					String date = dataScanner.next();
					Trade temp = new Trade();
					temp.time = time;
					temp.ticker = ticker;
					temp.quantity = quantity;
					temp.date = date;
					
					if(temp.date.length() != 10) {
						System.out.printf("The file %s is not formatted properly. Length of Date is incorrect. \n\n", filename);
						throw new InputMismatchException();
					}
					
					for(int s=0; s<10; s++) {
						if(s==4 || s==7) {
							if(temp.date.charAt(s) != '-') {
								System.out.printf("The file %s is not formatted properly. Expected '-' in date. \n\n", filename);
								throw new InputMismatchException();
							}
						}
						else {
							if(!Character.isDigit(temp.date.charAt(s))) {
								System.out.printf("The file %s is not formatted properly. Expected digit in date. \n\n", filename);
								throw new InputMismatchException();
							}
						}
					}
					
					trades.add(temp);
				}
				catch (InputMismatchException e) {
					System.out.println("Invalid CSV Format, exiting program");
					//System.out.println(sc.next() + " " +  sc.next() + " " +  
					//		sc.next() + " " +  sc.next() + " " +  sc.next() + " "  );
					return;
				}
				catch (NoSuchElementException nse) {
					System.out.println("Invalid data format, no such element exception, please restart and try with proper data");
					return;
				}
				catch(Exception e) {
					System.out.println(e.getLocalizedMessage());
					return;
				}	
			}
			// System.out.println(trades.get(4).ticker); // SANITY CHECK THAT PARSING IS WORKING
			break;
			}
			
			//STEP 2a: Prompt User for name of TRADERS file
			while(true) {
				Scanner sc = null;
				Scanner dataScanner = null;
				try {
				System.out.print("What is the name of the file containing the trade information? ");
				filename = in.nextLine();
				//With reference to https://www.javatpoint.com/how-to-read-csv-file-in-java
				sc = new Scanner(new File(filename));
				}
				catch (FileNotFoundException fnfe) {
					System.out.printf("The file %s could not be found. \n\n",filename);
					continue;
				}
				
			//STEP 2b: Parse TRADERS file
				
				while(sc.hasNextLine()) {
					dataScanner = new Scanner(sc.nextLine());
					dataScanner.useDelimiter(",");
					if(!dataScanner.hasNext()) {
						continue;
					}
					try {
						int serialNum = dataScanner.nextInt();
						int startBal = dataScanner.nextInt();
						Trader temp = new Trader();
						temp.serialNum = serialNum;
						temp.balance = startBal;
						traders.add(temp);
					}
					catch (InputMismatchException e) {
						System.out.println("Invalid CSV Format, exiting program");
						//System.out.println(sc.next() + " " +  sc.next() + " " +  
						//		sc.next() + " " +  sc.next() + " " +  sc.next() + " "  );
						break;
					}
					catch(Exception e) {
						System.out.println(e);
						break;
					}	
				}	
			break;
			}
			
			break;
			}
/*
		catch(IOException ioe) {
			System.out.printf("The file %s is not formatted properly. \n\n", filename);
			//System.out.println(ioe.getMessage());
		}
		catch(ClassCastException cce) {
			System.out.printf("The file %s is not formatted properly. \n\n", filename);
		}*/
		catch (com.google.gson.JsonSyntaxException jse) {
			System.out.printf("The file %s is not formatted properly. \n\n", filename);
		}
		catch (Exception e) {
			System.out.printf("The file %s is not formatted properly. \n\n", filename);
			//System.out.print(e.getMessage());
		}
		
		//--- END OF THE READ/PARSE SEGMENT OF CODE
		
		}
		
		//BEGINNING OF THE ACTUAL SERVER
		
		//ENTIRE SERVER ACCEPT CODE IS ADAPTED FROM LECTURE SLIDES
		//https://courses.uscden.net/d2l/le/content/20489/viewContent/348314/View
		
		
		//Handle the API call and data parsing upfront
		updateTrades();
	
		try {
			ServerSocket ss = new ServerSocket(3456);
			System.out.println("Listening on port 3456. Waiting for traders...");
			while(serverThreads.size() < traders.size()) {
				Socket s = ss.accept(); // blocking code 
				System.out.println("Connection from: " + s.getInetAddress());
				ServerThread st = new ServerThread(s, traders.get(serverThreads.size()));
				//System.out.println("Before");
				serverThreads.add(st);
				if(serverThreads.size() < traders.size()) {
					System.out.println("Waiting for " + (traders.size()-serverThreads.size()) + " more trader(s)");
					broadcast(new Message((traders.size()-serverThreads.size()) + " more trader(s) needed before the service can begin. Waiting..."));
				}
//				System.out.println("After");
			}
		}
		catch (IOException ioe) {
			System.out.println("IOE in Connection to Port");
		}
		catch (Exception e) {
			System.out.println("Exception:" + e.getLocalizedMessage());
		}
		
		System.out.println("Starting service. \n\n");
		broadcast(new Message("All traders have arrived, starting service"));
		
		int previous_time = 0;
		for(int i=0; i < trades.size(); i++) {
			Trade temp = trades.get(i);
			if(temp.time == previous_time) {
//				synchronized (activeTrades) {
				while(temp.time == previous_time) {
					activeTrades.add(temp);
					i++;
					if(i == trades.size()) {
						break;
					}
					temp = trades.get(i);
				}
				i--;
				}
			
			assignTrades();
			try {
			Thread.sleep((temp.time - previous_time)*1000);
			previous_time = temp.time;
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			//previous_time = temp.time;
			}
		
		try {
			
		//Code to make sure the Incomplete Trades and Processing messages go out at the same time for all threads
		while(true) {
			Boolean allDone = true;
			for(int i=0; i<Server.serverThreads.size(); i++) {
				if(Server.serverThreads.get(i).isTrading) {
					allDone = false;
					continue;
				}
			}
			if(allDone) {
				break;
			}
		}
	    
		//Code to broadcast final messages
		System.out.print("Incomplete trades: ");
		for(int i=0; i<activeTrades.size(); i++) {
			Trade temp = activeTrades.get(i);
			System.out.printf("(%d, %s, %d, %s), ", temp.time, temp.ticker, temp.quantity, temp.date);
		}
		System.out.println("");
		
		System.out.println("Processing complete!!");
		}
		catch(Exception e) {
			//
		}
		
		broadcast(new Message(activeTrades, 2));

		for(ServerThread thread : serverThreads) {
			try {
				thread.join();
			}
			catch(Exception e) {
				System.out.println(e.getLocalizedMessage());
			}
		}

		return;
	}
	
	private static void assignTrades() {
		lock.lock();
		for(int j=0; j<traders.size(); j++) {
			ServerThread traderThread = serverThreads.get(j);
			if(traderThread.isTrading) {
				continue;
			}
			if(!Server.activeTrades.isEmpty()) {
//				synchronized (Server.activeTrades) {
				Vector<Trade> currentTrades = new Vector<Trade>();
				for(int i=0; i<Server.activeTrades.size(); i++) {
						Trade temp = Server.activeTrades.get(i);
						if(temp.trade_value < traderThread.trader.balance) {
							if(temp.trade_value > 0) {
								traderThread.trader.balance -= temp.trade_value;
							}
							currentTrades.add(temp);
							Server.activeTrades.remove(i);
							i--; // if we remove a trade we need to change our locaion in activeTrade
						}
					}
				if(currentTrades.size() > 0) {
					traderThread.sendMessage(new Message(currentTrades));
				}
			}
//				}
			if(Server.activeTrades.isEmpty()) {
				return;
			}
	}
		lock.unlock();
	}
	public static void broadcast(Message m) {
		synchronized(serverThreads) {
			for(ServerThread thread : serverThreads) {
				thread.sendMessage(m);
			}
		}
	}
public static void printTrades() {
	for(int i=0; i< trades.size(); i++) {
		System.out.println(trades.get(i).ticker + "    " + trades.get(i).price);
	}
}

public static void updateTrades() {
	for(Trade t : trades) {
	//Trade t = trades.get(0);
		String trade_url = "https://api.tiingo.com/tiingo/daily/"
				+ t.ticker 
				+ "/prices?startDate="
				+ t.date
				+ "&endDate="
				+t.date 
				+ "&token="
				+ api_token;
		InputStream responseStream = null;
		try {
		URL url = new URL(trade_url);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		responseStream = connection.getInputStream();

		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception: " + e.getLocalizedMessage());
		}
		
		//Taken from stack overflow--> https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java

		Scanner s = new Scanner(responseStream).useDelimiter("\\A");
		String result = s.hasNext() ? s.next() : "";
		//get rid of [ and ] from json
		result = result.substring(1,result.length()-1);
		try {
			Gson gson = new Gson();
			Stock temp = gson.fromJson(result, Stock.class);
			t.price = temp.close;
			t.trade_value = t.price * t.quantity;	
		}
		catch (com.google.gson.JsonSyntaxException jse) {
			System.out.printf("The Data is not formatted properly. \n\n");
			System.out.println(jse.getLocalizedMessage());
			jse.printStackTrace();
		}
		catch (Exception e) {
			System.out.printf("The Data is not formatted properly. \n\n");
			//System.out.print(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}

}
	/*
	public static Boolean badFormat(StockList stocks, String filename) {
		for(int i=0; i<stocks.getSize(); i++) {
			Stock tempStock = stocks.data.get(i);
			if(tempStock.name == null || tempStock.startDate == null
					|| tempStock.description == null || tempStock.exchangeCode == null || tempStock.ticker == null
					|| tempStock.stockBrokers == null) {
				System.out.printf("The file %s is missing data parameters. \n\n", filename);
				return true;
			}
			if(!(tempStock.exchangeCode.equalsIgnoreCase("NASDAQ") || tempStock.exchangeCode.equalsIgnoreCase("NYSE"))) {
				System.out.printf("The file %s is not formatted properly. Invalid Exchange. \n\n", filename);
				return true;
			}	
			
			if(tempStock.startDate.length() != 10) {
				System.out.printf("The file %s is not formatted properly. Invalid Date. \n\n", filename);
				return true;
			}
			
			for(int s=0; s<10; s++) {
				if(s==4 || s==7) {
					if(tempStock.startDate.charAt(s) != '-') {
						System.out.printf("The file %s is not formatted properly. Invalid Date. \n\n", filename);
						return true;
					}
				}
				else {
					if(!Character.isDigit(tempStock.startDate.charAt(s))) {
						System.out.printf("The file %s is not formatted properly. Invalid Date. \n\n", filename);
						return true;
					}
				}
			}
			
			//Standardize ticker and exchange code
			stocks.data.get(i).ticker = stocks.data.get(i).ticker.toUpperCase();
			stocks.data.get(i).exchangeCode = stocks.data.get(i).exchangeCode.toUpperCase();
			
			for(int j=0; j<stocks.getSize(); j++) {
				if((tempStock.name.equalsIgnoreCase(stocks.data.get(j).name) || 
						tempStock.ticker.equalsIgnoreCase(stocks.data.get(j).ticker))
						&& i != j) {
					System.out.printf("The file %s is not formatted properly. Duplicates found. \n\n", filename);
					return true;
				}
			}
			
		}
		return false;
	}	

}*/
