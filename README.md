# jmcqg — Java multiple choice question generator

__jmcqg__ is a generator for Java multiple choice questions,
originally developed for summative tests of basic concepts' knowledge.
Questions consist of some code and multiple choice answers
regarding the value returned from a static method.
It offers the following facilities.

* Questions are easily created and auto-discovered in a package
  devoted to them.
  This allows their easy crowd-sourcing.
* Multiple versions of each question can be generated, based on randomization.
* The generator compiles and runs the code of each question, verifying
  the provided answer against it.
* The output can be plain text or in the
  [GIFT file format](https://en.wikipedia.org/wiki/GIFT_(file_format)),
  which is easily importable into [Moodle](https://moodle.org/).
* Potential for answering the questions through generative AI,
  such as ChatGPT, or by outsourcing the code understanding to a
  compiler, is minimized by creating the code as images,
  and exporting the result in the [GIFT with medias format](https://docs.moodle.org/39/en/Gift_with_medias_format).
  For this to work, a tight deadline must be given for answering the questions
  so that OCR becomes impractical.


## Suggestions for developing questions
If you are an academic teaching a Java course, drop me a note
from your academic account with a pointer to the course page and
your GitHub id, in order to give you access to the 35 questions
we developed.

You can contribute questions by adding a new class to the `questions` directory.
Use the `Variables` class as a model.
Each question should test _only one_ learning object and should be easily
answered with pen and paper:
much faster than copy-pasting the code to
try it out on an IDE or asking ChatGPT.
Keep the code readable and simple;
do not obfuscate it and do not rely on edge cases or gotchas.
Answers should not rely on knowing APIs in depth, but understanding basic
principles.
Each time the question is generated it should provide randomly
different answers and ensure that the answers are guaranteed to be
distinct.

## Compile
```
mvn compile
```

## Test
```
mvn test
```

Run tests multiple times by specifying the corresponding property, e.g.
```
mvn test -Dtest_repetitions=10
```

## Run
```
mvn exec:java
```

## Package for stand-alone execution
```
mvn package
```

## Run stand-alone package
```
java -jar target/jmcqg-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Command-line options
Get help on command invocation.
```
java -jar target/jmcqg-1.0-SNAPSHOT-jar-with-dependencies.jar -h
```

Specify to output a single question with the `-q` option,
number of repetitions with `-n`,
and the testing of generated questions with `-t`.
```
mvn package
java -jar target/jmcqg-1.0-SNAPSHOT-jar-with-dependencies.jar -q ConcatenatingStrings -n 10 -t
```

Generate and test 15 versions of each question in a ZIP with the
code embedded as PNG images.
```
java -jar target/jmcqg-1.0-SNAPSHOT-jar-with-dependencies.jar -p questions2.zip -n 14 -t
```
