<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.liquigraph</groupId>
    <artifactId>run-mojo-usage</artifactId>
    <version>0.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <build>
        <plugins>
            <plugin>
                <artifactId>liquigraph-maven-plugin</artifactId>
                <configuration>
                    <changelog>changelog.xml</changelog>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
