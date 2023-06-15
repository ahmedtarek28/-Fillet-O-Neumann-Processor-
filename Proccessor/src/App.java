import java.util.*;

import javax.swing.text.AbstractDocument.Content;

import exceptions.AddressOutOfRangeException;
import exceptions.ImmediateTooLargeException;
import exceptions.InvalidInstructionException;
import exceptions.InvalidRegisterException;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;

public class App {
	// ------------------------------------------------------------------------------
	static String[] memory = new String[2048];
	static int pc = 0;
	static int[] Register = new int[32];
	static String nextInstruction;
	static int opcode;
	static int r1;
	static int value_r1;
	static int r2;
	static int value_r2;
	static int r3;
	static int value_r3;
	static int shamt;
	static int imm;
	static int address;
	static int res;
	static boolean WB = false;
	// static boolean MA = false;
	static boolean load = false;
	static boolean store = false;
	static boolean finish = false;
	static Queue<Object> fetch = new LinkedList<Object>();
	static Queue<Object> decode = new LinkedList<Object>();
	static Queue<Object> exexute = new LinkedList<Object>();
	static Queue<Object> memoryaccess = new LinkedList<Object>();
	static boolean fet = true;
	static boolean dec1 = false;
	static boolean dec2 = false;
	static boolean ex1 = false;
	static boolean ex2 = false;
	static boolean mem = false;
	static boolean wb = false;
	static int clk = 0;
	static int numofinst;
	static boolean jump = false;
	static int target = 0;
	static int lastwritedregister = 0;
	static int valueOflastwritedregister = 0;
	static boolean forwdFromMemory = false;
	static int newpc = 0;
	static boolean branch = false;
	static boolean jump1 = false;

	public static void fetch() {
		if (memory[pc] == null) {
			finish = true;
			return;
		}
		fet = false;
		System.out.println("--->Instruction " + (pc + 1) + " is being fetched");
		dec1 = true;
		fetch.add(pc + 1);
		fetch.add(memory[pc++]);
	}

	public static void decode1() {
		if (fetch.isEmpty()) {
			dec1 = false;
			// fet = false;
			return;
		}
		System.out.println("--->Instruction " + (fetch.peek()) + " is being decoded for its first clock cycle");
		int currpc = (int) fetch.poll();
		String currinst = (String) fetch.poll();
		System.out.println("----------------------------------------------------");
		System.out.println("	Inputs: ");
		System.out.println("    		Current PC: " + currpc);
		System.out.println("		Current Instruction: " + currinst);
		System.out.println("----------------------------------------------------");
		fetch.add(currpc);
		fetch.add(currinst);
		dec2 = true;
		dec1 = false;
	}

	public static void decode2() {
		if (fetch.isEmpty()) {
			dec2 = false;
			return;
		}
		dec2 = false;
		fet = true;
		ex1 = true;
		int instructionnum = (int) fetch.poll();
		System.out.println("--->Instruction " + (instructionnum) + " is being decoded for its second clock cycle");
		nextInstruction = (String) fetch.poll();
		opcode = Integer.parseInt(nextInstruction.substring(0, 4), 2);
		r1 = Integer.parseInt(nextInstruction.substring(4, 9), 2);
		r2 = Integer.parseInt(nextInstruction.substring(9, 14), 2);
		r3 = Integer.parseInt(nextInstruction.substring(14, 19), 2);
		shamt = Integer.parseInt(nextInstruction.substring(19, 32), 2);
		imm = binaryToDecimal(nextInstruction.substring(14, 32));
		address = Integer.parseInt(nextInstruction.substring(4, 32), 2);

		value_r1 = Register[r1];
		value_r2 = Register[r2];
		value_r3 = Register[r3];
		decode.add(instructionnum);
		decode.add(opcode);
		decode.add(r1);
		decode.add(r2);
		decode.add(r3);
		decode.add(shamt);
		decode.add(imm);
		decode.add(address);
		decode.add(value_r1);
		decode.add(value_r2);
		decode.add(value_r3);
	}

	public static void execute1() {
		if (decode.isEmpty()) {
			ex1 = false;
			return;
		}
		int instructionnum = (int) decode.poll();
		System.out.println("--->Instruction " + (instructionnum) + " is being executed for its first clock cycle");
		opcode = (int) decode.poll();
		r1 = (int) decode.poll();
		r2 = (int) decode.poll();
		r3 = (int) decode.poll();
		shamt = (int) decode.poll();
		imm = (int) decode.poll();
		address = (int) decode.poll();
		value_r1 = (int) decode.poll();
		value_r2 = (int) decode.poll();
		value_r3 = (int) decode.poll();
		System.out.println("----------------------------------------------------");
		System.out.println("	Inputs: ");
		System.out.println("    		Current PC: " + instructionnum);
		System.out.println("		Current opcode: " + opcode);
		if (opcode != 4 && opcode != 7 && opcode != 10 && opcode != 11) {
			if (r2 == lastwritedregister && !forwdFromMemory)
				value_r2 = valueOflastwritedregister;
			if (r3 == lastwritedregister && !forwdFromMemory)
				value_r3 = valueOflastwritedregister;
			if (opcode != 3 && opcode != 6 && opcode != 8 && opcode != 9) {
				System.out.println("		Dest. Reg: R" + r1 + " / Src. Reg1: R" + r2 + " / Src. Reg2: R" + r3);
				System.out.println("	 	Value of Src. Reg1: " + value_r2 + " / Value of Src. Reg2: " + value_r3);
			}
			if (opcode == 3) {
				System.out.println("		Dest. Reg: R" + r1 + " / Value of the Imm.num: " + imm);
			}
			if (opcode == 6) {
				System.out.println("		Dest. Reg: R" + r1 + " / Src. Reg1: R" + r2 + " / imm.value: " + imm);
				System.out.println("		Value of Src. Reg1: " + value_r2);
			}
			if (opcode == 8 || opcode == 9) {
				System.out.println("		Dest. Reg: R" + r1 + " / Src. Reg1: R" + r2 + " / shift.value: " + shamt);
				System.out.println("		Value of Src. Reg1: " + value_r2);
			}
		}
		if (opcode == 4) {
			if (r1 == lastwritedregister && !forwdFromMemory)
				value_r1 = valueOflastwritedregister;
			if (r2 == lastwritedregister && !forwdFromMemory)
				value_r2 = valueOflastwritedregister;
			System.out.println(" 		Src. Reg1: R" + r1 + " / Src. Reg2: R" + r2);
			System.out.println("		Value of Src. Reg1: " + value_r1 + " / Value of Src. Reg2: " + value_r2);
			if (imm > 0) {
				System.out.println("		Jump down by " + imm);
			} else if (imm < 0) {
				System.out.println("		Jump up by " + Math.abs(imm));
			}
		}
		if (opcode == 10) {
			if (r2 == lastwritedregister && !forwdFromMemory)
				value_r2 = valueOflastwritedregister;
			System.out.println("		Dest. Reg: R" + r1 + " / Src. Reg1: R" + r2 + " / imm.value: " + shamt);
			System.out.println("		Value of Src. Reg1: " + value_r2);
		}
		if (opcode == 11) {
			if (r1 == lastwritedregister && !forwdFromMemory)
				value_r1 = valueOflastwritedregister;
			if (r2 == lastwritedregister && !forwdFromMemory)
				value_r2 = valueOflastwritedregister;
			System.out.println("		Dest. Reg: R" + r1 + " / Src. Reg1: R" + r2 + " / imm.value: " + shamt);
			System.out.println("		Value of Src.Reg1: " + value_r1 + " / Value of Src. Reg2: " + value_r2);
		}
		if (opcode == 7)
			System.out.println("    	Jump address: " + address);
		System.out.println("----------------------------------------------------");
		decode.add(instructionnum);
		decode.add(opcode);
		decode.add(r1);
		decode.add(r2);
		decode.add(r3);
		decode.add(shamt);
		decode.add(imm);
		decode.add(address);
		decode.add(value_r1);
		decode.add(value_r2);
		decode.add(value_r3);
		ex1 = false;
		dec1 = true;
		ex2 = true;
	}

	public static void execute2() {
		if (decode.isEmpty()) {
			ex2 = false;
			return;
		}
		ex2 = false;
		dec2 = true;
		mem = true;
		int instructionnum = (int) decode.poll();
		System.out.println("--->Instruction " + (instructionnum) + " is being executed for its second clock cycle");
		opcode = (int) decode.poll();
		r1 = (int) decode.poll();
		r2 = (int) decode.poll();
		r3 = (int) decode.poll();
		shamt = (int) decode.poll();
		imm = (int) decode.poll();
		address = (int) decode.poll();
		value_r1 = (int) decode.poll();
		value_r2 = (int) decode.poll();
		value_r3 = (int) decode.poll();
		if (forwdFromMemory) {
			forwdFromMemory = false;
			if (opcode != 4 && opcode != 7 && opcode != 10 && opcode != 11) {
				if (r2 == lastwritedregister)
					value_r2 = valueOflastwritedregister;
				if (r3 == lastwritedregister)
					value_r3 = valueOflastwritedregister;
			}
			if (opcode == 4) {
				if (r1 == lastwritedregister)
					value_r1 = valueOflastwritedregister;
				if (r2 == lastwritedregister)
					value_r2 = valueOflastwritedregister;
			}
			if (opcode == 10) {
				if (r2 == lastwritedregister)
					value_r2 = valueOflastwritedregister;
			}
			if (opcode == 11) {
				if (r1 == lastwritedregister)
					value_r1 = valueOflastwritedregister;
				if (r2 == lastwritedregister)
					value_r2 = valueOflastwritedregister;
			}
		}
		switch (opcode) {
		case (0):
			res = value_r2 + value_r3;
			WB = true;
			break;
		case (1):
			res = value_r2 - value_r3;
			WB = true;
			break;
		case (2):
			res = value_r2 * value_r3;
			WB = true;
			break;
		case (3):
			res = imm;
			WB = true;
			break;
		case (4):
			if (value_r1 == value_r2 && imm != 0) {
				branch = true;
				newpc = instructionnum + imm;
			}
			break;
		case (5):
			res = value_r2 & value_r3;
			WB = true;
			break;
		case (6):
			res = value_r2 ^ imm;
			WB = true;
			break;
		case (7):
			jump1 = true;
			String temp = toBinary(pc,32).substring(0, 4) + toBinary(address, 28);
			newpc = Integer.parseInt(temp, 2);
			break;

		case (8):
			res = value_r2 << shamt;
			WB = true;
			break;
		case (9):
			res = value_r2 >> shamt;
			WB = true;
			break;
		case (10):
			res = value_r2 + imm;
			WB = true;
			load = true;
			break;
		case (11):
			res = value_r2 + imm;
			WB = false;
			store = true;
			break;
		}
		if (branch || jump1) {
			branch = false;
			jump1 = false;
			jump = true;
			target = clk + 2;
		}
		if (opcode != 4 && opcode != 7 && opcode != 10 && opcode != 11 && r1 != 0) {
			lastwritedregister = r1;
			valueOflastwritedregister = res;
		}
		exexute.add(instructionnum);
		exexute.add(res);
		exexute.add(load);
		exexute.add(store);
		exexute.add(WB);
		exexute.add(r1);
		exexute.add(value_r1);
		return;
	}

	public static void memoryAccess() throws AddressOutOfRangeException {
		if (exexute.isEmpty()) {
			mem = false;
			return;
		}
		mem = false;
		ex1 = true;
		wb = true;
		int instructionnum = (int) exexute.poll();
		System.out.println("--->Instruction " + (instructionnum) + " is in the memoryAccess stage");
		res = (int) exexute.poll();
		load = (boolean) exexute.poll();
		store = (boolean) exexute.poll();
		WB = (boolean) exexute.poll();
		r1 = (int) exexute.poll();
		value_r1 = (int) exexute.poll();
		System.out.println("----------------------------------------------------");
		System.out.println("	Inputs: ");
		System.out.println("    		Current PC: " + instructionnum);
		System.out.println("		Will Load? " + load + " / Will Strore? " + store);

		if (load) {
			forwdFromMemory = true;
			System.out.println("		The address to be accessed: " + res);
			System.out.println("----------------------------------------------------");
			if (res > 2047 || res < 1024)
				throw new AddressOutOfRangeException("AddressOutOfRange");
			System.out.println("**************************************************************************");
			System.out.println("**************************************************************************");
			System.out.println("        At address " + res + " in the Data Memory, the value "
					+ binaryToDecimal(memory[res]) + " is being loaded to Register R" + r1);
			System.out.println("**************************************************************************");
			System.out.println("**************************************************************************");
			res = binaryToDecimal(memory[res]);
			if (r1 != 0) {
				lastwritedregister = r1;
				valueOflastwritedregister = res;
			}
		} else if (store) {
			forwdFromMemory = true;
			System.out.println("		The address to be accessed: " + res);
			System.out.println("		The value to be stored: " + value_r1);
			System.out.println("----------------------------------------------------");
			if (res > 2047 || res < 1024)
				throw new AddressOutOfRangeException("AddressOutOfRange");
			System.out.println("**************************************************************************");
			System.out.println("**************************************************************************");
			System.out
					.println("	At address " + res + " in the Data Memory, the value " + value_r1 + " is being stored");
			System.out.println("**************************************************************************");
			System.out.println("**************************************************************************");
			memory[res] = toBinary(value_r1, 32);
		}
		memoryaccess.add(instructionnum);
		memoryaccess.add(res);
		memoryaccess.add(WB);
		memoryaccess.add(r1);
		return;
	}

	public static void writeBack() {
		if (memoryaccess.isEmpty()) {
			// dec1 = false;
			wb = false;
			return;
		}
		wb = false;
		ex2 = true;
		int instructionnum = (int) memoryaccess.poll();
		System.out.println("--->Instruction " + (instructionnum) + " is in the writeBack stage");
		res = (int) memoryaccess.poll();
		WB = (boolean) memoryaccess.poll();
		r1 = (int) memoryaccess.poll();
		System.out.println("----------------------------------------------------");
		System.out.println("	Inputs: ");
		System.out.println("    		Current PC: " + instructionnum);
		System.out.println("		The result: " + res);
		WB = r1 == 0 ? false : WB;
		System.out.println("		Will write-back? " + WB);
		System.out.println("----------------------------------------------------");
		if (r1 == 0)
			return;
		if (WB) {
			System.out.println("**************************************************************************");
			System.out.println("**************************************************************************");
			System.out.println("	REGISTER " + r1 + " value is being changed from " + Register[r1] + " to " + res);
			System.out.println("**************************************************************************");
			System.out.println("**************************************************************************");
			Register[r1] = res;
		}
		return;
	}

	public static void loadToMemory() throws Exception {
		File f = new File("F:\\GUC\\test.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s;
		int i = 0;
		while ((s = br.readLine()) != null) {
			if (s.isBlank())
				continue;
			numofinst++;
			String[] inst = s.split(" ");
			String toMem;
			if (inst[0].equals("JMP")) {
				toMem = instructionToBinary(inst[0]) + toBinary(Integer.parseInt(inst[1]), 28);
			} else if (inst[0].equals("LSL") || inst[0].equals("LSR")) {
				toMem = instructionToBinary(inst[0]) + registerToBinary(inst[1]) + registerToBinary(inst[2])
						+ toBinary(0, 5) + toBinary(Integer.parseInt(inst[3]), 13);
			} else if (inst[0].equals("ADD") || inst[0].equals("SUB") || inst[0].equals("MUL")
					|| inst[0].equals("AND")) {
				toMem = instructionToBinary(inst[0]) + registerToBinary(inst[1]) + registerToBinary(inst[2])
						+ registerToBinary(inst[3]) + toBinary(0, 13);
			} else if (inst[0].equals("MOVI")) {
				if (Integer.parseInt(inst[2]) > 131071 || Integer.parseInt(inst[2]) < -131072)
					throw new ImmediateTooLargeException("imm must be in range -131072<=imm<=131071");
				toMem = instructionToBinary(inst[0]) + registerToBinary(inst[1]) + toBinary(0, 5)
						+ toBinary(Integer.parseInt(inst[2]), 18);
			} else {
				toMem = instructionToBinary(inst[0]) + registerToBinary(inst[1]) + registerToBinary(inst[2])
						+ toBinary(Integer.parseInt(inst[3]), 18);
				if (Integer.parseInt(inst[3]) > 131071 || Integer.parseInt(inst[3]) < -131072)
					throw new ImmediateTooLargeException("imm must be in range -131072<=imm<=131071");
			}
			memory[i++] = toMem;
		}
	}

	public static void reset() {
		load = false;
		store = false;
		WB = false;
	}

	public static void start() throws Exception {
		loadToMemory();
		while (true) {
			reset();
			clk++;
			if (finish && !(dec1 || dec2 || ex1 || ex2 || mem || wb)) {
				break;
			}
			System.out.println("In Clock Cycle " + (clk) + ":");
			if (jump && clk == target) {
				jump = false;
				target = 0;
				fet = true;

			}
			if (wb)
				writeBack();
			reset();
			if (mem)
				memoryAccess();
			reset();
			if (ex2)
				execute2();
			reset();
			if (ex1)
				execute1();
			reset();
			if (dec2)
				decode2();
			reset();
			if (dec1)
				decode1();
			reset();
			if (fet)
				fetch();
			reset();
			if (jump) {
				pc = newpc;
				fetch = new LinkedList<Object>();
				decode = new LinkedList<Object>();
			}
		}
		System.out.println("\n");
		System.out.println("The Register File: ");
		int j = 0;
		for (int i : Register) {
			System.out.println("R" + j + " = " + i);
			j++;
		}
		System.out.println();
		System.out.println("The Instruction Memory: ");
		for (int i = 0; i < numofinst; i++) {
			System.out.println("	" + memory[i]);
		}
		System.out.println();
		System.out.println("The Data Memory: ");
		for (int i = 1024; i < 2048; i++) {
			if (memory[i] != null) {
				System.out.println("	At address " + i + ": " + binaryToDecimal(memory[i]));
			}
		}
		System.out.println();
		System.out.println("Clock Cycles= " + (clk - 1));
	}

	public static int binaryToDecimal(String s) {
		if (s.charAt(0) == '1') {
			String invertedInt = invertDigits(s);
			int decimalValue = Integer.parseInt(invertedInt, 2);
			decimalValue = (decimalValue + 1) * -1;
			return decimalValue;
		} else {
			return Integer.parseInt(s, 2);
		}
	}

	public static String invertDigits(String s) {
		String result = s;
		result = result.replace("0", " ");
		result = result.replace("1", "0");
		result = result.replace(" ", "1");
		return result;
	}

	public static void main(String[] args) throws Exception {
		start();
		int j = 0;
	}

	public static String instructionToBinary(String s) throws InvalidInstructionException {
		switch (s) {
		case "ADD":
			return toBinary(0, 4);
		case "SUB":
			return toBinary(1, 4);
		case "MUL":
			return toBinary(2, 4);
		case "MOVI":
			return toBinary(3, 4);
		case "JEQ":
			return toBinary(4, 4);
		case "AND":
			return toBinary(5, 4);
		case "XORI":
			return toBinary(6, 4);
		case "JMP":
			return toBinary(7, 4);
		case "LSL":
			return toBinary(8, 4);
		case "LSR":
			return toBinary(9, 4);
		case "MOVR":
			return toBinary(10, 4);
		case "MOVM":
			return toBinary(11, 4);
		default:
			throw new InvalidInstructionException("Invalid Instruction in line " + (numofinst));
		}

	}

	public static String registerToBinary(String s) throws InvalidRegisterException {
		for (int i = 0; i < 32; i++) {
			if (s.equals("R" + i)) {
				return toBinary(i, 5);
			}
		}
		throw new InvalidRegisterException("Invalid Register " + s);
	}

	public static String toBinary(int x, int len) {
		if (len > 0) {
			if (x >= 0) {
				return String.format("%" + len + "s", Integer.toBinaryString(x)).replaceAll(" ", "0");
			} else {
				return Integer.toBinaryString(x).substring(14, 32);
			}
		}

		return null;
	}

}