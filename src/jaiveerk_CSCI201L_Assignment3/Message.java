package jaiveerk_CSCI201L_Assignment3;

import java.io.Serializable;
import java.util.Vector;

public class Message implements Serializable {
	private static final long serialVersionUID = 1;
	public String description = "";
	public Vector<Trade> trades;
	public Boolean to_execute = false;
	public String print_message = "";
	public float startingBalance = 0;
	
	public  Message(String s){
		if(s.equals("shutdown")) {
			description = "shutdown";
		}
		else {
		description = "print";
		}
		print_message = s;
	}
	
	public  Message(Vector<Trade> t){
		description = "trades";
		trades = t;
	}
	
	public Message(Trader t) {
		description = "initialization";
		startingBalance = t.balance;
	}
	
	public Message(Vector<Trade> t, int j) {
		description = "final";
		trades = t;
	}
	
}
