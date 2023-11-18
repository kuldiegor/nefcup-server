/*
    Copyright 2023 Dmitrij Kulabuhov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package org.nefcup.server.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IgnoreService {
    private final Set<String> patternSet;

    public IgnoreService(String patternsText) {
        this.patternSet = new HashSet<>(parsePatterns(patternsText));
        this.patternSet.add("ignore.nefcup");
        this.patternSet.add("clean_ignore.nefcup");
        this.patternSet.add("nefcup.sh");
    }

    public static List<String> parsePatterns(String patternsText) {
        if (patternsText==null){
            return new ArrayList<>();
        }
        return patternsText.lines()
                .filter(it -> !it.isBlank())
                .filter(it -> !it.startsWith("#"))
                .map(it -> it.replace("\\#","#"))
                .collect(Collectors.toList());
    }

    public boolean isIgnore(Path path){
        String fileName = path.getName(0).toString();
        if (patternSet.contains(fileName)){
            return true;
        }
        for (int i=1;i<path.getNameCount();i++){
            fileName += "/"+path.getName(i);
            if (patternSet.contains(fileName)){
                return true;
            }
        }
        return false;
    }


}
