import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class testClass
{
	public void fun() throws FileNotFoundException 
	{
		File f = new File("");
		FileReader fr = new FileReader(f);
		
		if(true) 
		{System.out.println("TRUE");}
		
		int i = 5;
		int j = 2;
		while(j < i) {
			int k = sum(i, j);
			j++;
		}
	}
	
	public int sum(int i, int j) {
		return (i+j);
	}
}
