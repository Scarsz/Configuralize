package github.scarsz.configuralize.test;

import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.ParseException;
import github.scarsz.configuralize.Source;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class BasicTest {

    private DynamicConfig config;

    @Before
    public void setUp() {
        config = new DynamicConfig();
        config.addSource(BasicTest.class, "config", new File("config.yml"));
        config.addSource(BasicTest.class, "messages", new File("messages.yml"));
    }

    @Test
    public void test() throws IOException, ParseException {
        config.saveAllDefaults();
        config.loadAll();

        Assert.assertEquals(config.getString("config key"), "value from config");
        Assert.assertEquals(config.getString("messages key"), "value from messages");
    }

    @After
    public void tearDown() {
        config.getSources().keySet().stream()
                .map(Source::getFile)
                .filter(file -> !file.delete())
                .forEach(File::deleteOnExit);
    }

}
