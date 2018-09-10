package com.vanillage.bukkitutils.configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

public class ValueGetterParentChild extends ValueGetter {// TODO: Not finished. No difference to super at the moment.
    @Override
    public <T extends Object> T getValue(ConfigurationSection configurationSection, String path, Class<T> clazz) {
        if (configurationSection == null) {
            throw new IllegalArgumentException("configurationSection cannot be null");
        }

        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        Object value = configurationSection.get(path);

        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        if (value == null) {
            return null;
        }

        if (configurationSection instanceof MemorySection) {
            try {
                Method method = MemorySection.class.getDeclaredMethod("getDefault", String.class);
                method.setAccessible(true);
                value = method.invoke(configurationSection, path);

                if (clazz.isInstance(value)) {
                    return clazz.cast(value);
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public <T extends Object> String getPath(ConfigurationSection configurationSection, String path, Class<T> clazz) {
        if (configurationSection == null) {
            throw new IllegalArgumentException("configurationSection cannot be null");
        }

        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        Configuration root = configurationSection.getRoot();

        if (root == null) {
            throw new IllegalStateException("Cannot access section without a root");
        }

        Object value = configurationSection.get(path);

        if (clazz.isInstance(value)) {
            return configurationSection.getCurrentPath() + root.options().pathSeparator() + path;
        }

        if (value == null) {
            return null;
        }

        if (configurationSection instanceof MemorySection) {
            try {
                Method method = MemorySection.class.getDeclaredMethod("getDefault", String.class);
                method.setAccessible(true);

                if (clazz.isInstance(method.invoke(configurationSection, path))) {
                    return configurationSection.getCurrentPath() + root.options().pathSeparator() + path;
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
