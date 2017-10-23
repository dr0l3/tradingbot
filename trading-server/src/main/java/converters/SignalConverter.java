package converters;

import model.Signal;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

public class SignalConverter extends TypeConverter {

    public SignalConverter() {
        super(Signal.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        return null;
    }

    @Override
    public Object encode(final Object value, final MappedField optionInfo){
        return value.toString();
    }
}
