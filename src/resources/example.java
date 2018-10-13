public class example {

	    public static void main(String[] args) {

	        // Test true and false booleans.
	        boolean value = true;
	        if (true) {
	            System.out.println("A");
	        }
	        value = false;
	        if (!value) {
	            System.out.println("B");
	        }
//	     	TODO Test        
	        Integer[] a = new Integer[5];
	        try {
	        	System.out.print(a[5]);
	        }
	        catch(ArrayIndexOutOfBoundsException e) {
	        	int i = 1;	
//	         	FIXME Test        
//	        	throw e;
//	        	System.out.println("Array Index out of bounds exception");
//	        	e.printStackTrace();
	        }
	    }
	    public void equals(){
	    	System.out.println("hello");
	    }
	

}
