package bagel.map;

import bagel.BagelError;
import bagel.Image;
import bagel.map.Animation.Frame;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

class TileSet {

    final int tileWidth;
    final int tileHeight;
    final Image image;
    final int offset;
    private Map<String, Map<Integer, String>> properties = new HashMap<>();
    private Map<Integer, TileInformation> tileInformation;

    String getProperty(String property, int id) {
        if (!properties.containsKey(property)) {
            return null;
        } else {
            return properties.get(property).get(id);
        }
    }

    void setProperty(String property, String value, int rawId) {
        if (!properties.containsKey(property)) {
            properties.put(property, new HashMap<>());
        }
        properties.get(property).put(rawId + offset, value);
    }

    static TileSet fromNode(Node node, String filename) {
        NamedNodeMap attrs = node.getAttributes();
        Node widthNode = attrs.getNamedItem("tilewidth");
        if (widthNode == null) {
            throw new BagelError(
                "Malformed TSX file `" + filename + "`: no tilewidth attribute for tileset");
        }
        Node heightNode = attrs.getNamedItem("tileheight");
        if (heightNode == null) {
            throw new BagelError(
                "Malformed TSX file `" + filename + "`: no tileheight attribute for tileset");
        }
        Node firstGidNode = attrs.getNamedItem("firstgid");
//        if (firstGidNode == null) {
//            throw new BagelError(
//                "Malformed TSX file `" + filename + "`: no firstgid attribute for tileset");
//        }
        String firstGidVal = firstGidNode == null ? "1" : firstGidNode.getTextContent();
        if (!firstGidVal.matches("0|([1-9]\\d*)")) {
            throw new BagelError("Malformed TSX file `" + filename
                + "`: firstgid attribute for tileset not valid integer");
        }
        String widthVal = widthNode.getTextContent();
        if (!widthVal.matches("0|([1-9]\\d*)")) {
            throw new BagelError("Malformed TSX file `" + filename
                + "`: tilewidth attribute for tileset not valid integer");
        }
        String heightVal = heightNode.getTextContent();
        if (!heightVal.matches("0|([1-9]\\d*)")) {
            throw new BagelError("Malformed TSX file `" + filename
                + "`: tileheight attribute for tileset not valid integer");
        }
        int width = Integer.parseInt(widthVal);
        int height = Integer.parseInt(heightVal);
        int offset = Integer.parseInt(firstGidVal);

        Node imageNode = null;
        NodeList imageChildren = node.getChildNodes();
        for (int i = 0; i < imageChildren.getLength(); ++i) {
            if (imageChildren.item(i).getNodeName().equals("image")) {
                if (imageNode != null) {
                    throw new BagelError("Malformed TSX file `" + filename
                        + "`: tileset element has multiple image elements");
                }
                imageNode = imageChildren.item(i);
            }
        }
        if (imageNode == null) {
            throw new BagelError(
                "Malformed TSX file `" + filename + "`: tileset element has no image element");
        }
        Node imageNameNode = imageNode.getAttributes().getNamedItem("source");
        if (imageNameNode == null) {
            throw new BagelError("Malformed TSX file `" + filename
                + "`: image element does not have source attribute");
        }
        String imageFile =
            filename.substring(0, filename.lastIndexOf("/")) + "/" + imageNameNode.getTextContent();

        // Parse animations
        Map<Integer, TileInformation> tileInformation = parseTileInformation(node, filename);
        return new TileSet(width, height, offset, tileInformation, imageFile);
    }

    private static Map<Integer, TileInformation> parseTileInformation(Node root, String filename) {
        NodeList children = root.getChildNodes();
        Map<Integer, TileInformation> informationMap = new HashMap<>();
        for (int i = 0; i < children.getLength(); i++) {
            // Not a tile node - we don't care
            Node current = children.item(i);
            if (!current.getNodeName().equals("tile")) {
                continue;
            }
            Node tileIdNode = current.getAttributes().getNamedItem("id");
            if (tileIdNode == null || !tileIdNode.getTextContent().matches("0|([1-9]\\d*)")) {
                throw new BagelError("Malformed TSX file `" + filename
                    + "`: tile element does not have id attribute");
            }
            int tileId = Integer.parseInt(tileIdNode.getTextContent());
            NodeList childNodeList = current.getChildNodes();
            if (childNodeList.getLength() == 0) {
                continue;
            }
//            int ll = animationList.getLength();
//            if (animationList.getLength() > 3) {
//                throw new BagelError("Bagel limitation:`" + filename + "`: Bagel does not support more than basic tile animations");
//            }
            Animation animation = new Animation();
            Node animationNode = null;
            for (int j = 0; j < childNodeList.getLength(); j++) {
                if (childNodeList.item(j).getNodeName().equalsIgnoreCase("animation")) {
                    animationNode = childNodeList.item(j);
                    break;
                }
            }
            if (animationNode == null) {
                continue;
            }
            NodeList frameList = animationNode.getChildNodes();
            for (int j = 0; j < frameList.getLength(); j++) {
                Node currentFrame = frameList.item(j);
                String frameNodeName = currentFrame.getNodeName();
                if (!frameNodeName.equals("frame")) {
                    continue;
                }
                NamedNodeMap frameAttributes = currentFrame.getAttributes();
                Node frameIdNode = frameAttributes.getNamedItem("tileid");
                Node durationNode = frameAttributes.getNamedItem("duration");
                if (frameIdNode == null || durationNode == null) {
                    throw new BagelError("Malformed TSX file `" + filename
                        + "`: animation frame should contain both a tileid and duration");
                }
                String frameIdText = frameIdNode.getTextContent();
                String durationText = durationNode.getTextContent();
                if (!frameIdText.matches("0|([1-9]\\d*)")) {
                    throw new BagelError("Malformed TSX file `" + filename
                        + "`: animation frame id not a valid integer");
                }
                if (!durationText.matches("0|([1-9]\\d*)")) {
                    throw new BagelError("Malformed TSX file `" + filename
                        + "`: animation frame duration not a valid integer");
                }
                int frameId = Integer.parseInt(frameIdText);
                int duration = Integer.parseInt(durationText);
                animation.addFrame(new Frame(frameId, duration));
            }
            informationMap.put(tileId, new TileInformation(animation));
        }
        return informationMap;
    }

    private TileSet(int tileWidth, int tileHeight, int offset,
        Map<Integer, TileInformation> tileInformation, String imageFile) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.offset = offset;
        this.image = new Image(imageFile);
        this.tileInformation = tileInformation;
    }

    public Map<Integer, TileInformation> getTileInformation() {
        return tileInformation;
    }

    public void setTileInformation(
        Map<Integer, TileInformation> tileInformation) {
        this.tileInformation = tileInformation;
    }
}
