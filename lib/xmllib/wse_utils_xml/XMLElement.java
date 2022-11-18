package wse.utils.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import wse.utils.MimeType;
import wse.utils.exception.XMLException;
import wse.utils.internal.IElement;
import wse.utils.internal.StringGatherer;
import wse.utils.writable.StreamCatcher;

public class XMLElement extends XMLNode implements IElement {

	protected byte[] value;
	protected boolean cdata;

	protected final XChildList children = new XChildList();
	protected final XAttributeList attributes = new XAttributeList();

	protected LocationData parsedLocation;

	public XMLElement() {
		this(null);
	}

	public XMLElement(String name) {
		super(name);
	}

	public XMLElement(String name, String namespace) {
		super(name, namespace);
	}

	public XMLElement(String prefix, String name, String namespace) {
		super(prefix, name, namespace);
	}

	public LocationData getLocationData() {
		return parsedLocation;
	}

	/**
	 * returns this xml elements index in its parent's element list or -1 if this
	 * element has no parent
	 */
	public int index() {
		if (parent != null)
			return -1;
		return parent.getChildren().indexOf(this);
	}

	public String getValue() {
		return value == null ? null : new String(value, tree.getCharset());
	}

	public byte[] getRawValue() {
		return value;
	}

	public XMLElement setValue(String value) {
		return setValue(value, false);
	}

	public <T> XMLElement setValue(T value) {
		return setValue(String.valueOf(value), false);
	}

	public <T> XMLElement setValue(T value, boolean cdata) {
		return setValue(String.valueOf(value), cdata);
	}

	public XMLElement setValue(String value, boolean cdata) {
		if (cdata) {
			this.value = value.getBytes();
		} else {
			this.value = value.getBytes(tree.getCharset());
		}
		return this;
	}

	public XMLElement setValueRaw(byte[] value) {
		return setValueRaw(value, false);
	}

	public XMLElement setValueRaw(byte[] value, boolean cdata) {
		this.value = value;
		this.cdata = cdata;
		return this;
	}

	public XMLElement writeAsCDATA(boolean cdata) {
		this.cdata = cdata;
		return this;
	}

	public boolean hasCDATA() {
		return cdata;
	}

	public void propagateTree(XMLTree tree) {
		super.propagateTree(tree);
		for (XMLNode n : children)
			n.propagateTree(tree);
		for (XMLNode n : attributes)
			n.propagateTree(tree);
	}

	public XChildList getChildren() {
		return children;
	}

	public XAttributeList getAttributes() {
		return attributes;
	}

	public XMLElement getChild(String name) {
		return children.getFirst(name);
	}

	public XMLElement getChild(String name, String namespace) {
		return children.getFirst(name, namespace);
	}

	public Collection<XMLElement> getChildren(String name) {
		return children.getAll(name);
	}

	public Collection<XMLElement> getChildren(String name, String namespace) {
		return children.getAll(name, namespace);
	}

	public XMLAttribute getAttribute(String name) {
		return attributes.getFirst(name);
	}

	public List<XMLAttribute> getNamespaceDeclarations() {
		return getAttributesNS(XMLUtils.XMLNS);
	}

	public List<XMLAttribute> getAttributesNS(String namespace) {
		return attributes.getAllNS(namespace);
	}

	public List<XMLAttribute> getAttributes(String name) {
		return attributes.getAll(name);
	}

	public XMLAttribute getAttribute(String name, String namespace) {
		return attributes.getFirst(name, namespace);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public boolean hasValue() {
		return value != null && value.length != 0;
	}

	public boolean hasAttributes() {
		return attributes.size() != 0;
	}

	public byte[] toByteArray() {
		return StreamCatcher.from(this, getTree().getCharset()).toByteArray();
	}

	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		tree.write(stream, charset, 0);
		write(stream, charset, 0);
	}

	public void write(OutputStream stream, Charset charset, int level) throws IOException {
		byte[] tabs = XMLUtils.level(level);
		stream.write(tabs);
		stream.write('<');
		byte[] qname = getQualifiedName().getBytes(charset);
		stream.write(qname);

		if (hasAttributes()) {
			Iterator<XMLAttribute> it = attributes.iterator();
			int len = 0;

			XMLAttribute att;
			while (it.hasNext() && len < 30 && (att = it.next()) != null) {
				stream.write(' ');
				att.write(stream, charset, 0);
				len += att.length();
			}

			while (it.hasNext() && (att = it.next()) != null) {
				stream.write('\n');
				att.write(stream, charset, level + 2);
			}
		}

		if (hasChildren()) {
			stream.write('>');
			stream.write('\n');

			for (XMLElement child : children) {
				child.write(stream, charset, level + 1);
			}

			stream.write(tabs);
			stream.write('<');
			stream.write('/');
			stream.write(qname);
		} else if (hasValue()) {
			stream.write('>');

			if (cdata) {
				stream.write(XMLUtils.convertToCDATA(value));
			} else {
				stream.write(XMLUtils.escape(getValue()).getBytes(charset));
			}
			stream.write('<');
			stream.write('/');
			stream.write(qname);
		} else {
			stream.write('/');
		}
		stream.write('>');
		stream.write('\n');
	}

	@Deprecated
	@Override
	public void prettyPrint(StringGatherer builder, int level) {

		String tabs = XMLUtils.level_str(level);

		builder.add(tabs);
		builder.add("<");
		String qname = getQualifiedName();

		builder.add(qname);

		if (hasAttributes()) {
			Iterator<XMLAttribute> it = attributes.iterator();
			int len = builder.length();

			XMLAttribute att;
			while (it.hasNext() && (builder.length() - len) < 30 && (att = it.next()) != null) {
				builder.add(" ");
				att.prettyPrint(builder, 0);
			}

			while (it.hasNext() && (att = it.next()) != null) {
				builder.add("\n");
				att.prettyPrint(builder, level + 2);
			}
		}

		if (hasChildren()) {
			builder.add(">\n");

			for (XMLElement child : children) {
				child.prettyPrint(builder, level + 1);
			}

			builder.add(tabs);
			builder.add("</");
			builder.add(qname);
		} else if (hasValue()) {
			builder.add(">");

			if (cdata) {
				builder.add(new String(XMLUtils.convertToCDATA(value), tree.getCharset()));
			} else {
				builder.add(XMLUtils.escape(getValue()));
			}
			builder.add("</");
			builder.add(qname);
		} else {
			builder.add("/");
		}
		builder.add(">\n");
	}

	public XMLElement addChild(XMLElement node) {
		children.add(node);
		return node;
	}

	public XMLElement addChild(String name) {
		return addChild(new XMLElement(name));
	}

	public XMLElement addChild(String name, String namespace) {
		return addChild(new XMLElement(name, namespace));
	}

	public <T> XMLElement addChildValue(String name, T value) {
		return addChildValue(name, null, value);
	}

	public <T> XMLElement addChildValue(String name, String namespace, T value) {
		return addChild(new XMLElement(name, namespace).setValue(String.valueOf(value)));
	}

	public <T> void addChildValues(String name, T[] values) {
		for (T value : values)
			addChildValue(name, value);
	}

	public <T> void addChildValues(String name, String namespace, T[] values) {
		for (T value : values)
			addChildValue(name, namespace, value);
	}

	public <T> void addChildValues(String name, Iterable<T> values) {
		for (T value : values)
			addChildValue(name, value);
	}

	public <T> void addChildValues(String name, String namespace, Iterable<T> values) {
		for (T value : values)
			addChildValue(name, namespace, value);
	}

	public String getChildValue(String name) {
		XMLElement child = getChild(name);
		if (child != null)
			return child.getValue();
		return null;
	}

	public String getChildValue(String name, String namespace) {
		XMLElement child = getChild(name, namespace);
		if (child != null)
			return child.getValue();
		return null;
	}

	public String getChildValueDef(String name, String defValue) {
		String result = getChildValue(name);
		return result == null ? defValue : result;
	}

	public String getChildValueDef(String name, String namespace, String defValue) {
		String result = getChildValue(name, namespace);
		return result == null ? defValue : result;
	}

	public Collection<String> getChildValues(String name) {
		return XMLUtils.values(getChildren(name));
	}

	public Collection<String> getChildValues(String name, String namespace) {
		return XMLUtils.values(getChildren(name, namespace));
	}

	public XMLAttribute addAttribute(XMLAttribute attribute) {
		attributes.add(attribute);
		return attribute;
	}

	public XMLAttribute addAttribute(String name, String value) {
		return addAttribute(new XMLAttribute(name, value));
	}

	public XMLAttribute addAttribute(String name, String value, String namespace) {
		return addAttribute(new XMLAttribute(name, value, namespace));
	}

	public String getAttributeValue(String name) {
		XMLAttribute a = getAttribute(name);
		if (a != null)
			return a.getValue();
		return null;
	}

	public String getAttributeValueDef(String name, String defaultValue) {
		XMLAttribute a = getAttribute(name);
		if (a != null)
			return a.getValue();
		return defaultValue;
	}

	public String getAttributeValue(String name, String namespace) {
		XMLAttribute a = getAttribute(name, namespace);
		if (a != null)
			return a.getValue();
		return null;
	}

	public String getAttributeValueDef(String name, String namespace, String defaultValue) {
		XMLAttribute a = getAttribute(name, namespace);
		if (a != null)
			return a.getValue();
		return defaultValue;
	}

	public XMLAttribute declareNamespace(String name, String namespace) {
		return addAttribute(XMLAttribute.namespace(name, namespace));
	}

	public XMLElement getChildOfNames(String namespace, String... names) {
		for (XMLElement c : getChildren()) {
			if (Objects.equals(namespace, c.getNamespaceURI())) {
				for (String name : names)
					if (Objects.equals(name, c.getName()))
						return c;
			}
		}
		return null;
	}

	public List<XMLElement> getChildrenOfNames(String namespace, String... names) {
		List<XMLElement> result = new LinkedList<>();
		for (XMLElement c : getChildren()) {
			if (Objects.equals(namespace, c.getNamespaceURI())) {
				for (String name : names)
					if (Objects.equals(name, c.getName()))
						result.add(c);
			}
		}
		return result;
	}

	public XMLElement getFirstParent(String name) {
		if (Objects.equals(this.name, name))
			return this;
		if (parent != null)
			return parent.getFirstParent(name);
		return null;
	}

	public XMLElement getFirstParent(String name, String namespace) {
		if (Objects.equals(this.name, name) && Objects.equals(this.namespace, namespace))
			return this;
		if (parent != null)
			return parent.getFirstParent(name, namespace);
		return null;
	}

	public String findDefaultNamespace() {
		return findNamespaceURI(null);
	}

	public String findNamespaceURI(String prefix) {
		for (XMLAttribute attrib : this.attributes) {
			if (attrib.isNamespaceDeclaration()
					&& ((prefix == null && attrib.isDefaultNamespace()) || Objects.equals(prefix, attrib.getName()))) {
				return attrib.getValue();
			}
		}
		if (parent != null)
			return parent.findNamespaceURI(prefix);
		return null;
	}

	public String findPrefix(String namespaceURI) {
		for (XMLAttribute attrib : this.attributes) {
			if (attrib.isNamespaceDeclaration() && Objects.equals(namespaceURI, attrib.getValue())) {
				if ("xmlns".equals(attrib.name))
					return "";
				else
					return attrib.getName();
			}
		}

		if (parent != null)
			return parent.findPrefix(namespaceURI);
		return null;
	}

	public class XChildList extends XMLNodeList<XMLElement> {
		protected XChildList() {
			super(XMLElement.this);
		}
	}

	public class XAttributeList extends XMLNamedNodeMap<XMLAttribute> {
		protected XAttributeList() {
			super(XMLElement.this);
		}
	}

	public static XMLElement parseNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE)
			throw new IllegalStateException(
					"Could not convert Node of type " + XMLUtils.nodeTypeStr(node.getNodeType()) + " to Element");

		String[] nodeName = node.getNodeName().split(":", 2);

		XMLElement res = new XMLElement();
		res.prefix = node.getPrefix();
		res.namespace = node.getNamespaceURI();
		res.name = nodeName[nodeName.length - 1];
		res.parsedLocation = (LocationData) node.getUserData(LocationData.LOCATION_DATA_KEY);

		boolean cdata = false;

		for (Node e = node.getFirstChild(); e != null; e = e.getNextSibling()) {
			if (e.getNodeType() == Node.ELEMENT_NODE)
				res.addChild(parseNode(e));
			else if (e.getNodeType() == Node.CDATA_SECTION_NODE) {
				cdata = true;
			}
		}

		if (!res.hasChildren()) {
			res.setValue(node.getTextContent(), cdata);
		}

		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			res.addAttribute(XMLAttribute.parseNode(attributes.item(i)));
		}

		return res;
	}

	public Iterable<XMLElement> treeIterable() {
		return XMLHierarchyIterator.iterable(this);
	}

	//
	// IElement compatible
	//

	@Override
	public int getRow() {
		return 0;
	}

	@Override
	public int getColumn() {
		return 0;
	}

	@Override
	public String getValue(String key) {
		return getValue(key, null);
	}

	@Override
	public String getValue(String key, String namespace) {
		XMLElement child = getChild(key, namespace);
		if (child == null) {
			XMLAttribute a = getAttribute(key, namespace);
			if (a == null)
				return null;
			return a.getValue();
		}
		return child.getValue();
	}

	@Override
	public Collection<String> getValueArray(String key) {
		return getChildValues(key);
	}

	@Override
	public Collection<String> getValueArray(String key, String namespace) {
		return getChildValues(key, namespace);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<XMLElement> getChildArray(String key) {
		return getChildren(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<XMLElement> getChildArray(String key, String namespace) {
		return getChildren(key, namespace);
	}

	@Override
	public XMLElement createEmpty() {
		return new XMLElement();
	}

	@Override
	public void setValue(String key, Object value) {
		setValue(key, null, value);
	}

	@Override
	public void setValue(String key, String namespace, Object value) {
		addChildValue(key, namespace, value);
	}

	@Override
	public void setValueArray(String key, Iterable<Object> value) {
		setValueArray(key, null, value);
	}

	@Override
	public void setValueArray(String key, String namespace, Iterable<Object> value) {
		addChildValues(key, namespace, value);
	}

	@Override
	public void setAttributeValue(String key, Object value) {
		setAttributeValue(key, null, value);
	}

	@Override
	public void setAttributeValue(String key, String namespace, Object value) {
		addAttribute(key, String.valueOf(value), namespace);
	}

	@Override
	public void setAttributeValueArray(String key, Iterable<Object> value) {
		setAttributeValueArray(key, null, value);
	}

	@Override
	public void setAttributeValueArray(String key, String namespace, Iterable<Object> value) {
		setValueArray(key, namespace, value);
	}

	@Override
	public void setChild(String key, IElement child) {
		setChild(key, null, child);
	}

	@Override
	public void setChild(String key, String namespace, IElement child) {
		if (!(child instanceof XMLElement))
			throw new XMLException(String.format("Tried to add invalid child element type '%s', expected '%s'",
					child.getClass().getName(), XMLElement.class.getName()));

		XMLElement xmlChild = (XMLElement) child;
		xmlChild.setName(key);
		xmlChild.setNamespaceURI(namespace, false);

		addChild(xmlChild);
	}

	@Override
	public void setChildArray(String key, Iterable<IElement> children) {
		setChildArray(key, null, children);
	}

	@Override
	public void setChildArray(String key, String namespace, Iterable<IElement> children) {
		for (IElement child : children) {
			setChild(key, namespace, child);
		}
	}

	@Override
	public MimeType getMimeType() {
		return MimeType.application.xml;
	}

	@Override
	public Charset preferredCharset() {
		return getTree().getCharset();
	}

	@Override
	public Collection<String> getAttributeValueArray(String key) {
		return getAttributeValueArray(key, null);
	}

	@Override
	public Collection<String> getAttributeValueArray(String key, String namespace) {
		return getValueArray(key, namespace);
	}

	@Override
	public void setName(String name, String namespace) {
		this.setName(name);
		this.setNamespaceURI(namespace);
	}

	@Override
	public void preparePrint() {
		XMLUtils.resetNamespaces(this);
	}
}
