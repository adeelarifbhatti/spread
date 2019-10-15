package Account;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import Spread.SpreadException;
import Spread.BasicMessageListener;
import Spread.SpreadConnection;
import Spread.SpreadGroup;
import Spread.SpreadMessage;
import Spread.MembershipInfo;

public class BankAccount implements BasicMessageListener {
	private double balance;
	private boolean isRun;
	private SpreadConnection sConnection;
	private SpreadGroup sGroup;
	private String memberName;
	private int instances;
	private int presentMemebers;
	private BufferedReader reader;
	
	public BankAccount(String host, String accounterName, int instances) {
		upDate("New BankAccount Replica host:" + host + ", accounterName:" + accounterName + ", numReplicas: " + instances);	
		
		// Init spread
		if ( initSpread(host, 4803, accounterName) ) {
		
			// Init variables
			balance = 0.0;
			presentMemebers = 0;
			isRun = true;
			this.instances = instances;	
		}
	}
	private void balance() {
		System.out.println("balance=" + balance);
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
	
	private void setBalance(double amount) {
		balance += amount;
		System.out.println("New balance=" + balance);
	}
	private void addinginterest(double percent) {
		balance *= (1 + percent/100);
		System.out.println("New balance=" + balance);
	}
	private void sleeping(int duration) {
		try {
			Thread.sleep(duration * 1000);
		} catch (InterruptedException e) {
			errorHandler("commandSleep", "InterruptedException", e);
		}
	}
	private void errorHandler(String tag, String type, Exception e) {
		System.out.println("[Error] in " + tag + ": " + type + " " + e.getMessage());
	}

	public void userCommand(String line, boolean isCommand) {
		
		try {
	        switch(line) {
		        case "balance":
		        	balance();
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
		        			if (balance == 0) {
		        				balance += amount;
		        				System.out.println("New balance=" + balance);
		        			}
		        			break;
				        case "deposit":
				        	amount = Double.parseDouble(options[1]);
				    		if (isCommand) messageSending("deposit " + amount);
				    		else setBalance(amount);
				        	break;
				        case "withdraw":
				        	amount = Double.parseDouble(options[1]);
				        	if (isCommand) messageSending("withdraw " + amount);
				        	else setBalance(amount * (-1));
				        	break;
				        case "addinterest":
				        	double percent = Double.parseDouble(options[1]);
				        	if (isCommand) messageSending("addinterest " + percent);
				        	else addinginterest(percent);
				        	break;
				        case "sleep":
				        	int duration = Integer.parseInt(options[1]);
				        	sleeping(duration);
				        	break;
					}
		        	break;
	        }
			
		} catch (NumberFormatException e) {
			errorHandler("userCommand", "NumberFormatException", e);
		} catch (ArrayIndexOutOfBoundsException e) {	// split command
			errorHandler("userCommand", "ArrayIndexOutOfBoundsException", e);
		}
	}
	private boolean initSpread(String host, int port, String groupName) {
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(100);
		memberName =  "DoRo" + randomInt;
		
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
			
			userCommand(content, false);
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

//					System.out.println("joind=" + joined.toString() + ", myName:" + privateName);	
					CharSequence cs = memberName;
					if (!info.getJoined().toString().contains(cs)) {
						messageSending("balance " + balance);
					}
				}	else if(info.isCausedByLeave()) {
					System.out.println("\tLEAVE of " + info.getLeft());
				}	else if(info.isCausedByDisconnect()) {
					System.out.println("\tDISCONNECT of " + info.getDisconnected());
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
