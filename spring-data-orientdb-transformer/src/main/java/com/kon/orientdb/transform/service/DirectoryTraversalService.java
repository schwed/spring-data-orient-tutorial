package com.kon.orientdb.transform.service;

import com.kon.orientdb.transform.exception.TransformationException;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by kshevchuk on 10/5/2015.
 */
public interface DirectoryTraversalService {
    List<String> traverseDirectory(Path directory) throws TransformationException;
}
