package com.permadeath.client;

import com.permadeath.ClanActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Interfaz sencilla de clanes. El servidor valida todos los permisos. */
public final class ClanScreen extends Screen {
 private TextFieldWidget field;
 public ClanScreen(){super(Text.literal("Clanes"));}
 @Override public boolean shouldPause(){return false;}
 @Override protected void init(){int x=(width-300)/2,y=(height-190)/2;
  field=new TextFieldWidget(textRenderer,x,y+25,300,20,Text.literal("Nombre|INICIALES|COLOR"));field.setMaxLength(60);field.setPlaceholder(Text.literal("Nombre del clan o jugador"));addDrawableChild(field);
  addDrawableChild(ButtonWidget.builder(Text.literal("Crear"),b->send("create",field.getText().contains("|")?field.getText():field.getText()+"|CLN|AQUA")).dimensions(x,y+52,70,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Unirse"),b->send("join",field.getText())).dimensions(x+75,y+52,70,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Salir"),b->send("leave","")).dimensions(x+150,y+52,70,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Invitar"),b->send("invite",field.getText())).dimensions(x+225,y+52,75,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Eliminar miembro"),b->send("remove",field.getText())).dimensions(x,y+78,145,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Ascender"),b->send("promote",field.getText())).dimensions(x+150,y+78,145,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Cambiar color"),b->send("color",field.getText().toUpperCase())).dimensions(x,y+104,145,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Eliminar clan"),b->send("delete","")).dimensions(x+150,y+104,145,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Mi clan / estado"),b->send("status","")).dimensions(x,y+130,145,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"),b->close()).dimensions(x+150,y+130,145,20).build());
 }
 private void send(String action,String value){if(client!=null&&client.getNetworkHandler()!=null)ClientPlayNetworking.send(new ClanActionPayload(action,value));}
 @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta){renderBackground(ctx);int x=(width-300)/2,y=(height-190)/2;ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,y-2,0xFFFFFF);ctx.drawTextWithShadow(textRenderer,"Crear: escribe Nombre|INICIALES|COLOR (ej. Lobos|LOB|AQUA)",x,y+158,0xBBBBBB);ctx.drawTextWithShadow(textRenderer,"Nuevos miembros entran como Prueba. Color: AQUA, RED, GOLD...",x,y+170,0xBBBBBB);super.render(ctx,mouseX,mouseY,delta);}
}
