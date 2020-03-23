package com.goros.moneycontrol;

public class Item {
    private String fecha;
    private String categoria;
    private String importe;

    public Item(String fecha, String categoria, String importe){
        this.fecha = fecha;
        this.categoria = categoria;
        this.importe = importe;
    }

    public String getFecha() {
        return fecha;
    }

    public String getImporte() {
        return importe;
    }

    public String getCategoria() {
        return categoria;
    }
}
