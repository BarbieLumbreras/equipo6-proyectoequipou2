package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import android.util.Log;

/**
 * Clase auxiliar para debuggear el tablero y verificar posiciones
 */
public class DebugHelper {
    private static final String TAG = "HexChessDebug";

    /**
     * Imprime todas las piezas en el tablero con sus coordenadas
     */
    public static void printAllPieces(HexBoard board) {
        Log.d(TAG, "=== TODAS LAS PIEZAS EN EL TABLERO ===");

        for (int r = -5; r <= 5; r++) {
            StringBuilder line = new StringBuilder(String.format("Fila r=%2d: ", r));
            for (int q = -5; q <= 5; q++) {
                HexCell cell = board.getCell(q, r);
                if (cell != null && cell.getPiece() != null) {
                    ChessPiece piece = cell.getPiece();
                    String symbol = piece.getSymbol();
                    String color = piece.getColor() == ChessPiece.PieceColor.WHITE ? "W" : "N";
                    line.append(String.format("(%d,%d)%s%s ", q, r, color, symbol));
                }
            }
            if (line.length() > 15) { // Si hay piezas en esta fila
                Log.d(TAG, line.toString());
            }
        }

        Log.d(TAG, "=== FIN ===");
    }

    /**
     * Verifica que las piezas clave estén en su posición
     */
    public static void verifyKeyPieces(HexBoard board) {
        Log.d(TAG, "=== VERIFICACIÓN DE PIEZAS CLAVE ===");

        // Verificar reyes
        boolean whiteKingOk = checkPiece(board, 1, 4, ChessPiece.PieceType.KING, ChessPiece.PieceColor.WHITE);
        boolean blackKingOk = checkPiece(board, -1, -4, ChessPiece.PieceType.KING, ChessPiece.PieceColor.BLACK);

        Log.d(TAG, "Rey Blanco en posición correcta: " + whiteKingOk);
        Log.d(TAG, "Rey Negro en posición correcta: " + blackKingOk);

        // Contar peones
        int whitePawns = 0;
        int blackPawns = 0;

        for (HexCell cell : board.getAllCells().values()) {
            if (cell.getPiece() != null && cell.getPiece().getType() == ChessPiece.PieceType.PAWN) {
                if (cell.getPiece().getColor() == ChessPiece.PieceColor.WHITE) {
                    whitePawns++;
                } else {
                    blackPawns++;
                }
            }
        }

        Log.d(TAG, "Peones Blancos: " + whitePawns + " (esperado: 9)");
        Log.d(TAG, "Peones Negros: " + blackPawns + " (esperado: 9)");

        Log.d(TAG, "=== FIN VERIFICACIÓN ===");
    }

    private static boolean checkPiece(HexBoard board, int q, int r,
                                      ChessPiece.PieceType expectedType,
                                      ChessPiece.PieceColor expectedColor) {
        HexCell cell = board.getCell(q, r);
        if (cell == null) {
            Log.d(TAG, String.format("Celda (%d,%d) no existe", q, r));
            return false;
        }

        ChessPiece piece = cell.getPiece();
        if (piece == null) {
            Log.d(TAG, String.format("Celda (%d,%d) está vacía", q, r));
            return false;
        }

        boolean typeOk = piece.getType() == expectedType;
        boolean colorOk = piece.getColor() == expectedColor;

        if (!typeOk || !colorOk) {
            Log.d(TAG, String.format("Celda (%d,%d) tiene %s %s pero se esperaba %s %s",
                    q, r, piece.getColor(), piece.getType(), expectedColor, expectedType));
        }

        return typeOk && colorOk;
    }

    /**
     * Imprime el tablero en formato ASCII para visualización
     */
    public static void printBoardASCII(HexBoard board) {
        Log.d(TAG, "=== TABLERO ASCII ===");

        for (int r = -5; r <= 5; r++) {
            StringBuilder line = new StringBuilder();

            // Indentación para crear forma hexagonal
            int indent = Math.abs(r);
            for (int i = 0; i < indent; i++) {
                line.append("  ");
            }

            line.append(String.format("r=%2d ", r));

            for (int q = -5; q <= 5; q++) {
                HexCell cell = board.getCell(q, r);
                if (cell != null) {
                    if (cell.getPiece() != null) {
                        line.append(cell.getPiece().getSymbol());
                    } else {
                        line.append("·");
                    }
                    line.append(" ");
                }
            }

            Log.d(TAG, line.toString());
        }

        Log.d(TAG, "=== FIN TABLERO ===");
    }
}