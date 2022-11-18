package wse.utils.xml;

import java.util.Iterator;

public class XMLHierarchyIterator {

	public static Iterable<XMLElement> iterable(final XMLElement root) {
		return new Iterable<XMLElement>() {

			@Override
			public Iterator<XMLElement> iterator() {
				return XMLHierarchyIterator.iterator(root);
			}
		};
	}

	public static Iterator<XMLElement> iterator(final XMLElement root) {

		return new Iterator<XMLElement>() {
			XMLElement first = root;
			final Iterator<XMLElement> children = root.getChildren().iterator();
			Iterator<XMLElement> currentChild = null;

			@Override
			public boolean hasNext() {
				if (first != null) {
					return true;
				}
				if (children.hasNext())
					return true;
				if (currentChild != null)
					return currentChild.hasNext();
				return false;
			}

			@Override
			public XMLElement next() {
				if (first != null) {
					try {
						return first;
					} finally {
						first = null;
					}
				}
				if (currentChild != null && currentChild.hasNext()) {
					return currentChild.next();
				}

				if (!children.hasNext())
					return null;
				XMLElement next = children.next();
				if (next == null)
					return this.next();

				currentChild = iterator(next);
				return this.next();
			}

			@Override
			public void remove() {

			}
		};

	}

}
