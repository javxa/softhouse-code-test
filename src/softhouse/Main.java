package softhouse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import wse.WSE;

public class Main {
	
	static String TEST_DATA = 
			  "P|Elof|Sundin\r\n"
			+ "T|073-101801|018-101801\r\n"
			+ "A|S:t Johannesgatan 16|Uppsala|75330\r\n"
			+ "F|Hans|1967\r\n"
			+ "A|Frodegatan 13B|Uppsala|75325\r\n"
			+ "F|Anna|1969\r\n"
			+ "T|073-101802|08-101802\r\n"
			+ "P|Boris|Johnson\r\n"
			+ "A|10 Downing Street|London";

	public static void main(String[] args) {
		
		InputStream input;
		OutputStream output;
		boolean allowDuplicateInfo = false;
		
		if (args.length >= 1) {
			// Input file in first argument
			try {
				input = new FileInputStream(new File(args[0]));
			} catch (FileNotFoundException e) {
				System.err.println("Input file not found: " + args[0]);
				return;
			}
		} else {
			input = new ByteArrayInputStream(TEST_DATA.getBytes());
			
			System.out.println("Using test data as input:");
			System.out.println(TEST_DATA);
			System.out.println();
		}
		
		if (args.length >= 2) {
			try {
				output = new FileOutputStream(args[1]);
			} catch (FileNotFoundException e) {
				System.err.println("Output file not found: " + args[0]);
				return;
			}
		} else {			
			output = System.out;
		}
		
		if (args.length >= 3) {
			// true if equals "1" or "true", else false
			allowDuplicateInfo = WSE.parseBool(args[2]);
		}
		
		SystemConverter converter = new SystemConverter(input, output, StandardCharsets.UTF_8, allowDuplicateInfo);
		
		try {
			converter.convert();
		} catch (BadFormatException e) {
			System.out.println(e.getMessage());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		try {
			input.close();
			output.close();
		} catch (IOException ignore) {
		}
	}
}