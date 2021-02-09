package github.scarsz.configuralize.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the attached field as one that can be mapped with {@link github.scarsz.configuralize.DynamicConfig#map(Class, MappingFunction[])}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option {

    String key();

}
