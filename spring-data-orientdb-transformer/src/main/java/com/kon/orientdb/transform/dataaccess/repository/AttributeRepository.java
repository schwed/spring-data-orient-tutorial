package com.kon.orientdb.transform.dataaccess.repository;

import com.kon.orientdb.transform.dataaccess.data.Attribute;
import com.kon.orientdb.transform.exception.TransformationException;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.orient.object.repository.OrientObjectRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * Created by kshevchuk on 10/2/2015.
 */
public interface AttributeRepository extends OrientObjectRepository<Attribute> {

    Attribute findByNodeidAndType(String nodeid, String type);

    @Component
    class AttributeGraphRepository {

        private static final Logger logger = LogManager.getLogger(AttributeGraphRepository.class);

        @Autowired
        Environment environment;

        @Autowired
        OrientGraphFactory orientGraphFactory;

        Boolean testMode = Boolean.FALSE;

        /**
         * the collections remain,
         * because the cost of removing them is higher than just set to 0 elements.
         * However the traversing considers empty collection as no edge,
         * so everything is consistent.
         * @param targetNodeId
         */
        public void removeEdges(String targetNodeId) {

            OrientGraph orientGraph = orientGraphFactory.getTx();
            try {
                Iterable<Vertex> target = orientGraph.getVertices("Attribute", new String[]{"nodeid", "type"}, new Object[]{targetNodeId, "target"});
                Vertex targetVertex = target.iterator().hasNext() ? target.iterator().next() : null;

                if (targetVertex == null) {
                    throw new TransformationException("CAN'T REMOVE IN FEEDS BECAUSE TARGET VERTEX IS NULL.");
                }

                Iterable<Edge> targetVertexInEdges = targetVertex.getEdges(Direction.IN);
                for (Edge edge : targetVertexInEdges) {
                    if (edge.getProperty("type").equals("correlation")) {
                        logger.trace("WILL REMOVE IN FEEDS EDGE: " + edge.getId());
                        if (testMode) {
                            orientGraph.getRawGraph().begin();
                            orientGraph.removeEdge(edge);
                            orientGraph.rollback();
                        }
                        else {
                            orientGraph.removeEdge(edge);
                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
                orientGraph.rollback();
            } finally {
                if (testMode) {
                    orientGraph.rollback();
                    // close db without commit
                    orientGraph.shutdown(true, false);
                } else {
                    // will commit and close db
                    orientGraph.shutdown();
                }
            }
        }

        @PostConstruct
        public void init() {
            MutablePropertySources envPropSources = ((ConfigurableEnvironment) environment).getPropertySources();
            testMode = (Boolean) envPropSources.get("TEST-MODE").getProperty("testMode");
        }

    }
}
