package wse.utils.xml;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class EmptyErrorListener implements ErrorListener {
	public void warning(TransformerException exception) throws TransformerException {
	}

	public void error(TransformerException exception) throws TransformerException {
	}

	public void fatalError(TransformerException exception) throws TransformerException {
	}

}
