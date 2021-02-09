package github.scarsz.configuralize.test;

import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.ParseException;
import github.scarsz.configuralize.Source;
import github.scarsz.configuralize.mapping.MappingFunction;
import github.scarsz.configuralize.mapping.Option;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MappedClassTest {

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
        config.map(MappedConfig.class,
                new MappingFunction<>("integer disguised as string", d -> d.convert().intoInteger()),
                new MappingFunction<>("more config keys.inner string disguised as integer", d -> d.convert().intoString())
        );

        Assert.assertEquals("value from config", MappedConfig.configOption);
        Assert.assertEquals(1, MappedConfig.configInt);
        Assert.assertEquals(1d, MappedConfig.configDouble, 0);
        Assert.assertEquals(1, MappedConfig.configIntDisguised);
        Assert.assertEquals("inner value", MappedConfig.Inner.anInnerConfigOption);
        Assert.assertEquals("1", MappedConfig.Inner.innerDisguisedString);
        Assert.assertEquals("value from messages", MappedConfig.messagesOption);
    }

    @After
    public void tearDown() {
        config.getSources().keySet().stream()
                .map(Source::getFile)
                .filter(file -> !file.delete())
                .forEach(File::deleteOnExit);
    }

    static class MappedConfig {

        @Option(key = "config key")
        public static String configOption;

        @Option(key = "config int")
        public static int configInt;

        @Option(key = "config double")
        public static double configDouble;

        @Option(key = "integer disguised as string")
        public static int configIntDisguised;

        static class Inner {

            @Option(key = "more config keys.inner")
            public static String anInnerConfigOption;

            @Option(key = "more config keys.inner string disguised as integer")
            public static String innerDisguisedString;

        }

        @Option(key = "messages key")
        public static String messagesOption;

    }

}
