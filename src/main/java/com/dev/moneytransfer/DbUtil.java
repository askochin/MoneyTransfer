package com.dev.moneytransfer;

import org.jdbi.v3.core.Jdbi;
import spark.utils.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;

public class DbUtil {

    public static void initDb(Jdbi jdbi, String scriptFilePath) throws IOException {
        String scriptContent = IOUtils.toString(new FileInputStream(scriptFilePath));
        jdbi.useHandle(handle -> handle.createScript(scriptContent).execute());
    }
}
