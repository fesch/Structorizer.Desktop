/*
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-09-18      Raw types (Class etc.) replaced by type inference 
 *      
 ******************************************************************************************************
 */
package com.creativewidgetworks.goldparser.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ResourceHelper {
    
    /**
     * Scans all classes accessible from the context class loader which belong 
     * to the given package and its descendants.
     * 
     * @param packageName package to search
     * @return list of classes found (never null)
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static List<Class<?>> findClassesInPackage(String packageName) throws ClassNotFoundException, IOException {

        if (packageName == null) {
            return new ArrayList<Class<?>>();
        }
        
        JarFile jarFile = null;
        List<File> dirs = new ArrayList<File>();
    
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String path = URLDecoder.decode(resource.getFile(), "UTF-8");
            jarFile = getJarFile(path);
            if (jarFile != null) {
                break;
            }
            dirs.add(new File(path));
        }
        
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

        if (jarFile != null ) {
            classes.addAll(findClassesFromJar(jarFile, packageName));
        } else {        
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, packageName));
            }
        }

        return classes;
    }

    /*----------------------------------------------------------------------------*/

    /**
     * Scans all classes in the specified jar file which belong to the given 
     * package and its descendants.
     * 
     * @param jarFile to search
     * @param packageName 
     * @return list of classes found (never null)
     * @throws ClassNotFoundException
     * @throws IOException
     */    
    public static List<Class<?>> findClassesFromJar(JarFile jarFile, String packageName) throws ClassNotFoundException, MalformedURLException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (jarFile == null || packageName == null) {
            return classes;
        }
        
        URL[] urls = { new URL("jar:file:/" + jarFile.getName() + "!/")};
        URLClassLoader loader = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
        
        for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
           JarEntry entry = entries.nextElement();
           if (!entry.isDirectory()) {
               String className = entry.getName().replace('/', '.');
               if (className.startsWith(packageName)) {
                   if (className.endsWith(".class") && !className.contains("$")) {
                       className = className.substring(0, className.lastIndexOf('.'));
                       Class<?> clazz = loader.loadClass(className);
                       if (!classes.contains(clazz)) { //Needed to handle a bug in Maven that causes duplicate files to be jarred.
                           classes.add(loader.loadClass(className));
                       }
                   }
               }
           }           
        }
        
        return classes;
    }    
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Helper method used to find all classes in a given directory and its descendants.
     * @param directory 
     * 
     * @param packageName the package name for classes found inside the base directory
     * @return list of classes found (never null)
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (directory == null || packageName == null || !directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().contains(".")) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                }
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
    
    /*----------------------------------------------------------------------------*/
    
    /**
     * Helper method used to open a jar file so resources within it can be accessed.
     * 
     * @param file path of the jar file to access.
     * @return initialized JarFile or null if the jar is invalid.
     * @throws IOException
     */
    private static JarFile getJarFile(String filePath) throws IOException {
        if (filePath != null && filePath.contains(".jar!")) {
            filePath = filePath.replaceAll("%20", " ");
            filePath = filePath.substring((filePath.indexOf("jar:file:/") + 9), filePath.indexOf('!'));
            JarFile jar = new JarFile(filePath);
            Manifest mf = jar.getManifest();
            if (mf != null) {
                return jar;
            } 
            jar.close();
        }
        return null;
    }    

}
