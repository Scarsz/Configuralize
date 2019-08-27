package github.scarsz.configuralize.test;

import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.ParseException;
import github.scarsz.configuralize.Source;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

public class Test {

    private DynamicConfig config;

    @Before
    public void setUp() throws Exception {
        config = new DynamicConfig();
        config.addSource(new Source(Test.class, "config", new File("config.yml")));
        config.addSource(new Source(Test.class, "messages", new File("messages.yml")));
    }

    @org.junit.Test
    public void test() throws IOException, ParseException {
        config.saveAllDefaults();
        config.loadAll();

        Assert.assertEquals(config.getString("config key"), "value from config");
        Assert.assertEquals(config.getString("messages key"), "value from messages");
    }

    @After
    public void tearDown() throws Exception {
        config.getSources().keySet().stream()
                .map(Source::getFile)
                .filter(file -> !file.delete())
                .forEach(File::deleteOnExit);
    }

}
