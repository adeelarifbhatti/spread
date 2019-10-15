package Client;

import java.net.InetAddress;

import Account.BankAccount;
import Spread.SpreadGroup;
import Spread.SpreadMessage;
import Spread.NULLAuth;
public class FirstClient  {
	
	public void getQuickBalance() {
		
	}
	
	public static void main(String[] args) {
	BankAccount connection = new BankAccount();
	SpreadGroup group = new SpreadGroup();
	
	try {
		
	connection.connect(InetAddress.getByName("localhost"),4803,"privatename",false,true);
	group.join(connection, "UIO");
	System.out.println("####################Conected");
	SpreadMessage msg = new SpreadMessage();
	msg.setSafe();
	msg.addGroup("UIO");
	connection.multicast(msg);

	//connection.disconnect();
	}
	catch(Exception e) {
		System.out.println("Exception is "+ e);
		
	}
	}

	


}
