package desmoj.extensions.visualization2d.engine.viewer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import desmoj.extensions.visualization2d.engine.Constants;

public class LanguageSupport {

    private ResourceBundle bundle = null;
    private Locale locale = null;

    protected LanguageSupport(Locale locale) {

        this.locale = locale;
        this.bundle = ResourceBundle.getBundle(
            Constants.PACKAGE_PATH + Constants.BUNDLE_NAME, locale, new XMLControl());
    }

    public Locale getLocale() {
        return this.locale;
    }

    public String getString(String key) {
        return this.bundle.getString(key);
    }

    public URL getInternURL(String key) {
        return getClass().getResource(Constants.FILE_PATH + bundle.getString(key));
    }

    public URL getExternURL(String key) {
        URL out = null;
        try {
            out = new URL(bundle.getString(key));
        } catch (MalformedURLException e) {
            out = null;
        }
        return out;
    }


    private class XMLControl extends ResourceBundle.Control {

        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return Arrays.asList("xml");
        }

        public ResourceBundle newBundle(String baseName, Locale locale,
                                        String format, ClassLoader loader, boolean reload) throws IOException {
            ResourceBundle bundle = null;
            if (baseName == null || locale == null || format == null || loader == null) {
                throw new NullPointerException();
            }
            if (format.equals("xml")) {
                String bundleName = this.toBundleName(baseName, locale);
                String resourceName = this.toResourceName(bundleName, format);
                InputStream stream = null;
                if (reload) {
                    URL url = loader.getResource(resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        if (connection != null) {
                            // Disable caches
                            connection.setUseCaches(false);
                            stream = connection.getInputStream();
                        }
                    }
                } else {
                    stream = loader.getResourceAsStream(resourceName);
                }
                if (stream != null) {
                    BufferedInputStream bis = new BufferedInputStream(stream);
                    bundle = new XMLResourceBundle(bis);
                    bis.close();
                }
            }
            return bundle;
        }
    }

    private class XMLResourceBundle extends ResourceBundle {

        private final Properties props;

        XMLResourceBundle(InputStream stream) throws IOException {
            this.props = new Properties();
            this.props.loadFromXML(stream);

        }

        public Enumeration<String> getKeys() {
            Vector<String> v = new Vector<String>();
            Enumeration<Object> en = this.props.keys();
            while (en.hasMoreElements()) {
                v.add((String) en.nextElement());
            }
            return v.elements();
        }

        protected Object handleGetObject(String key) {
            return this.props.getProperty(key);
        }

    }

}
