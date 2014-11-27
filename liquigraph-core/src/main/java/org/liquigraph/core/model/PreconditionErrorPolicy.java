package org.liquigraph.core.model;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum PreconditionErrorPolicy {

    CONTINUE, MARK_AS_EXECUTED, FAIL;
}
