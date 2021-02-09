package github.scarsz.configuralize.test;

import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.ParseException;
import github.scarsz.configuralize.mapping.Option;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;

public class ChildDynamicConfigExample extends DynamicConfig {

    @Option(key = "config key")
    public static String configValue;

    @Option(key = "messages key")
    public static String messagesValue;

    public ChildDynamicConfigExample() throws IOException, ParseException {
        addSource(ChildDynamicConfigExample.class, "config", new File("config.yml"));
        addSource(ChildDynamicConfigExample.class, "messages", new File("messages.yml"));
        saveAllDefaults();
        loadAll();
        map();

        Assert.assertEquals("value from config", configValue);
        Assert.assertEquals("value from messages", messagesValue);
    }

}
