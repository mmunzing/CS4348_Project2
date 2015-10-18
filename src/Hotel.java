import java.util.Random;
import java.util.concurrent.Semaphore;
//https://orajavasolutions.wordpress.com/tag/sleeping-barber-problem/

public class Hotel {
	
	
	public static void main(String[] args) {
		System.out.println("Simulation starts");
		Employee empArr[] = new Employee[2];
		Thread empThreads[] = new Thread[2];
		Bellhop bellArr[] = new Bellhop[2];
		Thread bellThreads[] = new Thread[2];
		Guest guestArr[] = new Guest[25];
	    Thread guestThreads[] = new Thread[25];
	    
	    for(int i = 0; i < 2; i++){
	    	empArr[i] = new Employee(i);
	    	empThreads[i] = new Thread(empArr[i]);
	    	empThreads[i].start();
	    }
	    
	    for(int j = 0; j < 2; j++){
	    	bellArr[j] = new Bellhop(j);
	    	bellThreads[j] = new Thread(bellArr[j]);
	    	bellThreads[j].start();
	    }
	    
	    for(int k = 0; k < 25; k++ ) 
	      {
	    	 guestArr[k] = new Guest(k);
	         guestThreads[k] = new Thread( guestArr[k] );
	         guestThreads[k].start();
	      }

	    System.out.println("Simulation ends");
	    System.exit(0);
	}

}

class Employee implements Runnable {
	private int empNum;
	Employee(int num) {
		this.empNum = num;
	}	// End of constructor
	
	public void run() {
		System.out.println("Front desk employee " + empNum + " created");
		try {
			Thread.sleep(1000);
		} 
		catch (InterruptedException e) {
		}
	}	// End of run function
	
	public void post() {
		System.out.println("EmpPost");
	}	// End of post function
}	// End of Employee class

class Bellhop implements Runnable {
	private int bellNum;
	Bellhop (int num) {
		this.bellNum = num;
	}	// End of constructor
	
	public void run() {
		System.out.println("Bellhop " + bellNum + " created");
		try {
			Thread.sleep(1000);
		} 
		catch (InterruptedException e) {
		}
	}	// End of run function
	
	public void post() {
		System.out.println("BellPost");
	}	// End of post function
}	// End of Bellhop class

class Guest implements Runnable {
	private int guestNum;
	private int numBags;
	protected static Semaphore waitForEmployee = new Semaphore(2, true );
	
	Guest(int num){
		this.guestNum = num;
	}
	
	public void run() {
	   System.out.println( "Guest " + guestNum + " created" );
	   try {
		   Thread.sleep(1000);
	   } 
	   catch (InterruptedException e) {
	   }
	   
	   
	   try
	   {
	      waitForEmployee.acquire();
	   }
	   catch (InterruptedException e)
	   {
	   }
	   
	   System.out.println( "Thread " + guestNum + " resuming" );
	}
	
	public static void post() {
	   waitForEmployee.release();
	}
	
	private int getNum(){
		return guestNum;
	}
	
	private int randBags(){
		Random randomNum = new Random();
    	return randomNum.nextInt(6);
	}
}
