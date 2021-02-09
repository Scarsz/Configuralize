package github.scarsz.configuralize.mapping;

import alexh.weak.Dynamic;

import java.util.function.Function;

/**
 * POJO that holds a configuration key and a function to map a {@link Dynamic} to the desired type/value
 * @param <T> the output type of the function
 */
public class MappingFunction<T> {

    private final String key;
    private final Function<Dynamic, T> function;

    public MappingFunction(String key, Function<Dynamic, T> function) {
        this.key = key;
        this.function = function;
    }

    public String getKey() {
        return key;
    }
    public Function<Dynamic, T> getFunction() {
        return function;
    }

}
