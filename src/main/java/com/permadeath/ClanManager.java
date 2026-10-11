package com.permadeath;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
public final class ClanManager {
 private ClanManager(){}
 private static final Gson G=new GsonBuilder().setPrettyPrinting().create();
 private static final Map<String,Clan> C=new LinkedHashMap<>();
 private static final Map<String,Invite> I=new HashMap<>();
 private static final Set<String> OWN=new HashSet<>();
 private static MinecraftServer server;
 private static final List<String> COLORS=List.of("WHITE","GRAY","DARK_GRAY","RED","DARK_RED","GOLD","YELLOW","GREEN","DARK_GREEN","AQUA","DARK_AQUA","BLUE","DARK_BLUE","LIGHT_PURPLE","DARK_PURPLE");
 public static final class Member { public String name,rank="Prueba"; public Member(){} Member(String n,String r){name=n;rank=r;} }
 public static final class Clan { public String name,initials,color,leader; public List<Member> members=new ArrayList<>(); public Clan(){} }
 private record Invite(String clan,String leader){}
 public static void init(MinecraftServer s){server=s;load();for(Clan c:C.values())OWN.add(legacy(c));sync();}
 private static Path file(){return server.getSavePath(WorldSavePath.ROOT).resolve("permadeath-clans.json");}
 private static String k(String s){return s.toLowerCase(Locale.ROOT);}
 private static boolean validColor(String s){return s!=null&&COLORS.contains(s.toUpperCase(Locale.ROOT));}
 private static void load(){try{Path p=file();if(Files.exists(p)){Clan[] a=G.fromJson(Files.readString(p),Clan[].class);C.clear();if(a!=null)for(Clan c:a)if(c!=null&&c.name!=null&&!c.name.isBlank()){if(c.initials==null||c.initials.isBlank())c.initials="CLN";if(!validColor(c.color))c.color="AQUA";if(c.members==null)c.members=new ArrayList<>();C.put(k(c.name),c);}}}catch(Exception e){System.err.println("[permadeath] Error cargando clanes: "+e);}}
 private static void save(){try{Files.writeString(file(),G.toJson(C.values()),StandardCharsets.UTF_8);}catch(IOException e){System.err.println("[permadeath] Error guardando clanes: "+e);}}
 private static Member member(Clan c,String n){if(c!=null&&c.members!=null)for(Member m:c.members)if(m!=null&&m.name!=null&&m.name.equalsIgnoreCase(n))return m;return null;}
 private static Clan of(String n){for(Clan c:C.values())if(member(c,n)!=null)return c;return null;}
 private static boolean leader(Clan c,ServerPlayerEntity p){return c!=null&&c.leader!=null&&c.leader.equalsIgnoreCase(p.getGameProfile().getName());}
 private static void say(ServerPlayerEntity p,String s){p.sendMessage(Text.literal("[Clanes] "+s),false);}
 public static void handle(ServerPlayerEntity p,String action,String raw){
  String v=raw==null?"":raw.trim(),n=p.getGameProfile().getName();String[] a=v.split("\\|",-1);Clan m=of(n);String msg="Acción no válida.";
  switch(action.toLowerCase(Locale.ROOT)){
   case "status" -> msg=m==null?"No pertenecés a ningún clan.":"Clan: "+m.name+" | Iniciales: "+m.initials+" | Color: "+m.color+" | Rango: "+member(m,n).rank+" | Miembros: "+m.members.size();
   case "create" -> {String cn=a.length>0?a[0].trim():"",tag=a.length>1?a[1].trim():"",col=a.length>2?a[2].trim().toUpperCase(Locale.ROOT):"AQUA";
    if(m!=null)msg="Primero tenés que salir de tu clan.";
    else if(cn.length()<3||cn.length()>20||!cn.matches("[\\p{L}0-9 _-]+")||tag.length()<1||tag.length()>4||!tag.matches("[\\p{L}0-9]+"))msg="Nombre: 3-20 caracteres; iniciales: 1-4 letras o números.";
    else if(C.containsKey(k(cn)))msg="Ya existe un clan con ese nombre.";
    else if(!validColor(col))msg="Color no válido.";
    else{Clan c=new Clan();c.name=cn;c.initials=tag.toUpperCase(Locale.ROOT);c.color=col;c.leader=n;c.members.add(new Member(n,"Líder"));C.put(k(cn),c);sync();save();msg="Creaste "+cn+". Sos el líder.";}}
   case "join" -> msg="La unión directa está desactivada: necesitás invitación del líder.";
   case "invite" -> {if(m==null||!leader(m,p))msg="Solo el líder puede invitar.";else{ServerPlayerEntity t=server.getPlayerManager().getPlayer(v);if(t==null)msg="El jugador debe estar conectado.";else if(of(v)!=null)msg="Ese jugador ya está en un clan.";else if(t.getUuid().equals(p.getUuid()))msg="No podés invitarte a vos mismo.";else{I.put(k(v),new Invite(k(m.name),n));t.sendMessage(Text.literal("Invitación al clan "+m.name+". Abrí /clan y aceptá o rechazá."),false);msg="Invitación enviada; aún no es miembro.";}}}
   case "accept" -> {Invite i=I.get(k(n));if(i==null)msg="No tenés invitaciones pendientes.";else{Clan c=C.get(i.clan());ServerPlayerEntity l=server.getPlayerManager().getPlayer(i.leader());if(c==null||l==null||!leader(c,l)){I.remove(k(n));msg="La invitación venció."; }else if(of(n)!=null){I.remove(k(n));msg="Ya pertenecés a un clan.";}else{c.members.add(new Member(n,"Prueba"));I.remove(k(n));sync();save();msg="Aceptaste la invitación a "+c.name+".";}}}
   case "reject" -> msg=I.remove(k(n))==null?"No tenés invitaciones pendientes.":"Invitación rechazada.";
   case "leave" -> {if(m==null)msg="No estás en un clan.";else if(leader(m,p)&&m.members.size()>1)msg="Transferí el liderazgo antes de salir.";else{String cn=m.name;m.members.removeIf(x->x.name.equalsIgnoreCase(n));if(m.members.isEmpty())C.remove(k(cn));sync();save();msg="Saliste del clan"+(m.members.isEmpty()?"; fue eliminado.":".");}}
   case "transfer" -> {if(m==null||!leader(m,p))msg="Solo el líder puede transferir el cargo.";else if(v.isBlank()||v.equalsIgnoreCase(n)||member(m,v)==null)msg="Indicá un miembro actual distinto de vos.";else{member(m,n).rank="Colíder";m.leader=member(m,v).name;member(m,v).rank="Líder";save();msg="Liderazgo transferido a "+m.leader+".";}}
   case "remove" -> {if(m==null||!leader(m,p))msg="Solo el líder puede expulsar.";else if(v.equalsIgnoreCase(m.leader))msg="No podés expulsar al líder.";else if(member(m,v)==null)msg="Ese jugador no pertenece al clan.";else{m.members.removeIf(x->x.name.equalsIgnoreCase(v));sync();save();msg="Miembro expulsado: "+v;}}
   case "promote" -> {if(m==null||!leader(m,p))msg="Solo el líder puede ascender.";else{Member x=member(m,v);if(x==null)msg="Ese jugador no pertenece al clan.";else if("Prueba".equals(x.rank)){x.rank="Oficial";save();msg=v+" ahora es Oficial.";}else if("Oficial".equals(x.rank)){x.rank="Colíder";save();msg=v+" ahora es Colíder.";}else msg="Ese miembro ya tiene el rango máximo.";}}
   case "delete" -> {if(m==null||!leader(m,p))msg="Solo el líder puede eliminar el clan.";else{String cn=m.name;C.remove(k(cn));I.entrySet().removeIf(e->e.getValue().clan().equals(k(cn)));sync();save();msg="Clan eliminado: "+cn;}}
   case "color" -> {String col=v.toUpperCase(Locale.ROOT);if(m==null||!leader(m,p))msg="Solo el líder puede cambiar el color.";else if(!validColor(col))msg="Color no válido.";else{m.color=col;sync();save();msg="Color actualizado.";}}
   default -> {}
  }say(p,msg);
 }
 private static String legacy(Clan c){return "pm_"+Integer.toHexString(k(c.name).hashCode());}
 private static void sync(){if(server==null)return;var sb=server.getScoreboard();for(String s:new HashSet<>(OWN)){Team t=sb.getTeam(s);if(t!=null)sb.removeTeam(t);}OWN.clear();Set<String> used=new HashSet<>();
  for(Clan c:C.values()){String base="permadeath_clan_"+Integer.toUnsignedString(k(c.name).hashCode(),36),tn=base;int i=2;while(used.contains(tn)||sb.getTeam(tn)!=null&&!OWN.contains(tn))tn=base+"_"+(i++);used.add(tn);Team t=sb.getTeam(tn);if(t==null)t=sb.addTeam(tn);OWN.add(tn);Formatting f;try{f=Formatting.valueOf(c.color.toUpperCase(Locale.ROOT));}catch(Exception e){f=Formatting.AQUA;}t.setPrefix(Text.literal("["+c.initials+"] ").formatted(f));t.setColor(f);for(Member x:c.members){Team old=sb.getScoreHolderTeam(x.name);if(old==null||old==t){if(old!=t)sb.addScoreHolderToTeam(x.name,t);}}}
 }
}
