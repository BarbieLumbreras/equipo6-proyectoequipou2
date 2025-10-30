package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

public class HexCell {
    private int q; // coordenada axial q
    private int r; // coordenada axial r
    private ChessPiece piece;
    private String color; // "white", "gray", "black" (colores del tablero)

    public HexCell(int q, int r, String color) {
        this.q = q;
        this.r = r;
        this.color = color;
        this.piece = null;
    }

    // Sistema de coordenadas cúbicas para facilitar cálculos
    public int getS() {
        return -q - r;
    }
    public int getQ() { return q; }
    public int getR() { return r; }
    public String getColor() { return color; }
    public ChessPiece getPiece() { return piece; }
    public void setPiece(ChessPiece piece) { this.piece = piece; }

    // Distancia entre dos celdas hexagonales
    public int distanceTo(HexCell other) {
        return (Math.abs(q - other.q) + Math.abs(r - other.r) + Math.abs(getS() - other.getS())) / 2;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HexCell)) return false;
        HexCell other = (HexCell) obj;
        return q == other.q && r == other.r;
    }

    @Override
    public int hashCode() {
        return q * 1000 + r;
    }
}