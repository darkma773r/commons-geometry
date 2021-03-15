package org.apache.commons.geometry.io.euclidean.threed;

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.io.core.GeometryFormat;

/** Enum containing 3D geometry formats supported internally by Apache Commons Geometry.
 */
public enum GeometryFormat3D implements GeometryFormat {

    /** Value representing the OBJ file format.
     * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront .obj file</a>
     */
    OBJ(Collections.singletonList("obj")),

    /** Value representing the simple text format described by
     * {@link org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionReader TextFacetDefinitionReader}
     * and
     * {@link org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionWriter TextFacetDefinitionWriter}.
     * This format describes facets by listing the coordinates of its vertices in order, with one facet
     * described per line. Facets may have 3 or more vertices and do not need to all have the same
     * number of vertices.
     */
    TXT(Collections.singletonList("txt")),

    /** Value representing the CSV file format as described by
     * {@link org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionWriter#csvFormat(java.io.Writer)
     * TextFacetDefinitionWriter}. When used to represent 3D geometry information, the coordinates of the vertices of
     * the facets are listed in order, with one facet defined per row. This is similar to the {@link #TXT} format
     * with the exception that facets are converted to triangles before writing so that all rows have the same
     * number of columns.
     */
    CSV(Collections.singletonList("csv"));

    /** List of file extensions associated with the format. The first file extension
     * listed is taken as the default.
     */
    private final List<String> fileExtensions;

    /** Construct a new instance with the given file extensions.
     * @param fileExtensions file extensions
     */
    GeometryFormat3D(final List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormatName() {
        return name();
    }

    /** {@inheritDoc} */
    @Override
    public String getDefaultFileExtension() {
        return fileExtensions.get(0);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getFileExtensions() {
        return fileExtensions;
    }
}
