package Client;

import Account.BankAccount;

public class ThirdClient {

	public static void main(String[] args) {
		
		try {
			
			// Get arguments
			String host = args[0];
			String accountName = args[1];
			int numReplicas = Integer.parseInt(args[2]);

			
			// Create a replica account and run it
			BankAccount bankAccount = new BankAccount(host, accountName, numReplicas);
			bankAccount.run();
			
		} catch(Exception e) {
			System.out.println("Usage:\n"
				+ "\tjava BankAccount <server address> <account name> <number of replicas> ");
			e.printStackTrace();
		}
	}
}