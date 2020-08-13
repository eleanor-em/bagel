package bagel.map;

import bagel.BagelError;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Optional;

class Layer {

    private Tile[][] tiles;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    final int width;
    final int height;
    final int id;

    static Layer fromNode(Node node, String filename) {
        NamedNodeMap layerAttributes = node.getAttributes();
        Node idNode = layerAttributes.getNamedItem("id");
        if (idNode == null) {
            throw new BagelError(
                "Malformed TMX file `" + filename + "`: no id attribute for layer");
        }
        Node widthNode = layerAttributes.getNamedItem("width");
        if (widthNode == null) {
            throw new BagelError(
                "Malformed TMX file `" + filename + "`: no width attribute for layer");
        }
        Node heightNode = layerAttributes.getNamedItem("height");
        if (heightNode == null) {
            throw new BagelError(
                "Malformed TMX file `" + filename + "`: no height attribute for layer");
        }
        String widthVal = widthNode.getTextContent();
        if (!widthVal.matches("0|([1-9]\\d*)")) {
            throw new BagelError("Malformed TMX file `" + filename
                + "`: width attribute for layer not valid integer");
        }
        String heightVal = heightNode.getTextContent();
        if (!heightVal.matches("0|([1-9]\\d*)")) {
            throw new BagelError("Malformed TMX file `" + filename
                + "`: width attribute for layer not valid integer");
        }
        String idVal = idNode.getTextContent();
        if (!idVal.matches("0|([1-9]\\d*)")) {
            throw new BagelError("Malformed TMX file `" + filename
                + "`: id attribute for layer not valid integer");
        }
        int width = Integer.parseInt(widthVal);
        int height = Integer.parseInt(heightVal);
        int id = Integer.parseInt(idVal);
        return new Layer(id, width, height);
    }

    private Layer(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    Optional<Tile> at(int tx, int ty) {
        if (tx < 0 || tx >= tiles.length || ty < 0 || ty >= tiles[0].length) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(tiles[tx][ty]);
        }
    }

    private final static int FLIPPED_HORIZONTALLY_FLAG = 0x80000000;
    private final static int FLIPPED_VERTICALLY_FLAG = 0x40000000;
    private final static int FLIPPED_DIAGONALLY_FLAG = 0x20000000;

    void loadFromCSV(String csv, String filename) {
        tiles = new Tile[width][height];
        String[] tileGids = csv
            .trim()
            .split("[\\s]*,[\\s]*");
        tiles = new Tile[width][height];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                String gid = tileGids[x + y * width];
                int tileId = Integer.parseInt(gid);
                boolean flippedHorizontally = false, flippedVertically = false, flippedDiagonally = false;
                flippedHorizontally = (tileId & FLIPPED_HORIZONTALLY_FLAG) != 0;
                flippedVertically = (tileId & FLIPPED_VERTICALLY_FLAG) != 0;
                flippedDiagonally = (tileId & FLIPPED_DIAGONALLY_FLAG) != 0;
                tileId &= ~(FLIPPED_HORIZONTALLY_FLAG
                    | FLIPPED_VERTICALLY_FLAG
                    | FLIPPED_DIAGONALLY_FLAG);
                Orientation orientation = Orientation.NORMAL;
                if (flippedHorizontally) {
                    orientation = Orientation.FLIPPED_HORIZONTAL;
                }
                if (flippedVertically) {
                    orientation = Orientation.FLIPPED_VERTICAL;
                }
                if (flippedHorizontally && flippedVertically || flippedDiagonally) {
                    orientation = Orientation.FLIPPED_DIAGONAL;
                }
                tiles[x][y] = new Tile(x, y, tileId, orientation, this);
            }
        }

    }

    public void loadFromInputStream(InputStream is, String filename) throws IOException {
        // See https://doc.mapeditor.org/en/stable/reference/tmx-map-format/#tmx-data
        tiles = new Tile[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gId = is.read()
                    | is.read() << 8
                    | is.read() << 16
                    | is.read() << 24;
                boolean flipHoriz = (gId & FLIPPED_HORIZONTALLY_FLAG) != 0;
                boolean flipVert = (gId & FLIPPED_VERTICALLY_FLAG) != 0;
                boolean flipDiag = (gId & FLIPPED_DIAGONALLY_FLAG) != 0;
                gId &= ~(FLIPPED_HORIZONTALLY_FLAG |
                    FLIPPED_VERTICALLY_FLAG |
                    FLIPPED_DIAGONALLY_FLAG);
                Orientation orientation = Orientation.NORMAL;
                if (flipHoriz) {
                    orientation = Orientation.FLIPPED_HORIZONTAL;
                }
                if (flipVert) {
                    orientation = Orientation.FLIPPED_VERTICAL;
                }
                if (flipHoriz && flipVert || flipDiag) {
                    orientation = Orientation.FLIPPED_DIAGONAL;
                }
                tiles[x][y] = new Tile(x, y, (int) gId, orientation, this);
            }
        }
    }
}
