package bagel.map;

enum Orientation {
    NORMAL,
    FLIPPED_HORIZONTAL,
    FLIPPED_VERTICAL,
    FLIPPED_DIAGONAL
}

class Tile {
    final int x;
    final int y;
    final int id;
    final Orientation orientation;
    final Layer layer;

    Tile(int x, int y, int id, Orientation orientation, Layer layer) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.orientation = orientation;
        this.layer = layer;
    }
}
