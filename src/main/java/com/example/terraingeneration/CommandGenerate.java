package com.example.terraingeneration;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Vector;

public class CommandGenerate implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        Vector<Integer> path = new Vector<>();

        // Format
        String regex = "^\\d+:[NEWSnews]$";

        // Parse Input
        for (String block: args){
            if (!block.matches(regex)){
                player.sendMessage("REGEX couldn't parse directions <distance>:<direction> where direction = NEW or S");
                return false;
            }

            // First part should be numbers
            int dis = Integer.parseInt(block.split(":")[0]);

            // Second part should be direction (NEWS)
            String dirAsString = block.split(":")[1];
            int dir;

            switch (dirAsString.toLowerCase()){
                case "n":
                    dir = 0;
                    break;
                case "e":
                    dir = 1;
                    break;
                case "s":
                    dir = 2;
                    break;
                case "w":
                    dir = 3;
                    break;
                default:
                    System.out.println("Something went wrong with parsing");
                    return false;
            }

            for (int i = 0; i < dis; i++){
                path.add(dir);
            }
        }
        player.sendMessage(path.toString());

        int[] pathAsArray = new int[path.size()];
        for (int i = 0; i < path.size(); i++){
            pathAsArray[i] = path.get(i);
        }

        Location startingLocation = player.getLocation().add(0, 0, 0);
        TerrainGeneration.generateTerrain(startingLocation, pathAsArray);

        return true;
    }
    public Vector<Integer> parseMessage(String[] args){
        Vector<Integer> returnVec = new Vector<>(args.length);

        return returnVec;
    }

}
