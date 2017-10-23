package converters;

import com.mongodb.BasicDBObject;
import model.Selector;
import model.selectors.SingleCompanySelector;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

public class SelectorConverter extends TypeConverter{

    public SelectorConverter() {
        super(Selector.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        BasicDBObject basic = (BasicDBObject) fromDBObject;
        return null;
    }

    @Override
    public Object encode(final Object value, final MappedField optionInfo){
        return value.toString();
    }
}
