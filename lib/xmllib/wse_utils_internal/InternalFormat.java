package wse.utils.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import wse.utils.MimeType;
import wse.utils.exception.WseBuildingException;
import wse.utils.exception.WseParsingException;
import wse.utils.ini.IniUtils;
import wse.utils.json.JUtils;
import wse.utils.options.IOptions;
import wse.utils.xml.XMLUtils;

public class InternalFormat {
	private static final Map<MimeType, IParser<? extends ILeaf>> registered = new HashMap<>();

	static {
		InternalFormat.registerParser(JUtils.JSON_PARSER, MimeType.application.json);
		InternalFormat.registerParser(XMLUtils.XML_PARSER, MimeType.application.xml, MimeType.text.xml);
		InternalFormat.registerParser(IniUtils.INI_PARSER, MimeType.application.textedit,
				MimeType.application.zz_winassoc_ini, MimeType.text.plain);
	}

	public static void registerParser(IParser<? extends ILeaf> parser, MimeType... types) {

		if (parser == null)
			throw new NullPointerException("parser == null");

		if (types.length == 0) {

			ILeaf il = parser.createEmpty();
			if (il == null)
				return;

			MimeType mt = il.getMimeType();
			if (mt == null)
				return;

			registered.put(mt, parser);
			return;
		}

		for (MimeType mt : types) {
			if (mt != null)
				registered.put(mt, parser);
		}
	}

	public static ILeaf parse(File file, Charset cs) throws FileNotFoundException, IOException {
		MimeType mt = MimeType.getByExtension(file);
		try (InputStream is = new FileInputStream(file)) {
			return parse(mt, is, cs);
		}
	}

	public static ILeaf parse(MimeType mt, InputStream input, Charset cs) throws IOException {
		return parse(mt, input, cs, null);
	}

	public static ILeaf parse(MimeType mt, InputStream input, Charset cs, IOptions options) throws IOException {
		IParser<? extends ILeaf> parser = registered.get(mt);

		if (parser == null)
			throw new WseParsingException(
					String.format("No suitable parser found for mimetype '%s'", mt.getFullName()));

		return parser.parse(input, cs, options);
	}

	public static ILeaf createEmpty(MimeType mt) {
		IParser<? extends ILeaf> parser = registered.get(mt);

		if (parser == null)
			throw new WseBuildingException(
					String.format("No suitable builder found for mimetype '%s'", mt.getFullName()));

		return parser.createEmpty();
	}

}
