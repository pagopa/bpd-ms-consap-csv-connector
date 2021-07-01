package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.mapping;


import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.RowMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset.RowSet;
import org.springframework.batch.support.DefaultPropertyEditorRegistrar;

import org.springframework.beans.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class BeanWrapperRowMapper<T> extends DefaultPropertyEditorRegistrar
        implements RowMapper<T>, BeanFactoryAware, InitializingBean {

    private String name;

    private Class<? extends T> type;

    private BeanFactory beanFactory;

    private final ConcurrentMap<DistanceHolder, ConcurrentMap<String, String>> propertiesMatched = new ConcurrentHashMap<>();

    private int distanceLimit = 5;

    private boolean strict = true;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * The maximum difference that can be tolerated in spelling between input key names
     * and bean property names. Defaults to 5, but could be set lower if the field names
     * match the bean names.
     * @param distanceLimit the distance limit to set
     */
    public void setDistanceLimit(int distanceLimit) {
        this.distanceLimit = distanceLimit;
    }

    /**
     * The bean name (id) for an object that can be populated from the field set that will
     * be passed into {@link #mapRow(RowSet)}. Typically a prototype scoped bean so that a
     * new instance is returned for each field set mapped.
     *
     * Either this property or the type property must be specified, but not both.
     * @param name the name of a prototype bean in the enclosing BeanFactory
     */
    public void setPrototypeBeanName(String name) {
        this.name = name;
    }

    /**
     * Public setter for the type of bean to create instead of using a prototype bean. An
     * object of this type will be created from its default constructor for every call to
     * {@link #mapRow(RowSet)}.<br>
     *
     * Either this property or the prototype bean name must be specified, but not both.
     * @param type the type to set
     */
    public void setTargetType(Class<? extends T> type) {
        this.type = type;
    }

    /**
     * Check that precisely one of type or prototype bean name is specified.
     * @throws IllegalStateException if neither is set or both properties are set.
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(this.name != null || this.type != null, "Either name or type must be provided.");
        Assert.state(this.name == null || this.type == null, "Both name and type cannot be specified together.");
    }

    /**
     * Map the {@link org.springframework.batch.item.file.transform.FieldSet} to an object
     * retrieved from the enclosing Spring context, or to a new instance of the required
     * type if no prototype is available.
     * @throws org.springframework.validation.BindException if there is a type conversion
     * or other error (if the {@link org.springframework.validation.DataBinder} from
     * {@link #createBinder(Object)} has errors after binding).
     * @throws org.springframework.beans.NotWritablePropertyException if the
     * {@link org.springframework.batch.item.file.transform.FieldSet} contains a field
     * that cannot be mapped to a bean property.
     * @see org.springframework.batch.item.file.mapping.FieldSetMapper#mapFieldSet(org.springframework.batch.item.file.transform.FieldSet)
     */
    @Override
    public T mapRow(RowSet rs) throws BindException {
        T copy = getBean();
        DataBinder binder = createBinder(copy);
        binder.bind(new MutablePropertyValues(getBeanProperties(copy, rs.getProperties())));
        if (binder.getBindingResult().hasErrors()) {
            throw new BindException(binder.getBindingResult());
        }
        return copy;
    }

    /**
     * Create a binder for the target object. The binder will then be used to bind the
     * properties form a field set into the target object. This implementation creates a
     * new {@link DataBinder} and calls out to {@link #initBinder(DataBinder)} and
     * {@link #registerCustomEditors(org.springframework.beans.PropertyEditorRegistry)}.
     * @param target the object to bind to.
     * @return a {@link DataBinder} that can be used to bind properties to the target.
     */
    protected DataBinder createBinder(Object target) {
        DataBinder binder = new DataBinder(target);
        binder.setIgnoreUnknownFields(!this.strict);
        initBinder(binder);
        registerCustomEditors(binder);
        return binder;
    }

    /**
     * Initialize a new binder instance. This hook allows customization of binder settings
     * such as the {@link DataBinder#initDirectFieldAccess() direct field access}. Called
     * by {@link #createBinder(Object)}.
     * <p>
     * Note that registration of custom property editors can be done in
     * {@link #registerCustomEditors(org.springframework.beans.PropertyEditorRegistry)}.
     * </p>
     * @param binder new binder instance
     * @see #createBinder(Object)
     */
    protected void initBinder(DataBinder binder) {
    }

    @SuppressWarnings("unchecked")
    private T getBean() {
        if (this.name != null) {
            return (T) this.beanFactory.getBean(this.name);
        }
        return BeanUtils.instantiateClass(this.type);
    }

    private Properties getBeanProperties(Object bean, Properties properties) {

        Class<?> cls = bean.getClass();

        // Map from field names to property names
        DistanceHolder distanceKey = new DistanceHolder(cls, this.distanceLimit);
        if (!this.propertiesMatched.containsKey(distanceKey)) {
            this.propertiesMatched.putIfAbsent(distanceKey, new ConcurrentHashMap<>());
        }
        Map<String, String> matches = new HashMap<>(this.propertiesMatched.get(distanceKey));

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Set<String> keys = new HashSet(properties.keySet());
        for (String key : keys) {

            if (matches.containsKey(key)) {
                switchPropertyNames(properties, key, matches.get(key));
                continue;
            }

            String name = findPropertyName(bean, key);

            if (name != null) {
                if (matches.containsValue(name)) {
                    throw new NotWritablePropertyException(cls, name, "Duplicate match with distance <= "
                            + this.distanceLimit + " found for this property in input keys: " + keys
                            + ". (Consider reducing the distance limit or changing the input key names to get a closer match.)");
                }
                matches.put(key, name);
                switchPropertyNames(properties, key, name);
            }
        }

        this.propertiesMatched.replace(distanceKey, new ConcurrentHashMap<>(matches));
        return properties;
    }

    private String findPropertyName(Object bean, String key) {

        if (bean == null) {
            return null;
        }

        Class<?> cls = bean.getClass();

        int index = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(key);
        String prefix;
        String suffix;

        // If the property name is nested recurse down through the properties
        // looking for a match.
        if (index > 0) {
            prefix = key.substring(0, index);
            suffix = key.substring(index + 1);
            String nestedName = findPropertyName(bean, prefix);
            if (nestedName == null) {
                return null;
            }

            Object nestedValue = getPropertyValue(bean, nestedName);
            String nestedPropertyName = findPropertyName(nestedValue, suffix);
            return (nestedPropertyName != null) ? nestedName + "." + nestedPropertyName : null;
        }

        String name = null;
        int distance = 0;
        index = key.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);

        if (index > 0) {
            prefix = key.substring(0, index);
            suffix = key.substring(index);
        }
        else {
            prefix = key;
            suffix = "";
        }

        while (name == null && distance <= this.distanceLimit) {
            String[] candidates = PropertyMatches.forProperty(prefix, cls, distance).getPossibleMatches();
            // If we find precisely one match, then use that one...
            if (candidates.length == 1) {
                String candidate = candidates[0];
                if (candidate.equals(prefix)) { // if it's the same don't
                    // replace it...
                    name = key;
                }
                else {
                    name = candidate + suffix;
                }
            }
            distance++;
        }
        return name;
    }

    private Object getPropertyValue(Object bean, String nestedName) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
        wrapper.setAutoGrowNestedPaths(true);

        Object nestedValue = wrapper.getPropertyValue(nestedName);
        if (nestedValue == null) {
            nestedValue = BeanUtils.instantiateClass(wrapper.getPropertyType(nestedName));
            wrapper.setPropertyValue(nestedName, nestedValue);
        }
        return nestedValue;
    }

    private void switchPropertyNames(Properties properties, String oldName, String newName) {
        String value = properties.getProperty(oldName);
        properties.remove(oldName);
        properties.setProperty(newName, value);
    }

    /**
     * Public setter for the 'strict' property. If true, then {@link #mapRow(RowSet)} will
     * fail if the RowSet contains fields that cannot be mapped to the bean.
     * @param strict fail if non-mappable properties are found
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    private static class DistanceHolder {

        private final Class<?> cls;

        private final int distance;

        DistanceHolder(Class<?> cls, int distance) {
            this.cls = cls;
            this.distance = distance;

        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DistanceHolder other = (DistanceHolder) obj;
            if (this.cls == null) {
                if (other.cls != null) {
                    return false;
                }
            }
            else if (!this.cls.equals(other.cls)) {
                return false;
            }
            return this.distance == other.distance;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.cls == null) ? 0 : this.cls.hashCode());
            result = prime * result + this.distance;
            return result;
        }


    }

}
