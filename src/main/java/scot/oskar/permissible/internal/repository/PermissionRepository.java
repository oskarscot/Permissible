package scot.oskar.permissible.internal.repository;

import scot.oskar.permissible.internal.entity.GroupMetadata;
import scot.oskar.permissible.internal.entity.GroupPermission;
import scot.oskar.permissible.internal.entity.PlayerGroup;
import scot.oskar.permissible.internal.entity.PlayerPermission;
import scot.oskar.volt.Result;
import scot.oskar.volt.Transaction;
import scot.oskar.volt.Volt;
import scot.oskar.volt.exception.VoltError;
import scot.oskar.volt.query.Query;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PermissionRepository {

    private final Volt volt;

    public PermissionRepository(Volt volt) {
        this.volt = volt;
    }

    public Set<String> loadPlayerPermissions(UUID playerUuid) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("player_uuid").eq(playerUuid);
            Result<List<PlayerPermission>, VoltError> result = tx.findAllBy(PlayerPermission.class, query);

            if (result.isSuccess()) {
                Set<String> permissions = new HashSet<>();
                for (PlayerPermission pp : result.getValue()) {
                    permissions.add(pp.getPermission());
                }
                return permissions;
            }
            return new HashSet<>();
        }
    }

    public void savePlayerPermissions(UUID playerUuid, Set<String> permissions) {
        try (Transaction tx = volt.beginTransaction()) {
            Query deleteQuery = Query.where("player_uuid").eq(playerUuid);
            Result<List<PlayerPermission>, VoltError> findResult = tx.findAllBy(PlayerPermission.class, deleteQuery);
            if (findResult.isSuccess()) {
                for (PlayerPermission pp : findResult.getValue()) {
                    tx.delete(pp);
                }
            }

            for (String permission : permissions) {
                PlayerPermission pp = new PlayerPermission(playerUuid, permission);
                tx.save(pp);
            }

            tx.commit();
        }
    }

    public void addPlayerPermission(UUID playerUuid, String permission) {
        try (Transaction tx = volt.beginTransaction()) {
            PlayerPermission pp = new PlayerPermission(playerUuid, permission);
            tx.save(pp);
            tx.commit();
        }
    }

    public void removePlayerPermission(UUID playerUuid, String permission) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("player_uuid").eq(playerUuid)
                    .and("permission").eq(permission);
          Result<PlayerPermission, VoltError> findByOne = tx.findOneBy(PlayerPermission.class, query);
          if (findByOne.isSuccess()) {
            PlayerPermission pp = findByOne.getValue();
            tx.delete(pp);
          }
          tx.commit();
        }
    }

    public Set<String> loadPlayerGroups(UUID playerUuid) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("player_uuid").eq(playerUuid);
            Result<List<PlayerGroup>, VoltError> result = tx.findAllBy(PlayerGroup.class, query);

            if (result.isSuccess()) {
                Set<String> groups = new HashSet<>();
                for (PlayerGroup pg : result.getValue()) {
                    groups.add(pg.getGroupName());
                }
                return groups;
            }
            return new HashSet<>();
        }
    }

    public void savePlayerGroups(UUID playerUuid, Set<String> groups) {
        for (String group : groups) {
            ensureGroupMetadata(group);
        }
        try (Transaction tx = volt.beginTransaction()) {
            Query deleteQuery = Query.where("player_uuid").eq(playerUuid);
            Result<List<PlayerGroup>, VoltError> findResult = tx.findAllBy(PlayerGroup.class, deleteQuery);
            if (findResult.isSuccess()) {
                for (PlayerGroup pg : findResult.getValue()) {
                    tx.delete(pg);
                }
            }

            for (String group : groups) {
                PlayerGroup pg = new PlayerGroup(playerUuid, group);
                tx.save(pg);
            }

            tx.commit();
        }
    }

    public void addPlayerToGroup(UUID playerUuid, String groupName) {
        ensureGroupMetadata(groupName);
        try (Transaction tx = volt.beginTransaction()) {
            PlayerGroup pg = new PlayerGroup(playerUuid, groupName);
            tx.save(pg);
            tx.commit();
        }
    }

    public void removePlayerFromGroup(UUID playerUuid, String groupName) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("player_uuid").eq(playerUuid)
                    .and("group_name").eq(groupName);
          Result<PlayerGroup, VoltError> oneBy = tx.findOneBy(PlayerGroup.class, query);
          if(oneBy.isSuccess()) {
            PlayerGroup pg = oneBy.getValue();
            tx.delete(pg);
          }
            tx.commit();
        }
    }

    public Set<String> loadGroupPermissions(String groupName) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("group_name").eq(groupName);
            Result<List<GroupPermission>, VoltError> result = tx.findAllBy(GroupPermission.class, query);

            if (result.isSuccess()) {
                Set<String> permissions = new HashSet<>();
                for (GroupPermission gp : result.getValue()) {
                    permissions.add(gp.getPermission());
                }
                return permissions;
            }
            return new HashSet<>();
        }
    }

    public void saveGroupPermissions(String groupName, Set<String> permissions) {
        ensureGroupMetadata(groupName);
        try (Transaction tx = volt.beginTransaction()) {
            Query deleteQuery = Query.where("group_name").eq(groupName);
            Result<List<GroupPermission>, VoltError> findResult = tx.findAllBy(GroupPermission.class, deleteQuery);
            if (findResult.isSuccess()) {
                for (GroupPermission gp : findResult.getValue()) {
                    tx.delete(gp);
                }
            }

            for (String permission : permissions) {
                GroupPermission gp = new GroupPermission(groupName, permission);
                tx.save(gp);
            }

            tx.commit();
        }
    }

    public void addGroupPermission(String groupName, String permission) {
        ensureGroupMetadata(groupName);
        try (Transaction tx = volt.beginTransaction()) {
            GroupPermission gp = new GroupPermission(groupName, permission);
            tx.save(gp);
            tx.commit();
        }
    }

    public void removeGroupPermission(String groupName, String permission) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("group_name").eq(groupName).and("permission").eq(permission);
          Result<GroupPermission, VoltError> oneBy = tx.findOneBy(GroupPermission.class, query);
          if (oneBy.isSuccess()) {
            GroupPermission pg = oneBy.getValue();
            tx.delete(pg);
          }
          tx.commit();
        }
    }

    public void deleteGroup(String groupName) {
        try (Transaction tx = volt.beginTransaction()) {
            Query permissionQuery = Query.where("group_name").eq(groupName);
            Result<List<GroupPermission>, VoltError> permissionsResult =
                tx.findAllBy(GroupPermission.class, permissionQuery);
            if (permissionsResult.isSuccess()) {
                for (GroupPermission permission : permissionsResult.getValue()) {
                    tx.delete(permission);
                }
            }
            Result<GroupMetadata, VoltError> metadataResult =
                tx.findOneBy(GroupMetadata.class, permissionQuery);
            if (metadataResult.isSuccess()) {
                tx.delete(metadataResult.getValue());
            }
            tx.commit();
        }
    }

    public int getGroupWeight(String groupName) {
        GroupMetadata metadata = getGroupMetadata(groupName);
        return metadata == null ? 0 : metadata.getWeight();
    }

    public Set<String> getAllGroups() {
        Set<String> groups = new HashSet<>();
        try (Transaction tx = volt.beginTransaction()) {
            Result<List<GroupMetadata>, VoltError> metadataResult = tx.findAll(GroupMetadata.class);
            if (metadataResult.isSuccess()) {
                for (GroupMetadata metadata : metadataResult.getValue()) {
                    groups.add(metadata.getGroupName());
                }
            }
            Result<List<GroupPermission>, VoltError> permissionResult = tx.findAll(GroupPermission.class);
            if (permissionResult.isSuccess()) {
                for (GroupPermission permission : permissionResult.getValue()) {
                    groups.add(permission.getGroupName());
                }
            }
            Result<List<PlayerGroup>, VoltError> playerGroupResult = tx.findAll(PlayerGroup.class);
            if (playerGroupResult.isSuccess()) {
                for (PlayerGroup playerGroup : playerGroupResult.getValue()) {
                    groups.add(playerGroup.getGroupName());
                }
            }
        }
        for (String group : groups) {
            ensureGroupMetadata(group);
        }
        return groups;
    }

    public boolean ensureGroupExists(String groupName) {
        if (getGroupMetadata(groupName) != null || !loadGroupPermissions(groupName).isEmpty()) {
            ensureGroupMetadata(groupName);
            return false;
        }
        ensureGroupMetadata(groupName);
        return true;
    }

    public Set<UUID> getPlayersInGroup(String groupName) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("group_name").eq(groupName);
            Result<List<PlayerGroup>, VoltError> result = tx.findAllBy(PlayerGroup.class, query);

            if (result.isSuccess()) {
                Set<UUID> players = new HashSet<>();
                for (PlayerGroup pg : result.getValue()) {
                    players.add(pg.getPlayerUuid());
                }
                return players;
            }
            return new HashSet<>();
        }
    }

    public GroupMetadata getGroupMetadata(String groupName) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("group_name").eq(groupName);
            Result<GroupMetadata, VoltError> result = tx.findOneBy(GroupMetadata.class, query);
            if (result.isSuccess()) {
                return result.getValue();
            }
            return null;
        }
    }

    public Map<String, GroupMetadata> getAllGroupMetadata() {
        try (Transaction tx = volt.beginTransaction()) {
            Result<List<GroupMetadata>, VoltError> result = tx.findAll(GroupMetadata.class);
            Map<String, GroupMetadata> metadata = new HashMap<>();
            if (result.isSuccess()) {
                for (GroupMetadata entry : result.getValue()) {
                    metadata.put(entry.getGroupName(), entry);
                }
            }
            return metadata;
        }
    }

    public void saveGroupMetadata(
        String groupName,
        int weight,
        String prefix,
        String suffix,
        String displayName) {
        try (Transaction tx = volt.beginTransaction()) {
            Query query = Query.where("group_name").eq(groupName);
            Result<GroupMetadata, VoltError> result = tx.findOneBy(GroupMetadata.class, query);
            GroupMetadata metadata;
            if (result.isSuccess()) {
                metadata = result.getValue();
                metadata.setWeight(weight);
                metadata.setPrefix(prefix);
                metadata.setSuffix(suffix);
                metadata.setDisplayName(displayName);
            } else {
                metadata = new GroupMetadata(groupName, weight, prefix, suffix, displayName);
            }
            tx.save(metadata);
            tx.commit();
        }
    }

    public GroupMetadata ensureGroupMetadata(String groupName) {
        GroupMetadata metadata = getGroupMetadata(groupName);
        if (metadata != null) {
            return metadata;
        }
        saveGroupMetadata(groupName, 0, null, null, null);
        return getGroupMetadata(groupName);
    }

    public GroupMetadata updateGroupMetadata(String groupName, int weight, String prefix, String suffix, String displayName) {
        saveGroupMetadata(groupName, weight, prefix, suffix, displayName);
        return getGroupMetadata(groupName);
    }

    public String getPrimaryGroupForPlayer(UUID playerUuid) {
        Set<String> groups = loadPlayerGroups(playerUuid);
        if (groups.isEmpty()) {
            return null;
        }
        Map<String, GroupMetadata> metadata = getAllGroupMetadata();
        String primaryGroup = null;
        int highestWeight = Integer.MIN_VALUE;
        for (String group : groups) {
            GroupMetadata groupMetadata = metadata.get(group);
            int weight = groupMetadata == null ? 0 : groupMetadata.getWeight();
            if (weight > highestWeight) {
                highestWeight = weight;
                primaryGroup = group;
            } else if (weight == highestWeight && primaryGroup != null
                && group.compareToIgnoreCase(primaryGroup) > 0) {
                primaryGroup = group;
            }
        }
        return primaryGroup;
    }
}
