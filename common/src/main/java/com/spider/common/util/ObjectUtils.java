package com.spider.common.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ObjectUtils {
    private final ObjectMapper mapper;

    @Autowired
    public ObjectUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T mergeObject(Object from, T into) throws IOException {
        ObjectReader updater = mapper.readerForUpdating(into);
        return updater.readValue(mapper.writeValueAsString(from));
    }
}
