package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel;

import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class ReflectionTestUtils {

    private ReflectionTestUtils() { }

    @Nullable
    public static Object getField(Object targetObject, String name) {
        Class<?> targetClass = targetObject.getClass();

        Field field = ReflectionUtils.findField(targetClass, name);
        if (field == null) {
            throw new IllegalArgumentException(String.format("Could not find field '%s' on %s or target class [%s]",
                    name, targetObject, targetClass));
        }

        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, targetObject);
    }

}
