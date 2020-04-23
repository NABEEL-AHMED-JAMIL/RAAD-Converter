package com.raad.converter.util;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class RaadUtil {

    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println(
                    String.format("<p>%s <a href=\"%s\"> %s</a></p>",
                            fileEntry.getName(), fileEntry.getName(),fileEntry.getName()));
            }
        }
    }
}
