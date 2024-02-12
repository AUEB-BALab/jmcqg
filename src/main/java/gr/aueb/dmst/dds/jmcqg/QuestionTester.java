package gr.aueb.dmst.dds.jmcqg;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import java.util.Vector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import gr.aueb.dmst.dds.jmcqg.Question;
import gr.aueb.dmst.dds.jmcqg.QuestionException;

public class QuestionTester {

    /** Delete all class files from current directory */
    private static void deleteClassFiles() {
        Path startPath = Paths.get(".");
        try (Stream<Path> stream = Files.list(startPath)) {
            stream.filter(Files::isRegularFile)
                  .filter(path -> path.toString().endsWith(".class"))
                  .forEach(path -> {
                      try {
                          Files.delete(path);
                      } catch (IOException e) {
                          throw new QuestionException("Error deleting file: " + path + " " + e.getMessage());
                      }
                  });
        } catch (IOException e) {
            throw new QuestionException("Error listing class files: " + e.getMessage());
        }
    }

    /**
     * Evaluate a method call in the context of the given source code.
     * The method call should be a simple call to a static method
     * of an available class.
     */
    public static Object callResult(String sourceCode,
            String methodCall) throws Exception {
        int dotIndex = methodCall.indexOf('.');
        String className = methodCall.substring(0, dotIndex);
        String methodName = methodCall.substring(dotIndex + 1,
                methodCall.indexOf('('));

        // Prepare source for compilation
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        JavaFileObject file = new JavaSourceFromString(className, sourceCode);

        // Compile source file
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, Arrays.asList(file));
        Boolean result = task.call();
        if (result == null || !result) {
            throw new QuestionException("Compilation failed.");
        }

        // Load and instantiate compiled class
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { new File("").toURI().toURL() });
        Class<?> cls = Class.forName(className, true, classLoader);
        Method method = cls.getDeclaredMethod(methodName);

        // Invoke method
        Object evaluationResult = method.invoke(null);

        // Delete compiled class file
        deleteClassFiles();

        return evaluationResult;
    }

    static class JavaSourceFromString extends javax.tools.SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(java.net.URI.create("string:///" + name.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    /** Test the specified question.
     * @throws QuestionException on failure
     */
    static void test(Question q) throws QuestionException {
        String category = q.getCategory();

        // Convert answers into a vector
        List <Object> answers = q.getAnswers();

        if (answers.size() < 1)
            throw new QuestionException(category
                    + ": At least two answers expected");

        var set = new HashSet<>(answers);
        if (set.size() != answers.size())
            throw new QuestionException(category
                    + ": Answers should contain unique elements");

        Object result = null;
        try {
            result = callResult(q.getQuestionCode(), q.getAnswerCall());
        } catch (Exception e) {
            // Fail the test if the exception is caught
            throw new QuestionException(category
                    + ": Code evaluation failed");
        }
        if (!result.equals(answers.get(0)))
            throw new QuestionException(category
                    + ": Obtained answer " + result
                    + " is different from provided " + answers.get(0));
    }
}
