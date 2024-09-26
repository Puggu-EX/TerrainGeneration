package com.example.terraingeneration;

import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.util.Random;
import java.util.Vector;

public final class TerrainGeneration extends JavaPlugin implements Listener {
    private long lastTimeUsed = 0;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        if (getDataFolder().exists()) {
            TileHandler.loadTiles(getDataFolder(), "tiles.json");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent event) {
        // Make sure lastTimeUsed < 5 second
        long currentTime = System.currentTimeMillis();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.COMPASS && (currentTime - lastTimeUsed) > 1000L) {
            RayTraceResult ray = player.rayTraceBlocks(100);
            if (ray != null){
                Block hitBlock = ray.getHitBlock();
                if (hitBlock != null){
                    generateTerrain(hitBlock.getLocation().add(0, 1, 0));
                }
            }
            lastTimeUsed = currentTime;
        }
    }

    int tileSize = 7;
    int mapHeight = 101;
    int mapWidth = 101;
    int pathLength = 100;

    public void generateTerrain(Location location) {
        String[] keys = TileHandler.getTiles().keySet().toArray(new String[0]);

        // Worst case (most stone) is every block is a dead-end which shouldn't happen (34/49 blocks are stone)
        // Best case (fewer stone) is every block is ____ (28/49 blocks are stone)
        Vector<Location> stoneLocations = new Vector<>(pathLength * 30);

        int[] path = generatePath(pathLength);

        JsonObject tile = TileHandler.getTile(keys[0]);
//        placeTile(location.clone(), JSONHandler.getLayout(tile));
//        placeTile(location.clone().add(0, 0, tileSize), JSONHandler.getLayout(tile));

        Location loc = location.clone();
        placeTile(loc.clone(), TileHandler.getLayout(tile));
//        loc.getBlock().setType(Material.YELLOW_WOOL);
        for (int i = 1; i < path.length; i++) {
            int prevDirection = path[i - 1];
            int nextDirection = path[i];

            int determined = determineTile(prevDirection, nextDirection);
            if (determined == -1) {
                System.out.println("Issue");
                return;
            }

            if (prevDirection == 0) {
                loc.add(tileSize, 0, 0);
            } else if (prevDirection == 1) {
                loc.add(0, 0, tileSize);
            } else if (prevDirection == 2) {
                loc.add(-tileSize, 0, 0);
            } else if (prevDirection == 3) {
                loc.add(0, 0, -tileSize);
            }

            tile = TileHandler.getTile(keys[determined]);
            Vector<Location> locations = placeTile(loc.clone(), TileHandler.getLayout(tile));
            stoneLocations.addAll(locations);
        }
        // Go to all locations and check to see if stone still exists
        // If so build wall
        for (Location stoneLocation : stoneLocations) {
            if (stoneLocation.clone().getBlock().getType() == Material.STONE) {
                for (int k = 0; k < 4; k++) {
                    stoneLocation.add(0, 1, 0).getBlock().setType(Material.STONE);
                }
            }
        }
    }

    public int[][] generateMap(){
        int[][] map = new int[mapHeight][mapWidth];
        for (int x = 0; x < mapHeight; x++) {
            for (int y = 0; y < mapWidth; y++) {

            }
        }

        return map;
    }

    public int[] generatePath(int length) {
        Random rand = new Random();
        int[] pathDirection = new int[length];
        pathDirection[0] = 0; // Always start north

        for (int i = 1; i < length; i++) {
            int prevDirection = pathDirection[i - 1];
            int nextDirection = rand.nextInt(4);

            // Avoid 180s
            while (Math.abs(nextDirection - prevDirection) == 2) {
                nextDirection = rand.nextInt(4);
            }

            pathDirection[i] = nextDirection;
        }

//        System.out.print("Path: ");
//        for (int direction : pathDirection) {
//            System.out.print(direction + " ");
//        }
        return pathDirection;
    }

    public Vector<Location> placeTile(Location location, String[][] layout) {
        location = location.add(tileSize/2, -1, -tileSize/2);
//        System.out.println("Placing at: " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());

        Vector<Location> stoneLocations = new Vector<>(tileSize);

        for (int i = 0; i < tileSize; i++) {
            for (int j = 0; j < tileSize; j++) {
                Material material = Material.getMaterial(layout[i][j]);
                Block currentBlock = location.getBlock();

                // Don't replace black wool with another block type
                if (material != null && currentBlock.getType() != Material.BLACK_WOOL) {
                    currentBlock.setType(material);
                    // If material is STONE make a wall
                    if (material == Material.STONE) {
                        stoneLocations.add(currentBlock.getLocation().clone());
                    }
                }
                location.add(0, 0, 1);
            }
            location.add(-1, 0, -tileSize);
        }

        stoneLocations.trimToSize();
        return stoneLocations;

    }

    public int determineTile(int prevDirection, int nextDirection) {
        // Tile types:
        // 0: North-South, 1: East-West, 2: North-East, 3: North-West, 4: South-East, 5: South-West

        if ((prevDirection == 0 && nextDirection == 2) || (prevDirection == 2 && nextDirection == 0) || (prevDirection == 0 && nextDirection == 0) || (prevDirection == 2 && nextDirection == 2)) {
            return 0; // North-South tile
        }
        if ((prevDirection == 1 && nextDirection == 3) || (prevDirection == 3 && nextDirection == 1) || (prevDirection == 3 && nextDirection == 3) || (prevDirection == 1 && nextDirection == 1)) {
            return 1; // East-West tile
        }
        if ((prevDirection == 2 && nextDirection == 1) || (prevDirection == 3 && nextDirection == 0)) {
            return 2; // North-East tile
        }
        if ((prevDirection == 0 && nextDirection == 1) || (prevDirection == 3 && nextDirection == 2)) {
            return 3; // South-East tile
        }
        if ((prevDirection == 2 && nextDirection == 3) || (prevDirection == 1 && nextDirection == 0)) {
            return 4; // north-west tile
        }
        if ((prevDirection == 0 && nextDirection == 3) || (prevDirection == 1 && nextDirection == 2)) {
            return 5; // South-West tile
        }

        System.out.println(prevDirection);
        System.out.println(nextDirection);
        return -1; // Error or undefined tile
    }
}

//        System.out.println("Getting tile: " + keys[index]);
// South = 1, East = 1, North = -1, West = -1
//        Vector direction = location.getDirection().setY(0).normalize();
//        int x = (int) Math.round(direction.getX());
//        int z = (int) Math.round(direction.getZ());
