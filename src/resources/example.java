public class example {
	    public static void main(String[] args) {
	        // Test true and false booleans.
	    	String c = "a";
	    	String b = "b";
	        boolean value = true;
	        if (true) {
	            System.out.println("A");
	        }
	        value = false;
	        if ("a" == "n") {
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
	        catch(Throwable e)
	        {
	        	System.out.println("koi vi exception");
	        	System.exit(1);
	        }
	    }
	    //unUsed Method Test
	    public void unUsed()
	    {
	    	System.out.println("This method will never be called");
	    }
	    public void equals(){
	    	System.out.println("hello");
	    }
	
	    @Override
	    public int hashCode(){
	    	return 1;
	    }

}

