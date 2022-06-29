package jpiccoli.mt.sort;

/**
 * Unchecked Exception which signals that an error occurred during
 * a sorting operation.
 * 
 * @author Juliano Piccoli
 *
 */
public class SortingException extends RuntimeException {
	
	public SortingException(final Throwable t) {
		super(t);
	}

}
