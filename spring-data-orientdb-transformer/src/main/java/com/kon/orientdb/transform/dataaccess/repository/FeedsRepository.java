package com.kon.orientdb.transform.dataaccess.repository;

import com.kon.orientdb.transform.dataaccess.data.Feeds;
import org.springframework.data.orient.object.repository.OrientObjectRepository;

import java.util.List;

/**
 * Created by kshevchuk on 10/2/2015.
 */
public interface FeedsRepository extends OrientObjectRepository<Feeds> {
    List<Feeds> findByinAndType(String nodeId, String type);
}
