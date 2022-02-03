package de.felixeckert.apersasm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Apers Assembly Runtime.
 * Based on my sparse specifications for the Runtime
 * in the <a href="https://esolangs.org/wiki/Apers_Assembly#The_Runtime">Esolang wiki entry</a>.
 *
 * @author Felix Eckert
 * */
public class Runtime implements Runnable {
	private byte[] MEMORY = new byte[16 * 1024]; // 16 kB of RAM as Stated in Wiki
	private byte   REGS_A = 0x0;
	private byte   REGS_B = 0x0;
	private byte   REGS_C = 0x0;
	private byte   COMPARE= 0x0;
	private int    programCounter = 0;
	
	/**
	 * Program Bytecode
	 * */
	private byte[] bytecode;

	/**
	 * Thread for the Runtime
	 * */
	private Thread thread;
	/**
	 * Start Time of Program in NS
	 * */
	private long  startTime;
	/**
	 * End Time of Program in NS
	 * */
	private long  endTime;
	
	private Scanner SYS_IN;
	
	/**
	 * @param bytecode The Program Bytecode
	 * */
	private Runtime(byte[] bytecode) {
		this.bytecode = bytecode;
		this.SYS_IN = new Scanner(System.in);
	}
	
	public void start() {
		this.thread = new Thread(this, "ApersAssemblyRuntime");
		this.thread.start();
	}
	
	public void run() {
		startTime = System.nanoTime();
		while (programCounter < bytecode.length) {
			byte instruction = bytecode[programCounter];
			byte register;
			byte address;
			byte value;
			
			switch (instruction) {
			case 0x00: // apers (Branch Result Zero)
				if (COMPARE == 0x00) {
					programCounter = getInt(programCounter+1);
				} else {
					programCounter += 5;
				}
				break;
			case 0x01: // apres (Branch Result Greater)
				if (COMPARE > 0x00) {
					programCounter = getInt(programCounter+1);
				} else {
					programCounter += 5;
				}
				break;
			case 0x02: // asper (Branch Result Less)
				if (COMPARE < 0x00) {
					programCounter = getInt(programCounter+1);
				} else {
					programCounter += 5;
				}
				break;
			case 0x03: // pares (Branch Non Conditional)
				programCounter = getInt(programCounter+1);
				break;
			case 0x04: // parse (Load Register, VALUE)
				register = bytecode[programCounter+1];
				value    = bytecode[programCounter+2];
				
				loadRegister(register, value);
				programCounter += 3;
				break;
			case 0x05: // parse (Load Register, ADDRESS)
				register = bytecode[programCounter+1];
				value    = MEMORY[bytecode[programCounter+2]];
				
				loadRegister(register, value);
				programCounter += 3;
				break;
			case 0x06: // parse (Load Register, POINTER)
				register = bytecode[programCounter+1];
				value    = MEMORY[
				                  MEMORY[bytecode[programCounter+2]]
				                 ];
				
				loadRegister(register, value);
				programCounter += 3;
				break;
			case 0x07: // pears (Store Register, ADDRESS)
				register = bytecode[programCounter+1];
				address = bytecode[programCounter+2];
				
				storeRegister(register, address);
				programCounter += 3;
				break;
			case 0x08: // pears (Store Register, POINTER)
				register = bytecode[programCounter+1];
				address = MEMORY[bytecode[programCounter+2]];
				
				storeRegister(register, address);
				programCounter += 3;
				break;
			case 0x09: // prase (Add Register + Memory, ADDRESS)
				value = MEMORY[
				               bytecode[programCounter+2]
						];
				register = bytecode[programCounter+1];
				
				loadRegister(register, (byte) (getRegisterValue(register)+value));
				programCounter += 3;
				break;
			case 0x0a: // prase (Add Register + Memory, POINTER)
				value = MEMORY[
				               MEMORY[bytecode[programCounter+2]]
						];
				register = bytecode[programCounter+1];
				
				loadRegister(register, (byte) (getRegisterValue(register)+value));
				programCounter += 3;
				break;
			case 0x0b: // presa (Sub Register - Memory, ADDRESS)
				value = MEMORY[
				               bytecode[programCounter+2]
						];
				register = bytecode[programCounter+1];
				
				loadRegister(register, (byte) (getRegisterValue(register)-value));
				programCounter += 3;
				break;
			case 0x0c: // presa (Sub Register - Memory, POINTER)
				value = MEMORY[
				               MEMORY[bytecode[programCounter+2]]
						];
				register = bytecode[programCounter+1];
				
				loadRegister(register, (byte) (getRegisterValue(register)-value));
				programCounter += 3;
				break;
			case 0x0d: // rapes (Input To Register)
				register = bytecode[programCounter+1];
				loadRegister(register, (byte) SYS_IN.nextInt());
				programCounter += 2;
				break;
			case 0x0e: // reaps (Output Register)
				register = bytecode[programCounter+1];
				System.out.print((char) getRegisterValue(register));
				programCounter += 2;
				break;
			case 0x0f: // spare (Compare Registers)
				register = bytecode[programCounter+1];
				value    = bytecode[programCounter+2];
				
				COMPARE  = (byte) (getRegisterValue(register)-getRegisterValue(value));
				programCounter += 3;
				break;
			case 0x10: // spear (Exit Program)
				programCounter = bytecode.length;
				break;
			}
		}
		endTime = System.nanoTime();
	}

	/**
	 * Internal helper method to get a Integer from
	 * program bytecode.
	 * 
	 * @param i Where to start assembling the Integer at
	 * @return A 32-bit integer.
	 * */
	private int getInt(int i) {
		int value = 0;
	    for (int j = i; j < i+4; j++) {
	        int shift = (4 - 1 - j) * 8;
	        value += (bytecode[j] & 0x000000FF) << shift;
	    }
	    return value;
	}

	/**
	 * @return The Time the Program took to execute in nanoseconds.
	 * */
	public long getExecutionTime() {
		return endTime-startTime;
	}
	
	/**
	 * Internal Helper Method to Load a Register.
	 * @param register Register ID (0 = a; 1 = b; 2 = c; default = c;)
	 * @param value    Value to Load with
	 * */
	private void loadRegister(byte register, byte value) {
		switch (register) {
		case 0x00:
			REGS_A = value;
			break;
		case 0x01:
			REGS_B = value;
			break;
		case 0x02:
		default:
			REGS_C = value;
			break;
		}
	}

	/**
	 * Internal Helper Method to Store a Register.
	 * @param register Register ID (0 = a; 1 = b; 2 = c; default = c;)
	 * @param address  Where to Store to
	 * */
	private void storeRegister(byte register, byte address) {
		switch (register) {
		case 0x00:
			MEMORY[address] = REGS_A;
			break;
		case 0x01:
			MEMORY[address] = REGS_B;
			break;
		case 0x02:
		default:
			MEMORY[address] = REGS_C;
			break;
		}
	}
	
	/**
	 * Internal Helper Method to get Value of A register
	 * @param register Register ID (0 = a; 1 = b; 2 = c; default = c;)
	 * @return The Registers value.
	 * */
	private byte getRegisterValue(byte register) {
		switch (register) {
		case 0x00:
			return REGS_A;
		case 0x01:
			return REGS_B;
		case 0x02:
		default:
			return REGS_C;
		}
	}
	
	private boolean isAlive() {
		return thread.isAlive();
	}
	
	public static void runProgram(String inputFile) {
		byte[] bytecode = null;
		Path   path = new File(inputFile).toPath();
		try {
			bytecode = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		runProgramDirect(bytecode);
	}

	public static void runProgramDirect(byte[] bytecode) {
		Runtime runtime = new Runtime(bytecode);
		runtime.start();
		try {
			while (runtime.isAlive()) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.printf("\nFinished execution in %s nanoseconds\n", runtime.getExecutionTime());
	}
}
