package gr.aueb.dmst.dds.jmcqg;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Vector;

/** A multiple choice question */
public class Question {
    /** Question's category (class name) */
    private String category;

    /** Question template with {value} elemements */
    private String template;

    /** Replacements for template elements */
    private Map<String, Object> replacements;

    /** Call of the method returning the answer */
    private String answerCall;

    /** Possible answers; first on is the correct one */
    private Vector<Object> answers;

    // Pattern to match words within braces
    private Pattern pattern = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    private static String getCallerClassName() {
        // Get the current stack trace
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        // stackTraceElements[0] is getStackTrace
        // stackTraceElements[1] is getCallerClassName (this method)
        // stackTraceElements[2] is the caller of getCallerClassName
        // stackTraceElements[3] is the constructor's class
        
        try {
            if (stackTraceElements.length > 3) {
                Class<?> callerClass = Class.forName(stackTraceElements[3].getClassName());
                return callerClass.getSimpleName();
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Question's class not found: " + e);
        }
        throw new IllegalStateException("Cannot determine question's class");
    }

    /**
     * Question constructor
     *
     * @param template The code of the question to pose.
     *   Can include names in braces.
     * @param replacements The replacement for each template name.
     * @param answerCall The method call returning the answer when the code
     *   is run.
     * @param answers Possible answers to the question. The first one is
     *   the correct one.
     */
    public Question(String template, Map<String, Object> replacements,
            String answerCall, Object... answers)
    {
        this.template = template;
        this.replacements = replacements;
        this.answerCall = answerCall;
        this.category = getCallerClassName();

        this.answers = new Vector<Object>();
        for (var a : answers)
            this.answers.add(a);
    }

    /** Return the question's Java code */
    public String getQuestionCode() {
        var result = new StringBuilder();

        // Start index for next append operation
        int start = 0;

        var matcher = pattern.matcher(template);
        while (matcher.find()) {
            // Append part of the string before the current match
            result.append(template, start, matcher.start());

            // Get the matched key (without braces)
            String key = matcher.group(1);

            // Replace with value from the map, if present
            result.append(replacements.getOrDefault(key, matcher.group(0)));

            // Update start index
            start = matcher.end();
        }

        // Append the remainder of the template string
        result.append(template.substring(start));

        return result.toString();
    }

    /** Return an interator over the question's possible answers */
    public List<Object> getAnswers() {
        return Collections.unmodifiableList(answers);
    }

    /** Return the variable containing the result */
    public String getAnswerCall() {
        return answerCall;
    }

    /** Return the question's category */
    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        var result = new StringBuilder();

        result.append(getQuestionCode());
        result.append("\n\n");

        String av = getAnswerCall();
        int i = 1;
        for (var answer : getAnswers()) {
            result.append(String.valueOf(i) + ". " + av + " == " + answer + "\n");
            i += 1;
        }

        return result.toString();
    }
}
