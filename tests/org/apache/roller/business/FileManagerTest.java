/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.business;

import java.io.InputStream;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.model.FileManager;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.RollerPropertyData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.RollerMessages;


/**
 * Test File Management business layer operations.
 */
public class FileManagerTest extends TestCase {
    
    public static Log log = LogFactory.getLog(FileManagerTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    
    
    public FileManagerTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(FileManagerTest.class);
    }
    
    
    public void setUp() throws Exception {
        
        try {
            testUser = TestUtils.setupUser("FileManagerTest_userName");
            testWeblog = TestUtils.setupWeblog("FileManagerTest_handle", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
    }
    
    
    public void testCanSave() throws Exception {
        
        // update roller properties to prepare for test
        PropertiesManager pmgr = RollerFactory.getRoller().getPropertiesManager();
        Map config = pmgr.getProperties();
        ((RollerPropertyData)config.get("uploads.enabled")).setValue("false");
        ((RollerPropertyData)config.get("uploads.types.forbid")).setValue("gif");
        ((RollerPropertyData)config.get("uploads.dir.maxsize")).setValue("1.00");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        // test quota functionality
        FileManager fmgr = RollerFactory.getRoller().getFileManager();
        RollerMessages msgs = new RollerMessages();
        assertFalse(fmgr.canSave(testWeblog.getHandle(), "test.gif", "image/gif", 2500000, msgs));
    }
    
    
    public void testSave() throws Exception {
        
        // update roller properties to prepare for test
        PropertiesManager pmgr = RollerFactory.getRoller().getPropertiesManager();
        Map config = pmgr.getProperties();
        ((RollerPropertyData)config.get("uploads.enabled")).setValue("true");
        ((RollerPropertyData)config.get("uploads.types.allowed")).setValue("opml");
        ((RollerPropertyData)config.get("uploads.dir.maxsize")).setValue("1.00");
        pmgr.saveProperties(config);
        TestUtils.endSession(true);
        
        /* NOTE: upload dir for unit tests is set in
               roller/personal/testing/roller-custom.properties */
        FileManager fmgr = RollerFactory.getRoller().getFileManager();
        RollerMessages msgs = new RollerMessages();
        
        // store a file
        InputStream is = getClass().getResourceAsStream("/bookmarks.opml");
        fmgr.saveFile(testWeblog.getHandle(), "bookmarks.opml", "text/xml", 1545, is);
        
        // make sure file was stored successfully
        assertEquals(1, fmgr.getFiles(testWeblog.getHandle()).length);
        
        // delete a file
        fmgr.deleteFile(testWeblog.getHandle(), "bookmarks.opml");
        
        // make sure delete was successful
        Thread.sleep(2000);
        assertEquals(0, fmgr.getFiles(testWeblog.getHandle()).length);
    }
    
}
