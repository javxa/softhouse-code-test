package wse.utils.internal;

import java.util.Collection;

public interface IElement extends ILeaf {

	public IElement createEmpty();

	public IElement getChild(String key);

	public IElement getChild(String key, String namespace);

	public <T extends IElement> Collection<T> getChildArray(String key);

	public <T extends IElement> Collection<T> getChildArray(String key, String namespace);

	public void setChild(String key, IElement child);

	public void setChild(String key, String namespace, IElement child);

	public void setChildArray(String key, Iterable<IElement> children);

	public void setChildArray(String key, String namespace, Iterable<IElement> children);

	public void setName(String name);

	public void setName(String name, String namespace);

}
