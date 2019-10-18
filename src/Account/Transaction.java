package Account;

public class Transaction {
	
	 String command;
	 String unique_id;
	 int order_counter;
public Transaction(String command, String unique_id) {
	this.command=command;
	this.unique_id=unique_id;
	
}
public Transaction(int order_counter, String command, String unique_id) {
	this.order_counter=order_counter;
	this.command=command;
	this.unique_id=unique_id;
	
}


}
