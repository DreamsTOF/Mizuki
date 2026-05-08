-- =============================================
-- 歌曲信息表
-- =============================================

CREATE TABLE song (
                      id              UUID PRIMARY KEY,
                      name            VARCHAR(255) NOT NULL,
                      singer          VARCHAR(255),
                      album           VARCHAR(255),
                      language        VARCHAR(50),
                      jump_url        VARCHAR(500),
                      remark          TEXT,
                      status          VARCHAR(50),
                      rule            VARCHAR(50),
                      cover_url       VARCHAR(500),
                      total_clicks    INTEGER DEFAULT 0,
                      created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      created_by      UUID,
                      updated_by      UUID,
                      deleted_at      TIMESTAMP WITH TIME ZONE,
                      version         INTEGER NOT NULL DEFAULT 0,
                      styles          JSONB,
                      clips           JSONB
);

COMMENT ON TABLE song IS '歌曲信息表';

COMMENT ON COLUMN song.id IS 'ID';
COMMENT ON COLUMN song.name IS '歌曲名称';
COMMENT ON COLUMN song.singer IS '歌手名称';
COMMENT ON COLUMN song.album IS '专辑名称';
COMMENT ON COLUMN song.language IS '语言：中/英/日/韩/其他';
COMMENT ON COLUMN song.jump_url IS '第三方平台跳转链接';
COMMENT ON COLUMN song.remark IS '备注信息';
COMMENT ON COLUMN song.status IS '歌曲状态：AVAILABLE-可点, LEARNING-学习中, UNAVAILABLE-暂不可点';
COMMENT ON COLUMN song.rule IS '点歌规则';
COMMENT ON COLUMN song.cover_url IS '歌曲封面图URL';
COMMENT ON COLUMN song.total_clicks IS '总点击次数（冗余字段）';
COMMENT ON COLUMN song.created_at IS '创建时间';
COMMENT ON COLUMN song.updated_at IS '更新时间';
COMMENT ON COLUMN song.created_by IS '创建人 ID';
COMMENT ON COLUMN song.updated_by IS '更新人 ID';
COMMENT ON COLUMN song.deleted_at IS '软删除时间戳';
COMMENT ON COLUMN song.version IS '版本号，用于乐观锁';
COMMENT ON COLUMN song.styles IS '曲风';
COMMENT ON COLUMN song.clips IS '歌切';

CREATE INDEX idx_song_status ON song(status);
CREATE INDEX idx_song_singer ON song(singer);
CREATE INDEX idx_song_deleted_at ON song(deleted_at) WHERE deleted_at IS NULL;

-- =============================================
-- 歌单信息表
-- =============================================

CREATE TABLE playlist (
                          id              UUID PRIMARY KEY,
                          user_id         UUID NOT NULL,
                          name            VARCHAR(255) NOT NULL,
                          description     TEXT,
                          cover_url       VARCHAR(500),
                          status          VARCHAR(50) NOT NULL DEFAULT 'OTHER',
                          type            VARCHAR(50) NOT NULL DEFAULT 'OTHER',
                          created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          created_by      UUID,
                          updated_by      UUID,
                          deleted_at      TIMESTAMP WITH TIME ZONE,
                          version         INTEGER NOT NULL DEFAULT 0
);

COMMENT ON TABLE playlist IS '歌单信息表';

COMMENT ON COLUMN playlist.id IS 'ID';
COMMENT ON COLUMN playlist.user_id IS '所有者用户 ID';
COMMENT ON COLUMN playlist.name IS '歌单名称';
COMMENT ON COLUMN playlist.description IS '歌单描述';
COMMENT ON COLUMN playlist.cover_url IS '歌单封面图 URL';
COMMENT ON COLUMN playlist.status IS '歌单状态：1-草稿，2-公开，3-私有';
COMMENT ON COLUMN playlist.type IS '歌单类型：1-主播默认歌单，2-主播其他歌单';
COMMENT ON COLUMN playlist.created_at IS '创建时间';
COMMENT ON COLUMN playlist.updated_at IS '更新时间';
COMMENT ON COLUMN playlist.created_by IS '创建人 ID';
COMMENT ON COLUMN playlist.updated_by IS '更新人 ID';
COMMENT ON COLUMN playlist.deleted_at IS '软删除时间戳';
COMMENT ON COLUMN playlist.version IS '版本号，用于乐观锁';

CREATE INDEX idx_playlist_user_id ON playlist(user_id);
CREATE INDEX idx_playlist_status ON playlist(status);
CREATE INDEX idx_playlist_deleted_at ON playlist(deleted_at) WHERE deleted_at IS NULL;

-- =============================================
-- 标签信息表
-- =============================================

CREATE TABLE label (
                       id              UUID PRIMARY KEY,
                       name            VARCHAR(100) NOT NULL UNIQUE,
                       color           VARCHAR(20),
                       is_system       BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by      UUID,
                       deleted_at      TIMESTAMP WITH TIME ZONE,
                       version         INTEGER NOT NULL DEFAULT 0
);

COMMENT ON TABLE label IS '标签信息';

COMMENT ON COLUMN label.id IS 'ID';
COMMENT ON COLUMN label.name IS '标签名称';
COMMENT ON COLUMN label.color IS '标签颜色，如：#00BFFF 天青色';
COMMENT ON COLUMN label.is_system IS '是否为系统预设：true-系统预设，false-用户自定义';
COMMENT ON COLUMN label.created_at IS '创建时间';
COMMENT ON COLUMN label.updated_at IS '更新时间';
COMMENT ON COLUMN label.created_by IS '创建人 ID';
COMMENT ON COLUMN label.deleted_at IS '软删除时间戳';
COMMENT ON COLUMN label.version IS '版本号，用于乐观锁';

CREATE INDEX idx_label_name ON label(name);
CREATE INDEX idx_label_deleted_at ON label(deleted_at) WHERE deleted_at IS NULL;

-- =============================================
-- 歌单歌曲关联表
-- =============================================

CREATE TABLE playlist_song (
                               id              UUID PRIMARY KEY,
                               playlist_id     UUID NOT NULL,
                               song_id         UUID NOT NULL,
                               sort_order      INTEGER NOT NULL DEFAULT 0,
                               created_by      UUID,
                               created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE playlist_song IS '歌单歌曲关联表';

COMMENT ON COLUMN playlist_song.id IS 'ID';
COMMENT ON COLUMN playlist_song.playlist_id IS '歌单 ID';
COMMENT ON COLUMN playlist_song.song_id IS '歌曲 ID';
COMMENT ON COLUMN playlist_song.sort_order IS '在歌单中的排序';
COMMENT ON COLUMN playlist_song.created_by IS '添加人 ID';
COMMENT ON COLUMN playlist_song.created_at IS '添加时间';

CREATE INDEX idx_playlist_song_playlist_id ON playlist_song(playlist_id);
CREATE INDEX idx_playlist_song_song_id ON playlist_song(song_id);
CREATE UNIQUE INDEX uk_playlist_song ON playlist_song(playlist_id, song_id);

-- =============================================
-- 歌曲点击日志表
-- =============================================

CREATE TABLE song_click_log (
                                id              UUID PRIMARY KEY,
                                song_id         UUID NOT NULL,
                                user_id         UUID,
                                ip_address      VARCHAR(45),
                                user_agent      TEXT,
                                create_time     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                referer         VARCHAR(500)
);

COMMENT ON TABLE song_click_log IS '歌曲点击日志表';

COMMENT ON COLUMN song_click_log.id IS 'ID';
COMMENT ON COLUMN song_click_log.song_id IS '歌曲 ID';
COMMENT ON COLUMN song_click_log.user_id IS '点击用户 ID（未登录为 NULL）';
COMMENT ON COLUMN song_click_log.ip_address IS 'IP 地址';
COMMENT ON COLUMN song_click_log.user_agent IS '用户代理（浏览器/设备信息）';
COMMENT ON COLUMN song_click_log.create_time IS '创建时间';
COMMENT ON COLUMN song_click_log.referer IS '来源页面';

CREATE INDEX idx_song_click_log_song_id ON song_click_log(song_id);
CREATE INDEX idx_song_click_log_user_id ON song_click_log(user_id);
CREATE INDEX idx_song_click_log_create_time ON song_click_log(create_time);

-- =============================================
-- 歌曲点击统计表
-- =============================================

CREATE TABLE song_click_stat (
                                 id               UUID PRIMARY KEY,
                                 song_id          UUID NOT NULL UNIQUE,
                                 total_clicks     INTEGER NOT NULL DEFAULT 0,
                                 today_clicks     INTEGER NOT NULL DEFAULT 0,
                                 week_clicks      INTEGER NOT NULL DEFAULT 0,
                                 month_clicks     INTEGER NOT NULL DEFAULT 0,
                                 last_click_time  TIMESTAMP WITH TIME ZONE,
                                 update_time      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE song_click_stat IS '歌曲点击统计表';

COMMENT ON COLUMN song_click_stat.id IS 'ID';
COMMENT ON COLUMN song_click_stat.song_id IS '歌曲 ID';
COMMENT ON COLUMN song_click_stat.total_clicks IS '总有效点击次数';
COMMENT ON COLUMN song_click_stat.today_clicks IS '今日点击次数';
COMMENT ON COLUMN song_click_stat.week_clicks IS '本周点击次数';
COMMENT ON COLUMN song_click_stat.month_clicks IS '本月点击次数';
COMMENT ON COLUMN song_click_stat.last_click_time IS '最后点击时间';
COMMENT ON COLUMN song_click_stat.update_time IS '更新时间';

CREATE INDEX idx_song_click_stat_song_id ON song_click_stat(song_id);

-- =============================================
-- 歌曲标签关联表
-- =============================================

CREATE TABLE song_label (
                            id              UUID PRIMARY KEY,
                            song_id         UUID NOT NULL,
                            label_id        UUID NOT NULL,
                            created_by      UUID,
                            created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE song_label IS '歌曲标签关联表';

COMMENT ON COLUMN song_label.id IS 'ID';
COMMENT ON COLUMN song_label.song_id IS '歌曲 ID';
COMMENT ON COLUMN song_label.label_id IS '标签 ID';
COMMENT ON COLUMN song_label.created_by IS '创建人 ID';
COMMENT ON COLUMN song_label.created_at IS '创建时间';

CREATE INDEX idx_song_label_song_id ON song_label(song_id);
CREATE INDEX idx_song_label_label_id ON song_label(label_id);
CREATE UNIQUE INDEX uk_song_label ON song_label(song_id, label_id);

-- =============================================
-- 用户信息表
-- =============================================

CREATE TABLE "user" (
                        id                  UUID PRIMARY KEY,
                        username            VARCHAR(100) NOT NULL UNIQUE,
                        password            VARCHAR(255) NOT NULL,
                        nike_name           VARCHAR(100),
                        avatar_url          VARCHAR(500),
                        bio                 TEXT,
                        personal_space_url  VARCHAR(500),
                        live_room_url       VARCHAR(500),
                        last_login_time     TIMESTAMP WITH TIME ZONE,
                        last_login_ip       VARCHAR(45),
                        created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at          TIMESTAMP WITH TIME ZONE,
                        version             INTEGER NOT NULL DEFAULT 0,
                        slug                VARCHAR(200) UNIQUE
);

COMMENT ON TABLE "user" IS '用户信息表';

COMMENT ON COLUMN "user".id IS 'ID';
COMMENT ON COLUMN "user".username IS '用户名（唯一标识）';
COMMENT ON COLUMN "user".password IS '密码';
COMMENT ON COLUMN "user".nike_name IS '昵称';
COMMENT ON COLUMN "user".avatar_url IS '头像URL';
COMMENT ON COLUMN "user".bio IS '个人简介';
COMMENT ON COLUMN "user".personal_space_url IS '个人空间链接';
COMMENT ON COLUMN "user".live_room_url IS '直播间链接';
COMMENT ON COLUMN "user".last_login_time IS '最后登录时间';
COMMENT ON COLUMN "user".last_login_ip IS '最后登录IP';
COMMENT ON COLUMN "user".created_at IS '创建时间';
COMMENT ON COLUMN "user".updated_at IS '更新时间';
COMMENT ON COLUMN "user".deleted_at IS '软删除时间戳';
COMMENT ON COLUMN "user".version IS '版本号，用于乐观锁';
COMMENT ON COLUMN "user".slug IS '访问路径';

CREATE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_slug ON "user"(slug);
CREATE INDEX idx_user_deleted_at ON "user"(deleted_at) WHERE deleted_at IS NULL;