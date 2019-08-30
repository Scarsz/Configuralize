package github.scarsz.configuralize;

import alexh.weak.Dynamic;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public class Provider {

    public static Dynamic load(DynamicConfig config, Source source, String raw) throws ParseException {
        Map parsed;
        String extension = source.getFile().getName().substring(source.getFile().getName().lastIndexOf(".") + 1);
        try {
            if (extension.equalsIgnoreCase("yml")) {
                parsed = config.getYamlParser().loadAs(raw, Map.class);
            } else if (extension.equalsIgnoreCase("json")) {
                parsed = (Map) config.getJsonParser().parse(raw);
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
        return load(config, source, new String(Files.readAllBytes(source.getFile().toPath())));
    }

    public Dynamic loadResource() throws ParseException, IOException {
        InputStream stream = source.getClazz().getResource(source.getLocalizedResource(config.getLanguage())).openStream();
        if (stream == null) throw new IllegalArgumentException("Unknown resource " + source.getLocalizedResource(config.getLanguage()));
        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name()).useDelimiter(Pattern.compile("\\Z"))) {
            return load(config, source, scanner.next());
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

        String resource = source.getLocalizedResource(config.getLanguage());
        InputStream stream = source.getClazz().getResourceAsStream(resource);
        Files.copy(Objects.requireNonNull(stream), source.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        stream.close();
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
