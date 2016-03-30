package com.kon.orientdb.transform.parser;

import com.kon.orientdb.transform.exception.TransformationException;
import org.h2.tools.Csv;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kshevchuk on 10/2/2015.
 */
@Component
public class CSVParser {

    public Map<String, List<String>> parse(String filePath) throws TransformationException {
        Map<String, List<String>> map;
        Csv csv = new Csv();
        try {
            ResultSet resultSet = csv.read(Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8), null);
            map = processResultSetAsMap(resultSet);
            resultSet.close();
        } catch(IOException | SQLException e) {
            throw new TransformationException("Failed to parse file.", e);
        }

        return map;
    }

    private Map<String, List<String>> processResultSetAsMap(ResultSet resultSet) throws SQLException {

        ResultSetMetaData md = resultSet.getMetaData();
        int columns = md.getColumnCount();
        Map<String, List<String>> map = new HashMap<>(columns);
        for (int i = 1; i <= columns; ++i) {
            map.put(md.getColumnName(i), new ArrayList<String>());
        }
        while (resultSet.next()) {
            for (int i = 1; i <= columns; ++i) {
                map.get(md.getColumnName(i)).add(resultSet.getString(i));
            }
        }

        return map;
    }
}
