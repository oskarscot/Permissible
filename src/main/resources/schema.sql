CREATE TABLE IF NOT EXISTS player_permissions (
    id BIGSERIAL PRIMARY KEY,
    player_uuid UUID NOT NULL,
    permission VARCHAR(255) NOT NULL,
    UNIQUE(player_uuid, permission)
);

CREATE INDEX IF NOT EXISTS idx_player_permissions_uuid ON player_permissions(player_uuid);
CREATE INDEX IF NOT EXISTS idx_player_permissions_permission ON player_permissions(permission);

CREATE TABLE IF NOT EXISTS player_groups (
    id BIGSERIAL PRIMARY KEY,
    player_uuid UUID NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    UNIQUE(player_uuid, group_name)
);

CREATE INDEX IF NOT EXISTS idx_player_groups_uuid ON player_groups(player_uuid);
CREATE INDEX IF NOT EXISTS idx_player_groups_name ON player_groups(group_name);

CREATE TABLE IF NOT EXISTS group_permissions (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(255) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    UNIQUE(group_name, permission)
);

CREATE INDEX IF NOT EXISTS idx_group_permissions_name ON group_permissions(group_name);
CREATE INDEX IF NOT EXISTS idx_group_permissions_permission ON group_permissions(permission);

CREATE TABLE IF NOT EXISTS group_metadata (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(255) NOT NULL UNIQUE,
    weight INT NOT NULL DEFAULT 0,
    prefix VARCHAR(255),
    suffix VARCHAR(255),
    display_name VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_group_metadata_name ON group_metadata(group_name);
