/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.uruma.eclipath.classpath;

import static org.seasar.uruma.eclipath.Constants.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;
import org.seasar.uruma.eclipath.util.AssertionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The class dealing with the Eclipse
 * {@code org.eclipse.wst.common.project.facet.core.xml} file.
 *
 * @author y-komori
 */
public class WstProjectFacet {
    private static final String WST_COMMON_PROJECT_FACET_CORE_XML_FILENAME = "org.eclipse.wst.common.project.facet.core.xml";

    private static final String ELEMENT_INSTALLED = "installed";

    private static final String ATTR_FACET = "facet";

    private static final String ATTR_VERSION = "version";

    private static final String FACET_JAVA = "java";

    private final File facetCoreFile;

    private Document document;

    private Element facetedProjectElement;

    private boolean isChanged;

    /**
     * Constructs new instance.
     *
     * @param projectBaseDir
     */
    public WstProjectFacet(File projectBaseDir) {
        AssertionUtil.assertNotNull("projectBaseDir", projectBaseDir);
        String filename = projectBaseDir.getAbsolutePath() + SEP + SETTINGS_DIR + SEP
                + WST_COMMON_PROJECT_FACET_CORE_XML_FILENAME;
        facetCoreFile = new File(filename);
    }

    /**
     * Loads {@code org.eclipse.wst.common.project.facet.core.xml}.
     *
     * @return {@link Document} of the xml file
     */
    public Document load() {
        if (facetCoreFile.exists()) {
            isChanged = false;
            return loadProjectFacet(facetCoreFile);
        } else {
            Logger.debug("project facet is not exist. : " + facetCoreFile.getAbsolutePath());
            return null;
        }
    }

    /**
     * Returns the project's java facet version. If java project facet is not
     * installed, returns {@code null}.
     *
     * @return installed java facet version, or {@code null}
     */
    public String getJavaFacetVersion() {
        if (facetedProjectElement != null) {
            return facetedProjectElement.getAttribute(ATTR_VERSION);
        } else {
            return null;
        }
    }

    /**
     * Sets new java facet version. If java project facet is not installed, this
     * method does nothing.
     *
     * @param version
     *        new java facet version
     */
    public void setJavaFacetVersion(String version) {
        AssertionUtil.assertNotNull("version", version);
        String javaFacetVersion = getJavaFacetVersion();
        if (javaFacetVersion == null) {
            // java project facet is not installed, do nothing.
            return;
        }

        if (!version.equals(javaFacetVersion)) {
            facetedProjectElement.setAttribute(ATTR_VERSION, version);
            isChanged = true;
            Logger.info("java project facet version is changed. : " + version);
        }
    }

    private Document loadProjectFacet(File file) {
        Logger.debug("loading " + file.getAbsolutePath() + " ...");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(file);
            facetedProjectElement = document.getDocumentElement();

            NodeList installedElements = document.getElementsByTagName(ELEMENT_INSTALLED);
            int length = installedElements.getLength();
            for (int i = 0; i < length; i++) {
                Element installed = (Element) installedElements.item(i);
                String facet = installed.getAttribute(ATTR_FACET);
                if (FACET_JAVA.equals(facet)) {
                    String version = installed.getAttribute(ATTR_VERSION);
                    if (StringUtils.isNotEmpty(version)) {
                        facetedProjectElement = installed;
                        Logger.debug("detected java facet version : " + getJavaFacetVersion());
                    }
                }
            }
            return document;
        } catch (Exception ex) {
            throw new PluginRuntimeException(ex);
        }
    }

    /**
     * Write the project facet file. If java project facet version is not
     * changed, this method does nothing. Notice that this method never create
     * new project facet file, because when java project facet is not installed,
     * this class doesn't affect the project facet.
     */
    public void write() {
        if (!isChanged) {
            Logger.info("facet core file  is not changed.");
            return;
        }

        BufferedOutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(facetCoreFile));
            writeDocument(os);
            os.flush();
            Logger.info("facet core file wrote : " + facetCoreFile.getAbsolutePath());
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

    @Override
    public String toString() {
        if (getJavaFacetVersion() != null) {
            return "java project facet version : " + getJavaFacetVersion();
        } else {
            return "java project facet is not installed.";
        }
    }

}
