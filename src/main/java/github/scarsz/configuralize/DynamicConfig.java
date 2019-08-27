package github.scarsz.configuralize;

import alexh.weak.Dynamic;
import alexh.weak.Weak;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"SameParameterValue", "UnusedReturnValue", "unused", "WeakerAccess", "unchecked"})
public class DynamicConfig {

    private final Map<Source, Provider> sources = new LinkedHashMap<>();
    private final Map<String, Object> runtimeValues = new HashMap<>();
    private Language language;

    public DynamicConfig(Source... sources) {
        this(Language.EN, sources);
    }

    public DynamicConfig(Language language, Source... sources) {
        this.language = language;
        Arrays.stream(sources).forEach(this::addSource);
    }

    /**
     * Add the given source to the dynamic config
     * @param source The source to add
     * @return true if this source wasn't already in the dynamic config
     */
    public boolean addSource(Source source) {
        return sources.put(source, new Provider(this, source)) == null;
    }

    /**
     * Remove the given source from the dynamic config
     * @param source The source to remove
     * @return true if this source was in the dynamic config and thus removed
     */
    public boolean removeSource(Source source) {
        return sources.remove(source) != null;
    }

    /**
     * Saves all of the linked sources to the file, skipping if the file already exists
     * @throws IOException if saving to the file fails
     */
    public void saveAllDefaults() throws IOException {
        saveAllDefaults(false);
    }

    /**
     * Saves all of the linked sources to the file
     * @param overwrite whether or not to skip saving defaults if the file already exists
     * @throws IOException if saving to the file fails
     */
    public void saveAllDefaults(boolean overwrite) throws IOException {
        for (Map.Entry<Source, Provider> source : this.sources.entrySet()) {
            source.getValue().saveDefaults(overwrite);
        }
    }

    public void loadAll() throws IOException, ParseException {
        for (Map.Entry<Source, Provider> source : this.sources.entrySet()) {
            source.getValue().load();
        }
    }

    public Dynamic dget(String key) throws IllegalArgumentException {
        if (runtimeValues.containsKey(key)) return Dynamic.from(runtimeValues.get(key));
        return sources.values().stream()
                .filter(Objects::nonNull)
                .map(provider -> provider.getValues().dget(key))
                .filter(Weak::isPresent)
                .findFirst().orElseGet(() -> sources.values().stream()
                        .map(provider -> provider.getDefaults().dget(key))
                        .filter(Weak::isPresent)
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid key: " + key)));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) throws RuntimeException {
        return (T) dget(key).asObject();
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getMap(String key) throws RuntimeException {
        return (Map<K, V>) dget(key).convert().intoMap();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) throws RuntimeException {
        return (List<T>) dget(key).convert().intoList();
    }

    public String getString(String key) throws RuntimeException {
        return dget(key).convert().intoString();
    }
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) throws RuntimeException {
        return (List<String>) dget(key).convert().intoList();
    }

    public boolean getBoolean(String key) throws RuntimeException {
        String value = dget(key).convert().intoString();
        switch (value.toLowerCase()) {
            case "true":
            case "yes":
            case "on":
            case "1":
                return true;
            case "false":
            case "no":
            case "off":
            case "0":
                return false;
            default:
                throw new RuntimeException("Can't convert key " + key + " value \"" + value + "\" to boolean");
        }
    }
    @SuppressWarnings("unchecked")
    public List<Boolean> getBooleanList(String key) throws RuntimeException {
        return (List<Boolean>) dget(key).convert().intoList();
    }

    public int getInt(String key) throws RuntimeException {
        return dget(key).convert().intoInteger();
    }
    @SuppressWarnings("unchecked")
    public List<Integer> getIntList(String key) throws RuntimeException {
        return (List<Integer>) dget(key).convert().intoList();
    }

    public long getLong(String key) throws RuntimeException {
        return dget(key).convert().intoLong();
    }
    @SuppressWarnings("unchecked")
    public List<Long> getLongList(String key) throws RuntimeException {
        return (List<Long>) dget(key).convert().intoList();
    }

    public double getDouble(String key) throws RuntimeException {
        return dget(key).convert().intoDouble();
    }
    @SuppressWarnings("unchecked")
    public List<Double> getDoubleList(String key) throws RuntimeException {
        return (List<Double>) dget(key).convert().intoList();
    }

    public BigDecimal getDecimal(String key) throws RuntimeException {
        return dget(key).convert().intoDecimal();
    }
    @SuppressWarnings("unchecked")
    public List<BigDecimal> getDecimalList(String key) throws RuntimeException {
        return (List<BigDecimal>) dget(key).convert().intoList();
    }

    public <T> T getSilent(String key) {
        try {
            return (T) dget(key).asObject();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
    public void getSilent(String key, Consumer<Dynamic> success) {
        getSilent(key, success, null);
    }
    public void getSilent(String key, Consumer<Dynamic> success, Consumer<Dynamic> failure) {
        try {
            if (success != null) success.accept(dget(key));
        } catch (IllegalArgumentException e) {
            if (failure != null) failure.accept(dget(key));
        }
    }

    public void setRuntimeValue(String key, Object value) {
        runtimeValues.put(key, value);
    }

    private JSONParser jsonParser = null;
    JSONParser getJsonParser() {
        return jsonParser != null ? jsonParser : (jsonParser = new JSONParser());
    }

    private Yaml yamlParser = null;
    Yaml getYamlParser() {
        return yamlParser != null ? yamlParser : (yamlParser = new Yaml());
    }

    public Language getLanguage() {
        return language;
    }
    public void setLanguage(Language language) {
        this.language = language;
    }
    
    public Map<Source, Provider> getSources() {
        return sources;
    }

    /**
     * Get the {@link Dynamic} value from the source matching the given resource name
     * @param source the resource name to filter the sources by
     * @return the values of the source's provider
     */
    public Dynamic getValues(String source) {
        return sources.entrySet().stream()
                .filter(entry -> entry.getKey().getResourceName().equals(source))
                .map(Map.Entry::getValue)
                .map(Provider::getValues)
                .findFirst().orElse(null);
    }

}
