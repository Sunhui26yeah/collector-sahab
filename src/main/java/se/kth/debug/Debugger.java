package se.kth.debug;

import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.debug.struct.FileAndBreakpoint;
import se.kth.debug.struct.result.*;

public class Debugger {
    private Process process;
    private final Logger logger = Logger.getLogger("Debugger");

    private final String[] pathToBuiltProject;
    private final String pathToTestDirectory;
    private final List<FileAndBreakpoint> classesAndBreakpoints;

    public Debugger(
            String[] pathToBuiltProject,
            String pathToTestDirectory,
            List<FileAndBreakpoint> classesAndBreakpoints) {
        this.pathToBuiltProject = pathToBuiltProject;
        this.pathToTestDirectory = pathToTestDirectory;
        this.classesAndBreakpoints = classesAndBreakpoints;
    }

    public VirtualMachine launchVMAndJunit() {
        try {
            String classpath = Utility.getClasspathForRunningJUnit(pathToBuiltProject);
            String testsSeparatedBySpace = Utility.getAllJUnitTestClasses(pathToTestDirectory);
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "java",
                            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y",
                            "-cp",
                            classpath,
                            MethodTestRunner.class.getCanonicalName(),
                            testsSeparatedBySpace);
            logger.log(
                    Level.INFO,
                    "java -cp "
                            + classpath
                            + " "
                            + MethodTestRunner.class.getCanonicalName()
                            + " "
                            + testsSeparatedBySpace);

            process = processBuilder.start();

            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String lineRead = br.readLine();
            Pattern pattern = Pattern.compile("([0-9]{4,})");
            Matcher matcher = pattern.matcher(lineRead);
            matcher.find();
            int port = Integer.parseInt(matcher.group());

            final VirtualMachine vm = new VMAcquirer().connect(port);
            logger.log(Level.INFO, "Connected to port: " + port);
            // kill process when the program exit
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread() {
                                public void run() {
                                    shutdown(vm);
                                }
                            });
            return vm;
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Wrong URL: " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addClassPrepareEvent(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();
        for (FileAndBreakpoint classToBeDebugged : classesAndBreakpoints) {
            ClassPrepareRequest cpr = erm.createClassPrepareRequest();
            cpr.addClassFilter(classToBeDebugged.getFileName());
            cpr.setEnabled(true);
            logger.log(Level.INFO, classToBeDebugged.getFileName() + " added!");
            vm.resume();
        }
    }

    public void setBreakpoints(VirtualMachine vm, ClassPrepareEvent event)
            throws AbsentInformationException {
        EventRequestManager erm = vm.eventRequestManager();

        List<Integer> breakpoints =
                classesAndBreakpoints.stream()
                        .filter(cb -> cb.getFileName().equals(event.referenceType().name()))
                        .findFirst()
                        .get()
                        .getBreakpoints();

        for (int lineNumber : breakpoints) {
            List<Location> locations = event.referenceType().locationsOfLine(lineNumber);
            BreakpointRequest br = erm.createBreakpointRequest(locations.get(0));
            br.setEnabled(true);
        }
    }

    public List<StackFrameContext> processBreakpoints(BreakpointEvent bpe)
            throws IncompatibleThreadStateException, AbsentInformationException {
        ThreadReference threadReference = bpe.thread();

        int totalFrames = threadReference.frameCount();

        List<StackFrameContext> stackFrameContexts = new ArrayList<>();
        for (int i = 0; i < totalFrames; ++i) {
            StackFrame stackFrame = threadReference.frame(i);
            StackFrameContext stackFrameContext =
                    new StackFrameContext(i + 1, stackFrame.location().toString());
            try {
                List<LocalVariableData> localVariables = collectLocalVariable(stackFrame);
                stackFrameContext.addRuntimeValueCollection(localVariables);

                List<FieldData> fields = collectFields(stackFrame);
                stackFrameContext.addRuntimeValueCollection(fields);

                stackFrameContexts.add(stackFrameContext);
            } catch (AbsentInformationException e) {
                logger.warning(
                        "Information does not exist for " + stackFrame + " and frames later on");
                return stackFrameContexts;
            }
        }

        return stackFrameContexts;
    }

    private List<LocalVariableData> collectLocalVariable(StackFrame stackFrame)
            throws AbsentInformationException {
        List<LocalVariableData> result = new ArrayList<>();

        List<LocalVariable> localVariables = stackFrame.visibleVariables();
        for (LocalVariable localVariable : localVariables) {
            Value value = stackFrame.getValue(localVariable);
            LocalVariableData localVariableData =
                    new LocalVariableData(localVariable.name(), getStringRepresentation(value));
            result.add(localVariableData);
        }
        return result;
    }

    private List<FieldData> collectFields(StackFrame stackFrame) {
        List<FieldData> result = new ArrayList<>();

        List<Field> visibleFields = stackFrame.location().declaringType().visibleFields();
        for (Field field : visibleFields) {
            Value value;
            if (field.isStatic()) {
                value = stackFrame.location().declaringType().getValue(field);
            } else {
                value = stackFrame.thisObject().getValue(field);
            }
            FieldData fieldData = new FieldData(field.name(), getStringRepresentation(value));
            result.add(fieldData);
        }
        return result;
    }

    private String getStringRepresentation(Value value) {
        if (value instanceof ArrayReference) {
            return String.valueOf(((ArrayReference) value).getValues());
        }
        return String.valueOf(value);
    }

    public void shutdown(VirtualMachine vm) {
        try {
            process.destroy();
            vm.exit(0);
        } catch (Exception e) {
            // ignore
        }
    }

    public Process getProcess() {
        return process;
    }
}
