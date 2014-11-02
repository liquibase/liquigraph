package com.liquigraph.core.model;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({SimpleQuery.class, AndQuery.class, OrQuery.class})
public interface PreconditionQuery {}
