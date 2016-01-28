package assembler;


//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
//import java.io.PrintStream;

/**
 * This assembler can read a text file in our assembly language
 * and convert the instructions into a .coe formated text filled with machine
 * code (1's and 0's).
 * We can then use copy that into our processor and run it.
 * The assembler can properly deal with labels, and jumping,
 * and it assumes that values in the text file are in hex or decimal;
 * '10' -> dec, '0x10'->hex.
 * 
 * Labels are converted to addresses in stage 2, after line number calculation
 * is complete; this allows jump instructions to have labels as the jump address.
 * The assembler does not interpret memory to memory instructions 
 * since we never used them, ie. instructions with 3 parameters.
 * 
 * The assembler can ignore comments too.
 * 
 * A version of this can be run on www.rose-hulman.edu/~hallamja/csse232 
 * in the 'Assembly' section,
 * 
 * or can be executed here, with the appropriate file path. (see Main)
 * 
 * The website does not support comments.
 * 
 * @author Armando Luja Salmeron
 *
 */
public class AssemblerMain {

	// the size of our immediate values in instructions
	private static final int IMM_SIZE = 11;
	
	public static void main(String[] args) throws FileNotFoundException {
		//BEGIN ASSEMBLY
		AssemblerMain assembler = new AssemblerMain();
		
		/*
		 * 1.
		 * Uncomment/switchto the code below if you are not using the .jar on website
		 * ie. passing a file from the project
		 */
//		begin: code for turning file into array of lines
//		Test comments with "CodeRelPrimeWithComments.txt"
		/*
		 * Edit the path below to the absolute path of "CodeRelPrimeWithComments.txt"
		 * to demo
		*/
		Scanner sc = new Scanner(new File("C:\\EclipseWorkspaces\\csse290\\Assembler2.0\\src\\assembler\\CodeRelPrimeWithComments.txt"));
		List<String> lines = new ArrayList<String>();
		while (sc.hasNextLine()) {
		  lines.add(sc.nextLine());
		}

		String[] arr = lines.toArray(new String[0]);
//		end 
		
		/*
		 * 2.
		 * change arr to args if you want to use .jar executable
		 */
		assembler.assemble(arr);
		
		
		/*
		 * 3.
		 * and uncomment this
		 */
		sc.close();
	}

		/**
		 * This is the main controller in the process.
		 * @param fileToAssemble
		 */
		public void assemble(String[] fileToAssemble){
			ArrayList<String> stage1 = convertAllNumToBinary(fileToAssemble);
//			for(int i = 0 ; i < stage1.size(); i ++){
//				System.out.println(stage1.get(i));
//			}
//			System.out.println("");
		
			ArrayList<String> stage2 = convertAllPseudoInstructions(stage1);
			for(int i = 0 ; i < stage2.size(); i ++){
				System.out.println(stage2.get(i));
			}
			
			System.out.println("");
			ArrayList<String> stage3 = convertToMachineCode(stage2);
			
			for(int i = 0 ; i < stage3.size(); i ++){
				System.out.println(stage3.get(i));
			}
	}
		
	
	/**
	 * Replaces hexadecimal values with binary values in the specified file
	 * The file must specify hex using the "0x" prefix for this to work
	 * @param filename
	 */
	public static ArrayList<String> convertAllNumToBinary(String[] file){
//		InputStream inStream = AssemblerMain.class.getResourceAsStream(filename);
//		Scanner input = new Scanner(inStream);
		ArrayList<String> arrayToRet = new ArrayList<String>();
//		while(input.hasNextLine()){
		for(int m = 0 ; m < file.length; m ++){
//			String[] line = input.nextLine().trim().split(" ");
			String[] line = file[m].trim().split("//")[0].split(" ");
			for(int i = 0 ; i < line.length; i ++){
				if(line[i].contains("0x")){//convert hex to binary
					String hexNum = line[i].substring(2,line[i].length());
					String binaryNum = Integer.toBinaryString(Integer.parseInt(hexNum, 16));
					while(binaryNum.length() < 16){//pad zeros
						binaryNum = "0" + binaryNum;
					}
					line[i] = binaryNum;
				}else if(i > 0){
					if(isInteger(line[i])){//convert dec to binary
						String binaryNum = Integer.toBinaryString(Integer.parseInt(line[i],10));
						while(binaryNum.length() < 16){//pad zeros
							binaryNum = "0" + binaryNum;
						}
						line[i] = binaryNum;
					}
				}
			}
			String write = "";
			for(int j = 0 ; j < line.length ; j++){
				write = write + " " + line[j];
			}
			write = write.trim();
//			System.out.println(write);
			arrayToRet.add(write);
		}
//		input.close();
		return arrayToRet;
	}
	
	/**
	 * Determines whether the string can be parsed to int
	 * @param string
	 * @return
	 */
	public static boolean isInteger(String string) {
	    try {
	        Integer.valueOf(string);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	
	/**
	 * Determines whether the string can be parsed to int
	 * @param string
	 * @return
	 */
	public static boolean isIntegerBin(String string) {
	    try {
	        Integer.valueOf(string,2);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	
	/**
	 * Reads through a file containing pseudo instructions and writes
	 * to a new file, replacing all pseudo instructions with 
	 * primitive instructions found in the Instructions.txt file
	 * @param filename
	 * @throws InterruptedException 
	 */
	public ArrayList<String> convertAllPseudoInstructions(ArrayList<String> fileAsArray){
		HashMap<String, String> instructions = new HashMap<String,String>();
		fillHashmap(instructions);
//		InputStream inStream = AssemblerMain.class.getResourceAsStream(filename);
//		Scanner input = new Scanner(inStream);
		HashMap<String, String> labels = new HashMap<String,String>();
		int currentInstructionAddress = 0;
//		while(input.hasNextLine()){
		ArrayList<String> arrayToRet = new ArrayList<String>();
		PseudoInstructionConverter conv = new PseudoInstructionConverter();
		for(int i = 0 ; i < fileAsArray.size(); i ++){
//			String[] line = input.nextLine().trim().split(" ");
			String[] line = fileAsArray.get(i).trim().split(" ");
//			System.out.println("instruction: "+currentInstructionAddress);
			if(!line[0].contains(":")){//if not a label
				if(line.length > 1 && (!instructions.containsKey(line[0]) || line[0].equals("push"))){//it is a pseudo instruction
					currentInstructionAddress += conv.convert(line, labels, arrayToRet);																											
				}else{//it is not a pseudo instruction, put it back together and print it out
					String write = "";
					for(int j = 0 ; j < line.length ; j++){
						write = write + " " + line[j];
					}
					write = write.trim();
//					System.out.println(write);
					arrayToRet.add(write);
					currentInstructionAddress++;
				}
			}else{
				//the current line is a label such as "Loop:"
				int addressOfLabel = currentInstructionAddress;
				String label = line[0].substring(0,line[0].length()-1);
				if(labels.containsKey(label)){
					System.out.println("Error, duplicate label name at instruction number: " + addressOfLabel);
				}else{
					String binaryNum = Integer.toBinaryString(addressOfLabel);
					while(binaryNum.length()< 16){
						binaryNum = "0" + binaryNum;
					}
					labels.put(label,binaryNum);
				}
			}
			
		}
		
		/*
		 * Code below is to write into a file in the project, use for debugging
		 */
//		for(Entry<String,String> e : labels.entrySet()){
//			System.out.println(e.getKey() + " has an address of: " + e.getValue());
//		}
//		input.close();
//		PrintStream out;
//		try {
//			out = new PrintStream(new FileOutputStream("C:\\EclipseWorkspaces\\csse290\\Assembler2.0\\src\\assembler\\Stage3.txt"));
//			System.setOut(out);
//			out.close();
//		} catch (FileNotFoundException e) {
//			System.out.println("Count not find file 2!");
//			return;
//		}
		return convertLabelsToAddresses(arrayToRet, labels);
	}
	
	/**
	 * Fills a hashmap with instructions
	 * @param map
	 */
	public static void fillHashmap(HashMap<String, String> map){
		InputStream inStream = null;
		inStream = AssemblerMain.class.getResourceAsStream("Instructions.txt");
		Scanner input = new Scanner(inStream);
		while(input.hasNextLine()){
			String line = input.nextLine();
			String[] arr = line.split(" ");
			map.put(arr[0], arr[1]);
		}
		input.close();
	}
	
	/**
	 * Reads through a file that has already gone through 
	 * convertAllHexToBinary() and convertAllPseudoInstructions()
	 * When it finds a label (which is dictated by a ":" in a line),
	 * it stores the label and assigns it an address equal to the current
	 * line in instruction memory that is being read. The label is not 
	 * passed over to the new file; it is simply replaced by the next line
	 * in the file, so that it doesn't affect the line count in the
	 * new file ( since labels are not actual instructions, they should
	 * not take up a line ).
	 * @param filename
	 */
	public ArrayList<String> convertLabelsToAddresses(ArrayList<String> fileAsArray, HashMap<String,String> labels){
//		InputStream inStream = AssemblerMain.class.getResourceAsStream(filename);
//		Scanner input = new Scanner(inStream);
//		while(input.hasNextLine()){
		ArrayList<String> arrayToRet = new ArrayList<String>();
		for(int m = 0 ; m < fileAsArray.size(); m++){
//			String[] line = input.nextLine().trim().split(" ");
			String[] line = fileAsArray.get(m).trim().split(" ");
			for(int i = 0 ; i < line.length; i ++){
				String up = "::upper::";
				String lo = "::lower::";
				if(line[i].contains(up)){
					String label = line[i].substring(9, line[i].length());//removes the "::upper::"
					if(labels.containsKey(label)){
						String addr = labels.get(label);
						while(addr.length()< 16){
							addr = "0" + addr;
						}
						line[i] = addr.substring(0,8);
					}else{
						System.out.println("Label not found");
					}
				}else if(line[i].contains(lo)){
					String label = line[i].substring(9, line[i].length());//removes the "::lower::"
					if(labels.containsKey(label)){
						String addr = labels.get(label);
						while(addr.length()< 16){
							addr = "0" + addr;
						}
						line[i] = addr.substring(8,16);
					}else{
						System.out.println("Label not found");
					}
				}
			}
			String write = "";
			for(int j = 0 ; j < line.length ; j++){
				write = write + " " + line[j];
			}
			write = write.trim();
//			System.out.println(write);
			arrayToRet.add(write);
			
		}
//		input.close();
		return arrayToRet;
	}
	
	/**
	 * Returns the machine code file as an array
	 * @param filename
	 */
	public ArrayList<String> convertToMachineCode(ArrayList<String> fileAsArray){
		HashMap<String, String> instructions = new HashMap<String,String>();
		fillHashmap(instructions);
//		InputStream inStream = AssemblerMain.class.getResourceAsStream(filename);
//		Scanner input = new Scanner(inStream);
		ArrayList<String> arrayToRet = new ArrayList<String>();
		
//		while(input.hasNextLine()){
		for(int i = 0 ; i < fileAsArray.size(); i ++){
//			String[] line = input.nextLine().trim().split(" ");
			String[] line = fileAsArray.get(i).trim().split(" ");
			String inst = line[0];
			String op = instructions.get(inst);
			String imm = "";
			if(inst.equals("beq") || inst.equals("bne")){
				imm = Integer.toBinaryString(3);//set to 3
				while(imm.length() < IMM_SIZE){
					imm = "0" + imm;
				}
			}else if(inst.equals("store")){
				imm = Integer.toBinaryString(2);//set to 2 
				while(imm.length() < IMM_SIZE){
					imm = "0" + imm;
				}
			}else if(inst.equals("pushui") || inst.equals("pushli") ||
					inst.equals("dupo") || inst.equals("repo")){
				imm = line[1];
				if(imm.length() > IMM_SIZE){
					imm = imm.substring(imm.length() - IMM_SIZE, imm.length());
				}else if(imm.length()<IMM_SIZE){
					while(imm.length() < IMM_SIZE){
						imm = "0" + imm;
					}
				}
			}else{
				while(imm.length() < IMM_SIZE){
					imm = "0" + imm;
				}
			}
			
//			System.out.println(op + imm);
			arrayToRet.add(op + imm);
		}
		return arrayToRet;
	}
	
}
