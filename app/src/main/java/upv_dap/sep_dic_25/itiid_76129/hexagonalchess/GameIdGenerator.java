
package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import java.util.Random;

public class GameIdGenerator {

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 6; // Puedes ajustar la longitud aquí

    /**
     * Genera un ID de sala alfanumérico, corto y en mayúsculas.
     * @return Un String como "A4F2K9".
     */
    public static String generateNewId() {
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        while (result.length() < ID_LENGTH) {
            int index = (int) (random.nextFloat() * ALPHANUMERIC_CHARS.length());
            result.append(ALPHANUMERIC_CHARS.charAt(index));
        }
        return result.toString();
    }
}
