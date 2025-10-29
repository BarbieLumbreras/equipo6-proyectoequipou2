package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;
public class ChessPiece {
    public enum PieceType {
        PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
    }

    public enum PieceColor {
        WHITE, BLACK, GRAY // Gliński tiene 3 jugadores opcionales
    }

    private PieceType type;
    private PieceColor color;
    private boolean hasMoved; // Para enroque y movimiento del peón

    public ChessPiece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }

    public PieceType getType() { return type; }
    public PieceColor getColor() { return color; }
    public boolean hasMoved() { return hasMoved; }
    public void setMoved(boolean moved) { this.hasMoved = moved; }

    // Obtener símbolo Unicode para representar la pieza
    public String getSymbol() {
        // Usar símbolos más consistentes (todos rellenos o todos vacíos)
        switch (type) {
            case KING:   return "♚"; //REY
            case QUEEN:  return "♛"; //DAMA
            case ROOK:   return "♜"; //TORRE
            case BISHOP: return "♝"; //ALFIL
            case KNIGHT: return "♞"; //JUAN
            case PAWN:   return "♟"; //CORNEJO
            default:     return "";
        }
    }
}