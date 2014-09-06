package wumpusenv;

/**
ErrorDetails shows the details of an Exception.
Normal printStackTrace() does NOT do a job, nothing appears on screen...
For now we print on stdout, such that application continues running without much fuss
(wishful thinking ;-) )
@author W.Pasman
*/

public class ErrorDetails
{
	ErrorDetails(Exception e,String message)
	{
		System.out.println("EXCEPTION RAISED:"+message);
		System.out.println("-----details:-----");
		System.out.println(e.getMessage());
		System.out.println("stacktrace of the problem:");
		StackTraceElement[] elts=e.getStackTrace();
		for (int i=0; i<elts.length; i++) System.out.println(elts[i]);
		System.out.println("----------");
		
	}
}