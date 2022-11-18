package wse.utils.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import wse.utils.collections.CollectionController;
import wse.utils.collections.ControlledList;

public class XMLNodeList<T extends XMLNode> extends ControlledList<T> {

	protected XMLNodeList(XMLElement node) {
		this(new ArrayList<T>(), new XMLHierarchyController<T>(node));
	}

	protected XMLNodeList(List<T> list, CollectionController<T> controller) {
		super(list, controller);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return super.addAll(c);
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof String) {
			return remove((String) o);
		}
		return super.remove(o);
	}

	public boolean remove(String name) {
		int i;
		if ((i = __indexOf(name)) != -1) {
			remove(i);
			return true;
		}
		return false;
	}

	public boolean remove(String name, String namespace) {
		int i;
		if ((i = __indexOf(name, namespace)) != -1) {
			remove(i);
			return true;
		}
		return false;
	}

	protected int __indexOf(String name) {
		if (name == null)
			return -1;
		int i = 0;
		for (T t : this.internal) {
			if (Objects.equals(t.name, name))
				return i;
			i++;
		}
		return -1;
	}

	protected int __indexOf(String name, String namespace) {
		if (name == null)
			return -1;
		int i = 0;
		for (T t : this.internal) {
			if (Objects.equals(t.name, name) && Objects.equals(t.namespace, namespace))
				return i;
			i++;
		}
		return -1;
	}

	public T getFirst(String name) {
		if (name == null)
			return null;
		for (T t : this.internal) {
			if (Objects.equals(t.name, name))
				return t;
		}
		return null;
	}

	public T getFirstNS(String namespace) {
		for (T t : this.internal) {
			if (Objects.equals(t.namespace, namespace))
				return t;
		}
		return null;
	}

	public T getFirst(String name, String namespace) {
		if (name == null)
			return null;
		if (namespace == null)
			return getFirst(name);
		for (T t : this.internal) {
			if (Objects.equals(t.name, name) && Objects.equals(t.namespace, namespace))
				return t;
		}
		return null;
	}

	public List<T> getAll(String name) {
		List<T> list = new LinkedList<>();
		for (T t : this.internal)
			if (Objects.equals(t.name, name))
				list.add(t);
		return list;
	}

	public List<T> getAllNS(String namespace) {
		List<T> list = new LinkedList<>();
		for (T t : this.internal)
			if (Objects.equals(t.namespace, namespace))
				list.add(t);
		return list;
	}

	public List<T> getAll(String name, String namespace) {
		List<T> list = new LinkedList<>();
		for (T t : this.internal)
			if (Objects.equals(t.name, name) && Objects.equals(t.namespace, namespace))
				list.add(t);
		return list;
	}
}
