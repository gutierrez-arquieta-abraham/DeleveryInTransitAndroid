package com.PolicodeLabs.Delevery_In_Transit.api;

import com.PolicodeLabs.Delevery_In_Transit.model.LoginRequest;
import com.PolicodeLabs.Delevery_In_Transit.model.NegocioDto;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoRequest;
import com.PolicodeLabs.Delevery_In_Transit.model.RegistroRequest;
import com.PolicodeLabs.Delevery_In_Transit.model.UbicacionDto;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioDto;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- AUTENTICACIÓN ---
    @POST("api/usuarios/login")
    Call<UsuarioResponse> login(@Body LoginRequest request);

    @POST("api/usuarios/registrar/repartidor")
    Call<UsuarioResponse> registrarRepartidor(@Body RegistroRequest request);

    // --- PEDIDOS ---
    @POST("api/pedidos")
    Call<PedidoDto> crearPedido(@Body PedidoRequest pedido);

    // UNIFICADO: Solo una versión de getPedidosPorNegocio
    @GET("api/pedidos/negocio/{idLicencia}")
    Call<List<PedidoDto>> getPedidosPorNegocio(@Path("idLicencia") int idLicencia);

    // --- ASIGNACIÓN ---
    @GET("api/usuarios/negocio/{idLicencia}/repartidores")
    Call<List<UsuarioResponse>> getRepartidoresPorNegocio(@Path("idLicencia") int idLicencia);

    @PUT("api/pedidos/{numOrd}/asignar/{idRepartidor}")
    Call<Void> asignarPedido(@Path("numOrd") int numOrd, @Path("idRepartidor") int idRepartidor);

    @GET("api/negocios/propietario")
    Call<NegocioDto> obtenerMiNegocio(@Query("email") String email);

    // --- UBICACIÓN (CORREGIDO: Solo una versión y con ResponseBody) ---
    @GET("api/ubicacion/activos")
    Call<List<UbicacionDto>> getRepartidoresActivos();

    @POST("api/usuarios/actualizar-ubicacion")
    Call<ResponseBody> actualizarUbicacion(
            @Query("idRepartidor") int idRepartidor, // Nota: Tu backend usa @RequestParam ("idRepartidor"), así que en Retrofit es @Query
            @Query("latitud") double latitud,
            @Query("longitud") double longitud
    );

    @GET("/api/usuarios/repartidores/{idLicencia}")
    Call<List<UsuarioDto>> obtenerRepartidoresPorNegocio(@Path("idLicencia") int idLicencia);

    // --- HISTORIAL ---
    @GET("/api/pedidos/negocio/{id}/historial")
    Call<List<PedidoDto>> obtenerHistorialNegocio(@Path("id") int idLicencia);

    @GET("/api/pedidos/repartidor/{id}/historial")
    Call<List<PedidoDto>> obtenerHistorial(@Path("id") int idRepartidor);

    // --- NEGOCIO ---
    @PUT("api/negocios/{id}")
    Call<NegocioDto> actualizarNegocio(@Path("id") int id, @Body NegocioDto negocio);

    @GET("api/negocios/codigo/{codigo}")
    Call<Integer> validarCodigoLicencia(@Path("codigo") String codigo);

    @POST("api/usuarios/vincular-equipo")
    Call<UsuarioDto> unirseAEquipo(@Query("idUsuario") Integer idUsuario, @Query("codigo") String codigo);

    // --- MIS PEDIDOS ---
    @GET("api/pedidos/mis-pedidos")
    Call<List<PedidoDto>> obtenerMisPedidos(@Query("idRepartidor") Integer idRepartidor);

    // --- ACTUALIZAR ESTADO (CORREGIDO) ---
    // Usamos @POST sin @FormUrlEncoded porque tu backend espera @RequestParam en la URL, no en el cuerpo form-data
    @POST("api/pedidos/actualizar-estado")
    Call<PedidoDto> actualizarEstadoPedido(
            @Query("numOrd") int idPedido,
            @Query("nuevoEstado") String estado
    );

    @POST("api/usuarios/actualizar-estatus")
    Call<UsuarioDto> actualizarEstatus(@Query("idUsuario") Integer idUsuario, @Query("nuevoEstatus") String nuevoEstatus);
}