import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;
//https://orajavasolutions.wordpress.com/tag/sleeping-barber-problem/

public class Hotel {
	protected static int roomNum = 1;
	
	protected static Semaphore waitForEmp = new Semaphore(2, true);
	protected static Semaphore waitForBellhop = new Semaphore(2, true);
	protected static Semaphore guestReady = new Semaphore(0, true);
	protected static Semaphore keysReady = new Semaphore(0, true);
	
	protected static Semaphore checkinQSema = new Semaphore(1, true);
	protected static Semaphore bellhopQSema = new Semaphore(1, true);
	protected static Semaphore roomIncrement = new Semaphore(1, true);
	
	protected static Queue<Guest> checkinQ = new LinkedList<Guest>();
	protected static Queue<Guest> bellhopQ = new LinkedList<Guest>();
	
	private void runHotel() throws InterruptedException {
		Employee empArr[] = new Employee[2];
		Thread empThreads[] = new Thread[2];
		Bellhop bellArr[] = new Bellhop[2];
		Thread bellThreads[] = new Thread[2];
		Guest guestArr[] = new Guest[10];		// Change these to 25 before turn in
	    Thread guestThreads[] = new Thread[10];
	    
	    // Create 2 front desk workers
	    for(int i = 0; i < empArr.length; i++){
	    	empArr[i] = new Employee(i);
	    	empThreads[i] = new Thread(empArr[i]);
	    	empThreads[i].start();
	    }
	    
	    // Create 2 Bellhops
	    for(int j = 0; j < bellArr.length; j++){
	    	bellArr[j] = new Bellhop(j);
	    	bellThreads[j] = new Thread(bellArr[j]);
	    	bellThreads[j].start();
	    }
	    
	    // Create 25 Guests
	    for(int k = 0; k < guestArr.length; k++ ) {
	    	guestArr[k] = new Guest(k);
	        guestThreads[k] = new Thread( guestArr[k] );
	        guestThreads[k].start();
	    }
	    
	    // Join the 25 guest threads back into the main class
	    for(int l = 0; l < guestArr.length; l++){
	    	guestThreads[l].join();
	    	System.out.println("Guest " + l + " has joined");
	    }
	}	// End of runHotel function
	
	public static void main(String[] args) throws InterruptedException {
		Hotel hotel = new Hotel();
		
		System.out.println("Simulation starts");
		
		hotel.runHotel();
		
	    System.out.println("Simulation ends");
	    System.exit(0);
	}
}



	class Employee implements Runnable {
	private int empNum;
	private Guest helping;
	Employee(int num) {
		this.empNum = num;
	}	// End of constructor
	
	public void run() {
		System.out.println("Front desk employee " + empNum + " created");
		while(true){
			try
			{
//				Thread.sleep(1000);
				Hotel.guestReady.acquire();
				
				Hotel.checkinQSema.acquire();
				helping = Hotel.checkinQ.remove();
				Hotel.checkinQSema.release();
				
				Hotel.roomIncrement.acquire();
				System.out.println("Front desk employee " + empNum + " registers guest " + helping.getNum() + " and assigns room " + Hotel.roomNum);
				helping.setRoomNum(Hotel.roomNum);
				
				
				
				Hotel.roomNum++;
				Hotel.roomIncrement.release();
				Hotel.waitForEmp.release();
				
				
			}
			catch (InterruptedException e)
			{
			}
		}	// End of while loop
	}	// End of run function
}	// End of Employee class

class Bellhop implements Runnable {
	private int bellNum;
	Bellhop (int num) {
		this.bellNum = num;
	}	// End of constructor
	
	public void run() {
		System.out.println("Bellhop " + bellNum + " created");
//		try {
//			Thread.sleep(1000);
//		} 
//		catch (InterruptedException e) {
//		}
	}	// End of run function
}	// End of Bellhop class

class Guest implements Runnable {
	private int guestNum;
	private int numBags;
	private int roomNum;
	
	Guest(int num){
		this.guestNum = num;
	}
	
	public void run() {
	   System.out.println( "Guest " + guestNum + " created" );
	   try
	   {
//		  Thread.sleep(1000); 
	      Hotel.waitForEmp.acquire();
	      
	      Hotel.checkinQSema.acquire();
	      Hotel.checkinQ.add(this);
	      Hotel.checkinQSema.release();
	      
	      randBags();
		   if(numBags == 1)
			   System.out.println( "Guest " + guestNum + " enters hotel with " + numBags + " bag" );
		   else
			   System.out.println( "Guest " + guestNum + " enters hotel with " + numBags + " bags" );
//		   Thread.sleep(1000);
		   
		   Hotel.guestReady.release();
		   
	   }
	   catch (InterruptedException e)
	   {
	   }	   
	}	// End of run function
	
//	public void post() {
//	   Hotel.s1.release();
//	}	// End of post function
	
	public int getNum(){
		return guestNum;
	}	// End of getNum function
	
	public void setRoomNum(int num){
		this.roomNum = num;
	}	// End of setRoomNum function
	
	public int getRoomNum(){
		return this.roomNum;
	}	// End of getRoomNum function
	
	private void randBags(){
		Random randomNum = new Random();
    	this.numBags = randomNum.nextInt(6);
	}	// End of randBags function
}	// End of Employee Class

