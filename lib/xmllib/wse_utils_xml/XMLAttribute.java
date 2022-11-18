package wse.utils.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.w3c.dom.Node;

import wse.utils.internal.StringGatherer;

public class XMLAttribute extends XMLNode {

	public XMLAttribute() {
		super(null);
	}

	public XMLAttribute(String name, String value) {
		super(name);
		this.setValue(value);
		if (name != null)
			setName(name);
	}

	public XMLAttribute(String name, String value, String namespace) {
		super(name, namespace);
		setValue(value);
		if (name != null)
			setName(name);
	}

	public static XMLAttribute namespace(String name, String namespace) {
		if (name == null)
			name = "xmlns";
		XMLAttribute a = new XMLAttribute(name, namespace, XMLUtils.XMLNS);
		if (!"xmlns".equals(name)) {
			a.prefix = "xmlns";
		}
		return a;
	}

	public void setDefaultNamespace() {
		if (!isNamespaceDeclaration())
			throw new IllegalStateException("This xml attribute is not a namespace declaration");

		this.name = "xmlns";
		this.prefix = null;
	}

	@Override
	public void setName(String name) {
		if ("xmlns".equals(name)) {
			setNamespaceURI(XMLUtils.XMLNS, false);
			setDefaultNamespace();
		}
		super.setName(name);
	}

	@Override
	public void setPrefix(String prefix) {
		if (prefix != null) {
			if ("xmlns".equals(prefix))
				setNamespaceURI(XMLUtils.XMLNS);
		}
		super.setPrefix(prefix);
	}

	public boolean isNamespaceDeclaration() {
		return XMLUtils.XMLNS.equals(this.namespace);
	}

	public boolean isDefaultNamespace() {
		return isNamespaceDeclaration() && this.prefix == null && "xmlns".equals(this.name);
	}

	@Override
	public boolean setNamespaceURI(String namespaceURI, boolean solvePrefix) {
		if (XMLUtils.XMLNS.equals(namespaceURI))
			return super.setNamespaceURI(XMLUtils.XMLNS, false);
		return super.setNamespaceURI(namespaceURI, solvePrefix);
	}

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int length() {
		int len = 0;
		if (this.prefix != null)
			len += this.prefix.length();
		if (this.name != null && !this.isDefaultNamespace())
			len += this.name.length();
		if (this.value != null)
			len += XMLUtils.escape(this.value).length();
		return len;
	}

	public void write(OutputStream stream, Charset charset, int level) throws IOException {
		stream.write(XMLUtils.level(level));
		writeQualifiedName(stream, charset);
		stream.write('=');
		stream.write('"');
		if (value != null) {
			byte[] data = XMLUtils.escape(value).getBytes(charset);
			stream.write(data);
		}
		stream.write('"');
	}

	@Override
	public void prettyPrint(StringGatherer builder, int level) {
		builder.add(XMLUtils.level_str(level));

		if (isDefaultNamespace()) {
			builder.add("xmlns");
		} else {
			builder.add(getQualifiedName());
		}

		builder.add("=\"");
		if (value != null) {
			builder.add(XMLUtils.escape(value));
		}
		builder.add("\"");

	}

	public void writeQualifiedName(OutputStream stream, Charset charset) throws IOException {
		if (isDefaultNamespace()) {
			stream.write("xmlns".getBytes(charset));
		} else {
			stream.write(getQualifiedName().getBytes(charset));
		}
	}

	public String findPrefix(String namespaceURI) {
		String ns = (parent != null) ? parent.findPrefix(namespaceURI) : null;
		if (ns != null && ns.isEmpty()) {
			ns = null;
		}
		return ns;
	}

	public static XMLAttribute parseNode(Node node) {
		if (node.getNodeType() != Node.ATTRIBUTE_NODE)
			throw new IllegalStateException(
					"Could not convert Node of type " + XMLUtils.nodeTypeStr(node.getNodeType()) + " to Attribute");
		String[] nodeName = node.getNodeName().split(":", 2);

		XMLAttribute res = new XMLAttribute();
		res.prefix = node.getPrefix();
		res.namespace = node.getNamespaceURI();
		res.name = nodeName[nodeName.length - 1];

		res.value = node.getTextContent();

		return res;
	}

	protected String[] ns() {
		return new String[] { isDefaultNamespace() ? null : name, value };
	}

}
