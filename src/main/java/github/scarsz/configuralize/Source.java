package github.scarsz.configuralize;

import java.io.File;

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

    public String getResourceName() {
        return resource;
    }

    public File getFile() {
        return file;
    }

    public Class getClazz() {
        return this.clazz;
    }

}
