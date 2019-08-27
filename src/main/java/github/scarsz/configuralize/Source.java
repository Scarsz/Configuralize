package github.scarsz.configuralize;

import java.io.File;

public class Source {

    private final Class clazz;
    private final String resource;
    private final File file;

    public Source(Class clazz, String resource, File file) {
        this.clazz = clazz;
        this.resource = resource;
        this.file = file;
    }

    public String getLocalizedResource() {
        return getLocalizedResource(Language.EN);
    }

    public String getLocalizedResource(Language language) {
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
