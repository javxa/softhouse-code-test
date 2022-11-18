package wse.utils.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import wse.utils.writable.StreamWriter;

public class XMLTree implements StreamWriter {
	private String version;
	private String encoding;
	private boolean standalone;
	private Charset charset;

	public XMLTree() {
		version = "1.0";
		encoding = "UTF-8";
		standalone = false;
		charset = Charset.forName(this.encoding);
	}

	public XMLTree(XMLTree copy) {
		version = copy.version;
		encoding = copy.encoding;
		charset = copy.charset;
		standalone = copy.standalone;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isStandalone() {
		return standalone;
	}

	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
	}

	public String getEncoding() {
		return encoding;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
		this.charset = Charset.forName(encoding);
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		write(stream, charset, 0);
	}

	public void write(OutputStream stream, Charset charset, int level) throws IOException {

		stream.write(XMLUtils.level(level));
		stream.write("<?xml version=\"".getBytes(charset));
		stream.write(version.getBytes(charset));
		stream.write("\" encoding=\"".getBytes(charset));
		stream.write(encoding.getBytes(charset));
		stream.write("\"?>\n".getBytes(charset));
	}
}
