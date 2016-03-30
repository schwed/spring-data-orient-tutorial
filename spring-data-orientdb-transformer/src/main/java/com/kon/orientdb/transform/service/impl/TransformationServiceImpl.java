package com.kon.orientdb.transform.service.impl;

import com.kon.orientdb.transform.dataaccess.data.Attribute;
import com.kon.orientdb.transform.dataaccess.data.Feeds;
import com.kon.orientdb.transform.dataaccess.repository.AttributeRepository;
import com.kon.orientdb.transform.dataaccess.repository.FeedsRepository;
import com.kon.orientdb.transform.exception.TransformationException;
import com.kon.orientdb.transform.parser.CSVParser;
import com.kon.orientdb.transform.service.TransformationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by kshevchuk on 10/2/2015.
 */
@Component
public class TransformationServiceImpl implements TransformationService {
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    AttributeRepository attributeRepository;

    @Autowired
    AttributeRepository.AttributeGraphRepository attributeGraphRepository;

    @Autowired
    FeedsRepository feedsRepository;

    @Autowired
    CSVParser csvParser;

    @Override
    public Map<String, List<String>> parse(String filePath) throws TransformationException {
        return csvParser.parse(filePath);
    }

    @Override
    public boolean performTransformation(Map<String, List<String>> data) throws TransformationException {
        boolean returnStatus = Boolean.FALSE;
        List<String> sourceNodeIdList = data.get("source".toUpperCase());
        List<String> targetNodeIdList = data.get("target".toUpperCase());
        List<String> transformationList = data.get("transformation".toUpperCase());
        List<String> confidenceList = data.get("confidence".toUpperCase());
        List<String> filenameList = data.get("filename".toUpperCase());
        List<String> typeList = data.get("type".toUpperCase());

        String sourceNodeId;
        String targetNodeId;
        String transformation;
        String confidence;
        String filename;
        String type;

        // for each row from csv file
        for (int i = 0; i < sourceNodeIdList.size(); i++) {
            sourceNodeId = sourceNodeIdList.get(i);
            targetNodeId = targetNodeIdList.get(i);
            transformation = transformationList.get(i);
            confidence = confidenceList.get(i);
            filename = filenameList.get(i);
            type = typeList.get(i);

            Attribute sourceVertex = fetchAttribute(sourceNodeId, "source");
            Attribute targetVertex = fetchAttribute(targetNodeId, "target");

            logger.trace("GOT SOURCE VERTEX: " + sourceVertex.getRid());
            logger.trace("GOT TARGET VERTEX: " + targetVertex.getRid());

            // check if the transformation link already exists per source and target vertices.
            List<Feeds> transformations = feedsRepository.findByinAndType(targetVertex.getRid(), "transformation");
            for (Feeds feeds : transformations) {
                logger.trace("WILL DELETE TRANSFORMATION LINK WITH RID: " + feeds.getRid());
                System.out.println("transformation found, rid = " + feeds.getRid());
                feedsRepository.delete(feeds);
            }

            // create transformation link between source and target Vertices
            Feeds sourceToTargetLink = new Feeds();
            sourceToTargetLink.setOut(sourceVertex.getRid());
            sourceToTargetLink.setIn(targetVertex.getRid());
            sourceToTargetLink.setTransformation(transformation);
            sourceToTargetLink.setConfidence(Long.valueOf(confidence));
            sourceToTargetLink.setFileName(filename);
            sourceToTargetLink.setType(type);

            String rid = feedsRepository.save(sourceToTargetLink).getRid();
            logger.trace("SAVED TRANSFORMATION LINK: rid = " + rid);

            // delete in feeds using graph api
            attributeGraphRepository.removeEdges(targetNodeId);

            /*
            // this deletes E but V still keep references because object api is not compatible with graph api
            List<Feeds> targetInFeeds = feedsRepository.findByinAndType(targetVertex.getRid(), "correlation");

            for(Feeds feeds : targetInFeeds) {
                logger.trace("WILL DELETE Feeds: rid = " + feeds.getRid() + ", type = " + feeds.getType());
                feedsRepository.delete(feeds);

            }
            feedsRepository.delete(targetInFeeds);
            */

            if (feedsRepository.findByinAndType(targetVertex.getRid(), "correlation").size() == 0) {
                logger.trace("SUCCESSFULLY DELETED IN-FEEDS.");
                returnStatus = Boolean.TRUE;
            }
        }


        return returnStatus;
    }

    private Attribute fetchAttribute(String nodeId, String type) {
        Attribute attribute;
        try {
            attribute = attributeRepository.findByNodeidAndType(nodeId, type);
        } catch (Exception e) {
            throw new TransformationException("Failed to find Vertex for nodeid: " + nodeId + ", and type: " + type, e);
        }
        return attribute;
    }
}
