package se.kth.debug.struct.result;

import com.google.gson.annotations.JsonAdapter;
import java.util.List;
import se.kth.debug.RuntimeValueAdapter;

@JsonAdapter(RuntimeValueAdapter.class)
public class ReturnData implements RuntimeValue {
    private final RuntimeValueKind kind = RuntimeValueKind.RETURN;
    private final String methodName;
    private final List<String> stackTrace;
    private final String type;
    private Object value;
    private final String location;
    private final List<LocalVariableData> parameterValues;
    private List<FieldData> fields = null;
    private List<ArrayElement> arrayElements = null;

    public ReturnData(
            String methodName,
            String type,
            Object value,
            String location,
            List<LocalVariableData> parameterValues,
            List<String> stackTrace) {
        this.methodName = methodName;
        this.type = type;
        this.value = value;
        this.location = location;
        this.parameterValues = parameterValues;
        this.stackTrace = stackTrace;
    }

    public void setFields(List<FieldData> fields) {
        this.fields = fields;
    }

    public void setArrayElements(List<ArrayElement> arrayElements) {
        this.arrayElements = arrayElements;
    }

    @Override
    public RuntimeValueKind getKind() {
        return kind;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public List<FieldData> getFields() {
        return fields;
    }

    @Override
    public List<ArrayElement> getArrayElements() {
        return arrayElements;
    }

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
