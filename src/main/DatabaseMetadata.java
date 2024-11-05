package main;

import java.util.ArrayList;

// Holds metadata for an specific
// database .txt file.
// No encapsulation since this is a simple data holder.
//
// The serialized Metadata format should be as follows:
// 1| <nEntries>, <nOrphanIDs>, <nElements>,
// 2| <[orphanIDs]>,
// 3| <[types]>,
//
// the [] inside a placeholder indicates that the 
// data should be placed as a comma-separated array

public class DatabaseMetadata {
	public int 
		nEntries, // Number of entries / entities
		nOrphanIDs; // Number of Available IDs to be re-used
	
	// Available IDs to be re-used
	public ArrayList<Integer> orphanIDs; 
	
	// Represents the amount of fields/values
	// in each entry
	public int nElements;
	
	// Represents the different fields of 
	// each entry, with their respective types
	public int[] types;
	
	// Default constructor, just make sure the 
	// attributes aren't null
	public DatabaseMetadata() {
		this.nEntries=0;
		this.nOrphanIDs=0;
		this.orphanIDs = new ArrayList<Integer>();
	}
}
