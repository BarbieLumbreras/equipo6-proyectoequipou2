package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HexBoard {
    private Map<String, HexCell> cells;
    private static final int BOARD_SIZE = 5;

    public HexBoard() {
        cells = new HashMap<>();
        initializeBoard();
    }

    private void initializeBoard() {
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

    private String getCellColor(int q, int r) {
        int s = -q - r;
        int qMod = ((q % 3) + 3) % 3;
        int rMod = ((r % 3) + 3) % 3;
        int sMod = ((s % 3) + 3) % 3;

        if (qMod == 0 && rMod == 0 && sMod == 0) return "light";
        if (qMod == 1 && rMod == 1 && sMod == 1) return "medium";
        if (qMod == 2 && rMod == 2 && sMod == 2) return "dark";

        int sum = (qMod + rMod + sMod) % 3;
        if (sum == 0) return "light";
        if (sum == 1) return "medium";
        return "dark";
    }

    // Posiciones según tablero estándar de Gliński
    // Mirando la imagen de referencia con fondo blanco:
    // - Negras arriba (filas 10-11)
    // - Blancas abajo (filas 1-2)
    private void setupInitialPieces() {
        // === PIEZAS NEGRAS (ARRIBA) ===
        // Fila 11 - La fila más superior con las piezas principales
        // En la imagen: a11=Torre, b11=Caballo, c11=Alfil, d11=Dama, e11=Rey, f11=Dama, g11=Alfil, h11=Caballo, i11=Torre

        // Usando r=-5 para la fila superior
        placePiece(-5, 0, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.BLACK);      // a11
        placePiece(-4, 0, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK);    // b11
        placePiece(-3, 0, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.BLACK);    // c11
        placePiece(-2, 0, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK);     // d11
        placePiece(-1, 0, ChessPiece.PieceType.KING, ChessPiece.PieceColor.BLACK);      // e11
        placePiece(0, 0, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK);      // f11
        placePiece(1, 0, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.BLACK);     // g11
        placePiece(2, 0, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK);     // h11
        placePiece(3, 0, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.BLACK);       // i11

        // Fila 10 - Peones negros
        placePiece(-5, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(-4, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(-3, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(-2, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(-1, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(0, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(1, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(2, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        placePiece(3, 1, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);

        // === PIEZAS BLANCAS (ABAJO) ===
        // Fila 2 - Peones blancos
        placePiece(-3, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(-2, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(-1, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(0, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(1, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(2, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(3, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(4, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        placePiece(5, 4, ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);

        // Fila 1 - Piezas principales blancas
        placePiece(-3, 5, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.WHITE);      // a1
        placePiece(-2, 5, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.WHITE);    // b1
        placePiece(-1, 5, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.WHITE);    // c1
        placePiece(0, 5, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.WHITE);      // d1
        placePiece(1, 5, ChessPiece.PieceType.KING, ChessPiece.PieceColor.WHITE);       // e1
        placePiece(2, 5, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.WHITE);      // f1
        placePiece(3, 5, ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.WHITE);     // g1
        placePiece(4, 5, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.WHITE);     // h1
        placePiece(5, 5, ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.WHITE);       // i1
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

    public void printColorDistribution() {
        int lightCount = 0, mediumCount = 0, darkCount = 0;

        for (HexCell cell : cells.values()) {
            switch (cell.getColor()) {
                case "light": lightCount++; break;
                case "medium": mediumCount++; break;
                case "dark": darkCount++; break;
            }
        }

        System.out.println("Total celdas: " + cells.size());
        System.out.println("Claras: " + lightCount);
        System.out.println("Medias: " + mediumCount);
        System.out.println("Oscuras: " + darkCount);
    }
}