package wumpusenv;

/**
 * ErrorDetails shows the details of an Exception. Normal printStackTrace() does
 * NOT do a job, nothing appears on screen... For now we print on stdout, such
 * that application continues running without much fuss (wishful thinking ;-) )
 */
public class ErrorDetails {
	ErrorDetails(final Exception e, final String message) {
		System.out.println("EXCEPTION RAISED:" + message);
		System.out.println("-----details:-----");
		System.out.println(e.getMessage());
		System.out.println("stacktrace of the problem:");
		final StackTraceElement[] elts = e.getStackTrace();
		for (final StackTraceElement elt : elts) {
			System.out.println(elt);
		}
		System.out.println("----------");
	}
}