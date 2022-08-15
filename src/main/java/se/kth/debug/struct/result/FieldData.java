package se.kth.debug.struct.result;

import com.google.gson.annotations.JsonAdapter;
import java.util.List;
import se.kth.debug.RuntimeValueAdapter;

@JsonAdapter(RuntimeValueAdapter.class)
public class FieldData implements RuntimeValue {
    private final RuntimeValueKind kind = RuntimeValueKind.FIELD;
    private final String name;
    private final String type;
    private Object value;
    private List<FieldData> fields = null;
    private List<ArrayElement> arrayElements = null;

    public FieldData(String name, String type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
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
}
