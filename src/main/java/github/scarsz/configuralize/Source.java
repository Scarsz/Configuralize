package github.scarsz.configuralize;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Source {

    private final DynamicConfig config;
    private final Class clazz;
    private final String resource;
    private final File file;

    public Source(DynamicConfig config, Class clazz, String resource, File file) {
        this.config = config;
        this.clazz = clazz;
        this.resource = resource;
        this.file = file.getAbsoluteFile();
    }

    public String getResourcePath() {
        return getResourcePath(config.getLanguage());
    }

    public String getResourcePath(Language language) {
        return "/" + resource + "/" + language.getCode().toLowerCase() + "." + file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    public URL getResource() {
        return getResource(config.getLanguage());
    }

    public URL getResource(Language language) {
        return clazz.getResource(getResourcePath(language));
    }

    public String getResourceName() {
        return resource;
    }

    public boolean isLanguageAvailable() {
        return isLanguageAvailable(config.getLanguage());
    }

    public boolean isLanguageAvailable(Language language) {
        try {
            InputStream stream = getResource(language).openStream();
            stream.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public File getFile() {
        return file;
    }

    public Class getClazz() {
        return this.clazz;
    }

}
