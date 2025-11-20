package academy.util;

public final class FormatUtils {

    private FormatUtils() {}

    public static String formatNumber(int number) {
        return String.format("%,d", number).replace(",", "_");
    }

    public static String formatBytes(double bytes) {
        if (bytes < 1024) {
            return String.format("%.0fb", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2fkb", bytes / 1024);
        } else {
            return String.format("%.2fmb", bytes / (1024 * 1024));
        }
    }
}
