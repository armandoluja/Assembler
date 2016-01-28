package assembler;

import java.util.ArrayList;
import java.util.HashMap;

/**converts pseudo instructions into primitive 
*instructions
*/
public class PseudoInstructionConverter {
	
	private ArrayList<String> array;

	/**
	 * Converts a Pseudo instruction to primitive
	 * instructions
	 * @param instruction
	 * @param array 
	 * @return
	 */
	public int convert(String[] instruction,HashMap<String, String> labels, ArrayList<String> array){
		this.array = array;
		String op = instruction[0];
		if(instruction.length == 2){
			//handles 1 parameter
			if(op.equals("sub")){
				return sub1(instruction);
			}else if(op.equals("j")){
				return jump(instruction);
			}else if(op.equals("pushi")){
				return pushi(instruction[1],labels);
			}else if(op.equals("push")){
				return push(instruction);
			}else{
				return op1(instruction);
			}
		}else if(instruction.length == 3){
			//handles 2 parameters
			if(op.equals("sub")){
				return sub2(instruction);
			}else{
				return op2(instruction);
			}
		}else if(instruction.length == 4){
			//handles 3 parameters
			if(op.equals("sub")){
				return sub3(instruction);
			}else{
				return op3(instruction);
			}
		}else{
//			this.array.add("Invalid instruction !!!!!!!!!!!!!!!");
			return 0;
		}
		
	}
	
	/**
	 * Handles pushi with 1 parameter, takes a binary number
	 * and pushes it on the stack
	 * @param labels 
	 * @param instruction
	 * @return number of instructions printed
	 */
	public int pushi(String binaryNum, HashMap<String,String> labels){
		if(!AssemblerMain.isIntegerBin(binaryNum) && !(labels == null)){
			//handle "push Label" here
			if(labels.containsKey(binaryNum)){//if the label has been addressed by assembler
				String addr = labels.get(binaryNum);
				while(addr.length() < 16){//pad zeros if necessary
					addr = "0" + addr;
				}
				this.array.add("pushui "+ addr.substring(0,8));
				this.array.add("pushli "+ addr.substring(8,16));
				this.array.add("or");
				return 3;
			}else{//if the label has not been addressed by assembler
				this.array.add("pushui ::upper::"+ binaryNum);
				this.array.add("pushli ::lower::"+ binaryNum);
				this.array.add("or");
				return 3;
			}
		}else{
			this.array.add("pushui "+ binaryNum.substring(0,8));
			this.array.add("pushli "+ binaryNum.substring(8,16));
			this.array.add("or");
			return 3;
		}
	}
	
	/**
	 * Handles push with 1 parameter
	 * @param instruction
	 */
	public int push(String[] instruction){
		int count1 = pushi(instruction[1], null);
		this.array.add("push");
		return count1 + 1;
	}
	
	/**
	 * Handles sub with 1 parameter
	 * @param instruction
	 */
	public int sub1(String[] instruction){
		int count1 = pushi(instruction[1], null);
		this.array.add("push");
		this.array.add("neg");
		this.array.add("add");
		return count1 + 3;
	}
	
	/**
	 * Handles sub with 2 parameters
	 * @param instruction
	 */
	public int sub2(String[] instruction){
		String src1 = instruction[1];
		String src2 = instruction[2];
		
		int count1 = pushi(src1, null);	
		this.array.add("push");
		int count2 = pushi(src2, null);
		this.array.add("push");
		this.array.add("neg");
		this.array.add("add");
		return count1 + 1 + count2 + 3;
	}
	
	/**
	 * Handles sub with 3 parameters
	 * @param instruction
	 */
	public int sub3(String[] instruction){
		String dest = instruction[1];
		String src1 = instruction[2];
		String src2 = instruction[3];
		
		int count1 = pushi(src1, null);
		this.array.add("push");
		int count2 = pushi(src2, null);
		this.array.add("push");
		this.array.add("neg");
		this.array.add("add");
		int count3 = pushi(dest, null);
		this.array.add("store");
		return count1 + 1 + count2 + 3 + count3 + 1;
	}
	
	
	/**
	 * Handles add, and, or, addi, subi, andi, ori
	 * with 1 parameter
	 * @param instruction
	 */
	public int op1(String[] instruction){
		String op = instruction[0];
		String srcOrVal1 = instruction[1];
		
		int count1 = pushi(srcOrVal1, null);
		if(op.contains("i")){
			//addi,subi,andi,ori
			op = op.substring(0,op.length()-1);//trims off i
			this.array.add(op);
			return count1 + 1;
		}else{
			this.array.add("push");
			this.array.add(op);
			return count1 + 2;
		}
	}
	
	
	/**
	 * Handles add,and,or with 2 parameters
	 * @param instruction
	 */
	public int op2(String[] instruction){
		String op = instruction[0];
		String src1 = instruction[1];
		String src2 = instruction[2];
		
		int count1 = pushi(src1, null);
		this.array.add("push");
		int count2 = pushi(src2, null);
		this.array.add("push");
		this.array.add(op);
		return count1 + 1 + count2 + 2;
	}
	
	/**
	 * Handles add,and, or with 3 parameters
	 * TODO: implement in AssemblerMain, so that instructions with 3 parameters can be converted into machine code
	 * @param instruction
	 */
	public int op3(String[] instruction){
		String op = instruction[0];
		String dest = instruction[1];
		String src1 = instruction[2];
		String src2 = instruction[3];
		
		int count1 = pushi(src1, null);
		this.array.add("push");
		int count2 = pushi(src2, null);
		this.array.add("push");
		this.array.add(op);
		int count3 = pushi(dest, null);
		this.array.add("store");
		return count1 + 1 + count2 + 2 + count3 + 1;
	}
	
	/**
	 * Handles jump instructions with 1 parameter
	 * @param instruction
	 */
	public int jump(String[] instruction){
		if(AssemblerMain.isIntegerBin(instruction[1])){
			int count1 = pushi(instruction[1], null);
			this.array.add("j");
			return count1 + 1;
		}
		return 0;
	}
}
