package Account;

public class AccountOperations {
	
	private double balance;
	public void balance() {
		System.out.println("balance=" + balance);
	}
	public void setBalance(double amount) {
		balance += amount;
		System.out.println("New balance=" + balance);
	}
	public double getBalance() {
		return balance;
	}
	public void addinginterest(double percent) {
		balance *= (1 + percent/100);
		System.out.println("New balance=" + balance);
	}

}
