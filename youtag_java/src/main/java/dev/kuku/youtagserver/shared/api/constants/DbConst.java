package dev.kuku.youtagserver.shared.api.constants;


public class DbConst {
    public static class Users {
        public static final String TABLE_NAME = "users";
        public static final String USERNAME = "name";
        public static final String THUMBNAIL_URL = "thumbnail_url";

    }

    public static class Videos {
        public static final String TABLE_NAME = "videos";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String THUMBNAIL_URL = "thumbnail_url";

    }

    public static class CommonColumn {
        public static final String ID = "id";
        public static final String UPDATED = "updated";
        public static final String USER_ID = "user_id";
        public static final String VIDEO_ID = "video_id";
        public static final String TAG = "tag";
    }

    public static class UserVideo {
        public static final String TABLE_NAME = "user_video";
    }


    public static class UserTag {
        public static final String TABLE_NAME = "user_tag";
    }

    public static class UserVideoTag {
        public static final String TABLE_NAME = "user_video_tag";
    }
}
