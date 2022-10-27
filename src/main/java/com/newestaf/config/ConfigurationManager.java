package com.newestaf.config;

import com.newestaf.annotation.FactoryMethod;
import com.newestaf.exception.NewestAFException;
import com.newestaf.util.Debugger;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("ConstantConditions")
public class ConfigurationManager {

    private final Plugin plugin;
    private final Configuration config;
    private final Configuration descConfig;
    private final Map<String,Class<?>> forceTypes = new HashMap<>();

    private final ConfigurationListener listener;
    private final String prefix;
    private final boolean validate;

    private ConfigurationManager(ConfigurationManagerBuilder builder) {
        this.plugin = builder.plugin;
        this.prefix = builder.prefix;
        this.listener = builder.listener;

        this.config = builder.config;
        config.options().copyDefaults(true);

        this.descConfig = new MemoryConfiguration();

        this.validate = builder.validate;

        plugin.saveConfig();
    }

    //region Getter, Setter
    public boolean isValidate() {
        return validate;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Configuration getConfig() {
        return config;
    }

    public void forceType(String key, Class<?> c) {
        forceTypes.put(key, c);
    }

    public void forceType(String key, String ClassName) {
        try {
            forceTypes.put(key, Class.forName(ClassName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public Class<?> getType(String key) {
        if (forceTypes.containsKey(key)) {
            return forceTypes.get(key);
        }
        else {
            key = addPrefixToKey(key);
            if (config.getDefaults().contains(key)) {
                return config.getDefaults().get(key).getClass();
            }
            else if (config.contains(key)) {
                return config.get(key).getClass();
            }
            else {
                throw new IllegalArgumentException("Can't Determine type for unknown key: " + "'" + key + "'");
            }
        }
    }

    //endregion

    public void insert(String key, Object value) {
        String prefixedKey = addPrefixToKey(key);
        if (config.contains(prefixedKey)) {
            throw new NewestAFException("Key already exists: " + "'" + prefixedKey + "'");
        }
        config.addDefault(prefixedKey, value);
        config.getDefaults().set(prefixedKey, value);
    }

    public Object get(String key) {
        String prefixedKey = addPrefixToKey(key);
        if (!config.contains(prefixedKey)) {
            throw new NewestAFException("Key does not exist: " + "'" + prefixedKey + "'");
        }
        return config.get(prefixedKey);
    }

    public Object check(String key) {
        String prefixedKey = addPrefixToKey(key);

        return config.get(prefixedKey);
    }

    public void set(String key, String value) {
        Object current = get(key);

        setItem(key, value);

        if (listener != null) {
            listener.onConfigurationChanged(this, key, current, get(key));
        }

        if (plugin != null) {
            plugin.saveConfig();
        }
    }

    public <T> void set(String key, List<T> value) {
        Object current = get(key);

        setItem(key, value);

        if (listener != null) {
            listener.onConfigurationChanged(this, key, current, get(key));
        }

        if (plugin != null) {
            plugin.saveConfig();
        }
    }

    public String addPrefixToKey(String key) {
        return prefix == null ? key : prefix + "." + key;
    }

    public String removePrefixFromKey(String key) {
        return key.replaceAll("^" + prefix + "\\.", "");
    }

    public void setDescription(String key, String desc) {
        String prefixedKey = addPrefixToKey(key);
        if (!config.contains(prefixedKey)) {
            throw new NewestAFException("No such config item: " + prefixedKey);
        }
        descConfig.set(prefixedKey, desc);
    }

    public String getDescription(String key) {
        String prefixedKey = addPrefixToKey(key);
        if (!config.contains(prefixedKey)) {
            throw new NewestAFException("No such config item: " + prefixedKey);
        }
        return descConfig.getString(prefixedKey, "");
    }

    @SuppressWarnings("unchecked")
    private void setItem(String key, String value) {
        Class<?> c = getType(key);
        Debugger.getInstance().debug(2, "setItem: key: " + key + " value: " + value + " type: " + c.getName());

        Object processedValue = null;

        if (value == null) {
            processedValue = null;
        }
        else if (List.class.isAssignableFrom(c)) {
            List<String> list = new ArrayList<>();
            list.add(value);
            processedValue = list;
        }
        else if (String.class.isAssignableFrom(c)) {
            processedValue = value;
        }
        else if (Enum.class.isAssignableFrom(c)) {
            @SuppressWarnings("rawtypes")
            Class<? extends Enum> cSub = c.asSubclass(Enum.class);

            try {
                processedValue = Enum.valueOf(cSub, value);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid value for enum: " + "'" + value + "'");
            }
        }
        else {
            try {
                for (Method method : c.getDeclaredMethods()) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length != 1 || !String.class.isAssignableFrom(params[0]))
                        continue;
                    if (method.isAnnotationPresent(FactoryMethod.class) && Modifier.isStatic(method.getModifiers())) {
                        processedValue = method.invoke(null, value);
                        break;
                    }
                }
                if (processedValue == null) {
                    Constructor<?> ctor = c.getDeclaredConstructor(String.class);
                    processedValue = ctor.newInstance(value);
                }
            }
            catch (NoSuchMethodException e) {
                throw new NewestAFException("Cannot convert '" + value + "' into a " + c.getName());
            }
            catch (IllegalArgumentException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            catch (InvocationTargetException e) {
                if (e.getCause() instanceof NumberFormatException) {
                    throw new NewestAFException("Invalid numeric value: " + value);
                }
                else if (e.getCause() instanceof IllegalArgumentException) {
                    throw new NewestAFException("Invalid argument: " + value);
                }
                else {
                    e.printStackTrace();
                }
            }

        }

        if (processedValue != null || value == null) {
            if (listener != null && validate) {
                processedValue = listener.onConfigurationValidate(this, key, get(key), processedValue);
            }
            config.set(addPrefixToKey(key), processedValue);
        } else {
            throw new NewestAFException("Don't know what to do with " + key + " = " + value);
        }

    }

    private <T> void setItem(String key, List<T> list) {
        String prefixedKey = addPrefixToKey(key);
        if (config.getDefaults().get(prefixedKey) == null) {
            throw new NewestAFException("No such Key: " + "'" + prefixedKey + "'");
        }
        if (!(config.getDefaults().get(prefixedKey) instanceof List<?>)) {
            throw new NewestAFException("Key is not a List: " + "'" + prefixedKey + "'");
        }
        if (listener != null && validate) {
            //noinspection unchecked
            list = (List<T>) listener.onConfigurationValidate(this, key, get(key), list);
        }
        config.set(addPrefixToKey(key), list);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> handleListValue(String key, List<T> list) {
        HashSet<T> current = new HashSet<>((List<T>) config.getList(addPrefixToKey(key)));

        if (list.get(0).equals("-")) {
            // Remove specified item from list
            list.remove(0);
            list.forEach(current::remove);
        }
        else if (list.get(0).equals("=")) {
            // Replace list with specified items
            list.remove(0);
            current = new HashSet<>(list);
        }
        else if (list.get(0).equals("+")) {
            // Add specified items to list
            list.remove(0);
            current.addAll(list);
        }
        else {
            // Replace list with specified items
            current.addAll(list);
        }

        return new ArrayList<>(current);
    }


    public static class ConfigurationManagerBuilder {

        private final Plugin plugin;
        private Configuration config;
        private ConfigurationListener listener;
        private String prefix;
        private boolean validate;

        public ConfigurationManagerBuilder(Plugin plugin) {
            this.plugin = plugin;
        }
        public ConfigurationManagerBuilder config(Configuration config) {
            this.config = config;
            return this;
        }

        public ConfigurationManagerBuilder listener(ConfigurationListener listener) {
            this.listener = listener;
            return this;
        }

        public ConfigurationManagerBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public ConfigurationManagerBuilder validate(boolean validate) {
            this.validate = validate;
            return this;
        }

        public ConfigurationManager build() {
            return new ConfigurationManager(this);
        }

    }


}
