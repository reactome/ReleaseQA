package org.reactome.release.qa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gk.persistence.MySQLAdaptor;
import org.gk.util.GKApplicationUtilities;
import org.reactome.release.qa.check.ChecksTwoDatabases;
import org.reactome.release.qa.common.MySQLAdaptorManager;
import org.reactome.release.qa.common.QACheck;
import org.reactome.release.qa.common.QAReport;
import org.reflections.Reflections;
	 
/**
 * The entry point to run all tests.
 * @author wug
 *
 */
public class Main {
    
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        File output = prepareOutput();
        // Make sure we have output
        MySQLAdaptorManager manager = MySQLAdaptorManager.getManager();
        MySQLAdaptor dba = manager.getDBA();
        MySQLAdaptor altDBA = null;
        
        Set<String> testTypes = getTestTypes();
        logger.info("Will execute tests of type: " + testTypes);
        
        // Get the list of QAs from packages
        Reflections reflections = new Reflections("org.reactome.release.qa.check",
                                                  "org.reactome.release.qa.graph");

        Set<Class<? extends QACheck>> releaseQAs = reflections.getSubTypesOf(QACheck.class)
                                                              .stream()
                                                              .filter(checker -> isPicked(checker, testTypes))
                                                              .collect(Collectors.toSet());

        for (Class<? extends QACheck> cls : releaseQAs) {
            QACheck check = cls.newInstance();
            logger.info("Perform " + check.getDisplayName() + "...");
            check.setMySQLAdaptor(dba);
            // Some checks might compare two databases to each other (usually test_reactome_## and test_reactome_##-1)
            // So far, this only happens with CompareSpeciesByClasses, but there could be other multi-database checks in the future.
            if (check instanceof ChecksTwoDatabases) {
                if (altDBA == null) {
                    // Let the exception thrown to the top level to stop the whole execution
                    altDBA = MySQLAdaptorManager.getManager().getAlternateDBA();
                }
                ((ChecksTwoDatabases)check).setOtherDBAdaptor(altDBA);
            }
            QAReport qaReport = check.executeQACheck();
            if (qaReport.isEmpty()) {
            	logger.info("Nothing to report!");
                continue;
            }
            else {
                String fileName = check.getDisplayName();
                qaReport.output(fileName + ".txt", output.getAbsolutePath());
                logger.info("Check "+ output.getAbsolutePath() + "/" + fileName + ".txt for report details.");
            }
        }
    }
    
    private static boolean isPicked(Class<? extends QACheck> checker, Set<String> testTypes) {
        Annotation[] annotations = checker.getAnnotations();
        if (annotations == null)
            return false;
        for (Annotation annotation : annotations) {
            if (testTypes.contains(annotation.annotationType().getSimpleName()))
                return true;
        }
        return false;
    }
    
    /**
     * Get pre-configured test types from auth.properties.
     * @return
     * @throws IOException
     */
    private static Set<String> getTestTypes() throws IOException {
        Set<String> rtn = new HashSet<>();
        InputStream is = MySQLAdaptorManager.getManager().getAuthConfig();
        Properties prop = new Properties();
        prop.load(is);
        String testTypes = prop.getProperty("testTypes");
        if (testTypes == null) {
            rtn.add("SliceQATest");
            rtn.add("GraphQATest");
        }
        else {
            String[] tokens = testTypes.split(",");
            Stream.of(tokens).forEach(token -> rtn.add(token.trim()));
        }
        return rtn;
    }

    private static File prepareOutput() throws IOException {
        String output = "output";
        File file = new File(output);
        if (file.exists()) {
            GKApplicationUtilities.delete(file);
        }
        file.mkdir();
        return file;
    }
    
}
