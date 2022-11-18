package wse.utils.internal;

import java.io.IOException;
import java.io.Reader;

public class PushableReader extends Reader implements HasRowColumn {

	private Integer push;
	private final Reader source;
	private int row = 1, column;

	private boolean end = false;

	private static final int NL = '\n';

	public PushableReader(Reader source) {
		this.source = source;
	}

	@Override
	public int read() throws IOException {

		int c;

		if (push != null) {
			c = push;
			push = null;
		} else {
			c = source.read();
		}

		if (c <= 0) {
			end = true;
			return 0;
		}

		if (c == NL) {
			row++;
			column = 0;
		} else {
			column++;
		}

		return c;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		source.close();
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	public void push(int c) {
		if (push != null) {
			throw new IllegalStateException("Can't push twice");
		}
		push = c;
	}

	public boolean end() {
		return end && push == null;
	}

}
