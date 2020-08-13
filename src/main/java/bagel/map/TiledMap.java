package bagel.map;

import bagel.BagelError;
import bagel.DrawOptions;
import bagel.util.Point;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Loads a tile map as produced by Tiled Map Editor.
 * <p>
 * Allows looking up properties of tiles in the map as well as basic rendering.
 */
public class TiledMap {

    private final ArrayList<Layer> layers = new ArrayList<>();
    private final ArrayList<ObjectGroup> objectGroups = new ArrayList<>();
    private final TileSet tileSet;

    /**
     * Build the tiled map from the provided .tmx file.
     */
    public TiledMap(String tmxFile) {
        String tsxFilename = parseTmx(tmxFile.replace("\\", "/"));
        tileSet = parseTsx(tsxFilename);
    }

    /**
     * Returns the width of an individual tile in the map, in pixels.
     */
    public int getTileWidth() {
        return tileSet.tileWidth;
    }

    /**
     * Returns the height of an individual tile in the map, in pixels.
     */
    public int getTileHeight() {
        return tileSet.tileHeight;
    }

    /**
     * Returns the width of the entire map, in pixels.
     */
    public int getWidth() {
        return layers.get(0).width * getTileWidth();
    }

    /**
     * Returns the height of the entire map, in pixels.
     */
    public int getHeight() {
        return layers.get(0).height * getTileHeight();
    }

    /**
     * Returns the string value of the provided property at the given (x, y) pixel coordinate of the
     * map.
     */
    public String getProperty(int x, int y, String property) {
        for (int i = 0; i < layers.size(); i++) {
            Tile tile = at(x, y, i);
            if (tile == null) {
                throw new BagelError("Position (" + x + ", " + y + ") not on map");
            }
            String layerMatch = tileSet.getProperty(property, tile.id);
            if (layerMatch != null) {
                return layerMatch;
            }
        }
        return null;
    }

    /**
     * Returns the string value of the provided property at the given (x, y) pixel coordinate of the
     * map.
     * <p>
     * If the property is missing, returns the default.
     */
    public String getProperty(int x, int y, String property, String defaultValue) {
        Tile tile = at(x, y, 0);
        if (tile == null) {
            throw new BagelError("Position (" + x + ", " + y + ") not on map");
        }
        String result = tileSet.getProperty(property, tile.id);
        return result == null ? defaultValue : result;
    }

    /**
     * Returns the integer value of the provided property at the given (x, y) pixel coordinate of
     * the map. If the value is not a valid integer, throws {@link BagelError}.
     * <p>
     * defaultValue is the value that will be returned if the tile has no such property.
     */
    public int getPropertyInt(int x, int y, String property, int defaultValue) {
        String result = getProperty(x, y, property);
        if (result == null) {
            return defaultValue;
        } else if (!result.matches("0|([1-9]\\d*)")) {
            throw new BagelError("Property `" + property + "` had non-integer value `" + result + "`");
        } else {
            return Integer.parseInt(result);
        }
    }

    /**
     * Returns the double value of the provided property at the given (x, y) pixel coordinate of the
     * map. If the value is not a valid double, throws {@link BagelError}.
     * <p>
     * defaultValue is the value that will be returned if the tile has no such property.
     */
    public double getPropertyDouble(int x, int y, String property, double defaultValue) {
        String result = getProperty(x, y, property);
        if (result == null) {
            return defaultValue;
        } else if (!result.matches("(0|([1-9]\\d*))\\.?\\d*")) {
            throw new BagelError("Property `" + property + "` had non-double value `" + result + "`");
        } else {
            return Double.parseDouble(result);
        }
    }

    /**
     * Returns the Boolean value of the provided property at the given (x, y) pixel coordinate of
     * the map. If the value is not a valid boolean, throws {@link BagelError}.
     * <p>
     * defaultValue is the value that will be returned if the tile has no such property.
     */
    public boolean getPropertyBoolean(int x, int y, String property, boolean defaultValue) {
        String result = getProperty(x, y, property);
        if (result == null) {
            return defaultValue;
        } else if (result.equalsIgnoreCase("true")) {
            return true;
        } else if (result.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new BagelError("Property `" + property + "` had non-boolean value `" + result + "`");
        }
    }

    /**
     * Returns true if the tile at the given (x, y) pixel coordinate of the map has the given
     * property.
     */
    public boolean hasProperty(int x, int y, String property) {
        return getProperty(x, y, property) != null;
    }

    private Point tileIdToPoint(int id) {
        int tw = (int) (tileSet.image.getWidth() / getTileWidth());
        int tx = id % tw;
        int ty = id / tw;
        return new Point(tx, ty);
    }

    /**
     * Returns all polylines associated with this map. A polyline is simply a list of points.
     *
     * @return List of polylines
     */
    public List<List<Point>> getAllPolylines() {
        List<List<Point>> allPolylines = new ArrayList<>();
        for (ObjectGroup group : objectGroups) {
            allPolylines.addAll(group.getPolylines());
        }
        return allPolylines;
    }

    /**
     * Draws the rectangular region of the map with top left at (mapX, mapY) and bottom right at
     * (mapX + width, mapY + height). The top left will be drawn on the window at (windowX, windowY).
     *
     * @param windowX The x coordinate of the window to start drawing the map region from
     * @param windowY The y coordinate of the window to start drawing the map region from
     * @param mapX    The x coordinate of the map to start drawing from
     * @param mapY    The y coordinate of the map to start drawing from
     * @param width   The width of the region to render
     * @param height  The height of the region to render
     */
    public void draw(double windowX, double windowY, double mapX, double mapY, double width,
        double height) {
        int xOffset = -((int) mapX) % getTileWidth();
        int yOffset = -((int) mapY) % getTileHeight();

        for (int x = (int) mapX; x < Math.min(mapX + width, getWidth()) + getTileWidth();
            x += getTileWidth()) {
            for (int y = (int) mapY; y < Math.min(mapY + height, getHeight()) + getTileHeight();
                y += getTileHeight()) {
                Tile tile = null;
                for (int i = 0; i < layers.size(); i++) {
                    tile = at(x, y, i);
                    if (tile != null) {
                        int id = tile.id - tileSet.offset;
                        Point tileLocation = tileIdToPoint(id);
                        DrawOptions opt = new DrawOptions();
                        switch (tile.orientation) {
                            case FLIPPED_HORIZONTAL:
                                opt.setScale(-1, 1);
                                break;
                            case FLIPPED_VERTICAL:
                                opt.setScale(1, -1);
                                break;
                            case FLIPPED_DIAGONAL:
                                opt.setScale(-1, -1);
                                break;
                        }
                        // Check if tile is animated
                        if (tileSet.getTileInformation().containsKey(id)) {
                            TileInformation tileInformation = tileSet.getTileInformation().get(id);
                            if (tileInformation.animation == null) {
                                continue;
                            }
                            int newId = tileInformation.animation.getCurrentFrame().id;
                            tileLocation = tileIdToPoint(newId);
                        }
                        opt.setSection(tileLocation.x * getTileWidth(),
                            tileLocation.y * getTileHeight(), getTileWidth(),
                            getTileHeight());
                        tileSet.image.drawFromTopLeft(windowX + x - mapX + xOffset,
                            windowY + y - mapY + yOffset,
                            opt);
                    }
                }
            }
        }
    }

    Tile at(int x, int y, int layer) {
        return layers.get(layer).at(x / tileSet.tileWidth, y / tileSet.tileHeight).orElse(null);
    }

    // Returns TSX file location
    private String parseTmx(String filename) {
        try {
            // First, we want to parse the TMX as XML.
            String xml = new String(Files.readAllBytes(Paths.get(filename)));
            // TMX files contain an incorrect DTD, and setting the factory to not validate doesn't stop SAX from failing
            // here.
            xml = xml.replaceAll("<!DOCTYPE.*>\\n", "");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            // Load map metadata
            NodeList tilesetNodes = doc.getElementsByTagName("tileset");
            if (tilesetNodes.getLength() != 1) {
                throw new BagelError(
                    "Mal1formed TMX file `" + filename + "`: no or multiple tileset elements");
            }
            Node tilesetNode = tilesetNodes.item(0);
            Node tsxLocation = tilesetNode.getAttributes().getNamedItem("source");
            if (tsxLocation == null) {
                throw new BagelError("Malformed T<X file `" + filename
                    + "`: tileset element does not have source attribute");
            }
            // Load object groups
            NodeList objectLayerNodes = doc.getElementsByTagName("objectgroup");
            for (int i = 0; i < objectLayerNodes.getLength(); i++) {
                Node rootObjectLayerNode = objectLayerNodes.item(i);
                objectGroups.add(ObjectGroup.fromNode(rootObjectLayerNode, filename));
            }
            // Load each layer
            NodeList nodes = doc.getElementsByTagName("layer");
            if (nodes.getLength() == 0) {
                throw new BagelError("Malformed TMX file `" + filename + "`: no layer elements");
            }

            for (int layerIndex = 0; layerIndex < nodes.getLength(); ++layerIndex) {
                Node layerNode = nodes.item(layerIndex);

                // Find the data element of the layer
                Node dataNode = null;
                NodeList layerChildren = layerNode.getChildNodes();
                for (int i = 0; i < layerChildren.getLength(); ++i) {
                    if (layerChildren.item(i).getNodeName().equals("data")) {
                        if (dataNode != null) {
                            throw new BagelError(
                                "Malformed TMX file `" + filename + "`: layer " + layerIndex
                                    + " has multiple data elements");
                        }
                        dataNode = layerChildren.item(i);
                    }
                }
                if (dataNode == null) {
                    throw new BagelError(
                        "Malformed TMX file `" + filename + "`: layer " + layerIndex
                            + " has no data element");
                }

                // Now we need to decode and decompress the base-64 data.
                String encoded = dataNode.getTextContent().trim();
                String encoding = dataNode.getAttributes().getNamedItem("encoding")
                    .getTextContent();
                switch (encoding.toLowerCase()) {
                    case "csv":
                        // Load the layer object.
                        Layer layer = Layer.fromNode(layerNode, filename);
                        layer.loadFromCSV(encoded, filename);
                        layers.add(layer);
                        break;
                    case "base64":
                        byte[] decoded = Base64.getDecoder().decode(encoded);
                        ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
                        InputStream is;
                        layer = Layer.fromNode(layerNode, filename);
                        Node compressionNode = dataNode.getAttributes().getNamedItem("compression");
                        String compression = null;
                        if (compressionNode != null) {
                            compression = compressionNode.getTextContent();
                            ;
                        }
                        if (compression == null) {
                            is = bais;
                        } else if (compression.equalsIgnoreCase("gzip")) {
                            is = new GZIPInputStream(bais);
                        } else if (compression.equalsIgnoreCase("zlib")) {
                            is = new InflaterInputStream(bais);
                        } else {
                            throw new BagelError(
                                "Malformed TMX file `" + filename + "`: layer " + layerIndex
                                    + " has an unrecognised compression format");
                        }
                        layer.loadFromInputStream(is, filename);
                        layers.add(layer);
                        break;
                }
            }

            return filename.substring(0, filename.lastIndexOf("/")) + "/" + tsxLocation
                .getTextContent();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new BagelError("Exception while loading TMX file `" + filename + "`: "
                + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private TileSet parseTsx(String filename) {
        try {
            String xml = new String(Files.readAllBytes(Paths.get(filename)));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            // Load the tileset object
            NodeList nodes = doc.getElementsByTagName("tileset");
            if (nodes.getLength() != 1) {
                throw new BagelError(
                    "Malformed TSX file `" + filename + "`: no or multiple tileset elements");
            }
            TileSet ts = TileSet.fromNode(nodes.item(0), filename);

            // We want to loop over every tile element...
            NodeList tiles = doc.getElementsByTagName("tile");
            int tilesLength = tiles.getLength();
            for (int i = 0; i < tilesLength; ++i) {
                Node tileNode = tiles.item(i);
                // Find the tile ID
                Node idNode = tileNode.getAttributes().getNamedItem("id");
                if (idNode == null) {
                    throw new BagelError(
                        "Malformed TSX file `" + filename + "`: tile " + i + " missing id");
                }
                String idVal = idNode.getTextContent();
                if (!idVal.matches("0|([1-9]\\d*)")) {
                    throw new BagelError(
                        "Malformed TSX file `" + filename + "`: tile " + i + " id `" + idVal
                            + "` not a valid integer");
                }
                int rawId = Integer.parseInt(idVal);
                // ... now find every properties element...
                NodeList tileChildren = tileNode.getChildNodes();
                int tiledChildrenLength = tileChildren.getLength();
                for (int j = 0; j < tiledChildrenLength; ++j) {
                    Node tileChild = tileChildren.item(j);
                    if (tileChild.getNodeName().equals("properties")) {
                        // ... and fill in the properties!
                        NodeList propertyChildren = tileChild.getChildNodes();
                        for (int k = 0; k < propertyChildren.getLength(); ++k) {
                            Node propertyNode = propertyChildren.item(k);
                            if (propertyNode.getNodeName().equals("property")) {
                                // Carefully extract the name and value
                                NamedNodeMap attrs = propertyNode.getAttributes();
                                Node nameNode = attrs.getNamedItem("name");
                                if (nameNode == null) {
                                    throw new BagelError(
                                        "Malformed TSX file `" + filename + "`: tile " + i
                                            + ", property " + k + " missing name");
                                }
                                Node valueNode = attrs.getNamedItem("value");
                                if (valueNode == null) {
                                    throw new BagelError(
                                        "Malformed TSX file `" + filename + "`: tile " + i
                                            + ", property " + k + " missing value");
                                }

                                // Finally, set it
                                ts.setProperty(nameNode.getTextContent(),
                                    valueNode.getTextContent(), rawId);
                            }
                        }
                    }
                }
            }
            return ts;
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new BagelError("Exception while loading TSX file `" + filename + "`: "
                + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
