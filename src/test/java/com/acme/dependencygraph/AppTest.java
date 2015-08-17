/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.acme.dependencygraph;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author suchys
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest {
    
    private static String pathToFile;
    private static List<App.GraphItem> entries;
    private static Map<String, App.GraphItem> graphMap = null;
    private static App.GraphItem rootElement;
    
    public AppTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        InputStream is = AppTest.class.getResourceAsStream("graph.txt");
        DataOutputStream dos = null;
        try {
            File tmp = File.createTempFile("graph", "txt");
            byte[] data = new byte[1024];
            dos = new DataOutputStream(new FileOutputStream(tmp));
            int len;
            while ((len = is.read(data)) > 0){
                dos.write(data, 0, len);
            }
            pathToFile = tmp.getAbsolutePath();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                dos.close();
            } catch (IOException ex) {
                Logger.getLogger(AppTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(AppTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        assertNotNull("Path to source file is null", pathToFile);
        
        entries = new ArrayList<>();
        entries.add(new App.GraphItem("B", new App.GraphItem("C", null)));
        entries.add(new App.GraphItem("B", new App.GraphItem("D", null)));
        entries.add(new App.GraphItem("D", new App.GraphItem("E", null)));
        entries.add(new App.GraphItem("A", new App.GraphItem("B", null)));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class App.
     */
    @org.junit.Test
    public void A_testMain() {
        File f = new File(pathToFile);
        assertEquals(true, f.exists());
        String[] args = {pathToFile};
        App.main(args);
    }

    /**
     * Test of loadData method, of class App.
     */
    @org.junit.Test
    public void B_testLoadData() {
        System.out.println("loadData");
        File f = new File(pathToFile);
        assertEquals(true, f.exists());
        
        BufferedReader br = null;
        int lines = 0;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null){
                String[] r = line.split("->"); //NOI18N
                assertEquals( 2, r.length);
                lines++;
            }
            
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            if (br != null){
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }  

        List result = App.loadData(f);
        
        assertEquals(result.size(), lines);
    }

    /**
     * Test of normalize method, of class App.
     */
    @org.junit.Test
    public void C_testNormalize() {
        System.out.println("normalize");
        graphMap = App.normalize(entries);
        assertEquals(3, graphMap.size());
    }

    /**
     * Test of findRootElement method, of class App.
     */
    @org.junit.Test
    public void D_testFindRootElement() {
        System.out.println("findRootElement");
        rootElement = App.findRootElement(graphMap);
        assertEquals(entries.get(entries.size()-1), rootElement);
    }

    /**
     * Test of buildDependencyGraph method, of class App.
     */
    @org.junit.Test
    public void E_testBuildDependencyGraph() {
        System.out.println("buildDependencyGraph");
        App.buildDependencyGraph(graphMap, rootElement);
    }


    /**
     * Test of printGraph method, of class App.
     */
    @org.junit.Test
    public void F_testPrintGraph() {
        System.out.println("printGraph");
        PrintStream ps = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));        
        App.printGraph(rootElement, false, new Stack());
        System.setOut(ps);
        String out = new String(baos.toByteArray());
        //TODO should be loaded from golden file
        assertEquals("A\r\n\\---B\r\n    |---C\r\n    \\---D\r\n", out);        
    }    
}
