package com.kon.orientdb.transform.service;

import com.kon.orientdb.transform.exception.TransformationException;

import java.util.List;
import java.util.Map;

/**
 * Created by kshevchuk on 10/2/2015.
 */
public interface TransformationService {

    Map<String, List<String>> parse(String filePath) throws TransformationException;

    boolean performTransformation(Map<String, List<String>> data) throws TransformationException;
}
