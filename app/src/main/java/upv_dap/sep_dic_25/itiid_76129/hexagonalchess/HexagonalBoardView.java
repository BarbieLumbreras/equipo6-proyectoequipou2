package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.HexBoard;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.HexCell;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.ChessPiece;

import java.util.Map;

public class HexagonalBoardView extends View {
    private HexBoard board;
    private Paint paint;
    private Paint textPaint;
    private float hexSize = 50f;
    private float centerX, centerY;
    private HexCell selectedCell;
    private OnCellClickListener listener;

    public HexagonalBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        board = new HexBoard();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(40f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;

        // Ajustar tamaño del hexágono según el espacio disponible
        hexSize = Math.min(w, h) / 15f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (board == null) return;

        // Dibujar todas las celdas
        for (Map.Entry<String, HexCell> entry : board.getAllCells().entrySet()) {
            HexCell cell = entry.getValue();
            drawHexagon(canvas, cell);

            // Dibujar pieza si existe
            if (cell.getPiece() != null) {
                drawPiece(canvas, cell);
            }
        }

        // Resaltar celda seleccionada
        if (selectedCell != null) {
            paint.setColor(Color.YELLOW);
            paint.setAlpha(100);
            paint.setStyle(Paint.Style.FILL);
            drawHexagonPath(canvas, selectedCell);
        }
    }

    private void drawHexagon(Canvas canvas, HexCell cell) {
        // Convertir coordenadas axiales a píxeles
        float[] pixel = hexToPixel(cell.getQ(), cell.getR());

        // Color según el tipo de celda
        switch (cell.getColor()) {
            case "white":
                paint.setColor(Color.parseColor("#EEEEEE"));
                break;
            case "gray":
                paint.setColor(Color.parseColor("#999999"));
                break;
            case "black":
                paint.setColor(Color.parseColor("#555555"));
                break;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);

        Path hexPath = createHexagonPath(pixel[0], pixel[1]);
        canvas.drawPath(hexPath, paint);

        // Borde
        paint.setColor(Color.parseColor("#333333"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        canvas.drawPath(hexPath, paint);
    }

    private void drawHexagonPath(Canvas canvas, HexCell cell) {
        float[] pixel = hexToPixel(cell.getQ(), cell.getR());
        Path hexPath = createHexagonPath(pixel[0], pixel[1]);
        canvas.drawPath(hexPath, paint);
    }

    private Path createHexagonPath(float x, float y) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i;
            float vx = x + hexSize * (float) Math.cos(angle);
            float vy = y + hexSize * (float) Math.sin(angle);

            if (i == 0) {
                path.moveTo(vx, vy);
            } else {
                path.lineTo(vx, vy);
            }
        }
        path.close();
        return path;
    }

    private void drawPiece(Canvas canvas, HexCell cell) {
        float[] pixel = hexToPixel(cell.getQ(), cell.getR());
        ChessPiece piece = cell.getPiece();

        // Color del texto según el jugador
        if (piece.getColor() == ChessPiece.PieceColor.WHITE) {
            textPaint.setColor(Color.WHITE);
        } else {
            textPaint.setColor(Color.BLACK);
        }

        // Dibujar sombra para mejor legibilidad
        textPaint.setShadowLayer(3f, 1f, 1f, Color.argb(150, 0, 0, 0));

        canvas.drawText(piece.getSymbol(), pixel[0], pixel[1] + 15f, textPaint);
    }

    // Convertir coordenadas axiales (q,r) a píxeles
    private float[] hexToPixel(int q, int r) {
        float x = hexSize * (float) (Math.sqrt(3) * q + Math.sqrt(3)/2 * r);
        float y = hexSize * (3f/2f * r);
        return new float[] {centerX + x, centerY + y};
    }

    // Convertir píxeles a coordenadas axiales
    private int[] pixelToHex(float x, float y) {
        float relX = (x - centerX) / hexSize;
        float relY = (y - centerY) / hexSize;

        float q = (float) ((Math.sqrt(3)/3 * relX - 1.0/3 * relY));
        float r = (2.0f/3 * relY);

        return hexRound(q, r);
    }

    private int[] hexRound(float q, float r) {
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

        return new int[] {rq, rr};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int[] hex = pixelToHex(event.getX(), event.getY());
            HexCell cell = board.getCell(hex[0], hex[1]);

            if (cell != null && listener != null) {
                listener.onCellClick(cell);

                // Seleccionar/deseleccionar celda
                if (selectedCell == null || !selectedCell.equals(cell)) {
                    selectedCell = cell;
                } else {
                    selectedCell = null;
                }

                invalidate(); // Redibujar
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setBoard(HexBoard board) {
        this.board = board;
        invalidate();
    }

    public HexBoard getBoard() {
        return board;
    }

    public HexCell getSelectedCell() {
        return selectedCell;
    }

    public void clearSelection() {
        selectedCell = null;
        invalidate();
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.listener = listener;
    }

    public interface OnCellClickListener {
        void onCellClick(HexCell cell);
    }
}