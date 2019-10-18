package Account;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Spread.SpreadException;
import Spread.BasicMessageListener;
import Spread.SpreadConnection;
import Spread.SpreadGroup;
import Spread.SpreadMessage;
import Spread.MembershipInfo;

public class BankAccount implements BasicMessageListener {
	
	private boolean isRun;
	private SpreadConnection sConnection;
	private SpreadGroup sGroup;
	private String memberName;
	private int instances;
	private int presentMemebers;
	private BufferedReader reader;
	private List<Transaction> executed_list= new ArrayList<Transaction>();
	private List<Transaction> outstanding_collection = new ArrayList<Transaction>();
	private int order_counter=0, outstanding_counter=0;
	private ArrayList<String> membersInfo = new ArrayList<>();
	private AccountOperations accountOperations=new AccountOperations();
	
	public BankAccount(String host, String accountname, int instances) {
		upDate("New BankAccount Replica host:" + host + ", accountname:" + accountname + ", numReplicas: " + instances);	
		
		// Init spread
		if ( initSpread(host, 4803, accountname) ) {
		
			// Init variables
			accountOperations.setBalance(0.0);
			presentMemebers = 0;
			isRun = true;
			this.instances = instances;	
		}
	}

	
	private void upDate(String message) {
		System.out.println("[Info] " + message);
	}
	private void bye() {
		try {
			isRun = false;
			
			// Leave group
			sGroup.leave();
			upDate("Leave group:" + sGroup);
			
			// Disconnect
			sConnection.remove(this);
			sConnection.disconnect();
			upDate("Disconnect connection");
			
			// Quiz
			System.exit(0);
		} catch (SpreadException e) {
			e.printStackTrace();
		}
	}
	
	private void messageSending(String content) {
		SpreadMessage message = new SpreadMessage();
		message.setSafe();
		message.addGroup(sGroup);
		message.setData(new String(content).getBytes());
		try {
			sConnection.multicast(message);
			upDate("Sent safe message");
		} catch (SpreadException e) {
			errorHandler("messageSend", "SpreadException", e);
		}
	}
	private void errorHandler(String tag, String type, Exception e) {
		System.out.println("[Error] in " + tag + ": " + type + " " + e.getMessage());
	}
	public void userCommand(String line) {
		
		try {
	        switch(line) {
		        case "balance":
		        	accountOperations.balance();
		        	break;
		        case "exit":
		        	bye();
		        	break;
		        default:
		        	String[] options = line.split(" ");
		        	double amount;
		        	switch (options[0]) {
		        		case "balance":
		        			amount = Double.parseDouble(options[1]);
		        			if (accountOperations.getBalance() == 0) {
		        				accountOperations.setBalance(amount);
		        				System.out.println("New balance=" + accountOperations.getBalance());
		        			}
		        			break;
				        case "getQuickBalance":
				        	System.out.println("New balance=" + accountOperations.getBalance());
				        	break;
				        case "deposit":
				        	amount = Double.parseDouble(options[1]);
				    			accountOperations.setBalance(amount);
				    			
					        	Transaction t= new Transaction(options[0]+" "+options[1],options[2]+" "+options[3]);
					        	String checkUniqueID=options[2]+" "+options[3];
					           	executed_list.add(t);
					           	System.out.println(executed_list.get(0).command+" "+executed_list.get(0).unique_id);
					           	for (int i=0;i<outstanding_collection.size();i++) {
						           	if(checkUniqueID.equals(outstanding_collection.get(i).unique_id)){
						           		outstanding_collection.remove(outstanding_collection.get(i));
					           	}

					           	}
					        	System.out.println("Size of the Collection from Deposit "+outstanding_collection.size());
					        	break;
				        	
				        case "withdraw":
				        	amount = Double.parseDouble(options[1]);
				        	 accountOperations.setBalance(amount * (-1));
				        	break;
				        	
				        case "addinterest":
				        	System.out.println("From Interest  from start #########"+ options[1]+" "+options[2]);
				        	double percent = Double.parseDouble(options[1]);
				        	accountOperations.addinginterest(percent);
				        	t= new Transaction(options[0]+" "+options[1],options[2]+" "+options[3]);
				        	String checkUniqueID2=options[2]+" "+options[3];
				        	executed_list.add(t);
				           	for (int i=0;i<outstanding_collection.size();i++) {
					           	if(checkUniqueID2.equals(outstanding_collection.get(i).unique_id)){
					           		outstanding_collection.remove(outstanding_collection.get(i));
				           	}

				           	}
				        	System.out.println("Size of the Collection from addinterest "+outstanding_collection.size());
				        	break;
				        case "getHistory":
				        	for(int i=0;i<executed_list.size();i++) {
				        		System.out.println("Executed Transactions are "+ executed_list.get(i).unique_id);		        		
				        	}
				        	for(int i=0;i<outstanding_collection.size();i++) {
				        		System.out.println("Pending Transactions are "+ outstanding_collection.get(i).unique_id);		        		
				        	}
				        	break;
				        case "memberInfo":
				        	System.out.println(membersInfo);
				        	break;
				        case "cleanHistory":
				        	executed_list.clear();
				        	break;
				        	default:
				        		System.out.println(" Nothing Matched " + options[0]);

					}
		        	break;
	        }
			
		} catch (NumberFormatException e) {
			errorHandler("userCommand", "NumberFormatException", e);
		} catch (ArrayIndexOutOfBoundsException e) {	// split command
			errorHandler("userCommand", "ArrayIndexOutOfBoundsException", e);
		}
	}

	public void userCommand(String line, boolean isCommand) {
		
		try {
	        switch(line) {
		        case "balance":
		        	accountOperations.balance();
		        	break;
		        case "exit":
		        	bye();
		        	break;
		        default:
		        	String[] options = line.split(" ");
		        	double amount;
		        	switch (options[0]) {
		        		case "balance":
		        			amount = Double.parseDouble(options[1]);
		        			if (accountOperations.getBalance() == 0) {
		        				accountOperations.setBalance(amount);
		        				System.out.println("New balance=" + accountOperations.getBalance());
		        			}
		        			break;
				        case "getQuickBalance":
				        	System.out.println("New balance=" + accountOperations.getBalance());
				        	break;
				        case "deposit":
				        	amount = Double.parseDouble(options[1]);
				        	outstanding_collection.add(new Transaction("deposit "+ amount, "uniqueid " + memberName+outstanding_counter));
				        	//messageSending(outstanding_collection);
				        	outstanding_counter=outstanding_counter+1;
				        	
				        	
				        	

				        	break;
				        case "withdraw":
				        	amount = Double.parseDouble(options[1]);
				        	if (isCommand) messageSending("withdraw " + amount);
				        	else accountOperations.setBalance(amount * (-1));
				        	break;
				        	
				        	
				        	case "checkTXstatus":
				        	boolean executed = false;
		        			
				        	for (int i=0;i<executed_list.size();i++) {
				        		String[] id=executed_list.get(i).unique_id.split(" ");
				        		
				        		if(options[1].equals(id[1])) {
				        			

				        			executed = true;
				        		}
				        	}
				        	
				        	if(executed) {
				        		System.out.println(options[1]+ " has been executed");
				        	}else {
				        		System.out.println(options[1]+ " is still pending");
				        	}
				        case "addinterest":
				        	amount = Double.parseDouble(options[1]);
				        	outstanding_collection.add(new Transaction("addinterest "+ amount,"uniqueid " +  memberName+outstanding_counter));
				        	//messageSending(outstanding_collection);
				        	outstanding_counter=outstanding_counter+1;
				        	
				        	
				        	break;
				        case "memberInfo":
				        	System.out.println(membersInfo);
				        	break;
				        case "getSyncedBalance":
				        	if(outstanding_collection.isEmpty())
				        	System.out.println("New balance=" + accountOperations.getBalance());
				        	else messageSending(outstanding_collection);
				        	break;
				        case "getHistory":
				        	System.out.println("Printing the History");
				        	messageSending("getHistory");
				        	break;
				        case "cleanHistory":
				        	System.out.println("Removing the History");
				        	messageSending("cleanHistory");
				        	break;

					}
		        	break;
	        }
			
		} catch (NumberFormatException e) {
			errorHandler("userCommand", "NumberFormatException", e);
		} catch (ArrayIndexOutOfBoundsException e) {	// split command
			errorHandler("userCommand", "ArrayIndexOutOfBoundsException", e);
		}
	
	
	
	
	/*private void executeTenSeconds(List<Transaction> localList) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
		for(int i=0;i<localList.size();i++) {
			Transaction t=localList.get(i);
		messageSending(localList.get(i).command);
		executed_list.add(localList.get(i));
		outstanding_collection.remove(t);
		System.out.println("New balance=" + accountOperations.getBalance());
		}
	}
	*/
		
	new java.util.Timer().schedule( 
		    new java.util.TimerTask() {
		        @Override
		        public void run() {
		        	System.out.println("The Size of the outstanding_collection is "+outstanding_collection.size());
		            messageSending(outstanding_collection);
		        }
		    }, 
		    10000 
		);
}
		private void messageSending(List<Transaction> localList) {
			SpreadMessage message2 = new SpreadMessage();
			message2.setSafe();
			message2.addGroup(sGroup);
			for(int i=0;i<localList.size();i++) {
			
				String content2 =(String)localList.get(i).command+ " "+localList.get(i).unique_id;
				message2.setData(new String(content2).getBytes());
	
			System.out.println("New balance=" + accountOperations.getBalance());
			}
			
			try {
				sConnection.multicast(message2);
				upDate("Sent safe message");
			} catch (SpreadException e) {
				errorHandler("messageSend", "SpreadException", e);
			}
			}
		
		
		
	
	private boolean initSpread(String host, int port, String groupName) {
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(100);
		memberName =  "Instance" + randomInt;
		
		sConnection = new SpreadConnection();
		sGroup = new SpreadGroup();
		try {
			sConnection.connect(InetAddress.getByName(host), port, memberName, false, true);
			sGroup.join(sConnection, groupName);
			upDate("Join group:" + groupName);
			
			sConnection.add(this);
			return true;
		} catch (SpreadException e) {
			errorHandler("initSpread", "SpreadException", e);
		} catch (UnknownHostException e) {
			errorHandler("initSpread", "UnknownHostException", e);
		}
		return false;
	}

	@Override
	public void messageReceived(SpreadMessage message) {

		if (message.isRegular()) {
			messageReceivedRegular(message);
		}
		else if (message.isMembership()) {
			messageReceivedMembership(message);
		}
		else if (message.isReject()) {
			upDate("Receive a rejected message.");
		}
		else {
			upDate("Receive a unkown message.");
		}		
		
	}
	private void messageReceivedRegular(SpreadMessage message) {
		
		if(message.isSafe()) {
			upDate("Receive a regular save message.");
		
			byte data[] = message.getData();
			String content = new String(data);
			
			System.out.println("\t data: " + data.length + " bytes, sender: " + message.getSender() + ", type: " + message.getType());
			System.out.println("\tcontent: " + content);
			
			userCommand(content);
		}
		else {
			upDate("Receive another regular message");
		}
	}
	private void messageReceivedMembership(SpreadMessage message) {
		if(message.isMembership()) {
			
			MembershipInfo info = message.getMembershipInfo();
			SpreadGroup group = info.getGroup();
			
			// ---------------- regular membership ----------------------------
			if (info.isRegularMembership()) {
				SpreadGroup members[] = info.getMembers();
				//GroupID groupID = info.getGroupID();
				
				upDate("Receive a membership message for group " + group + " with " + members.length + " members:");
				for(SpreadGroup member : members) {
					System.out.println("\t\t" + member);
				}
				
				if(info.isCausedByJoin()) {
					System.out.println("\tJOIN of " + info.getJoined());
					membersInfo.add(info.getJoined().toString());
					//					System.out.println("joind=" + joined.toString() + ", myName:" + privateName);	
					CharSequence cs = memberName;
					if (!info.getJoined().toString().contains(cs)) {
						messageSending("balance " + accountOperations.getBalance());
					}
				}	else if(info.isCausedByLeave()) {
					System.out.println("\tLEAVE of " + info.getLeft());
					membersInfo.remove(info.getLeft().toString());
				}	else if(info.isCausedByDisconnect()) {
					System.out.println("\tDISCONNECT of " + info.getDisconnected());
					membersInfo.remove(info.getDisconnected().toString());
				} else if(info.isCausedByNetwork()) {
					System.out.println("\tNETWORK change");
				}
				
				
				presentMemebers = members.length;
//				System.out.println("number of current replicas " + numGroupMembers + ", number of wanted replicas " + numReplicas);
			}
			// ---------------- transition membership -------------------------
			else if(info.isTransition()) {
				upDate("Receive a transition membership message for group " + group);
			}
			// ---------------- self-leave membership -------------------------
			else if(info.isSelfLeave()) {
				upDate("Receive a self-leav membership message for group " + group);
			}

		}
	}
	
	public void run() {
		
		if (sConnection.isConnected()) {

			// Wait for all replicas
			upDate("Wait for " + instances + " replicas join the group.");
			while(instances > presentMemebers) {
				System.out.print("");
			}
			
			upDate("All replicas joined group.");
			
			String line;
	
				while(isRun) {
						reader = new BufferedReader(new InputStreamReader(System.in));
						
						while(isRun) {
							try {
							line = reader.readLine();
							if (line != null) {
								userCommand(line, true);
							} else {
								break;
							}
							}
											
							catch (IOException e) {
								e.printStackTrace();
							}
						}
				}
		}
											
				
				
		
}
	

}
