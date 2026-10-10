package com.permadeath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.WorldSavePath;

/** Sistema de clanes del servidor. Todos los nuevos miembros entran con rango Prueba. */
public final class ClanManager {
 private ClanManager(){}
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static final Map<String,Clan> CLANS=new LinkedHashMap<>();
 private static MinecraftServer server;
 private static final String[] COLORS={"WHITE","GRAY","DARK_GRAY","RED","DARK_RED","GOLD","YELLOW","GREEN","DARK_GREEN","AQUA","DARK_AQUA","BLUE","DARK_BLUE","LIGHT_PURPLE","DARK_PURPLE"};
 public static final class Member { public String name; public String rank="Prueba"; Member(){} Member(String n,String r){name=n;rank=r;} }
 public static final class Clan { public String name, initials, color, leader; public List<Member> members=new ArrayList<>(); public Clan(){} }
 public static void init(MinecraftServer s){server=s; load(); syncTeams();}
 private static Path file(){return server.getSavePath(WorldSavePath.ROOT).resolve("permadeath-clans.json");}
 private static void load(){try{Path p=file();if(Files.exists(p)){Clan[] a=GSON.fromJson(Files.readString(p),Clan[].class);CLANS.clear();if(a!=null)for(Clan c:a)CLANS.put(c.name.toLowerCase(Locale.ROOT),c);}}catch(Exception e){System.err.println("[permadeath] No se pudieron cargar clanes: "+e);}}
 private static void save(){try{Files.writeString(file(),GSON.toJson(CLANS.values()),StandardCharsets.UTF_8);}catch(IOException e){System.err.println("[permadeath] No se pudieron guardar clanes: "+e);}}
 private static Clan clanOf(String player){for(Clan c:CLANS.values())for(Member m:c.members)if(m.name.equalsIgnoreCase(player))return c;return null;}
 private static Member member(Clan c,String name){for(Member m:c.members)if(m.name.equalsIgnoreCase(name))return m;return null;}
 private static boolean leader(Clan c,ServerPlayerEntity p){return c.leader.equalsIgnoreCase(p.getGameProfile().getName());}
 public static void handle(ServerPlayerEntity p,String action,String raw){String value=raw==null?"":raw.trim();String[] parts=value.split("\\|",-1);String name=p.getGameProfile().getName();Clan mine=clanOf(name);String msg="Acción de clan no válida.";
  switch(action){
   case "status" -> msg=mine==null?"No pertenecés a ningún clan.":"Clan: "+mine.name+" | Iniciales: "+mine.initials+" | Color: "+mine.color+" | Rango: "+member(mine,name).rank+" | Miembros: "+mine.members.size();
   case "create" -> {String cn=parts.length>0?parts[0].trim():"";String ini=parts.length>1?parts[1].trim():"";String color=parts.length>2?parts[2].trim().toUpperCase(Locale.ROOT):"AQUA";if(mine!=null)msg="Primero tenés que salir de tu clan.";else if(cn.length()<3||cn.length()>20||ini.isBlank()||ini.length()>4||!cn.matches("[\\p{L}0-9 _-]+"))msg="Nombre de 3-20 caracteres e iniciales de 1-4; sin símbolos raros.";else if(CLANS.containsKey(cn.toLowerCase(Locale.ROOT)))msg="Ya existe un clan con ese nombre.";else{Clan c=new Clan();c.name=cn;c.initials=ini.toUpperCase(Locale.ROOT);c.color=Arrays.asList(COLORS).contains(color)?color:"AQUA";c.leader=name;c.members.add(new Member(name,"Líder"));CLANS.put(cn.toLowerCase(Locale.ROOT),c);syncTeams();save();msg="Creaste el clan "+cn+". Sos el líder.";}}
   case "join" -> {Clan c=CLANS.get(value.toLowerCase(Locale.ROOT));if(mine!=null)msg="Ya pertenecés a un clan.";else if(c==null)msg="No existe ese clan.";else{c.members.add(new Member(name,"Prueba"));syncTeams();save();msg="Te uniste a "+c.name+" como Prueba.";}}
   case "leave" -> {if(mine==null)msg="No estás en un clan.";else if(leader(mine,p)&&mine.members.size()>1)msg="Como líder, transferí el liderazgo o eliminá el clan antes de salir.";else{boolean wasLeader=leader(mine,p);mine.members.removeIf(m->m.name.equalsIgnoreCase(name));if(mine.members.isEmpty())CLANS.remove(mine.name.toLowerCase(Locale.ROOT));else if(wasLeader)mine.leader=mine.members.get(0).name;syncTeams();save();msg="Saliste del clan.";}}
   case "invite" -> {if(mine==null||!leader(mine,p))msg="Solo el líder puede invitar miembros.";else if(server.getPlayerManager().getPlayer(value)==null)msg="Ese jugador debe estar conectado para esta invitación.";else{ServerPlayerEntity target=server.getPlayerManager().getPlayer(value);if(clanOf(value)!=null)msg="Ese jugador ya está en un clan.";else{mine.members.add(new Member(value,"Prueba"));syncTeams();save();target.sendMessage(Text.literal("Te invitaron al clan "+mine.name+" y entraste como Prueba."),false);msg="Invitaste a "+value+" como Prueba.";}}}
   case "remove" -> {if(mine==null||!leader(mine,p))msg="Solo el líder puede eliminar miembros.";else if(value.equalsIgnoreCase(mine.leader))msg="No podés eliminarte como líder.";else if(member(mine,value)==null)msg="Ese jugador no pertenece al clan.";else{mine.members.removeIf(m->m.name.equalsIgnoreCase(value));syncTeams();save();msg="Miembro eliminado: "+value;}}
   case "promote" -> {if(mine==null||!leader(mine,p))msg="Solo el líder puede ascender miembros.";else{Member m=member(mine,value);if(m==null)msg="Ese jugador no pertenece al clan.";else{m.rank=switch(m.rank){case "Prueba"->"Oficial";case "Oficial"->"Colíder";default->m.rank;};syncTeams();save();msg=value+" ahora es "+m.rank+".";}}}
   case "delete" -> {if(mine==null||!leader(mine,p))msg="Solo el líder puede eliminar el clan.";else{String n=mine.name;CLANS.remove(n.toLowerCase(Locale.ROOT));syncTeams();save();msg="Clan "+n+" eliminado.";}}
   case "color" -> {if(mine==null||!leader(mine,p))msg="Solo el líder puede cambiar el color.";else if(!Arrays.asList(COLORS).contains(value.toUpperCase(Locale.ROOT)))msg="Color no válido.";else{mine.color=value.toUpperCase(Locale.ROOT);syncTeams();save();msg="Color actualizado.";}}
   default -> {}
  }
  p.sendMessage(Text.literal("[Clanes] "+msg),false);
 }
 private static void syncTeams(){if(server==null)return;var sb=server.getScoreboard();for(Team oldTeam:new ArrayList<>(sb.getTeams())){if(oldTeam.getName().startsWith("pm_"))sb.removeTeam(oldTeam);}for(Clan c:CLANS.values()){String teamName="pm_"+Integer.toHexString(c.name.toLowerCase(Locale.ROOT).hashCode());Team t=sb.getTeam(teamName);if(t==null)t=sb.addTeam(teamName);t.setPrefix(Text.literal("["+c.initials+"] ").formatted(color(c.color)));t.setColor(color(c.color));for(Member m:c.members){Team old=sb.getPlayerTeam(m.name);if(old!=null&&old!=t)sb.removePlayerFromTeam(m.name,old);if(sb.getPlayerTeam(m.name)!=t)sb.addPlayerToTeam(m.name,t);}}}
 private static Formatting color(String s){try{return Formatting.valueOf(s);}catch(Exception e){return Formatting.AQUA;}}
}
