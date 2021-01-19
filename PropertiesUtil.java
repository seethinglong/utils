/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 19 OCT 2020
 * 
 */

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public final class PropertiesUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static PropertiesUtil instance;
    private CompositeConfiguration config;

    public static String getProperty(String propertyKey) {
        return getInstance().config.getString(propertyKey);
    }

    public static String getProperty(String propertyKey, Object[] arguments) {
        String propertyVal = getProperty(propertyKey);
        if (propertyVal == null) {
            propertyVal = propertyKey;
            return propertyVal;
        } else if (arguments == null) {
            return propertyVal;
        } else {
            return MessageFormat.format(propertyVal, arguments);
        }
    }

    public static String getProperty(String name, String defaultValue) {
        if (getInstance().config.containsKey(name)) {
            return getInstance().config.getString(name, defaultValue);
        } else {
            return defaultValue;
        }
    }

    public static String[] getProperties(String name) {
        if (getInstance().config.containsKey(name)) {
            return getInstance().config.getStringArray(name);
        } else {
            return new String[]{};
        }
    }

    private PropertiesUtil() {
        this.config = new CompositeConfiguration();
        this.loadConfiguration();
    }

    private static PropertiesUtil getInstance() {
        if (instance == null) {
            instance = new PropertiesUtil();
        }
        return instance;
    }

    @SuppressWarnings("deprecation")
    public void loadConfiguration() {
        ConfigurationFactory factory = new ConfigurationFactory();
        factory.setConfigurationURL(getClass().getClassLoader().getResource("propertiesConfig.xml"));
        try {
            config.addConfiguration(factory.getConfiguration());
            config.setListDelimiter(',');
        } catch (ConfigurationException e) {
            logger.error("Unable to get configuration file propertiesConfig.xml, it is not a classpath resource.", e);
        }
    }

}
