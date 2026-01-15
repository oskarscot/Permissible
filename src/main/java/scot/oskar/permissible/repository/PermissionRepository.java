package scot.oskar.permissible.repository;

import scot.oskar.permissible.entity.GroupPermission;
import scot.oskar.permissible.entity.PlayerGroup;
import scot.oskar.permissible.entity.PlayerPermission;
import scot.oskar.volt.Result;
import scot.oskar.volt.Transaction;
import scot.oskar.volt.Volt;
import scot.oskar.volt.exception.VoltError;
import scot.oskar.volt.query.Query;

import java.util.HashSet;
import java.util.List;
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

    public Set<String> getAllGroups() {
        try (Transaction tx = volt.beginTransaction()) {
            Result<List<GroupPermission>, VoltError> result = tx.findAll(GroupPermission.class);

            if (result.isSuccess()) {
                Set<String> groups = new HashSet<>();
                for (GroupPermission gp : result.getValue()) {
                    groups.add(gp.getGroupName());
                }
                return groups;
            }
            return new HashSet<>();
        }
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
}
