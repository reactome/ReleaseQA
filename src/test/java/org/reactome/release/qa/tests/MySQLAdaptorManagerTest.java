package org.reactome.release.qa.tests;

import org.gk.persistence.MySQLAdaptor;
import org.junit.Test;
import org.reactome.release.qa.common.MySQLAdaptorManager;

public class MySQLAdaptorManagerTest {


    @Test
    public void testGetDBA() throws Exception {
        MySQLAdaptorManager manager = MySQLAdaptorManager.getManager();
        MySQLAdaptor dba = manager.getDBA();
        System.out.println("dbName: " + dba.getDBName() + "@" + dba.getDBHost());
    }


}