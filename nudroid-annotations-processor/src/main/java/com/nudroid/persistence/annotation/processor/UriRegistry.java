package com.nudroid.persistence.annotation.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class UriRegistry {

    List<Uri> uris = new ArrayList<Uri>();

    int addUri(Uri uri) {

        int existing = uris.indexOf(uri);
        
        if (existing == -1) {
            uris.add(uri);
        }
        
        return uris.indexOf(uri);
    }

    @Override
    public String toString() {
        return "UriRegistry [uris=" + uris + "]";
    }
    
    public List<Uri> getUniqueUris() {
        return Collections.unmodifiableList(uris);
    }
}