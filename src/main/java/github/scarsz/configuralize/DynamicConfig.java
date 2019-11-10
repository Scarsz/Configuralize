package github.scarsz.configuralize;

import alexh.weak.Dynamic;
import alexh.weak.Weak;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
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
     * Checks whether or not the current language has translated files for all sources
     */
    public boolean isLanguageAvailable() {
        return isLanguageAvailable(language);
    }

    /**
     * Checks whether or not the given language has translated files for all sources
     * @param language The language to check for translations
     */
    public boolean isLanguageAvailable(Language language) {
        return sources.keySet().stream().allMatch(source -> source.isLanguageAvailable(language));
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
     * Add the given source to the dynamic config
     * @param clazz The class that should provide the resource
     * @param resource The name of the resource, i.e. "config" in /resources/config/en.yml
     * @param file The file that should provide this source
     * @return true if this source wasn't already in the dynamic config
     */
    public boolean addSource(Class clazz, String resource, File file) {
        Source source = new Source(this, clazz, resource, file);
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

    public <T> T get(String key) throws RuntimeException {
        return (T) dget(key).asObject();
    }
    public <T> Optional<T> getOptional(String key) {
        try {
            return Optional.ofNullable(get(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public <T> T getElse(String key, T otherwise) {
        try {
            return get(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public <K, V> Map<K, V> getMap(String key) throws RuntimeException {
        return (Map<K, V>) dget(key).convert().intoMap();
    }
    public <K, V> Optional<Map<K, V>> getOptionalMap(String key) {
        try {
            return Optional.ofNullable(getMap(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public <K, V> Map<K, V> getMapElse(String key, Map<K, V> otherwise) {
        try {
            return getMap(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public <T> List<T> getList(String key) throws RuntimeException {
        return (List<T>) dget(key).convert().intoList();
    }
    public <T> Optional<List<T>> getOptionalList(String key) {
        try {
            return Optional.ofNullable(
                    getList(key)
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public <T> List<T> getListElse(String key, List<T> otherwise) {
        try {
            return getList(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public String getString(String key) throws RuntimeException {
        return dget(key).convert().intoString();
    }
    public Optional<String> getOptionalString(String key) {
        try {
            return Optional.ofNullable(getString(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public String getStringElse(String key, String otherwise) {
        try {
            return getString(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public List<String> getStringList(String key) throws RuntimeException {
        return (List<String>) dget(key).convert().intoList();
    }
    public Optional<List<String>> getOptionalStringList(String key) {
        try {
            return Optional.ofNullable(getStringList(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<String> getStringListElse(String key, List<String> otherwise) {
        try {
            return getStringList(key);
        } catch (Exception e) {
            return otherwise;
        }
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
    public Optional<Boolean> getOptionalBoolean(String key) {
        try {
            return Optional.of(getBoolean(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public boolean getBooleanElse(String key, boolean otherwise) {
        try {
            return getBoolean(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public List<Boolean> getBooleanList(String key) throws RuntimeException {
        return (List<Boolean>) dget(key).convert().intoList();
    }
    public Optional<List<Boolean>> getOptionalBooleanList(String key) {
        try {
            return Optional.ofNullable(getBooleanList(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<Boolean> getBooleanListElse(String key, List<Boolean> otherwise) {
        try {
            return getBooleanList(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public int getInt(String key) throws RuntimeException {
        return dget(key).convert().intoInteger();
    }
    public Optional<Integer> getOptionalInt(String key) {
        try {
            return Optional.of(getInt(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public int getIntElse(String key, int otherwise) {
        try {
            return getInt(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public List<Integer> getIntList(String key) throws RuntimeException {
        return (List<Integer>) dget(key).convert().intoList();
    }
    public Optional<List<Integer>> getOptionalIntList(String key) {
        try {
            return Optional.ofNullable(getIntList(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<Integer> getIntListElse(String key, List<Integer> otherwise) {
        try {
            return getIntList(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public long getLong(String key) throws RuntimeException {
        return dget(key).convert().intoLong();
    }
    public Optional<Long> getOptionalLong(String key) {
        try {
            return Optional.of(getLong(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public long getLongElse(String key, long otherwise) {
        try {
            return getLong(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public List<Long> getLongList(String key) throws RuntimeException {
        return (List<Long>) dget(key).convert().intoList();
    }
    public Optional<List<Long>> getOptionalLongList(String key) {
        try {
            return Optional.ofNullable(getLongList(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<Long> getLongListElse(String key, List<Long> otherwise) {
        try {
            return getLongList(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public double getDouble(String key) throws RuntimeException {
        return dget(key).convert().intoDouble();
    }
    public Optional<Double> getOptionalDouble(String key) {
        try {
            return Optional.of(getDouble(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public double getDoubleElse(String key, double otherwise) {
        try {
            return getDouble(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public List<Double> getDoubleList(String key) throws RuntimeException {
        return (List<Double>) dget(key).convert().intoList();
    }
    public Optional<List<Double>> getOptionalDoubleList(String key) {
        try {
            return Optional.ofNullable(getDoubleList(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<Double> getDoubleListElse(String key, List<Double> otherwise) {
        try {
            return getDoubleList(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public BigDecimal getDecimal(String key) throws RuntimeException {
        return dget(key).convert().intoDecimal();
    }
    public Optional<BigDecimal> getOptionalDecimal(String key) {
        try {
            return Optional.ofNullable(getDecimal(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public BigDecimal getDecimalElse(String key, BigDecimal otherwise) {
        try {
            return getDecimal(key);
        } catch (Exception e) {
            return otherwise;
        }
    }

    public List<BigDecimal> getDecimalList(String key) throws RuntimeException {
        return (List<BigDecimal>) dget(key).convert().intoList();
    }
    public Optional<List<BigDecimal>> getOptionalDecimalList(String key) {
        try {
            return Optional.ofNullable(getDecimalList(key));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<BigDecimal> getDecimalListElse(String key, List<BigDecimal> otherwise) {
        try {
            return getDecimalList(key);
        } catch (Exception e) {
            return otherwise;
        }
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
    public Provider getProvider(String resource) {
        return sources.entrySet().stream()
                .filter(entry -> entry.getKey().getResourceName().equals(resource))
                .map(Map.Entry::getValue)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid resource " + resource));
    }

}
