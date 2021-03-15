package org.apache.commons.geometry.io.core;

import java.util.List;

public interface GeometryFormat {

    String getFormatName();

    String getDefaultFileExtension();

    List<String> getFileExtensions();
}
