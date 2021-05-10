import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

final class Main {
    private static final class Line {
        private final String sentence;
        private final long letterDelay;
        private final long endDelay;
        Line(final String sentence, final long letterDelay, final long endDelay) {
            this.sentence = sentence;
            this.letterDelay = letterDelay;
            this.endDelay = endDelay;
        }
        final void print() {
            sentence.chars().forEach(c -> {
                try {
                    Thread.sleep(letterDelay);
                } catch (final InterruptedException e) {}
                System.out.print((char) c);
            });
            try {
                Thread.sleep(endDelay);
            } catch (final InterruptedException e) {}
            System.out.println();
        }
    }

    private static enum ANSI {
        RESET("0m"),
        BRIGHT_WHITE("0;97m"),
        WHITE("0;37m"),
        BRIGHT_BLACK("0;90m"),
        BLACK("0;30m"),
        HIDE_CURSOR("?25l"),
        SHOW_CURSOR("?25h");
        private final String code;
        private ANSI(final String code) {
            this.code = "\u001b[" + code;
        }
        @Override
        public final String toString() {
            return code;
        }
    }

    private static final Line[] LINES = {
        new Line("Hello there!", 60, 500),
        new Line("Never have I seen you before.", 60, 200),
        new Line("Gonna make some tea if you want.", 60, 200),
        new Line("Give me a sec...", 60, 1500),
        new Line("You must be from there.", 60, 200),
        new Line("Up there, I mean.", 60, 200),
    };
    private static final String FULL_SCRIPT =
        "\\Hello there!\n" +
        ANSI.BRIGHT_WHITE + "Never\\ have I seen you before.\n" +
        ANSI.BRIGHT_WHITE + "Gonna\\ make some tea if you want.\n" +
        ANSI.BRIGHT_WHITE + "Give\\ me a sec...\n" +
        ANSI.BRIGHT_WHITE + "You\\ must be from there.\n" +
        ANSI.BRIGHT_WHITE + "Up\\ there, I mean.";

    public static final void main(final String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(ANSI.SHOW_CURSOR);
            System.out.println(ANSI.RESET);
            clearScreen();
        }));
        System.out.print(ANSI.HIDE_CURSOR);
        System.out.print(ANSI.BRIGHT_WHITE);
        clearScreen();
        Arrays.stream(LINES).forEach(Line::print);
        fade();
        play();
        video();
    }

    private static final void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/Q", "/C", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("bash", "-c", "clear").inheritIO().start().waitFor();
            }
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println(ANSI.SHOW_CURSOR);
            System.exit(0);
        }
    }

    private static final void fade() {
        try {
            Thread.sleep(1000);
            clearScreen();
            System.out.println(FULL_SCRIPT.replace("\\", ANSI.WHITE.toString()));
            Thread.sleep(1000);
            clearScreen();
            System.out.println(FULL_SCRIPT.replace("\\", ANSI.BRIGHT_BLACK.toString()));
            Thread.sleep(1000);
            clearScreen();
            System.out.println(FULL_SCRIPT.replace("\\", ANSI.BLACK.toString()));
            Thread.sleep(1000);
        } catch (final InterruptedException e) {}
    }

    private static final void play() {
        try {
            final Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File("bgmusic.wav")));
            clip.start();
        } catch (final IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static final void video() {
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File("resources.txt")))) {
            clearScreen();
            final double fps = 28.1;
            final double timePerFrame = 1.0 / fps;
            final StringBuilder buffer = new StringBuilder();
            long frame = 0;
            double nextFrame = 0;
            final double start = System.currentTimeMillis() / 1000.0;
            int lineNum = 0;
            for (final String line : reader.lines().toArray(String[]::new)) {
                if (lineNum % 32 == 0) {
                    ++frame;
                    System.out.print(buffer);
                    buffer.setLength(0);
                    final double elapsed = System.currentTimeMillis() / 1000.0 - start;
                    final double repose = (frame * timePerFrame) - elapsed;
                    if (repose > 0) {
                        Thread.sleep((long) (repose * 1000));
                    }
                    nextFrame = elapsed / timePerFrame;
                }
                if (frame >= nextFrame) {
                    buffer.append(line).append("\n");
                }
                ++lineNum;
            }
        } catch (final IOException | InterruptedException e) {}
    }
}
