package main;

// Represents an entry/entity in a Database
// An "abstract array" of objects
public class AbstractArray {
	
	//########## Static Datatype codes ##########//
	public static final int 
		ATR_INT = 0, // Integer
		ATR_STR = 1, // String
		ATR_DBL = 2, // Double
		ATR_FLT = 3, // Float
		ATR_CHR = 4, // Char
		ATR_BOL = 5; // Boolean
	
	//######## Static Datatype String constants ########//
	public static final String 
		STRATR_INT = "ATR_INT", 
		STRATR_STR = "ATR_STR",
		STRATR_DBL = "ATR_DBL",
		STRATR_FLT = "ATR_FLT", 
		STRATR_CHR = "ATR_CHR",
		STRATR_BOL = "ATR_BOL";
	
	//## Dynamic Attributes ##//
	public Object[] elements;
	public int[] types; 
	public int nElements;
	
	public AbstractArray(int nElements) {
		this.nElements=nElements;
	
		if(nElements>0) {
			this.elements = new Object[nElements];
			this.types = new int[nElements];
		}
	}
	
	public AbstractArray(int[] types) {
		int nElements = types.length;
		
		if(0!=nElements) {
			this.types = types;
			elements = new Object[nElements];
		}
	}
	
	public void printElements() {
		System.out.print("{ ");
		for(Object e : elements) {
			System.out.print(e + ", ");
		}
		System.out.print(" }\n");
	}
}


