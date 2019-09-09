package vn.topica.itlab.javasocket;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ArrayListUser extends ArrayList<UserInformation>{
	@Override
	public synchronized boolean add(UserInformation e) {
		// TODO Auto-generated method stub
		return super.add(e);
	}
};
//Main server manage a lot of thread, every thread is a socket connect to a client 
public class StateMachineServer {
	//arrayListUser store information of user
	public static ArrayListUser arrayListUser = new ArrayListUser();
	public static void main(String[] args) throws Exception {
		try (ServerSocket listener = new ServerSocket(9669)) {
			System.out.println("The state machine server is running...");
			ExecutorService pool = Executors.newFixedThreadPool(5);
			while (true) {
				pool.execute(new StateMachine(listener.accept(),arrayListUser));
				
			}
		}
	}	
}