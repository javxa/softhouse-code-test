package wse.utils.xml;

import wse.utils.collections.CollectionController;

public class XMLHierarchyController<T extends XMLNode> implements CollectionController<T> {

	protected XMLElement parentNode;

	public XMLHierarchyController(XMLElement parentNode) {
		this.parentNode = parentNode;
	}

	@Override
	public void onEntryAdded(T entry) {
		if (entry != null)
			onNodeAdded(entry);
	}

	@Override
	public void onEntryRemoved(T removed) {
		if (removed != null)
			onNodeRemoved(removed);
	}

	protected void onNodeRemoved(T node) {
		node.__orphan();
	}

	protected void onNodeAdded(T node) {
		node.parent = this.parentNode;
		node.propagateTree(this.parentNode.tree);
	}

}
