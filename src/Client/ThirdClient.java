package Client;

import java.net.InetAddress;

import Account.BankAccount;
import Spread.SpreadGroup;
import Spread.SpreadMessage;
public class ThirdClient  {
	
	public void getQuickBalance() {
		
	}
	
	public static void main(String[] args) {
	BankAccount connection = new BankAccount();
	SpreadGroup group = new SpreadGroup();
	
	try {
		
	connection.connect(InetAddress.getByName("localhost"),4803,"privatename",false,true);
	group.join(connection, "UIO");
	SpreadMessage message = connection.receive();
	System.out.println("Newmembershipmessage "+message.getMembershipInfo().getGroup());



	//connection.disconnect();
	}
	catch(Exception e) {
		System.out.println("Exception is "+ e);
		
	}
	}

	


}
