package github.scarsz.configuralize;

import alexh.weak.Dynamic;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
public class Provider {

    private static Dynamic load(DynamicConfig config, Source source, String raw) throws ParseException {
        if (raw == null) throw new IllegalArgumentException("Can't load null config");

        Map<?, ?> parsed;
        String extension = source.getFile().getName().substring(source.getFile().getName().lastIndexOf(".") + 1);
        try {
            if (extension.equalsIgnoreCase("yml")) {
                parsed = config.getYamlParser().loadAs(raw, Map.class);
            } else if (extension.equalsIgnoreCase("json")) {
                parsed = (Map<?, ?>) config.getJsonParser().parse(raw);
            } else {
                throw new IllegalArgumentException("Config source extension " + extension + " is not supported");
            }
        } catch (org.json.simple.parser.ParseException | ParserException e) {
            throw new ParseException(source, e);
        }
        return Dynamic.from(parsed);
    }

    private final DynamicConfig config;
    private final Source source;
    private Dynamic defaults;
    private Dynamic values;

    public Provider(DynamicConfig config, Source source) {
        this.config = config;
        this.source = source;
    }

    public void load() throws IOException, ParseException {
        this.defaults = loadResource();
        this.values = loadValues();
    }
    public Dynamic loadValues() throws ParseException, IOException {
        return load(config, source, FileUtils.readFileToString(source.getFile(), "UTF-8"));
    }
    public Dynamic loadResource() throws ParseException, IOException {
        try (InputStream stream = source.getResource().openStream()) {
            Objects.requireNonNull(stream, "Unknown resource " + source.getResourcePath(config.getLanguage()));
            return load(config, source, IOUtils.toString(stream, StandardCharsets.UTF_8));
        }
    }

    public void saveDefaults() throws IOException {
        saveDefaults(false);
    }
    public void saveDefaults(boolean overwrite) throws IOException {
        if (source.getFile().exists() && !overwrite) return;
        if (!source.getFile().getParentFile().exists() && !source.getFile().getParentFile().mkdirs()) {
            throw new IOException("Failed to create directory " + source.getFile().getParentFile().getAbsolutePath());
        }

        String resource = source.getResourcePath(config.getLanguage());
        try (InputStream stream = source.getResource().openStream()) {
            Objects.requireNonNull(stream, "Unknown resource " + source.getResourcePath(config.getLanguage()));
            FileUtils.copyInputStreamToFile(stream, source.getFile());
        }
    }

    public DynamicConfig getConfig() {
        return config;
    }
    public Source getSource() {
        return source;
    }
    public Dynamic getDefaults() {
        return defaults;
    }
    public Dynamic getValues() {
        return values;
    }

}
