package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HexBoard {
    private Map<String, HexCell> cells;
    private static final int BOARD_SIZE = 4; // Tablero de Gliński tiene radio 5

    public HexBoard() {
        cells = new HashMap<>();
        initializeBoard();
    }

    private void initializeBoard() {
        // Crear tablero hexagonal según Gliński (91 celdas)
        for (int q = -BOARD_SIZE; q <= BOARD_SIZE; q++) {
            int r1 = Math.max(-BOARD_SIZE, -q - BOARD_SIZE);
            int r2 = Math.min(BOARD_SIZE, -q + BOARD_SIZE);

            for (int r = r1; r <= r2; r++) {
                String color = getCellColor(q, r);
                HexCell cell = new HexCell(q, r, color);
                cells.put(getKey(q, r), cell);
            }
        }

        setupInitialPieces();
    }

    // Determinar color de celda según patrón de Gliński
    private String getCellColor(int q, int r) {
        int s = -q - r;
        int sum = (q % 3 + 3) % 3;

        if (sum == 0) return "white";
        else if (sum == 1) return "gray";
        else return "black";
    }

    // Configurar posición inicial de las piezas según Gliński
    private void setupInitialPieces() {
        // Peones blancos (fila más cercana al jugador blanco)
        for (int q = -4; q <= 4; q++) {
            int r = 4 - Math.max(0, q);
            if (Math.abs(q + r) <= 4) {
                HexCell cell = getCell(q, r);
                if (cell != null) {
                    cell.setPiece(new ChessPiece(ChessPiece.PieceType.PAWN,
                            ChessPiece.PieceColor.WHITE));
                }
            }
        }

        // Peones negros
        for (int q = -4; q <= 4; q++) {
            int r = -4 + Math.max(0, -q);
            if (Math.abs(q + r) <= 4) {
                HexCell cell = getCell(q, r);
                if (cell != null) {
                    cell.setPiece(new ChessPiece(ChessPiece.PieceType.PAWN,
                            ChessPiece.PieceColor.BLACK));
                }
            }
        }

        // Piezas especiales blancas
        placePiece(0, 5, ChessPiece.PieceType.KING, ChessPiece.PieceColor.WHITE);
        placePiece(-1, 5, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.WHITE);
        placePiece(1, 5, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.WHITE);
        placePiece(-2, 5, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.WHITE);
        placePiece(2, 5, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.WHITE);
        placePiece(-3, 5, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.WHITE);
        placePiece(3, 5, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.WHITE);
        placePiece(-4, 5, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.WHITE);
        placePiece(4, 5, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.WHITE);

        // Piezas especiales negras (simétricas)
        placePiece(0, -5, ChessPiece.PieceType.KING, ChessPiece.PieceColor.BLACK);
        placePiece(-1, -5, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK);
        placePiece(1, -5, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK);
        placePiece(-2, -5, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.BLACK);
        placePiece(2, -5, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.BLACK);
        placePiece(-3, -5, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK);
        placePiece(3, -5, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK);
        placePiece(-4, -5, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.BLACK);
        placePiece(4, -5, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.BLACK);
    }

    private void placePiece(int q, int r, ChessPiece.PieceType type, ChessPiece.PieceColor color) {
        HexCell cell = getCell(q, r);
        if (cell != null) {
            cell.setPiece(new ChessPiece(type, color));
        }
    }

    public HexCell getCell(int q, int r) {
        return cells.get(getKey(q, r));
    }

    private String getKey(int q, int r) {
        return q + "," + r;
    }

    public Map<String, HexCell> getAllCells() {
        return cells;
    }

    // Obtener celdas vecinas (6 direcciones hexagonales)
    public List<HexCell> getNeighbors(HexCell cell) {
        List<HexCell> neighbors = new ArrayList<>();
        int[][] directions = {
                {1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}
        };

        for (int[] dir : directions) {
            HexCell neighbor = getCell(cell.getQ() + dir[0], cell.getR() + dir[1]);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }
}