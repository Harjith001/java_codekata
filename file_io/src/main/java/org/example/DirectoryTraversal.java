package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DirectoryTraversal {

    private static final Logger LOG = LogManager.getLogger(DirectoryTraversal.class);

    public boolean search_in_file(String path, String content) {
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Pattern p = Pattern.compile(Pattern.quote(content));
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    br.close();
                    LOG.info("File contains {} with path {}", content, path);
                    return true;
                }
            }
            br.close();
            return false;
        } catch (Exception e) {
            LOG.error(e);
            return false;
        }
    }

    public List<String> search_content(String start, String content) {
        List<String> matches = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        queue.add(new File(start));
        while (!queue.isEmpty()) {
            File fileDir = queue.poll();

            File[] fileList = fileDir.listFiles();
            if (fileList != null) {
                for (File fd : fileList) {
                    if (fd.isDirectory()) {
                        queue.add(fd);
                    } else {
                        if(search_in_file(fd.getAbsolutePath(), content)){
                            matches.add(fd.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        String start = "/home/harjith/IdeaProjects/file_io/src";
        DirectoryTraversal dt = new DirectoryTraversal();

        List<String> matches = dt.search_content(start, "Ben 10");
        for(String match : matches){
            System.out.println(match);
        }
    }
}
