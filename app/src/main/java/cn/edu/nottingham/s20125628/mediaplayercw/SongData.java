package cn.edu.nottingham.s20125628.mediaplayercw;

import java.io.Serializable;
// So can pass model to other activity,keep an instance of each music objects
public class SongData implements Serializable {
    String path;
    String title;
    String duration;
    public SongData(String path, String title, String duration) {
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
