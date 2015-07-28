package com.acme.dependencygraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author ... ME!
 *
 */
public class App 
{
    /**
     * Entry method
     * @param args file name to parse
     */
    public static void main( String[] args ){
        if (args.length !=  1) {            
            System.out.println("Usage java -jar App.jar <file_to_parse.txt>");
            System.exit(1);            
        }
        
        File inputFile = new File(args[0]);
        if (!inputFile.exists()){
            System.out.println("File does not exist");
            System.exit(1);                        
        }
        
        List<GraphItem> entries = loadData(inputFile);
        Map<String, GraphItem> graphMap = normalize(entries); //normalized items in fast cache
        
        GraphItem rootElement = findRootElement(graphMap);//locate root element
        buildDependencyGraph(graphMap, rootElement);
        printGraph(rootElement, false, new Stack<String>());
        
        //System.exit(0); // say bye, bye (TODO this breaks the tests)
    }

    /**
     * 
     * @param f input file, expecting record on every new line
     * @return array of String
     */
    static List<GraphItem> loadData(File f){
        BufferedReader br = null;

        List<GraphItem> items = new ArrayList();
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null){
                String[] r = line.split("->"); //NOI18N
                assert r.length == 2 : "Record array must be exactly 2 items";
                String parentName = r[0];
                String childName =  r[1];
                items.add(new GraphItem(parentName, new GraphItem(childName, null)));
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); //NOI18N
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); //NOI18N
        } finally {
            if (br != null){
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex); //NOI18N
                }
            }
        }  
        return items;
    }
    
    /**
     * Create map of elements and their respective children (without their children) normalizing same parent elements
     * @param entries loaded from file
     * @return map of entries has all their respective children (mapped by element name for fast access)
     */
    static Map<String, GraphItem> normalize(List<GraphItem> entries) {
        Map<String, GraphItem> map = new HashMap();
        
        for (Iterator<GraphItem> it = entries.iterator(); it.hasNext();) {
            GraphItem next = it.next();
            GraphItem gi = map.get(next.name);
            if (gi == null){
                map.put(next.name, next);
            } else {
                gi.childDependencies.addAll(next.childDependencies);
                it.remove(); //do not need this element one anymore (dupe)
            }            
        }
        return map;
    }

    /**
     * Build dependency graph using previously created cache
     * @param graphMap normalized map with all elements
     * @param rootElement the top element which is not child of anyone
     */
    static void buildDependencyGraph(Map<String, GraphItem> graphMap, GraphItem rootElement) {
        List<GraphItem> children = rootElement.childDependencies;
        int index = 0;
        for (Iterator<GraphItem> it = children.iterator(); it.hasNext(); index++) {
            GraphItem graphItem = it.next();
            assert graphItem != null : "Next item can't be null";
            GraphItem realChild = graphMap.get(graphItem.name);
            if (realChild == null){
                return;
            }
            children.set(index, realChild); //replace with real child with dependencies
            buildDependencyGraph(graphMap, realChild);
        }
    }

    /**
     * Searches for root element (element which does not depend on anything)
     * Will not work on circual dependency !!
     * Will not work on more than one root !!
     * 
     * @param graphMap with all correctly mapped elements
     * @return GraphItem which is root of dependency graph 
     */
    static GraphItem findRootElement(Map<String, GraphItem> graphMap) {
        //create independent copy to avoid modification of original map!! Lil quirky though....
        Set<String> hasDependency = new HashSet(Arrays.asList(graphMap.keySet().toArray(new String[graphMap.size()])));
        for (Map.Entry<String, GraphItem> entry : graphMap.entrySet()) {
            String string = entry.getKey();
            List<GraphItem> deps = entry.getValue().childDependencies;
            for (Iterator<GraphItem> it = deps.iterator(); it.hasNext();) {
                GraphItem graphItem = it.next();
                hasDependency.remove(graphItem.name);
            }
            
        }
        assert hasDependency.size() == 1 : "Couldn't locate root element";
        
        return graphMap.get(hasDependency.iterator().next());
    }

    /**
     * Print constants
     */
    private static final String CHILD_INDENT   = "|   ";
    private static final String DEFAULT_INDENT = "    ";
    private static final String DEFAULT_PATH  =  "|---";
    private static final String LAST_PATH     =  "\\---";
    
    static void printGraph(GraphItem rootElement, boolean lastElement, Stack<String> depth) {
        for (int i = 0; i < depth.size(); i++){
            System.out.print(depth.elementAt(i));
        }        
        System.out.println(depth.isEmpty()? rootElement.toString() : rootElement.toString(lastElement));
            
        if(!rootElement.childDependencies.isEmpty()){
            if (depth.isEmpty()){
                depth.push("");
            } else if (lastElement) {
                depth.push(DEFAULT_INDENT);
            } else {
                depth.push(CHILD_INDENT);
            }
            
            for (Iterator<GraphItem> it = rootElement.childDependencies.iterator(); it.hasNext();) {
                GraphItem item = it.next();
                printGraph(item, !it.hasNext(), depth);
            }                                
            depth.pop();
        } 
    }
       
    static class GraphItem {
        private final String name;
        private List<GraphItem> childDependencies = new ArrayList<GraphItem>();

        public GraphItem(String name, GraphItem child) {
            this.name = name;
            if (child != null){
                childDependencies.add(child);
            }
        }

        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }        

        /**
         * @param last true if it is last element in collection of children
         * @return well formated name of element
         */
        public String toString(boolean last) {
            return (last ? LAST_PATH : DEFAULT_PATH) + name;
        }        

        /*
         * This can be easily acheived manually, this show usage of Maven library depndency and its usage
         */
        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(this.name).hashCode();
        }

        /*
         * This can be easily acheived manually, this show usage of Maven library depndency and its usage
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GraphItem other = (GraphItem) obj;
            return new EqualsBuilder().append(this.name, other.name).isEquals();
        }        
    }    
}
