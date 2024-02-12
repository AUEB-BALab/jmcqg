package gr.aueb.dmst.dds.jmcqg;

import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Supplier;
import java.util.Optional;
import javax.imageio.ImageIO;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.IOException;

import gr.aueb.dmst.dds.jmcqg.Util;
import gr.aueb.dmst.dds.jmcqg.Imager;
import gr.aueb.dmst.dds.jmcqg.Question;
import gr.aueb.dmst.dds.jmcqg.QuestionException;
import gr.aueb.dmst.dds.jmcqg.QuestionIterator;
import gr.aueb.dmst.dds.jmcqg.QuestionTester;

/** Generate questions and answers in diverse formats */
public class Cli {

    /** Replace GIFT special characters with their escaped version */
    public static String escapeGift(String input) {
        return input.replace("~", "\\~")
                    .replace("=", "\\=")
                    .replace("#", "\\#")
                    .replace(":", "\\:")
                    .replace("{", "\\{")
                    .replace("}", "\\}");
    }

    /** Replace HTML special characters with their escpaed version */
    public static String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\n", "<br>")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    /** Display program's usage */
    private static void usage(PrintStream out) {
        out.println("""
jmcqg [-hlt] [-g|-p file] [-n number]
-h, --help          Print this message and exit.
-l, --list          List available questions and exit.
-g, --gift          Generate output in GIFT format
-n, --number number Generate specified number of questions per category.
-p, --png zipfile   Generate GIFT with PNG code output in the specified file.
-q, --question      Generate only the specified question.
-s, --seed number   Seed RNG with specified number
-t, --test          Test each generated question.
""");
    }

    /** Append the question in GIFT format to the passed StringBuilder */
    private static String giftGenerator(StringBuilder buffer, Question q,
            Optional<ZipOutputStream> zos, Optional <String> entryName)
            throws IOException {
        buffer.append("\n$CATEGORY: " + q.getCategory() + "\n\n");
        buffer.append("::" + q.getCategory()
                + "::[html]<p>Based on the following code, what value will a call to "
                + q.getAnswerCall() + " return?</p>");
        if (zos.isPresent()) {
            ZipEntry zipEntry = new ZipEntry(entryName.get());
            zos.get().putNextEntry(zipEntry);

            ImageIO.write(Imager.getImage(q.getQuestionCode()), "PNG",
                    zos.get());
            zos.get().closeEntry();
            buffer.append("<p><img  src\\=\"@@PLUGINFILE@@/");
            buffer.append(entryName.get());
            buffer.append("\"/></p>{\n");
        } else {
            buffer.append("<p><pre>");
            buffer.append(escapeGift(escapeHtml(q.getQuestionCode())));
            buffer.append("</pre>{\n");
        }
        int n = 0;
        for (Object ai : q.getAnswers()) {
            // First answer is the correct one
            buffer.append('\t');
            buffer.append(n++ == 0 ? '=' : '~');
            buffer.append("<pre>" + ai + "</pre>\n");
        }
        buffer.append("}\n");
        return buffer.toString();
    }

    /** Return the question in text format to the passed StringBuilder */
    private static String textGenerator(StringBuilder buffer, Question q) {
        buffer.append("Με βάση τον παρακάτω κώδικα, τι τιμή θα επιστρέψει η κλήση "
                + q.getAnswerCall() + ";\n");
        buffer.append(q.getQuestionCode());
        buffer.append("\n");
        int n = 0;
        for (Object ai : q.getAnswers()) {
            // First answer is the correct one
            buffer.append(ai + "\n");
        }
        buffer.append("\n");
        return buffer.toString();
    }

    /** Write the specified text to the zip file */
    private static void writeQuestions(StringBuilder text,
            ZipOutputStream zos) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    text.toString().getBytes());
            ZipEntry zipEntry = new ZipEntry("questions.txt");
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = bais.read(buffer)) > 0)
                zos.write(buffer, 0, length);

            zos.closeEntry();
            bais.close();
    }

    public static void main(String[] args) {
        // Set output to UTF-8
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // This shouldn't happen for UTF-8
        }

        // Parse options
        boolean test = false;
        boolean giftFormat = false;
        boolean listQuestions = false;
        int numQuestions = 1;
        Optional <String> onlyQuestion = Optional.empty();
        Optional <String> pngZipFile = Optional.empty();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-g":
                case "--gift":
                    giftFormat = true;
                    break;
                case "-h":
                case "--help":
                    usage(System.out);
                    System.exit(0);
                    break;
                case "-l":
                case "--list":
                    listQuestions = true;
                    break;
                case "-n":
                case "--number":
                    if (i + 1 >= args.length) {
                        System.out.println("No number value provided");
                        usage(System.err);
                        System.exit(1);
                    }
                    try {
                        numQuestions = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.err.println("Number is not a valid integer.");
                        usage(System.err);
                        System.exit(1);
                    }
                    break;
                case "-p":
                case "--png":
                    if (i + 1 >= args.length) {
                        System.out.println("No question name provided");
                        usage(System.err);
                        System.exit(1);
                    }
                    pngZipFile = Optional.of(args[++i]);
                    break;
                case "-q":
                case "--question":
                    if (i + 1 >= args.length) {
                        System.out.println("No question name provided");
                        usage(System.err);
                        System.exit(1);
                    }
                    onlyQuestion = Optional.of(args[++i]);
                    break;
                case "-s":
                case "--seet":
                    if (i + 1 >= args.length) {
                        System.out.println("No seed value provided");
                        usage(System.err);
                        System.exit(1);
                    }
                    try {
                        Util.seed(Integer.parseInt(args[++i]));
                    } catch (NumberFormatException e) {
                        System.err.println("Number is not a valid integer.");
                        usage(System.err);
                        System.exit(1);
                    }
                    break;
                case "-t":
                case "--test":
                    test = true;
                    break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
                    usage(System.err);
                    break;
            }
        }

        StringBuilder buffer = new StringBuilder();
        Optional <ZipOutputStream> zos = Optional.empty();
        int pngFileNumber = 0;

        try {
            if (pngZipFile.isPresent()) {
                // Prepare the zip file
                FileOutputStream fos = new FileOutputStream(pngZipFile.get());
                zos = Optional.of(new ZipOutputStream(fos));
            }

            // Generate questions
            var questions = new QuestionIterator();
            for (Supplier<Question> qi : questions) {

                if (listQuestions) {
                    System.out.print(qi.get().getCategory() + "\n");
                    continue;
                }

                for (int i = 0; i < numQuestions; i++) {
                    Question q = qi.get();
                    if (onlyQuestion.isPresent()
                            && !q.getCategory().equals(onlyQuestion.get()))
                        continue;

                    if (test)
                        try {
                            QuestionTester.test(q);
                        } catch (QuestionException e) {
                            System.err.print("Question "
                                    + q.getCategory()
                                    + " failed: " + e + " (skipping)");
                            continue;
                        }

                    if (pngZipFile.isPresent()) {
                        Optional <String> entryName = Optional.of(
                                String.format("images/%05d.png",
                                    pngFileNumber++));
                        giftGenerator(buffer, q, zos, entryName);
                    } else if (giftFormat) {
                        buffer.setLength(0);
                        giftGenerator(buffer, q, zos, Optional.empty());
                        System.out.print(buffer);
                    } else {
                        buffer.setLength(0);
                        textGenerator(buffer, q);
                        System.out.print(buffer);
                    }
                }
            }

            // Write the accumulated question text
            if (zos.isPresent()) {
                writeQuestions(buffer, zos.get());
                zos.get().close();
            }
        } catch (IOException e) {
            System.err.print("I/O error: " + e);
            System.exit(1);
        }
    }
}
