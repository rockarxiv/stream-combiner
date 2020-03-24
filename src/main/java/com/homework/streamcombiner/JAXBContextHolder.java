package com.homework.streamcombiner;

import com.homework.streamcombiner.busobj.Data;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBContextHolder {

    public static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(Data.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not initialise JAXBContext", e);
        }
    }
}
