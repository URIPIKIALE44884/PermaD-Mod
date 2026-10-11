package com.permadeath.client;
import com.permadeath.ClanActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
public final class ClanScreen extends Screen {
 private TextFieldWidget field;
 public ClanScreen(){super(Text.literal("Clanes"));}
 @Override public boolean shouldPause(){return false;}
 @Override protected void init(){int x=(width-330)/2,y=(height-230)/2;
  field=new TextFieldWidget(textRenderer,x,y+25,330,20,Text.literal("Nombre|INICIALES|COLOR"));field.setMaxLength(60);field.setPlaceholder(Text.literal("Nombre, jugador o color"));addDrawableChild(field);
  addDrawableChild(ButtonWidget.builder(Text.literal("Crear"),b->send("create",field.getText().contains("|")?field.getText():field.getText()+"|CLN|AQUA")).dimensions(x,y+52,78,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Invitar"),b->send("invite",field.getText())).dimensions(x+84,y+52,78,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Aceptar invitación"),b->send("accept","")).dimensions(x+168,y+52,162,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Rechazar"),b->send("reject","")).dimensions(x,y+78,78,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Salir"),b->send("leave","")).dimensions(x+84,y+78,78,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Transferir liderazgo"),b->send("transfer",field.getText())).dimensions(x+168,y+78,162,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Expulsar miembro"),b->send("remove",field.getText())).dimensions(x,y+104,162,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Ascender"),b->send("promote",field.getText())).dimensions(x+168,y+104,162,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Cambiar color"),b->send("color",field.getText().toUpperCase())).dimensions(x,y+130,162,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Eliminar clan"),b->send("delete","")).dimensions(x+168,y+130,162,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Mi clan / estado"),b->send("status","")).dimensions(x,y+156,162,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"),b->close()).dimensions(x+168,y+156,162,20).build());
 }
 private void send(String a,String v){if(client!=null&&client.getNetworkHandler()!=null)ClientPlayNetworking.send(new ClanActionPayload(a,v));}
 @Override public void render(DrawContext c,int mx,int my,float d){renderBackground(c,mx,my,d);int x=(width-330)/2,y=(height-230)/2;c.drawCenteredTextWithShadow(textRenderer,title,width/2,y-2,0xFFFFFF);c.drawTextWithShadow(textRenderer,"Crear: Nombre|INICIALES|COLOR (Lobos|LOB|AQUA)",x,y+183,0xBBBBBB);c.drawTextWithShadow(textRenderer,"Colores: WHITE, GRAY, RED, GOLD, YELLOW, GREEN,",x,y+195,0xBBBBBB);c.drawTextWithShadow(textRenderer,"AQUA, BLUE, DARK_PURPLE, LIGHT_PURPLE, etc.",x,y+207,0xBBBBBB);super.render(c,mx,my,d);}
}
