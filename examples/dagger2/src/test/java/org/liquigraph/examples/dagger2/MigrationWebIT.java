package org.liquigraph.examples.dagger2;

import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.examples.dagger2.Application;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.containsString;

public class MigrationWebIT {

    @Test
    public void testMigration() throws Exception {


        Application.main(null);

        get("/")
        .then()
        .assertThat()
        .body(containsString("Hello World!"));

    }
}
