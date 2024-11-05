package main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

// Database Management System Class
// Template value "T" refers to the specific datatype used by 
// the database.
public class DatabaseMS<T> {

	// Paths to the data and metadata .txt files
	private String dbFilePath, metaFilePath;
	
	// Set this variable to true to print the 
	// loaded/written data on screen after each 
	// operation
	private boolean verbose = false;
	
	// Represents the success/failure status
	// of the last operation performed
	private int lastOperationStatus = 0;
	
	//############### ERROR CODES ###############//
	public static final int OPERATIONSUCCESS = 0;
	public static final int NONEXISTANTID = 1;
	public static final int FILEREADINGERROR = 2;
	public static final int FILEWRITINGERROR = 3;
	public static final int EMPTYLOADEDFILE = 4;
	public static final int INVALIDDATAFORMAT = 5;
	public static final int INVALIDMETADATAFILE = 6;
	public static final int INVALIDMETADATAFORMAT = 7;
	public static final int INVALIDSEARCHCRITERIA = 8;
	
	public DatabaseMS(String dbFilePath, String metaFilePath) {
		this.dbFilePath=dbFilePath;
		this.metaFilePath=metaFilePath;
	}

	//###################################################//
	//############### UTILITY FUNCTIONS #################//
	//###################################################//	
	
	// Use this to check that an AbstractArray as a correct format
	// (i.e.: Checking that the "types" array is filled with valid types)
	private boolean validateAbstractArray(AbstractArray arr, DatabaseMetadata dbMetadata) {
		
		// Check that the number of elements are the same
		boolean valid = (dbMetadata.nElements == arr.nElements);
		
		// Check that the element types are the same
		for(int t=0; t<dbMetadata.nElements ; t++) {
		
			// If the current element is null, return false
			if(null == arr.elements[t]) {
				valid = false;
				break;
			}
			
			// If any type is different, return false
			valid &= (dbMetadata.types[t] == arr.types[t]);
		}
		
		return valid;
	}
	
	//###################################################//
	//################ CORE OPERATIONS ##################//
	//############# (get/add/set/remove) ################//
	//###################################################//
	
	
	//---------- ABSTRACT CORE OPERATIONS ------------//
	// Manipulate AbstractArrays instead of classes.  //
	// Use the real index of the entry in the Array   //
	// instead of a search criteria inherent to the   //
	// entry.                                         //
	//------------------------------------------------//

	// Get an AbstractArray at real index "index"
	// Returns null if the index is invalid
	public AbstractArray getAbstractEntry(int index) {
		
		// Set to success by default
		lastOperationStatus = OPERATIONSUCCESS;
		
		AbstractArray getResult = null;
		
		// First deserialize both data and metadata files
		ArrayList<String> dataStrArr = deserializeFile(dbFilePath);
		ArrayList<String> metadataStrArr = deserializeFile(metaFilePath);
		
		// Then parse the string arrays
		DatabaseMetadata dbMetadata = parseMetadataFromStrArr(metadataStrArr);
		ArrayList<AbstractArray> entries = parseDataFromStrArr(dataStrArr, dbMetadata);
		
		// Avoid an out-of-bounds index
		if(entries.size() > index) {
			getResult = entries.get(index);
		} else {
			lastOperationStatus = NONEXISTANTID;
		}
		
		return getResult;
	}
	
	// Appends an AbstractArray "newObj" to the database
	public void addAbstractEntry(AbstractArray newObj) {

		// Set to success by default
		lastOperationStatus = OPERATIONSUCCESS;
		
		// Then parse the string arrays
		//ArrayList<String> dataStrArr = deserializeFile(dbFilePath);
		//ArrayList<String> metadataStrArr = deserializeFile(metaFilePath);
				
		// First deserialize both data and metadata files
		DatabaseMetadata dbMetadata = parseMetadataFromStrArr(deserializeFile(metaFilePath));
		ArrayList<AbstractArray> entries = parseDataFromStrArr(deserializeFile(dbFilePath), dbMetadata);
		
		// Check the format of "newObj"
		boolean correctFormat = validateAbstractArray(newObj, dbMetadata);
		
		if(correctFormat) {
			entries.add(newObj);
			
			if(verbose) {				
				System.out.print("\nNew set of entries:\n");
				System.out.print("---------------------\n");
				for(AbstractArray e : entries) {
					e.printElements();
				}
				System.out.print("---------------------\n\n");
			}
			
			dbMetadata.nEntries++;
			
			// Serialize <data>.txt
			serializeFile(this.dbFilePath, unparseDataToStrArr(entries));
			
			// Serialize <metadata.txt>
			serializeFile(this.metaFilePath, unparseMetadataToStrArr(dbMetadata));
			
		} else {
			// Error
			lastOperationStatus = INVALIDDATAFORMAT;
		}
	}
	
	// Sets an AbstractArray "newObj" at real index "index"
	public void setAbstractEntry(int index, AbstractArray newObj) {
		
		// Set to success by default
		lastOperationStatus = OPERATIONSUCCESS;
		
		// First, load the metadata 
		DatabaseMetadata dbMetadata = parseMetadataFromStrArr(deserializeFile(this.metaFilePath));
		
		// Check that the index is valid
		if(index >= dbMetadata.nElements) {
			lastOperationStatus = NONEXISTANTID;
			return;
		}
		
		// Check the correct format of "newObj"
		boolean correctFormat = validateAbstractArray(newObj, dbMetadata);
		if(!correctFormat) {
			lastOperationStatus = INVALIDMETADATAFORMAT;
			return;
		}
		
		// If everything is valid, proceed to load the actual data
		ArrayList<AbstractArray> absArr = parseDataFromStrArr(deserializeFile(this.dbFilePath), dbMetadata);
	
		// Modify the entry
		absArr.set(index, newObj);
		
		// Serialize the modified array metadata
		serializeFile(metaFilePath, unparseMetadataToStrArr(dbMetadata));
	
		// Serialize the modified array data
		serializeFile(dbFilePath, unparseDataToStrArr(absArr));
	}

	// Removes an AbstractArray at real index "index"
	public void removeAbstractEntry(int index) {
		
		// Set to success by default
		lastOperationStatus = OPERATIONSUCCESS;		
	
		// First, load the metadata 
		DatabaseMetadata dbMetadata = parseMetadataFromStrArr(deserializeFile(this.metaFilePath));
				
		// Check that the index is valid
		if(index >= dbMetadata.nEntries) {
			lastOperationStatus = NONEXISTANTID;
			return;
		}
		
		// If everything is valid, proceed to load the actual data
		ArrayList<AbstractArray> absArr = parseDataFromStrArr(deserializeFile(this.dbFilePath), dbMetadata);
			
		// Modify the entry
		absArr.remove(index);
		
		// Update metadata
		dbMetadata.nEntries--;
		
		// Serialize the modified array metadata
		serializeFile(metaFilePath, unparseMetadataToStrArr(dbMetadata));
	
		// Serialize the modified array data
		serializeFile(dbFilePath, unparseDataToStrArr(absArr));
	}
	
	//---------- SPECIFIC CORE OPERATIONS -------------//
	// Manipulate specific classes stored by the       //
	// specific database.  						       //
	// Use the real a search criteria inherent to the  //
	// entry (i.e.: "objectID", "username", etc).      //
	// These need to be overriden in the child classes.//
	//-------------------------------------------------//
	
	public T getData(int index) {return null;}
	
	public void addData(T newEntry) {}
	
	public void setData(int index, T newEntry) {}
	
	public void removeData(int index) {}
	
	//###################################################//
	//############# (UN)PARSE FUNCTIONS #################//
	//###################################################//
	
	// Parses entries from a deserialized String Array into an Abstract array
	private ArrayList<AbstractArray> parseDataFromStrArr(ArrayList<String> strArr, DatabaseMetadata dbMetaRef) {
		
		// Instantiate the output 
		ArrayList<AbstractArray> output = new ArrayList<AbstractArray>();
		
		// Deep copy the types array from metadata
		int[] typesCopy = new int[dbMetaRef.types.length];
		System.arraycopy(
				dbMetaRef.types, 
				0, typesCopy, 
				0, 
				dbMetaRef.types.length);
		
		StringBuffer strBuffer = new StringBuffer();
		char separator = ',';

		// String iteration
		for(int s=0; s<strArr.size(); s++) {
			
			String iStr = strArr.get(s);

			// Avoid null/empty strings
			if(null==iStr) continue;
			if(0 >= iStr.length()) continue;
			
			// Instantiate the current AbstractArray
			AbstractArray iAbsArr = new AbstractArray(dbMetaRef.nElements);
			
			// Get the formats from the metadata
			int[] format = dbMetaRef.types;
			
			// Char iteration
			int valueIndex = 0;
			for(int c=0; c<iStr.length(); c++) {
				
				char cchar = iStr.charAt(c);
				
				// Proceed to parse the current String
				if((separator == cchar) && (valueIndex < dbMetaRef.nElements)) {
					
					// Trim the string to avoid Number 
					// conversion anomalies
					String trimmedBuffer = strBuffer.toString().trim();
										
					if(format.length < dbMetaRef.nElements) {
						//Error
						lastOperationStatus = INVALIDMETADATAFORMAT;
					}
					else {
						// Check each single value type, and parse 
						// from String into the respective type
						switch(format[valueIndex]) {
						case AbstractArray.ATR_INT: {
							iAbsArr.elements[valueIndex] = 
									Integer.parseInt(trimmedBuffer);
						}
							break;
						case AbstractArray.ATR_DBL: {
							iAbsArr.elements[valueIndex] =
									Double.parseDouble(trimmedBuffer);
						}
							break;
						case AbstractArray.ATR_CHR: {
							iAbsArr.elements[valueIndex] =
									// Takes only the first char
									trimmedBuffer.charAt(0);
						}
							break;
						case AbstractArray.ATR_STR: {
							iAbsArr.elements[valueIndex] = 
									// Only deep copy
									new String(trimmedBuffer);
						}
							break;
						case AbstractArray.ATR_FLT: {
							iAbsArr.elements[valueIndex] = Float.parseFloat(trimmedBuffer);
						}
							break;
						case AbstractArray.ATR_BOL: {						
							iAbsArr.elements[valueIndex] = Boolean.parseBoolean(trimmedBuffer);
						}
							break;
						default:
							break;
						}
						
						strBuffer.delete(0, strBuffer.length()); // Reset buffer
						valueIndex++; // go to next attribute
					}
				} else {
					strBuffer.append(cchar); // Update buffer
				}
			}
			
			// Push the parse entry to the output array
			output.add(iAbsArr);
		}
		
		return output;
	}
	
	// Converts an array of BankAccounts back to a String array
	private ArrayList<String> unparseDataToStrArr(ArrayList<AbstractArray> objArr) {
		ArrayList<String> outArr = new ArrayList<String>();
		
		if(null == objArr) {
			lastOperationStatus = INVALIDDATAFORMAT;
			return null;
		}
		
		// Entry Iteration
		for(int i=0; i<objArr.size(); i++) {
			AbstractArray iobj = objArr.get(i);
			
			StringBuffer strBuffer = new StringBuffer();
			
			// Stringify values of the entry
			for(int j=0; j<iobj.nElements; j++) {
				if(null != iobj.elements[j])
					strBuffer.append(
							iobj.elements[j].toString() + ", ");
			}
			
			// Append strigified entry
			outArr.add(strBuffer.toString());

			// Reset Buffer
			if(!strBuffer.isEmpty()) 
				strBuffer.delete(0, strBuffer.length());
		}
		
		return outArr;
	}
	
	// Parses the DatabaseMetadata from an array of Strings
	private DatabaseMetadata parseMetadataFromStrArr(ArrayList<String> strArr) {
		
		// Set success by default
		lastOperationStatus = OPERATIONSUCCESS;
		
		// Make sure the strArr is non null and valid in size
		// Metadata.txt should have at least 3 lines
		if(strArr == null || strArr.size() < 3) {
			lastOperationStatus = INVALIDMETADATAFILE;
			return null;
		}
		
		DatabaseMetadata dbMeta = new DatabaseMetadata();
		
		// Use this as a temporary buffer
		StringBuffer strBuffer = new StringBuffer();
		
		// Use this for knowing which value to read
		int valueCounter = 0;

		// Get and validate the first line
		String firstLine = strArr.get(0);
		if(null == firstLine) {
			lastOperationStatus = INVALIDMETADATAFILE;
			return null;
		} 
		if(0==firstLine.length()) {
			lastOperationStatus = INVALIDMETADATAFILE;			
			return null;
		}
		
		// Get nEntries and nOrphanIDs from first line
		for(int i = 0; i < firstLine.length(); i++) {
			
			// Comma detected, means new value
			if(','==firstLine.charAt(i)) {
				
				if(0==valueCounter) {
					// Parse nEntries
					dbMeta.nEntries = Integer.parseInt(strBuffer.toString().trim());
					valueCounter++;
				} else if(1==valueCounter) {
					// Parse nOrphanIDs
					dbMeta.nOrphanIDs = Integer.parseInt(strBuffer.toString().trim());
					valueCounter++;
				} else if(2==valueCounter){
					// Parse nElements
					dbMeta.nElements = Integer.parseInt(strBuffer.toString().trim());
				} else {
					break;
				}
				
				if(!strBuffer.isEmpty())
					strBuffer.delete(0, strBuffer.length()); // Clear the buffer
			
			} else {
				strBuffer.append(firstLine.charAt(i)); // update the buffer
			}
		}
		
		// Check that at least 3 values were parsed
		if(valueCounter < 2) {
			lastOperationStatus = INVALIDMETADATAFORMAT;
			return null;
		}
		
		// Check that nElements is at least 1
		if(dbMeta.nElements <= 0) {
			lastOperationStatus = INVALIDMETADATAFORMAT;
			return null;
		}
		
		// reset these for the next line
		//valueCounter = 0;
		if(!strBuffer.isEmpty())
			strBuffer.delete(0, strBuffer.length());
		
		// Get and validate the second line
		String secondLine = strArr.get(1);
		if(null == secondLine) {
			lastOperationStatus = INVALIDMETADATAFILE;
			return null;
		} 
		
		// Get orphanIDs from the second line
		if(0!=secondLine.length()) {			
			for(int i=0; i<secondLine.length() ;i++) {
				
				// Comma detected, means new value
				if(','==secondLine.charAt(i)) {
								
					// Parse and add the ID
					dbMeta.orphanIDs.add(
						Integer.parseInt(strBuffer.toString().trim()));
					
					if(!strBuffer.isEmpty())
						strBuffer.delete(0, strBuffer.length()); // Clear the buffer
							
				} else {
					strBuffer.append(secondLine.charAt(i)); // Update the buffer
				}
			}
		}
		
		// Validate the orphanIDs array
		if(dbMeta.orphanIDs.size() != dbMeta.nOrphanIDs) {
			lastOperationStatus = INVALIDMETADATAFORMAT;
			return null;
		}

		// reset these for the next line
		if(!strBuffer.isEmpty())
			strBuffer.delete(0, strBuffer.length());
		
		// Get and validate the third line
		String thirdLine = strArr.get(2);
		if(null == thirdLine) {
			lastOperationStatus = INVALIDMETADATAFILE;
			return null;
		}
		
		// Represents the current type being parsed
		int typeCounter = 0;
		
		// Make sure to initialize the types array in metadata
		dbMeta.types = new int[dbMeta.nElements];
		
		// Get types from the third line
		for(int i=0; i<thirdLine.length(); i++) {
			if(',' == thirdLine.charAt(i)) {
				
				String bufferContent = strBuffer.toString().trim();
				
				if(bufferContent.contentEquals(AbstractArray.STRATR_INT)) 
				{
					dbMeta.types[typeCounter] = AbstractArray.ATR_INT;
				} 
				else if(bufferContent.contentEquals(AbstractArray.STRATR_STR))
				{
					dbMeta.types[typeCounter] = AbstractArray.ATR_STR;					
				}
				else if(bufferContent.contentEquals(AbstractArray.STRATR_DBL))
				{
					dbMeta.types[typeCounter] = AbstractArray.ATR_DBL;
				}
				else if(bufferContent.contentEquals(AbstractArray.STRATR_FLT))
				{
					dbMeta.types[typeCounter] = AbstractArray.ATR_FLT;
				}
				else if(bufferContent.contentEquals(AbstractArray.STRATR_CHR))
				{
					dbMeta.types[typeCounter] = AbstractArray.ATR_CHR;
				}
				else if(bufferContent.contentEquals(AbstractArray.STRATR_BOL))
				{
					dbMeta.types[typeCounter] = AbstractArray.ATR_BOL;
				}
				else {
					// INVALID TYPE CODE, HANDLE THIS
				}
				
				// Go to next element's type
				typeCounter++;
				
				// Clear the buffer				
				if(!strBuffer.isEmpty())
					strBuffer.delete(0, strBuffer.length()); 
				
			} else {
				// Update buffer
				strBuffer.append(thirdLine.charAt(i));
			}
		}
		
		return dbMeta;
	}
	
	// Converts a DatabaseMetadata object back to a String array
	private ArrayList<String> unparseMetadataToStrArr(DatabaseMetadata metadata) {
		
		if(null==metadata) {
			return null;
		}
		
		ArrayList<String> outArr = new ArrayList<String>();
		
		// Unparse first line (nEntries, nOrphanIDs, nElements)
		outArr.add(
				metadata.nEntries + ", " +
				metadata.nOrphanIDs + ", " +
				metadata.nElements + ", "
				); 
		
		// Unparse second line (orphanIDs)
		StringBuffer secondLine = new StringBuffer();
		for(int o : metadata.orphanIDs) {
			secondLine.append(o + ", ");
		}
		
		outArr.add(secondLine.toString());
		
		// Unparse third line (types)
		StringBuffer thirdLine = new StringBuffer();
		
		// Loop for each element type
		for(int t=0; t<metadata.types.length; t++) {
			switch(metadata.types[t]) {
			case AbstractArray.ATR_INT: {
				thirdLine.append(
						AbstractArray.STRATR_INT + ", ");
			}
			break;
			case AbstractArray.ATR_STR: {
				thirdLine.append(
						AbstractArray.STRATR_STR + ", ");			
			}
			break;
			case AbstractArray.ATR_DBL: {
				thirdLine.append(
						AbstractArray.STRATR_DBL + ", ");
			}
			break;
			case AbstractArray.ATR_FLT: {
				thirdLine.append(
						AbstractArray.STRATR_FLT + ", ");
			}
			break;
			case AbstractArray.ATR_CHR: {
				thirdLine.append(
						AbstractArray.STRATR_CHR + ", ");
			}
			break;
			case AbstractArray.ATR_BOL: {
				thirdLine.append(
						AbstractArray.STRATR_BOL + ", ");
			}
			break;
			default: {
				// INVALID TYPE, ERROR
				break;
			}
			}
			
		}
		outArr.add(thirdLine.toString());
		
		return outArr;
	}
	
	//###################################################//
	//############ SERIALIZE/DESERIALIZE ################//
	//###################################################//
	
	// Deserializes a .txt file into an Array of Strings
	private ArrayList<String> deserializeFile(String filePath) {
		
		ArrayList<String> strArr = new ArrayList<String>();
		String fileStr = null;
		
		try {
			fileStr = Files.readString(Path.of(filePath));
		} catch (IOException e) {
			e.printStackTrace();
			lastOperationStatus = FILEREADINGERROR;
			return null;
		}
		
		// Empty string handling
		if(null==fileStr || 0==fileStr.length()) {
			lastOperationStatus = EMPTYLOADEDFILE;
			return null;
		}
		
		// Split String into lines
		StringBuffer linebuff = new StringBuffer();
		for(int i=0; i<fileStr.length();i++) {
			
			char ichar = fileStr.charAt(i);
			
			if('\n'==ichar) {
				strArr.add(linebuff.toString()); // Push the string to the string array
				linebuff.delete(0, linebuff.length()); // clear the StringBuffer
			} else {
				linebuff.append(ichar); // Add the char to the buffer
			}
		}
		
		if(verbose) {			
			System.out.print("Loaded String Array:\n");
			System.out.print("------------------------\n");
			for(int i=0; i<strArr.size(); i++) {
				System.out.print(strArr.get(i)+"\n");
			}
			System.out.println("------------------------\n");
		}
		
		return strArr;
	}
	
	// Serializes a .txt file from an Array of Strings
	private void serializeFile(String filePath, ArrayList<String> strArr) {
		
		StringBuffer fileStrBuffer = new StringBuffer();

		// Concatenate all the strings first
		for(int s=0; s<strArr.size(); s++) {
			fileStrBuffer.append(strArr.get(s)+"\n");
		}
		
		if(verbose) {			
			System.out.print("Concatenated String array:\n");
			System.out.print("--------------------------\n");
			System.out.print(fileStrBuffer.toString() + "\n");
			System.out.print("--------------------------\n");
		}
		
		// Write the whole concatenated String to the file
		try {
			Files.writeString(
					Paths.get(filePath), 
					fileStrBuffer.toString(), 
					StandardOpenOption.TRUNCATE_EXISTING);
			
		} catch (IOException e) {
			e.printStackTrace();
			lastOperationStatus = FILEWRITINGERROR;
			return;
		}			
	}

	//###################################################//
	//############ GETTER/SETTER FUNCTIONS ##############//
	//###################################################//
	
	public int getLastOperationStatus() {
		return this.lastOperationStatus;
	}
	
	public String getLastOperationStatusStr() {
		String output = "";
		
		switch(this.lastOperationStatus) {
		case OPERATIONSUCCESS:
			output= "OPERATION SUCCESS";
			break;
		case NONEXISTANTID: 
			output= "NON EXISTANT ID";
			break;
		case FILEREADINGERROR:
			output= "FILE READING ERROR";
			break;
		case FILEWRITINGERROR:
			output= "FILE WRITING ERROR";
			break;
		case EMPTYLOADEDFILE:
			output= "EMPTY LOADED FILE";
			break;
		case INVALIDDATAFORMAT:
			output= "INVALID DATA FORMAT";
			break;
		case INVALIDMETADATAFILE:
			output= "INVALID METADATA FILE";
			break;
		case INVALIDMETADATAFORMAT:
			output= "INVALID METADATA FORMAT";
			break;
		case INVALIDSEARCHCRITERIA:
			output = "INVALID SEARCH CRITERIA";
			break;
		default:
			output="!UNKNOWNERROR!";
			break;
		}
		
		return output;
	}	
	
	public void setVerbose(boolean verbose) {
		this.verbose=verbose;
	}
	
}
