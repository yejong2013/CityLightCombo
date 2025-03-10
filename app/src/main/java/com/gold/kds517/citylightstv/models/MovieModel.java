package com.gold.kds517.citylightstv.models;

import java.io.Serializable;

public class MovieModel implements Serializable {
    private String num;
    private String name;
    private String stream_type;
    private String stream_id;
    private String stream_icon;
    private String extension;
    private String type;

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    private String category_id;
    private double rating;

    public boolean isIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }

    private boolean is_favorite;
    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    private String added;
    public String getNum(){
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStream_type() {
        return stream_type;
    }

    public void setStream_type(String stream_type) {
        this.stream_type = stream_type;
    }

    public String getStream_id() {
        return stream_id;
    }

    public void setStream_id(String stream_id) {
        this.stream_id = stream_id;
    }

    public String getStream_icon() {
        return stream_icon;
    }

    public void setStream_icon(String stream_icon) {
        this.stream_icon = stream_icon;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
