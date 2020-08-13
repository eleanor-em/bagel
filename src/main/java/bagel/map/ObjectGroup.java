package bagel.map;

import bagel.BagelError;
import bagel.util.Point;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

class ObjectGroup {

    final int id;
    final String name;
    private final List<List<Point>> polyLines = new ArrayList<>();

    public ObjectGroup(int id, String name) {
        this.id = id;
        this.name = name;
    }

    static ObjectGroup fromNode(Node root, String filename) {
        NamedNodeMap attrs = root.getAttributes();
        Node idNode = attrs.getNamedItem("id");
        if (idNode == null) {
            throw new BagelError(
                "Malformed TSX file `" + filename + "`: object group has no id");
        }
        if (!idNode.getTextContent().matches("0|([1-9]\\d*)")) {
            throw new BagelError(
                "Malformed TSX file `" + filename + "`: object group has invalid id");
        }
        Integer id = Integer.parseInt(idNode.getTextContent());
        Node nameNode = attrs.getNamedItem("name");
        if (nameNode == null) {
            throw new BagelError(
                "Malformed TSX file `" + filename + "`: object group has no name");
        }
        String name = nameNode.getTextContent();
        ObjectGroup group = new ObjectGroup(id, name);
        NodeList objectGroupChildren = root.getChildNodes();
        int numChildren = objectGroupChildren.getLength();
        // Iterate through all the children of the <objectgroup> tag. This can be either properties
        // or objects. We will worry about objects for now.
        for (int i = 0; i < numChildren; i++) {
            Node currentObject = objectGroupChildren.item(i);
            if (!currentObject.getNodeName().equalsIgnoreCase("object")) {
                // Not an object, skip it
                continue;
            }
            NamedNodeMap objectAttributes = currentObject.getAttributes();
            Node xNode = objectAttributes.getNamedItem("x");
            if (xNode == null || !xNode.getTextContent().matches("^-?(0|[1-9]\\d*)(\\.\\d+)?")) {
                throw new BagelError(
                    "Malformed TSX file `" + filename
                        + "`: object group has no valid x coordinate");
            }
            double objectX = Double.parseDouble(xNode.getTextContent());
            Node yNode = objectAttributes.getNamedItem("y");
            if (yNode == null || !yNode.getTextContent().matches("^-?(0|[1-9]\\d*)(\\.\\d+)?")) {
                throw new BagelError(
                    "Malformed TSX file `" + filename
                        + "`: object group has no valid y coordinate");
            }
            double objectY = Double.parseDouble(yNode.getTextContent());
            NodeList objectChildren = currentObject.getChildNodes();
            int objectChildrenCount = objectChildren.getLength();
            for (int j = 0; j < objectChildrenCount; j++) {
                Node current = objectChildren.item(j);
                switch (current.getNodeName()) {
                    case "polyline":
                        // Attributes for <polyline>
                        NamedNodeMap polyLineAttrs = current.getAttributes();
                        Node pointsNode = polyLineAttrs.getNamedItem("points");
                        if (pointsNode == null) {
                            throw new BagelError(
                                "Malformed TSX file `" + filename
                                    + "`: polyline in objectgroup has no points attribute");
                        }
                        String pointsStr = pointsNode.getTextContent();
                        String[] rawPoints = pointsStr.split("\\s+");
                        List<Point> polyLinePoints = new ArrayList<>();
                        for (String rawPoint : rawPoints) {
                            String[] pointParts = rawPoint.split(",");
                            if (pointParts.length != 2 || !pointParts[0].matches("^-?(0|[1-9]\\d*)(\\.\\d+)?")
                                || !pointParts[1].matches("^-?(0|[1-9]\\d*)(\\.\\d+)?")) {
                                throw new BagelError(
                                    "Malformed TSX file `" + filename
                                        + "`: polyline points attribute not valid");
                            }
                            polyLinePoints.add(new Point(objectX + Double.parseDouble(pointParts[0]),
                                objectY + Double.parseDouble(pointParts[1])));
                        }
                        group.addPolyline(polyLinePoints);
                        break;
                }
            }
        }
        return group;
    }

    public void addPolyline(List<Point> polyline) {
        this.polyLines.add(polyline);
    }

    public List<List<Point>> getPolylines() {
        return polyLines;
    }
}
