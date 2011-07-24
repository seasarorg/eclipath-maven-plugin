/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.uruma.eclipath.mojo;

import java.io.File;

import org.seasar.uruma.eclipath.Logger;

/**
 * @goal clean
 * @requiresDependencyResolution test
 * @phase process-sources
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CleanMojo extends CheckCleanMojo {

    /*
     * @see org.seasar.uruma.eclipath.mojo.CheckCleanMojo#deal(java.io.File)
     */
    @Override
    protected void deal(File file) {
        File parentDir = file.getParentFile();
        deleteFile(file);
        deleteDir(parentDir);
    }

    protected void deleteFile(File file) {
        if (file.delete()) {
            Logger.info("File deleted : " + file.getAbsolutePath());
        } else {
            Logger.warn("Failed to delete file : " + file.getAbsolutePath());
        }
    }

    /**
     * Delete directory if it is empty.
     * 
     * @param dir
     *        target directory
     */
    protected void deleteDir(File dir) {
        if (dir.listFiles().length == 0) {
            if (dir.delete()) {
                Logger.info("Directory deleted : " + dir.getAbsolutePath());
            } else {
                Logger.info("Failed to delete directory : " + dir.getAbsolutePath());
            }
        }
    }
}
