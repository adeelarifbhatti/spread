package Client;

import java.net.InetAddress;

import Account.BankAccount;
import Spread.SpreadGroup;
public class FirstClient  {
	
	public void getQuickBalance() {
		
	}
	
	public static void main(String[] args) {
	BankAccount connection = new BankAccount();
	try {
	connection.connect(InetAddress.getByName("localhost"),4803,"privatename",false,true);
	System.out.println("####################Conected");
	SpreadGroup group = new SpreadGroup();
	group.join(connection, "UIO");
	connection.disconnect();
	}
	catch(Exception e) {
		System.out.println("Exception is "+ e);
		
	}
	}

	


}
