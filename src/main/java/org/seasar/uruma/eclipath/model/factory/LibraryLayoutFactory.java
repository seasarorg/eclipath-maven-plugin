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
package org.seasar.uruma.eclipath.model.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for {@link LibraryLayout}
 *
 * @author y-komori
 */
public class LibraryLayoutFactory {
    private static LibraryLayout[] layouts = new LibraryLayout[] { new LibraryLayout.FlatLayout(),
            new LibraryLayout.StandAloneLayout(), new LibraryLayout.WebLayout() };

    private static Map<String, LibraryLayout> packagingMap;

    static {
        packagingMap = new HashMap<String, LibraryLayout>();
        packagingMap.put("war", new LibraryLayout.WebLayout());
    }

    public static LibraryLayout getLibraryLayout(String layoutName) {
        for (LibraryLayout layout : layouts) {
            if (layout.getName().equals(layoutName)) {
                return layout;
            }
        }
        return null;
    }

    public static LibraryLayout getLibraryLayoutFromPackaging(String packaging) {
        LibraryLayout layout = packagingMap.get(packaging);
        if (layout == null) {
            // Default layout
            layout = new LibraryLayout.FlatLayout();
        }
        return layout;
    }
}
