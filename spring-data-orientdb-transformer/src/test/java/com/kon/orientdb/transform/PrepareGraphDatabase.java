package com.kon.orientdb.transform;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kshevchuk on 10/14/2015.
 */
public class PrepareGraphDatabase {

    private static final Logger logger = LoggerFactory.getLogger(PrepareGraphDatabase.class);

    public static void main(String[] args) {

        OrientGraphFactory factory = new OrientGraphFactory("remote:USMDCKDDB6042/InformaticaTest", "root", "Genie.2013");
        OrientGraph graph = factory.getTx();

        try {

            // remove all test vertices
            List<String> list = removeAllVerticesLike(graph, "konstantin_nodeid_%");

            for (String vertexId : list) {
                System.out.println("REMOVED VERTEX WITH ID: " + vertexId);
            }

            /*
            CSVParser parser = new CSVParser();
            DirectoryTraverseServiceImpl traverseService = new DirectoryTraverseServiceImpl();
            List<String> filesList = traverseService.traverseDirectory(Paths.get("C:/DOWNLOADS/DOCUMENTS/KPMG/ORIENT/TRANSFORM"));
            for (String file : filesList) {
                List<String> removedList = new ArrayList<>();
                Map<String, List<String>> data = parser.parse(file);
                List<String> sourceNodeIdList = data.get("source".toUpperCase());
                List<String> targetNodeIdList = data.get("target".toUpperCase());

                String sourceNodeId;
                String targetNodeId;

                // for each row from csv file
                for (int i = 0; i < sourceNodeIdList.size(); i++) {
                    sourceNodeId = sourceNodeIdList.get(i);
                    targetNodeId = targetNodeIdList.get(i);
                    removedList.add(removeVertex(graph, sourceNodeId, "source"));
                    removedList.add(removeVertex(graph, targetNodeId, "target"));

                }

                for (String vertexID : removedList) {
                    System.out.println("REMOVED VERTEX: " + vertexID);
                }
            }
            */
            List<String> sourceIdList = populateVertices(graph, "source", 25);
            List<String> targetIdList = populateVertices(graph, "target", 25);
            List<String> edgeIdList = new ArrayList<>();
            String edgeId;
            for (int i = 0; i < targetIdList.size(); i++) {
                edgeId = populateEdges(graph, "correlation", sourceIdList.get(i), targetIdList.get(i));
                System.out.println("EDGE ID: " + edgeId);
                edgeIdList.add(edgeId);
            }

            graph.commit();

            /*
            for (Vertex vertex : graph.getVertices()) {
                if (vertex.getProperty("nodeid").toString().startsWith("konstantin_nodeid") ) {

                }
                logger.debug("Vertex: {}", vertex);
                for (Edge edge : vertex.getEdges(Direction.BOTH)) {
                    logger.debug("Edge: {}", edge);
                }

                logger.debug("Property keys: {}", vertex.getPropertyKeys());
            }

            for (Edge edge : graph.getEdges()) {
                logger.debug("Edge: {}", edge);
            }
            */
            //graph.addEdge(null, graph.getVertex("#13:0"), graph.getVertex("#13:0"), "link");
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            graph.shutdown();
        }
    }

    private static List<String> populateVertices(OrientGraph graph, String vertexType, int howMany) throws Exception {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            Map<String, String> vertexProperties = new HashMap<>();
            vertexProperties.put("nodeid", "konstantin_nodeid_" + i);
            vertexProperties.put("name", "konstantin_name_" + i);
            vertexProperties.put("repo", "konstantin_repo_" + i);
            vertexProperties.put("folder", "konstantin_folder_" + i);
            vertexProperties.put("source", "konstantin_source_" + i);
            vertexProperties.put("type", vertexType);
            Vertex attribute = graph.addVertex("class:Attribute", vertexProperties);
            list.add(attribute.getId().toString());
        }
        return list;
    }

    private static String populateEdges(OrientGraph graph, String edgeType, String from, String to) throws Exception {

        Map<String, String> linkProperties = new HashMap<>();
        linkProperties.put("confidence", "1");
        linkProperties.put("fileName", "Konstantin_Bonus_Transaction_Control");
        linkProperties.put("type", edgeType);

        OrientEdge edge;
        edge = graph.addEdge("class:Feeds", graph.getVertex(from), graph.getVertex(to), "Feeds");
        edge.setProperties(linkProperties);
        return edge.getProperty("@rid").toString();
    }

    private static String removeVertex(OrientGraph graph, String nodeId, String type) throws Exception {
        Iterable<Vertex> source = graph.getVertices("Attribute", new String[]{"nodeid", "type"}, new Object[]{nodeId, type});
        Vertex vertex = source.iterator().hasNext() ? source.iterator().next() : null;
        graph.removeVertex(vertex);

        return vertex.getId().toString();
    }

    private static List<String> removeAllVerticesLike(OrientGraph graph, String like) {
        List<String> list = new ArrayList<>();
        String query = String.format("select * from V where nodeid like \"%s\"", like);
        OSQLSynchQuery<OrientVertex> qr = new OSQLSynchQuery<>(query);
        Iterable<OrientVertex> vertices = graph.command(qr).execute();

        for (OrientVertex vertex : vertices) {
            list.add(vertex.getId().toString());
            //graph.removeVertex(vertex);
            vertex.remove();
        }


        return list;
    }
}
