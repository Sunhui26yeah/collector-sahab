import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import se.kth.debug.CollectorOptions;

public class TestHelper {
    public static final Path PATH_TO_SAMPLE_MAVEN_PROJECT =
            Paths.get("src/test/resources/sample-maven-project");
    public static final Path PATH_TO_INPUT = PATH_TO_SAMPLE_MAVEN_PROJECT.resolve("inputs");

    public static String[] getMavenClasspathFromBuildDirectory(Path buildDirectory) {
        List<String> classpath =
                List.of(
                        buildDirectory.resolve("classes").toString(),
                        buildDirectory.resolve("test-classes").toString(),
                        buildDirectory.resolve("dependency").toString());
        return classpath.toArray(new String[] {});
    }

    public static CollectorOptions getDefaultOptions() {
        CollectorOptions context = new CollectorOptions();
        context.setStackTraceDepth(1);
        context.setNumberOfArrayElements(10);
        context.setExecutionDepth(0);
        context.setSkipPrintingField(false);
        return context;
    }
}
