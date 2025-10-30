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
        // Patrón verificado para ajedrez hexagonal Gliński
        // Usando la fórmula: (q - r) mod 3

        int colorValue = (q - r) % 3;
        if (colorValue < 0) colorValue += 3;

        switch (colorValue) {
            case 0: return "medium";
            case 1: return "light";
            case 2: return "dark";
            default: return "light";
        }
    }

    // Posiciones según tablero estándar de Gliński
    // Mirando la imagen de referencia con fondo blanco:
    // - Negras arriba (filas 10-11)
    // - Blancas abajo (filas 1-2)
    private void setupInitialPieces() {

        placePiece(0, 0, ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK);

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