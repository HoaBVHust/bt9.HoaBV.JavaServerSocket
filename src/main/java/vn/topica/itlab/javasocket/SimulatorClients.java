package vn.topica.itlab.javasocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 

//Unit 2.5: Simulator some Client send requests to Server
public class SimulatorClients {
	public static void main(String[] args) throws Exception {
		try {
			System.out.println("The state machine client is running...");
			ExecutorService pool = Executors.newFixedThreadPool(3);
			String[] file = {"message.txt","message1.txt","message2.txt"};
			for (int i = 0; i < 3; i++) {
				pool.execute(new StateMachineClient(file[i],i));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}	
}
