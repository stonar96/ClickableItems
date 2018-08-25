package com.vanillage.bukkitutils.configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class LocationConfiguration {
	private final ConfigurationSection configurationSection;
	private final ValueGetter valueGetter;
	private final Plugin plugin;
	
	public LocationConfiguration(ConfigurationSection configurationSection) {
		this(configurationSection, new ValueGetter(), null);
	}
	
	public LocationConfiguration(ConfigurationSection configurationSection, ValueGetter valueGetter) {
		this(configurationSection, valueGetter, null);
	}
	
	public LocationConfiguration(ConfigurationSection configurationSection, Plugin plugin) {
		this(configurationSection, new ValueGetter(), plugin);
	}
	
	public LocationConfiguration(ConfigurationSection configurationSection, ValueGetter valueGetter, Plugin plugin) {
		if (configurationSection == null) {
			throw new IllegalArgumentException("configurationSection cannot be null");
		}
		
		if (valueGetter == null) {
			throw new IllegalArgumentException("valueGetter cannot be null");
		}
		
		this.configurationSection = configurationSection;
		this.valueGetter = valueGetter;
		this.plugin = plugin;
	}
	
	public ConfigurationSection getConfigurationSection() {
		return configurationSection;
	}
	
	public ValueGetter getValueGetter() {
		return valueGetter;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
	private WorldGuardPlugin getWorlGuardPlugin() {
		Plugin worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		
		// WorldGuard may not be loaded
		if (worldGuardPlugin == null || !(worldGuardPlugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}
		
		return (WorldGuardPlugin) worldGuardPlugin;
	}
	
	public <T extends Object> Set<T> getValues(Location location, String path, Class<T> clazz) {
		if (location == null) {
			throw new IllegalArgumentException("location cannot be null");
		}
		
		if (location.getWorld() == null) {
			throw new IllegalArgumentException("world of location cannot be null");
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
		
		char pathSeparator = root.options().pathSeparator();
		Set<T> values = new HashSet<T>();
		T value;
		ConfigurationSection locationConfigurationSection;
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("locations" + pathSeparator + location.getWorld().getName() + ";" + String.valueOf(location.getX()).replace(".", "_") + ";" + String.valueOf(location.getY()).replace(".", "_") + ";" + String.valueOf(location.getZ()).replace(".", "_"))) != null && (value = valueGetter.getValue(locationConfigurationSection, path, clazz)) != null) {
			values.add(value);
			return values;
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("locations" + pathSeparator + location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ())) != null && (value = valueGetter.getValue(locationConfigurationSection, path, clazz)) != null) {
			values.add(value);
			return values;
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("chunks" + pathSeparator + location.getWorld().getName() + ";" + location.getChunk().getX() + ";" + location.getChunk().getZ())) != null && (value = valueGetter.getValue(locationConfigurationSection, path, clazz)) != null) {
			values.add(value);
			return values;
		}
		
		WorldGuardPlugin worldGuardPlugin = getWorlGuardPlugin();
		
		if (worldGuardPlugin != null) {
			RegionManager regionManager = worldGuardPlugin.getRegionManager(location.getWorld());
			
			if (regionManager != null) {
				int priority = 0;
				
				for (ProtectedRegion region : regionManager.getApplicableRegions(location)) {
					if ((locationConfigurationSection = configurationSection.getConfigurationSection("regions" + pathSeparator + location.getWorld().getName() + ";" + region.getId())) != null && (value = valueGetter.getValue(locationConfigurationSection, path, clazz)) != null) {
						if (values.size() <= 0 || region.getPriority() == priority) {
							values.add(value);
							priority = region.getPriority();
						} else if (region.getPriority() > priority) {
							values.clear();
							values.add(value);
							priority = region.getPriority();
						}
					}
				}
				
				if (values.size() > 0) {
					return values;
				}
			}
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("worlds" + pathSeparator + location.getWorld().getName())) != null && (value = valueGetter.getValue(locationConfigurationSection, path, clazz)) != null) {
			values.add(value);
			return values;
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("everywhere")) != null && (value = valueGetter.getValue(locationConfigurationSection, path, clazz)) != null) {
			values.add(value);
		}
		
		return values;
	}
	
	public Set<String> getPaths(Location location, String path, Class<? extends Object> clazz) {
		if (location == null) {
			throw new IllegalArgumentException("location cannot be null");
		}
		
		if (location.getWorld() == null) {
			throw new IllegalArgumentException("world of location cannot be null");
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
		
		char pathSeparator = root.options().pathSeparator();
		Set<String> paths = new HashSet<String>();
		String valuePath;
		ConfigurationSection locationConfigurationSection;
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("locations" + pathSeparator + location.getWorld().getName() + ";" + String.valueOf(location.getX()).replace(".", "_") + ";" + String.valueOf(location.getY()).replace(".", "_") + ";" + String.valueOf(location.getZ()).replace(".", "_"))) != null && (valuePath = valueGetter.getPath(locationConfigurationSection, path, clazz)) != null) {
			paths.add(valuePath);
			return paths;
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("locations" + pathSeparator + location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ())) != null && (valuePath = valueGetter.getPath(locationConfigurationSection, path, clazz)) != null) {
			paths.add(valuePath);
			return paths;
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("chunks" + pathSeparator + location.getWorld().getName() + ";" + location.getChunk().getX() + ";" + location.getChunk().getZ())) != null && (valuePath = valueGetter.getPath(locationConfigurationSection, path, clazz)) != null) {
			paths.add(valuePath);
			return paths;
		}
		
		WorldGuardPlugin worldGuardPlugin = getWorlGuardPlugin();
		
		if (worldGuardPlugin != null) {
			RegionManager regionManager = worldGuardPlugin.getRegionManager(location.getWorld());
			
			if (regionManager != null) {
				int priority = 0;
				
				for (ProtectedRegion region : regionManager.getApplicableRegions(location)) {
					if ((locationConfigurationSection = configurationSection.getConfigurationSection("regions" + pathSeparator + location.getWorld().getName() + ";" + region.getId())) != null && (valuePath = valueGetter.getPath(locationConfigurationSection, path, clazz)) != null) {
						if (paths.size() <= 0 || region.getPriority() == priority) {
							paths.add(valuePath);
							priority = region.getPriority();
						} else if (region.getPriority() > priority) {
							paths.clear();
							paths.add(valuePath);
							priority = region.getPriority();
						}
					}
				}
				
				if (paths.size() > 0) {
					return paths;
				}
			}
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("worlds" + pathSeparator + location.getWorld().getName())) != null && (valuePath = valueGetter.getPath(locationConfigurationSection, path, clazz)) != null) {
			paths.add(valuePath);
			return paths;
		}
		
		if ((locationConfigurationSection = configurationSection.getConfigurationSection("everywhere")) != null && (valuePath = valueGetter.getPath(locationConfigurationSection, path, clazz)) != null) {
			paths.add(valuePath);
		}
		
		return paths;
	}
	
	public <T extends Object> T getValue(Location location, String path, Class<T> clazz) {
		Iterator<T> iterator = getValues(location, path, clazz).iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}
	
	public String getString(Location location, String path) {
		Object object = getValue(location, path, Object.class);
		return object == null ? null : object.toString();
	}
	
	public int getInt(Location location, String path) {
		Number number = getValue(location, path, Number.class);
		return number == null ? 0 : number.intValue();
	}
	
	public long getLong(Location location, String path) {
		Number number = getValue(location, path, Number.class);
		return number == null ? 0 : number.longValue();
	}
	
	public double getDouble(Location location, String path) {
		Number number = getValue(location, path, Number.class);
		return number == null ? 0 : number.doubleValue();
	}
	
	public boolean getBoolean(Location location, String path) {
		Boolean value = getValue(location, path, Boolean.class);
		return value == null ? false : value;
	}
	
	public boolean containsBoolean(Location location, String path, boolean value) {
		return getValues(location, path, Boolean.class).contains(value);
	}
	
	public boolean containsBoolean(Location location, String path, boolean value, boolean empty) {
		Set<Boolean> values = getValues(location, path, Boolean.class);
		return values.isEmpty() ? empty : values.contains(value);
	}
	
	public List<String> getStringList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<String>(0);
		}
		
		List<String> result = new ArrayList<String>();
		
		for (Object object : list) {
			if (object instanceof String || object instanceof Integer || object instanceof Boolean || object instanceof Character || object instanceof Byte || object instanceof Short || object instanceof Double ||object instanceof Long || object instanceof Float) {
				result.add(String.valueOf(object));
			}
		}
		
		return result;
	}
	
	public List<Integer> getIntegerList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Integer>(0);
		}
		
		List<Integer> result = new ArrayList<Integer>();
		
		for (Object object : list) {
			if (object instanceof Integer) {
				result.add((Integer) object);
			} else if (object instanceof String) {
				try {
					result.add(Integer.valueOf((String) object));
				} catch (Exception e) {
					
				}
			} else if (object instanceof Character) {
				result.add((int) ((Character) object).charValue());
			} else if (object instanceof Number) {
				result.add(((Number) object).intValue());
			}
		}
		
		return result;
	}
	
	public List<Boolean> getBooleanList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Boolean>(0);
		}
		
		List<Boolean> result = new ArrayList<Boolean>();
		
		for (Object object : list) {
			if (object instanceof Boolean) {
				result.add((Boolean) object);
			} else if (object instanceof String) {
				if (Boolean.TRUE.toString().equals(object)) {
					result.add(true);
				} else if (Boolean.FALSE.toString().equals(object)) {
					result.add(false);
				}
			}
		}
		
		return result;
	}
	
	public List<Double> getDoubleList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Double>(0);
		}
		
		List<Double> result = new ArrayList<Double>();
		
		for (Object object : list) {
			if (object instanceof Double) {
				result.add((Double) object);
			} else if (object instanceof String) {
				try {
					result.add(Double.valueOf((String) object));
				} catch (Exception e) {
					
				}
			} else if (object instanceof Character) {
				result.add((double) ((Character) object).charValue());
			} else if (object instanceof Number) {
				result.add(((Number) object).doubleValue());
			}
		}
		
		return result;
	}
	
	public List<Float> getFloatList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Float>(0);
		}
		
		List<Float> result = new ArrayList<Float>();
		
		for (Object object : list) {
			if (object instanceof Float) {
				result.add((Float) object);
			} else if (object instanceof String) {
				try {
					result.add(Float.valueOf((String) object));
				} catch (Exception e) {
					
				}
			} else if (object instanceof Character) {
				result.add((float) ((Character) object).charValue());
			} else if (object instanceof Number) {
				result.add(((Number) object).floatValue());
			}
		}
		
		return result;
	}
	
	public List<Long> getLongList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Long>(0);
		}
		
		List<Long> result = new ArrayList<Long>();
		
		for (Object object : list) {
			if (object instanceof Long) {
                result.add((Long) object);
            } else if (object instanceof String) {
                try {
                	result.add(Long.valueOf((String) object));
                } catch (Exception e) {
                	
                }
            } else if (object instanceof Character) {
            	result.add((long) ((Character) object).charValue());
            } else if (object instanceof Number) {
            	result.add(((Number) object).longValue());
            }
		}
		
		return result;
	}
	
	public List<Byte> getByteList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Byte>(0);
		}
		
		List<Byte> result = new ArrayList<Byte>();
		
		for (Object object : list) {
			if (object instanceof Byte) {
				result.add((Byte) object);
            } else if (object instanceof String) {
                try {
                	result.add(Byte.valueOf((String) object));
                } catch (Exception e) {
                	
                }
            } else if (object instanceof Character) {
            	result.add((byte) ((Character) object).charValue());
            } else if (object instanceof Number) {
            	result.add(((Number) object).byteValue());
            }
		}
		
		return result;
	}
	
	public List<Character> getCharacterList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Character>(0);
		}
		
		List<Character> result = new ArrayList<Character>();
		
		for (Object object : list) {
			if (object instanceof Character) {
				result.add((Character) object);
            } else if (object instanceof String) {
                String str = (String) object;

                if (str.length() == 1) {
                	result.add(str.charAt(0));
                }
            } else if (object instanceof Number) {
            	result.add((char) ((Number) object).intValue());
            }
		}
		
		return result;
	}
	
	public List<Short> getShortList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		
		if (list == null) {
			return new ArrayList<Short>(0);
		}
		
		List<Short> result = new ArrayList<Short>();
		
		for (Object object : list) {
			if (object instanceof Short) {
				result.add((Short) object);
            } else if (object instanceof String) {
                try {
                	result.add(Short.valueOf((String) object));
                } catch (Exception e) {
                	
                }
            } else if (object instanceof Character) {
            	result.add((short) ((Character) object).charValue());
            } else if (object instanceof Number) {
            	result.add(((Number) object).shortValue());
            }
		}
		
		return result;
	}
	
	public List<Map<?, ?>> getMapList(Location location, String path) {
		List<?> list = getValue(location, path, List.class);
		List<Map<?, ?>> result = new ArrayList<Map<?, ?>>();
		
		if (list == null) {
			return result;
		}
		
		for (Object object : list) {
			if (object instanceof Map) {
				result.add((Map<?, ?>) object);
            }
		}
		
		return result;
	}
}
