package gr.aueb.dmst.dds.jmcqg;


import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;

import gr.aueb.dmst.dds.jmcqg.Question;

public class QuestionIterator implements Iterable<Supplier<Question>>,
       Iterator<Supplier<Question>> {
    private Iterator<Class<?>> classIterator;

    /** Package containing the questions */
    private static String QUESTION_PACKAGE = "gr.aueb.dmst.dds.jmcqg.questions";

    public QuestionIterator() {
        // Create a Reflections instance configured to scan the specified package
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(QUESTION_PACKAGE))
                // false means don't exclude Object.class
                .setScanners(new SubTypesScanner(false))
                .filterInputsBy(new FilterBuilder().includePackage(QUESTION_PACKAGE)));

        // Get all classes in the package
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        classIterator = classes.iterator();
    }

    @Override
    public Iterator<Supplier<Question>> iterator() {
        return this; // Return the current instance, which is also an Iterator
    }

    @Override
    public boolean hasNext() {
        return classIterator.hasNext();
    }

    @Override
    public Supplier<Question> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Class<?> clazz = classIterator.next();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            return (Supplier<Question>) instance;
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException  e) {
            throw new RuntimeException("Failed to instantiate the class", e);
        }
    }

    // Example usage
    public static void main(String[] args) {
        var questions = new QuestionIterator();

        // Print out the questions
        System.out.println("Questions found in package "
                + QUESTION_PACKAGE + ":");
        for (Supplier<Question> question : questions) {
            System.out.println(question.get());
        }
    }
}
