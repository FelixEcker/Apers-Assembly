package de.felixeckert.apersasm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			giveHelp();
			return;
		}
		
		boolean noOptions = false;
		String  inputFile = "";
		for (int i = 0; i < args.length; i++) {
			if (noOptions) {
				inputFile += args[i];
				continue;
			}
			
			switch (args[i].toLowerCase()) {
			case "-c":
				       inputFile = getPathFromArgs(args, i+1);
				String outputFile = getPathFromArgs(args, inputFile.split(" ").length+i+1);
				if (Compiler.compileAndSave(inputFile, outputFile)) {
					System.exit(0);
				} else {
					System.err.println("Compilation Failed!");
					System.exit(-1);
				}
				break;
			case "-r":
				inputFile = getPathFromArgs(args, i+1);
				Runtime.runProgram(inputFile);
				System.exit(0);
				break;
			default:
				noOptions = true;
				inputFile += args[i];
				break;
			}
			
			byte[] temp = Compiler.compile(new String(Files.readAllBytes(new File(inputFile).toPath())));
			Runtime.runProgramDirect(temp);
		}
	}
	
	/**
	 * Gets a Path from Program Arguments.
	 * If a Path has spaces it should be wrapped in double-quotes.
	 * 
	 * @param args The Arguments to fetch the path from
	 * @param i    Index to start fetching from
	 * @return The path at i.
	 * */
	private static String getPathFromArgs(String[] args, int i) {
		String path = "";
		if (!args[i].startsWith("\"")) { // Check if we have a single-string path
			return args[i];
		} else {
			path += args[i].substring(1); // Add first string of path
			i++;
			if (path.endsWith("\"")) { // Check if path was already terminated
				return path.substring(0, path.length()-1);
			}
			
			// Assemble Path
			for (int j = i; j < args.length; j++) {
				path += args[j];

				if (path.endsWith("\"")) {
					return path.substring(0, path.length()-1);
				}
			}
		}
		return path;
	}

	public static void giveHelp() {
		System.out.println("Apers Assembly Compiler & Runtime by Felix Eckert\n");
		System.out.println("OPTIONS:");
		System.out.println("-c <input> <output> Compiles a source file.");
		System.out.println("-r <binary>         Executes a binary file.");
		System.out.println("<input>             Compiles & executes a source file.");
	}
}
