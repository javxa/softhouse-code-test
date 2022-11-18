package wse.utils.internal;

import java.nio.charset.Charset;
import java.util.Collection;

import wse.utils.MimeType;
import wse.utils.writable.StreamWriter;

public interface ILeaf extends HasRowColumn, StreamWriter, PrettyPrinter {

	public MimeType getMimeType();

	public Charset preferredCharset();

	public void preparePrint();

	// Values

	public String getValue(String key);

	public String getValue(String key, String namespace);

	public void setValue(String key, Object value);

	public void setValue(String key, String namespace, Object value);

	/// Array

	public Collection<String> getValueArray(String key);

	public Collection<String> getValueArray(String key, String namespace);

	public void setValueArray(String key, Iterable<Object> value);

	public void setValueArray(String key, String namespace, Iterable<Object> value);

	// Attributes

	public String getAttributeValue(String key);

	public String getAttributeValue(String key, String namespace);

	public void setAttributeValue(String key, Object value);

	public void setAttributeValue(String key, String namespace, Object value);

	/// Array

	public Collection<String> getAttributeValueArray(String key);

	public Collection<String> getAttributeValueArray(String key, String namespace);

	public void setAttributeValueArray(String key, Iterable<Object> value);

	public void setAttributeValueArray(String key, String namespace, Iterable<Object> value);

}
