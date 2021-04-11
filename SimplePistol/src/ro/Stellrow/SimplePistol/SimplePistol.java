package ro.Stellrow.SimplePistol;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class SimplePistol extends JavaPlugin implements Listener {
    //Gun
    private Material gun;
    private Material ammo;
    private ItemStack ammoItem;
    private int cooldownTime;
    private double damage;
    private int distance;
    private Particle particle;
    //Sound
    private boolean playSound;
    private Sound sound;
    private float volume;
    private float pitch;
    //Cooldown
    private Set<UUID> isCooldown = new HashSet<>();


    public void onEnable(){
        loadConfig();
        loadValues();
        loadSound();
        getServer().getPluginManager().registerEvents(this,this);
        getCommand("simplepistol").setExecutor(new SimplePistolCommand(this));

    }
    private void loadConfig(){
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
    private void loadValues(){
        try{
            gun = Material.valueOf(getConfig().getString("General.gunType"));
            ammo = Material.valueOf(getConfig().getString("General.ammoType"));
            ammoItem = new ItemStack(ammo);
            particle = Particle.valueOf(getConfig().getString("General.particle"));
        }catch (IllegalArgumentException ex){
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&c[SimplePistol]The gun type/ammo type or particle type could not be found!"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&c[SimplePistol]The plugin will use the default values!"));
            gun = Material.WOODEN_HOE;
            ammo = Material.IRON_NUGGET;
            particle = Particle.CRIT_MAGIC;
            ammoItem = new ItemStack(ammo);
        }
        cooldownTime = getConfig().getInt("General.shotCooldownTicks",14);
        damage = getConfig().getDouble("General.damage",18.0);
        distance = getConfig().getInt("General.distanceInBlocks",10);
    }
    private void loadSound(){
        try{
            sound = Sound.valueOf(getConfig().getString("Sound.soundToPlay"));
        }catch (IllegalArgumentException ex){
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&c[SimplePistol]The sound type could not be found!"));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&c[SimplePistol]The plugin will use the default values!"));
            sound = Sound.ENTITY_GENERIC_EXPLODE;
        }
        playSound = getConfig().getBoolean("Sound.play",false);
        volume = (float)getConfig().getDouble("Sound.volume",0.3);
        pitch = (float)getConfig().getDouble("Sound.pitch",3.5);
    }

    @EventHandler
    public void onShoot(PlayerInteractEvent event){
        if (event.getItem()!=null&&event.getItem().getType()==gun){
            if (event.getAction()== Action.RIGHT_CLICK_BLOCK||event.getAction()==Action.RIGHT_CLICK_AIR){
                Player player = event.getPlayer();
                if (!isCooldown.contains(player.getUniqueId())){
                    if (player.getInventory().containsAtLeast(ammoItem,1)){
                        player.getInventory().removeItem(ammoItem);
                        shootGun(player);
                        addCooldown(player);
                    }
                }
            }
        }
    }
    private void shootGun(Player player){
        if (playSound){
            player.getWorld().playSound(player.getLocation(),sound,volume,pitch);
        }
        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation().add(player.getEyeLocation().getDirection()),player.getEyeLocation().getDirection(),distance);
        if (result!=null){
            Entity hit = result.getHitEntity();
            if (hit!=null) {
                if (hit instanceof LivingEntity) {
                    ((LivingEntity) hit).damage(damage);
                    spawnParticleLine(player.getLocation(), particle, hit.getLocation());
                }
            }else{
                spawnParticleLine(player.getLocation(),particle,result.getHitBlock().getLocation());
            }
        }else{
            spawnParticleLine(player.getLocation(),particle);
        }
    }
    private void addCooldown(Player player){
        isCooldown.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(this,()->{
            isCooldown.remove(player.getUniqueId());
        },cooldownTime);
    }
    private void spawnParticleLine(Location startingLocation, Particle toUse,Location toReach){
        World world = startingLocation.getWorld();
        //Add offset so particle line starts higher ~eye level
        Location loc = startingLocation.add(0,1,0);
        double distance = startingLocation.distance(toReach);
        double step = 0.1D;
        Vector line = startingLocation.getDirection();
        for (double d = 0; d <=  distance; d += step) {
            loc.add(line);
            world.spawnParticle(toUse, loc, 0);
        }
    }
    private void spawnParticleLine(Location startingLocation, Particle toUse){
        World world = startingLocation.getWorld();
        //Add offset so particle line starts higher ~eye level
        Location loc = startingLocation.add(0,1,0);
        double step = 0.1D;
        Vector line = startingLocation.getDirection();
        for (double d = 0; d <=  distance; d += step) {
            loc.add(line);
            world.spawnParticle(toUse, loc, 0);
        }
    }
    public void reloadData(){
        reloadConfig();
        loadValues();
        loadSound();
    }

}
