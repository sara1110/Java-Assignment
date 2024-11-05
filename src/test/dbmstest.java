package test;

import main.*;

public class dbmstest {

	public static void main(String args[]) {
		DatabaseMS dbms = new DatabaseMS(
				"./data/data.txt",
				"./data/metadata.txt");
		
		dbms.setVerbose(true);
		
		// Try getting the first real entry
		//AbstractArray absArr = dbms.getAbstractEntry(0);
		//System.out.println("");
		//if(null!=absArr) absArr.printElements();
		
		// Try adding an AbstractArray
		/*
		int types[] = new int[] {		
				AbstractArray.ATR_INT, 
				AbstractArray.ATR_STR,
				AbstractArray.ATR_DBL,
				AbstractArray.ATR_BOL};
		
		AbstractArray addTest = new AbstractArray(4);
		
		addTest.types = types;
		addTest.nElements=4;
		addTest.elements = new Object[] {
				30, "Hello world", 7.9, false
		};
		
		dbms.addAbstractEntry(addTest);
		
		System.out.println("ADD Status = " + dbms.getLastOperationStatusStr());
		*/
		
		// Try setting (editing) an entry in the DB  
		/*
		int types[] = new int[] {		
				AbstractArray.ATR_INT, 
				AbstractArray.ATR_STR,
				AbstractArray.ATR_DBL,
				AbstractArray.ATR_BOL};
		
		AbstractArray setTest = new AbstractArray(types);
		
		setTest.nElements=4;
		setTest.elements = new Object[] {
				70, "Goodbye world", 3.14, true
		};
		
		dbms.setAbstractEntry(1, setTest);
	
		System.out.println("SET Status = " + dbms.getLastOperationStatusStr());
		*/
		
		// Try removing the third entry of the DB
		dbms.removeAbstractEntry(2);
		
		System.out.println("REMOVE Status = " + dbms.getLastOperationStatusStr());
	}
}
