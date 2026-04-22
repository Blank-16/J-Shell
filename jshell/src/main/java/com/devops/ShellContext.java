package com.devops;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShellContext {

    private File currentDirectory;
    private final List<String> history = new ArrayList<>();

    public ShellContext(File startDirectory) {
        this.currentDirectory = startDirectory.getAbsoluteFile();
    }

    public File currentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File dir) {
        this.currentDirectory = dir.getAbsoluteFile();
    }

    public void addHistory(String command) {
        history.add(command);
    }

    public List<String> history() {
        return Collections.unmodifiableList(history);
    }
}
