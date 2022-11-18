package wse.utils.xml;

import java.util.ArrayList;
import java.util.Collection;

public class XMLNamedNodeMap<T extends XMLNode> extends XMLNodeList<T> {

	public XMLNamedNodeMap(XMLElement node) {
		super(new ArrayList<T>(), new XMLHierarchyController<T>(node));
	}

	@Override
	public void add(int index, T element) {
		int i;
		if ((i = __indexOf(element.name, element.namespace)) != -1) {
			if (i == index) {
				set(index, element);
			} else if (i < index) {
				remove(i);
				super.add(index - 1, element);
			} else {
				remove(i);
				super.add(index, element);
			}
		} else {
			super.add(index, element);
		}
	}

	@Override
	public boolean add(T e) {
		int i;
		if ((i = __indexOf(e.name, e.namespace)) != -1) {
			this.set(i, e);
			return true;
		} else {
			return super.add(e);
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean any = false;
		for (T t : c)
			any = add(t) || any;
		return any;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return addAll(c);
	}

}
