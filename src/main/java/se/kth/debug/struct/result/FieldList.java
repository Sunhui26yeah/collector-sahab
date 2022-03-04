package se.kth.debug.struct.result;

import java.util.ArrayList;
import java.util.List;

public class FieldList implements RuntimeValueTypeChunk<Field> {
    private static final String IDENTIFIER = "Fields";
    private final List<Field> fields = new ArrayList<>();

    @Override
    public void addData(String name, String value) {
        fields.add(new Field(name, value));
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public List<Field> getCollection() {
        return fields;
    }
}

class Field implements RuntimeValue {
    private final String name;
    private final String value;

    Field(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
