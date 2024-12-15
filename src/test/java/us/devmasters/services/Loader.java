package us.devmasters.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public class Loader {
    private static final Logger logger= LoggerFactory.getLogger(Loader.class);
    public static Class<?> load(String path,String fileName) {
        URL[] urls ;
        try{
            urls=new URL[]{new File(path).toURI().toURL()};
        }catch (MalformedURLException malformedURLException)
        {
            logger.error("Unable to construct a url from path {}",path,malformedURLException);
            return null;
        }
        try (URLClassLoader classLoader = new URLClassLoader(urls)){
            return classLoader.loadClass(fileName);
        }catch (Exception exception)
        {
            logger.error("Unable to load class with name {} from path {}",fileName,path,exception);
        }
        return null;
    }
}
