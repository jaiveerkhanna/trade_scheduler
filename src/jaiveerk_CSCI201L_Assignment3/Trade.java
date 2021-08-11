package jaiveerk_CSCI201L_Assignment3;

import java.io.Serializable;

public class Trade implements Serializable {
	private static final long serialVersionUID = 1;
	int time;
	String ticker;
	int quantity;
	String date;
	float price = 0;
	float trade_value = 0;
	
/*	public int getQuantity() {
		return this.quantity;
	}*/
	
}
