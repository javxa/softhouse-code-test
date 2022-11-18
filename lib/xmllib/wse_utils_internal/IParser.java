package wse.utils.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import wse.utils.options.IOptions;

public abstract class IParser<T extends ILeaf> {
	public final T parse(InputStream input, Charset cs) throws IOException {
		return parse(input, cs, null);
	}

	public abstract T parse(InputStream input, Charset cs, IOptions options) throws IOException;

	public abstract T createEmpty();
}
