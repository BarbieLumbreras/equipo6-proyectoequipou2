package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.FirebaseManager;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.GameState;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.HexBoard;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.HexCell;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.HexagonalBoardView;

public class MainActivity extends AppCompatActivity {
    private HexagonalBoardView boardView;
    private TextView tvGameId, tvTurn;
    private Button btnCreateGame, btnJoinGame;

    private FirebaseManager firebaseManager;
    private HexCell selectedCell;
    private boolean isMyTurn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        firebaseManager = new FirebaseManager();
        setupListeners();
    }

    private void initializeViews() {
        boardView = findViewById(R.id.hexBoard);
        tvGameId = findViewById(R.id.tvGameId);
        tvTurn = findViewById(R.id.tvTurn);
        btnCreateGame = findViewById(R.id.btnCreateGame);
        btnJoinGame = findViewById(R.id.btnJoinGame);
    }

    private void setupListeners() {
        // Botón crear partida
        btnCreateGame.setOnClickListener(v -> createGame());

        // Botón unirse a partida
        btnJoinGame.setOnClickListener(v -> showJoinGameDialog());

        // Click en celdas del tablero
        boardView.setOnCellClickListener(cell -> handleCellClick(cell));
    }

    private void createGame() {
        firebaseManager.createGame(new FirebaseManager.OnGameCreatedListener() {
            @Override
            public void onGameCreated(String gameId) {
                tvGameId.setText("ID: " + gameId);
                Toast.makeText(MainActivity.this,
                        "Partida creada. Comparte el ID: " + gameId,
                        Toast.LENGTH_LONG).show();

                listenToGameUpdates();
                enableButtons(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showJoinGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unirse a partida");

        final EditText input = new EditText(this);
        input.setHint("Ingresa el ID de la partida");
        builder.setView(input);

        builder.setPositiveButton("Unirse", (dialog, which) -> {
            String gameId = input.getText().toString().trim();
            if (!gameId.isEmpty()) {
                joinGame(gameId);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void joinGame(String gameId) {
        firebaseManager.joinGame(gameId, new FirebaseManager.OnGameJoinedListener() {
            @Override
            public void onGameJoined(String gameId) {
                tvGameId.setText("ID: " + gameId);
                Toast.makeText(MainActivity.this,
                        "Te has unido a la partida", Toast.LENGTH_SHORT).show();

                listenToGameUpdates();
                enableButtons(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenToGameUpdates() {
        firebaseManager.listenToGame(new FirebaseManager.OnGameUpdateListener() {
            @Override
            public void onGameUpdate(GameState gameState) {
                updateBoard(gameState);
                updateTurnDisplay(gameState);
                checkMyTurn(gameState);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBoard(GameState gameState) {
        HexBoard board = boardView.getBoard(); // Necesitarías crear el método getBoard() en tu vista
        // Limpiar todas las piezas
        for (HexCell cell : board.getAllCells().values()) {
            cell.setPiece(null);
        }

        // Colocar piezas según Firebase
        if (gameState.getPieces() != null) {
            for (String key : gameState.getPieces().keySet()) {
                GameState.PieceData pieceData = gameState.getPieces().get(key);
                String[] coords = key.split(",");
                int q = Integer.parseInt(coords[0]);
                int r = Integer.parseInt(coords[1]);

                HexCell cell = board.getCell(q, r);
                if (cell != null && pieceData != null) {
                    cell.setPiece(pieceData.toPiece());
                }
            }
        }

        boardView.invalidate();
    }

    private void updateTurnDisplay(GameState gameState) {
        String turn = gameState.getCurrentTurn();
        tvTurn.setText("Turno: " + (turn.equals("white") ? "Blancas" : "Negras"));
        tvTurn.setTextColor(turn.equals("white") ?
                getColor(android.R.color.white) :
                getColor(android.R.color.darker_gray));
    }

    private void checkMyTurn(GameState gameState) {
        String myId = firebaseManager.getPlayerId();
        boolean amWhite = myId.equals(gameState.getWhitePlayerId());
        boolean amBlack = myId.equals(gameState.getBlackPlayerId());

        String currentTurn = gameState.getCurrentTurn();
        isMyTurn = (amWhite && "white".equals(currentTurn)) ||
                (amBlack && "black".equals(currentTurn));
    }

    // En MainActivity.java

    private void handleCellClick(HexCell cell) {
        if (!isMyTurn) {
            Toast.makeText(this, "No es tu turno", Toast.LENGTH_SHORT).show();
            // Limpiamos la selección si el usuario hace clic fuera de su turno
            selectedCell = null;
            boardView.clearSelection();
            return;
        }

        // Si no hay celda seleccionada...
        if (selectedCell == null) {
            // ...solo se puede seleccionar una celda si tiene una pieza DEL COLOR DEL JUGADOR.
            if (cell.getPiece() != null && isMyPiece(cell.getPiece())) {
                selectedCell = cell;
                Toast.makeText(this, "Pieza seleccionada", Toast.LENGTH_SHORT).show();
                // Opcional: podrías decirle a tu boardView que resalte la celda seleccionada
                // boardView.highlightSelection(cell);
            } else if (cell.getPiece() != null) {
                // Esto ocurre si el jugador intenta seleccionar una pieza del oponente
                Toast.makeText(this, "No puedes mover esa pieza", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Si ya hay una celda seleccionada, intentamos mover la pieza

            // Opcional pero recomendado: evitar "moverse" a la misma celda
            if (selectedCell.equals(cell)) {
                selectedCell = null;
                boardView.clearSelection();
                Toast.makeText(this, "Selección cancelada", Toast.LENGTH_SHORT).show();
                return;
            }

            String fromKey = selectedCell.getQ() + "," + selectedCell.getR();
            String toKey = cell.getQ() + "," + cell.getR();

            makeMove(fromKey, toKey);
            selectedCell = null;
            boardView.clearSelection(); // Limpia la selección después de intentar mover
        }
    }

    private boolean isMyPiece(ChessPiece piece) {
        if (piece == null) return false;

        // Obtiene el ID del jugador y el estado actual de la partida desde FirebaseManager
        String myId = firebaseManager.getPlayerId();
        GameState gameState = firebaseManager.getCurrentGameState(); // Necesitarás este método

        if (gameState == null) return false; // Si aún no se ha cargado el estado del juego

        boolean amWhite = myId.equals(gameState.getWhitePlayerId());
        boolean amBlack = myId.equals(gameState.getBlackPlayerId());

        // La pieza es mía si soy el jugador blanco y la pieza es blanca,
        // o si soy el jugador negro y la pieza es negra.
        return (amWhite && piece.getColor() == ChessPiece.PieceColor.WHITE) ||
                (amBlack && piece.getColor() == ChessPiece.PieceColor.BLACK);
    }


    private void makeMove(String fromKey, String toKey) {
        firebaseManager.makeMove(fromKey, toKey,
                new FirebaseManager.OnMoveCompleteListener() {
                    @Override
                    public void onMoveComplete() {
                        Toast.makeText(MainActivity.this,
                                "Movimiento realizado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this,
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void enableButtons(boolean enable) {
        btnCreateGame.setEnabled(enable);
        btnJoinGame.setEnabled(enable);
    }
}