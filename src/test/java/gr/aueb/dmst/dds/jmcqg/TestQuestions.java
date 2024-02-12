package gr.aueb.dmst.dds.jmcqg;

import java.util.function.Supplier;
import java.util.Spliterators;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.aueb.dmst.dds.jmcqg.Question;
import gr.aueb.dmst.dds.jmcqg.QuestionException;
import gr.aueb.dmst.dds.jmcqg.QuestionIterator;
import gr.aueb.dmst.dds.jmcqg.QuestionTester;

public class TestQuestions {
    /** Number of tests for each question */
    private static final int TEST_REPETITIONS;
    // Static initializer block to safely convert and set the constant
    static {
        String propertyValue = System.getProperty("test_repetitions", "1");
        int temp;
        try {
            temp = Integer.parseInt(propertyValue);
        } catch (NumberFormatException e) {
            temp = 1;
            System.err.println("The value of myProperty is not a valid integer: " + propertyValue + ". Defaulting to 1.");
        }
        TEST_REPETITIONS = temp;
    }

    private static final Logger logger = LoggerFactory.getLogger(TestQuestions.class);

    @Test
    public void testCallResult() {
        Object obj = null;
        try {
            obj = QuestionTester.callResult(
                    """
    public class Example {
        public static int value() {
            return 42;
        }
    }
                    """, "Example.value()");
        } catch (Exception e) {
            // Fail the test if the exception is caught
            fail("Code evaluation failed: " + e.getMessage());
        }

        assertTrue(obj instanceof Integer, "Object is not an instance of Integer");
        assertEquals(Integer.valueOf(42), obj, "Object is not equal to Integer(42)");
    }

    public static Stream<Supplier<Question>> questionProvider() {
        var questions = new QuestionIterator();
        // Return a sequential stream
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(questions, Spliterator.ORDERED), false);
    }

    @ParameterizedTest
    @MethodSource("questionProvider")
    public void testQuestion(Supplier<Question> supplier) {
        assertNotNull(supplier);
        Question q = supplier.get();
        logger.info("Testing question " + q.getCategory());
        try {
            for (int i = 0; i < TEST_REPETITIONS; i++) {
                q = supplier.get();
                QuestionTester.test(q);
            }
        } catch (QuestionException e) {
            // Fail the test if the exception is caught
            fail("Exception testing failed: " + e.getMessage());
        }
    }
}
