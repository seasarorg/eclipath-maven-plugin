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
package org.seasar.uruma.maven.plugin.eclipse;

/**
 * Constants for this plugin.
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface Constants {
    public static final String DOT_CLASSPATH_FILENAME = ".classpath";

    public static final String ATTR_SOURCEPATH = "sourcepath";

    public static final String ATTR_KIND = "kind";

    public static final String ATTR_VALUE = "value";

    public static final String ATTR_NAME = "name";

    public static final String ATTR_PATH = "path";

    public static final String ELEMENT_CLASSPATH = "classpath";

    public static final String ELEMENT_CLASSPATHENTRY = "classpathentry";

    public static final String ELEMENT_ATTRIBUTES = "attributes";

    public static final String ELEMENT_ATTRIBUTE = "attribute";

    public static final String ATTRNAME_JAVADOC_LOCATION = "javadoc_location";

    public static final String SEP = System.getProperty("file.separator");
}
