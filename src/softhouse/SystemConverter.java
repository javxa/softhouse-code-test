package softhouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import wse.utils.xml.XMLElement;

public class SystemConverter {

	private final InputStream input;
	private final OutputStream output;
	private final Charset charset;

	private Person currentPerson;
	private FamilyMember currentFamilyMember;
	private Address currentAddress;
	private Phone currentPhone;

	private boolean allowDuplicateInfo = false;

	public SystemConverter(InputStream input, OutputStream output, Charset charset, boolean allowDuplicateInfo) {
		this.input = input;
		this.output = output;
		this.charset = charset;
		this.allowDuplicateInfo = allowDuplicateInfo;
	}

	public void convert() throws Exception {

		/**
		 * I förberedelse för en stor mängd personer i databanken skriver vi ut en (1)
		 * person i taget, istället för att bygga upp en enda xml innehållandes dem
		 * alla.
		 * 
		 * Att databanken innehåller endast en person och 2^28 familjemedlemmar beräknas
		 * vara utanför rimlighetens gränser - vi gör därför ingen vidare optimisering
		 * som att skriva ut en familjemedlem i taget. Det bör dock övervägas om man
		 * anser detta vara rimligt eller vet att så är fallet.
		 * 
		 */

		output.write("<people>\n".getBytes(this.charset));

		BufferedReader reader = new BufferedReader(new InputStreamReader(input, this.charset));

		int lineCounter = 1;
		
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				OldSystemRow row = new OldSystemRow(line);
				parseRow(row);
				
				lineCounter++;
			} catch (BadFormatException e) {
				throw new BadFormatException("Bad format on line " + lineCounter + ": " + e.getMessage(), e);
			}
		}

		printCurrentPerson();

		output.write("</people>\n".getBytes(this.charset));
		output.flush();
	}

	private void parseRow(OldSystemRow row) throws IOException {
		switch (row.type) {
		case 'P': {
			parsePerson(row);
			break;
		}
		case 'T': {
			parsePhone(row);
			break;
		}
		case 'A': {
			parseAddress(row);
			break;
		}
		case 'F': {
			parseFamilyMember(row);
			break;
		}
		default: {
			throw new BadFormatException("Unknown row type: " + row.type);
		}
		}
	}

	private void parsePerson(OldSystemRow row) throws IOException {

		if (currentPerson != null) {
			// No more information will show about this person, we can now print them
			printCurrentPerson();
		}

		currentPerson = new Person(row.part(0), row.part(1));
		
		// Person, reset address and phone info (So we can make sure they
		// appear only once per individual)
		currentFamilyMember = null;
		currentAddress = null;
		currentPhone = null;
	}

	private void parseFamilyMember(OldSystemRow row) {
		if (currentPerson == null)
			throw new BadFormatException("FamilyMember row appeared before Person row");

		currentFamilyMember = new FamilyMember(row.part(0), row.part(1));
		currentPerson.family.add(currentFamilyMember);

		// New family member, reset address and phone info (So we can make sure they
		// appear only once per individual)
		currentAddress = null;
		currentPhone = null;
	}

	private void parsePhone(OldSystemRow row) {
		if (currentPerson == null)
			throw new BadFormatException("Phone row appeared before Person row");

		if (currentPhone != null)
			duplicateError("Phone");

		currentPhone = new Phone(row.part(0), row.part(1));

		if (currentFamilyMember != null)
			currentFamilyMember.phone = currentPhone;
		else
			currentPerson.phone = currentPhone;
	}

	private void parseAddress(OldSystemRow row) {
		if (currentPerson == null)
			throw new BadFormatException("Address row appeared before Person row");

		if (currentAddress != null)
			duplicateError("Address");

		currentAddress = new Address(row.part(0), row.part(1), row.part(2));

		if (currentFamilyMember != null)
			currentFamilyMember.address = currentAddress;
		else
			currentPerson.address = currentAddress;
	}

	/**
	 * Output the current person as XML with an indentation of 1
	 */
	private void printCurrentPerson() throws IOException {
		XMLElement person = currentPerson.toXML();
		person.write(output, charset, 1);
	}

	private void duplicateError(String message) {
		if (allowDuplicateInfo)
			return;
		throw new BadFormatException(
				"Duplicate error: " + message + " row cannot appear multiple times for the same person");
	}
}

/**
 * Helper exception class
 */
class BadFormatException extends RuntimeException {
	private static final long serialVersionUID = 7643118853223682701L;

	BadFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	BadFormatException(String message) {
		super(message);
	}
}

class OldSystemRow {
	final char type;
	final String[] args;

	OldSystemRow(String line) {
		if (line.length() <= 2)
			throw new BadFormatException("Line must have a minimum length of 3");
		if (line.charAt(1) != '|')
			throw new BadFormatException("The second character must be '|'");

		type = line.charAt(0);
		args = line.substring(2).split("\\|");
	}

	String part(int index) {
		if (index >= args.length)
			return null;
		return args[index].trim();
	}
}

class FamilyMember {
	final String firstName;
	final String born;

	Address address;
	Phone phone;

	FamilyMember(String firstName, String born) {
		this.firstName = firstName;
		this.born = born;
	}

	XMLElement toXML() {
		XMLElement res = new XMLElement("family");

		if (firstName != null)
			res.addChildValue("firstname", firstName);
		if (born != null)
			res.addChildValue("born", born);
		if (address != null)
			res.addChild(address.toXML());
		if (phone != null)
			res.addChild(phone.toXML());

		return res;
	}
}

class Person {
	final String firstName;
	final String lastName;

	Address address;
	Phone phone;

	final List<FamilyMember> family;

	Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;

		family = new LinkedList<>();
	}

	XMLElement toXML() {
		XMLElement res = new XMLElement("person");

		if (firstName != null)
			res.addChildValue("firstname", firstName);
		if (lastName != null)
			res.addChildValue("lastname", lastName);
		if (address != null)
			res.addChild(address.toXML());
		if (phone != null)
			res.addChild(phone.toXML());

		for (FamilyMember member : family)
			res.addChild(member.toXML());

		return res;
	}
}

class Address {
	final String street;
	final String city;
	final String zip;

	Address(String street, String city, String zip) {
		this.street = street;
		this.city = city;
		this.zip = zip;
	}

	XMLElement toXML() {
		XMLElement result = new XMLElement("address");

		if (street != null)
			result.addChildValue("street", street);
		if (city != null)
			result.addChildValue("city", city);
		if (zip != null)
			result.addChildValue("zip", zip);

		return result;
	}
}

class Phone {
	final String mobile;
	final String landline;

	Phone(String mobile, String landline) {
		this.mobile = mobile;
		this.landline = landline;
	}

	XMLElement toXML() {
		XMLElement result = new XMLElement("phone");

		if (mobile != null)
			result.addChildValue("mobile", mobile);
		if (landline != null)
			result.addChildValue("landline", landline);

		return result;
	}
}
