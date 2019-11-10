# Configuralize
A config library that doesn't waste your time. Built-in internationalization.
Supports *.yml files via SnakeYAML and .json files via json-simple.

# Example usage
Files are defined as their own folder in resources. In the below example,
the `config` and `messages` config resources have `en`, English; `fr`, French;
and `de`, German translations.
```
/resources/config/en.yml
/resources/config/fr.yml
/resources/config/de.yml
/resources/messages/en.yml
/resources/messages/fr.yml
/resources/messages/de.yml
```
```java
DynamicConfig config = new DynamicConfig();
config.addSource(Test.class, "config", new File("config.yml"));
config.addSource(Test.class, "messages", new File("messages.yml"));
config.saveAllDefaults(false /* overwrite files if they already exist? */);
config.loadAll();

// given either the config or messages resources contain a key for "Test key"...
String value = config.getString("Test key");
Optional<String> optionalValue = config.getOptionalString("Test key");
String otherwiseValue = config.getStringElse("Test key", "value if key not in either resource");
```

See more detailed usage in https://github.com/Scarsz/Configuralize/tree/master/src/test.