package jaiveerk_CSCI201L_Assignment3;

import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ServerThread extends Thread {
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	public Boolean isTrading = false;
	public Socket socket;
	Trader trader;
	float current_balance;
	public static Boolean allDoneTrading = true;
	
	public ServerThread(Socket s, Trader trader) {
		try {
			this.trader = trader;
			allDoneTrading = false;
			this.current_balance = trader.balance;
			this.socket = s;
			this.oos = new ObjectOutputStream(s.getOutputStream());
			this.oos.flush();
			Message initial = new Message(trader);
			sendMessage(initial); // initialize client side
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
		}
	}
	
	public void sendMessage(Message m) {
		try {
			Server.lock.lock();
			if(m.description.equals("trades")) {
				//System.out.println("ServerThread is now Trading -- Busy");
				isTrading = true;
			}
			oos.writeObject(m);
			oos.flush();
			Server.lock.unlock();
		}
		catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	public void run() {
		try {
			this.ois = new ObjectInputStream(socket.getInputStream());
			}
			catch (Exception e) {
				// TODO: handle exception
				System.out.println(e.getLocalizedMessage());
			}
		while(true) {
			try {
			Message received = (Message) ois.readObject();
			if(received.description.equals("print")) {
				isTrading = false;
				//System.out.println(received.print_message);
			}
			else if(received.description.equals("shutdown")) {
//				for(int i=0; i<Server.serverThreads.size(); i++) {
//					if(Server.serverThreads.get(i).isTrading) {
//						break;
//					}
//				}
//				allDoneTrading = true;
				break;
			}
			}
			catch (Exception e) {
				// TODO: handle exception
				System.out.println(e.getLocalizedMessage());
			}
		}
	}
}