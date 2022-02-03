package de.felixeckert.apersasm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;

public class Compiler {
	public static byte[] compile(String input) {
		HashMap<String, Integer> LABELS         = new HashMap<>();
		HashMap<Integer, String> MISSING_LABELS = new HashMap<>();
		LinkedList<String> words = new LinkedList<>();
		
		String[] lines = input.replace(",", "").replaceAll("\t", " ").split("\r\n");
		
		// Remove Comments
        for (int i = 0; i < lines.length; i++) {
            String[] lsplit = lines[i].split(" ");

            for (int j = 0; j < lsplit.length; j++) {
                String word = lsplit[j].replaceAll(" ", "");

                if (word.startsWith(";")) {
                    break;
                } else if (!word.matches("")) {
                    words.add(word);
                }
            }
        }     
        
        LinkedList<Byte> bytecode = new LinkedList<>();
        BigInteger bigInt;
        byte[] bytearray;
        
        // Translate
        for (int i = 0; i < words.size(); i++) {
        	String word = words.get(i);
        	
        	// Register Labels
        	if (word.matches("LABEL")) {
        		LABELS.put(words.get(i+1), bytecode.size()-1);
        		i++; continue;
        	}
        	
        	// Insert Labels
        	if (word.toCharArray()[0] == word.toUpperCase().toCharArray()[0]) {
        		if (!LABELS.containsKey(word)) {	
    				MISSING_LABELS.put(bytecode.size(), word);
        			
        			for (int j = 0; j < 4; j++) bytecode.add((byte) 0x00);
        			continue;
        		}
        		byte[] bytes = ByteBuffer.allocate(4).putInt(LABELS.get(word)).array();
                for (byte b : bytes) {
                    bytecode.add(b);
                }
                
        		continue;
        	}
        	
        	switch (word) {
        	case "apers":
        		bytecode.add((byte) 0x00); // OP-CODE
        		if (words.get(i+1).startsWith("$")) { // Check if a direct address was specified
        			// Convert hex string to int
					int val = Integer.parseInt(word.replace("$", "").replace("0x", ""), 16);
					byte[] bytes = ByteBuffer.allocate(4).putInt(val).array(); // ADD TO BYTECODE
					for (byte b : bytes) {
					    bytecode.add(b);
					}
        			i++;	
        		}
        		break;
        	case "apres":
        		bytecode.add((byte) 0x01); // OP-CODE
        		if (words.get(i+1).startsWith("$")) { // Check if a direct address was specified
        			// Convert hex string to int
					int val = Integer.parseInt(word.replace("$", "").replace("0x", ""), 16);
					byte[] bytes = ByteBuffer.allocate(4).putInt(val).array(); // ADD TO BYTECODE
					for (byte b : bytes) {
					    bytecode.add(b);
					}
        			i++;	
        		}
        		break;
        	case "asper":
        		bytecode.add((byte) 0x02); // OP-CODE
        		if (words.get(i+1).startsWith("$")) { // Check if a direct address was specified
        			// Convert hex string to int
					int val = Integer.parseInt(word.replace("$", "").replace("0x", ""), 16);
					byte[] bytes = ByteBuffer.allocate(4).putInt(val).array(); // ADD TO BYTECODE
					for (byte b : bytes) {
					    bytecode.add(b);
					}
        			i++;	
        		}
        		break;
        	case "pares":
        		bytecode.add((byte) 0x03); // OP-CODE
        		if (words.get(i+1).startsWith("$")) { // Check if a direct address was specified
        			// Convert hex string to int
					int val = Integer.parseInt(word.replace("$", "").replace("0x", ""), 16);
					byte[] bytes = ByteBuffer.allocate(4).putInt(val).array(); // ADD TO BYTECODE
					for (byte b : bytes) {
					    bytecode.add(b);
					}
        			i++;	
        		}
        		break;
        	case "parse":
        		// Add Appropriate OP-CODE
        		if (words.get(i+2).startsWith("#")) {
        			bytecode.add((byte) 0x04);
        		} else if (words.get(i+2).startsWith("$")) {
        			bytecode.add((byte) 0x05);
        		} else if (words.get(i+2).startsWith("*")) {
        			bytecode.add((byte) 0x06);
        		} else {
        			System.err.printf("COMPILATION ERROR: INVALID PARAMTER FOR INSTRUCTION %s (%s)\n",
        					word, words.get(i+2));
        			return null;
        		}
        		
        		// Translate Params
        		// 1st Register
        		bytecode.add(
        				words.get(i+1).matches("a") ? (byte) 0x00 : 
        					words.get(i+1).matches("b") ? (byte) 0x01 : (byte) 0x02);
        		
        		// 2nd VALUE/ADDRESS/POINTER
                bytecode.add((byte) (Integer.parseInt(words.get(i+2).substring(1),16) & 0xff));;
                i += 2;
        		break;
        	case "pears":
        		// Add Appropriate OP-CODE
        		if (words.get(i+2).startsWith("$")) {
        			bytecode.add((byte) 0x07);
        		} else if (words.get(i+2).startsWith("*")) {
        			bytecode.add((byte) 0x08);
        		} else {
        			System.err.printf("COMPILATION ERROR: INVALID PARAMTER FOR INSTRUCTION %s (%s)\n",
        					word, words.get(i+2));
        			return null;
        		}
        		
        		// Translate Params
        		// 1st Register
        		bytecode.add(
        				words.get(i+1).matches("a") ? (byte) 0x00 : 
        					words.get(i+1).matches("b") ? (byte) 0x01 : (byte) 0x02);
        		
        		// 2nd VALUE/ADDRESS/POINTER
                bytecode.add((byte) (Integer.parseInt(words.get(i+2).substring(1),16) & 0xff));
                i += 2;
        		break;
        	case "prase":
        		// Add Appropriate OP-CODE
        		if (words.get(i+2).startsWith("$")) {
        			bytecode.add((byte) 0x09);
        		} else if (words.get(i+2).startsWith("*")) {
        			bytecode.add((byte) 0x0a);
        		} else {
        			System.err.printf("COMPILATION ERROR: INVALID PARAMTER FOR INSTRUCTION %s (%s)\n",
        					word, words.get(i+2));
        			return null;
        		}
        		
        		// Translate Params
        		// 1st Register
        		bytecode.add(
        				words.get(i+1).matches("a") ? (byte) 0x00 : 
        					words.get(i+1).matches("b") ? (byte) 0x01 : (byte) 0x02);
        		
        		// 2nd VALUE/ADDRESS/POINTER
                bytecode.add((byte) (Integer.parseInt(words.get(i+2).substring(1),16) & 0xff));
                i += 2;
        		break;
        	case "presa":
        		// Add Appropriate OP-CODE
        		if (words.get(i+2).startsWith("$")) {
        			bytecode.add((byte) 0x0b);
        		} else if (words.get(i+2).startsWith("*")) {
        			bytecode.add((byte) 0x0c);
        		} else {
        			System.err.printf("COMPILATION ERROR: INVALID PARAMTER FOR INSTRUCTION %s (%s)\n",
        					word, words.get(i+2));
        			return null;
        		}
        		
        		// Translate Params
        		// 1st Register
        		bytecode.add(
        				words.get(i+1).matches("a") ? (byte) 0x00 : 
        					words.get(i+1).matches("b") ? (byte) 0x01 : (byte) 0x02);
        		
        		// 2nd VALUE/ADDRESS/POINTER
                bytecode.add((byte) (Integer.parseInt(words.get(i+2).substring(1),16) & 0xff));
                i += 2;
        		break;
        	case "rapes":
        		bytecode.add((byte) 0x0d);
        		bytecode.add(
        				words.get(i+1).matches("a") ? (byte) 0x00 : 
        					words.get(i+1).matches("b") ? (byte) 0x01 : (byte) 0x02);
        		i++;
        		break;
        	case "reaps":
        		bytecode.add((byte) 0x0e);
        		bytecode.add(
        				words.get(i+1).matches("a") ? (byte) 0x00 : 
        					words.get(i+1).matches("b") ? (byte) 0x01 : (byte) 0x02);
        		i++;
        		break;
        	case "spare":
        		bytecode.add((byte) 0x0f);
        		bytecode.add(
        				words.get(i+1).matches("a") ? (byte) 0x00 : 
        					words.get(i+1).matches("b") ? (byte) 0x01 : (byte) 0x02);

        		bytecode.add(
        				words.get(i+2).matches("a") ? (byte) 0x00 : 
        					words.get(i+2).matches("b") ? (byte) 0x01 : (byte) 0x02);
        		
        		i += 2;
        		break;
        	case "spear":
        		bytecode.add((byte) 0x10);
        		break;
        	}
        }
        
        // Replace Missing Labels
        LinkedList<Byte>[] tmp = new LinkedList[] {bytecode};
        if (!MISSING_LABELS.isEmpty()) {
        	MISSING_LABELS.forEach((codepoint, label) -> {
                if (!LABELS.containsKey(label)) {
                    System.err.println("COMPILATION ERROR: LABEL NOT DEFINED!");
                    System.err.printf("$%s : %s\n", codepoint, label);
                    return;
                }
                byte[] address = ByteBuffer.allocate(4).putInt(LABELS.get(label)).array();
                for (int i = 0; i < 4; i++)
                    tmp[0].set(codepoint+i, address[i]);
            });
        }
        
		byte[] returnVal = new byte[bytecode.size()];
		for (int i = 0; i < returnVal.length; i++) returnVal[i] = bytecode.pop();
        
		return returnVal;
	}
	
	public static boolean compileAndSave(String input, String output) {
		try {
			byte[] bytecode = compile(
					new String(Files.readAllBytes(new File(input).toPath()))
					);
			
			if (bytecode == null || bytecode.length == 0) return false;
			
			OutputStream os = new FileOutputStream(output);
            os.write(bytecode);
            os.close();
            return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
