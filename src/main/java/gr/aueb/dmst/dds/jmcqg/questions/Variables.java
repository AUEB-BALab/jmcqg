package gr.aueb.dmst.dds.jmcqg.questions;

import java.util.function.Supplier;
import java.util.Map;

import static gr.aueb.dmst.dds.jmcqg.Util.*;
import gr.aueb.dmst.dds.jmcqg.Question;

public class Variables implements Supplier<Question> {
    @Override
    public Question get() {
        int va = randomInt(2, 6);
        int vb = va + randomInt(1, 3);
        int vc = vb + randomInt(1, 3);

        return new Question(
            // Question
            """
public class Variables {
    public static int value() {
        int va = {va};
        int vb = {vb};
        int vc = {vc};
        int vd = va;

        vb = vc;
        va = vd + vb;
        return va;
    }
}
            """,
            // Variable map
            Map.of(
                "va", va,
                "vb", vb,
                "vc", vc
            ),
            // Answer method name
            "Variables.value()",
            // Correct answer
            va + vc,
            // Other answers
            va,
            va + vb
        );
    }
}
