package com.juhex.sms.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class HttpJSONResponseWriter<T> {

    private ObjectMapper mapper;
    private Logger logger;

    public HttpJSONResponseWriter() {
        mapper = new ObjectMapper();
        logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }

    public String generate(T t) {
        try (Writer w = new StringWriter()) {
            mapper.writeValue(w, t);
            return w.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
