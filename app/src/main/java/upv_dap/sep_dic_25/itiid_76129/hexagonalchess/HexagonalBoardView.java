package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

public class HexagonalBoardView extends View {
    private HexBoard board;
    private MoveValidator moveValidator;
    private Paint hexPaint, textPaint, highlightPaint, validMovePaint, dragPaint;
    private float hexSize = 50f;
    private float centerX, centerY;

    // Para drag and drop
    private HexCell draggedCell;
    private PointF dragPosition;
    private HexCell selectedCell;
    private List<HexCell> validMoves;

    // Listeners
    private OnCellClickListener cellClickListener;
    private OnMoveAttemptListener moveAttemptListener;

    public interface OnCellClickListener {
        void onCellClick(HexCell cell);
    }

    public interface OnMoveAttemptListener {
        void onMoveAttempt(HexCell from, HexCell to, OnMoveValidationCallback callback);
    }

    public interface OnMoveValidationCallback {
        void onMoveValidated(boolean isValid);
    }

    public HexagonalBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        board = new HexBoard();
        moveValidator = new MoveValidator(board);

        hexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hexPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(hexSize * 0.9f);; // Tamaño relativo al hexágono
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.BLACK);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(8f);
        highlightPaint.setColor(Color.YELLOW);

        validMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        validMovePaint.setStyle(Paint.Style.FILL);
        validMovePaint.setColor(Color.argb(100, 0, 255, 0));

        dragPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dragPaint.setStyle(Paint.Style.FILL);
        dragPaint.setAlpha(200);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        // Ajustar para radio 5 (11 hexágonos en el diámetro más ancho)
        hexSize = Math.min(w, h) / 20f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (board == null) return;

        // Dibujar todas las celdas
        for (HexCell cell : board.getAllCells().values()) {
            drawHexCell(canvas, cell);
        }
        //drawCoordinates(canvas);
        // Dibujar movimientos válidos si hay una celda seleccionada
        if (selectedCell != null && validMoves != null) {
            for (HexCell cell : validMoves) {
                drawValidMoveIndicator(canvas, cell);
            }
        }

        // Dibujar la celda seleccionada
        if (selectedCell != null && draggedCell == null) {
            drawHexHighlight(canvas, selectedCell);
        }

        // Dibujar la pieza siendo arrastrada
        if (draggedCell != null && dragPosition != null) {
            drawDraggedPiece(canvas);
        }
    }

    private void drawHexCell(Canvas canvas, HexCell cell) {
        PointF center = hexToPixel(cell.getQ(), cell.getR());

        // Determinar color del hexágono
        int color = getCellColor(cell);
        hexPaint.setColor(color);

        // Dibujar hexágono
        Path hexPath = createHexagonPath(center);
        canvas.drawPath(hexPath, hexPaint);

        // Dibujar borde más visible
        hexPaint.setStyle(Paint.Style.STROKE);
        hexPaint.setStrokeWidth(1.5f);
        hexPaint.setColor(Color.parseColor("#5D4037"));
        canvas.drawPath(hexPath, hexPaint);
        hexPaint.setStyle(Paint.Style.FILL);

        // Dibujar pieza (solo si no está siendo arrastrada)
        if (cell.getPiece() != null && cell != draggedCell) {
            drawPiece(canvas, center, cell.getPiece());
        }
    }

    private int getCellColor(HexCell cell) {
        // Colores que coinciden con la imagen de referencia
        switch (cell.getColor()) {
            case "light":
                return Color.parseColor("#ffce9e"); // Beige muy claro
            case "medium":
                return Color.parseColor("#e8ab6f"); // Café medio/naranja
            case "dark":
                return Color.parseColor("#d18b47"); // Café oscuro
            default:
                return Color.LTGRAY;
        }
    }

    private void drawPiece(Canvas canvas, PointF center, ChessPiece piece) {
        if (piece.getColor() == ChessPiece.PieceColor.WHITE) {
            textPaint.setColor(Color.WHITE);
        } else {
            textPaint.setColor(Color.parseColor("#000000"));
        }

        textPaint.setShadowLayer(4, 2, 2, Color.argb(180, 0, 0, 0));
        canvas.drawText(piece.getSymbol(), center.x, center.y + 15, textPaint);
        textPaint.clearShadowLayer();
    }

    private void drawDraggedPiece(Canvas canvas) {
        if (draggedCell == null || draggedCell.getPiece() == null) return;

        dragPaint.setColor(Color.argb(100, 255, 255, 0));
        canvas.drawCircle(dragPosition.x, dragPosition.y, hexSize * 0.8f, dragPaint);

        drawPiece(canvas, dragPosition, draggedCell.getPiece());
    }

    private void drawHexHighlight(Canvas canvas, HexCell cell) {
        PointF center = hexToPixel(cell.getQ(), cell.getR());
        Path hexPath = createHexagonPath(center);
        canvas.drawPath(hexPath, highlightPaint);
    }

    private void drawValidMoveIndicator(Canvas canvas, HexCell cell) {
        PointF center = hexToPixel(cell.getQ(), cell.getR());

        canvas.drawCircle(center.x, center.y, hexSize * 0.3f, validMovePaint);

        if (cell.getPiece() != null) {
            Paint capturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            capturePaint.setStyle(Paint.Style.STROKE);
            capturePaint.setStrokeWidth(5f);
            capturePaint.setColor(Color.RED);
            canvas.drawCircle(center.x, center.y, hexSize * 0.4f, capturePaint);
        }
    }

    private Path createHexagonPath(PointF center) {
        Path path = new Path();
        float radius = hexSize;

        // CORRECCIÓN: Orientación point-top (puntas arriba/abajo)
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI / 3 * i); // Sin offset = point-top
            float x = center.x + radius * (float) Math.cos(angle);
            float y = center.y + radius * (float) Math.sin(angle);

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleTouchDown(x, y);

            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(x, y);

            case MotionEvent.ACTION_UP:
                return handleTouchUp(x, y);

            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private boolean handleTouchDown(float x, float y) {
        HexCell cell = pixelToHex(x, y);

        if (cell != null && cell.getPiece() != null) {
            draggedCell = cell;
            dragPosition = new PointF(x, y);

            selectedCell = cell;
            validMoves = moveValidator.getValidMoves(cell);

            invalidate();
            return true;
        }

        return false;
    }

    private boolean handleTouchMove(float x, float y) {
        if (draggedCell != null) {
            dragPosition.set(x, y);
            invalidate();
            return true;
        }
        return false;
    }

    private boolean handleTouchUp(float x, float y) {
        if (draggedCell == null) return false;

        HexCell targetCell = pixelToHex(x, y);
        HexCell fromCell = draggedCell;

        draggedCell = null;
        dragPosition = null;

        if (targetCell != null && fromCell != targetCell) {
            if (moveValidator.isValidMove(fromCell, targetCell)) {
                if (moveAttemptListener != null) {
                    moveAttemptListener.onMoveAttempt(fromCell, targetCell, isValid -> {
                        selectedCell = null;
                        validMoves = null;
                        invalidate();
                    });
                    return true;
                }
            }
        }

        selectedCell = null;
        validMoves = null;
        invalidate();
        return true;
    }

    private void cancelDrag() {
        draggedCell = null;
        dragPosition = null;
        selectedCell = null;
        validMoves = null;
        invalidate();
    }

    // ==================== CONVERSIÓN DE COORDENADAS ====================

    private PointF hexToPixel(int q, int r) {
        // CORRECCIÓN: Fórmula para point-top orientation
        float x = hexSize * (float) (3.0 / 2 * q);
        float y = hexSize * (float) (Math.sqrt(3) * (r + q / 2.0));

        return new PointF(centerX + x, centerY + y);
    }

    private HexCell pixelToHex(float x, float y) {
        // CORRECCIÓN: Conversión inversa para point-top
        float relX = (x - centerX) / hexSize;
        float relY = (y - centerY) / hexSize;

        float q = (float) (2.0 / 3 * relX);
        float r = (float) ((-1.0 / 3 * relX + Math.sqrt(3) / 3 * relY));

        return hexRound(q, r);
    }

    private HexCell hexRound(float q, float r) {
        float s = -q - r;

        int rq = Math.round(q);
        int rr = Math.round(r);
        int rs = Math.round(s);

        float qDiff = Math.abs(rq - q);
        float rDiff = Math.abs(rr - r);
        float sDiff = Math.abs(rs - s);

        if (qDiff > rDiff && qDiff > sDiff) {
            rq = -rr - rs;
        } else if (rDiff > sDiff) {
            rr = -rq - rs;
        }

        return board.getCell(rq, rr);
    }

    // ==================== MÉTODOS PÚBLICOS ====================

    public HexBoard getBoard() {
        return board;
    }

    public void setBoard(HexBoard board) {
        this.board = board;
        this.moveValidator = new MoveValidator(board);
        invalidate();
    }

    public void clearSelection() {
        selectedCell = null;
        validMoves = null;
        draggedCell = null;
        dragPosition = null;
        invalidate();
    }

    public HexCell getSelectedCell() {
        return selectedCell;
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.cellClickListener = listener;
    }

    public void setOnMoveAttemptListener(OnMoveAttemptListener listener) {
        this.moveAttemptListener = listener;
    }
    // Método para debug - mostrar coordenadas
    private void drawCoordinates(Canvas canvas) {
        Paint coordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordPaint.setTextSize(hexSize * 0.60f);
        coordPaint.setColor(Color.BLUE);
        coordPaint.setTextAlign(Paint.Align.CENTER);

        for (HexCell cell : board.getAllCells().values()) {
            PointF center = hexToPixel(cell.getQ(), cell.getR());

            // Mostrar coordenadas (q,r)
            String coords = cell.getQ() + "," + cell.getR();
            canvas.drawText(coords, center.x, center.y, coordPaint);

            // Opcional: mostrar si tiene pieza
            if (cell.getPiece() != null) {
                coordPaint.setColor(Color.BLUE);
                String pieceInfo = cell.getPiece().getType().name().charAt(0) + "";
                canvas.drawText(pieceInfo, center.x, center.y + hexSize * 0.4f, coordPaint);
                coordPaint.setColor(Color.RED);
            }
        }
    }
}