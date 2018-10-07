package io.sonocoin.sdk.Sound;

/**
 * Encoder config
 * Created by sciner on 05.10.2017
 */
public class SonoConf {

    // constants
    public static final int START_FREQUENCY = 6000; // bottom signal position
    public static final int START_FREQUENCY_MIRROR = 11000; // bottom mirror signal position
    public static final double SYMBOL_DISTANCE_IN_FREQUENCY = 135.17241; // Vertical distance between signal lines (in Hz)
    public static final int COPY_OFFSET_X = 10; // Horizontal offset of a copy of the signal (in pixels)
    public static final String START_SIGNAL_CHAR = "*";
    public static final String END_SIGNAL_CHAR = "#";
    public static final String ALPHABET = "abcdefghjknpqrstuvwxyz0123456789"; // important: without * and #

    // options
    public double symbol_distance_in_frequency;     // Vertical distance between signal lines (in Hz)
    public int start_frequency;                     // bottom signal position,
    public int start_frequency_mirror;              // bottom mirror signal position,
    public int start_signal_mark;                   // Y-coordinate on bitmap of bottom point (745)
    public int end_signal_mark;                     // Y-coordinate on bitmap of top point (538)
    public int copy_offset_x;                       // Horizontal offset of a copy of the signal (in pixels)
    public int copy_offset_y;                       // (int)round((self::START_FREQUENCY - self::START_FREQUENCY_MIRROR) / $vertical_frequency_resolution), // X-coordinate horizontal offset on bitmap of bottom point of duplicate signal (-232)
    public String alphabet;                         // important: without * and #
    public int alphabet_length;                     // + start and end signal markers
    public double one_char_height;                  //
    public int min_line_length;                     // Minimal size of signal, which isn't being ignored
    public String start_signal_char;                // *
    public String end_signal_char;                  // #
    public int signal_height;                       //
    public int spectrum_height;                     // in pixels
    public int frequency;

    public static SonoConf getConf(int freq) {
        int spectrum_height = 1025;
        int spectrum_frequency_max_value = freq / 2; // in Hz
        double vertical_frequency_resolution = (double) spectrum_frequency_max_value / (double) spectrum_height; // Hz per 1 pixel
        int alphabet_length = ALPHABET.length() + 2; // start and end signal markers
        int start_signal_mark = (int) Math.round((double) (spectrum_frequency_max_value - START_FREQUENCY) / (double) vertical_frequency_resolution);
        int end_signal_mark = (int) Math.round((double) (spectrum_frequency_max_value - (START_FREQUENCY + (alphabet_length - 1) * SYMBOL_DISTANCE_IN_FREQUENCY)) / (double) vertical_frequency_resolution); // 538
        int signal_height = (int) Math.abs(start_signal_mark - end_signal_mark);
        double one_char_height = (double) signal_height / (double) (alphabet_length - 1); // 6.27272727273

        SonoConf conf = new SonoConf();
        conf.symbol_distance_in_frequency = SYMBOL_DISTANCE_IN_FREQUENCY;
        conf.start_frequency = START_FREQUENCY;
        conf.start_frequency_mirror = START_FREQUENCY_MIRROR;
        conf.start_signal_mark = start_signal_mark; // Y-coordinate on bitmap of bottom point (745)
        conf.end_signal_mark = end_signal_mark; // Y-coordinate on bitmap of top point (538)
        conf.copy_offset_y = (int) Math.round((START_FREQUENCY - START_FREQUENCY_MIRROR) / vertical_frequency_resolution); // X-coordinate horizontal offset on bitmap of bottom point of duplicate signal (-232)
        conf.copy_offset_x = COPY_OFFSET_X; // Y-coordinate vertical offset on bitmap of bottom point of duplicate signal (10)
        conf.signal_height = signal_height;
        conf.alphabet = ALPHABET;
        conf.alphabet_length = alphabet_length;
        conf.one_char_height = one_char_height;
        conf.min_line_length = 5; // Minimal size of signal, which isn't being ignored
        conf.start_signal_char = START_SIGNAL_CHAR; //
        conf.end_signal_char = END_SIGNAL_CHAR; //
        conf.spectrum_height = spectrum_height; //
        conf.frequency = freq;

        return conf;
    }

}
