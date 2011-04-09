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
package org.seasar.uruma.eclipath.util;

import java.io.File;

/**
 * Utility class dealing with paths.
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class PathUtil {
    private PathUtil() {
    }

    /**
     * Normalize path character to slash.
     * 
     * @param path
     *        path to normalize
     * @return normalized path
     */
    public static String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    /**
     * Concatenate given paths.
     * 
     * @param path1
     *        path1
     * @param path2
     *        path1
     * @return concatenated path
     */
    public static String concat(final String path1, final String path2) {
        if (path1 == null && path2 == null) {
            return null;
        }
        if ("".equals(path1) && "".equals(path2)) {
            return "";
        }
        if (path1 == null || "".equals(path1)) {
            return path2;
        }
        if (path2 == null || "".equals(path2)) {
            return path1;
        }

        StringBuilder result = new StringBuilder(path1.length() + path2.length() + 1);
        result.append(path1);
        if (!path1.endsWith("/")) {
            result.append("/");
        }

        if (path2.length() > 0) {
            if (path2.startsWith("/")) {
                result.append(path2.substring(1));
            } else {
                result.append(path2);
            }
        }
        return result.toString();
    }

    /**
     * Get {@code pathFile}'s relative path from {@code basePathFile}.
     * 
     * @param basePathFile
     *        base path File object
     * @param pathFile
     *        target path File object
     * @return relative path
     */
    public static String getRelativePath(final File basePathFile, final File pathFile) {
        String basePath = normalizePath(basePathFile.getAbsolutePath());
        String path = normalizePath(pathFile.getAbsolutePath());

        if (path.startsWith(basePath)) {
            String relPath = path.substring(basePath.length(), path.length());
            if (relPath.startsWith("/")) {
                relPath = relPath.substring(1);
            }
            return relPath;
        } else {
            return path;
        }
    }
}
