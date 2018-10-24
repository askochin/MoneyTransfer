package com.dev.moneytransfer;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.net.URL;

import static com.google.common.io.Resources.getResource;

public class DbUtil {

    public static final String INTERNAL_SCRIPT = "internal";

    public static void initDb(Jdbi jdbi, String scriptFilePath) throws IOException {
        URL scriptUrl = scriptFilePath.equals(INTERNAL_SCRIPT) ? getResource("schema.sql") : new URL("file://" + scriptFilePath);
        String scriptContent = Resources.toString(scriptUrl, Charsets.UTF_8);
        jdbi.useHandle(handle -> handle.createScript(scriptContent).execute());
    }
}
