package wse.utils.internal;

import wse.utils.writable.StreamWriter;

public interface PrettyPrinter extends StreamWriter {
	public StringGatherer prettyPrint();

	public StringGatherer prettyPrint(int level);

	void prettyPrint(StringGatherer builder, int level);
}
