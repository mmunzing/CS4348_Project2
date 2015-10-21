import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;
//https://orajavasolutions.wordpress.com/tag/sleeping-barber-problem/

public class Hotel {
	Employee empArr[] = new Employee[2];
	Thread empThreads[] = new Thread[2];
	Bellhop bellArr[] = new Bellhop[2];
	Thread bellThreads[] = new Thread[2];
	protected static Guest guestArr[] = new Guest[10];		// Change these to 25 before turn in
    private Thread guestThreads[] = new Thread[10];
    
	protected static int roomNum = 1;
	
	protected static Semaphore empAvailable = new Semaphore(2, true);
	protected static Semaphore bellhopAvailable = new Semaphore(2, true);
	protected static Semaphore waitForBellhop = new Semaphore(0, true);
	protected static Semaphore guestReady = new Semaphore(0, true);
	protected static Semaphore giveKeys = new Semaphore(0, true);
	protected static Semaphore bagsReady = new Semaphore(0 , true);
	protected static Semaphore bagsGiven = new Semaphore(0 , true);
	protected static Semaphore hop0Deliver = new Semaphore(0, true);
	protected static Semaphore hop0BagsGiven = new Semaphore(0, true);
	protected static Semaphore hop1Deliver = new Semaphore(0, true);
	protected static Semaphore hop1BagsGiven = new Semaphore(0, true);
	protected static Semaphore tipHop = new Semaphore(0, true);
	
	protected static Semaphore checkinQSema = new Semaphore(1, true);
	protected static Semaphore bellhopQSema = new Semaphore(1, true);
	protected static Semaphore roomIncrement = new Semaphore(1, true);
	
	protected static Queue<Guest> checkinQ = new LinkedList<Guest>();
	protected static Queue<Guest> bellhopQ = new LinkedList<Guest>();
	
	private void runHotel() throws InterruptedException {
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
	private int empNum, guestNum;
	//private Guest helping;
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
				guestNum = Hotel.checkinQ.remove().getNum();
				Hotel.roomIncrement.acquire();
				System.out.println("Front desk employee " + empNum + " registers guest " 
						+ guestNum + " and assigns room " + Hotel.roomNum);
				
				Hotel.guestArr[guestNum].setRoomNum(Hotel.roomNum, empNum);
				Hotel.roomNum++;
				Hotel.checkinQSema.release();
				
				Hotel.giveKeys.release();
				Hotel.empAvailable.release();
			}
			catch (InterruptedException e)
			{
			}
		}	// End of while loop
	}	// End of run function
}	// End of Employee class

class Bellhop implements Runnable {
	private int bellNum, guestNum;
	
	Bellhop (int num) {
		this.bellNum = num;
	}	// End of constructor
	
	public void run() {
		System.out.println("Bellhop " + bellNum + " created");
		while(true){
			try {
//				Thread.sleep(1000);
				Hotel.bagsReady.acquire();
				
				Hotel.bellhopQSema.acquire();
				guestNum = Hotel.bellhopQ.remove().getNum();
				Hotel.guestArr[guestNum].setBellNum(bellNum);
				Hotel.bellhopQSema.release();
				
				System.out.println("Bellhop " + bellNum + " receives bags from guest " + guestNum);
				Hotel.bagsGiven.release();
				
				if(bellNum == 0){
					Hotel.hop0Deliver.acquire();
					System.out.println("Bellhop 0 delivers bags to guest " + guestNum);
					Hotel.hop0BagsGiven.release();
				}
				else {
					Hotel.hop1Deliver.acquire();
					System.out.println("Bellhop 1 delivers bags to guest " + guestNum);
					Hotel.hop1BagsGiven.release();
				}
				
				Hotel.tipHop.acquire();
				Hotel.bellhopAvailable.release();
			} 
			catch (InterruptedException e) {
			}
		}
	}	// End of run function
}	// End of Bellhop class

class Guest implements Runnable {
	private int guestNum, numBags, roomNum, bellNum,  empNum;
	
	Guest(int num){
		this.guestNum = num;
	}
	
	public void run() {
	   System.out.println( "Guest " + guestNum + " created" );
	   try
	   {
//		   Thread.sleep(1000);
		   randBags();
		   if(numBags == 1)
			   System.out.println( "Guest " + guestNum + " enters hotel with " + numBags + " bag" );
		   else
			   System.out.println( "Guest " + guestNum + " enters hotel with " + numBags + " bags" );
//		  Thread.sleep(1000); 
	      
		   Hotel.empAvailable.acquire();
		   
		   // Add guest to checkin queue using a mutex. 
		   Hotel.checkinQSema.acquire();
		   Hotel.checkinQ.add(this);
		   Hotel.checkinQSema.release();
		   
	       Hotel.guestReady.release();
	       Hotel.giveKeys.acquire();
	       
	       System.out.println("Guest " + guestNum + " receives room key for room " + roomNum 
	    		   + " from front desk employee " + empNum);
	       //Thread.sleep(1000);
	       
	       if(numBags > 2){
	    	   System.out.println("Guest " + guestNum + " requests help with bags");
	    	   Hotel.bellhopAvailable.acquire();
	    	   
	    	   Hotel.bellhopQSema.acquire();
	    	   Hotel.bellhopQ.add(this);
	    	   Hotel.bellhopQSema.release();
	    	   
	    	   Hotel.bagsReady.release();
	    	   Hotel.bagsGiven.acquire();
	    	   
	    	   System.out.println("Guest " + guestNum + " enters room " + roomNum);
	    	   
	    	   if(bellNum == 0){
	    		   Hotel.hop0Deliver.release();
	    		   Hotel.hop0BagsGiven.acquire();
	    		   System.out.println("Guest " + guestNum + " receives bags from bellhop 0 and gives tip");
	    	   }
	    	   else {
	    		   Hotel.hop1Deliver.release();
	    		   Hotel.hop1BagsGiven.acquire();
	    		   System.out.println("Guest " + guestNum + " receives bags from bellhop 1 and gives tip");
	    	   }
	    	   
	    	   Hotel.tipHop.release();
	    	   System.out.println("Guest " + guestNum + " has retired");
	       }
	       else {
	    	   System.out.println("Guest " + guestNum + " enters room " + roomNum);
	    	   System.out.println("Guest " + guestNum + " has retired");
	    	   
	       }
	       
	   }
	   catch (InterruptedException e)
	   {
	   }	   
	}	// End of run function
	
	public int getNum(){
		return guestNum;
	}	// End of getNum function
	
	public void setRoomNum(int roomNum, int empNum){
		this.roomNum = roomNum;
		this.empNum = empNum;
	}	// End of setRoomNum function
	
	public int getRoomNum(){
		return this.roomNum;
	}	// End of getRoomNum function
	
	public void setBellNum(int num){
		this.bellNum = num;
	}	// End of setBellNum function
	
	public int getBellNum(){
		return this.bellNum;
	}
	
	private void randBags(){
		Random randomNum = new Random();
    	this.numBags = randomNum.nextInt(6);
	}	// End of randBags function
}	// End of Employee Class

