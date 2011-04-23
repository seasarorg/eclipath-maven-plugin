/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.uruma.eclipath;

import static org.seasar.uruma.eclipath.Constants.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;
import org.seasar.uruma.eclipath.model.ClasspathKind;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * The class dealing with the Eclipse .classpath file.
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class EclipseClasspath {
    private static final String DOT_CLASSPATH_FILENAME = ".classpath";

    public static final String ELEMENT_CLASSPATH = "classpath";

    public static final String ELEMENT_CLASSPATHENTRY = "classpathentry";

    public static final String ELEMENT_ATTRIBUTES = "attributes";

    public static final String ELEMENT_ATTRIBUTE = "attribute";

    public static final String ATTR_KIND = "kind";

    public static final String ATTR_SOURCEPATH = "sourcepath";

    public static final String ATTR_VALUE = "value";

    public static final String ATTR_NAME = "name";

    public static final String ATTR_PATH = "path";

    public static final String ATTRNAME_JAVADOC_LOCATION = "javadoc_location";

    public static final String KIND_LIB = ClasspathKind.LIB.toString();

    public static final String KIND_VAR = ClasspathKind.VAR.toString();

    protected File classpathFile;

    protected Document document;

    protected Element classpathElement;

    protected boolean isChanged;

    /**
     * Constructs new instance.
     * 
     * @param projectBaseDir
     */
    public EclipseClasspath(File projectBaseDir) {
        String filename = projectBaseDir.getAbsolutePath() + SEP + DOT_CLASSPATH_FILENAME;
        classpathFile = new File(filename);
    }

    public Document load() {
        if (classpathFile.exists()) {
            isChanged = false;
            return loadDotClassPath(classpathFile);
        } else {
            isChanged = true;
            return createEmptyDotClassPath();
        }
    }

    private Document createEmptyDotClassPath() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            classpathElement = document.createElement(ELEMENT_CLASSPATH);
            document.appendChild(classpathElement);
            return document;
        } catch (ParserConfigurationException ex) {
            throw new PluginRuntimeException(ex);
        }
    }

    private Document loadDotClassPath(File file) {
        Logger.info("Loading " + file.getAbsolutePath() + " ...");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(file);
            classpathElement = document.getDocumentElement();

            NodeList nodeList = document.getElementsByTagName(ELEMENT_CLASSPATHENTRY);
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                Element element = (Element) nodeList.item(i);
                Logger.debug("ClasspathEntry loaded.  path=" + element.getAttribute(ATTR_PATH));
            }
            return document;
        } catch (Exception ex) {
            throw new PluginRuntimeException(ex);
        }
    }

    public void addClasspathEntry(ClasspathKind kind, String path, String sourcePath, String javadocPath) {
        if (kind != ClasspathKind.LIB && kind != ClasspathKind.VAR) {
            throw new IllegalArgumentException("kind must be 'lib' or 'var'");
        }

        Element entry = document.createElement(ELEMENT_CLASSPATHENTRY);
        classpathElement.appendChild(entry);
        entry.setAttribute(ATTR_KIND, kind.toString());
        entry.setAttribute(ATTR_PATH, path);

        if (sourcePath != null) {
            entry.setAttribute(ATTR_SOURCEPATH, sourcePath);
        }
        if (javadocPath != null) {
            addAttributeElement(entry, ATTRNAME_JAVADOC_LOCATION, javadocPath);
        }
        isChanged = true;
        return;
    }

    /**
     * Add 'attribute' element to specified parent element. if parent element
     * has no 'attributes' element, this method automatically creates that
     * element.
     * 
     * @param parent
     *        parent element
     * @param name
     *        attribute element's 'name' attribute
     * @param value
     *        attribute element's 'value' attribute
     */
    private void addAttributeElement(Element parent, String name, String value) {
        Element attributeElement = document.createElement(ELEMENT_ATTRIBUTE);
        attributeElement.setAttribute(ATTR_NAME, name);
        attributeElement.setAttribute(ATTR_VALUE, value);

        NodeList attributesList = parent.getElementsByTagName(ELEMENT_ATTRIBUTES);
        Element attributesElement;
        if (attributesList.getLength() == 0) {
            attributesElement = document.createElement(ELEMENT_ATTRIBUTES);
            parent.appendChild(attributesElement);
        } else {
            attributesElement = (Element) attributesList.item(0);
        }

        attributesElement.appendChild(attributeElement);
        isChanged = true;
    }

    public Element findClasspathEntry(String path) {
        NodeList elements = document.getElementsByTagName(ELEMENT_CLASSPATHENTRY);
        int size = elements.getLength();
        for (int i = 0; i < size; i++) {
            Element element = (Element) (elements.item(i));
            if (path.equals(element.getAttribute(ATTR_PATH))) {
                return element;
            }
        }
        return null;
    }

    public List<Element> findClasspathEntry(Pattern pattern) {
        List<Element> result = new ArrayList<Element>();
        NodeList elements = document.getElementsByTagName(ELEMENT_CLASSPATHENTRY);
        int size = elements.getLength();
        for (int i = 0; i < size; i++) {
            Element element = (Element) (elements.item(i));
            String path = element.getAttribute(ATTR_PATH);
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                result.add(element);
            }
        }
        return result;
    }

    public void removeClasspathEntries(List<Element> entries) {
        for (Element entry : entries) {
            removeClasspathEntry(entry);
        }
    }

    public void removeClasspathEntry(Element entry) {
        Node nextSibling = entry.getNextSibling();
        Node removed = classpathElement.removeChild(entry);
        if (removed != null) {
            if (nextSibling != null && isWhitespaceText(nextSibling)) {
                classpathElement.removeChild(nextSibling);
            }
            isChanged = true;
        }
    }

    public String getPath(Element classpathEntry) {
        return classpathEntry.getAttribute(ATTR_PATH);
    }

    public boolean isWhitespaceText(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = ((Text) node).getData();
            return StringUtils.isWhitespace(text);
        } else {
            return false;
        }
    }

    public void write() {
        if (!isChanged) {
            Logger.info(".classpath is not changed.");
            return;
        }

        BufferedOutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(classpathFile));
            writeDocument(os);
            os.flush();
            Logger.info(".classpath wrote : " + classpathFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.error(ex.getLocalizedMessage(), ex);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private void writeDocument(OutputStream out) {
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            Source source = new DOMSource(document);
            Result result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (Exception ex) {
            throw new PluginRuntimeException(ex);
        }
    }
}
