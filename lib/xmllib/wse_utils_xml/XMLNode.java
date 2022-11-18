package wse.utils.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import wse.utils.ArrayUtils;
import wse.utils.internal.PrettyPrinter;
import wse.utils.internal.StringGatherer;

public abstract class XMLNode implements PrettyPrinter {

	protected XMLTree tree = new XMLTree();
	protected XMLElement parent;
	protected String prefix, name, namespace;

	public XMLNode(String name) {
		this(name, null);
	}

	public XMLNode(String name, String namespace) {
		this(null, name, namespace);
	}

	public XMLNode(String prefix, String name, String namespace) {
		this.prefix = prefix;
		this.name = name;
		this.namespace = namespace;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public void setPrefix(String prefix) {
		setPrefix(prefix, false);
	}

	public boolean setPrefix(String prefix, boolean solveNamespaceURI) {
		this.prefix = prefix == null ? null : prefix.trim();
		if (this.prefix != null && this.prefix.isEmpty())
			this.prefix = null;

		if (solveNamespaceURI) {
			return solveNamespace();
		} else {
			return false;
		}
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	public String getQualifiedName() {
		return ArrayUtils.joinFilterEmpty(":", this.getPrefix(), this.getName());
	}

	public void setNamespaceURI(String namespaceURI) {
		setNamespaceURI(namespaceURI, false);
	}

	public boolean setNamespaceURI(String namespaceURI, boolean solvePrefix) {
		this.namespace = namespaceURI;
		if (this.namespace != null && this.namespace.isEmpty())
			this.namespace = null;
		if (solvePrefix) {
			return solvePrefix();
		} else {
			return false;
		}
	}

	public boolean solvePrefix() {
		String p = findPrefix(this.namespace);
		setPrefix(p);
		return p != null;
	}

	public boolean solveNamespace() {
		String ns = findNamespaceURI(this.prefix);
		setNamespaceURI(ns, false);
		return ns != null;
	}

	/**
	 * Will traverse namespace declarations of this node and parents and return the
	 * closest that matches the given prefix, or the closest default namespace if
	 * given prefix is null
	 * 
	 * @param prefix
	 * @return
	 */
	public String findNamespaceURI(String prefix) {
		if (parent != null)
			return parent.findNamespaceURI(prefix);
		return null;
	}

	/**
	 * Will traverse namespace declarations of this node and parents
	 * 
	 * @param namespaceURI
	 * @return An empty string if the closest default namespace matches, the prefix
	 *         of a matching namespace declaration, or null if there was no matching
	 *         namespace, in that order.
	 */
	public String findPrefix(String namespaceURI) {
		if (parent != null)
			return parent.findPrefix(namespaceURI);
		return null;
	}

	public String getNamespaceURI() {
		return this.namespace;
	}

	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		prettyPrint().writeToStream(stream, charset);
	}

	@Deprecated
	public abstract void write(OutputStream stream, Charset charset, int level) throws IOException;

	public void writeQualifiedName(OutputStream stream) throws IOException {
		stream.write(getQualifiedName().getBytes(tree.getCharset()));
	}

	protected final void __orphan() {
		this.parent = null;
		this.propagateTree(new XMLTree());
	}

	public final void orphan() {
		if (parent != null) {
			if (this instanceof XMLElement) {
				parent.children.remove(this);
			} else if (this instanceof XMLAttribute) {
				parent.attributes.remove(this);
			}
		}
		__orphan();
	}

	public XMLTree getTree() {
		return this.tree;
	}

	public void propagateTree(XMLTree tree) {
		this.tree = tree;
	}

	@Override
	public String toString() {
		return prettyPrint().toString();
	}

	@Override
	public StringGatherer prettyPrint() {
		return prettyPrint(0);
	}

	@Override
	public StringGatherer prettyPrint(int level) {
		StringGatherer gs = new StringGatherer();
		prettyPrint(gs, level);
		return gs;
	}
}
