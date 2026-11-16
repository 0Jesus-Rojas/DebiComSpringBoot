package com.debicomspringboot.exception;

public class ExcepcionRegistroUsuario extends RuntimeException {

    public enum Tipo {
        REFERENCIA_INVALIDA,
        CORREO_DUPLICADO,
        IDENTIFICACION_DUPLICADA,
        ERROR_BASE_DATOS
    }

    private final Tipo tipo;

    public ExcepcionRegistroUsuario(Tipo tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    public ExcepcionRegistroUsuario(Tipo tipo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }
}