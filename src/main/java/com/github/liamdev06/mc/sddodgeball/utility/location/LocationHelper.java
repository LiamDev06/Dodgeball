package com.github.liamdev06.mc.sddodgeball.utility.location;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility helper class when working with Bukkit locations.
 */
public class LocationHelper {

    /**
     * Write a bukkit location to a string to be able to save it.
     *
     * @param location The location to write.
     * @return A string location in the format of "X,Y,Z,YAW,PITCH" for example "23.2,-5,132,4.4,23.7".
     */
    public static String writeLocation(@Nullable Location location) {
        // Location is null and cannot be parsed
        if (location == null) {
            return "";
        }

        // The world is null and cannot be parsed
        World world = location.getWorld();
        if (world == null) {
            return "";
        }

        // Return the location in the specific format
        return world.getName() + ","  + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    /**
     * Parses a location from the location string format used in {@link LocationHelper#writeLocation(Location)} to a bukkit location.
     *
     * @param inputLocation The string with the correct format to parse.
     * @return A bukkit location from the {@param inputLocation} string.
     */
    public static Location parseLocation(@NonNull String inputLocation) throws InvalidLocationParseException {
        String[] args = inputLocation.split("\\,");
        if (inputLocation.equals("") || args.length == 0) {
            throw new InvalidLocationParseException("The input location does not contain any values (arguments)!");
        }

        // Get the world
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            throw new InvalidLocationParseException("The world with name " + args[0] + " is not a bukkit world!");
        }

        // Read X Y Z
        if (args.length == 4) {
            return new Location(
                    world,
                    Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]),
                    Double.parseDouble(args[3])
            );
        }

        // Read X Y Z YAW PITCH
        return new Location(
                world,
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2]),
                Double.parseDouble(args[3]),
                Float.parseFloat(args[4]),
                Float.parseFloat(args[5])
        );
    }

    /**
     * Takes the world and coordinates of a location and creates a string
     * that looks like "World, X, Y, Z" for nicer viewing.
     *
     * @param location The location itself to turn into a nicer string.
     * @param mainColor The main color of the message that is used for the "," to make it blend into the message.
     * @param variableColor The color that is used for the actual coordinates in the string.
     * @see LocationHelper#simpleFriendlyLocationText(Location, ChatColor, ChatColor) for the second part of the string.
     * @return A formatted and nicer version to display location coordinates.
     */
    public static String friendlyLocationText(@NonNull Location location, @NonNull ChatColor mainColor, @NonNull ChatColor variableColor) {
        World world = location.getWorld();
        if (world == null) {
            return simpleFriendlyLocationText(location, mainColor, variableColor);
        }

        return variableColor + world.getName() + mainColor + ", " +
                simpleFriendlyLocationText(location, mainColor, variableColor);
    }

    /**
     * Takes the world and coordinates of a location and creates a string
     * that looks like "World, X, Y, Z, Yaw, Pitch" for nicer viewing.
     *
     * @param location The location itself to turn into a nicer string.
     * @param mainColor The main color of the message that is used for the "," to make it blend into the message.
     * @param variableColor The color that is used for the actual coordinates in the string.
     * @see LocationHelper#friendlyLocationText(Location, ChatColor, ChatColor) for the first part of the string.
     * @return A formatted and nicer version to display location coordinates.
     */
    public static String fullFriendlyLocationText(@NonNull Location location, @NonNull ChatColor mainColor, @NonNull ChatColor variableColor) {
        return friendlyLocationText(location, mainColor, variableColor) + variableColor +
                location.getYaw() + mainColor + ", " + variableColor +
                location.getPitch() + mainColor;
    }

    /**
     * Takes the coordinates of a location and creates a string
     * that looks like "X, Y, Z" for nicer viewing.
     *
     * @param location The location itself to turn into a nicer string.
     * @param mainColor The main color of the message that is used for the "," to make it blend into the message.
     * @param variableColor The color that is used for the actual coordinates in the string.
     * @return A formatted and nicer version to display location coordinates.
     */
    public static String simpleFriendlyLocationText(@NonNull Location location, @NonNull ChatColor mainColor, @NonNull ChatColor variableColor) {
        return "" + variableColor +
                location.getBlockX() + mainColor + ", " + variableColor +
                location.getBlockY() + mainColor + ", " + variableColor +
                location.getBlockZ() + mainColor;
    }
}